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

package eu.quanticol.moonlight.io;

import eu.quanticol.moonlight.offline.signal.DataHandler;
import eu.quanticol.moonlight.offline.signal.SpatialTemporalSignal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * This class is used to
 */
public class CsvSpatialTemporalSignalWriter implements SpatialTemporalSignalWriter {

    @Override
    public <S> void write(DataHandler<S> handler, SpatialTemporalSignal<S> signal, File file) throws IOException {
        Files.write(file.toPath(),stringOf(handler,signal).getBytes());
    }

    @Override
    public <S> String stringOf(DataHandler<S> handler, SpatialTemporalSignal<S> signal) {
        double[] timePoints = signal.getTimeArray();
        String[][] elements = new String[signal.size()][timePoints.length];
        signal.fill(timePoints,elements,handler::stringOf);
        return combine(signal.size(),timePoints,elements);
    }

    private String combine(int size, double[] timePoints, String[][] elements) {
        StringBuilder toReturn = new StringBuilder();
        toReturn.append("LOCATIONS "+size+"\n");
        for( int i=0 ; i<timePoints.length ; i++ ) {
            StringBuilder row = new StringBuilder(timePoints[i] + "");
            for( int j=0 ; j<size ; j++ ) {
                row.append(";").append(elements[j][i]);
            }
            toReturn.append(row).append("\n");
        }
        return toReturn.toString();
    }


//    @Override
//    public <S> void write(DataHandler<S> handler, Signal<S> signal, File file) throws IOException {
//        Files.write(file.toPath(),stringOf(handler,signal).getBytes());
//    }
//
//    @Override
//    public <S> String stringOf(DataHandler<S> handler, Signal<S> signal) {
//        return signal.reduce((p,s) -> s+p.getFirst ()+";"+handler.toString(p.getSecond())+"\n","");
//    }

}
