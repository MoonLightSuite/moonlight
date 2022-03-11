package eu.quanticol.moonlight.examples.city;

import eu.quanticol.moonlight.core.formula.Formula;
import eu.quanticol.moonlight.core.space.DefaultDistanceStructure;
import eu.quanticol.moonlight.core.space.DistanceStructure;
import eu.quanticol.moonlight.core.space.LocationService;
import eu.quanticol.moonlight.core.space.SpatialModel;
import eu.quanticol.moonlight.domain.DoubleDomain;
import eu.quanticol.moonlight.formula.*;
import eu.quanticol.moonlight.formula.classic.NegationFormula;
import eu.quanticol.moonlight.formula.classic.OrFormula;
import eu.quanticol.moonlight.formula.spatial.SomewhereFormula;
import eu.quanticol.moonlight.core.base.MoonLightRecord;
import eu.quanticol.moonlight.offline.monitoring.SpatialTemporalMonitoring;
import eu.quanticol.moonlight.offline.monitoring.spatialtemporal.SpatialTemporalMonitor;
import eu.quanticol.moonlight.core.base.DataHandler;
import eu.quanticol.moonlight.offline.signal.EnumerationHandler;
import eu.quanticol.moonlight.offline.signal.RecordHandler;
import eu.quanticol.moonlight.offline.signal.SpatialTemporalSignal;
import eu.quanticol.moonlight.domain.BooleanDomain;
import eu.quanticol.moonlight.core.signal.SignalDomain;
import eu.quanticol.moonlight.space.*;
import eu.quanticol.moonlight.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Function;


public class MainSp {
    public static void main(String[] argv) {
        Integer size = 7;
        GraphModel<Double> city = new GraphModel<Double>(size);
        city.add(0, 2.0, 1);
        city.add(1, 2.0, 0);
        city.add(0, 2.0, 5);
        city.add(5, 2.0, 0);
        city.add(1, 9.0, 2);
        city.add(2, 9.0, 1);
        city.add(2, 3.0, 3);
        city.add(3, 3.0, 2);
        city.add(3, 6.0, 4);
        city.add(4, 6.0, 3);
        city.add(4, 7.0, 5);
        city.add(5, 7.0, 4);
        city.add(6, 4.0, 1);
        city.add(1, 4.0, 6);
        city.add(6, 15.0, 3);
        city.add(3, 15.0, 6);

        String[] placeArray = new String[] { "BusStop", "Hospital", "MetroStop", "MainSquare", "BusStop", "Museum", "MetroStop" };
        ArrayList<String> place = new ArrayList<>(Arrays.asList(placeArray));
        ArrayList<Boolean> taxi = new ArrayList<>(Arrays.asList(false, false, true, false, false, true, false));
        ArrayList<Integer> people = new ArrayList<>(Arrays.asList(3, 145, 67, 243, 22, 103, 6));

        //// SpatioTemporalSignal
        RecordHandler factory = new RecordHandler(new EnumerationHandler<>(String.class, placeArray), DataHandler.BOOLEAN,DataHandler.INTEGER);
        ArrayList<MoonLightRecord> signalSP = new ArrayList<MoonLightRecord>();
        for (int i = 0; i < size; i++) {
            signalSP.add(factory.fromObjectArray(place.get(i), taxi.get(i), people.get(i)));
        }
        SpatialTemporalSignal<MoonLightRecord> citySignal = new SpatialTemporalSignal<>(size);
        citySignal.add(0, signalSP);
        citySignal.add(1, signalSP);
        citySignal.add(3, signalSP);

        LocationService<Double, Double> locService = Utils.createLocServiceStatic(0, 1, 3,city);

        HashMap<String, Function<Parameters, Function<MoonLightRecord, Boolean>>> atomicPropositions = new HashMap<>();
        atomicPropositions.put("thereIsATaxi", par -> a -> a.get(1, Boolean.class));
        atomicPropositions.put("thereIsAStop", par -> a -> a.get(0, String.class).equals("BusStop") || a.get(0, String.class).equals("MetroStop"));
        atomicPropositions.put("thereIsaMainSquare", par -> a -> a.get(0, String.class).equals("MainSquare"));
        atomicPropositions.put("thereIsanHospital", par -> a -> a.get(0, String.class).equals("Hospital"));
        atomicPropositions.put("manyPeople", par -> a -> a.get(2, Integer.class) > 200);


        Formula isH = new AtomicFormula("thereIsanHospital");
        Formula isT = new AtomicFormula("thereIsATaxi");
        Formula notIsH = new NegationFormula(isH);

        double range = 10;
        DefaultDistanceStructure<Double, Double> minutes = new DefaultDistanceStructure<>(x -> x, new DoubleDomain(), 0.0, range, city);
        HashMap<String, Function<SpatialModel<Double>, DistanceStructure<Double, ? extends Object>>> distanceFunctions = new HashMap<>();
        distanceFunctions.put("minutes", g -> minutes);
        Formula someT = new SomewhereFormula("minutes", isT);

        Formula r1 = new OrFormula(notIsH, someT);


        SignalDomain<Boolean> module = new BooleanDomain();
        SpatialTemporalMonitoring<Double, MoonLightRecord, Boolean> monitorFactory = new SpatialTemporalMonitoring<Double, MoonLightRecord, Boolean>(atomicPropositions, distanceFunctions, module, true);
        SpatialTemporalMonitor<Double, MoonLightRecord, Boolean> m = monitorFactory.monitor(someT, null);

        SpatialTemporalSignal<Boolean> out = m.monitor(locService, citySignal);

        System.out.println(out.getSignals().get(0));

        /// stop reach_{<=10} mainsquare
        //ArrayList<Boolean> reacmainsquare = minutes.reach(new BooleanDomain(), taxi::get, mainsquare::get);
    }


}



