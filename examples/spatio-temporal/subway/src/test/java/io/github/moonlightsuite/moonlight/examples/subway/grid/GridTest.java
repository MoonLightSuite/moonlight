package io.github.moonlightsuite.moonlight.examples.subway.grid;

import io.github.moonlightsuite.moonlight.domain.DoubleDomain;
import io.github.moonlightsuite.moonlight.core.space.DefaultDistanceStructure;
import io.github.moonlightsuite.moonlight.core.space.SpatialModel;
import io.github.moonlightsuite.moonlight.examples.subway.Subway;
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

        DefaultDistanceStructure<Double, Double> ds =
                new DefaultDistanceStructure<>(x -> x, new DoubleDomain(),
                              0.0, (double) network.size(), network);
        Double d = ds.getDistance(21, 42);
        assertEquals(1, d);
    }
}