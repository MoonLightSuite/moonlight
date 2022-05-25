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

import eu.quanticol.moonlight.core.base.MoonLightRecord;
import eu.quanticol.moonlight.core.signal.SignalDomain;
import eu.quanticol.moonlight.core.space.LocationService;
import eu.quanticol.moonlight.io.SpatialTemporalSignalWriter;
import eu.quanticol.moonlight.offline.monitoring.spatialtemporal.SpatialTemporalMonitor;
import eu.quanticol.moonlight.offline.signal.RecordHandler;
import eu.quanticol.moonlight.offline.signal.SpatialTemporalSignal;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author loreti
 */
public class SpatialTemporalScriptComponent<S> {

    private final SpatialTemporalMonitorDefinition definition;
    private final SignalDomain<S> domain;

    public SpatialTemporalScriptComponent(
            SpatialTemporalMonitorDefinition definition,
            SignalDomain<S> domain) {
        super();
        this.definition = definition;
        this.domain = domain;
    }

    public double[][][] monitorToArrayFromDouble(LocationService<Double, MoonLightRecord> locations, SpatialTemporalSignal<MoonLightRecord> input, double... parameters) {
        return monitorFromDouble(locations, input, parameters).toArray(domain.getDataHandler()::doubleOf);
    }

    public SpatialTemporalSignal<S> monitorFromDouble(LocationService<Double, MoonLightRecord> locations, SpatialTemporalSignal<MoonLightRecord> input, double... parameters) {
        SpatialTemporalMonitor<MoonLightRecord, MoonLightRecord, S> monitor = getMonitor(parameters);
        return monitor.monitor(locations, input);
    }

    public SpatialTemporalMonitor<MoonLightRecord, MoonLightRecord, S> getMonitor(double... values) {
        return definition.getMonitorFromDouble(domain, values);
    }

    public double[][][] monitorToObjectArrayAdjacencyMatrix(double[] locationTimeArray, String[][][][] graph, double[] signalTimeArray, String[][][] signalValues, String... parameters) {
        int locations = signalValues.length;
        SpatialTemporalSignal<MoonLightRecord> signal = RecordHandler.buildSpatioTemporalSignal(locations, definition.getSignalRecordHandler(), signalTimeArray, signalValues);
        LocationService<Double, MoonLightRecord> locationService = LocationService.buildLocationServiceFromAdjacencyMatrix(locations, definition.getEdgeRecordHandler(), locationTimeArray, graph);
        return monitorToArrayFromString(locationService, signal, parameters);
    }

    public double[][][] monitorToArrayFromString(LocationService<Double, MoonLightRecord> locations, SpatialTemporalSignal<MoonLightRecord> input, String... parameters) {
        return monitorFromString(locations, input, parameters).toArray(domain.getDataHandler()::doubleOf);
    }

    public SpatialTemporalSignal<S> monitorFromString(LocationService<Double, MoonLightRecord> locations, SpatialTemporalSignal<MoonLightRecord> input, String... parameters) {
        SpatialTemporalMonitor<MoonLightRecord, MoonLightRecord, S> monitor = getMonitor(parameters);
        return monitor.monitor(locations, input);
    }

    public SpatialTemporalMonitor<MoonLightRecord, MoonLightRecord, S> getMonitor(String... values) {
        return definition.getMonitorFromString(domain, values);
    }

    public double[][][] monitorToObjectArrayAdjacencyMatrix(String[][][] graph, double[] signalTimeArray, String[][][] signalValues, String... parameters) {
        int locations = signalValues.length;
        SpatialTemporalSignal<MoonLightRecord> signal = RecordHandler.buildSpatioTemporalSignal(locations, definition.getSignalRecordHandler(), signalTimeArray, signalValues);
        LocationService<Double, MoonLightRecord> locationService = LocationService.buildLocationServiceFromAdjacencyMatrix(locations, definition.getEdgeRecordHandler(), signalTimeArray[0], graph);
        return monitorToArrayFromString(locationService, signal, parameters);
    }

    public double[][][] monitorToObjectArrayAdjacencyMatrix(double[] locationTimeArray, double[][][][] graph, double[] signalTimeArray, double[][][] signalValues, double... parameters) {
        int locations = signalValues.length;
        SpatialTemporalSignal<MoonLightRecord> signal = RecordHandler.buildSpatioTemporalSignal(locations, definition.getSignalRecordHandler(), signalTimeArray, signalValues);
        LocationService<Double, MoonLightRecord> locationService = LocationService.buildLocationServiceFromAdjacencyMatrix(locations, definition.getEdgeRecordHandler(), locationTimeArray, graph);
        return monitorFromDouble(locationService, signal, parameters).toArray(domain.getDataHandler()::doubleOf);
    }

    public double[][][] monitorToObjectArrayAdjacencyMatrix(double[][][] graph, double[] signalTimeArray, double[][][] signalValues, double... parameters) {
        int locations = signalValues.length;
        SpatialTemporalSignal<MoonLightRecord> signal = RecordHandler.buildSpatioTemporalSignal(locations, definition.getSignalRecordHandler(), signalTimeArray, signalValues);
        LocationService<Double, MoonLightRecord> locationService = LocationService.buildLocationServiceFromAdjacencyMatrix(locations, definition.getEdgeRecordHandler(), signalTimeArray[0], graph);
        return monitorFromDouble(locationService, signal, parameters).toArray(domain.getDataHandler()::doubleOf);
    }

