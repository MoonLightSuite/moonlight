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

package eu.quanticol.moonlight.examples.temporal.afc;

import eu.quanticol.moonlight.core.base.AbstractInterval;
import eu.quanticol.moonlight.core.signal.Sample;
import eu.quanticol.moonlight.online.signal.TimeSegment;
import eu.quanticol.moonlight.util.Stopwatch;
import eu.quanticol.moonlight.api.MatlabRunner;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static eu.quanticol.moonlight.examples.temporal.afc.AFCHelpers.*;
import static eu.quanticol.moonlight.examples.temporal.afc.AFCMoonlightRunner.moonlight;
import static eu.quanticol.moonlight.examples.temporal.afc.AFCSettings.*;

public class AbstractFuelControl {

    private static double[] input;

    private static final List<String> output = new ArrayList<>();
    private static final List<Stopwatch> stopwatches = new ArrayList<>();

    public static void main(String[] args) {
        repeatedRunner("In-Order M",
                       s -> moonlight(false, LAST_TIME, s, true),
                       stopwatches, output);

        repeatedRunner("Out-Of-Order M",
                       s -> moonlight(true, LAST_TIME, s, false),
                       stopwatches, output);

        repeatedRunner("Breach", AbstractFuelControl::runBreach,
                       stopwatches, output);


        LOG.info("------> Experiment results (sec):");

        output.forEach(LOG::info);

        if(PLOTTING)
            plt.plot(Arrays.stream(input)
                           .boxed().collect(Collectors.toList()),
                    "alw_[10, 30] ((abs(AF[t]-AFRef[t]) > 0.05) => " +
                            "(ev_[0, 1] (abs(AF[t]-AFRef[t]) < 0.05)))",
                    "|AF-AFRef|");
    }

    private static void runBreach(List<Stopwatch> s) {
        try (MatlabRunner matlab = new MatlabRunner(localPath())) {
            matlab.addPath(dataPath());
            matlab.putVar("tot", LAST_TIME);
            matlab.eval("AFC_Online_FromFile");

            List<Sample<Double, AbstractInterval<Double>>>
                    breach = executeBreach(matlab, s);

            List<Double> bStart = IntStream.range(0, breach.size())
                    .boxed()
                    .map(i -> breach.get(i)
                            .getValue()
                            .getStart())
                    .collect(Collectors.toList());

            List<Double> bEnd = IntStream.range(0, breach.size())
                    .boxed()
                    .map(i -> breach.get(i)
                            .getValue()
                            .getEnd())
                    .collect(Collectors.toList());

            if (PLOTTING)
                plt.plot(bStart, bEnd, "Breach");


        } catch (IOException e) {
            e.printStackTrace();
            throw new UnknownError("Unable to run Breach");
        }
    }

    private static List<Sample<Double, AbstractInterval<Double>>>
    executeBreach(MatlabRunner matlab, List<Stopwatch> stopwatches)
    {

        matlab.putVar("tot2", LAST_TIME);
        Stopwatch rec = Stopwatch.start();
        matlab.eval("AFC_Online_FromFile2");
        long duration = rec.stop();
        stopwatches.add(rec);

        LOG.info("Breach Execution Time (sec): " + duration / 1000.);

        double[] rhoLow = matlab.getVar("rho_low");
        double[] rhoUp = matlab.getVar("rho_up");
        //input = matlab.getVar("input");

        assert rhoLow != null;
        assert rhoUp != null;
        return IntStream.range(0, rhoLow.length).boxed()
                        .map(i -> (Sample<Double,
                                                        AbstractInterval<Double>>)
                                new TimeSegment<>((double) i,
                                        new AbstractInterval<>(rhoLow[i],
                                                rhoUp[i])))
                        .collect(Collectors.toList());

        //return condenseSignal(output);
    }
}
