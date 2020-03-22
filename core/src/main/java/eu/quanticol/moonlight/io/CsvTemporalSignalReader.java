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

package eu.quanticol.moonlight.io;

import eu.quanticol.moonlight.io.json.IllegalFileFormat;
import eu.quanticol.moonlight.signal.Record;
import eu.quanticol.moonlight.signal.RecordHandler;
import eu.quanticol.moonlight.signal.Signal;
import eu.quanticol.moonlight.util.Pair;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * This class is used to read a temporal signal from a CSV file or a string.
 * The file format consits of a sequence of data separated by a ';', the first of which must
 * be a double. The first column must be ordered.
 */
public class CsvTemporalSignalReader implements TemporalSignalLoader {

    @Override
    public Signal<Record> load(RecordHandler handler, File input) throws IOException, IllegalFileFormatException {
        return load(handler, Files.lines(input.toPath()));
    }

    private Signal<Record> load(RecordHandler handler, Stream<String> lines) throws IllegalFileFormatException {
        List<Row> data = lines.map(s -> new Row(s)).collect(Collectors.toList());
        int line = 1;
        for (Row r: data) {
            r.setLine(line++);
        }
        checkData(handler,data);
        Signal<Record> s = new Signal<>();
        for (Row row: data) {
            row.addValueToSignal(handler,s);
        }
        s.end();
        return s;
    }

    private void checkData(RecordHandler handler, List<Row> data) throws IllegalFileFormatException {
        for (Row row: data) {
            if (!row.firstIsADouble()) {
                throw new IllegalFileFormatException(row.index, "First element of each row must be a double!");
            }
            if (!row.checkRecord(handler)) {
                throw new IllegalFileFormatException(row.index,"Input data error!");
            }
        }
    }

    private boolean isDouble( String str ) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public Signal<Record> load(RecordHandler handler, String input) throws IllegalFileFormatException {
        return load(handler,Stream.of(input.split("\n")));
    }

    public class Row {
        int index;
        String[] elements;

        public Row(String row) {
            if (row.trim().isEmpty()) {
                elements = null;
            } else {
                elements = row.split(";");
            }
        }

        public void setLine(int index) {
            this.index = index;
        }

        public boolean firstIsADouble() {
            if (elements == null) {
                return true;
            }
            if (elements.length>0) {
                return isDouble(elements[0]);
            }
            return false;
        }

        public boolean checkRecord(RecordHandler handler) {
            return (elements == null)||handler.checkValuesFromStrings(elements,1,elements.length);
        }

        public void addValueToSignal(RecordHandler handler, Signal<Record> s) {
            if (elements != null) {
                double t = Double.parseDouble(elements[0]);
                Record r = handler.fromString(elements,1,elements.length);
                s.add(t,r);
            }
        }
    }
}
