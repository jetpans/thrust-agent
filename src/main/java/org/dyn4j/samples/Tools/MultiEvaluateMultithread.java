package org.dyn4j.samples.Tools;

import org.dyn4j.samples.demos.thrustTraining.fitnessFunctions.FitnessFunction;
import org.dyn4j.samples.demos.thrustTraining.fitnessFunctions.TestFitness;
import org.dyn4j.samples.models.NeuralNetwork;
import org.dyn4j.samples.models.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class MultiEvaluateMultithread {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        String[] paths = {
                "parameterOptimization/neuralNetConfig/layer4neuron15/2024-06-06_22-01-55/AlphaThruster.obj4",
                "parameterOptimization/neuralNetConfig/layer4neuron15/2024-06-06_22-30-51/AlphaThruster.obj8",
                "parameterOptimization/neuralNetConfig/layer4neuron15/2024-06-06_23-22-10/AlphaThruster.obj3",
                "parameterOptimization/neuralNetConfig/layer4neuron15/2024-06-07_01-35-31/AlphaThruster.obj8",
                "parameterOptimization/neuralNetConfig/layer4neuron15/2024-06-07_01-42-45/AlphaThruster.obj2",
                "parameterOptimization/neuralNetConfig/layer4neuron15/2024-06-07_01-49-30/AlphaThruster.obj6",
                "parameterOptimization/neuralNetConfig/layer4neuron15/2024-06-07_01-54-17/AlphaThruster.obj8",
                "parameterOptimization/neuralNetConfig/layer4neuron15/2024-06-07_02-02-28/AlphaThruster.obj8"
        };

        ExecutorService executor = Executors.newFixedThreadPool(paths.length);
        List<Future<Double>> results = new ArrayList<>();

        for (String path : paths) {
            FitnessTask task = new FitnessTask(path);
            results.add(executor.submit(task));
        }

        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        executor.shutdown();
        int ctr = 0;
        for (Future<Double> result : results) {
            System.out.format("RUN %d: %f\n", ++ctr, result.get());
        }

        executor.shutdown();
    }
}

class FitnessTask implements Callable<Double> {
    private String path;

    public FitnessTask(String path) {
        this.path = path;
    }

    @Override
    public Double call() {
        NeuralNetwork test = Util.loadNeuralNetworkFromFile(this.path);
        FitnessFunction tester = new TestFitness();
        int ITERATIONS = 10;
        double result = 0;

        for (int i = 0; i < ITERATIONS; i++) {
            result += tester.fitness(test);
        }

        System.out.println("FINISHED ONE");
        return result / ITERATIONS;
    }
}

