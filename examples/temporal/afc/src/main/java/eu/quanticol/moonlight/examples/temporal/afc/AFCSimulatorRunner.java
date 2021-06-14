package eu.quanticol.moonlight.examples.temporal.afc;

import com.mathworks.engine.MatlabEngine;
import eu.quanticol.moonlight.io.DataWriter;
import eu.quanticol.moonlight.io.FileType;
import eu.quanticol.moonlight.io.parsing.PrintingStrategy;
import eu.quanticol.moonlight.io.parsing.RawTrajectoryExtractor;
import eu.quanticol.moonlight.util.Stopwatch;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class AFCSimulatorRunner {
    //TODO: set environment variable or replace with path to breach
    private static final String BREACH_PATH = System.getProperty("BREACH_PATH");

    private static final double LAST_TIME = 20;

    private static final String OUTPUT_NAME = "/afc_sim_" + LAST_TIME + ".csv";

    private static final List<String> output = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("Executing Breach AFC simulator...");

        runSimulation();

        System.out.println("------> Experiment results (sec):");
        for (String s : output) {
            System.out.println(s);
        }
    }

    private static void runSimulation() {
        MatlabEngine eng = matlabInit();
        try {
            assert eng != null;
            executeMoonlight(eng);
            eng.close();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    private static MatlabEngine matlabInit() {
        try {
            MatlabEngine eng = MatlabEngine.startMatlab();
            String localPath = localPath() ;

            //eng.eval("addpath(\"" + BREACH_PATH + "\")");
            eng.eval("addpath(\"" + localPath + "\")");

            return eng;
        } catch (ExecutionException |
                InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            return null;
        }
    }

    private static void executeMoonlight(MatlabEngine eng)
            throws ExecutionException, InterruptedException {
        // Model execution recording....
        eng.eval("clear");
        eng.putVariable("tot", LAST_TIME);
        Stopwatch rec = Stopwatch.start();
        eng.eval("afc_moonlight_monitoring");
        long duration = rec.stop();
        output.add("Simulink Model execution time: " + duration / 1000.);

        double[] input = eng.getVariable("input");
        double[][] input2 = new double[1][input.length];
        input2[0] = input;

        PrintingStrategy<double[][]> str = new RawTrajectoryExtractor(1);

        new DataWriter<>(localPath() + OUTPUT_NAME, FileType.CSV, str).write(input2);

        System.out.println("Saving output in: " + localPath() + OUTPUT_NAME);
    }

    public static String localPath() {
        try {
            return Paths.get(Objects.requireNonNull(
                    AFCSimulatorRunner.class
                            .getResource("matlab/afc_moonlight_monitoring.m"))
                    .toURI()).getParent().toAbsolutePath().toString();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return null;
    }
}
