package ru.threedisevenzeror.marionette.model.message.generic;

import io.netty.buffer.ByteBuf;
import ru.threedisevenzeror.marionette.model.message.NetworkObject;

/**
 * Ping packet
 * Sended to channel to receive response and ensure that client is reachable
 */
public class PingPongMessage implements NetworkObject {

    /**
     * Flag indicating that client which receives this packet should send
     * Pong packet back with this flag set to false
     */
    public boolean isPingPacket;

    public PingPongMessage() {}
    public PingPongMessage(boolean isPingPacket) {
        this.isPingPacket = isPingPacket;
    }

    @Override
    public void write(ByteBuf output) {
        output.writeBoolean(isPingPacket);
    }

    @Override
    public void read(ByteBuf input) {
        isPingPacket = input.readBoolean();
    }
}
