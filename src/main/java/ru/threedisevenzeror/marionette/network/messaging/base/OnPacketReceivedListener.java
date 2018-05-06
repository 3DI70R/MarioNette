package ru.threedisevenzeror.marionette.network.messaging.base;

public interface OnPacketReceivedListener<T> {

    /**
     * Method that will be invoked when packet
     * @param packet
     */
    void onPacketReceived(T packet);
}
