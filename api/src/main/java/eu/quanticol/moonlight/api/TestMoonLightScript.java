/**
 * 
 */
package eu.quanticol.moonlight.api;

import java.util.function.Function;

import eu.quanticol.moonlight.MoonLightScript;
import eu.quanticol.moonlight.SpatioTemporalScriptComponent;
import eu.quanticol.moonlight.TemporalScriptComponent;
import eu.quanticol.moonlight.formula.DoubleDomain;
import eu.quanticol.moonlight.monitoring.temporal.TemporalMonitor;
import eu.quanticol.moonlight.signal.DataHandler;
import eu.quanticol.moonlight.signal.Record;
import eu.quanticol.moonlight.signal.RecordHandler;

public class TestMoonLightScript extends MoonLightScript {
	
	private final static String[] TEMPORAL = new String[]  { "TemporalMonitor" };
	private final static String[] SPATIAL = new String[]  { };
	
	private DoubleDomain domain = new DoubleDomain();
	
	private TemporalMonitor<Record,Double> atomicTemporal = TemporalMonitor.atomicMonitor(r -> r.get(1, Double.class)-10);
	
	private Function<Record,TemporalMonitor<Record,Double>> temporalBuilder = r -> 
			TemporalMonitor.globallyMonitor(atomicTemporal, domain::conjunction, domain.max());

	private TemporalScriptComponent<?> temporalMonitor = new TemporalScriptComponent<Double>( TEMPORAL[0],
			new RecordHandler(DataHandler.REAL),
			DataHandler.REAL,
			temporalBuilder);

	public RecordHandler getRecordHandler(){
		return new RecordHandler(DataHandler.REAL,DataHandler.REAL);

	}
	public TestMoonLightScript() {
		super(TEMPORAL,SPATIAL);
	}

	@Override
	public TemporalScriptComponent<?> selectTemporalComponent(String name) {
		if (TEMPORAL[0].equals(name)) {
			return selectDefaultTemporalComponent();
		}
		return null;
	}

	@Override
	public SpatioTemporalScriptComponent<?> selectSpatioTemporalComponent(String name) {
		return null;
	}

	@Override
	public TemporalScriptComponent<?> selectDefaultTemporalComponent() {
		return temporalMonitor;
	}

	@Override
	public SpatioTemporalScriptComponent<?> selectDefaultSpatioTemporalComponent() {
		return null;
	}

}