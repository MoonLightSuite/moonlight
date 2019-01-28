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
    private int maxSize = DEFAULT_MAXSIZE;
    private String[] atomidId;
    private double maxTime;

    FormulaGenerator(Random random, double maxTime, String... ids) {
        this.random = random;
        this.atomidId = ids;
        this.maxTime = maxTime;
    }

    public FormulaGenerator(double maxTime, String... ids) {
        this(new Random(), maxTime, ids);
    }

    public FormulaGenerator(String... ids) {
        this(new Random(), DEFAULT_MAXTIME, ids);
    }

    public int getMaxSize() {
        return maxSize;
    }

    public Formula getFormula() {
        return getFormula(random.nextInt(maxSize));
    }

    public Formula getFormula(int size) {
        if (size == 0) {
            return getAtomicFormula();
        } else {
            return generateFormula(size);
        }
    }

    private Formula getAtomicFormula() {
        return new AtomicFormula(atomidId[random.nextInt(atomidId.length)]);
    }

    private Formula generateFormula(int size, String... ids) {
        FormulaType[] types = getFormulaType();
        switch (types[getRandom().nextInt(types.length)]) {
            case AND:
                return new AndFormula(getFormula(size - 1), getFormula(size - 1));
            case ATOMIC:
                return getAtomicFormula();
            case EVENTUALLY:
                return new EventuallyFormula(getFormula(size - 1), getInterval(false));
            case GLOBALLY:
                return new GloballyFormula(getFormula(size - 1), getInterval(false));
            case HYSTORICALLY:
                return new HystoricallyFormula(getFormula(size - 1), getInterval(true));
            case NOT:
                return new NegationFormula(getFormula(size - 1));
            case ONCE:
                return new OnceFormula(getFormula(size - 1), getInterval(true));
            case OR:
                return new OrFormula(getFormula(size - 1), getFormula(size - 1));
            case SINCE:
                return new SinceFormula(getFormula(size - 1), getFormula(size - 1), getInterval(true));
            case UNTIL:
                return new UntilFormula(getFormula(size - 1), getFormula(size - 1), getInterval(false));
        }
        return null;
    }

    public abstract FormulaType[] getFormulaType();

    protected Interval getInterval(boolean isNullable) {
        if (isNullable && random.nextBoolean()) {
            return null;
        }
        double start = random.nextDouble() * maxTime;
        double end = start + random.nextDouble() * maxTime + EPS;
        return new Interval(start, end);
    }

    public Random getRandom() {
        return random;
    }
}
