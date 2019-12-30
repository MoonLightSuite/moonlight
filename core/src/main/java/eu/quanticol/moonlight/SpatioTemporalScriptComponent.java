/**
 * 
 */
package eu.quanticol.moonlight;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Function;

import eu.quanticol.moonlight.io.SpatioTemporalSignalReader;
import eu.quanticol.moonlight.io.SpatioTemporalSignalWriter;
import eu.quanticol.moonlight.io.TemporalSignalReader;
import eu.quanticol.moonlight.io.TemporalSignalWriter;
import eu.quanticol.moonlight.monitoring.SpatioTemporalMonitorinInput;
import eu.quanticol.moonlight.monitoring.spatiotemporal.SpatioTemporalMonitor;
import eu.quanticol.moonlight.monitoring.temporal.TemporalMonitor;
import eu.quanticol.moonlight.signal.DataHandler;
import eu.quanticol.moonlight.signal.LocationService;
import eu.quanticol.moonlight.signal.Record;
import eu.quanticol.moonlight.signal.RecordHandler;
import eu.quanticol.moonlight.signal.Signal;
import eu.quanticol.moonlight.signal.SpatioTemporalSignal;

/**
 * @author loreti
 *
 */
public abstract class SpatioTemporalScriptComponent<S> {
	
	private final String name;
	private final RecordHandler signalRecordHandler;
	private final RecordHandler edgeRecordHandler;
	private final DataHandler<S> outputTypeHandler;
	private final RecordHandler parameters;
	private final Function<Record, SpatioTemporalMonitor<Record,Record, S>>  builder;
	
	public SpatioTemporalScriptComponent(String name, RecordHandler edgeRecordHandler, RecordHandler signalRecordHandler, DataHandler<S> outputTypeHandler, RecordHandler parameters,
			Function<Record, SpatioTemporalMonitor<Record,Record, S>> builder) {
		super();
		this.name = name;
		this.signalRecordHandler = signalRecordHandler;
		this.outputTypeHandler = outputTypeHandler;
		this.edgeRecordHandler = edgeRecordHandler;
		this.parameters = parameters;
		this.builder = builder;
	}

	public String getName() {
		return name;
	}
	
	public SpatioTemporalMonitor<Record, Record, S> getMonitor( Object ... values ) {
		return builder.apply(parameters.fromObject(values));
	}

	public SpatioTemporalSignal<S> monitor( LocationService<Record> locations, SpatioTemporalSignal<Record> input , Object ... values ) {
		SpatioTemporalMonitor<Record, Record, S> monitor = getMonitor(values);
		return monitor.monitor(locations,input);
	}
	
	public Object[][][] monitorToObjectArray( LocationService<Record> locations, SpatioTemporalSignal<Record> input , Object ... values ) {
		return monitor(locations,input,values).toObjectArray();
	}
	
	public void monitorToFile( SpatioTemporalSignalWriter writer, OutputStream stream, LocationService<Record> locations, SpatioTemporalSignal<Record> input , Object ... values ) throws IOException {
		SpatioTemporalSignal<S> signal = monitor(locations,input,values);
		writer.write(outputTypeHandler, signal, stream);
	}
	
	public void monitor( SpatioTemporalSignalReader reader, InputStream input, SpatioTemporalSignalWriter writer, OutputStream output, Object ... values ) throws IOException {
		SpatioTemporalMonitorinInput<Record, Record> monitorInput = reader.load(edgeRecordHandler, signalRecordHandler, input);
		monitorToFile( writer, output, monitorInput.getLocationService(), monitorInput.getSignal());
	}

	public String getInfo() {
		return this.toString();
	}
}
