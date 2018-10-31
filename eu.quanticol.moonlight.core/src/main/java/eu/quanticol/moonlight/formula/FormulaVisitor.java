/**
 * 
 */
package eu.quanticol.moonlight.formula;

/**
 * @author loreti
 *
 */
public interface FormulaVisitor<T,R> {

	public R visit(AtomicFormula atomicFormula, T parameters);

	public R visit(AndFormula andFormula, T parameters);

	public R visit(NegationFormula negationFormula, T parameters);

	public R visit(OrFormula orFormula, T parameters);

	public R visit(EventuallyFormula eventuallyFormula, T parameters);

	public R visit(GloballyFormula globallyFormula, T parameters);

	public R visit(UntilFormula untilFormula, T parameters);
	
	public R monitor(Formula f, T parameters);
	
}
