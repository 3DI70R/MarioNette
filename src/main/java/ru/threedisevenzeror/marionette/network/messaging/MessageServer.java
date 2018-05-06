package ru.threedisevenzeror.marionette.network.messaging;

import ru.threedisevenzeror.marionette.network.messaging.base.MessageChannel;
import ru.threedisevenzeror.marionette.network.messaging.base.OnClientConnectedListener;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class MessageServer {

    private ServerSocket serverSocket;
    private ExecutorService connectionThread;
    private ScheduledExecutorService messageThread;
    private long messageCheckPeriod;
    private TimeUnit messagecheckTimeUnit;

    private Future<?> connectionThreadTask;
    private ScheduledFuture<?> messageThreadTask;
    private OnClientConnectedListener clientListener;

    private final List<MessageChannelImpl> messageChannels;

    public MessageServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);

        messageCheckPeriod = 250;
        messagecheckTimeUnit = TimeUnit.MILLISECONDS;

        connectionThread = Executors.newSingleThreadExecutor();
        messageThread = Executors.newSingleThreadScheduledExecutor();
        messageChannels = new ArrayList<>();
    }

    public int getConnectedClientsCount() {
        return messageChannels.size();
    }

    public List<MessageChannel> getConnectedClients() {
        return new ArrayList<>(messageChannels);
    }

    public void setClientListener(OnClientConnectedListener clientListener) {
        this.clientListener = clientListener;
    }

    public void startServer() {
        System.out.println("Starting server of Marionette on port " + serverSocket.getLocalPort());
        startAcceptingNewConnections();
    }

    public void stopServer() {
        stopAcceptingNewConnections();
        stopMessageProcessing();
    }

    private void startAcceptingNewConnections() {

        if(connectionThreadTask != null) {
            return;
        }

        connectionThreadTask = connectionThread.submit(() -> {
            while (true) {
                Socket client = serverSocket.accept();

                try {
                    MessageChannelImpl channel = new MessageChannelImpl(client, messageThread);
                    channel.handshake();

                    channel.addOnChannelClosedListener(c ->
                            onClientDisconnected(channel));

                    onNewClientConnected(channel);

                    System.out.println("New " + channel.getClientType() + " client connected: "
                            + "[" + client.getInetAddress().toString() + "] "
                            + channel.getName());

                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Handshake with client "
                            + client.getInetAddress().toString()
                            + " was unsuccessful");
                }
            }
        });
    }

    private void stopAcceptingNewConnections() {
        if(connectionThreadTask != null) {
            connectionThreadTask.cancel(true);
        }
    }

    private void startMessageProcessing() {

        if(messageThreadTask != null) {
            return;
        }

        messageThreadTask = messageThread.scheduleAtFixedRate(() -> {

            synchronized (messageChannels) {
                for(int i = messageChannels.size() - 1; i >= 0; i--) {
                    MessageChannelImpl channel = messageChannels.get(i);

                    if(channel.isAlive()) {
                        channel.receivePacketIfAvailable();
                        continue;
                    }
                }
            }

        }, 0, messageCheckPeriod, messagecheckTimeUnit);
    }

    private void stopMessageProcessing() {
        if(messageThreadTask != null) {
            messageThreadTask.cancel(false);
        }
    }

    private void onNewClientConnected(MessageChannelImpl channel) {

        if(clientListener != null) {
            clientListener.onClientConnected(channel);
        }

        startMessageProcessing();

        synchronized (messageChannels) {
            messageChannels.add(channel);
        }
    }

    private void onClientDisconnected(MessageChannelImpl channel) {
        if(getConnectedClientsCount() == 0) {
            stopMessageProcessing();
        }

        synchronized (messageChannels) {
            messageChannels.remove(channel);
            System.out.println("Client " + channel.getName() + " is disconnected");
        }
    }
}
