package eu.quanticol.moonlight.utility.matlab;

import com.mathworks.engine.MatlabEngine;

import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class MatlabRunner {
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

    public void executeAndDie(Consumer<MatlabEngine> f) {
        try {
            f.accept(engine);
            engine.close();
        } catch (ExecutionException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            throw new UnknownError("Unable to execute " + f +"in matlab");
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

    public MatlabEngine getEngine() {
        return engine;
    }

    private void impossible(String s, Exception e) {
        throw new IllegalArgumentException(s, e);
    }
}
