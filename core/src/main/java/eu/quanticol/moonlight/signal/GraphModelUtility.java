package eu.quanticol.moonlight.signal;

public class GraphModelUtility {

    private GraphModelUtility() {
        //utility class
    }

    public static GraphModel<Double> fromMatrix(double[][] matrix) {
        int size = matrix.length;
        GraphModel<Double> graphModel = new GraphModel<>(size);
        for (int i = 0; i < matrix.length; i++) {
            for (int j = i + 1; j < matrix[i].length; j++) {
                graphModel.add(i, matrix[i][j], j);
                graphModel.add(j, matrix[j][i], i);
            }
        }
        return graphModel;
    }
}