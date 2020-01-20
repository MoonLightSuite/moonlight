package eu.quanticol.moonlight.api;

import eu.quanticol.moonlight.MoonLightScript;
import eu.quanticol.moonlight.SpatioTemporalScriptComponent;
import eu.quanticol.moonlight.TemporalScriptComponent;
import eu.quanticol.moonlight.formula.DoubleDomain;
import eu.quanticol.moonlight.monitoring.spatiotemporal.SpatioTemporalMonitor;
import eu.quanticol.moonlight.monitoring.temporal.TemporalMonitor;
import eu.quanticol.moonlight.signal.DataHandler;
import eu.quanticol.moonlight.signal.Record;
import eu.quanticol.moonlight.signal.RecordHandler;

import java.util.function.Function;

public class TestMoonLightScript extends MoonLightScript {

    private final static String[] TEMPORAL = new String[]{"TemporalMonitor"};
    private final static String[] SPATIAL = new String[]{"SpatialMonitor"};

    private DoubleDomain domain = new DoubleDomain();

    //TEMPORAL
    private TemporalMonitor<Record, Double> atomicTemporal = TemporalMonitor.atomicMonitor(r -> r.get(0, Double.class) - 10);

    private Function<Record, TemporalMonitor<Record, Double>> temporalBuilder = r ->
            TemporalMonitor.globallyMonitor(atomicTemporal, domain );

    private TemporalScriptComponent<?> temporalMonitor = new TemporalScriptComponent<Double>(TEMPORAL[0],
            new RecordHandler(DataHandler.REAL, DataHandler.REAL),
            DataHandler.REAL,
            temporalBuilder);

    //SPATIO-TEMPORAL
    private Function<Record, SpatioTemporalMonitor<Record, Record, Boolean>> spatialBuilder = r -> SpatioTemporalMonitor.atomicMonitor(a -> a.get(0, Boolean.class));

    private SpatioTemporalScriptComponent<?> spatioTemporalMonitor = new SpatioTemporalScriptComponent<>(SPATIAL[0],
            new RecordHandler(DataHandler.REAL), new RecordHandler(DataHandler.BOOLEAN, DataHandler.INTEGER),
            DataHandler.BOOLEAN,
            spatialBuilder);


    public TestMoonLightScript() {
        super(TEMPORAL, SPATIAL);
    }

    @Override
    public TemporalScriptComponent<?> selectTemporalComponent(String name) {
        if (TEMPORAL[0].equals(name)) {
            return selectDefaultTemporalComponent();
        }
        return null;
    }

    @Override
    public SpatioTemporalScriptComponent<?> selectSpatioTemporalComponent(String name) {
        if (SPATIAL[0].equals(name)) {
            return selectDefaultSpatioTemporalComponent();
        }
        return null;
    }

    @Override
    public TemporalScriptComponent<?> selectDefaultTemporalComponent() {
        return temporalMonitor;
    }

    @Override
    public SpatioTemporalScriptComponent<?> selectDefaultSpatioTemporalComponent() {
        return spatioTemporalMonitor;
    }

}