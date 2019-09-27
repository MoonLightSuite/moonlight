/**
 *
 */
package eu.quanticol.moonlight.tests;

import eu.quanticol.moonlight.formula.*;
import eu.quanticol.moonlight.monitoring.SpatioTemporalMonitoring;
import eu.quanticol.moonlight.signal.DistanceStructure;
import eu.quanticol.moonlight.signal.GraphModel;
import eu.quanticol.moonlight.signal.SpatialModel;
import eu.quanticol.moonlight.signal.SpatioTemporalSignal;
import eu.quanticol.moonlight.util.Pair;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.junit.Assert.*;
public class TestCity {
    int size = 7;
    double range = 40;


    public SpatialModel<Double> buildingCity() {

        HashMap<Pair<Integer,Integer>,Double> map = new HashMap<>();

        map.put(new Pair<>(0,1),2.0);
        map.put(new Pair<>(0,1), 2.0);
        map.put(new Pair<>(1,0), 2.0);
        map.put(new Pair<>(0,5), 2.0);
        map.put(new Pair<>(5,0), 2.0);
        map.put(new Pair<>(1,2), 9.0);
        map.put(new Pair<>(2,1), 9.0);
        map.put(new Pair<>(2,3), 3.0);
        map.put(new Pair<>(3,2), 3.0);
        map.put(new Pair<>(3,4), 6.0);
        map.put(new Pair<>(4,3), 6.0);
        map.put(new Pair<>(4,5), 7.0);
        map.put(new Pair<>(5,4), 7.0);
        map.put(new Pair<>(6,1), 4.0);
        map.put(new Pair<>(1,6), 4.0);
        map.put(new Pair<>(6,3), 15.0);
        map.put(new Pair<>(3,6), 15.0);

        return  TestUtils.createSpatialModel(this.size, map);
    }

    @Test
    public void testDistanceInCity() {

        SpatialModel<Double> city = buildingCity();
        DistanceStructure<Double, Double> ds = new DistanceStructure<Double, Double>(x -> x, new DoubleDistance(), 0.0, this.range, city);

        assertNotNull(city);
        assertEquals("d(0,1)",2.0,ds.getDistance(0, 1),0.0);
        assertEquals("d(0,2)",11.0,ds.getDistance(0, 2),0.0);

    }

    public void testAtomicPropCity() {

        SpatialModel<Double> city = buildingCity();
        double range = 40;
        DistanceStructure<Double, Double> ds = new DistanceStructure<Double, Double>(x -> x, new DoubleDistance(), 0.0, range, city);

        ArrayList<String> place = new ArrayList<>(Arrays.asList("BusStop", "Hospital", "MetroStop", "MainSquare", "BusStop", "Museum", "MetroStop"));
        ArrayList<Boolean> taxi=new ArrayList<>(Arrays.asList(false,false,true,false,false,true,false));
        ArrayList<Integer> people=new ArrayList<>(Arrays.asList(3,145,67,243,22,103,6));
        SpatioTemporalSignal<Pair<String, Boolean>> signal = TestUtils.createSpatioTemporalSignal(this.size, 0, 0.1, 10, (t, l) -> new Pair<>(place.get(l),taxi.get(l)));

        HashMap<String, Function<Parameters, Function<Pair<String,Boolean>, Boolean>>> atomic = new HashMap<>();
        atomic.put("isThereAStop", p -> (x -> x.getFirst().equals("BusStop") || x.getFirst().equals("MetroStop")));
        SpatioTemporalMonitoring<Boolean, Pair<String,Boolean>, Boolean> monitor = new SpatioTemporalMonitoring<Boolean, Pair<String, Boolean>, Boolean>(
                atomic ,
                new HashMap<>(),
                new BooleanDomain(),
                true);

        BiFunction<Function<Boolean, SpatialModel<Double>>, SpatioTemporalSignal<Pair<String,Boolean>>, SpatioTemporalSignal<Boolean>> m = monitor.monitor(new AtomicFormula("isThereAStop"), null);
        SpatioTemporalSignal<Boolean> sout = m.apply(t -> city, signal);


    }
}

