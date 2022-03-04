package eu.quanticol.moonlight.space;

import eu.quanticol.moonlight.core.space.DistanceDomain;
import eu.quanticol.moonlight.core.space.DistanceStructure;
import eu.quanticol.moonlight.domain.IntegerDomain;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class ManhattanDistanceStructureTest {
    @Test
    void simpleDistance() {
        DistanceDomain<Integer> domain = new IntegerDomain();
        RegularGridModel<Integer> model =
                new RegularGridModel<>(3, 3, 1);
        DistanceStructure<Integer, Integer> dist =
                new ManhattanDistanceStructure<>(defaultDistance(), domain,
                                       0, 10, model);

        assertEquals(2, dist.getDistance(0, 2));
        assertEquals(1, dist.getDistance(0, 3));
    }

    private Function<Integer, Integer> defaultDistance() {
        return x -> 1;
    }
}