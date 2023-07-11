package io.github.moonlightsuite.moonlight.api;

/**
 * The program provides a sample for Java Engine functions. It creates a matrix and squares the values greater than 5.
 * Copyright 2016-2017 The MathWorks, Inc.
 */

import com.mathworks.engine.MatlabEngine;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class EngineConsoleDemo {
    public static void main(String args[]) {
        try {
            //Start MATLAB asynchronously
            Future<MatlabEngine> eng = MatlabEngine.startMatlabAsync();

            // Get engine instance from the future result
            MatlabEngine ml = eng.get();

            /*
             * Find elements greater than 5
             * 1. Create input matrix
             * 2. Put variable matrix in the MATLAB base workspace
             * 3. Solve A(A>5) in MATLAB
             * 4. Return results and display
             * 5. Call the power function in MATLAB on the returned matrix
             * 6. Display results
             */
            double[][] input = new double[4][4];
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    double num = Math.random() * 10;
                    input[i][j] = num;
                }
            }
            System.out.println("\nFind numbers from a matrix that are greater than five and square them:\n");
            System.out.println("Input matrix: ");
            for (int i = 0; i < 4; i++) {
                {
                    for (int j = 0; j < 4; j++) {
                        System.out.print(String.format("%.2f", input[i][j]) + "\t");
                    }
                    System.out.print("\n");
                }
            }

            // Put the matrix in the MATLAB workspace
            ml.putVariableAsync("A", input);

            // Evaluate the command to search in MATLAB
            ml.eval("B=A(A>5);");

            // Get result from the workspace
            Future<double[]> futureEval = ml.getVariableAsync("B");
            double[] output = futureEval.get();

            // Display result
            System.out.println("\nElements greater than 5: ");
            for (int i = 0; i < output.length; i++) {
                System.out.print(" " + String.format("%.2f", output[i]));
            }

            // Square the returned elements using the power function in MATLAB
            double[] powResult = ml.feval("power", output, Double.valueOf(2));
            System.out.println("\n\nSquare of numbers greater than 5:");
            for (int i = 0; i < powResult.length; i++) {

                //Set precision for the output values
                System.out.print(" " + String.format("%.2f", powResult[i]));
            }
            System.out.println("\n");

            // Disconnect from the MATLAB session
            ml.disconnect();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
