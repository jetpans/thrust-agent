package org.dyn4j.samples.models;


import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.ejml.simple.SimpleMatrix;

import java.io.Serializable;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

public class NeuralNetwork implements Serializable {
    public long serialVersionUID = 1L;
    private final static double LOWER_BOUND = 1;
    private final static double UPPER_BOUND = -1;
    private List<NNLayer> layers = new ArrayList<>();

    private NNMutator myMutator;

    private NNCrossover myCrossover;

    public DoubleUnaryOperator transferFunction;

    private int inputs;
    private int outputs;

    public NeuralNetwork() {
    }

    private NeuralNetwork(List<NNLayer> layers, DoubleUnaryOperator transferFunction, NNMutator myMutator, NNCrossover myCrossover) {
        this.myMutator = myMutator;
        this.myCrossover = myCrossover;
        this.layers = layers;
        this.inputs = this.layers.getFirst().getWeights().getNumRows();
        this.outputs = this.layers.getLast().getWeights().getNumCols();
        this.transferFunction = transferFunction;
    }

    /**
     * Constructs a random neural network with given parameters using the sigmoid function as transfer function
     *
     * @param numberOfInputs  Number of inputs to the NN
     * @param numberOfLayers  INCLUDING THE INPUT AND OUTPUT LAYER
     * @param layerSize       Number of nodes in each layer
     * @param numberOfOutputs Number of outputs from the NN
     * @return
     */
    public static NeuralNetwork constructRandomNeuralNetwork(int numberOfInputs, int numberOfLayers, int layerSize, int numberOfOutputs, DoubleUnaryOperator transferFunction, NNMutator myMutator, NNCrossover myCrossover) {
        if (numberOfLayers < 2) {
            throw new IllegalArgumentException("Must have at least 2 layers.");
        }
        //First layer has to accept numberOfInputs and output layerSize outputs
        //Second to numberOfLayers-1 have to accept layerSize inputs and output layerSize outputs
        //Final layer has to accept layerSize inputs and output numberOfOutputs
        List<NNLayer> myLayers = new ArrayList<>();
        myLayers.add(constructRandomLayer(layerSize, numberOfInputs, transferFunction));
        for (int i = 0; i < numberOfLayers - 2; i++) {
            myLayers.add(constructRandomLayer(layerSize, layerSize, transferFunction));
        }
        myLayers.add(constructRandomLayer(numberOfOutputs, layerSize, transferFunction));

        return new NeuralNetwork(myLayers, transferFunction, myMutator, myCrossover);
    }


    private static NNLayer constructRandomLayer(int numberOfNodes, int numberOfInputs, DoubleUnaryOperator transferFunction) {
        SimpleMatrix myWeights = new SimpleMatrix(numberOfInputs, numberOfNodes);
        for (int i = 0; i < numberOfInputs; i++) {
            for (int j = 0; j < numberOfNodes; j++) {
                myWeights.set(i, j, Util.randomDoubleFromInterval(LOWER_BOUND, UPPER_BOUND, Math.random()));
            }
        }
        SimpleMatrix myBiases = new SimpleMatrix(1, numberOfNodes);
        for (int i = 0; i < numberOfNodes; i++) {
            myBiases.set(0, i, Util.randomDoubleFromInterval(LOWER_BOUND, UPPER_BOUND, Math.random()));
        }
        NNLayer myLayer = new NNLayer(myWeights, myBiases, transferFunction);
        return myLayer;
    }


    public double[] calculateForInputs(double[] inputs) {
        SimpleMatrix current = new SimpleMatrix(1, inputs.length);
        current.setRow(0, 0, inputs);
        NNLayer last = this.layers.getLast();
        for (NNLayer l : this.layers) {
            if (l != last) {
                current = l.apply(current);
                current = Util.applyTransferFunction(current, l.transferFunction);
            }
//            System.out.println(Util.getDimension(l.getWeights()));
        }
        current = last.apply(current);
        return current.getRow(0).getDDRM().data;

    }


    public void mutate() {
        myMutator.mutate(this);
    }

    /**
     * Crossovers this neural network with other. Produces NN[] of length 2 with two "symmetrical" offsprings
     * It does so by swapping layers
     *
     * @param other
     * @return
     */
    public NeuralNetwork[] crossoverWith(NeuralNetwork other) {
        return myCrossover.crossover(this, other);
    }

    public NeuralNetwork clone() {
        List<NNLayer> newLayers = new ArrayList<>();
        for (NNLayer l : this.layers) {
            newLayers.add(l.clone());
        }
        return new NeuralNetwork(newLayers, this.transferFunction, this.myMutator, this.myCrossover);
    }

    public List<NNLayer> getLayers() {
        return this.layers;
    }

    public int getInputCount() {
        return this.inputs;
    }

    public int getOutputCount() {
        return this.outputs;
    }

    public static void main(String[] args) {
//        NeuralNetwork myNetwork = constructRandomNeuralNetwork(3, 4, 5, 1);
//        double[] inputs = {-1, 0, 1};
//        double[] result = myNetwork.calculateForInputs(inputs);
//        for (double d : result) {
//            System.out.println(d);
//        }
    }

    public interface NNMutator extends Serializable {
        public void mutate(NeuralNetwork nn);
    }

    public interface NNCrossover extends Serializable {
        public NeuralNetwork[] crossover(NeuralNetwork first, NeuralNetwork second);
    }

