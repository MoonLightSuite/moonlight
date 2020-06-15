package eu.quanticol.moonlight;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

import eu.quanticol.moonlight.compiler.MoonlightCompiler;
import org.junit.jupiter.api.Test;

import eu.quanticol.moonlight.signal.DataHandler;
import eu.quanticol.moonlight.signal.GraphModel;
import eu.quanticol.moonlight.signal.LocationService;
import eu.quanticol.moonlight.signal.LocationServiceList;
import eu.quanticol.moonlight.signal.Record;
import eu.quanticol.moonlight.signal.RecordHandler;
import eu.quanticol.moonlight.signal.SpatialModel;
import eu.quanticol.moonlight.signal.SpatialTemporalSignal;
import eu.quanticol.moonlight.xtext.ScriptLoader;

class TestSensorScript {
	
	private static RecordHandler edgeRecordHandler = new RecordHandler(DataHandler.INTEGER);
	private static RecordHandler signalRecordHandkler = new RecordHandler(DataHandler.INTEGER);
	
	
	private static String code = "signal { int nodeType; }\n" +
			"             	space {\n" + 
			"             	edges { int hop;}\n" + 
			"             	}\n" + 
			"             	domain boolean;\n" + 
			"             	formula aFormula = somewhere [0, 1] ( nodeType==1 );";

	private static String code2 = "signal { int nodeType; }\n" +
			"space {\n" +
			"    edges { int hop; }\n" +
			"}\n" +
			"domain boolean;\n" +
			"formula MyFirstFormula = ( nodeType==3 ) reach(hop) [0, 1] ( nodeType==1 );";


	@Test
	void test() throws IOException {		
		ScriptLoader sl = new ScriptLoader();
		MoonLightScript script = sl.compileScript(code);
		assertTrue( script.isSpatialTemporal() );
		MoonLightSpatialTemporalScript spatialTemporalScript = script.spatialTemporal();
		SpatialTemporalScriptComponent<?> stc = spatialTemporalScript.selectDefaultSpatialTemporalComponent();
        List<Integer> typeNode = Arrays.asList( 1, 3, 3, 3, 3);
        SpatialTemporalSignal<Record> signal = createSpatioTemporalSignal(typeNode.size(), 0, 1, 20.0,
                (t, l) -> signalRecordHandkler.fromObjectArray(typeNode.get(l)));
        SpatialTemporalSignal<?> res = stc.monitorFromDouble(createLocService(0.0, 1, 20.0, getGraphModel()), signal);
        assertEquals(true, res.getSignals().get(0).valueAt(0.0));        
        double[][][] oArray = stc.monitorToArrayFromDouble(createLocService(0.0, 1, 20.0, getGraphModel()), signal);
        assertEquals(1.0, oArray[0][0][1]);
//		stc.monitorToObjectArray(graph, signalTimeArray, signalValues, parameters)
	}

	@Test
	void test2() throws IOException {
		ScriptLoader sl = new ScriptLoader();
		MoonLightScript script = sl.compileScript(code2);
		assertTrue( script.isSpatialTemporal() );
		MoonLightSpatialTemporalScript spatialTemporalScript = script.spatialTemporal();
		SpatialTemporalScriptComponent<?> stc = spatialTemporalScript.selectDefaultSpatialTemporalComponent();
		double[] locationTimeArray = new double[] { 0.0 };
		double[][][] graph = new double[][][] {
				new double[][] {
						new double[]{ 0, 1, 1.0 },
						new double[]{ 0, 3, 1.0 },
						new double[]{ 0, 4, 1.0 },
						new double[]{ 1, 0, 1.0 },
						new double[]{ 1, 4, 1.0 },
						new double[]{ 1, 2, 1.0 },
						new double[]{ 2, 1, 1.0 },
						new double[]{ 2, 4, 1.0 },
						new double[]{ 2, 3, 1.0 },
						new double[]{ 3, 0, 1.0 },
						new double[]{ 3, 2, 1.0 },
						new double[]{ 3, 4, 1.0 },
						new double[]{ 4, 0, 1.0 },
						new double[]{ 4, 1, 1.0 },
						new double[]{ 4, 2, 1.0 },
						new double[]{ 4, 3, 1.0 }
				}
		};

		double[][][] signal = new double[][][] {
				new double[][]{
						new double[]{1}
				} ,
				new double[][] {
						new double[]{3}
				}  ,
				new double[][] {
						new double[]{3}
				}  ,
				new double[][] {
						new double[]{3}
				}  ,
				new double[][]{
						new double[]{3}
				}
		};

//		List<Integer> typeNode = Arrays.asList( 1, 3, 3, 3, 3);
//		SpatialTemporalSignal<Record> signal = createSpatioTemporalSignal(typeNode.size(), 0, 1, 1.0,
//		(t, l) -> signalRecordHandkler.fromObjectArray(typeNode.get(l)));

//		double[][][] oArray = stc.monitorToArrayFromDouble(createLocService(0.0, 1, 1.0, getGraphModel2()), signal);
		double[][][] oArray = stc.monitorToObjectArrayAdjacencyList(locationTimeArray,graph,locationTimeArray,signal);
		System.out.println(oArray[0][0][1]);
		System.out.println(oArray[1][0][1]);
		System.out.println(oArray[2][0][1]);
		System.out.println(oArray[3][0][1]);
		System.out.println(oArray[4][0][1]);
//		assertEquals(1.0, oArray[0][0][1]);
//		stc.monitorToObjectArray(graph, signalTimeArray, signalValues, parameters)
	}
	
