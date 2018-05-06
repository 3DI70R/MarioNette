package ru.threedisevenzeror.marionette.logic;

import ru.threedisevenzeror.marionette.logic.base.BaseNeuralEvaluator;
import ru.threedisevenzeror.marionette.logic.base.NeuralEvaluator;
import ru.threedisevenzeror.marionette.model.neural.NeuralConfiguration;
import ru.threedisevenzeror.marionette.model.neural.NeuralEvaluationResult;

import java.util.*;

/**
 * Neural evaluator, which balances evaluation jobs across other evaluators
 * and acts like a failsafe measure, because he can redelegate failed jobs to other evaluators
 */
public class NeuralEvaluatorBalancer extends BaseNeuralEvaluator {

    private List<NeuralConfiguration> stashedConfigurations;
    private final Map<NeuralEvaluator, List<NeuralConfiguration>> evaluatorConfigurationMap;
    private final List<NeuralEvaluator> evaluators;

    public NeuralEvaluatorBalancer() {
        evaluators = new ArrayList<>();
        evaluatorConfigurationMap = new HashMap<>();
        stashedConfigurations = new ArrayList<>();
    }

    public void addEvaluator(NeuralEvaluator evaluator) {
        if(!evaluators.contains(evaluator)) {
            evaluators.add(evaluator);

            evaluator.setOnEvaluationErrorListener((e, ex) -> {
                ex.printStackTrace();
                removeEvaluator(e);
            });
        }
    }

    public void removeEvaluator(NeuralEvaluator evaluator) {
        evaluator.setOnEvaluationErrorListener(null);
        evaluators.remove(evaluator);
    }

    @Override
    public boolean canAcceptNewJobs() {
        return true;
    }

    @Override
    public float getEvaluationProgress() {
        float total = 0;

        for (NeuralEvaluator e : evaluators) {
            total = e.getEvaluationProgress();
        }

        return total * evaluators.size();
    }

    @Override
    public void evaluate(List<NeuralConfiguration> configurations) {

        List<NeuralEvaluator> evaluatorSchedule = new ArrayList<>();
        Map<NeuralEvaluator, List<NeuralConfiguration>> schedule = new HashMap<>();

        for (NeuralEvaluator e : evaluators) {
            if(e.canAcceptNewJobs()) {
                int threadCount = e.threadCount();
                for(int i = 0; i < threadCount; i++) {
                    evaluatorSchedule.add(e);
                }
            }
        }

        if(evaluatorSchedule.size() == 0) {
            stashedConfigurations.addAll(configurations);
        } else {
            int evaluator = 0;
            for (NeuralConfiguration c : configurations) {

                if(evaluator >= evaluatorSchedule.size()) {
                    evaluator = 0;
                }

                NeuralEvaluator e = evaluatorSchedule.get(evaluator++);
                schedule.computeIfAbsent(e, k -> new ArrayList<>())
                        .add(c);
            }

            for (Map.Entry<NeuralEvaluator, List<NeuralConfiguration>> e : schedule.entrySet()) {
                e.getKey().evaluate(e.getValue());
            }
        }
    }

    @Override
    public List<NeuralEvaluationResult> getEvaluationResults() {
        List<NeuralEvaluationResult> results = new ArrayList<>();

        for (NeuralEvaluator e : evaluators) {
            results.addAll(e.getEvaluationResults());
        }

        return results;
    }

    @Override
    public boolean isFinished() {

        for (NeuralEvaluator e : evaluators) {
            if(!e.isFinished()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void reset() {
        for (NeuralEvaluator e : evaluators) {
            e.reset();
        }
    }

    @Override
    public int threadCount() {
        int totalThreads = 0;

        for (NeuralEvaluator e : evaluators) {
            totalThreads += e.threadCount();
        }

        return Math.max(totalThreads, 1);
    }
}
