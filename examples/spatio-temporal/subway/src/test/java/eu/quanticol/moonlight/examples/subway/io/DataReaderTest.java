package eu.quanticol.moonlight.examples.subway.io;

import eu.quanticol.moonlight.examples.subway.parsing.ParsingStrategy;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class DataReaderTest {
    private static final Logger LOG = Logger.getLogger(DataReaderTest.class.getName());
    private static final String FILE_SYSTEM_ERROR = "Problems accessing the file system";

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
        URL source = getClass().getClassLoader().getResource(files[0]);

        if(source != null) {
            File file = new File(source.getFile());
            String path = file.getAbsolutePath();

            EmptyStrategy str = new EmptyStrategy();
            DataReader<Boolean> rdr = new DataReader<>(path, types[0], str);

            boolean processed = rdr.read();

            assertTrue(processed);

            assertEquals(3, str.header_count);
            assertEquals(2, str.line_sum);
        } else
            LOG.warning(FILE_SYSTEM_ERROR);
    }


    @Test
    void readCSV() {
        URL source = getClass().getClassLoader().getResource(files[1]);

        if(source != null) {
            File file = new File(source.getFile());
            String path = file.getAbsolutePath();

            EmptyStrategy str = new EmptyStrategy();
            DataReader<Boolean> rdr = new DataReader<>(path, types[1], str);

            boolean processed = rdr.read();

            assertTrue(processed);

            assertEquals(4, str.header_count);
            assertEquals(2, str.line_sum);
        } else
            LOG.warning(FILE_SYSTEM_ERROR);
    }

    @Test
    void readLongHeaderTxt() {
        URL source = getClass().getClassLoader().getResource(files[2]);

        if(source != null) {
            File file = new File(source.getFile());
            String path = file.getAbsolutePath();

            EmptyStrategy str = new EmptyStrategy();
            DataReader<Boolean> rdr = new DataReader<>(path, types[2], str);

            boolean processed = rdr.read();

            assertTrue(processed);

            assertEquals(672, str.header_count);
            assertEquals(1421.099239665429, str.line_sum);
        } else
            LOG.warning(FILE_SYSTEM_ERROR);
    }

    @Test
    void unsupportedFile() {
        URL source = getClass().getClassLoader().getResource(files[3]);

        if(source != null) {
            File file = new File(source.getFile());
            String path = file.getAbsolutePath();

            EmptyStrategy str = new EmptyStrategy();
            DataReader<Boolean> rdr = new DataReader<>(path, null, str);

            assertThrows(UnsupportedFileTypeException.class, rdr::read);
        } else
            LOG.warning(FILE_SYSTEM_ERROR);
    }


    /**
     * Mocked Parsing Strategy
     */
    static class EmptyStrategy implements ParsingStrategy<Boolean> {
        int header_count = 0;
        int line_count = 0;
        double line_sum = 0;

        @Override
        public void initialize(String[] header) {
            header_count = header.length;
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