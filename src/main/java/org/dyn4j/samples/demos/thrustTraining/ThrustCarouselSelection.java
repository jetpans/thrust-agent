package org.dyn4j.samples.demos.thrustTraining;

import org.apache.commons.math3.genetics.Fitness;
import org.dyn4j.samples.Thrust;
import org.dyn4j.samples.demos.thrustTraining.fitnessFunctions.AdvancedFitness;
import org.dyn4j.samples.demos.thrustTraining.fitnessFunctions.DefaultFitness;
import org.dyn4j.samples.demos.thrustTraining.fitnessFunctions.FitnessFunction;
import org.dyn4j.samples.demos.thrustTraining.fitnessFunctions.TestFitness;
import org.dyn4j.samples.logger.AlgorithmAnalyzer;
import org.dyn4j.samples.logger.Logger;
import org.dyn4j.samples.models.NeuralNetwork;
import org.dyn4j.samples.models.Util;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.DoubleUnaryOperator;

public class ThrustCarouselSelection extends JFrame {

    public static int tickLimit;

    private static FitnessFunction myFitness;

    public static int ITERS;

    private static boolean goCondition = true;


    private static class FitnessNeuralNetwork implements Comparator<FitnessNeuralNetwork>, Comparable<FitnessNeuralNetwork> {
        NeuralNetwork nn;
        double fitness = 0;

        public FitnessNeuralNetwork(NeuralNetwork nn) {
            this.nn = nn;
            this.fitness = fitness(nn);
        }

        void evaluate() {
            this.fitness = fitness(nn);
        }

        @Override
        public int compare(FitnessNeuralNetwork o1, FitnessNeuralNetwork o2) {
            return Double.compare(o1.fitness, o2.fitness);
        }

        @Override
        public int compareTo(FitnessNeuralNetwork o) {
            return Double.compare(this.fitness, o.fitness);
        }
    }


    public static double fitness(NeuralNetwork nn) {
        return ThrustCarouselSelection.myFitness.fitness(nn);
    }

