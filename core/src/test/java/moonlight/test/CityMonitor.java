/**
 * Code Generate by MoonLight tool.
 */ 
package moonlight.test;

import eu.quanticol.moonlight.*;
import eu.quanticol.moonlight.monitoring.temporal.*;
import eu.quanticol.moonlight.monitoring.spatialtemporal.*;
import eu.quanticol.moonlight.signal.*;
import eu.quanticol.moonlight.util.*;
import eu.quanticol.moonlight.*;
import eu.quanticol.moonlight.formula.*;
import java.util.function.Function;
import java.util.HashSet;
import java.util.HashMap;


public class CityMonitor extends MoonLightSpatialTemporalScript {			
	
				
	private final SignalDomain<Double> _singal_domain_ = new DoubleDomain();

	private RecordHandler _edge_handler_ = generateEdgesRecordHandler();
	
	private static RecordHandler generateEdgesRecordHandler() {
		int counter = 0;
		HashMap<String,Integer> variableIndex = new HashMap<>();
		variableIndex.put( "hop" , counter++ );
		variableIndex.put( "dist" , counter++ );
		return new RecordHandler( variableIndex , DataHandler.INTEGER, DataHandler.REAL);
	}
	
	private DataHandler<Double> _output_handler_ = DataHandler.REAL;
	
	
	private RecordHandler _signal_handler_ = generateSignalRecordHandler();
			
	private static RecordHandler generateSignalRecordHandler() {
		int counter = 0;
		HashMap<String,Integer> variableIndex = new HashMap<>();
		variableIndex.put( "nodeType" , counter++ );
		variableIndex.put( "battery" , counter++ );
		variableIndex.put( "temperature" , counter++ );
		return new RecordHandler( variableIndex , DataHandler.INTEGER, DataHandler.REAL, DataHandler.REAL);
	}
	
	private SpatialTemporalMonitor<Record,Record,Double> _FORMULA_SensNetkQuant( Record parameters ) {				
		return _FORMULA_SensNetkQuant( 
		);				
	}
	
	private SpatialTemporalMonitor<Record,Record,Double> _FORMULA_SensNetkQuant( 
	) {
		return SpatialTemporalMonitor.everywhereMonitor( 
		  SpatialTemporalMonitor.atomicMonitor( 
		    signal -> _singal_domain_.computeGreaterThan((((double) signal.get(1,Double.class))),(0.5))
		  )
		  ,
		  m -> DistanceStructure.buildDistanceStructure(m, 
		  	edge -> (double) 1.0,
		  	(double) 0.0,
		  	(double) 5.0),
		  _singal_domain_
		)
		;	
	}			
	
	private RecordHandler SensNetkQuant_parameters_handler_ = generateSensNetkQuantParametersRecordHandler();
	
	private static RecordHandler generateSensNetkQuantParametersRecordHandler() {
		int counter = 0;
		HashMap<String,Integer> variableIndex = new HashMap<>();
		return new RecordHandler( variableIndex );
	}
	
	private SpatialTemporalScriptComponent<Double> MONITOR_SensNetkQuant = new SpatialTemporalScriptComponent<>(
					"SensNetkQuant" ,
					_edge_handler_ ,
					_signal_handler_ ,
					_output_handler_ ,
					SensNetkQuant_parameters_handler_ ,
					r -> _FORMULA_SensNetkQuant( r )	
				);
	
	public CityMonitor() {
		super( new String[] { 
			"SensNetkQuant"
		});	
	}

public SpatialTemporalScriptComponent<?> selectSpatialTemporalComponent( String name ) {
	if ("SensNetkQuant".equals( name ) ) {
		return 	MONITOR_SensNetkQuant; 
	}
	return null;
}

public SpatialTemporalScriptComponent<?> selectDefaultSpatialTemporalComponent( ) {
	return MONITOR_SensNetkQuant;
}

}