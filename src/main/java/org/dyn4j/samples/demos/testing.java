package org.dyn4j.samples.demos;

import org.apache.commons.math3.distribution.GeometricDistribution;
import org.dyn4j.samples.models.NeuralNetwork;
import org.dyn4j.samples.models.Util;
import org.ejml.simple.SimpleMatrix;

public class testing {

    public static void main(String[] args) {
        NeuralNetwork parent1 = NeuralNetwork.
                constructRandomNeuralNetwork(2,
                        4, 3,
                        2, Util.OPERATOR_SIGMOID,
                        new NeuralNetwork.NormalMutator(1, 1),
                        new NeuralNetwork.UniformNeuronCrossover(0.5));
        NeuralNetwork parent2 = NeuralNetwork.
                constructRandomNeuralNetwork(2,
                        4, 3,
                        2, Util.OPERATOR_SIGMOID,
                        new NeuralNetwork.NormalMutator(1, 1),
                        new NeuralNetwork.UniformNeuronCrossover(0.5));
        NeuralNetwork[] children = parent1.crossoverWith(parent2);
        for (int i = 0; i < parent1.getLayers().size(); i++) {
            System.out.println("LAYER " + i);
            System.out.println("PARENT1: ");
            System.out.println(parent1.getLayers().get(i).getWeights());


            System.out.println("PARENT2: ");
            System.out.println(parent2.getLayers().get(i).getWeights());

            System.out.println("CHILD1: ");
            System.out.println(children[0].getLayers().get(i).getWeights());


            System.out.println("CHILD2: ");
            System.out.println(children[1].getLayers().get(i).getWeights());
        }
    }
}
