package eu.quanticol.moonlight.api.example;

import eu.quanticol.moonlight.MoonLightScript;
import eu.quanticol.moonlight.MoonLightTemporalScript;
import eu.quanticol.moonlight.SpatialTemporalScriptComponent;
import eu.quanticol.moonlight.TemporalScriptComponent;
import eu.quanticol.moonlight.formula.DoubleDomain;
import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor;
import eu.quanticol.moonlight.monitoring.temporal.TemporalMonitor;
import eu.quanticol.moonlight.signal.DataHandler;
import eu.quanticol.moonlight.signal.Record;
import eu.quanticol.moonlight.signal.RecordHandler;

import java.util.function.Function;

public class TestMoonLightTemporalScript extends MoonLightTemporalScript {

    private final static String[] TEMPORAL = new String[]{"TemporalMonitor"};

    private DoubleDomain domain = new DoubleDomain();

    //TEMPORAL
    private TemporalMonitor<Record, Double> atomicTemporal = TemporalMonitor.atomicMonitor(r -> r.get(0, Double.class) - 10);

    private Function<Record, TemporalMonitor<Record, Double>> temporalBuilder = r ->
            TemporalMonitor.globallyMonitor(atomicTemporal, domain );

    private TemporalScriptComponent<?> temporalMonitor = new TemporalScriptComponent<Double>(TEMPORAL[0],
            new RecordHandler(DataHandler.REAL, DataHandler.REAL),
            DataHandler.REAL,
            temporalBuilder);

    public TestMoonLightTemporalScript() {
        super(TEMPORAL);
    }

    @Override
    public TemporalScriptComponent<?> selectTemporalComponent(String name) {
        if (TEMPORAL[0].equals(name)) {
            return selectDefaultTemporalComponent();
        }
        return null;
    }


    @Override
    public TemporalScriptComponent<?> selectDefaultTemporalComponent() {
        return temporalMonitor;
    }

}