    public static class TwoPointCrossover implements NNCrossover {
        @Override
        public NeuralNetwork[] crossover(NeuralNetwork first, NeuralNetwork second) {
            NeuralNetwork offspring1 = first.clone();
            NeuralNetwork offspring2 = second.clone();
            int numOfLayers = first.getLayers().size();
            int position1 = Util.randomIntFromInterval(0, numOfLayers, Math.random());
            int position2 = Util.randomIntFromInterval(position1, numOfLayers, Math.random());
            for (int i = position1; i <= position2; i++) {
                NNLayer temp = offspring1.getLayers().get(i);
                offspring1.getLayers().set(i, offspring2.getLayers().get(i));
                offspring2.getLayers().set(i, temp);
            }

            return new NeuralNetwork[]{offspring1, offspring2};
        }
    }


    public static class UniformCrossover implements NNCrossover {
        public double probability;

        public UniformCrossover(double probability) {
            this.probability = probability;
        }

        @Override
        public NeuralNetwork[] crossover(NeuralNetwork first, NeuralNetwork second) {
            NeuralNetwork offspring1 = first.clone();
            NeuralNetwork offspring2 = second.clone();
            int numOfLayers = first.getLayers().size();
            for (int i = 0; i < numOfLayers; i++) {
                if (Math.random() < this.probability) {
                    NNLayer temp = offspring1.getLayers().get(i);
                    offspring1.getLayers().set(i, offspring2.getLayers().get(i));
                    offspring2.getLayers().set(i, temp);
                }
            }

            return new NeuralNetwork[]{offspring1, offspring2};
        }
    }


    public static class UniformNeuronCrossover implements NNCrossover {
        public double probability;

        public UniformNeuronCrossover(double probability) {
            this.probability = probability;
        }

        @Override
        public NeuralNetwork[] crossover(NeuralNetwork first, NeuralNetwork second) {
            NeuralNetwork offspring1 = first.clone();
            NeuralNetwork offspring2 = second.clone();
            int numOfLayers = first.getLayers().size();
            for (int i = 0; i < numOfLayers; i++) {
                NNLayer layer1 = offspring1.getLayers().get(i);
                NNLayer layer2 = offspring2.getLayers().get(i);
                for (int j = 0; j < layer2.getWeights().getNumCols(); j++) {
                    if (Math.random() < this.probability) {
                        SimpleMatrix node1 = layer1.getWeights().getColumn(j).copy();
                        SimpleMatrix node2 = layer2.getWeights().getColumn(j).copy();
                        layer1.getWeights().setColumn(j, node2);
                        layer2.getWeights().setColumn(j, node1);
                        double tempBias = layer1.getBiases().get(j);
                        layer1.getBiases().set(j, layer2.getBiases().get(j));
                        layer2.getBiases().set(j, tempBias);
                    }
                }

            }

            return new NeuralNetwork[]{offspring1, offspring2};
        }
    }

    public static class NormalMutator implements NNMutator {
        private double stdDev = 1;
        private double percentage = 0.1;

        public NormalMutator(double stdDev, double percentage) {
            this.stdDev = stdDev;
            this.percentage = percentage;
        }

        public void mutate(NeuralNetwork nn) {
            for (int l = 1; l < nn.layers.size() - 1; l++) {
                NNLayer curLayer = nn.layers.get(l);
                int dimJ = curLayer.getWeights().getNumCols();
                int dimI = curLayer.getWeights().getNumRows();
                for (int n = 0; n < dimJ; n++) {
                    for (int w = 0; w < dimI; w++) {
                        if (Math.random() < percentage) {
                            NormalDistribution distribution = new NormalDistribution(curLayer.getWeights().get(w, n), this.stdDev);
                            curLayer.getWeights().set(w, n, distribution.sample());
                        }
                    }
                }
                for (int i = 0; i < dimJ; i++) {
                    if (Math.random() < percentage) {
                        NormalDistribution distribution = new NormalDistribution(curLayer.getBiases().get(0, i), this.stdDev);
                        curLayer.getBiases().set(0, i, distribution.sample());

                    }
                }
            }
        }
    }

    public static class UniformMutator implements NNMutator {

        private double rangeStart = 1;
        private double rangeEnd = 1;
        private double percentage = 0.1;

        public UniformMutator(double rangeStart, double rangeEnd, double percentage) {
            if (rangeStart > rangeEnd) {
                throw new IllegalArgumentException("Start > end.");
            }
            this.rangeStart = rangeStart;
            this.rangeEnd = rangeEnd;
            this.percentage = percentage;
        }


        public void mutate(NeuralNetwork nn) {
            for (int l = 1; l < nn.layers.size() - 1; l++) {
                NNLayer curLayer = nn.layers.get(l);
                int dimJ = curLayer.getWeights().getNumCols();
                int dimI = curLayer.getWeights().getNumRows();
                for (int n = 0; n < dimJ; n++) {
                    for (int w = 0; w < dimI; w++) {
                        if (Math.random() < this.percentage) {
                            UniformRealDistribution distribution = new UniformRealDistribution(rangeStart, rangeEnd);
                            curLayer.getWeights().set(w, n, distribution.sample());
                        }
                    }
                }
                for (int i = 0; i < dimJ; i++) {
                    if (Math.random() < this.percentage) {
                        UniformRealDistribution distribution = new UniformRealDistribution(rangeStart, rangeEnd);
                        curLayer.getBiases().set(0, i, distribution.sample());

                    }
                }
            }
        }
    }
}
