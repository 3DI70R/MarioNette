package ru.threedisevenzeror.marionette.model.packets.fceux.out;

import ru.threedisevenzeror.marionette.model.fceux.EmulationSpeed;
import ru.threedisevenzeror.marionette.model.packets.base.OutPacket;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Packet with settings for FCEUX
 */
public class FceuxSetSettingsPacket implements OutPacket {

    /**
     * Emulation speed
     */
    public EmulationSpeed emulationSpeed;

    /**
     * Socket read-write timeout
     */
    public int socketTimeout;

    /**
     * How much time to run emulation, before checking for new packet
     */
    public int emulationPeriods;

    /**
     * Should debugging info be visible in emulator
     */
    public boolean debuggingInfo;

    @Override
    public void write(DataOutputStream stream) throws IOException {
        switch (emulationSpeed) {
            case Normal:
                stream.writeByte(0);
                break;
            case Nothrottle:
                stream.writeByte(1);
                break;
            case Maximum:
                stream.writeByte(2);
                break;
        }

        stream.writeInt(socketTimeout);
        stream.writeInt(emulationPeriods);
        stream.writeByte(debuggingInfo ? 1 : 0);
    }

    @Override
    public byte getType() {
        return 0x01;
    }
}
