package ru.threedisevenzeror.marionette.model.packets.generic;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Ping packet
 * Sended to channel to receive response and ensure that client is reachable
 */
public class PingPongMessage implements KryoSerializable {

    /**
     * Flag indicating that client which receives this packet should send
     * Pong packet back with this flag set to false
     */
    public boolean isPingPacket;

    public PingPongMessage() {
        // for kryo
    }

    public PingPongMessage(boolean isPingPacket) {
        this.isPingPacket = isPingPacket;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeBoolean(isPingPacket);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        isPingPacket = input.readBoolean();
    }
}
