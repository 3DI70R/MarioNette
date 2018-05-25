package ru.threedisevenzeror.marionette.model.message;

import io.netty.buffer.ByteBuf;

/**
 * Network message
 * Object that can be sent and received through network
 */
public interface NetworkObject {

    /**
     * Writes message to output stream
     */
    void write(ByteBuf output);

    /**
     * Reads message from input stream
     */
    void read(ByteBuf input);
}
