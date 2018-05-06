package ru.threedisevenzeror.marionette.logic;

import ru.threedisevenzeror.marionette.model.neural.NeuralConfiguration;
import ru.threedisevenzeror.marionette.model.neural.NeuralEvaluationResult;
import ru.threedisevenzeror.marionette.logic.base.BaseNeuralEvaluator;
import ru.threedisevenzeror.marionette.model.fceux.EmulationSpeed;
import ru.threedisevenzeror.marionette.model.packets.fceux.FceuxEvaluateUntill;
import ru.threedisevenzeror.marionette.model.packets.fceux.FceuxSetEmulationParams;
import ru.threedisevenzeror.marionette.model.packets.fceux.FceuxWaitingForNewCommands;
import ru.threedisevenzeror.marionette.network.messaging.base.MessageChannel;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Communication bridge between FCEUX and Marionette
 */
public class FceuxNeuralEvaluator extends BaseNeuralEvaluator {

    private long updateTime;
    private MessageChannel channel;

    public FceuxNeuralEvaluator(MessageChannel channel) {

        this.channel = channel;

        setEvaluationPeriods(250, TimeUnit.MILLISECONDS);
        channel.addPacketListener(new FceuxWaitingForNewCommands(),
                p -> channel.sendPacket(new FceuxEvaluateUntill(updateTime)));
    }

    public void setEvaluationPeriods(long period, TimeUnit unit) {
        updateTime = unit.toMillis(period);
    }

    public void setEmulationSpeed(EmulationSpeed speed) {
        channel.sendPacket(new FceuxSetEmulationParams(speed));
    }

    @Override
    public float getEvaluationProgress() {
        return 0;
    }

    @Override
    public void evaluate(List<NeuralConfiguration> configurations) {

    }

    @Override
    public List<NeuralEvaluationResult> getEvaluationResults() {
        return null;
    }

    @Override
    public boolean isFinished() {
        return false;
    }

    @Override
    public void reset() {

    }

    @Override
    public int threadCount() {
        return 1;
    }
}
