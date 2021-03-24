package eu.quanticol.moonlight.api.example;

import eu.quanticol.moonlight.MoonLightSpatialTemporalScript;
import eu.quanticol.moonlight.SpatialTemporalScriptComponent;
import eu.quanticol.moonlight.domain.BooleanDomain;
import eu.quanticol.moonlight.domain.SignalDomain;
import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor;
import eu.quanticol.moonlight.signal.DataHandler;
import eu.quanticol.moonlight.space.DistanceStructure;
import eu.quanticol.moonlight.space.MoonLightRecord;
import eu.quanticol.moonlight.signal.RecordHandler;

import java.util.HashMap;

public class CityMonitor extends MoonLightSpatialTemporalScript {

    public static enum poiType {
        BusStop,
        Hospital,
        MetroStop,
        MainSquare,
        Museum
    }

    private final SignalDomain<Boolean> _domain_City = new BooleanDomain();
    private final SignalDomain<Boolean> _domain_City2 = new BooleanDomain();

    private SpatialTemporalMonitor<MoonLightRecord, MoonLightRecord,Boolean> City_main(MoonLightRecord parameters ) {
        return SpatialTemporalMonitor.somewhereMonitor(
                SpatialTemporalMonitor.atomicMonitor(
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
    private SpatialTemporalMonitor<MoonLightRecord, MoonLightRecord,Boolean> City2_main(MoonLightRecord parameters ) {
        return SpatialTemporalMonitor.somewhereMonitor(
                SpatialTemporalMonitor.atomicMonitor(
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

    private SpatialTemporalScriptComponent<Boolean> MONITOR_City = new SpatialTemporalScriptComponent<>(
            "City" ,
            City_edge_handler_ ,
            City_signal_handler_ ,
            _domain_City ,
            City_parameters_handler_ ,
            r -> City_main( r )
    );
    private SpatialTemporalScriptComponent<Boolean> MONITOR_City2 = new SpatialTemporalScriptComponent<>(
            "City2" ,
            City2_edge_handler_ ,
            City2_signal_handler_ ,
            _domain_City ,
            City2_parameters_handler_ ,
            r -> City2_main( r )
    );

    public CityMonitor() {
        super( new String[] {
                        "City",
                        "City2"
                });
    }


    public SpatialTemporalScriptComponent<?> selectSpatialTemporalComponent(String name ) {
        if ("City".equals( name ) ) {
            return 	MONITOR_City;
        }
        if ("City2".equals( name ) ) {
            return 	MONITOR_City2;
        }
        return null;
    }

    public SpatialTemporalScriptComponent<?> selectDefaultSpatialTemporalComponent( ) {
        return MONITOR_City;
    }
}