    public double[][][] monitorToDoubleArrayAdjacencyList(double[] locationTimeArray, String[][][] graph, double[] signalTimeArray, String[][][] signalValues, String... parameters) {
        int locations = signalValues.length;
        SpatialTemporalSignal<MoonLightRecord> signal = RecordHandler.buildSpatioTemporalSignal(locations, definition.getSignalRecordHandler(), signalTimeArray, signalValues);
        LocationService<Double, MoonLightRecord> locationService = LocationService.buildLocationServiceFromAdjacencyList(locations, definition.getEdgeRecordHandler(), locationTimeArray, graph);
        return monitorToArrayFromString(locationService, signal, parameters);
    }

    public double[][][] monitorToDoubleArrayAdjacencyList(String[][] graph, double[] signalTimeArray, String[][][] signalValues, String... parameters) {
        int locations = signalValues.length;
        SpatialTemporalSignal<MoonLightRecord> signal = RecordHandler.buildSpatioTemporalSignal(locations, definition.getSignalRecordHandler(), signalTimeArray, signalValues);
        LocationService<Double, MoonLightRecord> locationService = LocationService.buildLocationServiceFromAdjacencyList(locations, definition.getEdgeRecordHandler(), signalTimeArray[0], graph);
        return monitorToArrayFromString(locationService, signal, parameters);
    }

    public double[][][] monitorToDoubleArrayAdjacencyList(double[] locationTimeArray, double[][][] graph, double[] signalTimeArray, double[][][] signalValues, double... parameters) {
        int locations = signalValues.length;
        SpatialTemporalSignal<MoonLightRecord> signal = RecordHandler.buildSpatioTemporalSignal(locations, definition.getSignalRecordHandler(), signalTimeArray, signalValues);
        LocationService<Double, MoonLightRecord> locationService = LocationService.buildLocationServiceFromAdjacencyList(locations, definition.getEdgeRecordHandler(), locationTimeArray, graph);
        return monitorFromDouble(locationService, signal, parameters).toArray(domain.getDataHandler()::doubleOf);
    }

    public double[][][] monitorToObjectArrayAdjacencyListWithPrint(double[] locationTimeArray, double[][][] graph, double[] signalTimeArray, double[][][] signalValues, double... parameters) {
        System.out.println("times = new double[]" + Arrays.toString(locationTimeArray).replace("[", "{").replace("]", "}"));
        System.out.println("graph = new double[][][]" + Arrays.deepToString(graph).replace("[", "{").replace("]", "}"));
        System.out.println("signalTimeArray = new double[]" + Arrays.toString(signalTimeArray).replace("[", "{").replace("]", "}"));
        System.out.println("signalValues = new double[][][]" + Arrays.deepToString(signalValues).replace("[", "{").replace("]", "}"));
        int locations = signalValues.length;
        SpatialTemporalSignal<MoonLightRecord> signal = RecordHandler.buildSpatioTemporalSignal(locations, definition.getSignalRecordHandler(), signalTimeArray, signalValues);
        LocationService<Double, MoonLightRecord> locationService = LocationService.buildLocationServiceFromAdjacencyList(locations, definition.getEdgeRecordHandler(), locationTimeArray, graph);
        return monitorFromDouble(locationService, signal, parameters).toArray(domain.getDataHandler()::doubleOf);
    }

    public double[][][] monitorToDoubleArrayAdjacencyList(double[][] graph, double[] signalTimeArray, double[][][] signalValues, double... parameters) {
        int locations = signalValues.length;
        SpatialTemporalSignal<MoonLightRecord> signal = RecordHandler.buildSpatioTemporalSignal(locations, definition.getSignalRecordHandler(), signalTimeArray, signalValues);
        LocationService<Double, MoonLightRecord> locationService = LocationService.buildLocationServiceFromAdjacencyList(locations, definition.getEdgeRecordHandler(), signalTimeArray[0], graph);
        return monitorFromDouble(locationService, signal, parameters).toArray(domain.getDataHandler()::doubleOf);
    }

    public void monitorToFile(SpatialTemporalSignalWriter writer, File output, LocationService<Double, MoonLightRecord> locations, SpatialTemporalSignal<MoonLightRecord> input, String... values) throws IOException {
        SpatialTemporalSignal<S> signal = monitorFromString(locations, input, values);
        writer.write(domain.getDataHandler(), signal, output);

    }

    public void monitorToFile(SpatialTemporalSignalWriter writer, File output, LocationService<Double, MoonLightRecord> locations, SpatialTemporalSignal<MoonLightRecord> input, double... values) throws IOException {
        SpatialTemporalSignal<S> signal = monitorFromDouble(locations, input, values);
        writer.write(domain.getDataHandler(), signal, output);
    }

    public String getInfo() {
        return getName();
    }

    public String getName() {
        return definition.getName();
    }

    public String[] getVariables() {
        return definition.getSignalRecordHandler().getVariables();
    }

    public RecordHandler getSignalHandler() {
        return this.definition.getSignalRecordHandler();
    }

    public RecordHandler getEdgeHandler() {
        return this.definition.getEdgeRecordHandler();
    }
}
