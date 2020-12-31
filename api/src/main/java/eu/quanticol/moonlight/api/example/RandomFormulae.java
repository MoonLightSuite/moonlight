package eu.quanticol.moonlight.api.example;

import eu.quanticol.moonlight.MoonLightTemporalScript;
import eu.quanticol.moonlight.TemporalScriptComponent;
import eu.quanticol.moonlight.domain.BooleanDomain;
import eu.quanticol.moonlight.domain.Interval;
import eu.quanticol.moonlight.domain.SignalDomain;
import eu.quanticol.moonlight.monitoring.temporal.TemporalMonitor;
import eu.quanticol.moonlight.signal.DataHandler;
import eu.quanticol.moonlight.signal.MoonLightRecord;
import eu.quanticol.moonlight.signal.RecordHandler;

import java.util.HashMap;

public class RandomFormulae extends MoonLightTemporalScript {


    private final SignalDomain<Boolean> _domain_RandomFormulae = new BooleanDomain();

    private TemporalMonitor<MoonLightRecord,Boolean> RandomFormulae_main (MoonLightRecord parameters ) {
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
            _domain_RandomFormulae ,
            RandomFormulae_parameters_handler_ ,
            r -> RandomFormulae_main( r )
    );

    public RandomFormulae() {
        super( new String[] {
                        "RandomFormulae"
                });
    }

    @Override
    public TemporalScriptComponent<?> selectTemporalComponent( String name ) {
        if ("RandomFormulae".equals( name ) ) {
            return 	MONITOR_RandomFormulae;
        }
        return null;
    }

    public TemporalScriptComponent<?> selectDefaultTemporalComponent( ) {
        return MONITOR_RandomFormulae;
    }


}

