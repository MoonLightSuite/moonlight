package eu.quanticol.moonlight.tests;

import eu.quanticol.moonlight.core.base.Pair;
import eu.quanticol.moonlight.core.space.SpatialModel;
import eu.quanticol.moonlight.util.Utils;
import org.jgrapht.alg.shortestpath.FloydWarshallShortestPaths;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import java.util.function.Function;

public class TestPerformance {

    public static void main(String[] args) {
        System.out.println("5");
        System.out.println("Graph Model:");
        testDistance(Utils.createGridModelAsGraph(5, 5, false, 1.0), v -> v);
        System.out.println("With GraphT:");
        testDistanceWithGraph(Utils.createGridModelAsGraph(5, 5, false, 1.0), v -> v);
        System.out.println("Array:");
        testDistance(5, 5);


        System.out.println("10");
        System.out.println("Graph Model:");
        testDistance(Utils.createGridModelAsGraph(10, 10, false, 1.0), v -> v);
        System.out.println("With GraphT:");
        testDistanceWithGraph(Utils.createGridModelAsGraph(10, 10, false, 1.0), v -> v);
        System.out.println("Array:");
        testDistance(10, 10);

        System.out.println("20");
        System.out.println("Graph Model:");
        testDistance(Utils.createGridModelAsGraph(20, 20, false, 1.0), v -> v);
        System.out.println("With GraphT:");
        testDistanceWithGraph(Utils.createGridModelAsGraph(20, 20, false, 1.0), v -> v);
        System.out.println("Array:");
        testDistance(20, 20);


        System.out.println("30");
        System.out.println("Graph Model:");
        testDistance(Utils.createGridModelAsGraph(30, 30, false, 1.0), v -> v);
        System.out.println("With GraphT:");
        testDistanceWithGraph(Utils.createGridModelAsGraph(30, 30, false, 1.0), v -> v);
        System.out.println("Array:");
        testDistance(30, 30);

        System.out.println("50");
        System.out.println("Graph Model:");
        testDistance(Utils.createGridModelAsGraph(50, 50, false, 1.0), v -> v);
        System.out.println("With GraphT:");
        testDistanceWithGraph(Utils.createGridModelAsGraph(50, 50, false, 1.0), v -> v);
        System.out.println("Array:");
        testDistance(50, 50);

        System.out.println("100");
        System.out.println("Graph Model:");
        testDistance(Utils.createGridModelAsGraph(100, 100, false, 1.0), v -> v);
        System.out.println("With GraphT:");
        testDistanceWithGraph(Utils.createGridModelAsGraph(100, 100, false, 1.0), v -> v);
        System.out.println("Array:");
        testDistance(100, 100);


        //        testDistance(100,100);
//        testDistance(1000,1000);
        System.out.println("======");
//        testDistance(Utils.createGridModel(100, 100, false, 1.0),v -> v);
        //       testDistance(Utils.createGridModel(1000, 1000, false, 1.0),v -> v);
//
//
    }

    public static <T> void testDistanceWithGraph(SpatialModel<T> model, Function<T, Double> distance) {
        long start = System.currentTimeMillis();
        SimpleDirectedWeightedGraph<Integer, Double> graph = new SimpleDirectedWeightedGraph<>(Double.class);
        for (int i = 0; i < model.size(); i++) {
            graph.addVertex(i);
        }
        for (int i = 0; i < model.size(); i++) {
            for (Pair<Integer, T> p : model.next(i)) {
                graph.addEdge(i, p.getFirst(), distance.apply(p.getSecond()));
            }
        }
        FloydWarshallShortestPaths<Integer, Double> alg = new FloydWarshallShortestPaths<>(graph);
        for (int i = 0; i < model.size(); i++) {
            for (int j = 0; j < model.size(); j++) {
                alg.getPathWeight(i, j);
            }
        }
        double elapsed = System.currentTimeMillis() - start;
        System.out.println(elapsed / 1000);
    }

    public static <T> void testDistance(SpatialModel<T> model, Function<T, Double> distance) {
        long start = System.currentTimeMillis();
        double[][] matrix = new double[model.size()][model.size()];
        for (int i = 0; i < model.size(); i++) {
            for (int j = 0; j < model.size(); j++) {
                if (i == j) {
                    matrix[i][j] = 0.0;
                } else {
                    matrix[i][j] = Double.POSITIVE_INFINITY;
                }
            }
            for (Pair<Integer, T> post : model.next(i)) {
                matrix[i][post.getFirst()] = distance.apply(post.getSecond());
            }
        }
        for (int k = 0; k < model.size(); k++) {
            for (int i = 0; i < model.size(); i++) {
                for (int j = 0; j < model.size(); j++) {
                    if (matrix[i][j] > matrix[i][k] + matrix[k][j]) {
                        matrix[i][j] = matrix[i][k] + matrix[k][j];
                    }
                }
            }
        }
        double elapsed = System.currentTimeMillis() - start;
        System.out.println(elapsed / 1000);
    }

    public static <T> void testDistance(int row, int columns) {
        long start = System.currentTimeMillis();
        int size = row * columns;
        double[][] matrix = new double[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i == j) {
                    matrix[i][j] = 0.0;
                } else {
                    matrix[i][j] = Double.POSITIVE_INFINITY;
                }
            }
        }
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < columns; j++) {
                if (j < columns - 1) {
                    matrix[i * row + j][i * row + j + 1] = 1.0;
                }
                if (j > 0) {
                    matrix[i * row + j][i * row + j - 1] = 1.0;
                }
                if (i < row - 1) {
                    matrix[i * row + j][(i + 1) * row + j] = 1.0;
                }
                if (i > 0) {
                    matrix[i * row + j][(i - 1) * row + j] = 1.0;
                }
            }
        }
        for (int k = 0; k < size; k++) {
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if (matrix[i][j] > matrix[i][k] + matrix[k][j]) {
                        matrix[i][j] = matrix[i][k] + matrix[k][j];
                    }
                }
            }
        }
        double elapsed = System.currentTimeMillis() - start;
        System.out.println(elapsed / 1000);
    }


}
