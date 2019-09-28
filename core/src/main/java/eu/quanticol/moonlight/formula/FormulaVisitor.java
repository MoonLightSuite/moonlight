/**
 * 
 */
package eu.quanticol.moonlight.formula;

/**
 * @author loreti
 *
 */
public interface FormulaVisitor<T,R> {
	
	public static IllegalArgumentException generateException( Class<?> c ) {
		return new IllegalArgumentException(c.getName()+" is not supported by this visitor!");
	}

	default R visit(AtomicFormula atomicFormula, T parameters) {
		throw generateException(atomicFormula.getClass());
	}

	default R visit(AndFormula andFormula, T parameters) {
		throw generateException(andFormula.getClass());
	}

	default R visit(NegationFormula negationFormula, T parameters) {
		throw generateException(negationFormula.getClass());
	}

	default R visit(OrFormula orFormula, T parameters) {
		throw generateException(orFormula.getClass());
	}

	default R visit(EventuallyFormula eventuallyFormula, T parameters) {
		throw generateException(eventuallyFormula.getClass());
	}

	default R visit(GloballyFormula globallyFormula, T parameters) {
		throw generateException(globallyFormula.getClass());
	}

	default R visit(UntilFormula untilFormula, T parameters) {
		throw generateException(untilFormula.getClass());
	}

	default R visit(SinceFormula sinceFormula, T parameters) {
		throw generateException(sinceFormula.getClass());
	}

	default R visit(HystoricallyFormula hystoricallyFormula, T parameters) {
		throw generateException(hystoricallyFormula.getClass());
	}

	default R visit(OnceFormula onceFormula, T parameters) {
		throw generateException(onceFormula.getClass());
	}
	
	default R visit(SomewhereFormula somewhereFormula, T parameters ) {
		throw generateException(somewhereFormula.getClass());
	}

	default R visit(EverywhereFormula everywhereFormula, T parameters) {
		throw generateException(everywhereFormula.getClass());		
	}
	
	default R visit(ReachFormula reachFormula, T parameters) {
		throw generateException(reachFormula.getClass());		
	}

	default R visit(EscapeFormula escapeFormula, T parameters) {
		throw generateException(escapeFormula.getClass());		
	}
	
	
}
