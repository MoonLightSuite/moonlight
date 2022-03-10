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

import java.io.File;
import java.io.IOException;

import eu.quanticol.moonlight.core.base.MoonLightRecord;
import eu.quanticol.moonlight.offline.signal.RecordHandler;
import eu.quanticol.moonlight.offline.signal.SpatialTemporalSignal;

/**
 * Classes implementing this interface are used to read a spatial-temporal signal either from a string or
 * from a file in a given format.
 *
 * @author loreti
 *
 */
public interface SpatialTemporalSignalReader {

	/**
	 * Reads a spatiotemporal signal from a file.
	 *
	 * @param handler Record handler used to read data from the file
	 * @param input input file
	 * @return the read spatial-temporal signal
	 * @throws IOException if an I/O error occurs while accessing the file
	 * @throws IllegalFileFormatException if the file is not well formatted
	 */
	SpatialTemporalSignal<MoonLightRecord> load(RecordHandler handler, File input) throws IOException, IllegalFileFormatException;

	/**
	 * Reades a spatiotemporal signal from a string
	 *
	 * @param handler Record handler used to read data from the file
	 * @param input input string
	 * @return the read spatial-temporal signal
	 * @throws IllegalFileFormatException is the string is not well formatted
	 */
	SpatialTemporalSignal<MoonLightRecord> load(RecordHandler handler, String input) throws IllegalFileFormatException;

}
