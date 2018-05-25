package ru.threedisevenzeror.marionette.model.message.fceux;

import io.netty.buffer.ByteBuf;
import ru.threedisevenzeror.marionette.model.fceux.FceuxEmulationSpeed;
import ru.threedisevenzeror.marionette.model.message.NetworkObject;

/**
 * Message with settings configuration for FCEUX
 */
public class FceuxSetSettingsMessage implements NetworkObject {

    /**
     * Emulation speed
     */
    public FceuxEmulationSpeed emulationSpeed;

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
    public boolean showDebuggingInfo;

    @Override
    public void write(ByteBuf output) {
        switch (emulationSpeed) {
            case Normal: output.writeByte(0); break;
            case Nothrottle: output.writeByte(1); break;
            case Maximum: output.writeByte(2); break;
        }

        output.writeInt(socketTimeout);
        output.writeInt(emulationPeriods);
        output.writeBoolean(showDebuggingInfo);
    }

    @Override
    public void read(ByteBuf input) {
        switch (input.readByte()) {
            case 0: emulationSpeed = FceuxEmulationSpeed.Normal; break;
            case 1: emulationSpeed = FceuxEmulationSpeed.Nothrottle; break;
            case 2: emulationSpeed = FceuxEmulationSpeed.Maximum; break;
        }

        socketTimeout = input.readInt();
        emulationPeriods = input.readInt();
        showDebuggingInfo = input.readBoolean();
    }
}
