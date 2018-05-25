package ru.threedisevenzeror.marionette;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import org.encog.ml.CalculateScore;
import org.encog.ml.MLMethod;
import org.encog.ml.ea.train.basic.TrainEA;
import org.encog.neural.neat.NEATLink;
import org.encog.neural.neat.NEATNetwork;
import org.encog.neural.neat.NEATPopulation;
import org.encog.neural.neat.NEATUtil;
import ru.threedisevenzeror.marionette.model.fceux.FceuxEmulationSpeed;
import ru.threedisevenzeror.marionette.model.message.fceux.*;
import ru.threedisevenzeror.marionette.model.message.generic.ClientInfoMessage;
import ru.threedisevenzeror.marionette.model.message.generic.ConnectionClosedMessage;
import ru.threedisevenzeror.marionette.model.message.generic.PingPongMessage;
import ru.threedisevenzeror.marionette.model.neatevolve.Genome;
import ru.threedisevenzeror.marionette.network.MessageDecoder;
import ru.threedisevenzeror.marionette.network.MessageEncoder;
import ru.threedisevenzeror.marionette.network.MessageHandler;
import ru.threedisevenzeror.marionette.network.MessageMapping;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Test {

    public static String fceuxPath =
            "E:\\Consoles\\Nintentdo Entertainment System, Floppy Disk System\\Emulators\\Fceux";

    private static final Object monitor = new Object();
    private static FceuxEvaluationResult result;
    private static float maxFitness;

    public static void main(String[] args) throws IOException, InterruptedException {

        MessageMapping protocol = new MessageMapping();
        protocol.registerPacket(0x00, FceuxSetSettingsMessage.class, FceuxSetSettingsMessage::new);
        protocol.registerPacket(0x01, FceuxShowMessageMessage.class, FceuxShowMessageMessage::new);
        protocol.registerPacket(0x02, FceuxMemoryDump.class, FceuxMemoryDump::new);
        protocol.registerPacket(0x03, FceuxEvaluateNetwork.class, FceuxEvaluateNetwork::new);
        protocol.registerPacket(0x04, FceuxEvaluationResult.class, FceuxEvaluationResult::new);

        protocol.registerPacket(0xfe, ClientInfoMessage.class, ClientInfoMessage::new);
        protocol.registerPacket(0xfd, PingPongMessage.class, PingPongMessage::new);
        protocol.registerPacket(0xfc, ConnectionClosedMessage.class, ConnectionClosedMessage::new);

        EventLoopGroup group = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(group);
        serverBootstrap.channelFactory(NioServerSocketChannel::new);
        serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(
                        new IdleStateHandler(30, 30,30, TimeUnit.SECONDS),

                        new LengthFieldBasedFrameDecoder(8192 * 1024,0,
                                4, 0, 4),
                        new MessageDecoder(protocol),

                        new LengthFieldPrepender(4),
                        new MessageEncoder(protocol),

                        new MessageHandler()
                                .addMessageHandler(FceuxEvaluationResult.class, (context, packet) -> {
                                    synchronized (monitor) {
                                        monitor.notify();
                                    }

                                    result = packet;
                                })
                                .addMessageHandler(ClientInfoMessage.class, (context, packet) ->
                                        System.out.println("Client connected: " + packet.clientName))
                );

                ch.writeAndFlush(new FceuxSetSettingsMessage() {{
                    emulationSpeed = FceuxEmulationSpeed.Nothrottle;
                    socketTimeout = 10000;
                    emulationPeriods = 50;
                    showDebuggingInfo = true;
                }});

                new Thread(() -> startTraining(ch)).start();
            }
        });

        ChannelFuture bindChannel = serverBootstrap
                .bind(34710)
                .sync();

        launchFceuxInstances();

        bindChannel.channel()
                .closeFuture()
                .sync();
    }

    private static int index = 0;

    private static void startTraining(SocketChannel channel) {
        NEATPopulation population = new NEATPopulation(169, 6, 300);
        population.setInitialConnectionDensity(0.0015f);
        population.setActivationCycles(1);
        population.createSpecies();
        population.createSpecies();
        population.createSpecies();
        population.createSpecies();
        population.createSpecies();
        population.createSpecies();
        population.reset();

        TrainEA trainer = NEATUtil.constructNEATTrainer(population, new CalculateScore() {
            @Override
            public double calculateScore(MLMethod method) {

                Genome def = new Genome();
                NEATNetwork network = (NEATNetwork) method;

                def.id = index++;
                def.description = "Net#" + def.id;
                def.neuronCount =  network.getPostActivation().length;
                def.inputCount = network.getInputCount();
                def.outputCount = network.getOutputCount();

                for (NEATLink l : network.getLinks()) {
                    def.links.add(new Genome.Link(l.getToNeuron(), l.getFromNeuron(), (float) l.getWeight()));
                }

                channel.writeAndFlush(new FceuxEvaluateNetwork(def));
                try {
                    synchronized (monitor) {
                        monitor.wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if(result.fitness > maxFitness) {
                    maxFitness = result.fitness;

                    System.out.println("Network #" + def.id + " is new top: " + result.fitness);
                }

                return result.fitness;
            }

            @Override
            public boolean shouldMinimize() {
                return false;
            }

            @Override
            public boolean requireSingleThreaded() {
                return false;
            }
        });

        trainer.setThreadCount(1);

        while (true) {
            System.out.println("New iteration");
            trainer.iteration();
        }
    }

    private static List<Process> launchFceuxInstances() throws IOException {
        int cpuCount = 1;//Runtime.getRuntime().availableProcessors();
        List<Process> processes = new ArrayList<>();

        for(int i = 0; i < cpuCount; i++) {
            int affinity = (int) Math.pow(2, i);

            Process process = launchFceuxWindows(new File(fceuxPath),
                    new File("E:\\Development\\IdeaProjects\\marionette\\src\\main\\lua\\marionette.lua"),
                    new File(fceuxPath + "\\smb.nes"),
                    affinity);

            /*Process process = launchFceuxLinux(
                    new File("/home/threedisevenzeror/Projects/Idea/MarioNette/src/main/lua/client.lua"),
                    new File("/home/threedisevenzeror/Projects/Emu/Roms/Super Mario Bros (W) [!].nes"),
                    affinity
            );*/

            processes.add(process);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for(Process p : processes) {
                p.destroy();
            }
        }));

        return processes;
    }

    private static Process launchFceuxLinux(File luaPath, File romPath, int affinity) throws IOException {
        return new ProcessBuilder("taskset", "0x" + Integer.toHexString(affinity).toUpperCase(),
                "fceux",
                    "--loadlua", luaPath.getPath(),
                    romPath.getPath())
                .start();
    }

    private static Process launchFceuxWindows(File emulatorPath, File luaPath, File romPath, int affinity) throws IOException {
        return new ProcessBuilder("cmd", "/c",
                "start",
                    "/affinity", Integer.toHexString(affinity).toUpperCase(),
                    "/wait",
                "fceux",
                    "-lua", luaPath.getPath(),
                    romPath.getPath())
                .directory(emulatorPath)
                .start();
    }
}
