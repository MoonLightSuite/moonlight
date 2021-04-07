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


import eu.quanticol.moonlight.signal.*;
import eu.quanticol.moonlight.space.MoonLightRecord;
import eu.quanticol.moonlight.space.GraphModel;
import eu.quanticol.moonlight.space.LocationService;
import eu.quanticol.moonlight.space.LocationServiceList;
import eu.quanticol.moonlight.space.StaticLocationService;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * This class is used to read a location service in CSV  format from a string or a file. The expected structure
 * is the following.
 *
 * The first line contains the declariatio of the number of locations in the considere spatial model:
 *
 * <code>LOCATIONS n</code>
 *
 * where <code>n</code> is an integer.
 *
 * After that we can have a line containing either
 *
 * <code>DIRECTED</code>
 *
 * or
 *
 * <code>UNDIRECTED</code>
 *
 * that is used to declare that the forthcoming spatial models are directed or not. If omitted, models are handled
 * as directed.
 *
 * The next line can contain either a declaration of a \emph{static} location service:
 *
 * <code>STATIC</code>
 *
 * or the first time in the sequence of evolving spatial models:
 *
 * <code>TIME t</code>
 *
 * where <code>t</code> is a (non negative) double.
 *
 * In both the cases the content of the model is expressed via a sequence of rows having the following
 * structure:
 *
 * <code>l1;l2;v_1;...;v_k</code>
 *
 * where <code>l1</code> and <code>l2</code> are the indexes (indexes start from 0!) of the two connected
 * locations, while <code>v_1;...;v_k</code> is the tuple of strings of values labelling the connecting edges.
 *
 * When the service is not static, another line of the form <code>TIME t'</code> indicates the beginning
 * of a new graph model at time <code>t'</code>.
 *
 * Empty lines and extra spaces are ignored.
 *
 */
public class CsvLocationServiceReader extends AbstractFileByRowReader implements LocationServiceReader {
    @Override
    public LocationService<Double, MoonLightRecord> read(RecordHandler handler, File input)
            throws IOException, IllegalFileFormatException {
        return load(handler, getRows(input));
    }

    @Override
    public LocationService<Double, MoonLightRecord> read(RecordHandler handler, String input) throws IllegalFileFormatException {
        return load(handler, getRows(input));
    }

    private LocationService<Double, MoonLightRecord> load(RecordHandler handler, List<Row> rows) throws IllegalFileFormatException {
        int size = getModelSize(rows);
        boolean isStatic = checkStatic(rows);
        boolean isDirected = checkDirected(rows);
        if (isStatic) {
            return loadStaticLocationService(handler,size,isDirected,rows);
        } else {
            return loadDynamicLocationService(handler,size,isDirected,rows);
        }
    }

    private LocationService<Double, MoonLightRecord> loadDynamicLocationService(RecordHandler handler, int size, boolean isDirected, List<Row> rows) throws IllegalFileFormatException {
        Iterator<Row> rowIterator = rows.iterator();
        Row current = shiftToFirstTimeDeclaration(rowIterator);
        LocationServiceList<MoonLightRecord> locationService = new LocationServiceList<>();
        while (current != null) {
            double time = extractTimeFromRow(current);
            GraphModel<MoonLightRecord> model = new GraphModel<>(size);
            current = (rowIterator.hasNext()?rowIterator.next():null);
            while ((current != null)&&(!current.getRow().startsWith("TIME"))) {
                addEdgeToModel(handler,model,size,isDirected,current);
                current = (rowIterator.hasNext()?rowIterator.next():null);
            }
            locationService.add(time,model);
        }
        return locationService;
    }

    private double extractTimeFromRow(Row row) throws IllegalFileFormatException {
        row.split(" ");
        if (!row.isDouble(1)) {
            throw new IllegalFileFormatException(row.getLine(),"TIME <double> is expected!");
        }
        return Double.parseDouble(row.get(1));
    }

