package eu.quanticol.moonlight.space;

import eu.quanticol.moonlight.core.space.SpatialModel;
import eu.quanticol.moonlight.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RegularGridModel<E> implements SpatialModel<E> {
    private final int rows;
    private final int columns;
    private final int size;
    private final E weight;

    public RegularGridModel(int rows, int columns, E weight) {
        this.rows = rows;
        this.columns = columns;
        this.size = rows * columns;
        this.weight = weight;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public E get(int source, int target) {
        return !getNeighbours(source).contains(target) ? null : weight;
    }

    @Override
    public List<Pair<Integer, E>> next(int location) {
        return listToWeighted(getNeighbours(location));
    }

    @Override
    public List<Pair<Integer, E>> previous(int location) {
        return listToWeighted(getNeighbours(location));
    }

    @Override
    public Set<Integer> getLocations() {
        return IntStream.range(0, size)
                .boxed()
                .collect(Collectors.toSet());
    }

    private List<Pair<Integer, E>> listToWeighted(List<Integer> list) {
        return list.stream()
                   .map(x -> new Pair<>(x, weight))
                   .collect(Collectors.toList());
    }

    @NotNull
    private List<Integer> getNeighbours(int node) {
        List<Integer> neighbours = new ArrayList<>(4);

        if (node + columns < size)                      // bot boundary
            neighbours.add(node + columns);

        if (node - columns >= 0)                        // top boundary
            neighbours.add(node - columns);

        if (node % columns == 0) {                      // left border
            neighbours.add(node + 1);

        } else if (node % columns == columns - 1) {     // right border
            neighbours.add(node - 1);

        } else {                                        // others
            neighbours.add(node - 1);
            neighbours.add(node + 1);
        }

        return neighbours;
    }
}


