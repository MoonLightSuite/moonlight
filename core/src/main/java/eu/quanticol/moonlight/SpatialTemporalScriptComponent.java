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
import eu.quanticol.moonlight.io.SpatialTemporalSignalWriter;
import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor;
import eu.quanticol.moonlight.signal.LocationService;
import eu.quanticol.moonlight.signal.Record;
import eu.quanticol.moonlight.signal.RecordHandler;
import eu.quanticol.moonlight.signal.SpatialTemporalSignal;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.Function;

/**
 * @author loreti
 */
public class SpatialTemporalScriptComponent<S> {

    private final String name;
    private final RecordHandler signalRecordHandler;
    private final RecordHandler edgeRecordHandler;
    private final SignalDomain<S> domain;
    private final RecordHandler parameters;
    private final Function<Record, SpatialTemporalMonitor<Record, Record, S>> builder;

    public SpatialTemporalScriptComponent(
            String name,
            RecordHandler edgeRecordHandler,
            RecordHandler signalRecordHandler,
            SignalDomain<S> domain,
            RecordHandler parameters,
            Function<Record, SpatialTemporalMonitor<Record, Record, S>> builder) {
        super();
        this.name = name;
        this.signalRecordHandler = signalRecordHandler;
        this.domain = domain;
        this.edgeRecordHandler = edgeRecordHandler;
        this.parameters = parameters;
        this.builder = builder;
    }

    public SpatialTemporalScriptComponent(
            String name,
            RecordHandler edgeRecordHandler,
            RecordHandler signalRecordHandler,
            SignalDomain<S> domain,
            Function<Record, SpatialTemporalMonitor<Record, Record, S>> builder) {
        this(name, edgeRecordHandler, signalRecordHandler, domain, null, builder);
    }

    public String getName() {
        return name;
    }

    public SpatialTemporalMonitor<Record, Record, S> getMonitor(String... values) {
        if (this.parameters != null && this.parameters.size() > 0 && values.length > 0) {
            return builder.apply(parameters.fromStringArray(values));
        } else {
            return builder.apply(null);
        }
    }

    public SpatialTemporalMonitor<Record, Record, S> getMonitor(double... values) {
        if (this.parameters != null && this.parameters.size() > 0 && values.length > 0) {
            return builder.apply(parameters.fromDoubleArray(values));
        } else {
            return builder.apply(null);
        }
    }


    public SpatialTemporalSignal<S> monitorFromString(LocationService<Record> locations, SpatialTemporalSignal<Record> input, String... parameters) {
        SpatialTemporalMonitor<Record, Record, S> monitor = getMonitor(parameters);
        return monitor.monitor(locations, input);
    }

    public SpatialTemporalSignal<S> monitorFromDouble(LocationService<Record> locations, SpatialTemporalSignal<Record> input, double... parameters) {
        SpatialTemporalMonitor<Record, Record, S> monitor = getMonitor(parameters);
        return monitor.monitor(locations, input);
    }

    public double[][][] monitorToArrayFromString(LocationService<Record> locations, SpatialTemporalSignal<Record> input, String... parameters) {
        return monitorFromString(locations, input, parameters).toArray(domain.getDataHandler()::doubleOf);
    }

    public double[][][] monitorToArrayFromDouble(LocationService<Record> locations, SpatialTemporalSignal<Record> input, double... parameters) {
        return monitorFromDouble(locations, input, parameters).toArray(domain.getDataHandler()::doubleOf);
    }


    public double[][][] monitorToObjectArrayAdjacencyMatrix(double[] locationTimeArray, String[][][][] graph, double[] signalTimeArray, String[][][] signalValues, String... parameters) {
        int locations = signalValues.length;
        SpatialTemporalSignal<Record> signal = RecordHandler.buildSpatioTemporalSignal(locations, signalRecordHandler, signalTimeArray, signalValues);
        LocationService<Record> locationService = LocationService.buildLocationServiceFromAdjacencyMatrix(locations, edgeRecordHandler, locationTimeArray, graph);
        return monitorToArrayFromString(locationService, signal, parameters);
    }

    public double[][][] monitorToObjectArrayAdjacencyMatrix(String[][][] graph, double[] signalTimeArray, String[][][] signalValues, String... parameters) {
        int locations = signalValues.length;
        SpatialTemporalSignal<Record> signal = RecordHandler.buildSpatioTemporalSignal(locations, signalRecordHandler, signalTimeArray, signalValues);
        LocationService<Record> locationService = LocationService.buildLocationServiceFromAdjacencyMatrix(locations, edgeRecordHandler, signalTimeArray[0], graph);
        return monitorToArrayFromString(locationService, signal, parameters);
    }

