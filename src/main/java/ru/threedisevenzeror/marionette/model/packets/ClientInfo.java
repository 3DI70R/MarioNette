package ru.threedisevenzeror.marionette.model.packets;

import ru.threedisevenzeror.marionette.model.packets.base.InPacket;
import ru.threedisevenzeror.marionette.utils.DataUtils;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Packet which is sent from every connecting client
 */
public class ClientInfo implements InPacket {

    public String clientName;
    public String clientType;

    @Override
    public void read(DataInputStream stream) throws IOException {
        clientName = DataUtils.readNullTerminatedString(stream);
        clientType = DataUtils.readNullTerminatedString(stream);
    }

    @Override
    public byte getType() {
        return (byte) 0xff;
    }
}
