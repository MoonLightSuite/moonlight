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
	
				
	private final SignalDomain<Boolean> _domain_RandomFormulae = new BooleanDomain();
	
	private TemporalMonitor<Record,Boolean> RandomFormulae_main ( Record parameters ) {
		return TemporalMonitor.globallyMonitor( 
			TemporalMonitor.atomicMonitor( 
				signal -> ((signal.get(0,Double.class))>=(0))
			)
			,
			_domain_RandomFormulae
			, new Interval(73,98)
		)
		;	
	}
	
	private RecordHandler RandomFormulae_signal_handler_ = generateRandomFormulaeSignalRecordHandler();
	
	private static RecordHandler generateRandomFormulaeSignalRecordHandler() {
		int counter = 0;
		HashMap<String,Integer> variableIndex = new HashMap<>();
		variableIndex.put( "x" , counter++ );
		variableIndex.put( "y" , counter++ );
		variableIndex.put( "z" , counter++ );
		return new RecordHandler( variableIndex , DataHandler.REAL, DataHandler.REAL, DataHandler.REAL);
	}
	
	private RecordHandler RandomFormulae_parameters_handler_ = generateRandomFormulaeParametersRecordHandler();
	
	private static RecordHandler generateRandomFormulaeParametersRecordHandler() {
		int counter = 0;
		HashMap<String,Integer> variableIndex = new HashMap<>();
		return new RecordHandler( variableIndex );
	}
	
	private DataHandler<Boolean> RandomFormulae_output_data_handler_ = DataHandler.BOOLEAN;
	
	private TemporalScriptComponent<Boolean> MONITOR_RandomFormulae = new TemporalScriptComponent<>(
					"RandomFormulae" ,
					RandomFormulae_signal_handler_ ,
					RandomFormulae_output_data_handler_ ,
					RandomFormulae_parameters_handler_ ,
					r -> RandomFormulae_main( r )	
				);
	
	public GeneratedScriptClass() {
		super( new String[] { 
			"RandomFormulae"
		}, 
		new String[] {
		});	
	}

	@Override
	public TemporalScriptComponent<?> selectTemporalComponent( String name ) {
		if ("RandomFormulae".equals( name ) ) {
			return 	MONITOR_RandomFormulae; 
		}
		return null;					
	}				

	public SpatioTemporalScriptComponent<?> selectSpatioTemporalComponent( String name ) {
		return null;
	}

	public TemporalScriptComponent<?> selectDefaultTemporalComponent( ) {
		return MONITOR_RandomFormulae;	
	}
		
	public SpatioTemporalScriptComponent<?> selectDefaultSpatioTemporalComponent( ) {
		return null;	
	}


}
