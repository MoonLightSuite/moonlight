package eu.quanticol.moonlight.offline.algorithms.temporal;

import eu.quanticol.moonlight.core.signal.SignalDomain;
import eu.quanticol.moonlight.domain.DoubleDomain;
import eu.quanticol.moonlight.offline.signal.Signal;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static eu.quanticol.moonlight.offline.TestSignalUtils.*;
import static eu.quanticol.moonlight.offline.algorithms.TemporalOp.computeFutureSignal;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class TemporalOpTest {

    @Test
    void unboundedGloballyWorks() {
        SignalDomain<Double> domain =  new DoubleDomain();
        var s = basicTemporalSignal();

        System.out.println("Input signal");
        printSignal(s);

        var x = computeFutureSignal(s, null, domain::conjunction, domain.max());
        System.out.println("Output signal");
        printSignal(x);
        assertSameTimes(new double[] {0.0, 10.0}, x.getTimeSet());
        assertArrayEquals(new double[] {0.0, 0.0}, new double[] {x.getValueAt(0.0), x.getValueAt(10.0)});
        // expected: (t:0.0, value:0.0) > (t:10.0, value:0.0)
    }


    static void printSignal(Signal<?> signal) {
        signal.forEach((t, v) -> System.out.print("(t:" + t + ", value:" + v + ") > "));
        System.out.println();
    }

    private static void assertSameTimes(double[] expected, Set<Double> actual) {
        assertArrayEquals(expected, actual.stream().mapToDouble(Double::doubleValue).toArray());
    }
}
