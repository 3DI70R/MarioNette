package ru.threedisevenzeror.marionette.model.message.fceux;

import io.netty.buffer.ByteBuf;
import ru.threedisevenzeror.marionette.model.message.NetworkObject;

/**
 * Packet which contains full memory map of current game running in fceux
 */
public class FceuxMemoryDump implements NetworkObject {

    /**
     * Raw ram memory of NES
     */
    public byte[] ram = new byte[0x800];

    @Override
    public void write(ByteBuf output) {
        output.writeBytes(ram);
    }

    @Override
    public void read(ByteBuf input) {
        ram = input.readBytes(0x800).array();
    }
}
