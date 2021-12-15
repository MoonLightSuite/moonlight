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

package eu.quanticol.moonlight.signal;

import eu.quanticol.moonlight.io.AbstractFileByRowReader;
import eu.quanticol.moonlight.io.json.IllegalFileFormat;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Load a location service in tra form from a file or string. A tra structure has the following structure:
 *
 * LOCATIONS l
 * TIME t0
 * TRANSITIONS k0
 * src1 trg1 a1 b1 c1
 * src2 trg2 a2 b2 b2
 * ...
 * srck trgk ak bk ck
 * ...
 * TIME tn
 * TRANSITIONS kn
 * src1 trg1 a1 b1 c1
 * src2 trg2 a2 b2 b2
 * ...
 * srck trgk ak bk ck
 *
 * I the graph is constant in time TIME line can be omitted:
 *
 * LOCATIONS l
 * TRANSITIONS k
 * src1 trg1 a1 b1 c1
 * src2 trg2 a2 b2 b2
 * ...
 * srck trgk ak bk ck
 *
 */
public class TRALocationServiceLoader extends AbstractFileByRowReader implements LocationServiceLoader {
    public static final String LOCATIONS_KEY = "LOCATIONS";
    private static final String TIME_KEY = "TIME";
    private static final String TRANSITIONS_KEY = "TRANSITIONS";

    @Override
    public LocationService<MoonLightRecord> load(RecordHandler handler, File input) throws IOException, IllegalFileFormat {
        return load(handler,getRows(input));
    }

    @Override
    public LocationService<MoonLightRecord> load(RecordHandler handler, String input) throws IllegalFileFormat {
        return load(handler,getRows(input));
    }

    private LocationService<MoonLightRecord> load(RecordHandler handler, List<Row> rows) throws IllegalFileFormat {
        Iterator<Row> iterator = rows.iterator();
        int size = parseSize( iterator );
        return parseLocationService(size,handler,iterator);
    }

    private LocationService<MoonLightRecord> parseLocationService(int size, RecordHandler handler, Iterator<Row> iterator) throws IllegalFileFormat {
        LocationServiceList<MoonLightRecord> loc = new LocationServiceList<>();
        Row row = nextNotEmpty(iterator);
        if (isTimeRow(row)) {
            do {
                double time = parseTime(row);
                row = nextNotEmpty(iterator);
                loc.add( time , parseGraph(handler, size, getNumberOfTransitions(row) , iterator));
                row = nextNotEmpty(iterator);
            } while( row != null );
        } else {
            if (isDeclarationOfTransition(row)) {
                loc.add(0.0, parseGraph( handler, size, getNumberOfTransitions(row) , iterator ) );
            } else {
                throw new IllegalFileFormat("Error: either "+TIME_KEY+" or "+TRANSITIONS_KEY+" is expected at line "+row.getLine());
            }
        }
        return loc;
    }

    private SpatialModel<MoonLightRecord> parseGraph(RecordHandler handler, int size, int numberOfTransitions, Iterator<Row> iterator) throws IllegalFileFormat {
        GraphModel<MoonLightRecord> model = new GraphModel<>(size);
        for( int i=0 ; i<numberOfTransitions ; i++ ) {
            Row row = nextNotEmpty(iterator);
            addTransition(row,model,handler);
        }
        return model;
    }

    private void addTransition(Row row, GraphModel<MoonLightRecord> model, RecordHandler handler) throws IllegalFileFormat {
        String[] elements = row.get(0).trim().split(" ");
        if (elements.length > 2) {
            try {
                int src = Integer.parseInt(elements[0])-1;
                int trg = Integer.parseInt(elements[1])-1;
                MoonLightRecord v = handler.fromStringArray(elements,2,elements.length);
                model.add(src,v,trg);
                return ;
            } catch (NumberFormatException e) {

            } catch (IllegalValueException e) {

            }
        }
        throw new IllegalFileFormat("Syntax error at line "+row.get(0)+": expected <int> <int> <val1>;...;<valn>, found "+row.get(0));
    }

    private boolean isDeclarationOfTransition(Row row) {
        if (row.get(0).startsWith(TRANSITIONS_KEY)) {
            try {
                Integer.parseInt(row.get(0).substring(TRANSITIONS_KEY.length()).trim());
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }

    private int getNumberOfTransitions(Row row) throws IllegalFileFormat {
        if (row.get(0).startsWith(TRANSITIONS_KEY)) {
            try {
                return Integer.parseInt(row.get(0).substring(TRANSITIONS_KEY.length()).trim());
            } catch (NumberFormatException e) {
            }
        }
        throw new IllegalFileFormat("Syntax error at line "+row.get(0)+": expected "+TRANSITIONS_KEY+" <int>, found "+row.get(0));
    }

    private double parseTime(Row row) throws IllegalFileFormat {
        if (row.get(0).startsWith(TIME_KEY)) {
            try {
                return Double.parseDouble(row.get(0).substring(TIME_KEY.length()).trim());
            } catch (NumberFormatException e) {}
        }
        throw new IllegalFileFormat("Syntax error at line "+row.get(0)+": expected "+TIME_KEY+" <double>, found "+row.get(0));
    }

    private boolean isTimeRow(Row row) {
        if (row.get(0).startsWith(TIME_KEY)) {
            try {
                Double.parseDouble(row.get(0).substring(TIME_KEY.length()).trim());
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }

    private Row nextNotEmpty(Iterator<Row> iterator) {
        while (iterator.hasNext()) {
            Row r = iterator.next();
            if (!r.isEmpty()) {
                return r;
            }
        }
        return null;
    }

    private int parseSize(Iterator<Row> iterator) throws IllegalFileFormat {
        Row row = nextNotEmpty(iterator);
        if (row != null) {
            String element = row.get(0);
            return parseSize(element);
        }
        throw new IllegalFileFormat("Declarations of number of localities is missing the the beginning of file.");
    }

    private int parseSize(String element) throws IllegalFileFormat {
        if (element.startsWith(LOCATIONS_KEY)) {
            try {
                return Integer.parseInt(element.substring(LOCATIONS_KEY.length()).trim());
            } catch (NumberFormatException e)  {}
        }
        throw new IllegalFileFormat("Error: '" + LOCATIONS_KEY + " <int>' is expected, found " +element);
    }


}
