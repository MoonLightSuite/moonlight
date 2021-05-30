package eu.quanticol.moonlight.formula;

import com.google.common.math.DoubleMath;
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

    @Disabled("Under investigation")
    @Test
    void afcTest() {
        List<Update<Double, Double>> input = loadInput();

        OnlineTimeMonitor<Double, Double> m = instrument();
        ArrayList<SegmentInterface<Double, AbstractInterval<Double>>>
                                                            rho = getRho();

        List<List<SegmentInterface<Double, AbstractInterval<Double>>>>
                result = new ArrayList<>();
        for(Update<Double, Double> u: input) {
            result.add(new ArrayList<>(m.monitor(u).getSegments()));
        }

        ArrayList<SegmentInterface<Double, AbstractInterval<Double>>> r =
                new ArrayList<>(result.get(result.size() - 1));
                //        new ArrayList<>(Objects.requireNonNull(result).getSegments());
        //r = condenseSignal(r);

        assertEquals(rho.size(), r.size());
        for(int i = 0; i < rho.size(); i++) {
            assertEquals(rho.get(i), r.get(i));
        }

        System.out.println("End");
    }

    public static List<Update<Double, Double>> loadInput() {
        RawTrajectoryExtractor ex = new RawTrajectoryExtractor(1);

        InputStream source = path(INPUT);
        double[] data = new DataReader<>(source, FileType.CSV, ex).read()[0];

        return genUpdates(data);
    }

    private static OnlineTimeMonitor<Double, Double> instrument()
    {
        Formula f = new GloballyFormula(
                        new OrFormula(
                                new AtomicFormula("smallError"),
                                new EventuallyFormula(
                                    new NegationFormula(
                                        new AtomicFormula("smallError"))
                                    ,  new Interval(0.0, 1.0))
                        ),
                new Interval(10.0, 30.0))
        ;

        // alw_[10, 30] ((abs(AF[t]-AFref[t]) > 0.05) => (ev_[0, 1] (abs(AF[t]-AFref[t]) < 0.05)))

        HashMap<String, Function<Double, AbstractInterval<Double>>>
                atoms = new HashMap<>();

        //positiveX is the atomic proposition: smallError abs(AF[t]-AFref[t])  <= 0.05
        atoms.put("smallError",
                trc -> new AbstractInterval<>(trc - 0.05,
                        trc - 0.05));

        return new OnlineTimeMonitor<>(f, new DoubleDomain(), atoms);
    }

    private ArrayList<SegmentInterface<Double, AbstractInterval<Double>>>
    condenseSignal(
            ArrayList<SegmentInterface<Double, AbstractInterval<Double>>> ss)
    {
        ArrayList<SegmentInterface<Double, AbstractInterval<Double>>> out =
                                                            new ArrayList<>();
        SegmentInterface<Double, AbstractInterval<Double>> bound = ss.get(0);
        out.add(ss.get(0));

        for(SegmentInterface<Double, AbstractInterval<Double>> curr: ss) {
            if(!fuzzyEquals(curr.getValue(), bound.getValue(), 0.05)) {
                bound = curr;
                out.add(curr);
            }
        }

        return out;
    }

    private boolean fuzzyEquals(AbstractInterval<Double>a,
                                AbstractInterval<Double>b, double tolerance)
    {
        return DoubleMath.fuzzyEquals(a.getStart(), b.getStart(), tolerance)
                &&
               DoubleMath.fuzzyEquals(a.getEnd(), b.getEnd(), tolerance);
    }



    private static List<Update<Double, Double>> genUpdates(double[] values) {
        List<Update<Double, Double>> updates = new ArrayList<>();
        for(int i = 0; i < values.length; i++) {
            //double ti = Math.round((double)i * 0.1 * 100.0) / 100.0;
            //double tj = Math.round(((double)i * 0.1 + 0.1) * 100.0) / 100.0;
            double ti = (double)i * 0.1;
            double tj = (double)(i + 1) * 0.1;
            updates.add(new Update<>(ti, tj, values[i]));
        }
        return updates;
    }

    private ArrayList<SegmentInterface<Double, AbstractInterval<Double>>>
    getRho()
    {
        RawTrajectoryExtractor ex = new RawTrajectoryExtractor(1);

        InputStream rhoLow = path(RHO_LOW);
        InputStream rhoUp = path(RHO_UP);

        double[] data1 = new DataReader<>(rhoLow, FileType.CSV, ex).read()[0];

        double[] data2 = new DataReader<>(rhoUp, FileType.CSV, ex).read()[0];

        ArrayList<SegmentInterface<Double, AbstractInterval<Double>>> rho = new ArrayList<>();
        for(int i = 0; i < data1.length; i++) {
            //if((double)i * 0.1 % 1 == 0)
                rho.add(new TimeSegment<>((double)i* 0.1, new AbstractInterval<>(data1[i], data2[i])));
        }

        return condenseSignal(rho);
        //return rho;
    }

    private static InputStream path(String filename) {
        return Objects.requireNonNull(
                  AFCTest.class.getClassLoader().getResourceAsStream(filename)
               );
    }
}
