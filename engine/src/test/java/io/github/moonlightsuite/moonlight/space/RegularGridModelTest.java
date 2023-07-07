package io.github.moonlightsuite.moonlight.space;

import io.github.moonlightsuite.moonlight.core.base.Pair;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class RegularGridModelTest {
    @Test
    void gridNeighbourhoodIsCorrect() {
        RegularGridModel<Integer> grid = new RegularGridModel<>(3, 3, 1);

        for(int location = 0; location < grid.size(); location++) {
            assertEqualCollections(baseGrid().previous(location),
                                   grid.previous(location));

            assertEqualCollections(baseGrid().next(location),
                                   grid.next(location));
        }
    }

    private static <F extends Comparable<F>, S> void assertEqualCollections(
            List<Pair<F, S>> a,
            List<Pair<F, S>> b)
    {
        assertEquals(reorderList(a), reorderList(b));
    }

    private static <F extends Comparable<F>, S> List<Pair<F, S>> reorderList(
            List<Pair<F, S>> list)
    {
        return list.stream()
                   .sorted(Comparator.comparing(Pair::getFirst))
                   .collect(Collectors.toList());
    }

    private GraphModel<Integer> baseGrid() {
        GraphModel<Integer> model = new GraphModel<>(9);

        // 0
        model.add(0, 1, 1);
        model.add(0, 1, 3);
        // 1
        model.add(1, 1, 0);
        model.add(1, 1, 2);
        model.add(1, 1, 4);
        // 2
        model.add(2, 1, 1);
        model.add(2, 1, 5);
        // 3
        model.add(3, 1, 0);
        model.add(3, 1, 4);
        model.add(3, 1, 6);
        // 4
        model.add(4, 1, 1);
        model.add(4, 1, 3);
        model.add(4, 1, 5);
        model.add(4, 1, 7);
        // 5
        model.add(5, 1, 2);
        model.add(5, 1, 4);
        model.add(5, 1, 8);
        // 6
        model.add(6, 1, 3);
        model.add(6, 1, 7);
        // 7
        model.add(7, 1, 4);
        model.add(7, 1, 6);
        model.add(7, 1, 8);
        // 8
        model.add(8, 1, 5);
        model.add(8, 1, 7);
        return model;
    }
}