    public static void main(String[] args) {
        String t = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        final String folderName = "results/" + t + "/";
        new File(folderName).mkdirs();
        AlgorithmAnalyzer analyzer = new AlgorithmAnalyzer(new Logger(folderName + "log.txt"));
        initKeyboardStopper();

        Properties prop = new Properties();
        int POPULATION_SIZE = 0, THREADS = 0, steps = 0, logFrequency = 1;
        int numOfL = 0, numOfN = 0;
        double KEEP_PERCENT = 0;
        String alphaFile = "", fitnessMode = "", mutatorMode = "", crossoverMode = "";
        DoubleUnaryOperator transferFunction;
        int saveId = 0;
        NeuralNetwork.NNMutator m = null;
        NeuralNetwork.NNCrossover c = null;
        int inputs = 11;
        int outputs = 2;

        try (FileInputStream fis = new FileInputStream("src/main/java/org/dyn4j/samples/resources/settings.properties")) {
            prop.load(fis);
            numOfL = Integer.parseInt(prop.getProperty("tcs.numOfLayers"));
            numOfN = Integer.parseInt(prop.getProperty("tcs.numOfNodesPerLayer"));
            logFrequency = Integer.parseInt(prop.getProperty("tcs.logFrequency"));
            POPULATION_SIZE = Integer.parseInt(prop.getProperty("tcs.population"));
            ThrustCarouselSelection.tickLimit = Integer.parseInt(prop.getProperty("tcs.tickLimit"));
            THREADS = Integer.parseInt(prop.getProperty("tcs.threads"));
            steps = Integer.parseInt(prop.getProperty("tcs.stepLimit"));
            alphaFile = prop.getProperty("tcs.saveAlphaToFile");
            KEEP_PERCENT = Double.parseDouble(prop.getProperty("tcs.keep")) / POPULATION_SIZE;
            ITERS = Integer.parseInt(prop.getProperty("tcs.iters"));

            fitnessMode = prop.getProperty("tcs.fitnessMode");
            switch (fitnessMode) {
                case "default":
                    ThrustCarouselSelection.myFitness = new DefaultFitness();
                    break;
                case "advanced":
                    ThrustCarouselSelection.myFitness = new AdvancedFitness();
                    break;
                default:
                    System.out.println("Wrong fitness type.");
                    System.exit(-1);
            }

            mutatorMode = prop.getProperty("tcs.mutator");
            double mutatorPercentage = Double.parseDouble(prop.getProperty("tcs.mutatorPercentage"));
            switch (mutatorMode) {

                case "normal":
                    double stdDev = Double.parseDouble(prop.getProperty("tcs.mutatorStdDev"));
                    m = new NeuralNetwork.NormalMutator(stdDev, mutatorPercentage);
                    break;
                case "uniform":
                    double rangeStart = Double.parseDouble(prop.getProperty("tcs.mutatorRangeStart"));
                    double rangeEnd = Double.parseDouble(prop.getProperty("tcs.mutatorRangeEnd"));
                    m = new NeuralNetwork.UniformMutator(rangeStart, rangeEnd, mutatorPercentage);
                    break;
                default:
                    System.out.println("Bad mutator.");
                    System.exit(-1);

            }

            crossoverMode = prop.getProperty("tcs.crossover");
            switch (crossoverMode) {
                case "twoPoint":
                    c = new NeuralNetwork.TwoPointCrossover();
                    break;
                case "uniform":
                    double percentage = Double.parseDouble(prop.getProperty("tcs.crossoverProbability"));
                    c = new NeuralNetwork.UniformCrossover(percentage);
                    break;
                case "uniformNeuron":
                    percentage = Double.parseDouble(prop.getProperty("tcs.crossoverProbability"));
                    c = new NeuralNetwork.UniformNeuronCrossover(percentage);
                    break;
                default:
                    System.out.println("Bad crossover");
                    System.exit(-1);
            }


        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Can't read settings.");
            System.exit(-1);
        }
        final NeuralNetwork.NNMutator mutator = m;
        final NeuralNetwork.NNCrossover crossover = c;
        final int numOfLayers = numOfL;
        final int numOfNodes = numOfN;
        DoubleUnaryOperator fun = Util.OPERATOR_SIGMOID;
        double bestResult = Double.POSITIVE_INFINITY;
        NeuralNetwork alpha = null;


        ExecutorService executorService = Executors.newFixedThreadPool(THREADS);
        FitnessNeuralNetwork[] population = new FitnessNeuralNetwork[POPULATION_SIZE];
        List<Future<FitnessNeuralNetwork>> futures = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            futures.add(executorService.submit(() -> new FitnessNeuralNetwork(NeuralNetwork.constructRandomNeuralNetwork(inputs, numOfLayers, numOfNodes, outputs, fun, mutator, crossover))));
        }
        executorService.shutdown();

