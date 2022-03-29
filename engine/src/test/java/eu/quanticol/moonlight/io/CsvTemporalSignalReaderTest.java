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

import eu.quanticol.moonlight.core.base.MoonLightRecord;
import eu.quanticol.moonlight.core.io.DataHandler;
import eu.quanticol.moonlight.offline.signal.RecordHandler;
import eu.quanticol.moonlight.offline.signal.Signal;
import eu.quanticol.moonlight.util.SignalGenerator;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.function.DoubleFunction;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class CsvTemporalSignalReaderTest {

    @Test
    void loadFromCSV() throws IllegalFileFormatException {
        String simple = "0.0;1;2;\n0.4;1;2;\n0.8;1;2;\n1.0;1;2;\n";
        RecordHandler rh = new RecordHandler(DataHandler.INTEGER,DataHandler.REAL);
        CsvTemporalSignalReader reader = new CsvTemporalSignalReader();
        Signal<MoonLightRecord> signal = reader.load(rh,simple);

        assertTrue(true);
    }

    @Test
    void loadMalformed() throws IllegalFileFormatException {
        String simple = "error;1;2;\n0.4;1;2;\n0.8;1;2;\nerror;1;2;\n";
        RecordHandler rh = new RecordHandler(DataHandler.INTEGER,DataHandler.REAL);
        CsvTemporalSignalReader reader = new CsvTemporalSignalReader();
        IllegalFileFormatException e = assertThrows(IllegalFileFormatException.class, () -> reader.load(rh,simple));
        assertEquals(1, e.getLine());
    }

    @Test
    void loadFromCSVEmptyLineIgnored() throws IllegalFileFormatException {
        String simple = "0.0;1;2;\n\n\n   \n\n0.4;1;2;\n0.8;1;2;\n1.0;1;2;\n";
        RecordHandler rh = new RecordHandler(DataHandler.INTEGER,DataHandler.REAL);
        CsvTemporalSignalReader reader = new CsvTemporalSignalReader();
        Signal<MoonLightRecord> signal = reader.load(rh,simple);

        assertTrue(true);
    }

    @Test
    void testComplexCSV() throws IllegalFileFormatException {
        String csv = "0.0;4.440193097192868;4;false\n" +
                "0.3;-9.449358648526411;-2;false\n" +
                "0.6;2.4619398262437997;6;true\n" +
                "0.8999999999999999;-7.838017737739646;8;false\n" +
                "1.2;-0.06019311677629524;-5;false\n" +
                "1.5;-8.240903810326028;2;true\n" +
                "1.8;1.3877930055159702;6;true\n" +
                "2.1;7.25790713768593;-2;true\n" +
                "2.4;-1.7351430992415402;9;false\n" +
                "2.6999999999999997;-7.764289161127127;5;true\n";
        RecordHandler rh = new RecordHandler(DataHandler.REAL,DataHandler.INTEGER,DataHandler.BOOLEAN);
        CsvTemporalSignalReader reader = new CsvTemporalSignalReader();
        Signal<MoonLightRecord>  signal = reader.load(rh,csv);
        MoonLightRecord r = signal.getValueAt(0.0);
        System.out.println(r);
        assertEquals(4.440193097192868,r.get(0,Double.class));
        assertEquals(4,r.get(1,Integer.class));
        assertEquals(false,r.get(2,Boolean.class));
    }

    @Test
    void randomTest() throws IllegalFileFormatException {
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
        for( int i=0 ; i<size ; i++ ) {
            MoonLightRecord expected = data[i];
            MoonLightRecord actual = signal.getValueAt(timePoints[i]);
            System.out.println(expected);
            System.out.println(actual);
            assertEquals(expected.get(0),actual.get(0));
            assertEquals(expected.get(1),actual.get(1));
            assertEquals(expected.get(2),actual.get(2));
            assertEquals(expected,actual);
        }
    }

    String generateCSV(int size, IntFunction<String> rowGenerator) {
        return IntStream.range(0,size).mapToObj(rowGenerator).collect(Collectors.joining());
    }



}