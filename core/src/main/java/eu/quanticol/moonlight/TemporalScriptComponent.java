/*
 * MoonLight: a light-weight framework for runtime monitoring
 * Copyright (C) 2018
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.quanticol.moonlight;

import eu.quanticol.moonlight.formula.SignalDomain;
import eu.quanticol.moonlight.io.IllegalFileFormatException;
import eu.quanticol.moonlight.io.TemporalSignalReader;
import eu.quanticol.moonlight.io.TemporalSignalWriter;
import eu.quanticol.moonlight.monitoring.temporal.TemporalMonitor;
import eu.quanticol.moonlight.signal.DataHandler;
import eu.quanticol.moonlight.signal.Record;
import eu.quanticol.moonlight.signal.RecordHandler;
import eu.quanticol.moonlight.signal.Signal;

import java.io.File;
import java.io.IOException;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * An instance of this class an be used to instantiate a monitor of given property.
 */
public class TemporalScriptComponent<S> {

    private final String name;
    private final RecordHandler signalRecordHandler;
    private final RecordHandler parameters;
    private final Function<Record, TemporalMonitor<Record, S>> builder;
    private final SignalDomain<S> domain;

    /**
     * Create a new TemporalScriptComponent
     * @param name property name.
     * @param signalRecordHandler signal input data handler.
     * @param domain signal domain.
     * @param parameters parameters that can be used to instantiate.
     * @param builder function used to build the monitor.
     */
    public TemporalScriptComponent(String name, RecordHandler signalRecordHandler,
                                   SignalDomain<S> domain,
                                   RecordHandler parameters,
                                   Function<Record, TemporalMonitor<Record, S>> builder) {
        super();
        this.name = name;
        this.signalRecordHandler = signalRecordHandler;
        this.domain = domain;
        this.parameters = parameters;
        this.builder = builder;
    }

    public TemporalScriptComponent(String name, RecordHandler signalRecordHandler,
                                   SignalDomain<S> domain,
                                   Function<Record, TemporalMonitor<Record, S>> builder) {
        this(name,signalRecordHandler,domain,null,builder);
    }

        public String getName() {
        return name;
    }

    /**
     * Creates a monitor given a list of parameters as an array of strings.
     * @param values parameters.
     * @return a monitor.
     */
    public TemporalMonitor<Record, S> getMonitorFromString(String ... values) {
        if (this.parameters != null && this.parameters.size() > 0 && values.length > 0) {
            return builder.apply(parameters.fromStringArray(values));
        } else {
            return builder.apply(null);
        }
    }

    /**
     * Creates a monitor given a list of parameters as an array of doulbe.
     * @param values parameters.
     * @return a monitor.
     */
    public TemporalMonitor<Record, S> getMonitorFromDouble(double ... values) {
        if (this.parameters != null && this.parameters.size() > 0 && values.length > 0) {
            return builder.apply(parameters.fromDoubleArray(values));
        } else {
            return builder.apply(null);
        }
    }

    /**
     * Creates a monitor given a list of parameters as an array of objects.
     * @param values parameters.
     * @return a monitor.
     */
    public TemporalMonitor<Record, S> getMonitorFromObject(Object ... values) {
        if (this.parameters != null && this.parameters.size() > 0 && values.length > 0) {
            return builder.apply(parameters.fromObjectArray(values));
        } else {
            return builder.apply(null);
        }
    }

    /**
     * Computes the result of the monitoring of the proprerty instantiated with the given parameters
     * on signal input.
     *
     * @param input input signal.
     * @param values parameters.
     * @return monitor result.
     */
    public Signal<S> monitor(Signal<Record> input, String ... values) {
        TemporalMonitor<Record, S> monitor = getMonitorFromString(values);
        return monitor.monitor(input);
    }

    /**
     * Computes the result of the monitoring of the proprerty instantiated with the given parameters
     * on signal input.
     *
     * @param input input signal.
     * @param values parameters.
     * @return monitor result.
     */
    public Signal<S> monitor(Signal<Record> input, Object ... values) {
        TemporalMonitor<Record, S> monitor = getMonitorFromObject(values);
        return monitor.monitor(input);
    }

    /**
     * Computes the result of the monitoring of the proprerty instantiated with the given parameters
     * on signal input.
     *
     * @param input input signal.
     * @param values parameters.
     * @return monitor result.
     */
    public Signal<S> monitor(Signal<Record> input, double ... values) {
        TemporalMonitor<Record, S> monitor = getMonitorFromDouble(values);
        return monitor.monitor(input);
    }

    /**
     * Computes the result of the monitoring of the proprerty instantiated with the given parameters
     * on signal input. Results are reported in an array of double.
     *
     * @param input input signal.
     * @param values parameters.
     * @return monitor result.
     */
    public double[][] monitorToArray(Signal<Record> input, String ... values) {
        return monitor(input, values).arrayOf(domain.getDataHandler()::doubleOf);
    }

    /**
     * Computes the result of the monitoring of the proprerty instantiated with the given parameters
     * on signal input. Results are reported in an array of double.
     *
     * @param input input signal.
     * @param values parameters.
     * @return monitor result.
     */
    public double[][] monitorToArray(Signal<Record> input, double ... values) {
        return monitor(input, values).arrayOf(domain.getDataHandler()::doubleOf);
    }


    /**
     * Computes the result of the monitoring of the proprerty instantiated with the given parameters
     * on a signal described as a matrix of strings. Results are reported in an array of double.
     *
     * @param time input signal time intervals.
     * @param values signal values.
     * @return monitor result as an array of doubles.
     */
    public double[][] monitorToArray(double[] time, String[][] signal, String... values) {
        return monitorToArray(RecordHandler.buildTemporalSignal(signalRecordHandler, time, signal), values);
    }

    /**
     * Computes the result of the monitoring of the proprerty instantiated with the given parameters
     * on a signal described as a matrix of doubles. Results are reported in an array of double.
     *
     * @param time input signal time intervals.
     * @param values signal values.
     * @return monitor result as an array of doubles.
     */
    public double[][] monitorToArray(double[] time, double[][] signal, double ... values) {
        return monitorToArray(RecordHandler.buildTemporalSignal(signalRecordHandler, time, signal), values);
    }

    public void monitorToFile(TemporalSignalWriter writer, File output, Signal<Record> input, String ... values) throws IOException {
        Signal<S> signal = monitor(input, values);
        writer.write(domain.getDataHandler(), signal, output);
    }

    public void monitor(TemporalSignalReader reader, File input, TemporalSignalWriter writer, File output, String ... values) throws IOException, IllegalFileFormatException {
        monitorToFile(writer, output, reader.load(signalRecordHandler, input), values);
    }

    public String getInfo() {
       return getName();
    }

    public String[] getVariables() {
        return signalRecordHandler.getVariables();
    }

}
