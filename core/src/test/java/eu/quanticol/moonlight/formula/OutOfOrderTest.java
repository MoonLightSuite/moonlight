package eu.quanticol.moonlight.formula;

import eu.quanticol.moonlight.domain.AbstractInterval;
import eu.quanticol.moonlight.domain.DoubleDomain;
import eu.quanticol.moonlight.domain.Interval;
import eu.quanticol.moonlight.monitoring.online.OnlineTimeMonitor;
import eu.quanticol.moonlight.signal.online.*;
import eu.quanticol.moonlight.util.Plotter;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OutOfOrderTest {
    private static final String POSITIVE_X = "x > 0";
    private static final Plotter plt = new Plotter();

    private static final boolean PLOTTING = false;
    private static final int RND_SEED = 1;  // TODO: vary seeds

    @Test
    void testSimple1() {
        List<Update<Double, Double>> updates = simpleUpdates();
        Formula formula = new AtomicFormula(POSITIVE_X);

        // Monitoring in-order updates
        TimeChain<Double, AbstractInterval<Double>> r1 =
                monitorChain(formula, updates);

        // Monitoring shuffled updates
        updates = shuffle(updates);
        TimeChain<Double, AbstractInterval<Double>> r2 =
                monitor(formula, updates);

        plotWhenEnabled(r1, r2);
        assertEquals(r1, r2);
    }

    @Test
    void testSimple2() {
        List<Update<Double, Double>> updates = simpleUpdates();
        Formula formula = new GloballyFormula(new AtomicFormula(POSITIVE_X),
                                              new Interval(0, 8));

        // Monitoring in-order updates
        TimeChain<Double, AbstractInterval<Double>> r1 =
                monitorChain(formula, updates);

        // Monitoring shuffled updates
        updates = shuffle(updates);
        TimeChain<Double, AbstractInterval<Double>> r2 =
                monitor(formula, updates);

        plotWhenEnabled(r1, r2);
        assertEquals(r1, r2);
    }

    @Test
    void testSimple3a() {
        List<Update<Double, Double>> updates = simpleUpdates();
        Formula formula = new EventuallyFormula(
                            new NegationFormula(new AtomicFormula(POSITIVE_X))
                            ,  new Interval(0.0, 1.0));

        // Monitoring in-order updates
        TimeChain<Double, AbstractInterval<Double>> r =
                monitorChain(formula, updates);

        assert r != null;
        List<SegmentInterface<Double, AbstractInterval<Double>>>
                segments = r.toList();
        assertEquals(new TimeSegment<>(0.0, new AbstractInterval<>(-2.0, -2.0)),
                     segments.get(0));
        assertEquals(new TimeSegment<>(1.0, new AbstractInterval<>(-3.0, -3.0)),
                     segments.get(1));
    }

    @Test
    void testSimple3() {
        List<Update<Double, Double>> updates = simpleUpdates();
        Formula formula = formulaAFC();

        // Monitoring in-order updates
        TimeChain<Double, AbstractInterval<Double>> r1 =
                monitorChain(formula, updates);

        // Monitoring shuffled updates
        updates = shuffle(updates);

        TimeChain<Double, AbstractInterval<Double>> r2 =
                monitor(formula, updates);

        plotWhenEnabled(r1, r2);
        assertEquals(r1, r2);
    }

    @Test
    void testComplex1() {
        List<Update<Double, Double>> updates = AFCTest.loadInput();
        Formula formula = new AtomicFormula(POSITIVE_X);

        // Monitoring in-order updates
        TimeChain<Double, AbstractInterval<Double>> r1 =
                monitorChain(formula, updates);

        // Monitoring shuffled updates
        updates = shuffle(updates);

        TimeChain<Double, AbstractInterval<Double>> r2 =
                monitor(formula, updates);

        plotWhenEnabled(r1, r2);
        assertEquals(r1, r2);
    }

    @Test
    void testComplex2() {
        List<Update<Double, Double>> updates = AFCTest.loadInput().subList(0, 80);
        Formula formula = new GloballyFormula(new AtomicFormula(POSITIVE_X),
                                              new Interval(0, 1));

        // Monitoring in-order updates
        TimeChain<Double, AbstractInterval<Double>> r1 =
                monitorChain(formula, updates);

        // Monitoring shuffled updates
        updates = shuffle(updates);
        TimeChain<Double, AbstractInterval<Double>> r2 =
                monitor(formula, updates);

        plotWhenEnabled(r1, r2);
        assertEquals(r1, r2);
    }

    @Test
    void testComplex3() {
        List<Update<Double, Double>> updates = AFCTest.loadInput().subList(0, 100);
        Formula formula = new GloballyFormula(new AtomicFormula(POSITIVE_X),
                                              new Interval(0, 8));

        // Monitoring in-order updates
        TimeChain<Double, AbstractInterval<Double>> r1 =
                monitorChain(formula, updates);

        // Monitoring shuffled updates
        updates = shuffle(updates);
        TimeChain<Double, AbstractInterval<Double>> r2 =
                monitor(formula, updates);

        plotWhenEnabled(r1, r2);
        assertEquals(r1, r2);
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

    private List<Update<Double, Double>> simpleUpdates() {
        List<Update<Double, Double>> updates = new ArrayList<>();
        updates.add(new Update<>(0.0, 1.0, 2.0));
        updates.add(new Update<>(1.0, 2.0, 3.0));
        updates.add(new Update<>(2.0, 3.0, 4.0));
        updates.add(new Update<>(3.0, 4.0, -1.0));
        updates.add(new Update<>(4.0, 5.0, 2.0));
        updates.add(new Update<>(5.0, 6.0, 2.0));
        updates.add(new Update<>(6.0, 7.0, 2.0));
        updates.add(new Update<>(7.0, 8.0, 2.0));

        return updates;
    }

    private static
    Map<String, Function<Double, AbstractInterval<Double>>> atoms()
    {
        Map<String, Function<Double, AbstractInterval<Double>>> atoms =
                new HashMap<>();
        atoms.put(POSITIVE_X, x -> new AbstractInterval<>(x, x));
        return atoms;
    }

    private static List<Update<Double, Double>> shuffle(
            List<Update<Double, Double>> data)
    {
        List<Update<Double, Double>> result = new ArrayList<>(data);
        Collections.shuffle(result, new Random(RND_SEED));
        return result;
    }

    private static void plotWhenEnabled(
            @NotNull TimeChain<Double, AbstractInterval<Double>> inOrder,
            @NotNull TimeChain<Double, AbstractInterval<Double>> outOfOrder)
    {
        if(PLOTTING) {
            plt.plot(inOrder, "In order");
            plt.plot(outOfOrder, "Out of order");
            plt.waitActivePlots(0);
        }
    }

    private TimeChain<Double, AbstractInterval<Double>> monitor(
            Formula formula,
            List<Update<Double, Double>> ups)
    {
        OnlineTimeMonitor<Double, Double> m = init(formula);
        TimeChain<Double, AbstractInterval<Double>> res = null;
        for (Update<Double, Double> u : ups) {
            res = m.monitor(u).getSegments().copy();
        }
        return res;
    }

    private TimeChain<Double, AbstractInterval<Double>> monitorChain(
            Formula formula,
            List<Update<Double, Double>> ups)
    {
        OnlineTimeMonitor<Double, Double> m = init(formula);
        TimeChain<Double, Double> chain = Update.asTimeChain(ups);
        return m.monitor(chain).getSegments();
    }

    private static OnlineTimeMonitor<Double, Double> init(Formula formula)
    {
        Map<String, Function<Double, AbstractInterval<Double>>> atoms = atoms();
        return new OnlineTimeMonitor<>(formula, new DoubleDomain(), atoms);
    }


}
