package org.dyn4j.samples.demos.functionTraining;

import org.dyn4j.samples.models.NeuralNetwork;
import org.dyn4j.samples.models.Util;
import org.math.plot.Plot2DPanel;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;
import java.util.function.DoubleUnaryOperator;

public class CarouselSelection {

    private static class FitnessNeuralNetwork implements Comparator<FitnessNeuralNetwork>, Comparable<FitnessNeuralNetwork> {
        NeuralNetwork nn;
        double fitness = 0;
        public static List<List<Double>> data;

        public FitnessNeuralNetwork(NeuralNetwork nn) {
            this.nn = nn;
            this.fitness = fitness(nn, data);
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
        List<List<Double>> data = Util.readRegressionDataFromFile("regression1.txt");
        double[] dataX = new double[data.size()];
        double[] dataY = new double[data.size()];
        for (int i = 0; i < data.size(); i++) {
            dataX[i] = data.get(i).get(0);
            dataY[i] = data.get(i).get(1);
        }
        FitnessNeuralNetwork.data = data;


        int numOfLayers = 4;
        int numOfNodes = 10;
        DoubleUnaryOperator fun = Util.OPERATOR_SWISH;

        double bestResult = Double.POSITIVE_INFINITY;
        NeuralNetwork alpha = null;
        final int POPULATION_SIZE = 10;
        final double KEEP_PERCENT = 1.f / 2;

        FitnessNeuralNetwork[] population = new FitnessNeuralNetwork[POPULATION_SIZE];
        for (int i = 0; i < POPULATION_SIZE; i++) {
            population[i] = new FitnessNeuralNetwork(NeuralNetwork.constructRandomNeuralNetwork(1, numOfLayers, numOfNodes, 1, fun, new NeuralNetwork.NormalMutator(1, 0.05), new NeuralNetwork.TwoPointCrossover()));
        }

        int steps = (int) 50000;
        while (steps-- > 0) {

            FitnessNeuralNetwork[] newPopulation = new FitnessNeuralNetwork[POPULATION_SIZE];
            int counter = 0;
            for (int i = 0; i < POPULATION_SIZE * KEEP_PERCENT; i++) {
                newPopulation[i] = population[i];
                counter++;
            }
            for (; counter < POPULATION_SIZE - 1; counter += 2) {

                int firstIndex = Util.randomIntFromIntervalGeometric(0, POPULATION_SIZE - 1);
                int secondIndex = Util.randomIntFromIntervalGeometric(firstIndex + 1, POPULATION_SIZE - 1);
                NeuralNetwork[] children = population[firstIndex].nn.crossoverWith(population[secondIndex].nn);
                children[0].mutate();
                children[1].mutate();
                newPopulation[counter] = new FitnessNeuralNetwork(children[0]);
                newPopulation[counter + 1] = new FitnessNeuralNetwork(children[1]);
            }
            for (; counter < POPULATION_SIZE; counter++) {
                newPopulation[counter] = new FitnessNeuralNetwork(NeuralNetwork.constructRandomNeuralNetwork(1, numOfLayers, numOfNodes, 1, fun, new NeuralNetwork.NormalMutator(1, 0.05), new NeuralNetwork.TwoPointCrossover()));
            }
            population = newPopulation;
            Arrays.sort(population);

            if (population[0].fitness < bestResult) {
                bestResult = population[0].fitness;
                alpha = population[0].nn;
                System.out.println("Best result so far: " + bestResult);
            }

            if (steps % 1000 == 0) {
                System.out.println(steps);
            }
        }


        List<double[]> nnResult = getPoints(alpha, -10, 10, 100);
        Plot2DPanel plot = new Plot2DPanel();
        plot.addLinePlot("Data", dataX, dataY);
        plot.addLinePlot("NN Prediction", nnResult.get(0), nnResult.get(1));

        JFrame frame = new JFrame("Wow");
        frame.setSize(800, 600);
        frame.setContentPane(plot);
        frame.setVisible(true);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);
                frame.dispose();
            }
        });

    }

    public static double fitness(NeuralNetwork nn, List<List<Double>> data) {
        double suma = 0;

        for (List<Double> entry : data) {
            double result = nn.calculateForInputs(new double[]{entry.get(0)})[0];
            suma += (result - entry.get(1)) * (result - entry.get(1));
        }

        return suma;
    }

    public static List<double[]> getPoints(NeuralNetwork nn, int min, int max, int points) {
        double[] xVector = new double[points];
        double[] yVector = new double[points];
        for (int i = 0; i < points; i++) {
            double x = min + 1.f * i * (max - min) / points;
            double y = nn.calculateForInputs(new double[]{x})[0];
            xVector[i] = x;
            yVector[i] = y;

        }

        return List.of(xVector, yVector);

    }
}
