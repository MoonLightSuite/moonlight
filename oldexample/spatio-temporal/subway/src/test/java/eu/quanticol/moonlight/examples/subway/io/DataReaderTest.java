package eu.quanticol.moonlight.examples.subway.io;

import eu.quanticol.moonlight.examples.subway.parsing.ParsingStrategy;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class DataReaderTest {
    private final String[] files = {
                                        "small_TXT.txt"
                                    ,   "small_CSV.csv"
                                    ,   "long_header_CSV.csv"
                                    ,   "unknown.unk"
                                   };
    private final FileType[] types = {  FileType.TEXT
                                      , FileType.CSV
                                      , FileType.CSV
                                     };



    @Test
    void readText() {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(files[0]).getFile());
        String path = file.getAbsolutePath();

        EmptyStrategy str = new EmptyStrategy();
        DataReader<Boolean> rdr = new DataReader<>(path, types[0], str);

        boolean processed = rdr.read();

        file.delete();

        assertTrue(processed);

        assertEquals(3, str.header_count);
        assertEquals(2, str.line_sum);
    }


    @Test
    void readCSV() {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(files[1]).getFile());
        String path = file.getAbsolutePath();

        EmptyStrategy str = new EmptyStrategy();
        DataReader<Boolean> rdr = new DataReader<>(path, types[1], str);

        boolean processed = rdr.read();

        file.delete();

        assertTrue(processed);

        assertEquals(4, str.header_count);
        assertEquals(2, str.line_sum);
    }

    @Test
    void readLongHeaderTxt() {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(files[2]).getFile());
        String path = file.getAbsolutePath();

        EmptyStrategy str = new EmptyStrategy();
        DataReader<Boolean> rdr = new DataReader<>(path, types[2], str);

        boolean processed = rdr.read();

        file.delete();

        assertTrue(processed);

        assertEquals(672, str.header_count);
        assertEquals(1421.099239665429, str.line_sum);
    }

    @Test
    void unsupportedFile() {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(files[3]).getFile());
        String path = file.getAbsolutePath();

        EmptyStrategy str = new EmptyStrategy();
        DataReader<Boolean> rdr = new DataReader<>(path, null, str);

        assertThrows(UnsupportedFileTypeException.class, rdr::read);

        file.delete();
    }


    /**
     * Mocked Parsing Strategy
     */
    class EmptyStrategy implements ParsingStrategy<Boolean> {
        int header_count = 0;
        int line_count = 0;
        double line_sum = 0;

        @Override
        public void initialize(String[] header) {
            for(String v : header) {
                header_count++;
            }
        }

        @Override
        public void process(String[] data) {
            line_count++;
            for(String v : data) {
                line_sum += Double.parseDouble(v);
            }
        }

        @Override
        public Boolean result() {
            return header_count > 0;
        }
    }
}