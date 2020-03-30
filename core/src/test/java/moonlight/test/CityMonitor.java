/**
 * Code Generate by MoonLight tool.
 */ 
package moonlight.test;

import eu.quanticol.moonlight.*;
import eu.quanticol.moonlight.monitoring.spatialtemporal.*;
import eu.quanticol.moonlight.signal.*;
import eu.quanticol.moonlight.formula.*;

import java.util.HashMap;


public class CityMonitor extends MoonLightScript {			
	
				
	private final SignalDomain<Boolean> _domain_SensTemp = new BooleanDomain();
	private final SignalDomain<Boolean> _domain_SensTemp2 = new BooleanDomain();
	private final SignalDomain<Boolean> _domain_SensTemp3 = new BooleanDomain();
	
	private SpatialTemporalMonitor<Record,Record,Boolean> SensTemp_main(Record parameters ) {
		return SpatialTemporalMonitor.somewhereMonitor(
		  SpatialTemporalMonitor.globallyMonitor(
		    SpatialTemporalMonitor.atomicMonitor(
		      signal -> ((((double) signal.get(1,Double.class)))>(0.5))
		    )
		    ,
		    new Interval(0,0.2),
		    	    _domain_SensTemp
		    	  )
		  ,
		  m -> DistanceStructure.buildDistanceStructure(m, 
		  	edge -> (double) ((int) edge.get(0,Integer.class)),
		  	(double) 0,
		  	(double) 3),
		  _domain_SensTemp
		)
		;	
	}			
	private SpatialTemporalMonitor<Record,Record,Boolean> SensTemp2_main(Record parameters ) {
		return SpatialTemporalMonitor.somewhereMonitor(
		  SpatialTemporalMonitor.eventuallyMonitor(
		    SpatialTemporalMonitor.atomicMonitor(
		      signal -> ((((double) signal.get(1,Double.class)))>(0.5))
		    )
		    ,
		    new Interval(0,0.2),
		    	    _domain_SensTemp2
		    	  )
		  ,
		  m -> DistanceStructure.buildDistanceStructure(m, 
		  	edge -> (double) ((int) edge.get(0,Integer.class)),
		  	(double) 0,
		  	(double) 3),
		  _domain_SensTemp2
		)
		;	
	}			
	private SpatialTemporalMonitor<Record,Record,Boolean> SensTemp3_main(Record parameters ) {
		return SpatialTemporalMonitor.somewhereMonitor(
		  SpatialTemporalMonitor.onceMonitor(
		    SpatialTemporalMonitor.atomicMonitor(
		      signal -> ((((double) signal.get(1,Double.class)))>(0.5))
		    )
		    ,
		    new Interval(0,0.2),
		    _domain_SensTemp3
		  )
		  ,
		  m -> DistanceStructure.buildDistanceStructure(m, 
		  	edge -> (double) ((int) edge.get(0,Integer.class)),
		  	(double) 0,
		  	(double) 3),
		  _domain_SensTemp3
		)
		;	
	}			
	
	private RecordHandler SensTemp_signal_handler_ = generateSensTempSignalRecordHandler();
	
	private static RecordHandler generateSensTempSignalRecordHandler() {
		int counter = 0;
		HashMap<String,Integer> variableIndex = new HashMap<>();
		variableIndex.put( "nodeType" , counter++ );
		variableIndex.put( "battery" , counter++ );
		variableIndex.put( "temperature" , counter++ );
		return new RecordHandler( variableIndex , DataHandler.INTEGER, DataHandler.REAL, DataHandler.REAL);
	}
	
	private RecordHandler SensTemp_parameters_handler_ = generateSensTempParametersRecordHandler();
	
	private static RecordHandler generateSensTempParametersRecordHandler() {
		int counter = 0;
		HashMap<String,Integer> variableIndex = new HashMap<>();
		return new RecordHandler( variableIndex );
	}
	private RecordHandler SensTemp_edge_handler_ = generateSensTempEdgesRecordHandler();
	
	private static RecordHandler generateSensTempEdgesRecordHandler() {
		int counter = 0;
		HashMap<String,Integer> variableIndex = new HashMap<>();
		variableIndex.put( "hop" , counter++ );
		variableIndex.put( "dist" , counter++ );
		return new RecordHandler( variableIndex , DataHandler.INTEGER, DataHandler.REAL);
	}
	
	private DataHandler<Boolean> SensTemp_output_data_handler_ = DataHandler.BOOLEAN;
	private RecordHandler SensTemp2_signal_handler_ = generateSensTemp2SignalRecordHandler();
	
	private static RecordHandler generateSensTemp2SignalRecordHandler() {
		int counter = 0;
		HashMap<String,Integer> variableIndex = new HashMap<>();
		variableIndex.put( "nodeType" , counter++ );
		variableIndex.put( "battery" , counter++ );
		variableIndex.put( "temperature" , counter++ );
		return new RecordHandler( variableIndex , DataHandler.INTEGER, DataHandler.REAL, DataHandler.REAL);
	}
	
