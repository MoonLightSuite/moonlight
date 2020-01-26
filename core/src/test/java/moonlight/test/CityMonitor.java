/**
 * Code Generate by MoonLight tool.
 */ 
package moonlight.test;

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


public class CityMonitor extends MoonLightScript {			
	
	public static enum poiType {
		BusStop,
		Hospital,
		MetroStop,
		MainSquare,
		Museum
	}
				
	private final SignalDomain<Boolean> _domain_City = new BooleanDomain();
	private final SignalDomain<Boolean> _domain_City2 = new BooleanDomain();
	private final SignalDomain<Boolean> _domain_City3 = new BooleanDomain();
	private final SignalDomain<Boolean> _domain_City4 = new BooleanDomain();
	private final SignalDomain<Boolean> _domain_City5 = new BooleanDomain();
	private final SignalDomain<Boolean> _domain_City6 = new BooleanDomain();
	private final SignalDomain<Boolean> _domain_City7 = new BooleanDomain();
	
	private SpatioTemporalMonitor<Record,Record,Boolean> City_main( Record parameters ) {
		return SpatioTemporalMonitor.somewhereMonitor( 
		  SpatioTemporalMonitor.atomicMonitor( 
		    signal -> signal.get(0,Boolean.class)
		  )
		  ,
		  m -> DistanceStructure.buildDistanceStructure(m, 
		  	edge -> 1.0,
		  	0.0,
		  	1.0),
		  _domain_City
		)
		;	
	}			
	private SpatioTemporalMonitor<Record,Record,Boolean> City2_main( Record parameters ) {
		return SpatioTemporalMonitor.somewhereMonitor( 
		  SpatioTemporalMonitor.atomicMonitor( 
		    signal -> signal.get(0,Boolean.class)
		  )
		  ,
		  m -> DistanceStructure.buildDistanceStructure(m, 
		  	edge -> 1.0,
		  	0.0,
		  	1.0),
		  _domain_City2
		)
		;	
	}			
	private TemporalMonitor<Record,Boolean> City3_main ( Record parameters ) {
		return TemporalMonitor.globallyMonitor( 
			TemporalMonitor.atomicMonitor( 
				signal -> signal.get(0,Boolean.class)
			)
			,
			_domain_City3
			, new Interval(0.0,1.0)
		)
		;	
	}
	private TemporalMonitor<Record,Boolean> City4_main ( Record parameters ) {
		return TemporalMonitor.eventuallyMonitor( 
			TemporalMonitor.atomicMonitor( 
				signal -> signal.get(0,Boolean.class)
			)
			,
			_domain_City4
			, new Interval(0.0,1.0)
		)
		;	
	}
	private TemporalMonitor<Record,Boolean> City5_main ( Record parameters ) {
		return TemporalMonitor.historicallyMonitor( 
			TemporalMonitor.atomicMonitor( 
				signal -> signal.get(0,Boolean.class)
			)
			,
			_domain_City5
			, new Interval(0.0,1.0)
		)
		;	
	}
	private TemporalMonitor<Record,Boolean> City6_main ( Record parameters ) {
		return TemporalMonitor.onceMonitor( 
			TemporalMonitor.atomicMonitor( 
				signal -> signal.get(0,Boolean.class)
			)
			,
			_domain_City6
			, new Interval(0.0,1.0)
		)
		;	
	}
	private SpatioTemporalMonitor<Record,Record,Boolean> City7_main( Record parameters ) {
		return SpatioTemporalMonitor.somewhereMonitor( 
		  SpatioTemporalMonitor.atomicMonitor( 
		    signal -> signal.get(0,Boolean.class)
		  )
		  ,
		  m -> DistanceStructure.buildDistanceStructure(m, 
		  	edge -> (double) edge.get(-1,Integer.class),
		  	0.0,
		  	1.0),
		  _domain_City7
		)
		;	
	}			
	
	private RecordHandler City_signal_handler_ = generateCitySignalRecordHandler();
	
	private static RecordHandler generateCitySignalRecordHandler() {
		int counter = 0;
		HashMap<String,Integer> variableIndex = new HashMap<>();
		variableIndex.put( "taxi" , counter++ );
		variableIndex.put( "peole" , counter++ );
		return new RecordHandler( variableIndex , DataHandler.BOOLEAN, DataHandler.INTEGER);
	}
	
	private RecordHandler City_parameters_handler_ = generateCityParametersRecordHandler();
	
	private static RecordHandler generateCityParametersRecordHandler() {
		int counter = 0;
		HashMap<String,Integer> variableIndex = new HashMap<>();
		return new RecordHandler( variableIndex );
	}
	private RecordHandler City_edge_handler_ = generateCityEdgesRecordHandler();
	
	private static RecordHandler generateCityEdgesRecordHandler() {
		int counter = 0;
		HashMap<String,Integer> variableIndex = new HashMap<>();
		variableIndex.put( "length" , counter++ );
		return new RecordHandler( variableIndex , DataHandler.REAL);
	}
	
