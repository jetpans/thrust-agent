package org.dyn4j.samples.agents;

import org.apache.commons.math3.linear.RealMatrix;
import org.dyn4j.samples.models.NeuralNetwork;
import org.dyn4j.samples.models.Util;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class NNBasketballAgent extends AbstractNNAgent {
    double lastTime = 0;
    public AgentBooleanStateKeyboardInputHandler angleUp;
    public AgentBooleanStateKeyboardInputHandler angleDown;
    public AgentBooleanStateKeyboardInputHandler fire;

    public NNBasketballAgent(NeuralNetwork neuralNetwork, Component comp) {
        super(neuralNetwork, comp);
        if (neuralNetwork != null && (neuralNetwork.getInputCount() != 3 || neuralNetwork.getOutputCount() != 2)) {
            System.out.println("Got " + neuralNetwork.getInputCount() + " " + neuralNetwork.getOutputCount());
            throw new IllegalArgumentException("Not appropriate NN for task!");
        }
        if (neuralNetwork != null) {
            this.angleUp = new AgentBooleanStateKeyboardInputHandler(comp, 0);
            this.angleDown = new AgentBooleanStateKeyboardInputHandler(comp, 0);
            this.fire = new AgentBooleanStateKeyboardInputHandler(comp, 0);
            super.inputHandlers = new AgentBooleanStateKeyboardInputHandler[]{angleUp, angleDown, fire};
        }
    }

    @Override
    public Map<String, Boolean> makeAction(Map<String, Double> state) {

        Double angle = state.get("angle");
        Double difX = state.get("difX");
        Double difY = state.get("difY");
        Double time = state.get("time");


        double[] result = super.neuralNetwork.calculateForInputs(new double[]{difX, difY, angle});
        Map<String, Boolean> actions = new HashMap<>();

        double rotate = Util.OPERATOR_SIGMOID.applyAsDouble(result[0]);
        double fire = Util.OPERATOR_SIGMOID.applyAsDouble(result[1]);
//        System.out.println(rotate);
//
//        System.out.println(fire);
        if (rotate > 0.2) {
            this.angleDown.setInactive();
            this.angleUp.setActive();
            actions.put("angleUp", true);
            actions.put("angleDown", false);

        } else if (rotate < -0.2) {
            this.angleUp.setInactive();
            this.angleDown.setActive();
            actions.put("angleDown", false);
            actions.put("angleUp", true);

        } else {
            this.angleUp.setInactive();
            this.angleDown.setInactive();
            actions.put("angleDown", false);
            actions.put("angleUp", false);
        }

        if (fire > 0 && time - lastTime > 50) {
            lastTime = time;
            this.fire.setHasBeenHandled(false);
            this.fire.setActive();
            actions.put("fire", true);
        } else {
            this.fire.setHasBeenHandled(false);
            this.fire.setInactive();
            actions.put("fire", false);

        }

        return actions;
//        this.fire.setInactive();
    }
}
