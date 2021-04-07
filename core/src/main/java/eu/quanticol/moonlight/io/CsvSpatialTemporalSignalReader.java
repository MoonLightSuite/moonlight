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

import eu.quanticol.moonlight.space.MoonLightRecord;
import eu.quanticol.moonlight.signal.RecordHandler;
import eu.quanticol.moonlight.signal.SpatialTemporalSignal;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * This class is used to read a spatial-temporal signal either from a file or from a string. The first line of the
 * file must contain the declaration of the number of locations in the model:
 *
 * <code>LOCATIONS n</code>
 *
 * After that the values of the signal at each location for each time separated by a semicolon:
 *
 * <code>time;loc_1_v_1;...;loc_1_v_k;..;loc_n_v_1;...;loc_n_v_k;</code>
 *
 * Empty lines and extra spaces are ignored in the file.
 */
public class CsvSpatialTemporalSignalReader extends AbstractFileByRowReader implements SpatialTemporalSignalReader {

    @Override
    public SpatialTemporalSignal<MoonLightRecord> load(RecordHandler handler, File input) throws IOException, IllegalFileFormatException {
        return load(handler, getRows(input));
    }

    @Override
    public SpatialTemporalSignal<MoonLightRecord> load(RecordHandler handler, String input) throws IllegalFileFormatException {
        return load(handler, getRows(input));
    }

    private SpatialTemporalSignal<MoonLightRecord> load(RecordHandler handler, List<Row> data) throws IllegalFileFormatException {
        int size = checkData(handler,data);
        Iterator<Row> dataIterator = data.iterator();
        dataIterator.next();
        SpatialTemporalSignal<MoonLightRecord> toReturn = new SpatialTemporalSignal<>(size);
        while (dataIterator.hasNext()) {
            Row row = dataIterator.next();
            row.addValueToSpatioTemporalSignal(size,handler,toReturn);
        }
        return toReturn;
    }

    private int checkData(RecordHandler handler, List<Row> data) throws IllegalFileFormatException {
        int size = 0;
        Iterator<Row> dataIterator = data.iterator();
        if (dataIterator.hasNext()) {
            size = parseSize(dataIterator.next());
        }
        int expected = 1+size*handler.size();
        while (dataIterator.hasNext()) {
            Row row = dataIterator.next();
            row.split(";");
            if (row.elements.length != expected) {
                throw new IllegalFileFormatException(row.index, "Expected "+expected+" columns at row "+row.index+" are "+row.elements.length+"!");
            }
            if (!row.isDouble(0)) {
                throw new IllegalFileFormatException(row.index, "First element of each row must be a double!");
            }
            for( int i=0 ; i<size; i++) {
                int index = 1+handler.size()*i;
                if (!row.checkRecord(handler,index,index+handler.size())) {
                    System.err.println("Line: "+row.getLine()+" "+i);
                    throw new IllegalFileFormatException(row.index,"Input data error! (Line "+row.getLine()+"@"+i);
                }
            }
        }
        return size;
    }

    private int parseSize(Row row) throws IllegalFileFormatException {
        if (row.getRow().startsWith("LOCATIONS")) {
            row.split(" ");
            if ((row.elements.length != 2)||(!row.isInteger(1))) {
                throw new IllegalFileFormatException(row.index, "LOCATIONS <num> is expected in the first line!");
            }
            return Integer.parseInt(row.get(1));
        } else {
            throw new IllegalFileFormatException(row.index, "Number of locations is expected in the first line!");
        }
    }

}
