package eu.quanticol.moonlight.offline.algorithms.temporal;

import eu.quanticol.moonlight.core.formula.Interval;
import eu.quanticol.moonlight.core.signal.SignalDomain;
import eu.quanticol.moonlight.domain.DoubleDomain;
import eu.quanticol.moonlight.domain.IntegerDomain;
import eu.quanticol.moonlight.offline.algorithms.ReduceOp;
import eu.quanticol.moonlight.offline.signal.Signal;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static eu.quanticol.moonlight.TestUtils.listOf;
import static eu.quanticol.moonlight.offline.TestSignalUtils.*;
import static eu.quanticol.moonlight.offline.algorithms.TemporalOp.computeFutureSignal;
import static eu.quanticol.moonlight.util.SignalGenerator.createSignal;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TemporalOpTest {

    @Test
    void reduceWorksOnTheTrivialSystem() {
        SignalDomain<Double> domain =  new DoubleDomain();
        var s = basicTemporalSignal();

        var x = computeFutureSignal(s, null, domain::conjunction, domain.max());
        x.forEach((t, v) -> System.out.print("(t:" + t + ", value:" + v + ") > "));
        System.out.println("");

    }



}
