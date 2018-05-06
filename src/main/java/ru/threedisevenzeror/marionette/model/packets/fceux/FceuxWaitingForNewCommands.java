package ru.threedisevenzeror.marionette.model.packets.fceux;

import ru.threedisevenzeror.marionette.model.packets.base.InPacket;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Packet which is sent by FCEUX, after he is ready to receive another pack of commands
 * Acts like a heartbeat packet
 */
public class FceuxWaitingForNewCommands implements InPacket {

    @Override
    public void read(DataInputStream stream) throws IOException {
        // noop
    }

    @Override
    public byte getType() {
        return 0x00;
    }
}
