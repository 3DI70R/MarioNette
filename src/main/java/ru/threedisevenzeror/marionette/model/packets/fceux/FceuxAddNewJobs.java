package ru.threedisevenzeror.marionette.model.packets.fceux;

import ru.threedisevenzeror.marionette.model.neural.NeuralConfiguration;
import ru.threedisevenzeror.marionette.model.packets.base.OutPacket;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class FceuxAddNewJobs implements OutPacket  {

    public List<NeuralConfiguration> neuralConfigurations;

    @Override
    public void write(DataOutputStream stream) throws IOException {

    }

    @Override
    public byte getType() {
        return 0x03;
    }
}
