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

import eu.quanticol.moonlight.core.signal.Sample;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.function.BiConsumer;

/**
 * Given a primary and a secondary chain, it mutates the primary chain 
 * by combining the respective values in the given order
 * 
 * <pre>
 *   Primary    Secondary       Result (Updated Primary)
 *     1                          1 
 *     |                          |
 *     2            2             2
 *     |            |             |
 *     4            3             3
 *     |            |             |
 *     .            4             4
 *     .                          .
 *     .                          .
 *                                .
 * </pre>
 * 
 *  Note: the iterator doesn't deal with duplicates in the output,
 *        it just returns the elements to be evaluated.
 *        This is a deliberate design decision to keep SRP (it just iterates),
 *        but, depending on feedbacks, this can be changed.
 *
 * @param <T> Type on which the order relation is defined
 * @param <V> Type of which the values will be combined in the resulting chain
 *
 * @author Ennio Visconti
 */
public class ChainsCombinator
        <T extends Comparable<T> & Serializable, V>
{
    private final ChainIterator<Sample<T, V>> primary;
    private final ChainIterator<Sample<T, V>> secondary;
    private final Sample<T, V> primaryEnd;
    private final Sample<T, V> secondaryEnd;

    Sample<T, V> primaryCurr;
    Sample<T, V> primaryNext;
    Sample<T, V> secondaryCurr;
    Sample<T, V> secondaryNext;

    public ChainsCombinator(@NotNull TimeChain<T, V> primaryChain,
                            @NotNull TimeChain<T, V> secondaryChain)
    {
        if(primaryChain.isEmpty() || secondaryChain.isEmpty())
            throw new IllegalArgumentException("Both chains must not be empty");

        primary = primaryChain.chainIterator();
        secondary = secondaryChain.chainIterator();


        primaryEnd = endingSegment(primaryChain);
        secondaryEnd = endingSegment(secondaryChain);
    }

    public void forEach(BiConsumer<Sample<T, V>, Sample<T, V>> operation) {
        movePrimary();
        moveSecondary();
        do {
            if(notYetRelevant()) {
                movePrimary();
                continue;
            }
            if(notAnymoreRelevant()) {
                moveSecondary();
                continue;
            }
            process(operation);
        } while(stillToProcess());
    }

    private void movePrimary() {
        primaryCurr = primary.hasNext() ? primary.next() : primaryEnd;
        primaryNext = primary.tryPeekNext(primaryEnd);
    }

    private void moveSecondary() {
        secondaryCurr = secondary.hasNext() ? secondary.next() : secondaryEnd;
        secondaryNext = secondary.tryPeekNext(secondaryEnd);
    }

    private boolean notAnymoreRelevant() {
        return primaryCurr.compareTo(secondaryNext) > 0;
    }

    private boolean notYetRelevant() {
        return primaryNext.compareTo(secondaryCurr) <= 0;
    }

    private void process(BiConsumer<Sample<T, V>, Sample<T, V>> op) {
        op.accept(primaryCurr, secondaryCurr);
        if(secondaryProcessingNotComplete())
            movePrimary();
        else
            moveSecondary();
    }

    private boolean secondaryProcessingNotComplete() {
        return primaryNext.compareTo(secondaryNext) < 0;
    }

    private boolean stillToProcess() {
        return !secondaryEnd.equals(secondaryCurr);
    }

    private Sample<T, V> endingSegment(TimeChain<T, V> chain) {
        // not sure this is a smart idea, we are saying there is a last
        // element that starts at the last allowed time and has the last value
        // therefore potentially resulting in infinite loops
        return new TimeSegment<>(chain.getEnd(), chain.getLast().getValue());
    }
}
