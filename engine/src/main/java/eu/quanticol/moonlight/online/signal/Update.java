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

package eu.quanticol.moonlight.online.signal;

import java.util.List;

/**
 * Data class to store updates of the kind <code>[start, end) -&gt; value</code>
 *
 * @param <T> Time domain of the update
 * @param <V> Value of the update
 */
public record Update<T extends Comparable<T>, V>(T start, T end, V value)  {

    public Update {
        if(start.compareTo(end) > 0 || start.equals(end))
            throw new IllegalArgumentException("Invalid update time span: [" +
                                                start + ", " + end + ")");
    }

    public T getStart() {
        return start;
    }

    public T getEnd() {
        return end;
    }

    public V getValue() {
        return value;
    }

    public static <T extends Comparable<T>, V>
    TimeChain<T, V> asTimeChain(List<Update<T, V>> ups)
    {
        T end = ups.get(ups.size() - 1).end();
        TimeChain<T, V> chain = new TimeChain<>(end);
        for(int i = 0; i < ups.size(); i++) {
            if(i != ups.size() - 1) {
                if (ups.get(i).getEnd().equals(ups.get(i + 1).getStart())) {
                    chain.add(new TimeSegment<>(ups.get(i).getStart(),
                            ups.get(i).getValue()));
                } else throw new UnsupportedOperationException("Updates " + i +
                        " and " + (i + 1) +
                        " are not sequential");
            } else
                chain.add(new TimeSegment<>(ups.get(i).getStart(),
                                            ups.get(i).getValue()));
        }

        return chain;
    }
}
