package ru.threedisevenzeror.marionette.model.packets.generic;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import ru.threedisevenzeror.marionette.utils.StreamUtils;

/**
 * Packet which is sent from every connecting client
 */
public class ClientInfoMessage implements KryoSerializable {

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
    public void write(Kryo kryo, Output output) {
        StreamUtils.writeNullTerminatedString(output, protocolVersion);
        StreamUtils.writeNullTerminatedString(output, clientName);
        StreamUtils.writeNullTerminatedString(output, clientType);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        protocolVersion = StreamUtils.readNullTerminatedString(input);
        clientName = StreamUtils.readNullTerminatedString(input);
        clientType = StreamUtils.readNullTerminatedString(input);
    }
}
