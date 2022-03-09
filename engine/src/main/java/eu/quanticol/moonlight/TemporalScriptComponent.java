/*
 * MoonLight: a light-weight framework for runtime monitoring
 * Copyright (C) 2018-2021
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

import eu.quanticol.moonlight.core.signal.SignalDomain;
import eu.quanticol.moonlight.io.IllegalFileFormatException;
import eu.quanticol.moonlight.io.TemporalSignalReader;
import eu.quanticol.moonlight.io.TemporalSignalWriter;
import eu.quanticol.moonlight.offline.monitoring.temporal.TemporalMonitor;
import eu.quanticol.moonlight.io.MoonLightRecord;
import eu.quanticol.moonlight.offline.signal.RecordHandler;
import eu.quanticol.moonlight.offline.signal.Signal;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * An instance of this class an be used to instantiate a monitor of given property.
 */
public class TemporalScriptComponent<S> {

    private final TemporalMonitorDefinition definition;
    private final SignalDomain<S> domain;

    /**
     * Create a new TemporalScriptComponent
     * @param definition temporal monitor definition.
     * @param domain     domain of resulting signal.
     */
    public TemporalScriptComponent(TemporalMonitorDefinition definition,
                                   SignalDomain<S> domain) {
        super();
        this.definition = definition;
        this.domain = domain;
    }

    public String getName() {
        return definition.getName();
    }

    /**
     * Creates a monitor given a list of parameters as an array of strings.
     * @param values parameters.
     * @return a monitor.
     */
    public TemporalMonitor<MoonLightRecord, S> getMonitorFromString(String ... values) {
        return definition.getMonitorFromString(domain, values);
    }

    /**
     * Creates a monitor given a list of parameters as an array of doulbe.
     * @param values parameters.
     * @return a monitor.
     */
    public TemporalMonitor<MoonLightRecord, S> getMonitorFromDouble(double ... values) {
        return definition.getMonitorFromDouble(domain, values);
    }

    /**
     * Creates a monitor given a list of parameters as an array of objects.
     * @param values parameters.
     * @return a monitor.
     */
    public TemporalMonitor<MoonLightRecord, S> getMonitorFromObject(Object ... values) {
        return definition.getMonitorFromObject(domain, values);
    }

    /**
     * Computes the result of the monitoring of the proprerty instantiated with the given parameters
     * on signal input.
     *
     * @param input input signal.
     * @param values parameters.
     * @return monitor result.
     */
    public Signal<S> monitor(Signal<MoonLightRecord> input, String ... values) {
        TemporalMonitor<MoonLightRecord, S> monitor = getMonitorFromString(values);
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
    public Signal<S> monitor(Signal<MoonLightRecord> input, Object ... values) {
        TemporalMonitor<MoonLightRecord, S> monitor = getMonitorFromObject(values);
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
    public Signal<S> monitor(Signal<MoonLightRecord> input, double ... values) {
        TemporalMonitor<MoonLightRecord, S> monitor = getMonitorFromDouble(values);
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
    public double[][] monitorToArray(Signal<MoonLightRecord> input, String ... values) {
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
    public double[][] monitorToArray(Signal<MoonLightRecord> input, double ... values) {
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
        return monitorToArray(RecordHandler.buildTemporalSignal(definition.getSignalRecordHandler(), time, signal), values);
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
        return monitorToArray(RecordHandler.buildTemporalSignal(definition.getSignalRecordHandler(), time, signal), values);
    }

    public void monitorToFile(TemporalSignalWriter writer, File output, Signal<MoonLightRecord> input, String ... values) throws IOException {
        Signal<S> signal = monitor(input, values);
        writer.write(domain.getDataHandler(), signal, output);
    }

    public void monitor(TemporalSignalReader reader, File input, TemporalSignalWriter writer, File output, String ... values) throws IOException, IllegalFileFormatException {
        monitorToFile(writer, output, reader.load(definition.getSignalRecordHandler(), input), values);
    }

    public String getInfo() {
       return getName()+ Arrays.toString(getVariables());
    }

    public String[] getVariables() {
        return definition.getArguments().getVariables();
    }

    public RecordHandler getSignalHandler() {
        return this.definition.getSignalRecordHandler();
    }
}
