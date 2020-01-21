package eu.quanticol.moonlight.api.example;

import eu.quanticol.moonlight.MoonLightScript;
import eu.quanticol.moonlight.SpatioTemporalScriptComponent;
import eu.quanticol.moonlight.TemporalScriptComponent;
import eu.quanticol.moonlight.formula.DoubleDomain;
import eu.quanticol.moonlight.formula.SignalDomain;
import eu.quanticol.moonlight.monitoring.spatiotemporal.SpatioTemporalMonitor;
import eu.quanticol.moonlight.signal.DataHandler;
import eu.quanticol.moonlight.signal.DistanceStructure;
import eu.quanticol.moonlight.signal.Record;
import eu.quanticol.moonlight.signal.RecordHandler;

import java.util.HashMap;


public class SensorMonitor extends MoonLightScript {


    private final SignalDomain<Double> _domain_SensorNetwork = new DoubleDomain();

    private SpatioTemporalMonitor<Record, Record, Double> SensorNetwork_main(Record parameters) {
        return SpatioTemporalMonitor.everywhereMonitor(
                SpatioTemporalMonitor.atomicMonitor(
                        signal -> (Math.min((((((signal.get(3, Double.class)) - (0.5))))), (((((signal.get(4, Double.class)) - (20)))))))
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
        variableIndex.put("x", counter++);
        variableIndex.put("y", counter++);
        variableIndex.put("battery", counter++);
        variableIndex.put("temperature", counter++);
        return new RecordHandler(variableIndex, DataHandler.INTEGER, DataHandler.REAL, DataHandler.REAL, DataHandler.REAL, DataHandler.REAL);
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

    private SpatioTemporalScriptComponent<Double> MONITOR_SensorNetwork = new SpatioTemporalScriptComponent<>(
            "SensorNetwork",
            SensorNetwork_edge_handler_,
            SensorNetwork_signal_handler_,
            SensorNetwork_output_data_handler_,
            SensorNetwork_parameters_handler_,
            r -> SensorNetwork_main(r)
    );

    public SensorMonitor() {
        super(new String[]{
                },
                new String[]{
                        "SensorNetwork"
                });
    }

    @Override
    public TemporalScriptComponent<?> selectTemporalComponent(String name) {
        return null;
    }

    public SpatioTemporalScriptComponent<?> selectSpatioTemporalComponent(String name) {
        if ("SensorNetwork".equals(name)) {
            return MONITOR_SensorNetwork;
        }
        return null;
    }

    public TemporalScriptComponent<?> selectDefaultTemporalComponent() {
        return null;
    }

    public SpatioTemporalScriptComponent<?> selectDefaultSpatioTemporalComponent() {
        return MONITOR_SensorNetwork;
    }


}
