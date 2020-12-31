package eu.quanticol.moonlight.api.example;

import eu.quanticol.moonlight.MoonLightSpatialTemporalScript;
import eu.quanticol.moonlight.SpatialTemporalScriptComponent;
import eu.quanticol.moonlight.formula.BooleanDomain;
import eu.quanticol.moonlight.formula.DoubleDomain;
import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor;
import eu.quanticol.moonlight.signal.DataHandler;
import eu.quanticol.moonlight.signal.MoonLightRecord;
import eu.quanticol.moonlight.signal.RecordHandler;

import java.util.function.Function;

public class TestMoonLightSpatialTemporalScript extends MoonLightSpatialTemporalScript {

    private final static String[] SPATIAL = new String[]{"SpatialMonitor"};

    private DoubleDomain domain = new DoubleDomain();

    //SPATIO-TEMPORAL
    private Function<MoonLightRecord, SpatialTemporalMonitor<MoonLightRecord, MoonLightRecord, Boolean>> spatialBuilder = r -> SpatialTemporalMonitor.atomicMonitor(a -> a.get(0, Boolean.class));

    private SpatialTemporalScriptComponent<?> spatioTemporalMonitor = new SpatialTemporalScriptComponent<Boolean>(SPATIAL[0],
            new RecordHandler(DataHandler.REAL), new RecordHandler(DataHandler.BOOLEAN, DataHandler.INTEGER),
            new BooleanDomain(),
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