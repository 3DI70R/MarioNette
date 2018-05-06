package ru.threedisevenzeror.marionette.model.packets.base;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Packet which can be received from socket
 */
public interface InPacket extends Packet {

    /**
     * Reads message from output stream
     */
    void read(DataInputStream stream) throws IOException;
}
