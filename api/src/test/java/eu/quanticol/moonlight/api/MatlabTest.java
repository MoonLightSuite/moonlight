package eu.quanticol.moonlight.api;

import eu.quanticol.moonlight.TemporalScriptComponent;
import eu.quanticol.moonlight.monitoring.temporal.TemporalMonitor;
import eu.quanticol.moonlight.signal.Record;
import eu.quanticol.moonlight.signal.RecordHandler;
import eu.quanticol.moonlight.signal.Signal;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

class MatlabTest {

    @Test
    void name() {
        TestMoonLightScript testMoonLightScript = new TestMoonLightScript();
        TemporalScriptComponent<?> temporalScriptComponent = testMoonLightScript.selectDefaultTemporalComponent();
        TemporalMonitor<Record, ?> monitor = temporalScriptComponent.getMonitor(new Object());
        double[] times = IntStream.range(0, 10).mapToDouble(s -> s).toArray();
        RecordHandler recordHandler = testMoonLightScript.getRecordHandler();

        Signal signal = new Signal();
        Object[][] traj = new Object[times.length][3];
        for (int i = 0; i < traj.length; i++) {
            signal.add(times[i], recordHandler.fromObject(times[i] * 2, times[i] * 2));
        }
        Signal monitor1 = monitor.monitor(signal);
        System.out.println();

    }


}