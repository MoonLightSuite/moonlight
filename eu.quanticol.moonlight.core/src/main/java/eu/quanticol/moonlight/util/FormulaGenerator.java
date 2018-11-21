/**
 * 
 */
package eu.quanticol.moonlight.util;

import java.util.Random;

import eu.quanticol.moonlight.formula.AndFormula;
import eu.quanticol.moonlight.formula.AtomicFormula;
import eu.quanticol.moonlight.formula.EventuallyFormula;
import eu.quanticol.moonlight.formula.Formula;
import eu.quanticol.moonlight.formula.GloballyFormula;
import eu.quanticol.moonlight.formula.HystoricallyFormula;
import eu.quanticol.moonlight.formula.Interval;
import eu.quanticol.moonlight.formula.NegationFormula;
import eu.quanticol.moonlight.formula.OnceFormula;
import eu.quanticol.moonlight.formula.OrFormula;
import eu.quanticol.moonlight.formula.SinceFormula;
import eu.quanticol.moonlight.formula.UntilFormula;
import eu.quanticol.moonlight.io.FormulaType;

/**
 * @author loreti
 *
 */
public class FormulaGenerator {

	private static final int DEFAULT_MAXSIZE = 10;
	private static final double EPS = 0.000001;
	private double DEFAULT_MAXTIME = 100.0;

	private final Random random;
	private int maxSize = DEFAULT_MAXSIZE;
	private String[] atomidId;
	private double maxTime = DEFAULT_MAXTIME ;
	
	public FormulaGenerator( Random random , String ... ids) {
		this.random = random;
		this.atomidId = ids;
	}
	
	public FormulaGenerator(String ... ids) {
		this(new Random(),ids);
	}
	
	public int getMaxSize() {
		return maxSize ;
	}
	
	public Formula getFormula( ) {
		return getFormula(random.nextInt(maxSize));
	}
	
	public Formula getFormula( int size ) {
		if (size == 0) {
			return getAtomicFormula();
		} else {
			return generateFormula( size );
		}
	}

	private Formula getAtomicFormula() {
		return new AtomicFormula(atomidId[random.nextInt(atomidId.length)]);
	}

	private Formula generateFormula(int size, String ... ids) {
		FormulaType[] types = FormulaType.values();
		switch (types[random.nextInt(types.length)]) {
		case AND:
			return new AndFormula(getFormula(size-1), getFormula(size-1));
		case ATOMIC:
			return getAtomicFormula();
		case EVENTUALLY:
			return new EventuallyFormula(getFormula(size-1), getInterval(false)); 
		case GLOBALLY:
			return new GloballyFormula(getFormula(size-1), getInterval(false)); 
		case HYSTORICALLY:
			return new HystoricallyFormula(getFormula(size-1), getInterval(true)); 
		case NOT:
			return new NegationFormula(getFormula(size-1)); 
		case ONCE:
			return new OnceFormula(getFormula(size-1), getInterval(true)); 
		case OR:
			return new OrFormula(getFormula(size-1), getFormula(size-1));
		case SINCE:
			return new SinceFormula(getFormula(size-1), getFormula(size-1), getInterval(true));
		case UNTIL:
			return new UntilFormula(getFormula(size-1), getFormula(size-1), getInterval(true));
		}
		return null;
	}

	private Interval getInterval(boolean isNullable) {
		if (isNullable&&random.nextBoolean()) {
			return null;
		}
		double start = random.nextDouble()*maxTime;
		double end = start+random.nextDouble()*maxTime+EPS;
		return new Interval(start, end);
	}
	
}
