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

import eu.quanticol.moonlight.io.json.IllegalFileFormat;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TRALocationServiceLoaderTest {

    @Test
    void testLoadSimple() throws IllegalFileFormat {
        RecordHandler rh = new RecordHandler(DataHandler.INTEGER);
        String tra = "LOCATIONS 4\n" +
                "TRANSITIONS 4\n" +
                "1 2 3\n" +
                "1 3 5\n" +
                "3 4 7\n" +
                "2 4 8\n";
        TRALocationServiceLoader loader = new TRALocationServiceLoader();
        LocationService<Record> ls = loader.load(rh,tra);
        assertNotNull(ls);
        SpatialModel<Record> g = ls.get(0.0);
        assertEquals(4,g.size());
    }

    @Test
    void testLoadSimpleTimed() throws IllegalFileFormat {
        RecordHandler rh = new RecordHandler(DataHandler.INTEGER);
        String tra = "LOCATIONS 4\n" +
                "TIME 0.0\n" +
                "TRANSITIONS 4\n" +
                "1 2 3\n" +
                "1 3 5\n" +
                "3 4 7\n" +
                "2 4 8\n" +
                "TIME 0.2\n" +
                "TRANSITIONS 4\n" +
                "1 2 3\n" +
                "1 3 5\n" +
                "3 4 7\n" +
                "2 4 8\n" +
                "TIME 0.4\n" +
                "TRANSITIONS 4\n" +
                "1 2 3\n" +
                "1 3 5\n" +
                "3 4 7\n" +
                "2 4 8\n" +
                "TIME 0.6\n" +
                "TRANSITIONS 4\n" +
                "1 2 3\n" +
                "1 3 5\n" +
                "3 4 7\n" +
                "2 4 8\n";
        TRALocationServiceLoader loader = new TRALocationServiceLoader();
        LocationService<Record> ls = loader.load(rh,tra);
        assertNotNull(ls);
        SpatialModel<Record> g = ls.get(0.0);
        assertEquals(4,g.size());
    }

    @Test
    void testLoadPairs() throws IllegalFileFormat {
        RecordHandler rh = new RecordHandler(DataHandler.INTEGER,DataHandler.REAL);
        String tra = "LOCATIONS 4\n" +
                "TRANSITIONS 4\n" +
                "1 2 3 2.1\n" +
                "1 3 5 2.2\n" +
                "3 4 7 3.4\n" +
                "2 4 8 4.5\n" +
                "TIME 0.1\n";
        TRALocationServiceLoader loader = new TRALocationServiceLoader();
        LocationService<Record> ls = loader.load(rh,tra);
        assertNotNull(ls);
        SpatialModel<Record> g = ls.get(0.0);
        assertEquals(4,g.size());
    }

    @Test
    void testLoadPairsTimed() throws IllegalFileFormat {
        RecordHandler rh = new RecordHandler(DataHandler.INTEGER,DataHandler.REAL);
        String tra = "LOCATIONS 4\n" +
                "TIME 0.0\n" +
                "TRANSITIONS 4\n" +
                "1 2 3 2.1\n" +
                "1 3 5 2.2\n" +
                "3 4 7 3.4\n" +
                "2 4 8 4.5\n" +
                "TIME 0.1\n" +
                "TRANSITIONS 4\n" +
                "1 2 3 2.1\n" +
                "1 3 5 2.2\n" +
                "3 4 7 3.4\n" +
                "2 4 8 4.5\n" +
                "TIME 0.2\n" +
                "TRANSITIONS 4\n" +
                "1 2 3 2.1\n" +
                "1 3 5 2.2\n" +
                "3 4 7 3.4\n" +
                "2 4 8 4.5\n" +
                "TIME 0.3\n" +
                "TRANSITIONS 4\n" +
                "1 2 3 2.1\n" +
                "1 3 5 2.2\n" +
                "3 4 7 3.4\n" +
                "2 4 8 4.5\n" +
                "TIME 0.4\n" +
                "TRANSITIONS 4\n" +
                "1 2 3 2.1\n" +
                "1 3 5 2.2\n" +
                "3 4 7 3.4\n" +
                "2 4 8 4.5\n";
        TRALocationServiceLoader loader = new TRALocationServiceLoader();
        LocationService<Record> ls = loader.load(rh,tra);
        assertNotNull(ls);
        SpatialModel<Record> g = ls.get(0.0);
        assertEquals(4,g.size());
    }


}