package eu.quanticol.moonlight.examples.subway.grid;

import eu.quanticol.moonlight.domain.DoubleDistance;
import eu.quanticol.moonlight.examples.subway.Erlang;
import eu.quanticol.moonlight.signal.space.DistanceStructure;
import eu.quanticol.moonlight.signal.space.SpatialModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GridTest {

    private static final String NETWORK_FILE = "adjacent_matrix_milan_grid_21x21.txt";
    private static final String NETWORK_SOURCE =
            Erlang.class.getResource(NETWORK_FILE).getPath();

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