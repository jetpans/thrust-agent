package org.dyn4j.samples.logger;

import org.math.plot.Plot2DPanel;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AlgorithmAnalyzer {
    private Logger logger;

    private static class DataEntry {
        int iteration;
        double alphaFitness;
        double averagePopulationFitness;

        double testAlphaFitness;
        double testAveragePopulationFitness;
        int populationSize;
        int alphaId;

        public DataEntry(int iteration, double alphaFitness, double testAlphaFitness, double averagePopulationFitness, double testAveragePopulationFitness, int populationSize, int alphaId) {
            this.iteration = iteration;
            this.alphaFitness = alphaFitness;
            this.testAlphaFitness = testAlphaFitness;
            this.averagePopulationFitness = averagePopulationFitness;
            this.testAveragePopulationFitness = testAveragePopulationFitness;
            this.populationSize = populationSize;
            this.alphaId = alphaId;
        }

        @Override
        public String toString() {
            return String.format("IT: %d Alpha(%d): %f TestAlpha: %f Average: %f TestAverage: %f PopSize: %d", iteration, alphaId, alphaFitness, testAlphaFitness, averagePopulationFitness, testAveragePopulationFitness, populationSize);
        }
    }

    private HashMap<Integer, DataEntry> data;

    public AlgorithmAnalyzer(Logger l) {
        this.logger = l;
        this.data = new HashMap<>();
    }

    public void addEntry(int iteration, double alphaFitness, double testAlphaFitness, double averagePopulationFitness, double testAveragePopulationFitness, int populationSize, int alphaId) {
        DataEntry newEntry = new DataEntry(iteration, alphaFitness, testAlphaFitness, averagePopulationFitness, testAveragePopulationFitness, populationSize, alphaId);
        data.put(iteration, newEntry);
        logger.appendLog(newEntry.toString());
    }

    public void plotResults(String location) {
        double[] iters = this.data.keySet().stream().mapToDouble(dataEntry -> dataEntry).toArray();
        double[] alphas = this.data.entrySet().stream().mapToDouble((Map.Entry<Integer, DataEntry> e) -> -e.getValue().alphaFitness).toArray();
        double[] averages = this.data.entrySet().stream().mapToDouble((Map.Entry<Integer, DataEntry> e) -> -e.getValue().averagePopulationFitness).toArray();
        double[] testAlphas = this.data.entrySet().stream().mapToDouble((Map.Entry<Integer, DataEntry> e) -> -e.getValue().testAlphaFitness).toArray();
        double[] testAverages = this.data.entrySet().stream().mapToDouble((Map.Entry<Integer, DataEntry> e) -> -e.getValue().testAveragePopulationFitness).toArray();

        Plot2DPanel plot = new Plot2DPanel();
        plot.addLinePlot("Alpha fitness", iters, alphas);
        plot.addLinePlot("NN Prediction", iters, averages);
        plot.addLinePlot("Test alpha fitness", iters, testAlphas);
        plot.addLinePlot("Test average fitness", iters, testAverages);
        plot.setPreferredSize(new Dimension(600, 600));

        JFrame frame = new JFrame("Plot of result");
        frame.setSize(800, 600);
        frame.setContentPane(plot);
        frame.setVisible(true);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);
                frame.dispose();
            }
        });

        try {
            Thread.sleep(1000);
            plot.toGraphicFile(new File(location));

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


}
