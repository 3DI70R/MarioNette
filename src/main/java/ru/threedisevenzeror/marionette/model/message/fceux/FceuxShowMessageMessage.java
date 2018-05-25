package ru.threedisevenzeror.marionette.model.message.fceux;

import io.netty.buffer.ByteBuf;
import ru.threedisevenzeror.marionette.model.message.NetworkObject;
import ru.threedisevenzeror.marionette.utils.StreamUtils;

/**
 * Message that is sent to fceux, to display arbitrary info in emulator's window
 */
public class FceuxShowMessageMessage implements NetworkObject {

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
    public void write(ByteBuf stream) {
        StreamUtils.writeNullTerminatedString(stream, message);
        stream.writeInt(displayTimeMiliseconds);
    }

    @Override
    public void read(ByteBuf stream)  {
        message = StreamUtils.readNullTerminatedString(stream);
        displayTimeMiliseconds = stream.readInt();
    }
}
