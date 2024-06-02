package org.dyn4j.samples.demos.thrustTraining.fitnessFunctions;

import org.dyn4j.samples.models.NeuralNetwork;

public interface FitnessFunction {
    public double fitness(NeuralNetwork nn);
}