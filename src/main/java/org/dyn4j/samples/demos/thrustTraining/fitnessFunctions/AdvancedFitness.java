package org.dyn4j.samples.demos.thrustTraining.fitnessFunctions;

import org.dyn4j.samples.Thrust;
import org.dyn4j.samples.demos.thrustTraining.ThrustCarouselSelection;
import org.dyn4j.samples.models.NeuralNetwork;
import org.dyn4j.samples.models.Util;
import org.dyn4j.samples.worlds.WorldDescriptor;
import org.dyn4j.samples.worlds.Worlds;

import java.util.Random;

public class AdvancedFitness implements FitnessFunction {
    @Override
    public double fitness(NeuralNetwork nn) {
        double result = 0;
        for (int i = 0; i < ThrustCarouselSelection.ITERS; i++) {
            result += this.helperFitness(nn, ThrustCarouselSelection.tickLimit,
                    new Random(0),
                    false,
                    Worlds.get(i % Worlds.count()));
        }
        return result / ThrustCarouselSelection.ITERS;
    }

    public double helperFitness(NeuralNetwork nn, int tickLimit, Random newRandom, boolean terminateOnPointCollection, WorldDescriptor w) {
        Thrust simulation = new Thrust(nn, w);
        simulation.renderGame = false;
        simulation.tickLimit = tickLimit;
        simulation.myRandom = newRandom;
        simulation.terminateOnPointCollection = terminateOnPointCollection;
        simulation.run();
        simulation.dispose();
        return -200 * simulation.scoring.collectedPoints
                + 0.2 * simulation.scoring.pathMade
                - 0.5 * simulation.scoring.ticksSpentLookingAtTrash
                + simulation.scoring.collisions * 20
                + simulation.scoring.minUncollectedTrashDistance * 2;
    }
}