package org.dyn4j.samples.models;

import org.apache.commons.math3.distribution.GeometricDistribution;
import org.apache.commons.math3.linear.RealMatrix;
import org.ejml.simple.SimpleMatrix;
import org.locationtech.jts.algorithm.LineIntersector;
import org.locationtech.jts.geom.Coordinate;

import java.io.*;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

public class Util {
    public static final DoubleUnaryOperator OPERATOR_SIGMOID = (DoubleUnaryOperator & Serializable) operand -> 2 / (1 + Math.pow(Math.E, -operand)) - 1;

    public static final DoubleUnaryOperator OPERATOR_IDENTITY = (DoubleUnaryOperator & Serializable) operand -> operand;
    public static final DoubleUnaryOperator OPERATOR_SIGMOID_BIG = (DoubleUnaryOperator & Serializable) operand -> 500 / (1 + Math.pow(Math.E, -operand)) - 250;

    public static final DoubleUnaryOperator OPERATOR_SWISH = (DoubleUnaryOperator & Serializable) operand -> operand * 1 / (1 + Math.pow(Math.E, -operand));


    private static GeometricDistribution geometricDistribution = new GeometricDistribution(0.45);

    public static String getDimension(RealMatrix m) {
        return String.format("%d x %d", m.getRowDimension(), m.getColumnDimension());
    }

    public static String getDimension(SimpleMatrix m) {
        return String.format("%d x %d", m.getNumRows(), m.getNumCols());
    }

    public static void printMatrix(RealMatrix m) {
        for (int i = 0; i < m.getRowDimension(); i++) {
            double[] curRow = m.getRow(i);
            String row = "";
            for (int j = 0; j < m.getColumnDimension(); j++) {
                row = row + curRow[j] + " ";
            }
            System.out.println(row);
        }
    }

    public static void printMatrix(SimpleMatrix m) {
        System.out.println(m.toString());
//        for (int i = 0; i < m.getNumRows(); i++) {
//            double[] curRow = m.getRow(i).toArray2()[m.getNumRows()];
//            String row = "";
//            for (int j = 0; j < m.getNumCols(); j++) {
//                row = row + curRow[j] + " ";
//            }
//            System.out.println(row);
//        }
    }

    public static double randomDoubleFromInterval(double lb, double ub, double random) {
        return lb + random * (ub - lb);
    }

    public static int randomIntFromInterval(int lb, int ub, double random) {
        return lb + (int) Math.round(Math.floor(random * (1.0 * ub - 1.0 * lb)));
    }

    public static int randomIntFromIntervalGeometric(double lb, double ub) {
        return (int) Math.min(lb + geometricDistribution.sample(), ub);
    }

    public static List<List<Double>> readRegressionDataFromFile(String filename) {
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename));
            List<List<Double>> result = (List<List<Double>>) in.readObject();


            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public static void writeRegressionDataToFile(String filename, List<List<Double>> data) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));
            out.writeObject(data);
        } catch (IOException e) {
            return;
        }
    }

    public static void writeNeuralNetworkToFile(String filename, NeuralNetwork network) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));
            out.writeObject(network);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    public static NeuralNetwork loadNeuralNetworkFromFile(String filename, DoubleUnaryOperator transferFunction) {
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename));
            NeuralNetwork result = (NeuralNetwork) in.readObject();
            result.transferFunction = transferFunction;
            for (NNLayer l : result.getLayers()) {
                l.transferFunction = transferFunction;
            }

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static NeuralNetwork loadNeuralNetworkFromFile(String filename) {
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename));
            NeuralNetwork result = (NeuralNetwork) in.readObject();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean rangeOverlap(double x1, double x2, double y1, double y2) {
        return Math.min(x1, x2) <= Math.max(y1, y2) && Math.min(y1, y2) <= Math.max(x1, x2);
    }

    public static SimpleMatrix applyTransferFunction(SimpleMatrix subject, DoubleUnaryOperator function) {
        SimpleMatrix result = subject.copy();
        for (int i = 0; i < result.getNumRows(); i++) {
            for (int j = 0; j < result.getNumCols(); j++) {
                result.set(i, j, function.applyAsDouble(result.get(i, j)));
            }
        }
        return result;
    }


    public static Coordinate getDistantCoordinate(double x, double y, double angle) {
        double radius = 10000;
        return new Coordinate(x + Math.cos(angle) * radius, y + Math.sin(angle) * radius);
    }

    public static double getDistanceToIntersection(Coordinate c1, Coordinate c2, Coordinate c3, Coordinate c4, LineIntersector i) {
        i.computeIntersection(c1, c2, c3, c4);
        if (i.hasIntersection()) {
            Coordinate intersection = i.getIntersection(0);
            return c1.distance(intersection);
        } else {
            return Float.POSITIVE_INFINITY;
        }
    }

    public static double normalizeAngle(double angle) {
        if (angle > Math.PI) {
            return angle - 2 * Math.PI;
        } else if (angle < -Math.PI) {
            return angle + 2 * Math.PI;
        }
        return angle;
    }

}