        try {
            boolean res = executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            if (!res) {
                System.out.println("AAAAAH TIMEOUT");
            }
            int i = 0;
            for (Future<FitnessNeuralNetwork> future : futures) {
                if (future != null) {
                    population[i] = future.get();
                    i++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Arrays.sort(population);
        if (population[0].fitness < bestResult) {
            bestResult = population[0].fitness;
            alpha = population[0].nn;
            System.out.println("Saving Alpha on ID " + saveId + " : " + bestResult + " to file " + folderName + alphaFile + saveId);
            Util.writeNeuralNetworkToFile(folderName + alphaFile + saveId++, alpha);

        }
        int ctr = 0;
        int staledCtr = 0;
        while (steps-- > 0 && goCondition) {
            if (steps % logFrequency == 0) {
//                analyzer.addEntry(ctr++,
//                        population[0].fitness,
//                        testFitness.fitness(population[0].nn),
//                        Arrays.stream(population).mapToDouble((FitnessNeuralNetwork f) -> f.fitness).average().getAsDouble(),
//                        Arrays.stream(population).mapToDouble((FitnessNeuralNetwork f) -> testFitness.fitness(f.nn)).average().getAsDouble(),
//                        POPULATION_SIZE);
                analyzer.addEntry(ctr++,
                        population[0].fitness,
                        0,
                        Arrays.stream(population).mapToDouble((FitnessNeuralNetwork f) -> f.fitness).average().getAsDouble(),
                        0, POPULATION_SIZE, saveId - 1);
            }
            if (staledCtr == 100) {
                goCondition = false;
            }
            staledCtr++;
            executorService = Executors.newFixedThreadPool(THREADS);
            FitnessNeuralNetwork[] newPopulation = new FitnessNeuralNetwork[POPULATION_SIZE];
            futures = new ArrayList<>();

            int counter = 0;
            for (int i = 0; i < POPULATION_SIZE * KEEP_PERCENT; i++) {
                newPopulation[i] = population[i];
                counter++;
            }
            int startFrom = counter;
            for (; counter < POPULATION_SIZE - 1; counter += 2) {

                int firstIndex = Util.randomIntFromIntervalGeometric(0, POPULATION_SIZE - 1);
                int secondIndex = Util.randomIntFromIntervalGeometric(firstIndex + 1, POPULATION_SIZE - 1);
                NeuralNetwork[] children = population[firstIndex].nn.crossoverWith(population[secondIndex].nn);
                children[0].mutate();
                children[1].mutate();
                futures.add(executorService.submit(() -> new FitnessNeuralNetwork(children[0])));
                futures.add(executorService.submit(() -> new FitnessNeuralNetwork(children[1])));
//                newPopulation[counter] = new FitnessNeuralNetwork(children[0]);
//                newPopulation[counter + 1] = new FitnessNeuralNetwork(children[1]);
            }
            for (; counter < POPULATION_SIZE; counter++) {
                futures.add(executorService.submit(() -> new FitnessNeuralNetwork(NeuralNetwork.constructRandomNeuralNetwork(inputs, numOfLayers, numOfNodes, outputs, fun, mutator, crossover))));
//                newPopulation[counter] = new FitnessNeuralNetwork(NeuralNetwork.constructRandomNeuralNetwork(3, numOfLayers, numOfNodes, 2, fun));
            }
//            if (executorService instanceof ThreadPoolExecutor) {
//                System.out.println("Active threads: " + ((ThreadPoolExecutor) executorService).getActiveCount());
//            }
            executorService.shutdown();
            try {
                boolean res = executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                if (!res) {
                    System.out.println("AAAAAH TIMEOUT");
                }
                for (Future<FitnessNeuralNetwork> future : futures) {
                    if (future != null) {
                        newPopulation[startFrom] = future.get();
                        startFrom++;
                    }
                }
                if (startFrom != POPULATION_SIZE) {
                    System.out.println("Bad logic happened.");
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            population = newPopulation;
            Arrays.sort(population);

            if (population[0].fitness < bestResult) {
                staledCtr = 0;
                bestResult = population[0].fitness;
                alpha = population[0].nn;
                System.out.println("Saving Alpha on ID " + saveId + " : " + bestResult + " to file " + folderName + alphaFile + saveId);
                Util.writeNeuralNetworkToFile(folderName + alphaFile + saveId++, alpha);

            }

            if (steps % 100 == 0) {
                System.out.println(steps);
            }
        }

        analyzer.plotResults(folderName + "img.png");
        try {
            Files.copy(Path.of("src/main/java/org/dyn4j/samples/resources/settings.properties"), Path.of(folderName + "settings.properties"), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.out.println("Didnt save settings");
        }
    }

    static void initKeyboardStopper() {


        java.awt.Frame frame = new java.awt.Frame();
        KeyListener keyListener = new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                System.out.println("Pressed something");
                if (e.getKeyChar() == 's') {
                    ThrustCarouselSelection.goCondition = false;
                    frame.dispose();
                    frame.setVisible(false);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        };
        frame.addKeyListener(keyListener);
        frame.setVisible(true);
    }

}
