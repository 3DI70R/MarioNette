package ru.threedisevenzeror.marionette.model.packets.fceux.in;

import ru.threedisevenzeror.marionette.model.packets.base.InPacket;

import java.io.DataInputStream;

/**
 * Pong packet
 * Received from FCEUX as Ping response
 */
public class FceuxPongPacket implements InPacket {

    @Override
    public void read(DataInputStream stream) {
        // No data
    }

    @Override
    public byte getType() {
        return 0x00;
    }
}
