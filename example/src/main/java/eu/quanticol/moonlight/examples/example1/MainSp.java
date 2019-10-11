package eu.quanticol.moonlight.examples.example1;

import eu.quanticol.moonlight.monitoring.SpatioTemporalMonitoring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.BiFunction;
import java.util.function.DoubleFunction;
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

        ArrayList<String> place = new ArrayList<String>(Arrays.asList("BusStop", "Hospital", "MetroStop", "MainSquare", "BusStop", "Museum", "MetroStop"));
        ArrayList<Boolean> taxi=new ArrayList<>(Arrays.asList(false,false,true,false,false,true,false));
        ArrayList<Integer> people=new ArrayList<>(Arrays.asList(3,145,67,243,22,103,6));

        //// SpatioTemporalSignal
        AssignmentFactory factory = new AssignmentFactory(String.class, Boolean.class, Integer.class);
        ArrayList<Assignment> signalSP = new ArrayList<Assignment>();
        for (int i = 0; i < size; i++) {
            signalSP.add(factory.get(place.get(i), taxi.get(i), people.get(i)));
        }
        SpatioTemporalSignal<Assignment> citySignal = new SpatioTemporalSignal<>(size);
        citySignal.add(0,signalSP);
        citySignal.add(1,signalSP);
        citySignal.add(3,signalSP);
 
        HashMap<String,Function<Parameters,Function<Assignment,Boolean>>> atomicPropositions = new HashMap<>();
        atomicPropositions.put( "thereIsATaxi" , par -> a -> a.get(1, Boolean.class) );
        atomicPropositions.put( "thereIsAStop" , par -> a -> a.get(0, String.class).equals("BusStop") || a.get(0, String.class).equals("MetroStop") );
        atomicPropositions.put("thereIsaMainSquare" , par -> a -> a.get(0, String.class).equals("MainSquare") );
        atomicPropositions.put("thereIsanHospital" , par -> a -> a.get(0, String.class).equals("Hospital") );
        atomicPropositions.put( "manyPeople" , par -> a -> a.get(2, Integer.class) > 200 );


        Formula isH = new AtomicFormula("thereIsanHospital");
        Formula isT = new AtomicFormula("thereIsATaxi");
        Formula notIsH = new NegationFormula(isH);

        double range = 10;
        DistanceStructure<Double, Double> minutes = new DistanceStructure<>(x -> x, new DoubleDistance(), 0.0 , range, city);
        HashMap<String,Function<SpatialModel<Double>,DistanceStructure<Double,? extends Object>>> distanceFunctions = new HashMap<>();
        distanceFunctions.put("minutes", g -> minutes);
        Formula someT = new SomewhereFormula("minutes", isT);

        Formula r1 = new OrFormula(notIsH,someT);


        SignalDomain<Boolean> module = new BooleanDomain();
		SpatioTemporalMonitoring<Double, Assignment, Boolean> monitorFactory = new SpatioTemporalMonitoring<Double, Assignment, Boolean>(atomicPropositions, distanceFunctions, module, true);
        BiFunction<DoubleFunction<SpatialModel<Double>>, SpatioTemporalSignal<Assignment>, SpatioTemporalSignal<Boolean>> m = monitorFactory.monitor(someT, null);

		SpatioTemporalSignal<Boolean> out = m.apply(t -> city, citySignal);

        System.out.println(out.getSignals().get(0));

        /// stop reach_{<=10} mainsquare
        //ArrayList<Boolean> reacmainsquare = minutes.reach(new BooleanDomain(), taxi::get, mainsquare::get);
    }


}



