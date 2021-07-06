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

import eu.quanticol.moonlight.domain.AbstractInterval;
import eu.quanticol.moonlight.io.DataReader;
import eu.quanticol.moonlight.io.FileType;
import eu.quanticol.moonlight.io.parsing.ParsingStrategy;
import eu.quanticol.moonlight.io.parsing.RawTrajectoryExtractor;
import eu.quanticol.moonlight.monitoring.online.OnlineTimeMonitor;
import eu.quanticol.moonlight.signal.online.*;
import eu.quanticol.moonlight.util.Plotter;
import eu.quanticol.moonlight.util.Stopwatch;

import java.util.*;

import static eu.quanticol.moonlight.examples.temporal.afc.AFCHelpers.*;
import static eu.quanticol.moonlight.examples.temporal.afc.AFCSettings.*;

public class AFCMoonlightRunner {

    private static final List<String> output = new ArrayList<>();
    private static final List<Stopwatch> stopwatches = new ArrayList<>();

    private static final int RND_SEED = 7;

    private static final List<List<SegmentInterface<
            Double, AbstractInterval<Double>>>> results = new ArrayList<>();

    private static final boolean PLOTTING = false;

    private static final Plotter plt = new Plotter();

    public static void main(String[] args) {

        repeatedRunner("In-Order " + LAST_TIME,
                       s -> moonlight(false, LAST_TIME, stopwatches),
                       stopwatches, output);
        repeatedRunner("OO-Order " + LAST_TIME,
                        s -> moonlight(true, LAST_TIME, stopwatches),
                       stopwatches, output);

        LOG.info("------> Experiment results (sec):");
        plt.waitActivePlots(10);

        output.forEach(LOG::info);

        if(results.get(0).equals(results.get(1)))
            LOG.info("Results match");
        else
            LOG.severe("Results don't match");
    }

    private static String intToString(Number v) {
        return String.valueOf(v.intValue());
    }

    static void moonlight(boolean shuffle, Number lastT, List<Stopwatch> s)
    {
        String id = intToString(lastT);
        List<List<SegmentInterface<Double, AbstractInterval<Double>>>>
                moonlightColl = execMoonlight(shuffle, id, s);
        List<SegmentInterface<Double, AbstractInterval<Double>>>
                moonlight = moonlightColl.get(moonlightColl.size() - 1);

        results.add(moonlight);

        List<List<Double>> mRes = handleData(moonlightColl);

        List<Double> mStart = mRes.get(0);
        List<Double> mEnd = mRes.get(1);

        if (PLOTTING)
            plt.plot(mStart, mEnd, "Moonlight");
    }

    static void moonlightChain(boolean shuffle, Number lastT,
                               List<Stopwatch> s)
    {
        String id = intToString(lastT);
        List<List<SegmentInterface<Double, AbstractInterval<Double>>>>
                moonlightColl = execChainMoonlight(shuffle, id, s);
        List<SegmentInterface<Double, AbstractInterval<Double>>>
                moonlight = moonlightColl.get(moonlightColl.size() - 1);

        results.add(moonlight);

        List<List<Double>> mRes = handleData(moonlightColl);

        List<Double> mStart = mRes.get(0);
        List<Double> mEnd = mRes.get(1);

        if (PLOTTING)
            plt.plot(mStart, mEnd, "Moonlight");
    }

    static List<List<SegmentInterface<Double, AbstractInterval<Double>>>>
    execMoonlight(boolean shuffle, String id, List<Stopwatch> stopwatches)
    {
        ParsingStrategy<double[][]> st = new RawTrajectoryExtractor(1);
        FileType type = FileType.CSV;
        double[] input = new DataReader<>(dataPath(id), type, st).read()[0];

        List<Update<Double, Double>> updates = genUpdates(input, shuffle,
                                                          SCALE, RND_SEED);
        OnlineTimeMonitor<Double, Double> m = instrument();

        List<List<SegmentInterface<Double, AbstractInterval<Double>>>>
                result = new ArrayList<>();

        // Moonlight execution recording...
        Stopwatch rec = Stopwatch.start();
        for (Update<Double, Double> u : updates) {
            result.add(m.monitor(u).getSegments().copy().toList());
        }
        rec.stop();
        stopwatches.add(rec);

        return result;
    }

    static List<List<SegmentInterface<Double, AbstractInterval<Double>>>>
    execChainMoonlight(boolean shuffle, String id, List<Stopwatch> stopwatches)
    {
        ParsingStrategy<double[][]> st = new RawTrajectoryExtractor(1);
        FileType type = FileType.CSV;
        double[] input = new DataReader<>(dataPath(id), type, st).read()[0];

        List<Update<Double, Double>> updates = genUpdates(input, shuffle,
                                                          SCALE, RND_SEED);
        OnlineTimeMonitor<Double, Double> m = instrument();

        List<List<SegmentInterface<Double, AbstractInterval<Double>>>>
                result = new ArrayList<>();

        TimeChain<Double, Double> chain = Update.asTimeChain(updates);

        // Moonlight execution recording...
        Stopwatch rec = Stopwatch.start();
        TimeChain<Double, AbstractInterval<Double>> r =
                                            m.monitor(chain).getSegments();
        rec.stop();
        result.add(r.toList());
        stopwatches.add(rec);

        return result;
    }


}
