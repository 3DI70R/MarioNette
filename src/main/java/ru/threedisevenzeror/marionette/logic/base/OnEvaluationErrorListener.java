package ru.threedisevenzeror.marionette.logic.base;

/**
 * Listener which will be invoked when evaluator is unable to complete his job
 */
public interface OnEvaluationErrorListener {

    /**
     * This method will be invoked, if evaluator failed to do his job and failed for some reason
     */
    void onEvaluatorError(NeuralEvaluator evaluator, Exception e);
}
