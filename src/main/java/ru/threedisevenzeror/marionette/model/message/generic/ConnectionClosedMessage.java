package ru.threedisevenzeror.marionette.model.message.generic;

import io.netty.buffer.ByteBuf;
import ru.threedisevenzeror.marionette.model.message.NetworkObject;
import ru.threedisevenzeror.marionette.utils.StreamUtils;

/**
 * Packet which is sent across network when client is disconnected,
 * or server wants client to disconnect
 */
public class ConnectionClosedMessage implements NetworkObject {

    /**
     * Reason of disconnect
     */
    public String reason;

    public ConnectionClosedMessage() { }
    public ConnectionClosedMessage(String reason) {
        this.reason = reason;
    }

    @Override
    public void write(ByteBuf output) {
        StreamUtils.writeNullTerminatedString(output, reason);
    }

    @Override
    public void read(ByteBuf input) {
        reason = StreamUtils.readNullTerminatedString(input);
    }
}
