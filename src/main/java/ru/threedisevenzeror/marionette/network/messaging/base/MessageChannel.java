package ru.threedisevenzeror.marionette.network.messaging.base;

import ru.threedisevenzeror.marionette.model.packets.base.InPacket;
import ru.threedisevenzeror.marionette.model.packets.base.OutPacket;

/**
 * Messaging channel between server and client
 */
public interface MessageChannel  {

    /**
     * Gets name of this message channel
     */
    String getName();

    /**
     * Gets type of client for this channel
     */
    String getClientType();

    /**
     * Forcefully disconnects from this channel
     */
    void disconnect();

    /**
     * Sends packet to client
     * @param packet packet instance with filled data
     */
    void sendPacket(OutPacket packet);

    /**
     * Register packet listener, which will be invoked, when packet is received from client
     * @param emptyPacket empty packet instance to put data from. Packet will be reused every miliseconds message is received
     * @param listener Listener which will handle packet receive logic
     * @param <T> Packet type
     */
    <T extends InPacket> void addPacketListener(T emptyPacket, OnPacketReceivedListener<T> listener);

    /**
     * Registers listener which will be invoked after channel is closed
     */
    void addOnChannelClosedListener(OnChannelClosedListener listener);
}
