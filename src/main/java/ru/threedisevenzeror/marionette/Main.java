package ru.threedisevenzeror.marionette;

import ru.threedisevenzeror.marionette.model.fceux.EmulationSpeed;
import ru.threedisevenzeror.marionette.model.packets.fceux.in.FceuxEmuDisconnectedPacket;
import ru.threedisevenzeror.marionette.model.packets.fceux.out.FceuxSetSettingsPacket;
import ru.threedisevenzeror.marionette.model.packets.fceux.out.FceuxShowMessagePacket;
import ru.threedisevenzeror.marionette.network.messaging.MessageServer;
import ru.threedisevenzeror.marionette.network.messaging.base.MessageChannel;
import ru.threedisevenzeror.marionette.network.messaging.base.OnChannelClosedListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static String fceuxPath =
            "E:\\Consoles\\Nintentdo Entertainment System, Floppy Disk System\\Emulators\\Fceux";

    public static void main(String[] args) throws IOException, InterruptedException {
        MessageServer server = new MessageServer(34710);
        server.setClientListener(c -> {

            FceuxSetSettingsPacket packet = new FceuxSetSettingsPacket();
            packet.emulationPeriods = 1500;
            packet.socketTimeout = 30000;
            packet.emulationSpeed = EmulationSpeed.Nothrottle;
            packet.debuggingInfo = true;

            c.sendPacket(packet);

            c.addPacketListener(new FceuxEmuDisconnectedPacket(), p -> {
                c.disconnect();
                System.out.println(p.reason);
            });

            c.sendPacket(new FceuxShowMessagePacket() {{
                message = "Test message";
            }});
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