	private DataHandler<Boolean> City_output_data_handler_ = DataHandler.BOOLEAN;
	private RecordHandler City2_signal_handler_ = generateCity2SignalRecordHandler();
	
	private static RecordHandler generateCity2SignalRecordHandler() {
		int counter = 0;
		HashMap<String,Integer> variableIndex = new HashMap<>();
		variableIndex.put( "taxi" , counter++ );
		variableIndex.put( "peole" , counter++ );
		return new RecordHandler( variableIndex , DataHandler.BOOLEAN, DataHandler.INTEGER);
	}
	
	private RecordHandler City2_parameters_handler_ = generateCity2ParametersRecordHandler();
	
	private static RecordHandler generateCity2ParametersRecordHandler() {
		int counter = 0;
		HashMap<String,Integer> variableIndex = new HashMap<>();
		return new RecordHandler( variableIndex );
	}
	private RecordHandler City2_edge_handler_ = generateCity2EdgesRecordHandler();
	
	private static RecordHandler generateCity2EdgesRecordHandler() {
		int counter = 0;
		HashMap<String,Integer> variableIndex = new HashMap<>();
		variableIndex.put( "length" , counter++ );
		return new RecordHandler( variableIndex , DataHandler.REAL);
	}
	
	private DataHandler<Boolean> City2_output_data_handler_ = DataHandler.BOOLEAN;
	private RecordHandler City3_signal_handler_ = generateCity3SignalRecordHandler();
	
	private static RecordHandler generateCity3SignalRecordHandler() {
		int counter = 0;
		HashMap<String,Integer> variableIndex = new HashMap<>();
		variableIndex.put( "taxi" , counter++ );
		variableIndex.put( "peole" , counter++ );
		return new RecordHandler( variableIndex , DataHandler.BOOLEAN, DataHandler.INTEGER);
	}
	
	private RecordHandler City3_parameters_handler_ = generateCity3ParametersRecordHandler();
	
	private static RecordHandler generateCity3ParametersRecordHandler() {
		int counter = 0;
		HashMap<String,Integer> variableIndex = new HashMap<>();
		return new RecordHandler( variableIndex );
	}
	
	private DataHandler<Boolean> City3_output_data_handler_ = DataHandler.BOOLEAN;
	private RecordHandler City4_signal_handler_ = generateCity4SignalRecordHandler();
	
	private static RecordHandler generateCity4SignalRecordHandler() {
		int counter = 0;
		HashMap<String,Integer> variableIndex = new HashMap<>();
		variableIndex.put( "taxi" , counter++ );
		variableIndex.put( "peole" , counter++ );
		return new RecordHandler( variableIndex , DataHandler.BOOLEAN, DataHandler.INTEGER);
	}
	
	private RecordHandler City4_parameters_handler_ = generateCity4ParametersRecordHandler();
	
	private static RecordHandler generateCity4ParametersRecordHandler() {
		int counter = 0;
		HashMap<String,Integer> variableIndex = new HashMap<>();
		return new RecordHandler( variableIndex );
	}
	
	private DataHandler<Boolean> City4_output_data_handler_ = DataHandler.BOOLEAN;
	private RecordHandler City5_signal_handler_ = generateCity5SignalRecordHandler();
	
	private static RecordHandler generateCity5SignalRecordHandler() {
		int counter = 0;
		HashMap<String,Integer> variableIndex = new HashMap<>();
		variableIndex.put( "taxi" , counter++ );
		variableIndex.put( "peole" , counter++ );
		return new RecordHandler( variableIndex , DataHandler.BOOLEAN, DataHandler.INTEGER);
	}
	
	private RecordHandler City5_parameters_handler_ = generateCity5ParametersRecordHandler();
	
	private static RecordHandler generateCity5ParametersRecordHandler() {
		int counter = 0;
		HashMap<String,Integer> variableIndex = new HashMap<>();
		return new RecordHandler( variableIndex );
	}
	
	private DataHandler<Boolean> City5_output_data_handler_ = DataHandler.BOOLEAN;
	private RecordHandler City6_signal_handler_ = generateCity6SignalRecordHandler();
	
	private static RecordHandler generateCity6SignalRecordHandler() {
		int counter = 0;
		HashMap<String,Integer> variableIndex = new HashMap<>();
		variableIndex.put( "taxi" , counter++ );
		variableIndex.put( "peole" , counter++ );
		return new RecordHandler( variableIndex , DataHandler.BOOLEAN, DataHandler.INTEGER);
	}
	
	private RecordHandler City6_parameters_handler_ = generateCity6ParametersRecordHandler();
	
	private static RecordHandler generateCity6ParametersRecordHandler() {
		int counter = 0;
		HashMap<String,Integer> variableIndex = new HashMap<>();
		return new RecordHandler( variableIndex );
	}
	
