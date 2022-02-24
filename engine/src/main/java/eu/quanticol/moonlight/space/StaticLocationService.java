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

package eu.quanticol.moonlight.space;

import java.util.ArrayList;
import java.util.List;

import eu.quanticol.moonlight.core.space.LocationService;
import eu.quanticol.moonlight.core.space.SpatialModel;
import eu.quanticol.moonlight.util.Pair;

import java.util.Iterator;

public class StaticLocationService<V> implements LocationService<Double, V> {

    private final SpatialModel<V> model;

    public StaticLocationService(SpatialModel<V> model) {
        this.model = model;
    }

    @Override
    public SpatialModel<V> get(Double t) {
        return model;
    }

    @Override
    public Iterator<Pair<Double, SpatialModel<V>>> times() {
        List<Pair<Double, SpatialModel<V>>> list = new ArrayList<>();
        list.add(new Pair<>(0.0,model));
        return list.iterator();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
