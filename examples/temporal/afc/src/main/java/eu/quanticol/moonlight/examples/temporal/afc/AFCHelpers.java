package eu.quanticol.moonlight.examples.temporal.afc;

import eu.quanticol.moonlight.domain.AbstractInterval;
import eu.quanticol.moonlight.domain.DoubleDomain;
import eu.quanticol.moonlight.domain.Interval;
import eu.quanticol.moonlight.formula.*;
import eu.quanticol.moonlight.monitoring.online.OnlineTimeMonitor;
import eu.quanticol.moonlight.signal.online.SegmentInterface;
import eu.quanticol.moonlight.signal.online.Update;
import eu.quanticol.moonlight.util.Stopwatch;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;

import static eu.quanticol.moonlight.examples.temporal.afc.AFCSettings.AFC_ATOM;
import static eu.quanticol.moonlight.examples.temporal.afc.AFCSettings.ITERATIONS;

public class AFCHelpers {
   private AFCHelpers() {}  // Hidden constructor

    static List<List<Double>> handleData(List<List<SegmentInterface<Double,
                                         AbstractInterval<Double>>>> data)
    {
        List<List<Double>> r = new ArrayList<>();
        r.add(new ArrayList<>());
        r.add(new ArrayList<>());

        for (List<SegmentInterface<Double, AbstractInterval<Double>>> v : data)
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

    static List<SegmentInterface<Double, AbstractInterval<Double>>>
    condenseSignal(List<SegmentInterface<Double, AbstractInterval<Double>>> ss)
    {
        List<SegmentInterface<Double, AbstractInterval<Double>>> out =
                new ArrayList<>();
        SegmentInterface<Double, AbstractInterval<Double>> bound = ss.get(0);
        out.add(ss.get(0));

        for (SegmentInterface<Double, AbstractInterval<Double>> curr : ss) {
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

        HashMap<String, Function<Double, AbstractInterval<Double>>>
                atoms = new HashMap<>();
        atoms.put(AFC_ATOM, trc -> new AbstractInterval<>(trc - 0.05,
                                                          trc - 0.05));

        return new OnlineTimeMonitor<>(f, new DoubleDomain(), atoms);
    }

}
