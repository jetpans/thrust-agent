package org.dyn4j.samples.demos.basketballTraining;

import org.apache.commons.math3.genetics.Fitness;
import org.dyn4j.samples.BasketBall;
import org.dyn4j.samples.models.NeuralNetwork;
import org.dyn4j.samples.models.Util;
import org.math.plot.Plot2DPanel;

import javax.security.auth.callback.TextInputCallback;
import javax.swing.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.DoubleUnaryOperator;

public class CarouselMultithread {

    private static class FitnessNeuralNetwork implements Comparator<FitnessNeuralNetwork>, Comparable<FitnessNeuralNetwork> {
        NeuralNetwork nn;
        double fitness = 0;

        public FitnessNeuralNetwork(NeuralNetwork nn) {
            this.nn = nn;
            this.fitness = fitness(nn);
        }

        @Override
        public int compare(FitnessNeuralNetwork o1, FitnessNeuralNetwork o2) {
            return Double.compare(o1.fitness, o2.fitness);
        }

        @Override
        public int compareTo(FitnessNeuralNetwork o) {
            return Double.compare(this.fitness, o.fitness);
        }
    }

    public static void main(String[] args) {
        int numOfLayers = 12;
        int numOfNodes = 12;
        DoubleUnaryOperator fun = Util.OPERATOR_SIGMOID;

        double bestResult = Double.POSITIVE_INFINITY;
        NeuralNetwork alpha = null;
        final int POPULATION_SIZE = 51;
        final double KEEP_PERCENT = 1.f / 2;

        FitnessNeuralNetwork[] population = new FitnessNeuralNetwork[POPULATION_SIZE];
        for (int i = 0; i < POPULATION_SIZE; i++) {
            population[i] = new FitnessNeuralNetwork(NeuralNetwork.constructRandomNeuralNetwork(3, numOfLayers, numOfNodes, 2, fun, new NeuralNetwork.NormalMutator(1, 0.05), new NeuralNetwork.TwoPointCrossover()));
        }

        int steps = (int) 20000;
        while (steps-- > 0) {
            FitnessNeuralNetwork[] newPopulation = new FitnessNeuralNetwork[POPULATION_SIZE];
            ExecutorService executorService = Executors.newFixedThreadPool(POPULATION_SIZE);
            List<Future<FitnessNeuralNetwork>> futures = new ArrayList<>();

            int counter = 0;
            for (int i = 0; i < POPULATION_SIZE * KEEP_PERCENT; i++) {
                newPopulation[i] = population[i];
                counter++;
            }
            int startFrom = counter;
            for (; counter < POPULATION_SIZE - 1; counter += 2) {

                int firstIndex = Util.randomIntFromIntervalGeometric(0, POPULATION_SIZE - 1);
                int secondIndex = Util.randomIntFromIntervalGeometric(firstIndex + 1, POPULATION_SIZE - 1);
                NeuralNetwork[] children = population[firstIndex].nn.crossoverWith(population[secondIndex].nn);
                children[0].mutate();
                children[1].mutate();
                futures.add(executorService.submit(() -> new FitnessNeuralNetwork(children[0])));
                futures.add(executorService.submit(() -> new FitnessNeuralNetwork(children[1])));
//                newPopulation[counter] = new FitnessNeuralNetwork(children[0]);
//                newPopulation[counter + 1] = new FitnessNeuralNetwork(children[1]);
            }
            for (; counter < POPULATION_SIZE; counter++) {
                futures.add(executorService.submit(() -> new FitnessNeuralNetwork(NeuralNetwork.constructRandomNeuralNetwork(3, numOfLayers, numOfNodes, 2, fun, new NeuralNetwork.NormalMutator(1, 0.05), new NeuralNetwork.TwoPointCrossover()))));
//                newPopulation[counter] = new FitnessNeuralNetwork(NeuralNetwork.constructRandomNeuralNetwork(3, numOfLayers, numOfNodes, 2, fun));
            }

            executorService.shutdown();
            try {
                boolean res = executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                if (!res) {
                    System.out.println("AAAAAH TIMEOUT");
                }
                for (Future<FitnessNeuralNetwork> future : futures) {
                    if (future != null) {
                        newPopulation[startFrom] = future.get();
                        startFrom++;
                    }
                }
                if (startFrom != POPULATION_SIZE) {
                    System.out.println("Bad logic happened.");
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            population = newPopulation;
            Arrays.sort(population);

            if (population[0].fitness < bestResult) {
                bestResult = population[0].fitness;
                alpha = population[0].nn;
                System.out.println("Best result so far: " + bestResult);
                Util.writeNeuralNetworkToFile("AlphaBasket.txt", alpha);

            }

            if (steps % 100 == 0) {
                System.out.println(steps);
            }
        }


        Util.writeNeuralNetworkToFile("Testnn1.txt", alpha);

    }

    public static double fitness(NeuralNetwork nn) {
        double result = BasketBall.runSimulation(nn, 1000);
        return -result;
    }


}