	private DataHandler<Boolean> City6_output_data_handler_ = DataHandler.BOOLEAN;
	private RecordHandler City7_signal_handler_ = generateCity7SignalRecordHandler();
	
	private static RecordHandler generateCity7SignalRecordHandler() {
		int counter = 0;
		HashMap<String,Integer> variableIndex = new HashMap<>();
		variableIndex.put( "taxi" , counter++ );
		variableIndex.put( "peole" , counter++ );
		return new RecordHandler( variableIndex , DataHandler.BOOLEAN, DataHandler.INTEGER);
	}
	
	private RecordHandler City7_parameters_handler_ = generateCity7ParametersRecordHandler();
	
	private static RecordHandler generateCity7ParametersRecordHandler() {
		int counter = 0;
		HashMap<String,Integer> variableIndex = new HashMap<>();
		variableIndex.put( "steps" , counter++ );
		return new RecordHandler( variableIndex , DataHandler.INTEGER);
	}
	private RecordHandler City7_edge_handler_ = generateCity7EdgesRecordHandler();
	
	private static RecordHandler generateCity7EdgesRecordHandler() {
		int counter = 0;
		HashMap<String,Integer> variableIndex = new HashMap<>();
		variableIndex.put( "length" , counter++ );
		variableIndex.put( "hop" , counter++ );
		return new RecordHandler( variableIndex , DataHandler.REAL, DataHandler.INTEGER);
	}
	
	private DataHandler<Boolean> City7_output_data_handler_ = DataHandler.BOOLEAN;
	
	private SpatioTemporalScriptComponent<Boolean> MONITOR_City = new SpatioTemporalScriptComponent<>(
					"City" ,
					City_edge_handler_ ,
					City_signal_handler_ ,
					City_output_data_handler_ ,
					City_parameters_handler_ ,
					r -> City_main( r )	
				);
	private SpatioTemporalScriptComponent<Boolean> MONITOR_City2 = new SpatioTemporalScriptComponent<>(
					"City2" ,
					City2_edge_handler_ ,
					City2_signal_handler_ ,
					City2_output_data_handler_ ,
					City2_parameters_handler_ ,
					r -> City2_main( r )	
				);
	private TemporalScriptComponent<Boolean> MONITOR_City3 = new TemporalScriptComponent<>(
					"City3" ,
					City3_signal_handler_ ,
					City3_output_data_handler_ ,
					City3_parameters_handler_ ,
					r -> City3_main( r )	
				);
	private TemporalScriptComponent<Boolean> MONITOR_City4 = new TemporalScriptComponent<>(
					"City4" ,
					City4_signal_handler_ ,
					City4_output_data_handler_ ,
					City4_parameters_handler_ ,
					r -> City4_main( r )	
				);
	private TemporalScriptComponent<Boolean> MONITOR_City5 = new TemporalScriptComponent<>(
					"City5" ,
					City5_signal_handler_ ,
					City5_output_data_handler_ ,
					City5_parameters_handler_ ,
					r -> City5_main( r )	
				);
	private TemporalScriptComponent<Boolean> MONITOR_City6 = new TemporalScriptComponent<>(
					"City6" ,
					City6_signal_handler_ ,
					City6_output_data_handler_ ,
					City6_parameters_handler_ ,
					r -> City6_main( r )	
				);
	private SpatioTemporalScriptComponent<Boolean> MONITOR_City7 = new SpatioTemporalScriptComponent<>(
					"City7" ,
					City7_edge_handler_ ,
					City7_signal_handler_ ,
					City7_output_data_handler_ ,
					City7_parameters_handler_ ,
					r -> City7_main( r )	
				);
	
	public CityMonitor() {
		super( new String[] { 
			"City3",
			"City4",
			"City5",
			"City6"
		}, 
		new String[] {
			"City",
			"City2",
			"City7"
		});	
	}

	@Override
	public TemporalScriptComponent<?> selectTemporalComponent( String name ) {
		if ("City3".equals( name ) ) {
			return 	MONITOR_City3; 
		}
		if ("City4".equals( name ) ) {
			return 	MONITOR_City4; 
		}
		if ("City5".equals( name ) ) {
			return 	MONITOR_City5; 
		}
		if ("City6".equals( name ) ) {
			return 	MONITOR_City6; 
		}
		return null;					
	}				

	public SpatioTemporalScriptComponent<?> selectSpatioTemporalComponent( String name ) {
		if ("City".equals( name ) ) {
			return 	MONITOR_City; 
		}
		if ("City2".equals( name ) ) {
			return 	MONITOR_City2; 
		}
		if ("City7".equals( name ) ) {
			return 	MONITOR_City7; 
		}
		return null;
	}

	public TemporalScriptComponent<?> selectDefaultTemporalComponent( ) {
		return MONITOR_City3;	
	}
		
	public SpatioTemporalScriptComponent<?> selectDefaultSpatioTemporalComponent( ) {
		return MONITOR_City;	
	}


}
