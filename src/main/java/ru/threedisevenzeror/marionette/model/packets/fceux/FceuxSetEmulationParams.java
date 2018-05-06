package ru.threedisevenzeror.marionette.model.packets.fceux;

import ru.threedisevenzeror.marionette.model.fceux.EmulationSpeed;
import ru.threedisevenzeror.marionette.model.packets.base.OutPacket;

import java.io.DataOutputStream;
import java.io.IOException;

public class FceuxSetEmulationParams implements OutPacket {

    public EmulationSpeed emulationSpeed;

    public FceuxSetEmulationParams(EmulationSpeed speed) {
        emulationSpeed = speed;
    }

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
    }

    @Override
    public byte getType() {
        return 0x01;
    }
}
