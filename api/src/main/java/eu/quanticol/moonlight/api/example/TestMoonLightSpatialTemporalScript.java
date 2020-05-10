package eu.quanticol.moonlight.api.example;

import eu.quanticol.moonlight.MoonLightScript;
import eu.quanticol.moonlight.MoonLightSpatialTemporalScript;
import eu.quanticol.moonlight.SpatialTemporalScriptComponent;
import eu.quanticol.moonlight.TemporalScriptComponent;
import eu.quanticol.moonlight.formula.DoubleDomain;
import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor;
import eu.quanticol.moonlight.monitoring.temporal.TemporalMonitor;
import eu.quanticol.moonlight.signal.DataHandler;
import eu.quanticol.moonlight.signal.Record;
import eu.quanticol.moonlight.signal.RecordHandler;

import java.util.function.Function;

public class TestMoonLightSpatialTemporalScript extends MoonLightSpatialTemporalScript {

    private final static String[] SPATIAL = new String[]{"SpatialMonitor"};

    private DoubleDomain domain = new DoubleDomain();

    //SPATIO-TEMPORAL
    private Function<Record, SpatialTemporalMonitor<Record, Record, Boolean>> spatialBuilder = r -> SpatialTemporalMonitor.atomicMonitor(a -> a.get(0, Boolean.class));

    private SpatialTemporalScriptComponent<?> spatioTemporalMonitor = new SpatialTemporalScriptComponent<>(SPATIAL[0],
            new RecordHandler(DataHandler.REAL), new RecordHandler(DataHandler.BOOLEAN, DataHandler.INTEGER),
            DataHandler.BOOLEAN,
            spatialBuilder);


    public TestMoonLightSpatialTemporalScript() {
        super(SPATIAL);
    }

    @Override
    public SpatialTemporalScriptComponent<?> selectSpatialTemporalComponent(String name) {
        if (SPATIAL[0].equals(name)) {
            return selectDefaultSpatialTemporalComponent();
        }
        return null;
    }

    @Override
    public SpatialTemporalScriptComponent<?> selectDefaultSpatialTemporalComponent() {
        return spatioTemporalMonitor;
    }

}