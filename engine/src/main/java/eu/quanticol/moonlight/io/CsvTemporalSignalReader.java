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

import eu.quanticol.moonlight.signal.RecordHandler;
import eu.quanticol.moonlight.signal.Signal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Stream;

/**
 * This class is used to read a temporal signal from a CSV file or a string.
 * The file format consits of a sequence of data separated by a ';', the first of which must
 * be a double. The first column must be ordered.
 */
public class CsvTemporalSignalReader extends AbstractFileByRowReader implements TemporalSignalReader {

    @Override
    public Signal<MoonLightRecord> load(RecordHandler handler, File input) throws IOException, IllegalFileFormatException {
        return load(handler, collectDataRows(Files.lines(input.toPath())));
    }

    private Signal<MoonLightRecord> load(RecordHandler handler, List<Row> data ) throws IllegalFileFormatException {
        checkData(handler,data);
        Signal<MoonLightRecord> s = new Signal<>();
        for (Row row: data) {
            row.addValueToSignal(handler,s);
        }
        s.end();
        return s;
    }

    private void checkData(RecordHandler handler, List<Row> data) throws IllegalFileFormatException {
        for (Row row: data) {
            row.split(";");
            if (!row.isEmpty()) {
                if (!row.isDouble(0)) {
                    throw new IllegalFileFormatException(row.index, "First element of each row must be a double!");
                }
                if (!row.checkRecord(handler)) {
                    throw new IllegalFileFormatException(row.index, "Input data error!");
                }
            }
        }
    }

    @Override
    public Signal<MoonLightRecord> load(RecordHandler handler, String input) throws IllegalFileFormatException {
        return load(handler,collectDataRows(Stream.of(input.split("\n"))));
    }

}
