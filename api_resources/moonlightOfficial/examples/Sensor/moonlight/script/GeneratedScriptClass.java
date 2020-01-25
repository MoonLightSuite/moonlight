/**
 * Code Generate by MoonLight tool.
 */ 
package moonlight.script;

import eu.quanticol.moonlight.*;
import eu.quanticol.moonlight.monitoring.temporal.*;
import eu.quanticol.moonlight.monitoring.spatiotemporal.*;
import eu.quanticol.moonlight.signal.*;
import eu.quanticol.moonlight.util.*;
import eu.quanticol.moonlight.*;
import eu.quanticol.moonlight.formula.*;
import java.util.function.Function;
import java.util.HashSet;
import java.util.HashMap;


public class GeneratedScriptClass extends MoonLightScript {			
	
				
	private final SignalDomain<Double> _domain_SensorNetwork = new DoubleDomain();
	
	private SpatioTemporalMonitor<Record,Record,Double> SensorNetwork_main( Record parameters ) {
		return SpatioTemporalMonitor.andMonitor( 
		  SpatioTemporalMonitor.everywhereMonitor( 
		    SpatioTemporalMonitor.atomicMonitor( 
		      signal -> ((signal.get(1,Double.class))-(0.5))
		    )
		    ,
		    m -> DistanceStructure.buildDistanceStructure(m, 
		    	edge -> 1.0,
		    	0.0,
		    	1.0),
		    _domain_SensorNetwork
		  )
		   ,
		  _domain_SensorNetwork , 
		  SpatioTemporalMonitor.atomicMonitor( 
		    signal -> ((signal.get(2,Double.class))-(20))
		  )
		)
		;	
	}			
	
	private RecordHandler SensorNetwork_signal_handler_ = generateSensorNetworkSignalRecordHandler();
	
	private static RecordHandler generateSensorNetworkSignalRecordHandler() {
		int counter = 0;
		HashMap<String,Integer> variableIndex = new HashMap<>();
		variableIndex.put( "nodeType" , counter++ );
		variableIndex.put( "battery" , counter++ );
		variableIndex.put( "temperature" , counter++ );
		return new RecordHandler( variableIndex , DataHandler.INTEGER, DataHandler.REAL, DataHandler.REAL);
	}
	
	private RecordHandler SensorNetwork_parameters_handler_ = generateSensorNetworkParametersRecordHandler();
	
	private static RecordHandler generateSensorNetworkParametersRecordHandler() {
		int counter = 0;
		HashMap<String,Integer> variableIndex = new HashMap<>();
		return new RecordHandler( variableIndex );
	}
	private RecordHandler SensorNetwork_edge_handler_ = generateSensorNetworkEdgesRecordHandler();
	
	private static RecordHandler generateSensorNetworkEdgesRecordHandler() {
		int counter = 0;
		HashMap<String,Integer> variableIndex = new HashMap<>();
		variableIndex.put( "hop" , counter++ );
		variableIndex.put( "weight" , counter++ );
		return new RecordHandler( variableIndex , DataHandler.INTEGER, DataHandler.REAL);
	}
	
	private DataHandler<Double> SensorNetwork_output_data_handler_ = DataHandler.REAL;
	
	private SpatioTemporalScriptComponent<Double> MONITOR_SensorNetwork = new SpatioTemporalScriptComponent<>(
					"SensorNetwork" ,
					SensorNetwork_edge_handler_ ,
					SensorNetwork_signal_handler_ ,
					SensorNetwork_output_data_handler_ ,
					SensorNetwork_parameters_handler_ ,
					r -> SensorNetwork_main( r )	
				);
	
	public GeneratedScriptClass() {
		super( new String[] { 
		}, 
		new String[] {
			"SensorNetwork"
		});	
	}

	@Override
	public TemporalScriptComponent<?> selectTemporalComponent( String name ) {
		return null;					
	}				

	public SpatioTemporalScriptComponent<?> selectSpatioTemporalComponent( String name ) {
		if ("SensorNetwork".equals( name ) ) {
			return 	MONITOR_SensorNetwork; 
		}
		return null;
	}

	public TemporalScriptComponent<?> selectDefaultTemporalComponent( ) {
		return null;	
	}
		
	public SpatioTemporalScriptComponent<?> selectDefaultSpatioTemporalComponent( ) {
		return MONITOR_SensorNetwork;	
	}


}
