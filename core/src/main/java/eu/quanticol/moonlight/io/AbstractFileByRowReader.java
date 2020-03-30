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

import eu.quanticol.moonlight.signal.Record;
import eu.quanticol.moonlight.signal.RecordHandler;
import eu.quanticol.moonlight.signal.Signal;
import eu.quanticol.moonlight.signal.SpatialTemporalSignal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AbstractFileByRowReader {

    protected List<Row> getRows( File input ) throws IOException {
        return collectDataRows(Files.lines(input.toPath()));
    }

    protected List<Row> getRows( String input ) {
        return collectDataRows(Stream.of(input.split("\n")));
    }

    protected List<Row> collectDataRows(Stream<String> lines ) {
        List<Row> data = lines.map(s -> new Row(s)).collect(Collectors.toList());
        int line = 1;
        for (Row r: data) {
            r.setLineNumber(line++);
        }
        return data;
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

        public void setLineNumber(int index) {
            this.index = index;
        }

        public boolean isEmpty() {
            return elements == null;
        }

        public boolean isDouble( int idx ) {
            if (!isEmpty()&&(0<=idx)&&(idx<elements.length)) {
                try {
                    Double.parseDouble(elements[idx]);
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
            return false;
        }

        public boolean isInteger( int idx ) {
            if (!isEmpty()&&(0<=idx)&&(idx<elements.length)) {
                try {
                    Integer.parseInt(elements[idx]);
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
            return false;
        }

        public boolean checkRecord(RecordHandler handler) {
            if (elements != null) {
                return checkRecord(handler,1,elements.length);
            }
            return true;
        }

        public boolean checkRecord(RecordHandler handler, int from, int to) {
            return (elements == null)||handler.checkValuesFromStrings(elements,from,to);
        }

        public void addValueToSignal(RecordHandler handler, Signal<Record> s) {
            if (elements != null) {
                double t = Double.parseDouble(elements[0]);
                Record r = handler.fromStringArray(elements,1,elements.length);
                s.add(t,r);
            }
        }

        public void addValueToSpatioTemporalSignal(int size, RecordHandler handler, SpatialTemporalSignal<Record> s) {
            if (elements != null) {
                double t = Double.parseDouble(elements[0]);
                Record[] data = new Record[size];
                for( int i=0 ; i<size ; i++ ) {
                    int first = 1+i*handler.size();
                    data[i] = handler.fromStringArray(elements,first,first+handler.size());
                }
                s.add(t,data);
            }
        }

        public String get(int i) {
            if (elements != null) {
                return elements[i];
            }
            throw new IllegalStateException();
        }

        public int getLine() {
            return index;
        }
    }
}
