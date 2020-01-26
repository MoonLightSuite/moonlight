/**
 *
 */
package eu.quanticol.moonlight;

import eu.quanticol.moonlight.io.TemporalSignalReader;
import eu.quanticol.moonlight.io.TemporalSignalWriter;
import eu.quanticol.moonlight.monitoring.temporal.TemporalMonitor;
import eu.quanticol.moonlight.signal.DataHandler;
import eu.quanticol.moonlight.signal.Record;
import eu.quanticol.moonlight.signal.RecordHandler;
import eu.quanticol.moonlight.signal.Signal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Function;

/**
 * @author loreti
 *
 */
public class TemporalScriptComponent<S> {

    private final String name;
    private final RecordHandler signalRecordHandler;
    private final DataHandler<S> outputTypeHandler;
    private final RecordHandler parameters;
    private final Function<Record, TemporalMonitor<Record, S>> builder;

    public TemporalScriptComponent(String name, RecordHandler signalRecordHandler,
                                   DataHandler<S> outputTypeHandler,
                                   RecordHandler parameters,
                                   Function<Record, TemporalMonitor<Record, S>> builder) {
        super();
        this.name = name;
        this.signalRecordHandler = signalRecordHandler;
        this.outputTypeHandler = outputTypeHandler;
        this.parameters = parameters;
        this.builder = builder;
    }

    public TemporalScriptComponent(String name, RecordHandler signalRecordHandler,
                                   DataHandler<S> outputTypeHandler,
                                   Function<Record, TemporalMonitor<Record, S>> builder) {
        this(name, signalRecordHandler, outputTypeHandler, null, builder);
    }

    public String getName() {
        return name;
    }

    public TemporalMonitor<Record, S> getMonitor(String... values) {
        if (this.parameters != null && this.parameters.size() > 0 && values.length > 0) {
            return builder.apply(parameters.fromString(values));
        } else {
            return builder.apply(null);
        }
    }

    public Signal<S> monitor(Signal<Record> input, String... values) {
        TemporalMonitor<Record, S> monitor = getMonitor(values);
        return monitor.monitor(input);
    }

    public Object[][] monitorToObjectArray(Signal<Record> input, String... values) {
        return monitor(input, values).toObjectArray();
    }

    public Object[][] monitorToObjectArray(double[] time, String[][] signal, String... values) {
        return monitorToObjectArray(RecordHandler.buildTemporalSignal(signalRecordHandler, time, signal), values);
    }

    public void monitorToFile(TemporalSignalWriter writer, OutputStream stream, Signal<Record> input, String... values) throws IOException {
        Signal<S> signal = monitor(input, values);
        writer.write(outputTypeHandler, signal, stream);
    }

    public void monitor(TemporalSignalReader reader, InputStream input, TemporalSignalWriter writer, OutputStream output, Object... values) throws IOException {
        monitorToFile(writer, output, reader.load(signalRecordHandler, input));
    }

    public String getInfo() {
        //TODO: Complete!
        return this.toString();
    }

    public String[] getVariables() {
        return signalRecordHandler.getVariables();
    }

}
