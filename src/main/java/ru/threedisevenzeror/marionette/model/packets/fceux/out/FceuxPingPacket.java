package ru.threedisevenzeror.marionette.model.packets.fceux.out;

import ru.threedisevenzeror.marionette.model.packets.base.OutPacket;

import java.io.DataOutputStream;

/**
 * Ping packet
 * Sended to FCEUX periodically to check that it's reachable
 */
public class FceuxPingPacket implements OutPacket {

    @Override
    public void write(DataOutputStream stream) {
        // No data
    }

    @Override
    public byte getType() {
        return 0x00;
    }
}
