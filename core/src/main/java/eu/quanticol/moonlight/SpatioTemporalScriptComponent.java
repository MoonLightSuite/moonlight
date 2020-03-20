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

import eu.quanticol.moonlight.io.SpatioTemporalSignalWriter;
import eu.quanticol.moonlight.monitoring.spatiotemporal.SpatioTemporalMonitor;
import eu.quanticol.moonlight.signal.*;

import java.io.IOException;
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

    public SpatioTemporalMonitor<Record, Record, S> getMonitor(String ... values) {
        if (this.parameters != null && this.parameters.size()>0 && values.length > 0) {
            return builder.apply(parameters.fromString(values));
        } else {
            return builder.apply(null);
        }
    }

    public SpatioTemporalMonitor<Record, Record, S> getMonitor(double ... values) {
        if (this.parameters != null && this.parameters.size()>0 && values.length > 0) {
            return builder.apply(parameters.fromDouble(values));
        } else {
            return builder.apply(null);
        }
    }


    public SpatioTemporalSignal<S> monitorFromString(LocationService<Record> locations, SpatioTemporalSignal<Record> input, String ... parameters) {
        SpatioTemporalMonitor<Record, Record, S> monitor = getMonitor(parameters);
        return monitor.monitor(locations, input);
    }

    public SpatioTemporalSignal<S> monitorFromDouble(LocationService<Record> locations, SpatioTemporalSignal<Record> input, double ... parameters) {
        SpatioTemporalMonitor<Record, Record, S> monitor = getMonitor(parameters);
        return monitor.monitor(locations, input);
    }

    public double[][][] monitorToArrayFromString(LocationService<Record> locations, SpatioTemporalSignal<Record> input, String ... parameters) {
        return monitorFromString(locations, input, parameters).toArray(outputTypeHandler::toDouble);
    }

    public double[][][] monitorToArrayFromDouble(LocationService<Record> locations, SpatioTemporalSignal<Record> input, double ... parameters) {
        return monitorFromDouble(locations, input, parameters).toArray(outputTypeHandler::toDouble);
    }


    public double[][][] monitorToObjectArrayAdjacencyMatrix(double[] locationTimeArray, String[][][][] graph, double[] signalTimeArray, String[][][] signalValues, String... parameters) {
        int locations = signalValues.length;
        SpatioTemporalSignal<Record> signal = RecordHandler.buildSpatioTemporalSignal(locations, signalRecordHandler, signalTimeArray, signalValues);
        LocationService<Record> locationService = LocationService.buildLocationServiceFromAdjacencyMatrix(locations, edgeRecordHandler, locationTimeArray, graph);
        return monitorToArrayFromString(locationService,signal,parameters);
    }

    public double[][][] monitorToObjectArrayAdjacencyMatrix(String[][][] graph, double[] signalTimeArray, String[][][] signalValues, String... parameters) {
        int locations = signalValues.length;
        SpatioTemporalSignal<Record> signal = RecordHandler.buildSpatioTemporalSignal(locations, signalRecordHandler, signalTimeArray, signalValues);
        LocationService<Record> locationService = LocationService.buildLocationServiceFromAdjacencyMatrix(locations, edgeRecordHandler, signalTimeArray[0], graph);
        return monitorToArrayFromString(locationService,signal,parameters);
    }

    public double[][][] monitorToObjectArrayAdjacencyMatrix(double[] locationTimeArray, double[][][][] graph, double[] signalTimeArray, double[][][] signalValues, double... parameters) {
        int locations = signalValues.length;
        SpatioTemporalSignal<Record> signal = RecordHandler.buildSpatioTemporalSignal(locations, signalRecordHandler, signalTimeArray, signalValues);
        LocationService<Record> locationService = LocationService.buildLocationServiceFromAdjacencyMatrix(locations, edgeRecordHandler, locationTimeArray, graph);
        return monitorFromDouble(locationService, signal, parameters).toArray(outputTypeHandler::toDouble);
    }

    public double[][][] monitorToObjectArrayAdjacencyMatrix(double[][][] graph, double[] signalTimeArray, double[][][] signalValues, double ... parameters) {
        int locations = signalValues.length;
        SpatioTemporalSignal<Record> signal = RecordHandler.buildSpatioTemporalSignal(locations, signalRecordHandler, signalTimeArray, signalValues);
        LocationService<Record> locationService = LocationService.buildLocationServiceFromAdjacencyMatrix(locations, edgeRecordHandler, signalTimeArray[0], graph);
        return monitorFromDouble(locationService, signal, parameters).toArray(outputTypeHandler::toDouble);
    }

    public double[][][] monitorToObjectArrayAdjacencyList(double[] locationTimeArray, String[][][] graph, double[] signalTimeArray, String[][][] signalValues, String... parameters) {
        int locations = signalValues.length;
        SpatioTemporalSignal<Record> signal = RecordHandler.buildSpatioTemporalSignal(locations, signalRecordHandler, signalTimeArray, signalValues);
        LocationService<Record> locationService = LocationService.buildLocationServiceFromAdjacencyList(locations, edgeRecordHandler, locationTimeArray, graph);
        return monitorToArrayFromString(locationService,signal,parameters);
    }

    public double[][][] monitorToObjectArrayAdjacencyList(String[][] graph, double[] signalTimeArray, String[][][] signalValues, String... parameters) {
        int locations = signalValues.length;
        SpatioTemporalSignal<Record> signal = RecordHandler.buildSpatioTemporalSignal(locations, signalRecordHandler, signalTimeArray, signalValues);
        LocationService<Record> locationService = LocationService.buildLocationServiceFromAdjacencyList(locations, edgeRecordHandler, signalTimeArray[0], graph);
        return monitorToArrayFromString(locationService,signal,parameters);
    }

    public double[][][] monitorToObjectArrayAdjacencyList(double[] locationTimeArray, double[][][] graph, double[] signalTimeArray, double[][][] signalValues, double... parameters) {
        int locations = signalValues.length;
        SpatioTemporalSignal<Record> signal = RecordHandler.buildSpatioTemporalSignal(locations, signalRecordHandler, signalTimeArray, signalValues);
        LocationService<Record> locationService = LocationService.buildLocationServiceFromAdjacencyList(locations, edgeRecordHandler, locationTimeArray, graph);
        return monitorFromDouble(locationService, signal, parameters).toArray(outputTypeHandler::toDouble);
    }

    public double[][][] monitorToObjectArrayAdjacencyList(double[][] graph, double[] signalTimeArray, double[][][] signalValues, double ... parameters) {
        int locations = signalValues.length;
        SpatioTemporalSignal<Record> signal = RecordHandler.buildSpatioTemporalSignal(locations, signalRecordHandler, signalTimeArray, signalValues);
        LocationService<Record> locationService = LocationService.buildLocationServiceFromAdjacencyList(locations, edgeRecordHandler, signalTimeArray[0], graph);
        return monitorFromDouble(locationService, signal, parameters).toArray(outputTypeHandler::toDouble);
    }

    public void monitorToFile(SpatioTemporalSignalWriter writer, OutputStream stream, LocationService<Record> locations, SpatioTemporalSignal<Record> input, String... values) throws IOException {
        SpatioTemporalSignal<S> signal = monitorFromString(locations, input, values);
        writer.write(outputTypeHandler, signal, stream);
    }

    public void monitorToFile(SpatioTemporalSignalWriter writer, OutputStream stream, LocationService<Record> locations, SpatioTemporalSignal<Record> input, double ... values) throws IOException {
        SpatioTemporalSignal<S> signal = monitorFromDouble(locations, input, values);
        writer.write(outputTypeHandler, signal, stream);
    }

    public String getInfo() {
        return this.toString();
    }

    public String[] getVariables() {
        return signalRecordHandler.getVariables();
    }
}
