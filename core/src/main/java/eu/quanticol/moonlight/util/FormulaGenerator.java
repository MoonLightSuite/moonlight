/**
 *
 */
package eu.quanticol.moonlight.util;

import eu.quanticol.moonlight.formula.*;
import eu.quanticol.moonlight.io.FormulaType;

import java.util.Random;

/**
 * @author loreti
 */
public abstract class FormulaGenerator {

    private static final int DEFAULT_MAXSIZE = 10;
    private static final double EPS = 0.000001;
    private static final double DEFAULT_MAXTIME = 100.0;

    private final Random random;
    private String[] atomidId;
    private double maxTime;

    FormulaGenerator(Random random, double maxTime, String... ids) {
        this.random = random;
        this.atomidId = ids;
        this.maxTime = maxTime;
    }

    FormulaGenerator(String... ids) {
        this(new Random(), DEFAULT_MAXTIME, ids);
    }

    public Formula getFormula() {
        return getFormula(random.nextInt(DEFAULT_MAXSIZE), DEFAULT_MAXTIME);
    }

    public Formula getFormula(int size) {
        return getFormula(size, maxTime);
    }

    private Formula getFormula(int size, double time) {
        if (size == 0 || time <= 0) {
            return getAtomicFormula();
        } else {
            return generateFormula(size, time);
        }
    }

    private Formula getAtomicFormula() {
        return new AtomicFormula(atomidId[random.nextInt(atomidId.length)]);
    }

    private Formula generateFormula(int size, double time) {
        FormulaType[] types = getFormulaType();
        Interval interval = getInterval(false, time);
        double newTime = Math.max(0, time - interval.getEnd());
        switch (types[getRandom().nextInt(types.length)]) {
            case AND:
                return new AndFormula(getFormula(size - 1, time), getFormula(size - 1, time));
            case ATOMIC:
                return getAtomicFormula();
            case EVENTUALLY:
                return new EventuallyFormula(getFormula(size - 1, newTime), interval);
            case GLOBALLY:
                return new GloballyFormula(getFormula(size - 1, newTime), interval);
            case HYSTORICALLY:
                return new HystoricallyFormula(getFormula(size - 1, newTime), interval);
            case NOT:
                return new NegationFormula(getFormula(size - 1, time));
            case ONCE:
                return new OnceFormula(getFormula(size - 1, newTime), interval);
            case OR:
                return new OrFormula(getFormula(size - 1, time), getFormula(size - 1, time));
            case SINCE:
                return new SinceFormula(getFormula(size - 1, newTime), getFormula(size - 1, newTime), interval);
            case UNTIL:
                return new UntilFormula(getFormula(size - 1, newTime), getFormula(size - 1, newTime), interval);
        }
        return null;
    }

    public abstract FormulaType[] getFormulaType();

    protected Interval getInterval(boolean isNullable, double time) {
        if (isNullable && random.nextBoolean()) {
            return null;
        }
        double start = random.nextDouble() * time;
        double end = start + random.nextDouble() * (time-start) + EPS;
        return new Interval(start, end);
    }

    private Random getRandom() {
        return random;
    }
}
