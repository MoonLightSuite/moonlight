package eu.quanticol.moonlight.utility.matlab;

import com.mathworks.engine.EngineException;
import com.mathworks.engine.MatlabEngine;

public class MatlabExecutor {

    private static MatlabEngine engine;

    public static MatlabEngine startMatlab() throws InterruptedException, EngineException {
        if(engine==null){
            engine = MatlabEngine.startMatlab();
        }
        return engine;
    }

    public static void getClear() throws EngineException {
        engine.close();
        engine=null;
    }

    public static void close() throws EngineException {
        engine.close();
        engine=null;
    }

}
