package org.dyn4j.samples.Tools;

import org.dyn4j.samples.demos.thrustTraining.ThrustCarouselSelection;

public class AutoRunner {

    public static void main(String[] args) {
        String[] settingsFiles = {

                "src/main/java/org/dyn4j/samples/resources/settings.properties"


        };
        for (String filename : settingsFiles) {
            for (int i = 0; i < 300; i++) {
                ThrustCarouselSelection.goCondition = true;
                ThrustCarouselSelection.main(new String[]{filename});
            }
        }
    }
}
