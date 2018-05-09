package ru.threedisevenzeror.marionette.model.packets.generic;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import ru.threedisevenzeror.marionette.utils.StreamUtils;

/**
 * Packet which is sent across network when client is disconnected,
 * or server wants client to disconnect
 */
public class ConnectionClosedMessage implements KryoSerializable {

    /**
     * Reason of disconnect
     */
    public String reason;

    public ConnectionClosedMessage() {
        // for kryo
    }

    public ConnectionClosedMessage(String reason) {
        this.reason = reason;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        StreamUtils.writeNullTerminatedString(output, reason);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        reason = StreamUtils.readNullTerminatedString(input);
    }
}
