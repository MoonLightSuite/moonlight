package eu.quanticol.moonlight.io.parsing;

import eu.quanticol.moonlight.core.space.SpatialModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AdjacencyExtractorTest {

    private final String[][] data = {   {"0", "1"}
                                    ,   {"1", "0"}
                                    };

    @Test
    void headerSize() {
        ParsingStrategy<SpatialModel<Double>> str = new AdjacencyExtractor();

        str.initialize(data[0]);
        SpatialModel<Double> grid = str.result();

        assertEquals(2, grid.size());
    }

    @Test
    void processData() {
        ParsingStrategy<SpatialModel<Double>> str = new AdjacencyExtractor();

        str.initialize(data[0]);

        for(String[] s: data) {
            str.process(s);
        }

        SpatialModel<Double> grid = str.result();

        assertEquals(1.0, grid.get(0, 1), 0.0);
        assertEquals(1.0, grid.get(1, 0), 0.0);

        assertNull(grid.get(1,1));
    }
}