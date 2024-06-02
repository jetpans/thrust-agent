package org.dyn4j.samples.demos.functionTraining;

import org.dyn4j.samples.models.Util;

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleFunction;

public class FunctionDataGenerator {

    public static final int numberOfEntries = 30;
    public static final double LOWER_BOUND = -5;
    public static final double UPPER_BOUND = 5;

    public static void main(String[] args) {
        DoubleFunction<Double> myFunction = new DoubleFunction() {
            @Override
            public Object apply(double x) {
                return x * x - 3 * x + 5;
            }
        };
        List<List<Double>> entries = new ArrayList<>();
        for (int i = 0; i < numberOfEntries; i++) {
            double x = LOWER_BOUND + i * (UPPER_BOUND - LOWER_BOUND) / numberOfEntries;
            List<Double> entry = List.of(x, myFunction.apply(x));
            entries.add(entry);
        }

        Util.writeRegressionDataToFile("regression1.txt", entries);
    }
}