	private RecordHandler SensTemp2_parameters_handler_ = generateSensTemp2ParametersRecordHandler();
	
	private static RecordHandler generateSensTemp2ParametersRecordHandler() {
		int counter = 0;
		HashMap<String,Integer> variableIndex = new HashMap<>();
		return new RecordHandler( variableIndex );
	}
	private RecordHandler SensTemp2_edge_handler_ = generateSensTemp2EdgesRecordHandler();
	
	private static RecordHandler generateSensTemp2EdgesRecordHandler() {
		int counter = 0;
		HashMap<String,Integer> variableIndex = new HashMap<>();
		variableIndex.put( "hop" , counter++ );
		variableIndex.put( "dist" , counter++ );
		return new RecordHandler( variableIndex , DataHandler.INTEGER, DataHandler.REAL);
	}
	
	private DataHandler<Boolean> SensTemp2_output_data_handler_ = DataHandler.BOOLEAN;
	private RecordHandler SensTemp3_signal_handler_ = generateSensTemp3SignalRecordHandler();
	
	private static RecordHandler generateSensTemp3SignalRecordHandler() {
		int counter = 0;
		HashMap<String,Integer> variableIndex = new HashMap<>();
		variableIndex.put( "nodeType" , counter++ );
		variableIndex.put( "battery" , counter++ );
		variableIndex.put( "temperature" , counter++ );
		return new RecordHandler( variableIndex , DataHandler.INTEGER, DataHandler.REAL, DataHandler.REAL);
	}
	
	private RecordHandler SensTemp3_parameters_handler_ = generateSensTemp3ParametersRecordHandler();
	
	private static RecordHandler generateSensTemp3ParametersRecordHandler() {
		int counter = 0;
		HashMap<String,Integer> variableIndex = new HashMap<>();
		return new RecordHandler( variableIndex );
	}
	private RecordHandler SensTemp3_edge_handler_ = generateSensTemp3EdgesRecordHandler();
	
	private static RecordHandler generateSensTemp3EdgesRecordHandler() {
		int counter = 0;
		HashMap<String,Integer> variableIndex = new HashMap<>();
		variableIndex.put( "hop" , counter++ );
		variableIndex.put( "dist" , counter++ );
		return new RecordHandler( variableIndex , DataHandler.INTEGER, DataHandler.REAL);
	}
	
	private DataHandler<Boolean> SensTemp3_output_data_handler_ = DataHandler.BOOLEAN;
	
	private SpatialTemporalScriptComponent<Boolean> MONITOR_SensTemp = new SpatialTemporalScriptComponent<>(
					"SensTemp" ,
					SensTemp_edge_handler_ ,
					SensTemp_signal_handler_ ,
					SensTemp_output_data_handler_ ,
					SensTemp_parameters_handler_ ,
					r -> SensTemp_main( r )	
				);
	private SpatialTemporalScriptComponent<Boolean> MONITOR_SensTemp2 = new SpatialTemporalScriptComponent<>(
					"SensTemp2" ,
					SensTemp2_edge_handler_ ,
					SensTemp2_signal_handler_ ,
					SensTemp2_output_data_handler_ ,
					SensTemp2_parameters_handler_ ,
					r -> SensTemp2_main( r )	
				);
	private SpatialTemporalScriptComponent<Boolean> MONITOR_SensTemp3 = new SpatialTemporalScriptComponent<>(
					"SensTemp3" ,
					SensTemp3_edge_handler_ ,
					SensTemp3_signal_handler_ ,
					SensTemp3_output_data_handler_ ,
					SensTemp3_parameters_handler_ ,
					r -> SensTemp3_main( r )	
				);
	
	public CityMonitor() {
		super( new String[] { 
		}, 
		new String[] {
			"SensTemp",
			"SensTemp2",
			"SensTemp3"
		});	
	}

	@Override
	public TemporalScriptComponent<?> selectTemporalComponent( String name ) {
		return null;					
	}				

	public SpatialTemporalScriptComponent<?> selectSpatialTemporalComponent(String name ) {
		if ("SensTemp".equals( name ) ) {
			return 	MONITOR_SensTemp; 
		}
		if ("SensTemp2".equals( name ) ) {
			return 	MONITOR_SensTemp2; 
		}
		if ("SensTemp3".equals( name ) ) {
			return 	MONITOR_SensTemp3; 
		}
		return null;
	}

	public TemporalScriptComponent<?> selectDefaultTemporalComponent( ) {
		return null;	
	}
		
	public SpatialTemporalScriptComponent<?> selectDefaultSpatialTemporalComponent( ) {
		return MONITOR_SensTemp;	
	}


}