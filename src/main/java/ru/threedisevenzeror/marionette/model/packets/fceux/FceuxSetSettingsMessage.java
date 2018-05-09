package ru.threedisevenzeror.marionette.model.packets.fceux;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import ru.threedisevenzeror.marionette.model.fceux.FceuxEmulationSpeed;

/**
 * Message with settings configuration for FCEUX
 */
public class FceuxSetSettingsMessage implements KryoSerializable {

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
    public void write(Kryo kryo, Output output) {
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
    public void read(Kryo kryo, Input input) {
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
