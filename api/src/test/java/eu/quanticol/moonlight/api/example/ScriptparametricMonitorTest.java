package eu.quanticol.moonlight.api.example;

import eu.quanticol.moonlight.TemporalScriptComponent;
import eu.quanticol.moonlight.monitoring.temporal.TemporalMonitor;
import eu.quanticol.moonlight.signal.Record;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class ScriptparametricMonitorTest {

    @Test
    void failTest() {
        ScriptparametricMonitor scriptparametricMonitor = new ScriptparametricMonitor();
        TemporalScriptComponent<?> booleanMonitorScript = scriptparametricMonitor.selectTemporalComponent("BooleanMonitorScript");
        double[] times = IntStream.range(0, 63).mapToDouble(t -> t * 0.10).toArray();
        String[][] value = new String[63][2];
        for (int i = 0; i < value.length; i++) {
            value[i]=new String[]{String.valueOf(Math.sin(times[i])), String.valueOf(Math.cos(times[i]))};
        }

        Object[][] objects = booleanMonitorScript.monitorToObjectArray(times, value, new String[]{"0", "4"});
        System.out.println();
    }
}