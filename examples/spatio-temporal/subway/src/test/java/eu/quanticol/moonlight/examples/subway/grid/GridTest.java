package eu.quanticol.moonlight.examples.subway.grid;

import eu.quanticol.moonlight.domain.DoubleDistance;
import eu.quanticol.moonlight.examples.subway.Subway;
import eu.quanticol.moonlight.space.DistanceStructure;
import eu.quanticol.moonlight.space.SpatialModel;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class GridTest {

    private static final String NETWORK_FILE = "adjacent_matrix_milan_grid_21x21.txt";
    private static final InputStream NETWORK_SOURCE =
            Objects.requireNonNull(Subway.class
                    .getResourceAsStream(NETWORK_FILE));

    @Test
    void distance() {
        SpatialModel<Double> network = new Grid().getModel(NETWORK_SOURCE);
        //SpatialModel<Double> network = Grid.simulateModel();

        DistanceStructure<Double, Double> ds =
                new DistanceStructure<>(x -> x, new DoubleDistance(),
                              0.0, (double) network.size(), network);
        Double d = ds.getDistance(21, 42);
        assertEquals(1, d);
    }
}