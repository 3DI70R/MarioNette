package ru.threedisevenzeror.marionette.model.packets.base;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Packet that can be sent to socket
 */
public interface OutPacket extends Packet  {

    /**
     * Writes packet content to output stream
     */
    void write(DataOutputStream stream) throws IOException;
}
