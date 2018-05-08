package ru.threedisevenzeror.marionette.model.packets.fceux.out;

import ru.threedisevenzeror.marionette.model.packets.base.OutPacket;
import ru.threedisevenzeror.marionette.utils.DataUtils;

import java.io.DataOutputStream;
import java.io.IOException;

public class FceuxShowMessagePacket implements OutPacket {

    /**
     * Message to fceux
     */
    public String message;

    /**
     * Message display time
     * -1 to infinite display
     */
    public int displayTimeMiliseconds;

    @Override
    public void write(DataOutputStream stream) throws IOException {
        DataUtils.writeNullTerminatedString(stream, message);
        stream.writeInt(displayTimeMiliseconds);
    }

    @Override
    public byte getType() {
        return 0x02;
    }
}
