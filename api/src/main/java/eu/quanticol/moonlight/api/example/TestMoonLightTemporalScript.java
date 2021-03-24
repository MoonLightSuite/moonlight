package eu.quanticol.moonlight.api.example;

import eu.quanticol.moonlight.MoonLightTemporalScript;
import eu.quanticol.moonlight.TemporalScriptComponent;
import eu.quanticol.moonlight.domain.DoubleDomain;
import eu.quanticol.moonlight.monitoring.temporal.TemporalMonitor;
import eu.quanticol.moonlight.signal.DataHandler;
import eu.quanticol.moonlight.signal.space.MoonLightRecord;
import eu.quanticol.moonlight.signal.RecordHandler;

import java.util.function.Function;

public class TestMoonLightTemporalScript extends MoonLightTemporalScript {

    private final static String[] TEMPORAL = new String[]{"TemporalMonitor"};

    private DoubleDomain domain = new DoubleDomain();

    //TEMPORAL
    private TemporalMonitor<MoonLightRecord, Double> atomicTemporal = TemporalMonitor.atomicMonitor(r -> r.get(0, Double.class) - 10);

    private Function<MoonLightRecord, TemporalMonitor<MoonLightRecord, Double>> temporalBuilder = r ->
            TemporalMonitor.globallyMonitor(atomicTemporal, domain );

    private TemporalScriptComponent<?> temporalMonitor = new TemporalScriptComponent<Double>(TEMPORAL[0],
            new RecordHandler(DataHandler.REAL, DataHandler.REAL),
            domain,
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