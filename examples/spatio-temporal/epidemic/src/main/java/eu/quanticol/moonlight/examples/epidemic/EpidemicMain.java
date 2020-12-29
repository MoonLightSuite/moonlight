package eu.quanticol.moonlight.examples.epidemic;

import eu.quanticol.moonlight.io.CsvLocationServiceReader;
import eu.quanticol.moonlight.io.CsvSpatialTemporalSignalReader;
import eu.quanticol.moonlight.io.IllegalFileFormatException;
import eu.quanticol.moonlight.signal.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

public class EpidemicMain {

    public static void main(String[] argv)  throws IllegalFileFormatException, URISyntaxException, IOException {
        ClassLoader classLoader = EpidemicMain.class.getClassLoader();

        // space model
        URL resourceL = classLoader.getResource("epidemic_simulation_network_0.txt");
        File fileL = new File(resourceL.toURI());
        CsvLocationServiceReader readerL =  new CsvLocationServiceReader();
        RecordHandler rhL = new RecordHandler(DataHandler.REAL);
        LocationService<Record> ls = readerL.read(rhL, fileL);

        // trajectory
        URL resourceT = classLoader.getResource("epidemic_simulation_trajectory_0.txt");
        File fileT = new File(resourceT.toURI());
        RecordHandler rhT = new RecordHandler(DataHandler.REAL);
        CsvSpatialTemporalSignalReader readerT = new CsvSpatialTemporalSignalReader();
        SpatialTemporalSignal<Record> s = readerT.load(rhT, fileT);


    }

}
