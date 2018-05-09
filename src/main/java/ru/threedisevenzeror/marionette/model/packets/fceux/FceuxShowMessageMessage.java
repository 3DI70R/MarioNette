package ru.threedisevenzeror.marionette.model.packets.fceux;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import ru.threedisevenzeror.marionette.utils.StreamUtils;

/**
 * Message that is sent to fceux, to display arbitrary info in emulator's window
 */
public class FceuxShowMessageMessage implements KryoSerializable {

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
    public void write(Kryo kryo, Output output) {
        StreamUtils.writeNullTerminatedString(output, message);
        output.writeInt(displayTimeMiliseconds);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        message = StreamUtils.readNullTerminatedString(input);
        displayTimeMiliseconds = input.readInt();
    }
}
