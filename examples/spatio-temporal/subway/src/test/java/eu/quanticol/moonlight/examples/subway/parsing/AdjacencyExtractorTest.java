package eu.quanticol.moonlight.examples.subway.parsing;

import eu.quanticol.moonlight.space.GraphModel;
import eu.quanticol.moonlight.space.ImmutableGraphModel;
import eu.quanticol.moonlight.space.SpatialModel;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

class AdjacencyExtractorTest {

    private final String[][] data = {   {"0", "1"}
                                    ,   {"1", "0"}
                                    };

    @Test
    void headerSize() {
        ParsingStrategy<ImmutableGraphModel<Double>> str = new AdjacencyExtractor();

        str.initialize(data[0]);
        SpatialModel<Double> grid = str.result();

        assertEquals(2, grid.size());
    }

    @Test
    void processData() {
        ParsingStrategy<ImmutableGraphModel<Double>> str = new AdjacencyExtractor();

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