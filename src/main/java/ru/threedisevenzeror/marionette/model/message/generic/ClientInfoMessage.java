package ru.threedisevenzeror.marionette.model.message.generic;

import io.netty.buffer.ByteBuf;
import ru.threedisevenzeror.marionette.model.message.NetworkObject;
import ru.threedisevenzeror.marionette.utils.StreamUtils;

/**
 * Packet which is sent from every connecting client
 */
public class ClientInfoMessage implements NetworkObject {

    /**
     * Protocol version of this client
     */
    public String protocolVersion;

    /**
     * Name of this client
     */
    public String clientName;

    /**
     * Type of this client
     */
    public String clientType;

    @Override
    public void write(ByteBuf output) {
        StreamUtils.writeNullTerminatedString(output, protocolVersion);
        StreamUtils.writeNullTerminatedString(output, clientName);
        StreamUtils.writeNullTerminatedString(output, clientType);
    }

    @Override
    public void read(ByteBuf input) {
        protocolVersion = StreamUtils.readNullTerminatedString(input);
        clientName = StreamUtils.readNullTerminatedString(input);
        clientType = StreamUtils.readNullTerminatedString(input);
    }
}
