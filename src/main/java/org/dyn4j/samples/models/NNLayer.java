package org.dyn4j.samples.models;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealMatrixChangingVisitor;
import org.ejml.simple.SimpleMatrix;

import java.io.Serial;
import java.io.Serializable;
import java.util.function.DoubleFunction;
import java.util.function.DoubleUnaryOperator;

public class NNLayer implements Serializable {
    private SimpleMatrix weights;
    private SimpleMatrix biases;

    public DoubleUnaryOperator transferFunction;
    private int nodeCount = 0;
    private int acceptsInputs = 0;

    /**
     * Each node's weights are in a vector column.
     * Each node is its own column.
     * Biases is a vector row.
     *
     * @param weights
     * @param biases
     */
    public NNLayer(SimpleMatrix weights, SimpleMatrix biases, DoubleUnaryOperator transferFunction) {
        if (weights.getNumCols() != biases.getNumCols()
                || biases.getNumRows() != 1) {
            System.out.println(String.format("weights: %s\nbiases: %s", Util.getDimension(weights), Util.getDimension(biases)));
            throw new IllegalArgumentException("Wrong weights and  biases structure.");
        }


        this.weights = weights;
        this.biases = biases;
        this.nodeCount = weights.getNumCols();
        this.acceptsInputs = weights.getNumRows();
        this.transferFunction = transferFunction;

    }


    /**
     * Applies this layer to the inputs
     * Returns a vector of length same as node count
     * Inputs must be a vector row.
     *
     * @param inputs
     * @return
     */
    public SimpleMatrix apply(SimpleMatrix inputs) {
        if (inputs.getNumCols() != this.acceptsInputs) {
            throw new IllegalArgumentException("Inputs of wrong dimension.");
        }
        SimpleMatrix result = null;
        try {
            result = inputs.mult(weights).plus(biases);
        } catch (Exception e) {
            System.out.println(inputs.toString());
            System.out.println(weights);
            System.exit(0);
        }
        
        return result;
    }


    public SimpleMatrix getWeights() {
        return weights;
    }

    public SimpleMatrix getBiases() {
        return this.biases;
    }

    public NNLayer clone() {
        return new NNLayer(this.weights.copy(), this.biases.copy(), this.transferFunction);
    }


    public void setWeights(SimpleMatrix weights) {
        this.weights = weights;
    }


}
