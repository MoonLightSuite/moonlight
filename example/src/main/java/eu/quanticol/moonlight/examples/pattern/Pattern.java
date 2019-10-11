package eu.quanticol.moonlight.examples.pattern;

import com.mathworks.engine.MatlabEngine;
import eu.quanticol.moonlight.signal.SpatialModel;
import eu.quanticol.moonlight.util.TestUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

public class Pattern {



    public static void main(String[] args) throws InterruptedException, ExecutionException, URISyntaxException {
        URI resource = new URI(Pattern.class.getResource("TuringDataGenerator.m").getPath()).resolve(".");
        String path = resource.getPath();

        SpatialModel<Double> gridModel = TestUtils.createGridModel(32, 32, false, 1.0);
        //occhio alla distance matrix
        MatlabEngine eng = MatlabExecutor.startMatlab();
        eng.eval("addpath(\""+ path+"\")");
        eng.eval("TuringDataGenerator");
        double[][][] Atraj = eng.getVariable("Atraj");
    }

//    public static void main2(String[] args) {
//        GraphModel graph = GraphModel.createGrid(32, 32, 1.0);
//        // Computing of the distance matrix
//        graph.dMcomputation();
//
//        // %%%%%%%%% PROPERTY %%%%%%% //
//
//        // loading the formulas files
//        jSSTLScript script = new jSSTLPatternScript();
//
//
//        // Loading the variables. That we have defined in the formulas files.
//        String [] var = script.getVariables();
//
//        // %%%%%%%%%%%%% Connection with Matlab %%%%%%%%%%%%/////////
//
//
//        Builder optionsBuilder = new Builder();
//        optionsBuilder.setHidden(true);
//        MatlabProxyFactoryOptions options = optionsBuilder.build();
//
//        MatlabProxyFactory factory = new MatlabProxyFactory(options);
//        System.out.print("Connecting to MATLAB... ");
//        MatlabProxy proxy = factory.getProxy();
//        MatlabTypeConverter processor = new MatlabTypeConverter(proxy);
//        System.out.println("Done!");
//
//        proxy.eval("cd matlab;");
//
//
//
//
//        /// Generation of the trace
//        proxy.eval("clear all;");
//        proxy.eval("TuringDataGenerator");
//        //double a = proxy.returningEval("A(1,1)",);
//        /// Loading the  trajectory files.
//        FolderSignalReader readSignal = new FolderSignalReader(graph, var);
//        File folder = new  File("data/allDataPattern");
//        Signal signal = readSignal.read(folder);
//        double[] satB = script.booleanSat("spotformation", new HashMap<String,Double>(), graph, signal);
//        double[] satQ = script.quantitativeSat("spotformation", new HashMap<String,Double>(), graph, signal);
//
//        /// Monitoring
//        //int nprop = 1;
////			String [] formulas ={"spot"};
////			//for ( int i=0 ; i< nprop; i++) {
////			Formula phi = script.getFormula(formulas[0]);
////			SpatialBooleanSignal boolSign = phi.booleanCheck(null, graph, signal);
////			BooleanSignal b = boolSign.spatialBoleanSignal.get(graph.getLocation(0));
////			//double satQ = qt.getValueAt(0);
////			//outputmatrix[i][j]= satQ;
////			System.out.println("all bool value: " + b.toString());
//        //}
//
//
//        System.out.println("Satisfaction value: " + Arrays.toString(satB));
//        System.out.println("Robustness value: " + Arrays.toString(satQ));
//
//
//        // Disconnect Matlab
//        proxy.disconnect();
//
//    }




}