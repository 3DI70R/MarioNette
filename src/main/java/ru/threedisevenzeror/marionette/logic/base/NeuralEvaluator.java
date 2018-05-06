package ru.threedisevenzeror.marionette.logic.base;

import ru.threedisevenzeror.marionette.model.neural.NeuralConfiguration;
import ru.threedisevenzeror.marionette.model.neural.NeuralEvaluationResult;

import java.util.List;

/**
 * Neural evaluator
 */
public interface NeuralEvaluator {

    /**
     * Returns if this evaluator can accept new jobs or it is in idle mode
     */
    boolean canAcceptNewJobs();

    /**
     * Returns evaluation progress for supplied neural networks
     */
    float getEvaluationProgress();

    /**
     * Schedules evaluation of this neural network configuration
     */
    void evaluate(List<NeuralConfiguration> configurations);

    /**
     * Returns list of currently evaluated networks and its results
     */
    List<NeuralEvaluationResult> getEvaluationResults();

    /**
     * Is evaluation of supplied neural networks is finished?
     */
    boolean isFinished();

    /**
     * Clears all stored information about previous evaluations, resetting this evaluator to idle state
     */
    void reset();

    /**
     * Simultaneous amount of parallel running jobs
     */
    int threadCount();

    /**
     * Listener for evaluator errors
     * This listener will be called if evaluator cannot complete evaluation task for some reason
     */
    void setOnEvaluationErrorListener(OnEvaluationErrorListener errorListener);
}
