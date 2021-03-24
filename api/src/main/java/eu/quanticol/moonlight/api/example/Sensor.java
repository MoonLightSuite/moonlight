package eu.quanticol.moonlight.api.example;

import eu.quanticol.moonlight.MoonLightSpatialTemporalScript;
import eu.quanticol.moonlight.SpatialTemporalScriptComponent;
import eu.quanticol.moonlight.domain.DoubleDomain;
import eu.quanticol.moonlight.domain.SignalDomain;
import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor;
import eu.quanticol.moonlight.signal.DataHandler;
import eu.quanticol.moonlight.space.DistanceStructure;
import eu.quanticol.moonlight.space.MoonLightRecord;
import eu.quanticol.moonlight.signal.RecordHandler;

import java.util.HashMap;


public class Sensor extends MoonLightSpatialTemporalScript {


    private final SignalDomain<Double> _domain_SensorNetwork = new DoubleDomain();

    private SpatialTemporalMonitor<MoonLightRecord, MoonLightRecord, Double> SensorNetwork_main(MoonLightRecord parameters) {
        return SpatialTemporalMonitor.everywhereMonitor(
                SpatialTemporalMonitor.atomicMonitor(
                        signal -> Math.min((((signal.get(1, Double.class)) - (0.5))), (((signal.get(2, Double.class)) - (20))))
                )
                ,
                m -> DistanceStructure.buildDistanceStructure(m,
                        edge -> 1.0,
                        0.0,
                        1.0),
                _domain_SensorNetwork
        )
                ;
    }

    private RecordHandler SensorNetwork_signal_handler_ = generateSensorNetworkSignalRecordHandler();

    private static RecordHandler generateSensorNetworkSignalRecordHandler() {
        int counter = 0;
        HashMap<String, Integer> variableIndex = new HashMap<>();
        variableIndex.put("nodeType", counter++);
        variableIndex.put("battery", counter++);
        variableIndex.put("temperature", counter++);
        return new RecordHandler(variableIndex, DataHandler.INTEGER, DataHandler.REAL, DataHandler.REAL);
    }

    private RecordHandler SensorNetwork_parameters_handler_ = generateSensorNetworkParametersRecordHandler();

    private static RecordHandler generateSensorNetworkParametersRecordHandler() {
        int counter = 0;
        HashMap<String, Integer> variableIndex = new HashMap<>();
        return new RecordHandler(variableIndex);
    }

    private RecordHandler SensorNetwork_edge_handler_ = generateSensorNetworkEdgesRecordHandler();

    private static RecordHandler generateSensorNetworkEdgesRecordHandler() {
        int counter = 0;
        HashMap<String, Integer> variableIndex = new HashMap<>();
        variableIndex.put("hop", counter++);
        variableIndex.put("weight", counter++);
        return new RecordHandler(variableIndex, DataHandler.INTEGER, DataHandler.REAL);
    }

    private DataHandler<Double> SensorNetwork_output_data_handler_ = DataHandler.REAL;

    private SpatialTemporalScriptComponent<Double> MONITOR_SensorNetwork = new SpatialTemporalScriptComponent<>(
            "SensorNetwork",
            SensorNetwork_edge_handler_,
            SensorNetwork_signal_handler_,
            _domain_SensorNetwork,
            SensorNetwork_parameters_handler_,
            r -> SensorNetwork_main(r)
    );

    public Sensor() {
        super(
                new String[]{
                        "SensorNetwork"
                });
    }


    public SpatialTemporalScriptComponent<?> selectSpatialTemporalComponent(String name) {
        if ("SensorNetwork".equals(name)) {
            return MONITOR_SensorNetwork;
        }
        return null;
    }

    public SpatialTemporalScriptComponent<?> selectDefaultSpatialTemporalComponent() {
        return MONITOR_SensorNetwork;
    }


}
