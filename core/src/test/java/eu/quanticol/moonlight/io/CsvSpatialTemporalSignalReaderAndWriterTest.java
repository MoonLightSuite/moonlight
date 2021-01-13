package eu.quanticol.moonlight.io;

import eu.quanticol.moonlight.signal.*;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CsvSpatialTemporalSignalReaderAndWriterTest {

    @Test
    public void testLoadSignal() throws IllegalFileFormatException {
        String data =
                "LOCATIONS 3\n" +
                "0.0;1.0;1.0;1.0\n" +
                "1.0;2.0;3.0;4.0\n" +
                "2.0;4.0;9.0;16.0\n" +
                "3.0;8.0;27.0;64.0\n";

        RecordHandler rh = new RecordHandler(DataHandler.REAL);
        CsvSpatialTemporalSignalReader reader = new CsvSpatialTemporalSignalReader();
        reader.load(rh,data);
        assertTrue(true);
    }

    @Test
    public void testLoadSignalWithSpaces() throws IllegalFileFormatException {
        String data =
                "LOCATIONS 3\n" +
                        "0.0; 1.0; 1.0; 1.0\n" +
                        "1.0; 2.0; 3.0; 4.0\n" +
                        "2.0; 4.0; 9.0; 16.0\n" +
                        "3.0; 8.0; 27.0; 64.0\n";

        RecordHandler rh = new RecordHandler(DataHandler.REAL);
        CsvSpatialTemporalSignalReader reader = new CsvSpatialTemporalSignalReader();
        reader.load(rh,data);
        assertTrue(true);
    }

    @Test
    public void testLoadSignalIntegerWithSpaces() throws IllegalFileFormatException {
        String data =
                "LOCATIONS 3\n" +
                        "0.0; 1; 1; 1\n" +
                        "1.0; 2; 3; 4\n" +
                        "2.0; 4; 9; 16\n" +
                        "3.0; 8; 27; 64\n";

        RecordHandler rh = new RecordHandler(DataHandler.INTEGER);
        CsvSpatialTemporalSignalReader reader = new CsvSpatialTemporalSignalReader();
        reader.load(rh,data);
        assertTrue(true);
    }

    @Test
    public void testLoadSignal2() throws IllegalFileFormatException {
        String data =
                "LOCATIONS 3\n" +
                        "0.0;1.0;1.0;1.0\n" +
                        "1.0;2.0;3.0;4.0\n" +
                        "2.0;4.0;9.0;16.0\n" +
                        "3.0;8.0;27.0;64.0\n";

        RecordHandler rh = new RecordHandler(DataHandler.REAL);
        CsvSpatialTemporalSignalReader reader = new CsvSpatialTemporalSignalReader();
        SpatialTemporalSignal<MoonLightRecord> s = reader.load(rh,data);
        List<Signal<MoonLightRecord>> signals = s.getSignals();
        for(int i=0 ; i<3; i++) {
            Signal<MoonLightRecord> ls = signals.get(i);
            for (double t=0.0; t<4.0;t=t+1.0) {
                assertEquals(Math.pow(i+2.0,t),ls.valueAt(t).get(0,Double.class));
            }
        }
    }

    @Test
    public void testReadAndWrite() throws IllegalFileFormatException {
        String data =
                "LOCATIONS 3\n" +
                        "0.0;1.0;1.0;1.0\n" +
                        "1.0;2.0;3.0;4.0\n" +
                        "2.0;4.0;9.0;16.0\n" +
                        "3.0;8.0;27.0;64.0\n";

        RecordHandler rh = new RecordHandler(DataHandler.REAL);
        CsvSpatialTemporalSignalReader reader = new CsvSpatialTemporalSignalReader();
        SpatialTemporalSignal<MoonLightRecord> s = reader.load(rh,data);
        CsvSpatialTemporalSignalWriter writer = new CsvSpatialTemporalSignalWriter();
        String output = writer.stringOf(rh,s);
        assertEquals(data,output);
    }

    @Test
    public void ReadCSVtraj() throws IllegalFileFormatException, URISyntaxException, IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("epidemic_simulation_trajectory_0.txt");
        File file = new File(resource.toURI());
        //List<String> lines = Files.readAllLines(file.toPath(), );
        //lines.forEach(System.out::println);
        RecordHandler rh = new RecordHandler(DataHandler.REAL);
        CsvSpatialTemporalSignalReader reader = new CsvSpatialTemporalSignalReader();
        SpatialTemporalSignal<MoonLightRecord> s = reader.load(rh, file);
        CsvSpatialTemporalSignalWriter writer = new CsvSpatialTemporalSignalWriter();
        //String output = writer.stringOf(rh, s);
        assertNotNull(reader.load(rh, file));
    }

}
