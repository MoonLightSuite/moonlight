package io.github.moonlightsuite.moonlight.api;


import com.mathworks.engine.MatlabEngine;

import java.util.concurrent.ExecutionException;

public class EngineMatalbProva {

    public static void main(String args[]) {
        System.loadLibrary("nativemvm");
            try {
        MatlabEngine eng = MatlabEngine.startMatlab();

        eng.close();
    } catch (ExecutionException | InterruptedException e) {
        e.printStackTrace();
        }
    }
}
