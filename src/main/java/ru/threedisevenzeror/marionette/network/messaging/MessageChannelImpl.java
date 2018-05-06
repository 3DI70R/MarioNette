package ru.threedisevenzeror.marionette.network.messaging;

import ru.threedisevenzeror.marionette.model.packets.ClientInfo;
import ru.threedisevenzeror.marionette.model.packets.base.InPacket;
import ru.threedisevenzeror.marionette.model.packets.base.OutPacket;
import ru.threedisevenzeror.marionette.network.messaging.base.MessageChannel;
import ru.threedisevenzeror.marionette.network.messaging.base.OnChannelClosedListener;
import ru.threedisevenzeror.marionette.network.messaging.base.OnPacketReceivedListener;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

class MessageChannelImpl implements MessageChannel {

    private interface PacketHandler {
        void handlePacket(DataInputStream stream) throws IOException;
    }

    private Socket socket;
    private DataInputStream inStream;
    private DataOutputStream outStream;
    private ScheduledExecutorService messageThread;
    private boolean isAlive;
    private String clientName;
    private String clientType;

    private final List<OnChannelClosedListener> channelClosedListener;
    private final Map<Byte, List<PacketHandler>> packetHandlers;

    MessageChannelImpl(Socket socket, ScheduledExecutorService messageThread) throws IOException {
        this.socket = socket;
        this.messageThread = messageThread;

        isAlive = true;

        packetHandlers = new HashMap<>();
        channelClosedListener = new ArrayList<>();

        inStream = new DataInputStream(socket.getInputStream());
        outStream = new DataOutputStream(socket.getOutputStream());

        addPacketListener(new ClientInfo(), c -> {
            clientName = c.clientName;
            clientType = c.clientType;
        });
    }

    @Override
    public String getName() {
        return clientName;
    }

    @Override
    public String getClientType() {
        return clientType;
    }

    @Override
    public void disconnect() {
        try {
            socket.close();
        } catch (IOException ignored) {
            // noop
        }

        isAlive = false;

        synchronized (channelClosedListener) {
            for (OnChannelClosedListener listener : channelClosedListener) {
                listener.onChannelClosed(this);
            }
        }
    }

    Socket getSocket() {
        return socket;
    }

    boolean isAlive() {
        return isAlive;
    }

    public void sendPacket(OutPacket packet) {
        messageThread.submit(() -> {
            try {
                outStream.writeByte(packet.getType());
                packet.write(outStream);
                outStream.flush();
            } catch (Exception e) {
                onConnectionException(e);
            }
        });
    }

    @Override
    public void addOnChannelClosedListener(OnChannelClosedListener listener) {
        channelClosedListener.add(listener);
    }

    @Override
    public <T extends InPacket> void addPacketListener(T instance, OnPacketReceivedListener<T> listener) {
        synchronized (packetHandlers) {
            packetHandlers
                    .computeIfAbsent(instance.getType(), k -> new ArrayList<>())
                    .add(stream -> {
                        instance.read(stream);
                        listener.onPacketReceived(instance);
                    });
        }
    }

    void receivePacketIfAvailable() {
        try {
            while (inStream.available() != 0) {
                receivePacket();
            }
        } catch (IOException e) {
            onConnectionException(e);
        }
    }

    void receivePacket() throws IOException {
        byte packetType = inStream.readByte();
        List<PacketHandler> handlers = packetHandlers.get(packetType);

        if(handlers == null) {
            throw new IllegalArgumentException("Unknown packet with id: " + packetType);
        } else {
            synchronized (handlers) {
                for (PacketHandler h : handlers) {
                    h.handlePacket(inStream);
                }
            }
        }
    }

    void handshake() throws IOException {
        receivePacket();
    }

    private void onConnectionException(Exception e) {
        e.printStackTrace();
        disconnect();
    }
}
