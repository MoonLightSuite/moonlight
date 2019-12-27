package eu.quanticol.moonlight.util;

import eu.quanticol.moonlight.formula.*;
import eu.quanticol.moonlight.io.FormulaToTaliro;
import eu.quanticol.moonlight.io.json.Deserializer;
import eu.quanticol.moonlight.monitoring.TemporalMonitoring;
import eu.quanticol.moonlight.monitoring.temporal.TemporalMonitor;
import eu.quanticol.moonlight.signal.Assignment;
import eu.quanticol.moonlight.signal.AssignmentFactory;
import eu.quanticol.moonlight.signal.Signal;
import eu.quanticol.moonlight.signal.SignalCursor;
import eu.quanticol.moonlight.signal.SignalDataHandler;
import eu.quanticol.moonlight.signal.VariableArraySignal;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Random;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class FormulaGeneratorTest {

    private final FormulaToTaliro toTaliro = new FormulaToTaliro();


    @Test
    void getFutureFormulaLoop() {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        File file = new File(classLoader.getResource("traceIdentity/traceLaura.json").getFile());
        try {
        	AssignmentFactory factory = AssignmentFactory.createFactory(
            		new Pair<String,SignalDataHandler<?>>("a",SignalDataHandler.REAL),
            		new Pair<String,SignalDataHandler<?>>("b",SignalDataHandler.REAL)
            	);
        	String contents = new String(Files.readAllBytes(Paths.get(file.toURI())));
            VariableArraySignal signal = Deserializer.getVariableArraySignalDeserializer(factory).deserialize(contents);
            FormulaGenerator formulaGenerator = new FutureFormulaGenerator(new Random(1), signal.getEnd(), "a");
            Formula generatedFormula = formulaGenerator.getFormula(2);
            System.out.println(generatedFormula.toString());
            System.out.println(toTaliro.toTaliro(generatedFormula));
            long timeInit = System.currentTimeMillis();
            HashMap<String, Function<Parameters, Function<Assignment, Double>>> mappa = new HashMap<>();
            int index_of_x = 0;
            //a is the atomic proposition: a>=0
            mappa.put("a", y -> assignment -> assignment.get(index_of_x, Double.class));
            TemporalMonitoring<Assignment, Double> monitoring = new TemporalMonitoring<>(mappa, new DoubleDomain());
            TemporalMonitor<Assignment, Double> m = monitoring.monitor(generatedFormula, null);
            Signal<Double> outputSignal = m.monitor(signal);
            long timeEnd = System.currentTimeMillis();
            SignalCursor<Assignment> expected = signal.getIterator(true);
            SignalCursor<Double> actual = outputSignal.getIterator(true);
            while (!actual.completed()) {
                assertFalse(expected.completed());
                Double valueActual = actual.value();
                Assignment valueExpected = expected.value();
                // assertEquals(valueExpected.get(0, Double.class), valueActual);
                expected.forward();
                actual.forward();
            }
            System.out.println("TIME MoonLight: " + (timeEnd - timeInit) / 1000.);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void getBothFormulaLoop() {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        File file = new File(classLoader.getResource("traceIdentity/traceLaura.json").getFile());
        try {
        	AssignmentFactory factory = AssignmentFactory.createFactory(
            		new Pair<String,SignalDataHandler<?>>("a",SignalDataHandler.REAL),
            		new Pair<String,SignalDataHandler<?>>("b",SignalDataHandler.REAL)
            	);
        	String contents = new String(Files.readAllBytes(Paths.get(file.toURI())));
            VariableArraySignal signal = Deserializer.getVariableArraySignalDeserializer(factory).deserialize(contents);
            FormulaGenerator formulaGenerator = new BothFormulaGenerator(new Random(1), signal.getEnd(), "a");
            Formula generatedFormula = formulaGenerator.getFormula(2);
            System.out.println(generatedFormula.toString());
            //System.out.println(toTaliro.toTaliro( generatedFormula ));
            long timeInit = System.currentTimeMillis();
            HashMap<String, Function<Parameters, Function<Assignment, Double>>> mappa = new HashMap<>();
            int index_of_x = 0;
            //a is the atomic proposition: a>=0
            mappa.put("a", y -> assignment -> assignment.get(index_of_x, Double.class));
            TemporalMonitoring<Assignment, Double> monitoring = new TemporalMonitoring<>(mappa, new DoubleDomain());
            TemporalMonitor<Assignment, Double> m = monitoring.monitor(generatedFormula, null);
            Signal<Double> outputSignal = m.monitor(signal);
            long timeEnd = System.currentTimeMillis();
            SignalCursor<Assignment> expected = signal.getIterator(true);
            SignalCursor<Double> actual = outputSignal.getIterator(true);
            while (!actual.completed()) {
                assertFalse(expected.completed());
                Double valueActual = actual.value();
                Assignment valueExpected = expected.value();
                // assertEquals(valueExpected.get(0, Double.class), valueActual);
                expected.forward();
                actual.forward();
            }
            System.out.println("TIME MoonLight: " + (timeEnd - timeInit) / 1000.);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testRobustnessLaura3() {
        //FORMULA: !<>_[0,500]!(a>=0)
        //TALIRO: //
        //BREACH: //
        Formula a = new AtomicFormula("a");
//        Formula notA = new NegationFormula(a);
        Formula eventually = new EventuallyFormula(a, new Interval(0, 1500));
//        Formula notEventuallyNotA = new NegationFormula(eventually);
        //signal
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        File file = new File(classLoader.getResource("traceIdentity/traceLaura.json").getFile());
        try {
        	AssignmentFactory factory = AssignmentFactory.createFactory(
            		new Pair<String,SignalDataHandler<?>>("a",SignalDataHandler.REAL),
            		new Pair<String,SignalDataHandler<?>>("b",SignalDataHandler.REAL)
            	);
        	String contents = new String(Files.readAllBytes(Paths.get(file.toURI())));
            VariableArraySignal signal = Deserializer.getVariableArraySignalDeserializer(factory).deserialize(contents);
            HashMap<String, Function<Parameters, Function<Assignment, Double>>> mappa = new HashMap<>();
            int index_of_x = 0;
            //a is the atomic proposition: a>=0
            mappa.put("a", y -> assignment -> assignment.get(index_of_x, Double.class));
            TemporalMonitoring<Assignment, Double> monitoring = new TemporalMonitoring<>(mappa, new DoubleDomain());
            TemporalMonitor<Assignment, Double> m = monitoring.monitor(eventually, null);
            Signal<Double> outputSignal = m.monitor(signal);
            SignalCursor<Assignment> expected = signal.getIterator(true);
            SignalCursor<Double> actual = outputSignal.getIterator(true);
            //assertTrue(outputSignal.end()==500.0);
            System.out.println(outputSignal.end());
            while (!actual.completed()) {
                assertFalse(expected.completed());
                Double nextActual = actual.value();
                Assignment nextExpected = expected.value();
                double time = expected.time();
//                if (time > 500) {
//                    break;
//                }
                assertEquals(nextExpected.get(0, Double.class), nextActual, "Time: " + time);
                actual.forward();
                expected.forward();
            }
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

}