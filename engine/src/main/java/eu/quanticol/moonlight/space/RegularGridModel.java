package eu.quanticol.moonlight.space;

import eu.quanticol.moonlight.core.space.SpatialModel;
import eu.quanticol.moonlight.core.base.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RegularGridModel<E> implements SpatialModel<E> {
    private final int columns;
    private final int size;
    private final E weight;

    public RegularGridModel(int rows, int columns, E weight) {
        this.columns = columns;
        this.size = rows * columns;
        this.weight = weight;
    }

    public E getWeight() {
        return weight;
    }

    public Pair<Integer, Integer> toCoordinates(int location) {
        checkLegalLocation(location);
        int column = location % columns;
        int row = location / columns;
        return new Pair<>(column, row);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public E get(int source, int target) {
        checkLegalLocation(source);
        checkLegalLocation(target);
        return !getNeighbours(source).contains(target) ? null : weight;
    }

    @Override
    public List<Pair<Integer, E>> next(int location) {
        checkLegalLocation(location);
        return listToWeighted(getNeighbours(location));
    }

    @Override
    public List<Pair<Integer, E>> previous(int location) {
        return next(location);
    }

    private void checkLegalLocation(int location) {
        if(location > size || location < 0)
            throw new IllegalArgumentException("invalid location passed");
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