    public double[][][] monitorToObjectArrayAdjacencyMatrix(double[] locationTimeArray, double[][][][] graph, double[] signalTimeArray, double[][][] signalValues, double... parameters) {
        int locations = signalValues.length;
        SpatialTemporalSignal<Record> signal = RecordHandler.buildSpatioTemporalSignal(locations, signalRecordHandler, signalTimeArray, signalValues);
        LocationService<Record> locationService = LocationService.buildLocationServiceFromAdjacencyMatrix(locations, edgeRecordHandler, locationTimeArray, graph);
        return monitorFromDouble(locationService, signal, parameters).toArray(domain.getDataHandler()::doubleOf);
    }

    public double[][][] monitorToObjectArrayAdjacencyMatrix(double[][][] graph, double[] signalTimeArray, double[][][] signalValues, double... parameters) {
        int locations = signalValues.length;
        SpatialTemporalSignal<Record> signal = RecordHandler.buildSpatioTemporalSignal(locations, signalRecordHandler, signalTimeArray, signalValues);
        LocationService<Record> locationService = LocationService.buildLocationServiceFromAdjacencyMatrix(locations, edgeRecordHandler, signalTimeArray[0], graph);
        return monitorFromDouble(locationService, signal, parameters).toArray(domain.getDataHandler()::doubleOf);
    }

    public double[][][] monitorToObjectArrayAdjacencyList(double[] locationTimeArray, String[][][] graph, double[] signalTimeArray, String[][][] signalValues, String... parameters) {
        int locations = signalValues.length;
        SpatialTemporalSignal<Record> signal = RecordHandler.buildSpatioTemporalSignal(locations, signalRecordHandler, signalTimeArray, signalValues);
        LocationService<Record> locationService = LocationService.buildLocationServiceFromAdjacencyList(locations, edgeRecordHandler, locationTimeArray, graph);
        return monitorToArrayFromString(locationService, signal, parameters);
    }

    public double[][][] monitorToObjectArrayAdjacencyList(String[][] graph, double[] signalTimeArray, String[][][] signalValues, String... parameters) {
        int locations = signalValues.length;
        SpatialTemporalSignal<Record> signal = RecordHandler.buildSpatioTemporalSignal(locations, signalRecordHandler, signalTimeArray, signalValues);
        LocationService<Record> locationService = LocationService.buildLocationServiceFromAdjacencyList(locations, edgeRecordHandler, signalTimeArray[0], graph);
        return monitorToArrayFromString(locationService, signal, parameters);
    }

    public double[][][] monitorToObjectArrayAdjacencyList(double[] locationTimeArray, double[][][] graph, double[] signalTimeArray, double[][][] signalValues, double... parameters) {
        System.out.println("times = new double[]" + Arrays.toString(locationTimeArray).replace("[", "{").replace("]", "}"));
        System.out.println("graph = new double[][][]" + Arrays.deepToString(graph).replace("[", "{").replace("]", "}"));
        System.out.println("signalTimeArray = new double[]" + Arrays.toString(signalTimeArray).replace("[", "{").replace("]", "}"));
        System.out.println("signalValues = new double[][][]" + Arrays.deepToString(signalValues).replace("[", "{").replace("]", "}"));
        int locations = signalValues.length;
        SpatialTemporalSignal<Record> signal = RecordHandler.buildSpatioTemporalSignal(locations, signalRecordHandler, signalTimeArray, signalValues);
        LocationService<Record> locationService = LocationService.buildLocationServiceFromAdjacencyList(locations, edgeRecordHandler, locationTimeArray, graph);
        return monitorFromDouble(locationService, signal, parameters).toArray(domain.getDataHandler()::doubleOf);
    }

    public double[][][] monitorToObjectArrayAdjacencyList(double[][] graph, double[] signalTimeArray, double[][][] signalValues, double... parameters) {
        int locations = signalValues.length;
        SpatialTemporalSignal<Record> signal = RecordHandler.buildSpatioTemporalSignal(locations, signalRecordHandler, signalTimeArray, signalValues);
        LocationService<Record> locationService = LocationService.buildLocationServiceFromAdjacencyList(locations, edgeRecordHandler, signalTimeArray[0], graph);
        return monitorFromDouble(locationService, signal, parameters).toArray(domain.getDataHandler()::doubleOf);
    }

    public void monitorToFile(SpatialTemporalSignalWriter writer, File output, LocationService<Record> locations, SpatialTemporalSignal<Record> input, String... values) throws IOException {
        SpatialTemporalSignal<S> signal = monitorFromString(locations, input, values);
        writer.write(domain.getDataHandler(), signal, output);

    }

    public void monitorToFile(SpatialTemporalSignalWriter writer, File output, LocationService<Record> locations, SpatialTemporalSignal<Record> input, double... values) throws IOException {
        SpatialTemporalSignal<S> signal = monitorFromDouble(locations, input, values);
        writer.write(domain.getDataHandler(), signal, output);
    }

    public String getInfo() {
        return getName();
    }

    public String[] getVariables() {
        return signalRecordHandler.getVariables();
    }
}
