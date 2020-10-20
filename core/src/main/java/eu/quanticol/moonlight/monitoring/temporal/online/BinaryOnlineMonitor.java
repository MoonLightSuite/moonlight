package eu.quanticol.moonlight.monitoring.temporal.online;

import eu.quanticol.moonlight.domain.AbstractInterval;
import eu.quanticol.moonlight.signal.ImmutableSegment;
import eu.quanticol.moonlight.signal.OnlineSignal;

import java.util.List;
import java.util.function.BinaryOperator;

public class BinaryOnlineMonitor<T extends Comparable<T>, R extends Comparable<R>> implements OnlineTemporalMonitor<T,R>  {

    private final BinaryOperator<R> operator;
    private final OnlineTemporalMonitor<T,R> leftMonitor;
    private final OnlineTemporalMonitor<T,R> rightMonitor;
    private final OnlineSignal<R> outputSignal;

    public BinaryOnlineMonitor(BinaryOperator<R> operator, OnlineTemporalMonitor<T, R> leftMonitor, OnlineTemporalMonitor<T, R> rightMonitor, OnlineSignal<R> outputSignal) {
        this.operator = operator;
        this.leftMonitor = leftMonitor;
        this.rightMonitor = rightMonitor;
        this.outputSignal = outputSignal;
    }


    @Override
    public List<ImmutableSegment<AbstractInterval<R>>> monitor(List<ImmutableSegment<AbstractInterval<T>>> updates) {
        List<ImmutableSegment<AbstractInterval<R>>> leftUpdates = leftMonitor.monitor(updates);
        List<ImmutableSegment<AbstractInterval<R>>> rightUpdates = rightMonitor.monitor(updates);
        //Faccio l'opportuno merge dei due update
        //Aggiorno il segnale con il merge
        //Potrebbe valere la pena considerare due oggetti diversi per il monitor dell'And e dell'Or.
        //PROP: se v=o(i1,i2) e i1' aggiorna i1 allora o(i1',i2)=o(i1',v)
        //TROVARE OPERATORE OPPORTUNO DI UPDATE!
        //Se vale quanto sopra, allora possiamo applicare gli aggiornamenti left e right direttamente senza merge
        return null;
    }

    @Override
    public OnlineSignal<R> getResult() {
        return outputSignal;
    }
}
