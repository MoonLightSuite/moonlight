package eu.quanticol.moonlight.monitoring.temporal.online;

import eu.quanticol.moonlight.domain.AbstractInterval;
import eu.quanticol.moonlight.signal.online.ImmutableSegment;
import eu.quanticol.moonlight.signal.online.OnlineSignal;
import eu.quanticol.moonlight.signal.online.SignalInterface;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class AtomicOnlineMonitor<V extends Comparable<V>, R extends Comparable<R>> implements OnlineTemporalMonitor<V,R> {


    private final Function<V, R> atomicProposition;
    private final OnlineSignal<R> outputSignal;

    public AtomicOnlineMonitor(Function<V, R> atomicProposition, OnlineSignal<R> outputSignal) {
        this.atomicProposition = atomicProposition;
        this.outputSignal = outputSignal;
    }


    @Override
    public List<ImmutableSegment<AbstractInterval<R>>> monitor(List<ImmutableSegment<AbstractInterval<V>>> updates) {
        List<ImmutableSegment<AbstractInterval<R>>> results = new LinkedList<>();
        for(ImmutableSegment<AbstractInterval<V>> i: updates) {
            //results.addAll(outputSignal.refine(i.getStart(),i.end(),i.getValue().apply(atomicProposition)));
        }
        return results;
    }

    @Override
    public SignalInterface<Double, AbstractInterval<R>> getResult() {
        return outputSignal;
    }
}
