package eu.quanticol.moonlight.utility.matlab;


import com.mathworks.engine.MatlabEngine;

import java.util.concurrent.ExecutionException;

public class EngineMatlabTest {

    public static void main(String args[]) {
        System.loadLibrary("nativemvm");
        try {
            MatlabEngine eng = MatlabEngine.startMatlab();
            eng.close();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }
}