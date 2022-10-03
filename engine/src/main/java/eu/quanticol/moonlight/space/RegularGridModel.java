package eu.quanticol.moonlight.space;

import eu.quanticol.moonlight.core.base.Pair;
import eu.quanticol.moonlight.core.space.SpatialModel;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RegularGridModel<E> implements SpatialModel<E> {
    private final int columns;
    private final int rows;
    private final int size;
    private final E weight;

    public RegularGridModel(int rows, int columns, E weight) {
        this.rows = rows;
        this.columns = columns;
        this.size = rows * columns;
        this.weight = weight;
    }

    public E getWeight() {
        return weight;
    }

    public int[] toCoordinates(int location) {
        if (location >= 0 && location <= size) {
            int column = location % columns;
            int row = location / columns;
            return new int[]{column, row};
        } else {
            throw new IllegalArgumentException("invalid location passed");
        }
    }

    public int[] unsafeToCoordinates(int location) {
        int column = location % columns;
        int row = location / columns;
        return new int[]{column, row};
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public E get(int source, int target) {
        checkLocationIsLegal(source);
        checkLocationIsLegal(target);
        return !getNeighbours(source).contains(target) ? null : weight;
    }

    private void checkLocationIsLegal(int location) {
        if (location > size || location < 0)
            throw new IllegalArgumentException("invalid location passed");
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

    public int[] getNeighboursArray(int node) {
        return getNeighbours(node).stream().mapToInt(i -> i).toArray();
    }

    @Override
    public List<Pair<Integer, E>> previous(int location) {
        return next(location);
    }

    @Override
    public List<Pair<Integer, E>> next(int location) {
        checkLocationIsLegal(location);
        return listToWeighted(getNeighbours(location));
    }

    private List<Pair<Integer, E>> listToWeighted(List<Integer> list) {
        return list.stream()
                .map(x -> new Pair<>(x, weight))
                .toList();
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public int fromCoordinates(int x, int y) {
        return y * columns + x;
    }
}


