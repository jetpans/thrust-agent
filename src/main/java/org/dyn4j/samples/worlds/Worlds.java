package org.dyn4j.samples.worlds;

import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.samples.framework.SimulationBody;
import org.dyn4j.world.World;

import java.util.ArrayList;

public class Worlds {
    private static ArrayList<WorldDescriptor> elements = new ArrayList<>();

    static {
        WorldDescriptor emptyWorld = new WorldDescriptor();
        emptyWorld.addBodyDescriptor(new BodyDescriptor(1, 10, -5, 0));
        emptyWorld.addBodyDescriptor(new BodyDescriptor(1, 10, 5, 0));
        emptyWorld.addBodyDescriptor(new BodyDescriptor(10, 1, 0, 5));
        emptyWorld.addBodyDescriptor(new BodyDescriptor(10, 1, 0, -5));
        elements.add(emptyWorld);

        WorldDescriptor oneRectWorld = new WorldDescriptor();

        oneRectWorld.addBodyDescriptor(new BodyDescriptor(1, 15, -5, 0));
        oneRectWorld.addBodyDescriptor(new BodyDescriptor(1, 15, 5, 0));
        oneRectWorld.addBodyDescriptor(new BodyDescriptor(15, 1, 0, 5));
        oneRectWorld.addBodyDescriptor(new BodyDescriptor(15, 1, 0, -5));
        oneRectWorld.addBodyDescriptor(new BodyDescriptor(5, 0.2, 0, 0));
        elements.add(oneRectWorld);

        WorldDescriptor rectMiddleWorld = new WorldDescriptor();

        rectMiddleWorld.addBodyDescriptor(new BodyDescriptor(1, 15, -5, 0));
        rectMiddleWorld.addBodyDescriptor(new BodyDescriptor(1, 15, 5, 0));
        rectMiddleWorld.addBodyDescriptor(new BodyDescriptor(15, 1, 0, 5));
        rectMiddleWorld.addBodyDescriptor(new BodyDescriptor(15, 1, 0, -5));
        rectMiddleWorld.addBodyDescriptor(new BodyDescriptor(0.2, 5, 0, 0));
        elements.add(rectMiddleWorld);

        WorldDescriptor boxMiddleWorld = new WorldDescriptor();

        boxMiddleWorld.addBodyDescriptor(new BodyDescriptor(1, 15, -5, 0));
        boxMiddleWorld.addBodyDescriptor(new BodyDescriptor(1, 15, 5, 0));
        boxMiddleWorld.addBodyDescriptor(new BodyDescriptor(15, 1, 0, 5));
        boxMiddleWorld.addBodyDescriptor(new BodyDescriptor(15, 1, 0, -5));
        boxMiddleWorld.addBodyDescriptor(new BodyDescriptor(3, 3, 0, 0));
        elements.add(boxMiddleWorld);

    }

    public static WorldDescriptor get(int index) {
        return elements.get(index);
    }

    public static WorldDescriptor getRandom() {
        return elements.get((int) Math.floor(Math.random() * elements.size()));
    }

    public static int count() {
        return elements.size();
    }
}
