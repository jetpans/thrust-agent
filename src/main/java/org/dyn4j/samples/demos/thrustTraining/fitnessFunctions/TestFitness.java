package org.dyn4j.samples.demos.thrustTraining.fitnessFunctions;

import org.dyn4j.samples.Thrust;
import org.dyn4j.samples.demos.thrustTraining.ThrustCarouselSelection;
import org.dyn4j.samples.models.NeuralNetwork;
import org.dyn4j.samples.worlds.WorldDescriptor;
import org.dyn4j.samples.worlds.Worlds;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class TestFitness implements FitnessFunction {
    public double fitness(NeuralNetwork nn) {
        double result = 0;
        int iters = 4;

        ExecutorService executorService = Executors.newFixedThreadPool(iters);
        List<Future<Double>> futures = new ArrayList<>();


        for (int i = 0; i < iters; i++) {
            int temp = i;
            futures.add(executorService.submit(
                    () ->
                            this.helperFitness(nn, 1000, new Random(Math.round(10e8 * Math.random())),
                                    false, Worlds.get(temp))));
        }


        executorService.shutdown();

        try {
            boolean res = executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            if (!res) {
                System.out.println("AAAAAH TIMEOUT");
            }
            for (Future<Double> future : futures) {
                if (future != null) {
                    result += future.get();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result / iters;
    }

    public double helperFitness(NeuralNetwork nn, int tickLimit, Random newRandom, boolean terminateOnPointCollection, WorldDescriptor d) {
        Thrust simulation = new Thrust(nn);
        simulation.renderGame = false;
        simulation.tickLimit = tickLimit;
        simulation.myRandom = newRandom;
        simulation.terminateOnPointCollection = terminateOnPointCollection;
        simulation.run();
        simulation.dispose();
        return -200 * simulation.scoring.collectedPoints;
    }
}
