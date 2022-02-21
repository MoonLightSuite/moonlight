package eu.quanticol.moonlight.api;

import com.mathworks.engine.EngineException;
import com.mathworks.engine.MatlabEngine;

import java.util.concurrent.ExecutionException;

public class MatlabRunner implements AutoCloseable {
    private final MatlabEngine engine;

    public MatlabRunner(String path) {
        this();
        addPath(path);
    }

    public MatlabRunner() {
        engine = matlabInit();
    }

    public void addPath(String path) {
        eval("addpath(\"" + path + "\")");
    }

    private static MatlabEngine matlabInit()
    {
        try {
            return MatlabEngine.startMatlab();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            throw new UnknownError("Unable to initialize matlab");
        }
    }

    public <T> void putVar(String id, T value) {
        try {
            engine.putVariable(id, value);
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            impossible("Variable creation failed for variable " + id, e);
        }
    }

    public <T> T getVar(String id) {
        try {
            return engine.getVariable(id);
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            impossible("Variable reading failed for variable " + id, e);
            return null;
        }
    }

    public void eval(String s) {
        try {
            engine.eval(s);
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            impossible("Command evaluation failed: " + s, e);
        }
    }

    private void impossible(String s, Exception e) {
        throw new IllegalArgumentException(s, e);
    }

    @Override
    public void close() {
        try {
            engine.close();
        } catch (EngineException e) {
            e.printStackTrace();
            throw new UnknownError("Unable to terminate MATLAB's engine");
        }
    }
}