	private LocationService<Record> createLocService(double start, double dt, double end,SpatialModel<Record> graph) {
        LocationServiceList<Record> locService = new LocationServiceList<Record>();
        double time = start;
        while (time < end) {
            double current = time;
            locService.add(time, graph);
            time += dt;
        }
        locService.add(end,graph);
        return locService;
    }
	
	private SpatialModel<Record> getGraphModel() { //metto alla fine tutti i metodi privati di servizio.
		GraphModel<Record> m = new GraphModel<>(5);
		m.add(0, edgeRecordHandler.fromDoubleArray(1.0), 2);
		m.add(0, edgeRecordHandler.fromDoubleArray(1.0), 4);
		m.add(1, edgeRecordHandler.fromDoubleArray(1.0), 2);
		m.add(1, edgeRecordHandler.fromDoubleArray(1.0), 3);
		m.add(1, edgeRecordHandler.fromDoubleArray(1.0), 4);
		m.add(2, edgeRecordHandler.fromDoubleArray(1.0), 0);
		m.add(2, edgeRecordHandler.fromDoubleArray(1.0), 1);
		m.add(2, edgeRecordHandler.fromDoubleArray(1.0), 3);
		m.add(2, edgeRecordHandler.fromDoubleArray(1.0), 4);
		m.add(3, edgeRecordHandler.fromDoubleArray(1.0), 1);
		m.add(3, edgeRecordHandler.fromDoubleArray(1.0), 2);
		m.add(4, edgeRecordHandler.fromDoubleArray(1.0), 0);
		m.add(4, edgeRecordHandler.fromDoubleArray(1.0), 1);
		m.add(4, edgeRecordHandler.fromDoubleArray(1.0), 2);
		return m;
    }

	private SpatialModel<Record> getGraphModel2() { //metto alla fine tutti i metodi privati di servizio.
		GraphModel<Record> m = new GraphModel<>(5);
		m.add(0, edgeRecordHandler.fromDoubleArray(1.0), 1);
		m.add(0, edgeRecordHandler.fromDoubleArray(1.0), 3);
		m.add(0, edgeRecordHandler.fromDoubleArray(1.0), 4);
		m.add(1, edgeRecordHandler.fromDoubleArray(1.0), 0);
		m.add(1, edgeRecordHandler.fromDoubleArray(1.0), 2);
		m.add(1, edgeRecordHandler.fromDoubleArray(1.0), 4);
		m.add(2, edgeRecordHandler.fromDoubleArray(1.0), 1);
		m.add(2, edgeRecordHandler.fromDoubleArray(1.0), 3);
		m.add(2, edgeRecordHandler.fromDoubleArray(1.0), 4);
		m.add(3, edgeRecordHandler.fromDoubleArray(1.0), 0);
		m.add(3, edgeRecordHandler.fromDoubleArray(1.0), 2);
		m.add(3, edgeRecordHandler.fromDoubleArray(1.0), 4);
		m.add(4, edgeRecordHandler.fromDoubleArray(1.0), 0);
		m.add(4, edgeRecordHandler.fromDoubleArray(1.0), 1);
		m.add(4, edgeRecordHandler.fromDoubleArray(1.0), 2);
		m.add(4, edgeRecordHandler.fromDoubleArray(1.0), 3);

		return m;
	}

	private static <T> SpatialTemporalSignal<T> createSpatioTemporalSignal(int size, double start, double dt, double end, BiFunction<Double, Integer, T> f) {
        SpatialTemporalSignal<T> s = new SpatialTemporalSignal(size);

        for(double time = start; time < end; time += dt) {
            double finalTime = time;
            s.add(time, (i) -> {
                return f.apply(finalTime, i);
            });
        }

        s.add(end, (i) -> {
            return f.apply(end, i);
        });
        return s;
    }

	
}
