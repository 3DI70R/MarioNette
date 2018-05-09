package ru.threedisevenzeror.marionette.model.packets.fceux;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Packet which contains full memory map of current game running in fceux
 */
public class FceuxMemoryDump implements KryoSerializable {

    /**
     * Raw ram memory of NES
     */
    public byte[] ram = new byte[0x800];

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeBytes(ram);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        ram = input.readBytes(0x800);
    }
}
