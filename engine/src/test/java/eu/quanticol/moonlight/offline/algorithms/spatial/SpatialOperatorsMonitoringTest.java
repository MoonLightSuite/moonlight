package eu.quanticol.moonlight.offline.algorithms.spatial;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class SpatialOperatorsMonitoringTest {
    @Disabled
    @Test
    void testReach() {
        var size = 1920 * 1080; // 2073600
        var x = 0;
        for (int i = 0; i < size; i++) {
            // System.out.println("i = " + i);
            x += i * 2;
        }
    }

}