    private Row shiftToFirstTimeDeclaration(Iterator<Row> rowIterator) throws IllegalFileFormatException {
        try {
            do {
                Row current = rowIterator.next();
                if (current.getRow().startsWith("TIME")) {
                    return current;
                }
            } while (rowIterator.hasNext());
        } catch (NoSuchElementException e) {
        }
        throw new IllegalFileFormatException(1,"Time declaration is missing!");
    }

    private boolean checkStatic(List<Row> rows) {
        return rows.stream().anyMatch(r -> r.getRow().startsWith("STATIC"));
    }

    private boolean checkDirected(List<Row> rows) {
        return rows.stream().anyMatch(r -> r.getRow().startsWith("DIRECTED"));
    }

    private LocationService<Double, MoonLightRecord> loadStaticLocationService(RecordHandler handler, int size, boolean isDirected, List<Row> rows) throws IllegalFileFormatException {
        GraphModel<MoonLightRecord> model = new GraphModel<>(size);
        Iterator<Row> rowIterator = rows.iterator();
        shiftToStatic(rowIterator);
        while (rowIterator.hasNext()) {
            addEdgeToModel(handler,model,size,isDirected,rowIterator.next());
        }
        return new StaticLocationService<>(model);
    }

    private void addEdgeToModel(RecordHandler handler, GraphModel<MoonLightRecord> model, int size, boolean isDirected, Row row) throws IllegalFileFormatException {
        row.split(";");
        int src = getSource(row,size);
        int trg = getTarget(row,size);
        MoonLightRecord record = getRecord(handler,row);
        model.add(src,record,trg);
        if (!isDirected) {
            model.add(trg,record,src);
        }
    }

    private MoonLightRecord getRecord(RecordHandler handler, Row row) throws IllegalFileFormatException {
        if (row.elements.length != handler.size()+2) {
            throw new IllegalFileFormatException(row.getLine(),"Wrong number of data!");
        }
        try {
            return handler.fromStringArray(row.elements,2,row.elements.length);
        } catch (IllegalArgumentException e) {
            throw new IllegalFileFormatException(row.getLine(),e.getMessage());
        }
    }

    private int getSource(Row row, int size) throws IllegalFileFormatException {
        if (!row.isInteger(0)) {
            throw new IllegalFileFormatException(row.getLine(),"An integer is expected for the source location!");
        }
        int src = Integer.parseInt(row.get(0));
        if ((src<0)||(src>=size)) {
            throw new IllegalFileFormatException(row.getLine(),"An integer between 0 and "+size+" is expected for the source location!");
        }
        return src;
    }

    private int getTarget(Row row, int size) throws IllegalFileFormatException {
        if (!row.isInteger(1)) {
            throw new IllegalFileFormatException(row.getLine(),"An integer is expected for the target location!");
        }
        int trg = Integer.parseInt(row.get(1));
        if ((trg<0)||(trg>=size)) {
            throw new IllegalFileFormatException(row.getLine(),"An integer between 0 and "+size+" is expected for the target location!");
        }
        return trg;
    }

    private void shiftToStatic(Iterator<Row> rowIterator) {
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            if (row.getRow().startsWith("STATIC")) {
                return ;
            }
        }
    }

    private int getModelSize(List<Row> rows) throws IllegalFileFormatException {
        Optional<Row> locationsSizeDeclarationRow = rows.stream().filter(r -> r.getRow().startsWith("LOCATIONS")).findFirst();
        if (locationsSizeDeclarationRow.isPresent()) {
            Row row = locationsSizeDeclarationRow.get();
            row.split(" ");
            if (!row.isInteger(1)) {
                throw new IllegalFileFormatException(row.getLine(),"Malformed location size declaration!");
            }
            return Integer.parseInt( row.get(1) );
        }
        throw new IllegalFileFormatException(1,"Declaration of number of locations in the model is missing!");
    }

}
