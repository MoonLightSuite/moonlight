package io.github.moonlightsuite.moonlight.examples.temporal.afc;

import io.github.moonlightsuite.moonlight.core.formula.Formula;
import io.github.moonlightsuite.moonlight.core.base.Box;
import io.github.moonlightsuite.moonlight.domain.DoubleDomain;
import io.github.moonlightsuite.moonlight.core.formula.Interval;
import io.github.moonlightsuite.moonlight.formula.*;
import io.github.moonlightsuite.moonlight.formula.classic.NegationFormula;
import io.github.moonlightsuite.moonlight.formula.classic.OrFormula;
import io.github.moonlightsuite.moonlight.formula.temporal.EventuallyFormula;
import io.github.moonlightsuite.moonlight.formula.temporal.GloballyFormula;
import io.github.moonlightsuite.moonlight.online.monitoring.OnlineTimeMonitor;
import io.github.moonlightsuite.moonlight.core.signal.Sample;
import io.github.moonlightsuite.moonlight.online.signal.Update;
import io.github.moonlightsuite.moonlight.util.Stopwatch;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;

import static io.github.moonlightsuite.moonlight.examples.temporal.afc.AFCSettings.AFC_ATOM;
import static io.github.moonlightsuite.moonlight.examples.temporal.afc.AFCSettings.ITERATIONS;

public class AFCHelpers {
   private AFCHelpers() {}  // Hidden constructor

    static List<List<Double>> handleData(List<List<Sample<Double,
            Box<Double>>>> data)
    {
        List<List<Double>> r = new ArrayList<>();
        r.add(new ArrayList<>());
        r.add(new ArrayList<>());

        for (List<Sample<Double, Box<Double>>> v : data)
        {
            r.get(0).add(v.get(0).getValue().getStart());
            r.get(1).add(v.get(0).getValue().getEnd());
        }

        return r;
    }

    static void repeatedRunner(String title, Consumer<List<Stopwatch>> task,
                               List<Stopwatch> stopwatches, List<String> output)
    {
        Optional<Long> total =
                IntStream.range(0, ITERATIONS)
                        .boxed()
                        .map(i -> {
                            task.accept(stopwatches);
                            return stopwatches.get(i).getDuration();
                        }).reduce(Long::sum);

        double tot = total.orElse(0L);

        tot = (tot / ITERATIONS) / 1000.; // Converted to seconds

        output.add(title + " Execution time (avg over " + ITERATIONS + "):" +
                   tot);

        stopwatches.clear();
    }

    static List<Sample<Double, Box<Double>>>
    condenseSignal(List<Sample<Double, Box<Double>>> ss)
    {
        List<Sample<Double, Box<Double>>> out =
                new ArrayList<>();
        Sample<Double, Box<Double>> bound = ss.get(0);
        out.add(ss.get(0));

        for (Sample<Double, Box<Double>> curr : ss) {
            if (!curr.getValue().equals(bound.getValue())) {
                bound = curr;
                out.add(curr);
            }
        }

        return out;
    }

    static List<Update<Double, Double>> genUpdates(double[] values,
                                                   boolean shuffle,
                                                   double scale,
                                                   int seed)
    {
        List<Update<Double, Double>> updates = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            double ui = i * scale;
            double uj = (i + 1) * scale;
            double uv = values[i];
            updates.add(new Update<>(ui, uj, uv));
        }

        if(shuffle)
            Collections.shuffle(updates, new Random(seed));

        return updates;
    }

    static Formula afcFormula() {
        // alw_[10, 30] ((abs(AF[t]-AFRef[t]) > 0.05) =>
        //               (ev_[0, 1] (abs(AF[t]-AFRef[t]) < 0.05)))
       return new GloballyFormula(
               new OrFormula(
                       new NegationFormula(new AtomicFormula(AFC_ATOM)),
                       new EventuallyFormula(
                               new NegationFormula(
                                       new AtomicFormula(AFC_ATOM))
                               , new Interval(0.0, 1.0))
               ),
               new Interval(10.0, 30.0));
    }

    static OnlineTimeMonitor<Double, Double> instrument() {
        Formula f = afcFormula();

        HashMap<String, Function<Double, Box<Double>>>
                atoms = new HashMap<>();
        atoms.put(AFC_ATOM, trc -> new Box<>(trc - 0.05,
                                                          trc - 0.05));

        return new OnlineTimeMonitor<>(f, new DoubleDomain(), atoms);
    }

}