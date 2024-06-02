package org.dyn4j.samples.Tools;

import org.dyn4j.samples.demos.thrustTraining.ThrustCarouselSelection;
import org.dyn4j.samples.demos.thrustTraining.fitnessFunctions.FitnessFunction;
import org.dyn4j.samples.demos.thrustTraining.fitnessFunctions.TestFitness;
import org.dyn4j.samples.models.NeuralNetwork;
import org.dyn4j.samples.models.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class EvaluateAgent {

    public static void main(String[] args) {
        NeuralNetwork test = Util.loadNeuralNetworkFromFile(
                "parameterOptimization/itersChoice/iters=5steps=160/2024-05-31_14-50-43/AlphaThruster.obj8");
        FitnessFunction tester = new TestFitness();
        int ITERATIONS = 10;
        double result = 0;

        for (int i = 0; i < ITERATIONS; i++) {
            result += tester.fitness(test);
        }


        System.out.println("Average result is " + result / ITERATIONS);


    }
}
