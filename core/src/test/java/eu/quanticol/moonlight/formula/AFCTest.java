package eu.quanticol.moonlight.formula;

import eu.quanticol.moonlight.domain.AbstractInterval;
import eu.quanticol.moonlight.domain.DoubleDomain;
import eu.quanticol.moonlight.domain.Interval;
import eu.quanticol.moonlight.io.DataReader;
import eu.quanticol.moonlight.io.FileType;
import eu.quanticol.moonlight.io.parsing.RawTrajectoryExtractor;
import eu.quanticol.moonlight.monitoring.online.OnlineTimeMonitor;
import eu.quanticol.moonlight.signal.online.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AFCTest {
    private final static String DIR = "afcBreach/";
    private final static String RHO_LOW = DIR + "rho_low.csv";
    private final static String RHO_UP = DIR + "rho_up.csv";
    private final static String INPUT = DIR + "input.csv";

    @Disabled("Under Investigation")
    @Test
    void afcTest() {
        List<Update<Double, Double>> input = loadInput();

        OnlineTimeMonitor<Double, Double> m = instrument();
        ArrayList<TimeSegment<Double, AbstractInterval<Double>>> rho = getRho();

        TimeSignal<Double, AbstractInterval<Double>> result = null;
        for(Update<Double, Double> u: input) {
            result = m.monitor(u);
        }

        ArrayList<SegmentInterface<Double, AbstractInterval<Double>>> r =
                new ArrayList<>(Objects.requireNonNull(result).getSegments());
        assertEquals(rho.size(), r.size());
        for(int i = 0; i < rho.size(); i++) {
            assertEquals(rho.get(i), r.get(i));
        }

        System.out.println("End");
    }

    private List<Update<Double, Double>> loadInput() {
        //URL input = getClass().getClassLoader().getResource(INPUT);
        //String source = Objects.requireNonNull(input).getPath();
        //source = "/eu/quanticol/moonlight/formula/" + INPUT;
        RawTrajectoryExtractor ex = new RawTrajectoryExtractor(1);

        InputStream source = AFCTest.class.getClassLoader().getResourceAsStream(INPUT);
        double[] data = new DataReader<>(source, FileType.CSV, ex).read()[0];

        return genUpdates(data);
    }

    private static OnlineTimeMonitor<Double, Double> instrument()
    {
        Formula f = //new GloballyFormula(
                        //new OrFormula(
                        //        new AtomicFormula("smallError"),
                                new EventuallyFormula(
                                    new AtomicFormula("smallError")
                                    ,  new Interval(0.0, 1.0))
                        //),
                //new Interval(10.0, 30.0))
        ;

        // alw_[10, 30] ((abs(AF[t]-AFref[t]) > 0.05) => (ev_[0, 1] (abs(AF[t]-AFref[t]) < 0.05)))

        HashMap<String, Function<Double, AbstractInterval<Double>>>
                atoms = new HashMap<>();

        //positiveX is the atomic proposition: smallError abs(AF[t]-AFref[t])  <= 0.05
        atoms.put("smallError",
                trc -> new AbstractInterval<>(0.05 - trc,
                        0.05 - trc));

        return new OnlineTimeMonitor<>(f, new DoubleDomain(), atoms);
    }

    private ArrayList<TimeSegment<Double, AbstractInterval<Double>>>
    condenseSignal(ArrayList<TimeSegment<Double, AbstractInterval<Double>>> ss)
    {
        ArrayList<TimeSegment<Double, AbstractInterval<Double>>> out =
                                                            new ArrayList<>();
        TimeSegment<Double, AbstractInterval<Double>> bound = ss.get(0);
        out.add(ss.get(0));

        for(TimeSegment<Double, AbstractInterval<Double>> curr: ss) {
            if(!curr.getValue().equals(bound.getValue())) {
                bound = curr;
                out.add(curr);
            }
        }

        return out;
    }



    private List<Update<Double, Double>> genUpdates(double[] values) {
        List<Update<Double, Double>> updates = new ArrayList<>();
        for(int i = 0; i < values.length; i++) {
            updates.add(new Update<>((double)i, (double)i + 1, values[i]));
        }
        return updates;
    }

    private ArrayList<TimeSegment<Double, AbstractInterval<Double>>>
    getRho()
    {
        RawTrajectoryExtractor ex = new RawTrajectoryExtractor(1);

        //URL rhoLow = getClass().getClassLoader().getResource(RHO_LOW);
        InputStream rhoLow = AFCTest.class.getClassLoader().getResourceAsStream(RHO_LOW);
        //URL rhoUp = getClass().getClassLoader().getResource(RHO_UP);
        InputStream rhoUp = AFCTest.class.getClassLoader().getResourceAsStream(RHO_UP);

        //String source = Objects.requireNonNull(rhoLow).getPath();
        double[] data1 = new DataReader<>(rhoLow, FileType.CSV, ex).read()[0];

        //source = Objects.requireNonNull(rhoUp).getPath();
        double[] data2 = new DataReader<>(rhoUp, FileType.CSV, ex).read()[0];

        ArrayList<TimeSegment<Double, AbstractInterval<Double>>> rho = new ArrayList<>();
        for(int i = 0; i < data1.length; i++) {
            rho.add(new TimeSegment<>((double)i, new AbstractInterval<>(data1[i], data2[i])));
        }

        return condenseSignal(rho);
    }
}
