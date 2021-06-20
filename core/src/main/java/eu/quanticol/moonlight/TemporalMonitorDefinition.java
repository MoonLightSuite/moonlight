package eu.quanticol.moonlight;

import eu.quanticol.moonlight.formula.SignalDomain;
import eu.quanticol.moonlight.monitoring.temporal.TemporalMonitor;
import eu.quanticol.moonlight.signal.MoonLightRecord;
import eu.quanticol.moonlight.signal.RecordHandler;

/**
 * Instances of this class are used to represent a definition for a temporal monitor. Each definition
 * has a name and a set of parameters that are used to build the temporal monitor.
 */
public class TemporalMonitorDefinition {

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
     * Producer used to build the temporal monitor.
     */
    private final TemporalMonitorProducer producer;

    /**
     * Create a new definition.
     *
     * @param name  monitor name.
     * @param arguments monitor arguments.
     * @param signalRecordHandler record handler describing monitored signal.
     * @param producer producer used to build the monitor.
     */
    public TemporalMonitorDefinition(String name, RecordHandler arguments, RecordHandler signalRecordHandler, TemporalMonitorProducer producer) {
        this.name = name;
        this.arguments = arguments;
        this.signalRecordHandler = signalRecordHandler;
        this.producer = producer;
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
    public TemporalMonitorProducer getProducer() {
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
    public <S> TemporalMonitor<MoonLightRecord,S> getMonitor(SignalDomain<S> domain, MoonLightRecord arguments) {
        return producer.apply(domain,arguments);
    }

    public <S> TemporalMonitor<MoonLightRecord,S> getMonitorFromString(SignalDomain<S> domain, String[] values) {
        return getMonitor(domain, arguments.fromStringArray(values));
    }

    public <S> TemporalMonitor<MoonLightRecord,S> getMonitorFromDouble(SignalDomain<S> domain, double[] values) {
        return getMonitor(domain, arguments.fromDoubleArray(values));
    }

    public <S> TemporalMonitor<MoonLightRecord,S> getMonitorFromObject(SignalDomain<S> domain, Object[] values) {
        return getMonitor(domain, arguments.fromObjectArray(values));
    }

    public RecordHandler getSignalRecordHandler() {
        return signalRecordHandler;
    }
}
