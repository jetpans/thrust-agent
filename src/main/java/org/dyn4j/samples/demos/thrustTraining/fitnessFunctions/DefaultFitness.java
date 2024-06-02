package org.dyn4j.samples.demos.thrustTraining.fitnessFunctions;

import org.dyn4j.samples.Thrust;
import org.dyn4j.samples.demos.thrustTraining.ThrustCarouselSelection;
import org.dyn4j.samples.models.NeuralNetwork;

import java.util.Random;

public class DefaultFitness implements FitnessFunction {
    @Override
    public double fitness(NeuralNetwork nn) {
        Thrust simulation = new Thrust(nn);
        simulation.renderGame = false;
        simulation.tickLimit = ThrustCarouselSelection.tickLimit;
        simulation.myRandom = new Random(0);
        simulation.terminateOnPointCollection = false;
        simulation.run();
        simulation.dispose();
        return -400 * simulation.scoring.collectedPoints - simulation.scoring.pathMade + 10 * simulation.scoring.minUncollectedTrashDistance - simulation.scoring.ticksSpentLookingAtTrash;
    }
}
