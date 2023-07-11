/*
 * MoonLight: a light-weight framework for runtime monitoring
 * Copyright (C) 2018-2021
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.moonlightsuite.moonlight.examples.temporal.afc;

import io.github.moonlightsuite.moonlight.io.DataWriter;
import io.github.moonlightsuite.moonlight.io.parsing.FileType;
import io.github.moonlightsuite.moonlight.io.parsing.PrintingStrategy;
import io.github.moonlightsuite.moonlight.io.parsing.RawTrajectoryExtractor;
import io.github.moonlightsuite.moonlight.util.Stopwatch;
import io.github.moonlightsuite.moonlight.api.MatlabRunner;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;

import static io.github.moonlightsuite.moonlight.examples.temporal.afc.AFCHelpers.repeatedRunner;
import static io.github.moonlightsuite.moonlight.examples.temporal.afc.AFCSettings.*;

public class AFCSimulatorRunner {
    private static final String OUTPUT_NAME = "/afc_sim_" + LAST_TIME + ".csv";
    private static final List<String> output = new ArrayList<>();
    private static final List<Stopwatch> stopwatches = new ArrayList<>();

    public static void main(String[] args) {
        LOG.info("Executing Breach AFC simulator...");
        repeatedRunner("AFC Simulator",
                        s -> executeBreachSimulator(stopwatches),
                        stopwatches, output);

        LOG.info("------> Experiment results (sec):");
        output.forEach(LOG::info);
    }

    static void executeBreachSimulator(List<Stopwatch> stopwatches) {
        // Matlab setup
        try(MatlabRunner matlab = new MatlabRunner(localPath())) {
        matlab.eval("clear");
        matlab.putVar("tot", LAST_TIME);

        // Model execution recording....
        Stopwatch rec = Stopwatch.start();
        matlab.eval("afc_moonlight_monitoring");
        long duration = rec.stop();
        stopwatches.add(rec);
        output.add("Simulink Model execution time: " + duration / 1000.);

        double[] input = matlab.getVar("input");
        assert input != null;
        double[][] input2 = new double[1][input.length];
        input2[0] = input;

        storeResults(input2);

        } catch (IOException e) {
            e.printStackTrace();
            throw new UncheckedIOException("Unable to access local path", e);
        }
    }

    private static void storeResults(double[][] data) {
        PrintingStrategy<double[][]> st = new RawTrajectoryExtractor(1);

        try {
            String destination = localPath() + OUTPUT_NAME;
            LOG.info("Saving output in: " + destination);
            new DataWriter<>(destination, FileType.CSV, st).write(data);
        } catch (IOException e) {
            e.printStackTrace();
            throw new UnknownError("Unable to write results");
        }
    }
}
