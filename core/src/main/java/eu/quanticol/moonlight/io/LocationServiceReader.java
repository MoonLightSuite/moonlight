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

import eu.quanticol.moonlight.space.LocationService;
import eu.quanticol.moonlight.space.MoonLightRecord;
import eu.quanticol.moonlight.signal.RecordHandler;

import java.io.File;
import java.io.IOException;

/**
 * Classes implementing this interface provide the methods that can be used to read a location service
 * from either a file or a string.
 */
public interface LocationServiceReader {

    /**
     * Reads a location service from a file.
     *
     * @param handler Record handeler used to read data on edges
     * @param input input file
     * @return a location service
     * @throws IOException if I/O error occurs while reading the file
     * @throws IllegalFileFormatException if the file is not well formatted
     */
    LocationService<MoonLightRecord> read(RecordHandler handler, File input) throws IOException, IllegalFileFormatException;

    /**
     * Reads a location service from a file.
     *
     * @param handler Record handeler used to read data on edges
     * @param input input string
     * @return a location service
     * @throws IllegalFileFormatException if the file is not well formatted
     */
    LocationService<MoonLightRecord> read(RecordHandler handler, String input) throws IllegalFileFormatException;


}
