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

import com.mathworks.engine.MatlabEngine;
import eu.quanticol.moonlight.domain.AbstractInterval;
import eu.quanticol.moonlight.signal.online.SegmentInterface;
import eu.quanticol.moonlight.signal.online.TimeSegment;
import eu.quanticol.moonlight.util.Plotter;
import eu.quanticol.moonlight.util.Stopwatch;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static eu.quanticol.moonlight.examples.temporal.afc.AFCHelpers.*;
import static eu.quanticol.moonlight.examples.temporal.afc.AFCMoonlightRunner.moonlight;
import static eu.quanticol.moonlight.examples.temporal.afc.AFCMoonlightRunner.moonlightChain;
import static eu.quanticol.moonlight.examples.temporal.afc.AFCSettings.*;

public class AbstractFuelControl {

    private static double[] input;

    private static final List<String> output = new ArrayList<>();
    private static final List<Stopwatch> stopwatches = new ArrayList<>();

    private static final boolean PLOTTING = false;

    private static final Plotter plt = new Plotter();

    public static void main(String[] args) {
        repeatedRunner("In-Order M", s -> moonlightChain(false, LAST_TIME, s),
                       stopwatches, output);

        repeatedRunner("Out-Of-Order M", s -> moonlight(true, LAST_TIME, s),
                       stopwatches, output);

        repeatedRunner("Breach",
                        s -> runInMatlab(eng -> runBreach(eng, s)),
                       stopwatches, output);

        LOG.info("------> Experiment results (sec):");

        output.forEach(LOG::info);

        AFCSimulatorRunner.main(null);

        if(PLOTTING)
            plt.plot(Arrays.stream(input)
                           .boxed().collect(Collectors.toList()),
                    "alw_[10, 30] ((abs(AF[t]-AFRef[t]) > 0.05) => " +
                            "(ev_[0, 1] (abs(AF[t]-AFRef[t]) < 0.05)))",
                    "|AF-AFRef|");
    }

    private static void runBreach(MatlabEngine eng, List<Stopwatch> s) {
            putVar(eng, "tot", LAST_TIME);
            List<SegmentInterface<Double, AbstractInterval<Double>>>
                    breach = executeBreach(eng, s);

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
    }

    private static List<SegmentInterface<Double, AbstractInterval<Double>>>
    executeBreach(MatlabEngine eng, List<Stopwatch> stopwatches)
    {
        Stopwatch rec = Stopwatch.start();
        eval(eng, "afc_breach_monitoring");
        long duration = rec.stop();
        stopwatches.add(rec);

        LOG.info("Breach Execution Time (sec): " + duration / 1000.);

        double[] rhoLow = getVar(eng, "rho_low");
        double[] rhoUp = getVar(eng, "rho_up");
        input = getVar(eng, "input");

        assert rhoLow != null;
        assert rhoUp != null;
        return IntStream.range(0, rhoLow.length).boxed()
                        .map(i -> (SegmentInterface<Double,
                                AbstractInterval<Double>>)
                                new TimeSegment<>((double) i,
                                        new AbstractInterval<>(rhoLow[i],
                                                rhoUp[i])))
                        .collect(Collectors.toList());

        //return condenseSignal(output);
    }
}
