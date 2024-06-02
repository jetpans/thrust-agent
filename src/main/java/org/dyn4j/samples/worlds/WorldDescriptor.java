package org.dyn4j.samples.worlds;

import java.awt.*;
import java.util.ArrayList;

public class WorldDescriptor {

    private ArrayList<BodyDescriptor> bodyDescriptors = new ArrayList<>();

    public ArrayList<BodyDescriptor> getBodyDescriptors() {
        return this.bodyDescriptors;
    }

    ;

    public void addBodyDescriptor(BodyDescriptor desc) {
        this.bodyDescriptors.add(desc);
    }

    ;

}
