package eu.quanticol.moonlight.offline.monitoring.mfr;

import eu.quanticol.moonlight.core.space.DefaultDistanceStructure;
import eu.quanticol.moonlight.core.space.DistanceStructure;
import eu.quanticol.moonlight.core.space.SpatialModel;
import eu.quanticol.moonlight.domain.BooleanDomain;
import eu.quanticol.moonlight.domain.DoubleDomain;
import eu.quanticol.moonlight.formula.AtomicFormula;
import eu.quanticol.moonlight.formula.Parameters;
import eu.quanticol.moonlight.formula.mfr.BinaryFormula;
import eu.quanticol.moonlight.formula.mfr.MapFormula;
import eu.quanticol.moonlight.formula.mfr.ReduceFormula;
import eu.quanticol.moonlight.offline.algorithms.mfr.MfrAlgorithm;
import eu.quanticol.moonlight.offline.signal.SpatialTemporalSignal;
import eu.quanticol.moonlight.space.ImmutableGraphModel;
import eu.quanticol.moonlight.util.Utils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MfrTest {
    private static final int locationsSetLength = 5;

    @Test
    void fetchCloseLocationsFromDistanceBound() {
        var ds = getDistanceStructure(1.0);

        var locations = MfrAlgorithm.getAllWithinDistance(0,
                locationsSetLength,
                ds);

        assertArrayEquals(new int[]{0, 1, 2}, locations);
    }

    private DistanceStructure<Double, Double> getDistanceStructure(
            double upperBound) {
        var domain = new DoubleDomain();
        Function<Double, Double> distFunId = (i) -> 1.0;
        return new DefaultDistanceStructure<>(distFunId, domain,
                0.0, upperBound, basicGraph());
    }

    /**
     * 0 - 1
     * | /
     * 2 - 3
     * | /
     * 4
     */
    private SpatialModel<Double> basicGraph() {
        ImmutableGraphModel<Double> model =
                new ImmutableGraphModel<>(locationsSetLength);
        double d = 1;
        // 0 <-> 1
        model = model.add(0, d, 1);
        model = model.add(1, d, 0);
        // 0 <-> 2
        model = model.add(0, d, 2);
        model = model.add(2, d, 0);
        // 1 <-> 2
        model = model.add(1, d, 2);
        model = model.add(2, d, 1);
        // 2 <-> 3
        model = model.add(3, d, 2);
        model = model.add(2, d, 3);
        // 2 <-> 4
        model = model.add(4, d, 2);
        model = model.add(2, d, 4);
        // 3 <-> 4
        model = model.add(3, d, 4);
        model = model.add(4, d, 3);
        return model;
    }

    @Disabled("working on it")
    @Test
    void testSimpleFormula() {

        SpatialTemporalSignal<Double> signal =
                Utils.createSpatioTemporalSignal(5, 0, 1, 3.0,
                        (t, l) -> +l.doubleValue());

        var locSvc = Utils.createLocServiceStatic(0.0, 1, 1.0, basicGraph());

        HashMap<String, Function<Parameters, Function<Double, Double>>> atoms =
                new HashMap<>();
        atoms.put("a", p -> x -> x);
        atoms.put("b", p -> (x -> x + 10));


        HashMap<String, Function<SpatialModel<Double>,
                DistanceStructure<Double, ?>>> distF = new HashMap<>();
        DistanceStructure<Double, Double> basic =
                new DefaultDistanceStructure<>(x -> x, new DoubleDomain(),
                        0.0, 1.0, basicGraph());
        distF.put("basic", x -> basic);


        var domainB = new BooleanDomain();
        var domainR = new DoubleDomain();
        var m = new MfrMonitoring<>(atoms, distF, domainR, locSvc);

        var a = new AtomicFormula("a");
        var b = new AtomicFormula("b");

        var f1 = new BinaryFormula<>(a,
                new ReduceFormula<>("basic",
                        this::sum,
                        new MapFormula<Double>(x -> x + 10, b)),
                domainR::conjunction);

        var output = m.monitor(f1).monitor(signal).getSignals();
        System.out.println(output);
        assertTrue(true);
    }

    private Double sum(List<Double> values) {
        return values.stream().reduce(0.0, Double::sum);
    }
}
