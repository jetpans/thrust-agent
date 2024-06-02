package org.dyn4j.samples.agents;

import org.dyn4j.samples.framework.input.BooleanStateKeyboardInputHandler;
import org.dyn4j.samples.framework.input.Key;

import java.awt.*;

public class AgentBooleanStateKeyboardInputHandler extends BooleanStateKeyboardInputHandler {

    public AgentBooleanStateKeyboardInputHandler(Component component, Key... keys) {
        super(component, keys);
    }

    public AgentBooleanStateKeyboardInputHandler(Component component, int... keys) {
        super(component, keys);
    }

    public void setActive() {
        super.active = true;
    }

    public void setInactive() {
        super.active = false;
    }


}
