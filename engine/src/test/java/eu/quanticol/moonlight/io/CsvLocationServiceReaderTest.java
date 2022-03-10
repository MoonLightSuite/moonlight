package eu.quanticol.moonlight.io;

import eu.quanticol.moonlight.core.base.MoonLightRecord;
import eu.quanticol.moonlight.core.base.DataHandler;
import eu.quanticol.moonlight.offline.signal.RecordHandler;
import eu.quanticol.moonlight.core.space.LocationService;
import eu.quanticol.moonlight.core.space.SpatialModel;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

class CsvLocationServiceReaderTest {

    @Test
    public void loadStaticLocationService() throws IllegalFileFormatException {
        String input = "LOCATIONS 3\n" +
                "STATIC\n" +
                "0; 1; 1; 1.0 ; true\n" +
                "1;2;2;2.0;false\n" +
                "2;0;3;3.0;true";
        CsvLocationServiceReader reader = new CsvLocationServiceReader();
        RecordHandler rh = new RecordHandler(DataHandler.INTEGER, DataHandler.REAL, DataHandler.BOOLEAN);
        assertNotNull(reader.read(rh, input));
    }

    @Test
    public void loadStaticLocationServiceAndCheckData() throws IllegalFileFormatException {
        String input = "LOCATIONS 3\n" +
                "STATIC\n" +
                "0;1;1;1.0;true\n" +
                "1;2;2;2.0;false\n" +
                "2;0;3;3.0;true";
        CsvLocationServiceReader reader = new CsvLocationServiceReader();
        RecordHandler rh = new RecordHandler(DataHandler.INTEGER, DataHandler.REAL, DataHandler.BOOLEAN);
        LocationService<Double, MoonLightRecord> ls = reader.read(rh, input);
        SpatialModel<MoonLightRecord> model = ls.get(100.0);
        MoonLightRecord record;
        record = model.get(0, 1);
        assertEquals(1, record.get(0));
        assertEquals(1.0, record.get(1));
        assertEquals(true, record.get(2));
        record = model.get(1, 0);
        assertEquals(1, record.get(0));
        assertEquals(1.0, record.get(1));
        assertEquals(true, record.get(2));
        record = model.get(1, 2);
        assertEquals(2, record.get(0));
        assertEquals(2.0, record.get(1));
        assertEquals(false, record.get(2));
        record = model.get(2, 1);
        assertEquals(2, record.get(0));
        assertEquals(2.0, record.get(1));
        assertEquals(false, record.get(2));
        record = model.get(2, 0);
        assertEquals(3, record.get(0));
        assertEquals(3.0, record.get(1));
        assertEquals(true, record.get(2));
        record = model.get(0, 2);
        assertEquals(3, record.get(0));
        assertEquals(3.0, record.get(1));
        assertEquals(true, record.get(2));
    }

    @Test
    public void loadStaticLocationServiceDirectedAndCheckData() throws IllegalFileFormatException {
        String input = "LOCATIONS 3\n" +
                "DIRECTED\n" +
                "STATIC\n" +
                "0;1;1;1.0;true\n" +
                "1;2;2;2.0;false\n" +
                "2;0;3;3.0;true";
        CsvLocationServiceReader reader = new CsvLocationServiceReader();
        RecordHandler rh = new RecordHandler(DataHandler.INTEGER, DataHandler.REAL, DataHandler.BOOLEAN);
        LocationService<Double, MoonLightRecord> ls = reader.read(rh, input);
        SpatialModel<MoonLightRecord> model = ls.get(100.0);
        MoonLightRecord record;
        record = model.get(0, 1);
        assertEquals(1, record.get(0));
        assertEquals(1.0, record.get(1));
        assertEquals(true, record.get(2));
        assertNull(model.get(1, 0));
        record = model.get(1, 2);
        assertEquals(2, record.get(0));
        assertEquals(2.0, record.get(1));
        assertEquals(false, record.get(2));
        assertNull(model.get(2, 1));
        record = model.get(2, 0);
        assertEquals(3, record.get(0));
        assertEquals(3.0, record.get(1));
        assertEquals(true, record.get(2));
        assertNull(model.get(0, 2));
    }

    @Test
    public void loadDynamicLocationService() throws IllegalFileFormatException {
        String input = "LOCATIONS 3\n" +
                "TIME 0.0\n" +
                "0;1;1;1.0;true\n" +
                "TIME 1.0\n" +
                "0;1;1;1.0;true\n" +
                "1;2;2;2.0;false\n" +
                "TIME 2.0\n" +
                "0;1;1;1.0;true\n" +
                "1;2;2;2.0;false\n" +
                "2;0;3;3.0;true";
        CsvLocationServiceReader reader = new CsvLocationServiceReader();
        RecordHandler rh = new RecordHandler(DataHandler.INTEGER, DataHandler.REAL, DataHandler.BOOLEAN);
        assertNotNull(reader.read(rh, input));
    }

    @Test
    public void loadDynamicLocationServiceAndCheckData() throws IllegalFileFormatException {
        String input = "LOCATIONS 3\n" +
                "TIME 0.0\n" +
                "0;1;1;1.0;true\n" +
                "TIME 1.0\n" +
                "0;1;1;1.0;true\n" +
                "1;2;2;2.0;false\n" +
                "TIME 2.0\n" +
                "0;1;1;1.0;true\n" +
                "1;2;2;2.0;false\n" +
                "2;0;3;3.0;true";
        CsvLocationServiceReader reader = new CsvLocationServiceReader();
        RecordHandler rh = new RecordHandler(DataHandler.INTEGER, DataHandler.REAL, DataHandler.BOOLEAN);
        LocationService<Double, MoonLightRecord> ls = reader.read(rh, input);
        SpatialModel<MoonLightRecord> model = ls.get(0.0);
        assertEquals(rh.fromString("1;1.0;true"), model.get(0, 1));
        assertNull(model.get(1, 2));
        assertNull(model.get(2, 0));
        model = ls.get(1.0);
        assertEquals(rh.fromString("1;1.0;true"), model.get(0, 1));
        assertEquals(rh.fromString("2;2.0;false"), model.get(1, 2));
        assertNull(model.get(2, 0));
        model = ls.get(2.5);
        assertEquals(rh.fromString("1;1.0;true"), model.get(0, 1));
        assertEquals(rh.fromString("2;2.0;false"), model.get(1, 2));
        assertEquals(rh.fromString("3;3.0;true"), model.get(2, 0));
    }

    @Test
    public void loadLocationServiceFromFile() throws IllegalFileFormatException, URISyntaxException, IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("epidemic_simulation_network_0.txt");
        File file = new File(resource.toURI());
        CsvLocationServiceReader reader =  new CsvLocationServiceReader();
        RecordHandler rh = new RecordHandler(DataHandler.REAL);
        assertNotNull(reader.read(rh,file));
    }
}