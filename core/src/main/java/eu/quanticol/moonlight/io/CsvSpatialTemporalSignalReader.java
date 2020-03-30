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
import eu.quanticol.moonlight.signal.SpatialTemporalSignal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Stream;

public class CsvSpatialTemporalSignalReader extends AbstractFileByRowReader implements SpatialTemporalSignalReader {

    @Override
    public SpatialTemporalSignal<Record> load(int size, RecordHandler handler, File input) throws IOException, IllegalFileFormatException {
        return load(size,handler, getRows(input));
    }

    @Override
    public SpatialTemporalSignal<Record> load(int size, RecordHandler handler, String input) throws IllegalFileFormatException {
        return load(size,handler, getRows(input));
    }

    public SpatialTemporalSignal<Record> load(int size, RecordHandler handler, List<Row> data) throws IllegalFileFormatException {
        checkData(size,handler,data);
        SpatialTemporalSignal<Record> toReturn = new SpatialTemporalSignal<>(size);
        for (Row row: data) {
            row.addValueToSpatioTemporalSignal(size,handler,toReturn);
        }
        return toReturn;
    }

    private void checkData(int size, RecordHandler handler, List<Row> data) throws IllegalFileFormatException {
        for (Row row: data) {
            int expected = 1+size*handler.size();
            if (row.elements.length != expected) {
                throw new IllegalFileFormatException(row.index, "Expected "+expected+" columns at row "+row.index+" are "+row.elements.length+"!");
            }
            if (!row.isDouble(0)) {
                throw new IllegalFileFormatException(row.index, "First element of each row must be a double!");
            }
            for( int i=0 ; i<size; i++) {
                int index = 1+handler.size()*i;
                if (!row.checkRecord(handler,index,handler.size())) {
                    throw new IllegalFileFormatException(row.index,"Input data error!");
                }
            }
        }
    }

}
