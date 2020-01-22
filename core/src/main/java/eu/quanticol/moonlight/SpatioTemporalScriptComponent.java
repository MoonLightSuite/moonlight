/**
 *
 */
package eu.quanticol.moonlight;

import eu.quanticol.moonlight.io.SpatioTemporalSignalReader;
import eu.quanticol.moonlight.io.SpatioTemporalSignalWriter;
import eu.quanticol.moonlight.monitoring.SpatioTemporalMonitorinInput;
import eu.quanticol.moonlight.monitoring.spatiotemporal.SpatioTemporalMonitor;
import eu.quanticol.moonlight.signal.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Function;

/**
 * @author loreti
 *
 */
public class SpatioTemporalScriptComponent<S> {

    private final String name;
    private final RecordHandler signalRecordHandler;
    private final RecordHandler edgeRecordHandler;
    private final DataHandler<S> outputTypeHandler;
    private final RecordHandler parameters;
    private final Function<Record, SpatioTemporalMonitor<Record, Record, S>> builder;

    public SpatioTemporalScriptComponent(String name, RecordHandler edgeRecordHandler, RecordHandler signalRecordHandler, DataHandler<S> outputTypeHandler, RecordHandler parameters,
                                         Function<Record, SpatioTemporalMonitor<Record, Record, S>> builder) {
        super();
        this.name = name;
        this.signalRecordHandler = signalRecordHandler;
        this.outputTypeHandler = outputTypeHandler;
        this.edgeRecordHandler = edgeRecordHandler;
        this.parameters = parameters;
        this.builder = builder;
    }

    public SpatioTemporalScriptComponent(String name, RecordHandler edgeRecordHandler, RecordHandler signalRecordHandler, DataHandler<S> outputTypeHandler,
                                         Function<Record, SpatioTemporalMonitor<Record, Record, S>> builder) {
        this(name, edgeRecordHandler, signalRecordHandler, outputTypeHandler, null, builder);
    }


    public String getName() {
        return name;
    }

    public SpatioTemporalMonitor<Record, Record, S> getMonitor(Object... values) {
        if (this.parameters != null && this.parameters.size() > 0) {
            return builder.apply(parameters.fromObject(values));
        } else {
            return builder.apply(null);
        }
    }

    public SpatioTemporalSignal<S> monitor(LocationService<Record> locations, SpatioTemporalSignal<Record> input, Object... parameters) {
        SpatioTemporalMonitor<Record, Record, S> monitor = getMonitor(parameters);
        return monitor.monitor(locations, input);
    }

    public Object[][][] monitorToObjectArray(LocationService<Record> locations, SpatioTemporalSignal<Record> input, Object... parameters) {
        return monitor(locations, input, parameters).toObjectArray();
    }

    public Object[][][] monitorToObjectArray(double[] locationTimeArray, String[][][][] graph, double[] signalTimeArray, String[][][] signalValues, Object... parameters) {
        int locations = signalValues.length;
        SpatioTemporalSignal<Record> signal = RecordHandler.buildSpatioTemporalSignal(locations, signalRecordHandler, signalTimeArray, signalValues);
        LocationService<Record> locationService = LocationService.buildLocationService(locations, edgeRecordHandler, locationTimeArray, graph);
        return monitor(locationService, signal, parameters).toObjectArray();
    }

    public Object[][][] monitorToObjectArray(String[][][] graph, double[] signalTimeArray, String[][][] signalValues, Object... parameters) {
        int locations = signalValues.length;
        SpatioTemporalSignal<Record> signal = RecordHandler.buildSpatioTemporalSignal(locations, signalRecordHandler, signalTimeArray, signalValues);
        LocationService<Record> locationService = LocationService.buildLocationService(locations, edgeRecordHandler, signalTimeArray[0], graph);
        return monitor(locationService, signal, parameters).toObjectArray();
    }

    public void monitorToFile(SpatioTemporalSignalWriter writer, OutputStream stream, LocationService<Record> locations, SpatioTemporalSignal<Record> input, Object... values) throws IOException {
        SpatioTemporalSignal<S> signal = monitor(locations, input, values);
        writer.write(outputTypeHandler, signal, stream);
    }

    public void monitor(SpatioTemporalSignalReader reader, InputStream input, SpatioTemporalSignalWriter writer, OutputStream output, Object... values) throws IOException {
        SpatioTemporalMonitorinInput<Record, Record> monitorInput = reader.load(edgeRecordHandler, signalRecordHandler, input);
        monitorToFile(writer, output, monitorInput.getLocationService(), monitorInput.getSignal());
    }

    public String getInfo() {
        return this.toString();
    }

    public String[] getVariables() {
        return signalRecordHandler.getVariables();
    }
}
