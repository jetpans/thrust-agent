package org.dyn4j.samples.worlds;

public class BodyDescriptor {
    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public double getTranslateX() {
        return translateX;
    }

    public double getTranslateY() {
        return translateY;
    }

    private double width;
    private double height;
    private double translateX;
    private double translateY;

    public BodyDescriptor(double width, double height, double translateX, double translateY) {
        this.width = width;
        this.height = height;
        this.translateX = translateX;
        this.translateY = translateY;
    }
}
