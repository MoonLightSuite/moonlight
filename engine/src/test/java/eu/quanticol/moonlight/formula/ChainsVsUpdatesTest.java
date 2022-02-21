package eu.quanticol.moonlight.formula;

import eu.quanticol.moonlight.TestUtils;
import eu.quanticol.moonlight.domain.AbstractInterval;
import eu.quanticol.moonlight.domain.DoubleDomain;
import eu.quanticol.moonlight.domain.Interval;
import eu.quanticol.moonlight.online.monitoring.OnlineTimeMonitor;
import eu.quanticol.moonlight.online.signal.TimeChain;
import eu.quanticol.moonlight.online.signal.TimeSignal;
import eu.quanticol.moonlight.online.signal.Update;
import eu.quanticol.moonlight.util.Plotter;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChainsVsUpdatesTest {
    private static final String POSITIVE_X = "x > 0";
    private static final Plotter plt = new Plotter(10.0);

    private static final boolean PLOTTING = false;
    private static final int RND_SEED = 1;  // TODO: vary seeds

    @Test
    void testSimple1() {
        List<Update<Double, Double>> updates = simpleShuffledUpdates();
        Formula formula = new AtomicFormula(POSITIVE_X);

        TimeChain<Double, AbstractInterval<Double>> r1 = monitor(formula, updates);
        TimeChain<Double, AbstractInterval<Double>> r2 = monitorChains(formula, updates);

        plotWhenEnabled(r1, r2);
        assertEquals(r1, r2);
    }

    @Test
    void testSimple2() {
        List<Update<Double, Double>> updates = simpleShuffledUpdates();
        updates = shuffle(updates);
        Formula formula = new GloballyFormula(new AtomicFormula(POSITIVE_X),
                new Interval(0, 8));

        // Monitoring updates
        TimeChain<Double, AbstractInterval<Double>> r1 =
                monitor(formula, updates);

        // Monitoring updates as chains
        TimeChain<Double, AbstractInterval<Double>> r2 =
                monitorChains(formula, updates);

        plotWhenEnabled(r1, r2);
        assertEquals(r1, r2);
    }

    @Test
    void testSimple3() {
        List<Update<Double, Double>> updates = simpleShuffledUpdates();
        updates = shuffle(updates);
        Formula formula = formulaAFC();

        // Monitoring updates
        TimeChain<Double, AbstractInterval<Double>> r1 =
                monitor(formula, updates);

        // Monitoring updates as chains
        TimeChain<Double, AbstractInterval<Double>> r2 =
                monitorChains(formula, updates);

        plotWhenEnabled(r1, r2);
        assertEquals(r1, r2);
    }

    @Test
    void testComplex1() {
        List<Update<Double, Double>> updates = AFCTest.loadInput();
        updates = shuffle(updates);
        Formula formula = new AtomicFormula(POSITIVE_X);

        // Monitoring updates
        TimeChain<Double, AbstractInterval<Double>> r1 =
                monitor(formula, updates);

        // Monitoring updates as chains
        TimeChain<Double, AbstractInterval<Double>> r2 =
                monitorChains(formula, updates);

        plotWhenEnabled(r1, r2);
        assertEquals(r1, r2);
    }

    @Test
    void testComplex2() {
        List<Update<Double, Double>> updates = AFCTest.loadInput().subList(0, 80);
        updates = shuffle(updates);
        Formula formula = new GloballyFormula(new AtomicFormula(POSITIVE_X),
                                              new Interval(0, 1));

        // Monitoring updates
        TimeChain<Double, AbstractInterval<Double>> r1 =
                monitor(formula, updates);

        // Monitoring updates as chains
        TimeChain<Double, AbstractInterval<Double>> r2 =
                monitorChains(formula, updates);

        plotWhenEnabled(r1, r2);
        assertEquals(r1, r2);
    }

    @Test
    void testComplex3() {
        List<Update<Double, Double>> updates = AFCTest.loadInput().subList(0, 80);
        updates = shuffle(updates);
        Formula formula = new GloballyFormula(new AtomicFormula(POSITIVE_X),
                                              new Interval(0, 8));

        // Monitoring updates
        TimeChain<Double, AbstractInterval<Double>> r1 =
                monitor(formula, updates);

        // Monitoring updates as chains
        TimeChain<Double, AbstractInterval<Double>> r2 =
                monitorChains(formula, updates);

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

    private List<Update<Double, Double>> simpleShuffledUpdates() {
        List<Update<Double, Double>> updates = new ArrayList<>();
        updates.add(new Update<>(0.0, 1.0, 2.0));
        updates.add(new Update<>(1.0, 2.0, 3.0));
        updates.add(new Update<>(2.0, 3.0, 4.0));
        updates.add(new Update<>(3.0, 4.0, -1.0));
        updates.add(new Update<>(4.0, 5.0, 2.0));
        updates.add(new Update<>(5.0, 6.0, 2.0));
        updates.add(new Update<>(6.0, 7.0, 2.0));
        updates.add(new Update<>(7.0, 8.0, 2.0));

        return shuffle(updates);
    }

    private static Map<String, Function<Double, AbstractInterval<Double>>> atoms()
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
            plt.plot(inOrder, "As Updates");
            plt.plot(outOfOrder, "As Chains");
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

    private TimeChain<Double, AbstractInterval<Double>> monitorUpdate(
            Formula formula,
            Update<Double, Double> u)
    {
        OnlineTimeMonitor<Double, Double> m = init(formula);
        return m.monitor(u).getSegments();
    }

    private TimeChain<Double, AbstractInterval<Double>> monitorChain(
            Formula formula,
            TimeChain<Double, Double> u)
    {
        OnlineTimeMonitor<Double, Double> m = init(formula);
        return m.monitor(u).getSegments();
    }

    private TimeChain<Double, AbstractInterval<Double>> monitorChains(
            Formula formula,
            List<Update<Double, Double>> ups)
    {
        OnlineTimeMonitor<Double, Double> m = init(formula);
        List<TimeChain<Double, Double>> chains = TestUtils.toChains(ups);
        TimeSignal<Double, AbstractInterval<Double>> result = null;
        for(TimeChain<Double, Double> c: chains) {
            result = m.monitor(c);
        }
        assert result != null;
        return result.getSegments();
    }

    private static OnlineTimeMonitor<Double, Double> init(Formula formula)
    {
        Map<String, Function<Double, AbstractInterval<Double>>> atoms = atoms();
        return new OnlineTimeMonitor<>(formula, new DoubleDomain(), atoms);
    }


}
