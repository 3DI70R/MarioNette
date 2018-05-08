package ru.threedisevenzeror.marionette.model.packets.fceux.in;

import ru.threedisevenzeror.marionette.model.packets.base.InPacket;
import ru.threedisevenzeror.marionette.utils.DataUtils;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Packet which is sent by FCEUX when it is disconnected
 */
public class FceuxEmuDisconnectedPacket implements InPacket {

    /**
     * Reason of disconnect
     */
    public String reason;

    @Override
    public void read(DataInputStream stream) throws IOException {
        reason = DataUtils.readNullTerminatedString(stream);
    }

    @Override
    public byte getType() {
        return 0x01;
    }
}
