package ru.threedisevenzeror.marionette;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import ru.threedisevenzeror.marionette.model.fceux.FceuxEmulationSpeed;
import ru.threedisevenzeror.marionette.model.packets.fceux.FceuxMemoryDump;
import ru.threedisevenzeror.marionette.model.packets.fceux.FceuxSetSettingsMessage;
import ru.threedisevenzeror.marionette.model.packets.fceux.FceuxShowMessageMessage;
import ru.threedisevenzeror.marionette.model.packets.generic.ClientInfoMessage;
import ru.threedisevenzeror.marionette.model.packets.generic.ConnectionClosedMessage;
import ru.threedisevenzeror.marionette.model.packets.generic.PingPongMessage;
import ru.threedisevenzeror.marionette.network.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Main {

    public static String fceuxPath =
            "E:\\Consoles\\Nintentdo Entertainment System, Floppy Disk System\\Emulators\\Fceux";

    public static void main(String[] args) throws IOException, InterruptedException {

        MessageMapping protocol = new MessageMapping();
        protocol.registerPacket(0x00, FceuxSetSettingsMessage.class);
        protocol.registerPacket(0x01, FceuxShowMessageMessage.class);
        protocol.registerPacket(0x02, FceuxMemoryDump.class);

        protocol.registerPacket(0xff, ClientInfoMessage.class);
        protocol.registerPacket(0xfe, PingPongMessage.class);
        protocol.registerPacket(0xfd, ConnectionClosedMessage.class);

        EventLoopGroup group = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(group);
        serverBootstrap.childOption(ChannelOption.AUTO_READ, true);
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
                                .addMessageHandler(FceuxMemoryDump.class, (context, p) -> {
                                    System.out.println(p.ram.length);
                                })
                                .addMessageHandler(ClientInfoMessage.class, (context, packet) -> {
                                    System.out.println("Client connected: " + packet.clientName);
                                })
                );

                FceuxSetSettingsMessage m = new FceuxSetSettingsMessage();
                m.emulationSpeed = FceuxEmulationSpeed.Maximum;
                m.emulationPeriods = 1500;
                m.socketTimeout = 10000;
                m.showDebuggingInfo = true;
                ch.writeAndFlush(m);

                FceuxShowMessageMessage p = new FceuxShowMessageMessage();
                p.message = "Test message";
                p.displayTimeMiliseconds = 3200;
                ch.writeAndFlush(p);
            }
        });

        ChannelFuture bindChannel = serverBootstrap.bind(34710)
                .sync();

        launchFceuxInstances();

        bindChannel.channel()
                .closeFuture()
                .sync();
    }

    private static List<Process> launchFceuxInstances() throws IOException {
        int cpuCount = Runtime.getRuntime().availableProcessors();
        List<Process> processes = new ArrayList<>();

        for(int i = 0; i < cpuCount; i++) {
            int affinity = (int) Math.pow(2, i);

            Process process = launchFceuxWindows(new File(fceuxPath),
                    new File("E:\\Development\\IdeaProjects\\marionette\\src\\main\\lua\\client.lua"),
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
