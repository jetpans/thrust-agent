package org.dyn4j.samples.agents;

import org.dyn4j.samples.framework.input.BooleanStateKeyboardInputHandler;
import org.dyn4j.samples.models.NeuralNetwork;
import org.dyn4j.samples.models.Util;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

public class NNThrustAgent extends AbstractNNAgent {
    public AgentBooleanStateKeyboardInputHandler up;
    public AgentBooleanStateKeyboardInputHandler down;
    public AgentBooleanStateKeyboardInputHandler left;
    public AgentBooleanStateKeyboardInputHandler right;

    public static int expectedInputs = 11;
    public static int expectedOutputs = 2;

    public NNThrustAgent(NeuralNetwork neuralNetwork, Component comp) {
        super(neuralNetwork, comp);
        //Inputs
        //Distance from collision front [~0, +inf]
        //Distance from collision back [~0, +inf]
        //Distance from collision left [~0, +inf]
        //Distance from collision right [~0, +inf]
        //linear Velocity [0, +inf]
        //Angle of linear velocity [-pi, pi]
        //Angular velocity [-inf, +inf]
        //Angle of orientation [-pi, pi]
        //Distance from trash [0, +inf]
        //Can see trash {-1, 1}
        //Direction angle from center of mass to trash [-pi, pi]
        //4x active inputs maybe?
        //Outputs: up/down, left/right
        if (neuralNetwork != null && (neuralNetwork.getInputCount() != expectedInputs || neuralNetwork.getOutputCount() != expectedOutputs)) {
            throw new IllegalArgumentException("Not appropriate NN dimensions.");
        }
        if (neuralNetwork != null) {
            this.up = new AgentBooleanStateKeyboardInputHandler(comp, 0);
            this.down = new AgentBooleanStateKeyboardInputHandler(comp, 0);
            this.left = new AgentBooleanStateKeyboardInputHandler(comp, 0);
            this.right = new AgentBooleanStateKeyboardInputHandler(comp, 0);

        }
    }

    @Override
    public Map<String, Boolean> makeAction(Map<String, Double> state) {

        double[] inputs =
                {
                        state.get("dFront"),
                        state.get("dBack"),
                        state.get("dLeft"),
                        state.get("dRight"),
                        state.get("lVelocity"),
                        state.get("lVelocityAngle"),
                        state.get("aVelocity"),
                        state.get("orientation"),
                        state.get("dTrash"),
                        state.get("canSeeTrash"),
                        state.get("trashAngle")

                };
        double[] result = super.neuralNetwork.calculateForInputs(inputs);
        double up = Util.OPERATOR_SIGMOID.applyAsDouble(result[0]);
        double left = Util.OPERATOR_SIGMOID.applyAsDouble(result[1]);


        Map<String, Boolean> actions = new HashMap<>();

        if (up > 0.2) {
            this.down.setInactive();
            this.up.setActive();
        } else if (up < -0.2) {
            this.up.setInactive();
            this.down.setActive();
        } else {
            this.up.setInactive();
            this.down.setInactive();
        }

        if (left > 0.2) {
            this.right.setInactive();
            this.left.setActive();
        } else if (left < -0.2) {
            this.left.setInactive();
            this.right.setActive();

        } else {
            this.left.setInactive();
            this.right.setInactive();

        }

        actions.put("up", up > 0.2);
        actions.put("down", up < -0.2);
        actions.put("right", left < -0.2);
        actions.put("left", left > 0.2);
        return actions;
    }
}
