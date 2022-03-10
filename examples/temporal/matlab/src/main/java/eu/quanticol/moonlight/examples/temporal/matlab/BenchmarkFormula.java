package eu.quanticol.moonlight.examples.temporal.matlab;

import eu.quanticol.moonlight.core.formula.Formula;
import eu.quanticol.moonlight.formula.*;
import eu.quanticol.moonlight.formula.temporal.GloballyFormula;
import eu.quanticol.moonlight.io.FormulaToBreach;
import eu.quanticol.moonlight.io.FormulaToTaliro;
import eu.quanticol.moonlight.offline.monitoring.TemporalMonitoring;
import eu.quanticol.moonlight.offline.monitoring.temporal.TemporalMonitor;
import eu.quanticol.moonlight.core.base.MoonLightRecord;
import eu.quanticol.moonlight.offline.signal.RecordHandler;
import eu.quanticol.moonlight.offline.signal.Signal;
import eu.quanticol.moonlight.offline.signal.SignalCreator;
import eu.quanticol.moonlight.core.base.DataHandler;
import eu.quanticol.moonlight.offline.signal.VariableArraySignal;
import eu.quanticol.moonlight.domain.DoubleDomain;
import eu.quanticol.moonlight.core.formula.Interval;
import eu.quanticol.moonlight.util.Pair;
import eu.quanticol.moonlight.utility.matlab.configurator.Matlab;
import org.n52.matlab.control.MatlabInvocationException;
import org.n52.matlab.control.MatlabProxy;
import org.n52.matlab.control.extensions.MatlabNumericArray;
import org.n52.matlab.control.extensions.MatlabTypeConverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class BenchmarkFormula {
    private static FormulaToTaliro toTaliro = new FormulaToTaliro();
    private static FormulaToBreach toBreach = new FormulaToBreach();
    private static final MatlabProxy eng = Matlab.startMatlab();


    public static void main(String[] args) throws Exception {
        //main3(args);
        //test(14,3);
        //test(15,3);
        test(14, 3);
        eng.disconnect();
    }

    public static void mainLoop(String[] args) throws Exception {
        for (int i = 1; i < 10; i++) {
            for (int j = 2; j < 10; j++) {
                System.out.println(i + "," + j + "-->" + test(i, j));
            }
        }
    }

	public static double[][] generateValues(double[] time, ArrayList<Function<Double,Double>> functions) {
	  double[][] values = new double[functions.size()][time.length];
	  for (int i = 0; i < time.length; i++) {
	      for (int j = 0; j < functions.size(); j++) {
	          Function<Double, Double> f = functions.get(j);
	          values[j][i] = f.apply(time[i]);
	      }
	  }
	  return values;
	}    
    
    private static double test(int seed, int formulaLength) throws MatlabInvocationException {
        try {
            Map<String, Function<Double, ?>> functionalMap = new HashMap<>();
            functionalMap.put("a", t -> Math.pow(t, 2.));
            functionalMap.put("b", Math::cos);
            functionalMap.put("c", Math::sin);
            ArrayList<Function<Double,Double>> functions = new ArrayList<>();
            functions.add(t -> Math.pow(t, 2.));
            functions.add(Math::cos);
            functions.add(Math::sin);
            
            double timeStep = 0.0001;
            RecordHandler factory = RecordHandler.createFactory(
            		new Pair<>("a",DataHandler.REAL),
            		new Pair<>("b",DataHandler.REAL),
            		new Pair<>("c",DataHandler.REAL)
            );
            SignalCreator signalCreator = new SignalCreator(factory,functionalMap);
            double[] time = signalCreator.generateTime(0, 500, timeStep);
            double[][] values = generateValues(time,functions);
            VariableArraySignal signal = signalCreator.generate(0, 100, timeStep);
            //name : "AbsentAQ10"
            //pattern : "historically((once[:10](q)) -> ((not p) since q))"
            Formula a = new AtomicFormula("a");
            Formula phi = new GloballyFormula(a, new Interval(0, 500));
            //System.out.println(generatedFormula.toString());
            //System.out.println(toTaliro.toTaliro(generatedFormula));
            //System.out.println(toTaliro.createPrefix(signalCreator));
            eng.setVariable("T", time);
            MatlabTypeConverter processor = new MatlabTypeConverter(eng);
            processor.setNumericArray("M", new MatlabNumericArray(values, null));
            eng.eval("M = transpose(M);");
            eng.eval("T = transpose(T);");
            String taliroFormula = toTaliro.toTaliro(phi);
            System.out.println(taliroFormula);
            eng.eval(taliroFormula);
            eng.eval(toTaliro.createPrefix(factory.getVariableIndex()));
            eng.eval("taliroRes = taliro(M,T);");
            double[] Z = (double[]) eng.getVariable("taliroRes");
            System.out.println("Taliro Robustness: " + Z[0]);
            int nReps = 1;
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
            String breachFormula = toBreach.toBreach(phi);
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

            HashMap<String, Function<Parameters, Function<MoonLightRecord, Double>>> mappa = new HashMap<>();
            //a is the atomic proposition: a>=0
            mappa.put("a", y -> assignment -> assignment.get(0, Double.class));
            mappa.put("b", y -> assignment -> assignment.get(1, Double.class));
            mappa.put("c", y -> assignment -> assignment.get(2, Double.class));
            TemporalMonitoring<MoonLightRecord, Double> monitoring = new TemporalMonitoring<>(mappa, new DoubleDomain());
            TemporalMonitor<MoonLightRecord, Double> m = monitoring.monitor(phi, null);
            before = System.currentTimeMillis();
            for (int i = 0; i < nReps; i++) {
                Signal<Double> outputSignal = m.monitor(signal);
                outputSignal.getIterator(true).value();
            }
            after = System.currentTimeMillis();
            Double value = m.monitor(signal).getIterator(true).value();
            System.out.println("MoonLight Robustness: " + value);
            System.out.println("MoonLight Avg. Time (msec) (" + nReps + " repetitions): " + (after - before) / 1000.);


            return Math.abs(Z[0] - value);
        } catch (Exception ex) {
            return Double.MAX_VALUE;
        }


    }

}