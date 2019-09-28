/**
 * 
 */
package eu.quanticol.moonlight.formula;

/**
 * @author loreti
 *
 */
public class EverywhereFormula implements Formula {

	private final String intervalId;
	private final String distanceFunctionId;
	private final Formula argument;
	

	/**
	 * @param intervalId
	 * @param distanceFunctionId
	 * @param formula
	 */
	public EverywhereFormula(String intervalId, String distanceFunctionId, Formula argument) {
		super();
		this.intervalId = intervalId;
		this.distanceFunctionId = distanceFunctionId;
		this.argument = argument;
	}

	@Override
	public <T, R> R accept(FormulaVisitor<T, R> visitor, T parameters) {
		return visitor.visit(this, parameters);
	}

	/**
	 * @return the intervalId
	 */
	public String getIntervalId() {
		return intervalId;
	}

	/**
	 * @return the distanceFunctionId
	 */
	public String getDistanceFunctionId() {
		return distanceFunctionId;
	}

	/**
	 * @return the formula
	 */
	public Formula getArgument() {
		return argument;
	}

}
