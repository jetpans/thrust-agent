package org.dyn4j.samples.logger;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Logger {
    private final String filename;

    boolean verbose = true;

    public Logger(String filename) {
        this.filename = filename;
    }

    public void clearLog() {
        try (FileWriter fw = new FileWriter(filename)) {
            fw.write("");
            fw.close();
            System.out.println("File cleared successfully.");
        } catch (IOException e) {
            System.err.println("An error occurred while clearing the file: " + e.getMessage());
        }
    }

    public boolean appendLog(String log) {
        if (verbose) System.out.println(log);
        try (FileWriter fw = new FileWriter(filename, true)) {
            fw.write(log + "\n");
            fw.close();
            return true;
        } catch (IOException e) {
            System.out.println("Couldn't append log");
            e.printStackTrace();
            return false;
        }
    }

    public List<String> readLines() {
        try {
            return Files.readAllLines(Paths.get(filename));
        } catch (IOException e) {
            System.err.println("An error occurred while reading the file: " + e.getMessage());
            return null;
        }
    }
}
