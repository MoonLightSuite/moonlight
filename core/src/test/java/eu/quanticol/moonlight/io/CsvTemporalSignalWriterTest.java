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

import eu.quanticol.moonlight.signal.DataHandler;
import eu.quanticol.moonlight.space.MoonLightRecord;
import eu.quanticol.moonlight.signal.RecordHandler;
import eu.quanticol.moonlight.signal.Signal;
import eu.quanticol.moonlight.util.SignalGenerator;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.function.DoubleFunction;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class CsvTemporalSignalWriterTest {

    @Test
    void testWriteAndRead() throws IllegalFileFormatException {
        String input = "0.0;1;2.0\n0.4;2;2.1\n0.8;3;3.1\n1.0;4;4.1\n";
        RecordHandler rh = new RecordHandler(DataHandler.INTEGER,DataHandler.REAL);

        CsvTemporalSignalReader reader = new CsvTemporalSignalReader();
        Signal<MoonLightRecord> signal = reader.load(rh,input);
        CsvTemporalSignalWriter writer = new CsvTemporalSignalWriter();
        String output = writer.stringOf(rh,signal);

        assertEquals(input,output);
    }

    @Test
    void randomTestone() throws IllegalFileFormatException {
        Random r = new Random(100);
        for( int i=0 ; i<10 ; i++ ) {
            singleRandomTest(r,10);
        }
    }

    void singleRandomTest( Random r , int size ) throws IllegalFileFormatException {
        RecordHandler rh = new RecordHandler(DataHandler.REAL,DataHandler.INTEGER,DataHandler.BOOLEAN);
        double[] timePoints = new double[size];
        DoubleFunction<Double> realGenerator = SignalGenerator.realGenerator(r,-10,10);
        DoubleFunction<Integer> intGenerator = SignalGenerator.integerGenerator(r,-10,10);
        DoubleFunction<Boolean> boolGenerator = SignalGenerator.booleanGenerator(r,0.5);

        MoonLightRecord[] data = new MoonLightRecord[size];
        DoubleFunction<MoonLightRecord> generateFunction = d -> rh.fromObjectArray(
                realGenerator.apply(d), intGenerator.apply(d),boolGenerator.apply(d)
        );
        SignalGenerator.fillArray(timePoints,data, generateFunction,0.0,d -> 0.3);
        String csv = generateCSV(size,i->timePoints[i]+";"+data[i]+"\n");
        CsvTemporalSignalReader reader = new CsvTemporalSignalReader();
        Signal<MoonLightRecord>  signal = reader.load(rh,csv);
        CsvTemporalSignalWriter writer = new CsvTemporalSignalWriter();
        String output = writer.stringOf(rh,signal);
        assertEquals(csv,output);
    }

    String generateCSV(int size, IntFunction<String> rowGenerator) {
        return IntStream.range(0,size).mapToObj(rowGenerator).collect(Collectors.joining());
    }

}