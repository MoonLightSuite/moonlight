package eu.quanticol.moonlight.utility.matlab;

import eu.quanticol.moonlight.formula.DoubleDomain;
import eu.quanticol.moonlight.formula.Formula;
import eu.quanticol.moonlight.formula.Parameters;
import eu.quanticol.moonlight.io.FormulaToBreach;
import eu.quanticol.moonlight.io.FormulaToTaliro;
import eu.quanticol.moonlight.monitoring.TemporalMonitoring;
import eu.quanticol.moonlight.signal.Assignment;
import eu.quanticol.moonlight.signal.Signal;
import eu.quanticol.moonlight.signal.SignalCreatorDouble;
import eu.quanticol.moonlight.signal.VariableArraySignal;
import eu.quanticol.moonlight.util.FormulaGenerator;
import eu.quanticol.moonlight.util.FutureFormulaGenerator;
import eu.quanticol.moonlight.utility.matlab.configurator.Matlab;
import org.n52.matlab.control.MatlabInvocationException;
import org.n52.matlab.control.MatlabProxy;
import org.n52.matlab.control.extensions.MatlabNumericArray;
import org.n52.matlab.control.extensions.MatlabTypeConverter;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

public class MatlabProva {
    private static FormulaToTaliro toTaliro = new FormulaToTaliro();
    private static FormulaToBreach toBreach = new FormulaToBreach();
    private static final MatlabProxy eng = Matlab.startMatlab();


    public static void main(String[] args) throws Exception {
        //main3(args);
        //test(14,3);
        //test(15,3);
        test(14, 3);
    }

    public static void mainLoop(String[] args) throws Exception {
        for (int i = 1; i < 10; i++) {
            for (int j = 2; j < 10; j++) {
                System.out.println(i + "," + j + "-->" + test(i, j));
            }
        }
    }

    private static double test(int seed, int formulaLength) throws MatlabInvocationException {
        try {
            Map<String, Function<Double, Double>> functionalMap = new HashMap<>();
            functionalMap.put("a", t -> Math.pow(t, 2.));
            functionalMap.put("b", Math::cos);
            functionalMap.put("c", Math::sin);
            double timeStep = 0.0001;
            SignalCreatorDouble signalCreator = new SignalCreatorDouble(functionalMap);
            double[] time = signalCreator.generateTime(0, 100, timeStep);
            double[][] values = signalCreator.generateValues(time);
            VariableArraySignal signal = signalCreator.generate(0, 100, timeStep);
            FormulaGenerator formulaGenerator = new FutureFormulaGenerator(new Random(seed), signal.getEnd(), signalCreator.getVariableNames());
            Formula generatedFormula = formulaGenerator.getFormula(formulaLength);
            //System.out.println(generatedFormula.toString());
            //System.out.println(toTaliro.toTaliro(generatedFormula));
            //System.out.println(toTaliro.createPrefix(signalCreator));
            eng.setVariable("T", time);
            MatlabTypeConverter processor = new MatlabTypeConverter(eng);
            processor.setNumericArray("M", new MatlabNumericArray(values, null));
            eng.eval("M = transpose(M);");
            eng.eval("T = transpose(T);");
            String taliroFormula = toTaliro.toTaliro(generatedFormula);
            System.out.println(taliroFormula);
            eng.eval(taliroFormula);
            eng.eval(toTaliro.createPrefix(signalCreator));
            eng.eval("taliroRes = taliro(M,T);");
            double[] Z = (double[]) eng.getVariable("taliroRes");
            System.out.println("Taliro Robustness: " + Z[0]);
            int nReps = 20;
            long before = System.currentTimeMillis();
            for (int i = 0; i < nReps; i++) {
                eng.eval("taliroRes = taliro(M,T);");
            }
            long after = System.currentTimeMillis();
            System.out.println("Taliro Avg. Time (msec) (" + nReps + " repetitions): " + (after - before) / 1000.);


            //            //BREACH
            eng.eval("trace = @(X,T)[T M]");
            eng.eval("stringTrace = {'a','b','c'}");
            eng.eval("stringFormulaName = 'phi'");
            String breachFormula = toBreach.toBreach(generatedFormula);
            System.out.println(breachFormula);
            eng.eval("stringFormula ='" + breachFormula + "'");
            eng.eval("robBreach = @(X,T) robEval(stringTrace, trace(X,T),stringFormulaName,stringFormula);");
            before = System.currentTimeMillis();
            for (int i = 0; i < nReps; i++) {
                eng.eval("robRes = robBreach(M,T);");
            }
            after = System.currentTimeMillis();
            Z = (double[]) eng.getVariable("robRes");
            System.out.println("Breach Robustness: " + Z[0]);
            System.out.println("Breach Avg. Time (msec) (" + nReps + " repetitions): " + (after - before) / 1000.);

            HashMap<String, Function<Parameters, Function<Assignment, Double>>> mappa = new HashMap<>();
            //a is the atomic proposition: a>=0
            mappa.put("a", y -> assignment -> assignment.get(0, Double.class));
            mappa.put("b", y -> assignment -> assignment.get(1, Double.class));
            mappa.put("c", y -> assignment -> assignment.get(2, Double.class));
            TemporalMonitoring<Assignment, Double> monitoring = new TemporalMonitoring<>(mappa, new DoubleDomain());
            Function<Signal<Assignment>, Signal<Double>> m = monitoring.monitor(generatedFormula, null);
            before = System.currentTimeMillis();
            for (int i = 0; i < nReps; i++) {
                Signal<Double> outputSignal = m.apply(signal);
                outputSignal.getIterator(true).value();
            }
            after = System.currentTimeMillis();
            Double value = m.apply(signal).getIterator(true).value();
            System.out.println("MoonLight Robustness: " + value);
            System.out.println("MoonLight Avg. Time (msec) (" + nReps + " repetitions): " + (after - before) / 1000.);


            return Math.abs(Z[0] - value);
        } catch (Exception ex) {
            return Double.MAX_VALUE;
        }


    }

}