package ru.threedisevenzeror.marionette.model.message.fceux;

import io.netty.buffer.ByteBuf;
import ru.threedisevenzeror.marionette.model.message.NetworkObject;

/**
 * Information about evaluated network
 */
public class FceuxEvaluationResult implements NetworkObject {

    /**
     * Id of network, which this result belongs to
     */
    public long networkId;

    /**
     * Result fitness of evaluated network
     */
    public float fitness;

    @Override
    public void write(ByteBuf output) {
        output.writeLong(networkId);
        output.writeFloat(fitness);
    }

    @Override
    public void read(ByteBuf input) {
        networkId = input.readLong();
        fitness = input.readFloat();
    }
}
