package io.github.moonlightsuite.moonlight.examples.city;

import io.github.moonlightsuite.moonlight.core.algorithms.SpatialAlgorithms;
import io.github.moonlightsuite.moonlight.core.base.MoonLightRecord;
import io.github.moonlightsuite.moonlight.core.io.DataHandler;
import io.github.moonlightsuite.moonlight.core.space.DefaultDistanceStructure;
import io.github.moonlightsuite.moonlight.core.space.DistanceStructure;
import io.github.moonlightsuite.moonlight.domain.BooleanDomain;
import io.github.moonlightsuite.moonlight.domain.DoubleDomain;
import io.github.moonlightsuite.moonlight.offline.signal.EnumerationHandler;
import io.github.moonlightsuite.moonlight.offline.signal.RecordHandler;
import io.github.moonlightsuite.moonlight.offline.signal.SpatialTemporalSignal;
import io.github.moonlightsuite.moonlight.space.GraphModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static io.github.moonlightsuite.moonlight.core.algorithms.SpatialAlgorithms.reach;


public class mainSp2 {
    public static void main(String[] argv) {
        int size = 7;
        GraphModel<Double> city = new GraphModel<>(size);
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

        String[] placeArray = new String[]{"BusStop", "Hospital", "MetroStop", "MainSquare", "BusStop", "Museum", "MetroStop"};
        ArrayList<String> place = new ArrayList<>(Arrays.asList(placeArray));
        ArrayList<Boolean> taxi = new ArrayList<>(Arrays.asList(false, false, true, false, false, true, false));
        ArrayList<Integer> people = new ArrayList<>(Arrays.asList(3, 145, 67, 243, 22, 103, 6));


        //// Stop property
        List<Boolean> stop = new ArrayList<>();
        place.forEach(i -> stop.add(i.equals("BusStop") || i.equals("MetroStop")));
        System.out.println(stop);

        //// MainSquare property
        List<Boolean> mainsquare = new ArrayList<>();
        place.forEach(i -> mainsquare.add(i.equals("MainSquare")));

        //// notHospital property
        List<Boolean> notHospital = new ArrayList<>();
        place.forEach(i -> notHospital.add(!i.equals("Hospital")));

        //// Somewere Taxi property
        double range = 10;
        DistanceStructure<Double, Double> minutes = new DefaultDistanceStructure<>(x -> x, new DoubleDomain(), 0.0, range, city);

        var somewhereTaxy =
                new SpatialAlgorithms<>(minutes, new BooleanDomain(), false).everywhere(taxi::get);

        //// (R1) Hospital -> Somewere Taxi property
        List<Boolean> r1 = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            r1.add(notHospital.get(i) || somewhereTaxy.apply(i));
        }
        System.out.println(r1);

        /// stop reach_{<=10} mainsquare
        var reacmainsquare = reach(new BooleanDomain(), taxi::get,
                mainsquare::get, minutes);


        System.out.println(reacmainsquare);


        //// SpatioTemporalMonitoring

        RecordHandler factory = new RecordHandler(new EnumerationHandler<>(String.class, placeArray), DataHandler.BOOLEAN, DataHandler.INTEGER);
        HashMap<String, Integer> vTable = new HashMap<>();
        vTable.put("place", 1);
        vTable.put("taxi", 2);
        vTable.put("people", 3);

        //VariableArraySignal variableArraySignal = new VariableArraySignal(new String[]{"place", "taxi", "people"}, factory);
        // variableArraySignal.add(0, place.get(j), taxi.get(j), people.get(j));


        ArrayList<MoonLightRecord> signalSP = new ArrayList<MoonLightRecord>();
        for (int i = 0; i < size; i++) {
            signalSP.add(factory.fromObjectArray(place.get(i), taxi.get(i), people.get(i)));
        }

        SpatialTemporalSignal<MoonLightRecord> citySignal = new SpatialTemporalSignal<>(size);
        citySignal.add(0, signalSP);


    }


}



