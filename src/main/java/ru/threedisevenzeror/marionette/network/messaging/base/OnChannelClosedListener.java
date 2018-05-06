package ru.threedisevenzeror.marionette.network.messaging.base;

public interface OnChannelClosedListener {

    /**
     * Method that will be invoked when message channel is closed
     */
    void onChannelClosed(MessageChannel channel);
}
