package org.dyn4j.samples.Tools;

import org.dyn4j.samples.demos.thrustTraining.fitnessFunctions.FitnessFunction;
import org.dyn4j.samples.demos.thrustTraining.fitnessFunctions.TestFitness;
import org.dyn4j.samples.models.NeuralNetwork;
import org.dyn4j.samples.models.Util;

public class MultiEvaluateAgent {


    public static void main(String[] args) {
        String[] paths = {
                "results/2024-06-11_12-01-07/AlphaThruster.obj15",
                "results/2024-06-11_13-08-31/AlphaThruster.obj17",
                "results/2024-06-11_13-33-05/AlphaThruster.obj11",
                "results/2024-06-11_14-26-10/AlphaThruster.obj15",
                "results/2024-06-11_15-00-33/AlphaThruster.obj6",
                "results/2024-06-11_15-33-09/AlphaThruster.obj17",
                "results/2024-06-11_16-58-49/AlphaThruster.obj13",
                "results/2024-06-11_18-01-51/AlphaThruster.obj7",
                "results/2024-06-11_18-32-42/AlphaThruster.obj10"
        };

        int ctr = 0;
        for (String p : paths) {
            NeuralNetwork test = Util.loadNeuralNetworkFromFile(
                    p);
            FitnessFunction tester = new TestFitness();
            int ITERATIONS = 100;
            double result = 0;

            for (int i = 0; i < ITERATIONS; i++) {
                result += tester.fitness(test);
            }


            System.out.format("RUN %d: %f\n", ++ctr, result / ITERATIONS);
        }

    }
}
