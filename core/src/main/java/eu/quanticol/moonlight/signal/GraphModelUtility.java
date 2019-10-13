package eu.quanticol.moonlight.signal;

public class GraphModelUtility {

    private GraphModelUtility() {
        //utility class
    }

    public static GraphModel<Double> fromMatrix(double[][] matrix) {
        int size = matrix.length;
        GraphModel<Double> graphModel = new GraphModel<>(size);
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                graphModel.add(i, matrix[i][j], j);
            }
        }
        return graphModel;
    }
}