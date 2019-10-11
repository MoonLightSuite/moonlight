package eu.quanticol.moonlight.matlab;


import com.mathworks.engine.MatlabEngine;

import java.util.concurrent.ExecutionException;

public class EngineMatalbProva {

    public static void main(String args[]) {
            try {
        MatlabEngine eng = MatlabEngine.startMatlab();

        eng.close();
    } catch (ExecutionException | InterruptedException e) {
        e.printStackTrace();
        }
    }
}

