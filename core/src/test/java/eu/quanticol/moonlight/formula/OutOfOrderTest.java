package eu.quanticol.moonlight.formula;

import eu.quanticol.moonlight.domain.AbstractInterval;
import eu.quanticol.moonlight.domain.DoubleDomain;
import eu.quanticol.moonlight.domain.Interval;
import eu.quanticol.moonlight.monitoring.online.OnlineTimeMonitor;
import eu.quanticol.moonlight.signal.online.TimeChain;
import eu.quanticol.moonlight.signal.online.TimeSegment;
import eu.quanticol.moonlight.signal.online.TimeSignal;
import eu.quanticol.moonlight.signal.online.Update;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.github.sh0nk.matplotlib4j.Plot;
import com.github.sh0nk.matplotlib4j.PythonExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OutOfOrderTest {

    private static final String POSITIVE_X = "x > 0";
    private static final List<Thread> threads = new ArrayList<>();

    @Test
    void test1() {
        HashMap<String, Function<Double, AbstractInterval<Double>>> atoms = new HashMap<>();
        atoms.put(POSITIVE_X, x -> new AbstractInterval<>(x, x));

        Formula f = new GloballyFormula(new AtomicFormula(POSITIVE_X), new Interval(0, 8));
        //f = new AtomicFormula(POSITIVE_X);
        //f = formulaAFC();
        OnlineTimeMonitor<Double, Double> m = new OnlineTimeMonitor<>(f, new DoubleDomain(), atoms);

        List<Update<Double, Double>> updates = new ArrayList<>();
        updates.add(new Update<>(0.0, 1.0, 2.0));
        updates.add(new Update<>(1.0, 2.0, 3.0));
        updates.add(new Update<>(2.0, 3.0, 4.0));
        updates.add(new Update<>(3.0, 4.0, -1.0));
        updates.add(new Update<>(4.0, 5.0, 2.0));

        updates = AFCTest.loadInput().subList(0, 213);

        TimeSignal<Double, AbstractInterval<Double>> r1 = null;
        int i = 1;
        for(Update<Double, Double> u: updates) {
            r1 =  m.monitor(u);
            TimeChain<Double, AbstractInterval<Double>> res = r1.getSegments().replicate();
            //plot(res, "io-" + i);
            //System.out.println(r1.getSegments());
            i++;
        }

        System.out.println("Shuffled updates");

        Collections.shuffle(updates, new Random(32));
        TimeSignal<Double, AbstractInterval<Double>> r2 = null;
        m = new OnlineTimeMonitor<>(f, new DoubleDomain(), atoms);
        int j = 1;
        for(Update<Double, Double> u: updates) {
            r2 =  m.monitor(u);
            TimeChain<Double, AbstractInterval<Double>> res = r2.getSegments().replicate();
            //plot(res, "ooo-" + j);
            //System.out.println(r2.getSegments());
            j++;
        }




        assert r1 != null;
        assert r2 != null;

        plot(r1.getSegments(), "In order");
        plot(r2.getSegments(), "Out of order");

        try {
            for (Thread t: threads) {
                t.join();
            }
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        assertSame(r1.getSegments(), r2.getSegments());
    }

    private Formula formulaAFC() {
        return new GloballyFormula(
                new OrFormula(
                        new AtomicFormula(POSITIVE_X),
                        new EventuallyFormula(
                                new NegationFormula(
                                        new AtomicFormula(POSITIVE_X))
                                ,  new Interval(0.0, 1.0))
                )
                , new Interval(10.0, 30.0))
                ;
    }

    private static void plot(TimeChain<Double, AbstractInterval<Double>> data, String name)
    {
        Thread t = new Thread(() -> {
            List<Double> dataDown = filterValues(data.stream().map(x -> x.getValue().getStart()).collect(Collectors.toList()));
            List<Double> dataUp = filterValues(data.stream().map(x -> x.getValue().getEnd()).collect(Collectors.toList()));
            try {
                Plot plt = Plot.create();
                plt.plot().add(dataUp).label("rho_up");
                plt.plot().add(dataDown).label("rho_down");
                plt.xlabel("times");
                plt.ylabel("robustness");
                plt.title(name);
                plt.legend();
                plt.show();
            } catch (PythonExecutionException | IOException e) {
                System.err.println("unable to plot!");
                e.printStackTrace();
            }
        }){{start();}};
        threads.add(t);
//        try {
//            t.join();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    private static List<Double> filterValues(List<Double> vs) {
        vs = vs.stream().map(v -> v.equals(Double.POSITIVE_INFINITY) ?  10 : v)
                .map(v -> v.equals(Double.NEGATIVE_INFINITY) ? -10 : v)
                .collect(Collectors.toList());
        return vs;
    }

    private static <T> void assertSame(List<T> expected, List<T> toTest) {
        assertEquals(expected.size(), toTest.size());
        for(int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), toTest.get(i));
        }
    }
}
