package eu.quanticol.moonlight.domain;

import eu.quanticol.moonlight.core.base.Tuple;
import eu.quanticol.moonlight.core.base.TupleType;
import eu.quanticol.moonlight.core.io.DataHandler;
import eu.quanticol.moonlight.core.signal.SignalDomain;

/**
 * Class to handle a tuple domain
 */
public class TupleDomain implements SignalDomain<Tuple> {

    private final TupleType type;

    public TupleDomain(TupleType type) {
        this.type = type;

    }

    @Override
    public Tuple conjunction(Tuple x, Tuple y) {
        if (x.getType().equals(type) && y.getType().equals(type)) {
            for (int i = 0; i < type.size(); i++) {
                //type.getIthType(i).cast();
            }
        }
        throw new UnsupportedOperationException("wrong tuple type passed");
    }

    @Override
    public Tuple disjunction(Tuple x, Tuple y) {
        return null;
    }

    @Override
    public Tuple min() {
        return null;
    }

    @Override
    public Tuple max() {
        return null;
    }

    @Override
    public Tuple any() {
        return null;
    }

    @Override
    public Tuple negation(Tuple x) {
        return null;
    }

    @Override
    public DataHandler<Tuple> getDataHandler() {
        return notImplementedYet();
    }

    @Override
    public boolean equalTo(Tuple x, Tuple y) {
        return notImplementedYet();
    }

    @Override
    public Tuple valueOf(boolean b) {
        return notImplementedYet();
    }

    @Override
    public Tuple valueOf(double v) {
        return notImplementedYet();
    }

    @Override
    public Tuple computeLessThan(double v1, double v2) {
        return notImplementedYet();
    }

    @Override
    public Tuple computeLessOrEqualThan(double v1, double v2) {
        return notImplementedYet();
    }

    @Override
    public Tuple computeEqualTo(double v1, double v2) {
        return notImplementedYet();
    }

    @Override
    public Tuple computeGreaterThan(double v1, double v2) {
        return notImplementedYet();
    }

    @Override
    public Tuple computeGreaterOrEqualThan(double v1, double v2) {
        return notImplementedYet();
    }

    private <T> T notImplementedYet() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
