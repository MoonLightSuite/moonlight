/**
 * Code Generate by MoonLight tool.
 */ 
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
	
	public CityMonitor() {
		super( new String[] { 
		}, 
		new String[] {
			"City",
			"City2"
		});	
	}

	@Override
	public TemporalScriptComponent<?> selectTemporalComponent( String name ) {
		return null;					
	}				

	public SpatioTemporalScriptComponent<?> selectSpatioTemporalComponent( String name ) {
		if ("City".equals( name ) ) {
			return 	MONITOR_City; 
		}
		if ("City2".equals( name ) ) {
			return 	MONITOR_City2; 
		}
		return null;
	}

	public TemporalScriptComponent<?> selectDefaultTemporalComponent( ) {
		return null;	
	}
		
	public SpatioTemporalScriptComponent<?> selectDefaultSpatioTemporalComponent( ) {
		return MONITOR_City;	
	}


}