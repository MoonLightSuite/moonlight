package eu.quanticol.moonlight.monitoring.temporal.online;

import eu.quanticol.moonlight.domain.AbstractInterval;
import eu.quanticol.moonlight.signal.ImmutableSegment;
import eu.quanticol.moonlight.signal.OnlineSignal;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class AtomicOnlineMonitor<T extends Comparable<T>, R extends Comparable<R>> implements OnlineTemporalMonitor<T,R> {

    private final Function<T,R> atomicProposition;
    private final OnlineSignal<R> outputSignal;

    public AtomicOnlineMonitor(Function<T, R> atomicProposition, OnlineSignal<R> outputSignal) {
        this.atomicProposition = atomicProposition;
        this.outputSignal = outputSignal;
    }


    @Override
    public List<ImmutableSegment<AbstractInterval<R>>> monitor(List<ImmutableSegment<AbstractInterval<T>>> updates) {
        List<ImmutableSegment<AbstractInterval<R>>> results = new LinkedList<>();
        for(ImmutableSegment<AbstractInterval<T>> i: updates) {
            results.addAll(outputSignal.update(i.start(),i.end(),i.getValue().apply(atomicProposition)));
        }
        return results;
    }

    @Override
    public OnlineSignal<R> getResult() {
        return outputSignal;
    }
}
