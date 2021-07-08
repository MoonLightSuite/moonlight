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
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.quanticol.moonlight.examples.temporal.afc;

import eu.quanticol.moonlight.util.Plotter;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.logging.Logger;

public class AFCSettings {
    private AFCSettings() {}

    // Configurations
    public static final double LAST_TIME = 1000;
    public static final double SCALE = 0.1;
    public static final int ITERATIONS = 1;
    public static final boolean PLOTTING = false;

    // Utilities
    public static final Logger LOG = Logger.getLogger("AFC Log");
    public static final Plotter plt = new Plotter();

    // Atom
    public static final String AFC_ATOM = "bigError";

    public static String localPath() throws IOException {
        try {
            URL url = AFCSettings.class
                        .getResource("matlab/afc_moonlight_monitoring.m");
            return Paths.get(Objects.requireNonNull(url).toURI())
                        .getParent().toAbsolutePath().toString();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new IOException("Unable to access the matlab scripts");
        }
    }

    public static InputStream dataPath(String id) {
        String path = "data/afc_sim_" + id + ".csv";
        InputStream stream = AFCSettings.class.getResourceAsStream(path);

        if(stream == null)
            throw new IllegalArgumentException("Cannot find " + path);

        return stream;
    }
}
