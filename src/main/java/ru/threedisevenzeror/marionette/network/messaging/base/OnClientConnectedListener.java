package ru.threedisevenzeror.marionette.network.messaging.base;

public interface OnClientConnectedListener {

    /**
     * Method that will be invoked when there is a new connection
     */
    void onClientConnected(MessageChannel channel);
}
