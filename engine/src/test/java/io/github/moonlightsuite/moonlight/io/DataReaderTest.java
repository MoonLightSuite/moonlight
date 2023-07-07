package io.github.moonlightsuite.moonlight.io;

import io.github.moonlightsuite.moonlight.io.parsing.FileType;
import io.github.moonlightsuite.moonlight.util.Logger;
import io.github.moonlightsuite.moonlight.io.parsing.ParsingStrategy;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class DataReaderTest {
    private final Logger LOG = Logger.getLogger();
    private static final String FILE_SYSTEM_ERROR = "Problems accessing the " +
                                                    "file system";

    private final String[] files = {
                                        "dataFormats/small_TXT.txt"
                                    ,   "dataFormats/small_CSV.csv"
                                    ,   "dataFormats/long_header_CSV.csv"
                                    ,   "dataFormats/unknown.unk"
                                   };
    private final FileType[] types = {  FileType.TEXT
                                      , FileType.CSV
                                      , FileType.CSV
                                     };



    @Test
    void readText() {
        InputStream source = getClass().getClassLoader().getResourceAsStream(files[0]);

        if(source != null) {
            EmptyStrategy str = new EmptyStrategy();
            DataReader<Boolean> rdr = new DataReader<>(source, types[0], str);

            boolean processed = rdr.read();

            assertTrue(processed);

            assertEquals(3, str.header_count);
            assertEquals(2, str.line_sum);
        } else {
            LOG.warn(FILE_SYSTEM_ERROR);
            fail();
        }
    }


    @Test
    void readCSV() {
        InputStream source = getClass().getClassLoader().getResourceAsStream(files[1]);

        if(source != null) {
            EmptyStrategy str = new EmptyStrategy();
            DataReader<Boolean> rdr = new DataReader<>(source, types[1], str);

            boolean processed = rdr.read();

            assertTrue(processed);

            assertEquals(4, str.header_count);
            assertEquals(2, str.line_sum);
        } else {
            LOG.warn(FILE_SYSTEM_ERROR);
            fail();
        }
    }

    @Test
    void readLongHeaderTxt() {
        InputStream source = getClass().getClassLoader().getResourceAsStream(files[2]);

        if(source != null) {
            EmptyStrategy str = new EmptyStrategy();
            DataReader<Boolean> rdr = new DataReader<>(source, types[2], str);

            boolean processed = rdr.read();

            assertTrue(processed);

            assertEquals(672, str.header_count);
            assertEquals(1421.099239665429, str.line_sum);
        } else {
            LOG.warn(FILE_SYSTEM_ERROR);
            fail();
        }
    }

    @Test
    void unsupportedFile() {
        InputStream source = getClass().getClassLoader().getResourceAsStream(files[3]);

        if(source != null) {
            EmptyStrategy str = new EmptyStrategy();
            DataReader<Boolean> rdr = new DataReader<>(source, null, str);

            assertThrows(UnsupportedFileTypeException.class, rdr::read);
        } else {
            LOG.warn(FILE_SYSTEM_ERROR);
            fail();
        }
    }


    /**
     * Mocked Parsing Strategy Mock
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
