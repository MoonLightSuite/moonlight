package eu.quanticol.moonlight.formula;

import eu.quanticol.moonlight.domain.AbstractInterval;
import eu.quanticol.moonlight.domain.DoubleDomain;
import eu.quanticol.moonlight.domain.Interval;
import eu.quanticol.moonlight.monitoring.online.OnlineTimeMonitor;
import eu.quanticol.moonlight.signal.online.TimeChain;
import eu.quanticol.moonlight.signal.online.TimeSignal;
import eu.quanticol.moonlight.signal.online.Update;
import eu.quanticol.moonlight.util.Plotter;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OutOfOrderTest {
    private static final String POSITIVE_X = "x > 0";
    private final Plotter plt = new Plotter();

    private static final boolean PLOTTING = false;
    private static final int RND_SEED = 1;  // TODO: vary seeds

    TimeChain<Double, AbstractInterval<Double>> io; //TODO: for debugging

    @Test
    void testSimple1() {
        // Atom
        HashMap<String, Function<Double, AbstractInterval<Double>>> atoms =
                                                                new HashMap<>();
        atoms.put(POSITIVE_X, x -> new AbstractInterval<>(x, x));

        // Monitor
        OnlineTimeMonitor<Double, Double> m;

        // Updates
        List<Update<Double, Double>> updates = simpleUpdates();

        // Formula
        Formula f = new AtomicFormula(POSITIVE_X);

        // Monitoring in-order updates
        m = new OnlineTimeMonitor<>(f, new DoubleDomain(), atoms);
        TimeChain<Double, AbstractInterval<Double>> r1 = monitor(m, updates);
        System.out.println("IO: " + r1);
        io = r1;

        // Monitoring shuffled updates
        Collections.shuffle(updates, new Random(RND_SEED));

        m = new OnlineTimeMonitor<>(f, new DoubleDomain(), atoms);
        TimeChain<Double, AbstractInterval<Double>> r2 = monitor(m, updates);
        System.out.println("OO: " + r2);

        assert r1 != null;
        assert r2 != null;

        if(PLOTTING) {
            plt.plot(r1, "In order");
            plt.plot(r2, "Out of order");
            plt.waitActivePlots();
        }

        assertEquals(r1, r2);
    }

    @Test
    void testSimple2() {
        // Atom
        HashMap<String, Function<Double, AbstractInterval<Double>>> atoms =
                                                                new HashMap<>();
        atoms.put(POSITIVE_X, x -> new AbstractInterval<>(x, x));

        // Monitor
        OnlineTimeMonitor<Double, Double> m;

        // Updates
        List<Update<Double, Double>> updates = simpleUpdates();

        // Formula
        Formula f = new GloballyFormula(new AtomicFormula(POSITIVE_X),
                                        new Interval(0, 8));

        // Monitoring in-order updates
        m = new OnlineTimeMonitor<>(f, new DoubleDomain(), atoms);
        TimeChain<Double, AbstractInterval<Double>> r1 = monitor(m, updates);
        System.out.println(r1);

        // Monitoring shuffled updates
        Collections.shuffle(updates, new Random(RND_SEED));

        m = new OnlineTimeMonitor<>(f, new DoubleDomain(), atoms);
        TimeChain<Double, AbstractInterval<Double>> r2 = monitor(m, updates);
        System.out.println(r1);

        assert r1 != null;
        assert r2 != null;

        if(PLOTTING) {
            plt.plot(r1, "In order");
            plt.plot(r2, "Out of order");
            plt.waitActivePlots();
        }

        assertEquals(r1, r2);
    }

    @Test
    void testSimple3() {
        // Atom
        HashMap<String, Function<Double, AbstractInterval<Double>>> atoms =
                                                                new HashMap<>();
        atoms.put(POSITIVE_X, x -> new AbstractInterval<>(x, x));

        // Monitor
        OnlineTimeMonitor<Double, Double> m;

        // Updates
        List<Update<Double, Double>> updates = simpleUpdates();

        // Formula
        Formula f = formulaAFC();

        // Monitoring in-order updates
        m = new OnlineTimeMonitor<>(f, new DoubleDomain(), atoms);
        TimeChain<Double, AbstractInterval<Double>> r1 = monitor(m, updates);
        System.out.println(r1);

        // Monitoring shuffled updates
        Collections.shuffle(updates, new Random(RND_SEED));

        m = new OnlineTimeMonitor<>(f, new DoubleDomain(), atoms);
        TimeChain<Double, AbstractInterval<Double>> r2 = monitor(m, updates);
        System.out.println(r1);

        assert r1 != null;
        assert r2 != null;

        if(PLOTTING) {
            plt.plot(r1, "In order");
            plt.plot(r2, "Out of order");
            plt.waitActivePlots();
        }

        assertEquals(r1, r2);
    }

    @Test
    void testComplex1() {
        // Atom
        HashMap<String, Function<Double, AbstractInterval<Double>>> atoms =
                                                                new HashMap<>();
        atoms.put(POSITIVE_X, x -> new AbstractInterval<>(x, x));

        // Monitor
        OnlineTimeMonitor<Double, Double> m;

        // Updates
        List<Update<Double, Double>> updates = AFCTest.loadInput();

        // Formula
        Formula f = new AtomicFormula(POSITIVE_X);

        // Monitoring in-order updates
        m = new OnlineTimeMonitor<>(f, new DoubleDomain(), atoms);
        TimeChain<Double, AbstractInterval<Double>> r1 = monitor(m, updates);
        System.out.println(r1);

        // Monitoring shuffled updates
        Collections.shuffle(updates, new Random(RND_SEED));

        m = new OnlineTimeMonitor<>(f, new DoubleDomain(), atoms);
        TimeChain<Double, AbstractInterval<Double>> r2 = monitor(m, updates);
        System.out.println(r2);

        assert r1 != null;
        assert r2 != null;

        if(PLOTTING) {
            plt.plot(r1, "In order");
            plt.plot(r2, "Out of order");
            plt.waitActivePlots();
        }

        assertEquals(r1, r2);
    }

    @Disabled("In review")
    @Test
    void testComplex2() {
        // Atom
        HashMap<String, Function<Double, AbstractInterval<Double>>> atoms =
                new HashMap<>();
        atoms.put(POSITIVE_X, x -> new AbstractInterval<>(x, x));

        // Monitor
        OnlineTimeMonitor<Double, Double> m;

        // Updates
        List<Update<Double, Double>> updates = AFCTest.loadInput().subList(0, 80);

        // Formula
        Formula f = new GloballyFormula(new AtomicFormula(POSITIVE_X),
                                        new Interval(0, 1));

        // Monitoring in-order updates
        m = new OnlineTimeMonitor<>(f, new DoubleDomain(), atoms);
        TimeChain<Double, AbstractInterval<Double>> r1 = monitor(m, updates);
        System.out.println(r1);

        // Monitoring shuffled updates
        Collections.shuffle(updates, new Random(RND_SEED));

        m = new OnlineTimeMonitor<>(f, new DoubleDomain(), atoms);
        TimeChain<Double, AbstractInterval<Double>> r2 = monitor(m, updates);
        System.out.println(r2);

        assert r1 != null;
        assert r2 != null;

        if(PLOTTING) {
            plt.plot(r1, "In order");
            plt.plot(r2, "Out of order");
            plt.waitActivePlots();
        }

        assertEquals(r1, r2);
    }

    @Test
    void testComplex3() {
        HashMap<String, Function<Double, AbstractInterval<Double>>> atoms = new HashMap<>();
        atoms.put(POSITIVE_X, x -> new AbstractInterval<>(x, x));

        Formula f = new GloballyFormula(new AtomicFormula(POSITIVE_X), new Interval(0, 8));
        OnlineTimeMonitor<Double, Double> m = new OnlineTimeMonitor<>(f, new DoubleDomain(), atoms);

        List<Update<Double, Double>> updates = AFCTest.loadInput().subList(0, 100);

        TimeSignal<Double, AbstractInterval<Double>> r1 = null;
        int i = 1;
        for(Update<Double, Double> u: updates) {
            r1 =  m.monitor(u);
            //TimeChain<Double, AbstractInterval<Double>> res = r1.getSegments().replicate();
            //plot(res, "io-" + i);
            //System.out.println(r1.getSegments());
            i++;
        }

        System.out.println("Shuffled updates");

        Collections.shuffle(updates, new Random(2));
        TimeSignal<Double, AbstractInterval<Double>> r2 = null;
        m = new OnlineTimeMonitor<>(f, new DoubleDomain(), atoms);
        int j = 1;
        for(Update<Double, Double> u: updates) {
            r2 =  m.monitor(u);
            //TimeChain<Double, AbstractInterval<Double>> res = r2.getSegments().replicate();
            //plot(res, "ooo-" + j);
            //System.out.println(r2.getSegments());
            j++;
        }

        assert r1 != null;
        assert r2 != null;

        if(PLOTTING) {
            Plotter plt = new Plotter();
            plt.plot(r1.getSegments(), "In order");
            plt.plot(r2.getSegments(), "Out of order");
            plt.waitActivePlots();
        }

        assertEquals(r1.getSegments(), r2.getSegments());
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
        updates.add(new Update<>(7.0, 8.0, 2.0));

        return updates;
    }

    private TimeChain<Double, AbstractInterval<Double>> monitor(
            OnlineTimeMonitor<Double, Double> m ,
            List<Update<Double, Double>> ups
            //,
    )
    {
        TimeChain<Double, AbstractInterval<Double>> res = null;
        int i = 1;
        for(Update<Double, Double> u: ups) {
            res = m.monitor(u).getSegments().copy();
            i++;
//            if(PLOTTING)
//                plt.plot(res, prefix +  At Update " + i);
        }

        return res;
    }
}
