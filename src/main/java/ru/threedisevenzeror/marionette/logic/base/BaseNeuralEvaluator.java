package ru.threedisevenzeror.marionette.logic.base;

public abstract class BaseNeuralEvaluator implements NeuralEvaluator {

    private OnEvaluationErrorListener errorListener;
    protected boolean canAcceptNewJobs = true;

    @Override
    public boolean canAcceptNewJobs() {
        return canAcceptNewJobs;
    }

    protected void onEvaluationError(Exception e) {
        if(errorListener != null) {
            errorListener.onEvaluatorError(this, e);
        }

        canAcceptNewJobs = false;
    }

    @Override
    public void setOnEvaluationErrorListener(OnEvaluationErrorListener errorListener) {
        this.errorListener = errorListener;
    }
}
