package org.dyn4j.samples.agents;

import org.dyn4j.samples.models.NeuralNetwork;

import java.awt.*;
import java.util.Map;

public abstract class AbstractNNAgent {


    protected final NeuralNetwork neuralNetwork;
    protected AgentBooleanStateKeyboardInputHandler[] inputHandlers = null;

    public AbstractNNAgent(NeuralNetwork neuralNetwork, Component comp) {
        this.neuralNetwork = neuralNetwork;
    }


    public AgentBooleanStateKeyboardInputHandler[] getInputHandlers() {
        return this.inputHandlers;
    }


    public Map<String, Boolean> makeAction(Map<String, Double> state) {
        return null;
    }


}
