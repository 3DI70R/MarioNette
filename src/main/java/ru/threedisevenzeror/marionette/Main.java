package ru.threedisevenzeror.marionette;

import ru.threedisevenzeror.marionette.logic.NeuralEvaluatorBalancer;
import ru.threedisevenzeror.marionette.model.fceux.EmulationSpeed;
import ru.threedisevenzeror.marionette.logic.FceuxNeuralEvaluator;
import ru.threedisevenzeror.marionette.network.messaging.MessageServer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static String fceuxPath =
            "E:\\Consoles\\Nintentdo Entertainment System, Floppy Disk System\\Emulators\\Fceux";

    public static void main(String[] args) throws IOException, InterruptedException {
        MessageServer server = new MessageServer(34710);
        NeuralEvaluatorBalancer balancer = new NeuralEvaluatorBalancer();

        server.setClientListener(c -> {
            FceuxNeuralEvaluator fceuxClient = new FceuxNeuralEvaluator(c);
            fceuxClient.setEmulationSpeed(EmulationSpeed.Maximum);

            balancer.addEvaluator(fceuxClient);
        });

        server.startServer();
        launchFceuxInstances();
    }

    private static List<Process> launchFceuxInstances() throws IOException {
        int cpuCount = Runtime.getRuntime().availableProcessors();
        List<Process> processes = new ArrayList<>();

        for(int i = 0; i < cpuCount; i++) {
            int affinity = (int) Math.pow(2, i);
            Process process = launchFceuxWindows(new File(fceuxPath),
                    new File("E:\\Development\\IdeaProjects\\marionette\\src\\main\\lua\\marionette.lua"),
                    new File(fceuxPath + "\\smb.nes"),
                    affinity);

            processes.add(process);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for(Process p : processes) {
                p.destroy();
            }
        }));

        return processes;
    }

    private static Process launchFceuxWindows(File emulatorPath, File luaPath, File romPath, int affinity) throws IOException {
        return new ProcessBuilder("cmd", "/c",
                "start",
                    "/affinity", Integer.toHexString(affinity).toUpperCase(),
                    "/wait",
                "fceux",
                    "-lua", luaPath.getPath(),
                    "-turbo", "1",
                    romPath.getPath())
                .directory(emulatorPath)
                .start();
    }
}
