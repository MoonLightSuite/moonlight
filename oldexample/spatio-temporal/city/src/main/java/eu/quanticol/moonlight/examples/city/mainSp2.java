package eu.quanticol.moonlight.examples.city;

import eu.quanticol.moonlight.formula.BooleanDomain;
import eu.quanticol.moonlight.formula.DoubleDistance;
import eu.quanticol.moonlight.signal.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


public class mainSp2 {
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


        //// Stop property
        ArrayList<Boolean> stop = new ArrayList<>();
        place.forEach(i -> stop.add(i.equals("BusStop") || i.equals("MetroStop")));
        System.out.println(stop);

        //// MainSquare property
        ArrayList<Boolean> mainsquare = new ArrayList<Boolean>();
        place.forEach(i -> mainsquare.add(i.equals("MainSquare")));

        //// notHospital property
        ArrayList<Boolean> notHospital = new ArrayList<Boolean>();
        place.forEach(i -> notHospital.add(!i.equals("Hospital")));

        //// Somewere Taxi property
        double range = 10;
        DistanceStructure<Double, Double> minutes = new DistanceStructure<>(x -> x, new DoubleDistance(), 0.0, range, city);
        ArrayList<Boolean> somewhereTaxy = minutes.somewhere(new BooleanDomain(), taxi::get);

        //// (R1) Hospital -> Somewere Taxi property
        ArrayList<Boolean> r1 = new ArrayList<Boolean>(size);
        for (int i = 0; i < size; i++) {
            r1.add(notHospital.get(i) || somewhereTaxy.get(i));
        }
        System.out.println(r1);

        /// stop reach_{<=10} mainsquare
        ArrayList<Boolean> reacmainsquare = minutes.reach(new BooleanDomain(), taxi::get, mainsquare::get);


        System.out.println(reacmainsquare);


        //// SpatioTemporalMonitoring

        RecordHandler factory = new RecordHandler(new EnumerationHandler<>(String.class, placeArray),DataHandler.BOOLEAN,DataHandler.INTEGER);
        HashMap<String, Integer> vTable = new HashMap<>();
        vTable.put("place", 1);
        vTable.put("taxi", 2);
        vTable.put("people", 3);

        //VariableArraySignal variableArraySignal = new VariableArraySignal(new String[]{"place", "taxi", "people"}, factory);
        // variableArraySignal.add(0, place.get(j), taxi.get(j), people.get(j));


        ArrayList<Record> signalSP = new ArrayList<Record>();
        for (int i = 0; i < size; i++) {
            signalSP.add(factory.fromObjectArray(place.get(i), taxi.get(i), people.get(i)));
        }

        SpatialTemporalSignal<Record> citySignal = new SpatialTemporalSignal<>(size);
        citySignal.add(0, signalSP);


    }


}



