package org.dyn4j.samples.demos.thrustTraining.fitnessFunctions;

import org.dyn4j.samples.Thrust;
import org.dyn4j.samples.models.NeuralNetwork;
import org.dyn4j.samples.worlds.WorldDescriptor;

import java.util.Random;

public class FitnessThree extends AdvancedFitness {


    @Override
    public double helperFitness(NeuralNetwork nn, int tickLimit, Random newRandom, boolean terminateOnPointCollection, WorldDescriptor w) {
        Thrust simulation = new Thrust(nn, w);
        simulation.renderGame = false;
        simulation.tickLimit = tickLimit;
        simulation.myRandom = newRandom;
        simulation.terminateOnPointCollection = terminateOnPointCollection;
        simulation.run();
        simulation.dispose();
        return -200 * simulation.scoring.collectedPoints
                + 0.5 * simulation.scoring.pathMade
                - 5 * simulation.scoring.ticksSpentLookingAtTrash
                + simulation.scoring.collisions * 30
                + simulation.scoring.minUncollectedTrashDistance * 4;
    }
}