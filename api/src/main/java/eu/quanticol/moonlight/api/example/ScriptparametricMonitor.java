package eu.quanticol.moonlight.api.example; /**
 * Code Generate by MoonLight tool.
 */
import eu.quanticol.moonlight.*;
import eu.quanticol.moonlight.monitoring.temporal.*;
import eu.quanticol.moonlight.signal.*;
import eu.quanticol.moonlight.domain.BooleanDomain;
import eu.quanticol.moonlight.domain.DoubleDomain;
import eu.quanticol.moonlight.domain.Interval;
import eu.quanticol.moonlight.domain.SignalDomain;
import eu.quanticol.moonlight.signal.space.MoonLightRecord;

import java.util.HashMap;


public class ScriptparametricMonitor extends MoonLightTemporalScript {


    private final SignalDomain<Boolean> _domain_BooleanMonitorScript = new BooleanDomain();
    private final SignalDomain<Double> _domain_QuantitativeMonitorScript = new DoubleDomain();

    private TemporalMonitor<MoonLightRecord,Boolean> BooleanMonitorScript_main (MoonLightRecord parameters ) {
        return TemporalMonitor.globallyMonitor(
                TemporalMonitor.atomicMonitor(
                        signal -> ((((signal.get(0,Double.class))-(signal.get(1,Double.class))))>(0))
                )
                ,
                _domain_BooleanMonitorScript
                , new Interval(parameters.get(0,Double.class),parameters.get(1,Double.class))
        )
                ;
    }
    private TemporalMonitor<MoonLightRecord,Double> QuantitativeMonitorScript_main (MoonLightRecord parameters ) {
        return TemporalMonitor.globallyMonitor(
                TemporalMonitor.atomicMonitor(
                        signal -> ((signal.get(0,Double.class))-(signal.get(1,Double.class)))
                )
                ,
                _domain_QuantitativeMonitorScript
                , new Interval(parameters.get(0,Double.class),parameters.get(1,Double.class))
        )
                ;
    }

    private RecordHandler BooleanMonitorScript_signal_handler_ = generateBooleanMonitorScriptSignalRecordHandler();

    private static RecordHandler generateBooleanMonitorScriptSignalRecordHandler() {
        int counter = 0;
        HashMap<String,Integer> variableIndex = new HashMap<>();
        variableIndex.put( "x" , counter++ );
        variableIndex.put( "y" , counter++ );
        return new RecordHandler( variableIndex , DataHandler.REAL, DataHandler.REAL);
    }

    private RecordHandler BooleanMonitorScript_parameters_handler_ = generateBooleanMonitorScriptParametersRecordHandler();

    private static RecordHandler generateBooleanMonitorScriptParametersRecordHandler() {
        int counter = 0;
        HashMap<String,Integer> variableIndex = new HashMap<>();
        variableIndex.put( "LB" , counter++ );
        variableIndex.put( "UB" , counter++ );
        return new RecordHandler( variableIndex , DataHandler.REAL, DataHandler.REAL);
    }

    private DataHandler<Boolean> BooleanMonitorScript_output_data_handler_ = DataHandler.BOOLEAN;
    private RecordHandler QuantitativeMonitorScript_signal_handler_ = generateQuantitativeMonitorScriptSignalRecordHandler();

    private static RecordHandler generateQuantitativeMonitorScriptSignalRecordHandler() {
        int counter = 0;
        HashMap<String,Integer> variableIndex = new HashMap<>();
        variableIndex.put( "x" , counter++ );
        variableIndex.put( "y" , counter++ );
        return new RecordHandler( variableIndex , DataHandler.REAL, DataHandler.REAL);
    }

    private RecordHandler QuantitativeMonitorScript_parameters_handler_ = generateQuantitativeMonitorScriptParametersRecordHandler();

    private static RecordHandler generateQuantitativeMonitorScriptParametersRecordHandler() {
        int counter = 0;
        HashMap<String,Integer> variableIndex = new HashMap<>();
        variableIndex.put( "LB" , counter++ );
        variableIndex.put( "UB" , counter++ );
        return new RecordHandler( variableIndex , DataHandler.REAL, DataHandler.REAL);
    }

    private DataHandler<Double> QuantitativeMonitorScript_output_data_handler_ = DataHandler.REAL;

    private TemporalScriptComponent<Boolean> MONITOR_BooleanMonitorScript = new TemporalScriptComponent<>(
            "BooleanMonitorScript" ,
            BooleanMonitorScript_signal_handler_ ,
            _domain_BooleanMonitorScript ,
            BooleanMonitorScript_parameters_handler_ ,
            r -> BooleanMonitorScript_main( r )
    );
    private TemporalScriptComponent<Double> MONITOR_QuantitativeMonitorScript = new TemporalScriptComponent<>(
            "QuantitativeMonitorScript" ,
            QuantitativeMonitorScript_signal_handler_ ,
            _domain_QuantitativeMonitorScript ,
            QuantitativeMonitorScript_parameters_handler_ ,
            r -> QuantitativeMonitorScript_main( r )
    );

    public ScriptparametricMonitor() {
        super( new String[] {
                        "BooleanMonitorScript",
                        "QuantitativeMonitorScript"
                });
    }

    @Override
    public TemporalScriptComponent<?> selectTemporalComponent( String name ) {
        if ("BooleanMonitorScript".equals( name ) ) {
            return 	MONITOR_BooleanMonitorScript;
        }
        if ("QuantitativeMonitorScript".equals( name ) ) {
            return 	MONITOR_QuantitativeMonitorScript;
        }
        return null;
    }

    public TemporalScriptComponent<?> selectDefaultTemporalComponent( ) {
        return MONITOR_BooleanMonitorScript;
    }


}
