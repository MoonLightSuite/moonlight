package eu.quanticol.moonlight.util;

import eu.quanticol.moonlight.formula.DoubleDomain;
import eu.quanticol.moonlight.formula.Formula;
import eu.quanticol.moonlight.formula.Parameters;
import eu.quanticol.moonlight.io.JSonSignalReader;
import eu.quanticol.moonlight.monitoring.TemporalMonitoring;
import eu.quanticol.moonlight.signal.Assignment;
import eu.quanticol.moonlight.signal.Signal;
import eu.quanticol.moonlight.signal.SignalCursor;
import eu.quanticol.moonlight.signal.VariableArraySignal;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Random;
import java.util.function.Function;

import static org.junit.Assert.*;

public class FormulaGeneratorTest {

    @Test
    public void getFutureFormula() {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        File file = new File(classLoader.getResource("traceIdentity/traceLaura.json").getFile());
        try {
            String contents = new String(Files.readAllBytes(Paths.get(file.toURI())));
            VariableArraySignal signal = JSonSignalReader.readSignal(contents);
            FormulaGenerator formulaGenerator = new FutureFormulaGenerator(new Random(2),signal.getEnd()/2,"a");
            Formula generatedFormula = formulaGenerator.getFormula(2);
            System.out.println(generatedFormula.toString());
            System.out.println(generatedFormula.toTaliro());
            long timeInit = System.currentTimeMillis();
            HashMap<String, Function<Parameters, Function<Assignment, Double>>> mappa = new HashMap<>();
            int index_of_x = 0;
            //a is the atomic proposition: a>=0
            mappa.put("a", y -> assignment -> assignment.get(index_of_x, Double.class));
            TemporalMonitoring<Assignment, Double> monitoring = new TemporalMonitoring<>(mappa, new DoubleDomain());
            Function<Signal<Assignment>, Signal<Double>> m = monitoring.monitor(generatedFormula, null);
            Signal<Double> outputSignal = m.apply(signal);
            long timeEnd = System.currentTimeMillis();
            SignalCursor<Assignment> expected = signal.getIterator(true);
            SignalCursor<Double> actual = outputSignal.getIterator(true);
//            while (!actual.completed()) {
//                assertFalse(expected.completed());
//                Double valueActual = actual.value();
//                Assignment valueExpected = expected.value();
//                assertEquals(valueExpected.get(0, Double.class), valueActual);
//                expected.forward();
//                actual.forward();
//            }
            System.out.println("TIME MoonLight: " +(timeEnd-timeInit)/1000.);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }
}