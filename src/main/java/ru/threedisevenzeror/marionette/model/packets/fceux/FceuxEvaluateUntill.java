package ru.threedisevenzeror.marionette.model.packets.fceux;

import ru.threedisevenzeror.marionette.model.packets.base.OutPacket;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Dummy packet which will be sent to FCEUX periodically to keep it running
 * since you cannot tell if there is data in socket
 */
public class FceuxEvaluateUntill implements OutPacket {

    public long evaluationEndTime;

    public FceuxEvaluateUntill(long milisecondsFromNow) {
        evaluationEndTime = System.currentTimeMillis() + milisecondsFromNow;
    }

    @Override
    public void write(DataOutputStream stream) throws IOException {
        stream.writeLong(evaluationEndTime);
    }

    @Override
    public byte getType() {
        return 0x00;
    }
}
