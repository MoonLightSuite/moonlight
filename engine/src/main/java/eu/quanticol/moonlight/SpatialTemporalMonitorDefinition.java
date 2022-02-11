package eu.quanticol.moonlight;

import eu.quanticol.moonlight.domain.SignalDomain;
import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor;
import eu.quanticol.moonlight.io.MoonLightRecord;
import eu.quanticol.moonlight.signal.RecordHandler;

import java.util.Arrays;

/**
 * Instances of this class are used to represent a definition for a temporal monitor. Each definition
 * has a name and a set of parameters that are used to build the temporal monitor.
 */
public class SpatialTemporalMonitorDefinition {

    /**
     * Monitor name.
     */
    private final String name;

    /**
     * Monitor arguments.
     */
    private final RecordHandler arguments;

    /**
     * Record handler describing monitored signal.
     */
    private final RecordHandler signalRecordHandler;

    /**
     * Record handler describing monitored signal.
     */
    private final RecordHandler edgeRecordHandler;

    /**
     * Producer used to build the temporal monitor.
     */
    private final SpatialTemporalMonitorProducer producer;

    /**
     * Create a new definition.
     *
     * @param name  monitor name.
     * @param arguments monitor arguments.
     * @param signalRecordHandler record handler describing monitored signal.
     * @param producer producer used to build the monitor.
     */
    public SpatialTemporalMonitorDefinition(String name, RecordHandler arguments, RecordHandler signalRecordHandler, RecordHandler edgeRecordHandler, SpatialTemporalMonitorProducer producer) {
        this.name = name;
        this.arguments = arguments;
        this.signalRecordHandler = signalRecordHandler;
        this.producer = producer;
        this.edgeRecordHandler = edgeRecordHandler;
    }


    /**
     * Return the monitor name.
     * @return the monitor name.
     */
    public String getName() {
        return name;
    }

    /**
     * Return the monitor arguments.
     *
     * @return the monitor arguments.
     */
    public RecordHandler getArguments() {
        return arguments;
    }

    /**
     * Return the producer used to build the monitor.
     *
     * @return the producer used to build the monitor.
     */
    public SpatialTemporalMonitorProducer getProducer() {
        return producer;
    }

    /**
     * Create a new temporal monitor built by using the given arguments and based on the given signal domain.
     *
     * @param domain signal domain used for the monitor.
     * @param arguments argument used to build the monitor.
     * @param <S> data type used in the resulting signal.
     * @return temporal monitor built by using the given arguments and based on the given signal domain.
     */
    public <S> SpatialTemporalMonitor<MoonLightRecord,MoonLightRecord,S> getMonitor(SignalDomain<S> domain, MoonLightRecord arguments) {
        return producer.apply(domain,arguments);
    }

    public <S> SpatialTemporalMonitor<MoonLightRecord,MoonLightRecord,S> getMonitorFromString(SignalDomain<S> domain, String[] values) {
        return getMonitor(domain, arguments.fromStringArray(values));
    }

    public <S> SpatialTemporalMonitor<MoonLightRecord,MoonLightRecord,S> getMonitorFromDouble(SignalDomain<S> domain, double[] values) {
        return getMonitor(domain, evalArgumentFromDoubleArray(values));
    }

    private MoonLightRecord evalArgumentFromDoubleArray(double[] values) {
        if (this.arguments == null) {
            return null;
        }
        if (values == null) {
            values = new double[0];
        }
        return this.arguments.fromDoubleArray(values);
    }


    public <S> SpatialTemporalMonitor<MoonLightRecord,MoonLightRecord,S> getMonitorFromObject(SignalDomain<S> domain, Object[] values) {
        return getMonitor(domain, arguments.fromObjectArray(values));
    }

    public RecordHandler getSignalRecordHandler() {
        return signalRecordHandler;
    }

    public RecordHandler getEdgeRecordHandler() { return edgeRecordHandler; }

    public String getInfo() {
        return name+ Arrays.toString( arguments.getVariables() )+
                "\n Signal: "+ Arrays.deepToString(signalRecordHandler.getVariables())+
                "\n Edges:  "+ Arrays.deepToString(edgeRecordHandler.getVariables());

    }
}
