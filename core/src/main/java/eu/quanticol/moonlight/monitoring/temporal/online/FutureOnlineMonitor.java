package eu.quanticol.moonlight.monitoring.temporal.online;

import eu.quanticol.moonlight.domain.AbstractInterval;
import eu.quanticol.moonlight.domain.Interval;
import eu.quanticol.moonlight.signal.online.ImmutableSegment;
import eu.quanticol.moonlight.signal.online.OnlineSignal;
import eu.quanticol.moonlight.signal.online.SignalInterface;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BinaryOperator;

public class FutureOnlineMonitor<T extends Comparable<T>, R extends Comparable<R>> implements OnlineTemporalMonitor<T,R>  {

    private final Interval timeBound;
    private final BinaryOperator<R> aggregator;
    private final OnlineTemporalMonitor<T,R> argument;
    private final OnlineSignal<R> outputSignal;

    public FutureOnlineMonitor(Interval timeBound, BinaryOperator<R> aggregator, OnlineTemporalMonitor<T, R> argument, OnlineSignal<R> outputSignal) {
        this.timeBound = timeBound;
        this.aggregator = aggregator;
        this.argument = argument;
        this.outputSignal = outputSignal;
    }

    @Override
    public List<ImmutableSegment<AbstractInterval<R>>> monitor(List<ImmutableSegment<AbstractInterval<T>>> updates) {
        List<ImmutableSegment<AbstractInterval<R>>> nestedUpdates = argument.monitor(updates);
        List<ImmutableSegment<AbstractInterval<R>>> results = new LinkedList<>();
        for(ImmutableSegment<AbstractInterval<R>> i: nestedUpdates) {


            //            results.addAll(outputSignal.update(i.start(),i.end(),i.getValue().apply(atomicProposition)));
        }
        return results;
    }

    @Override
    public SignalInterface<Double, AbstractInterval<R>> getResult() {
        return null;
    }
}
