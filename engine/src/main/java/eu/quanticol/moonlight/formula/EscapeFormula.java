/**
 * 
 */
package eu.quanticol.moonlight.formula;

/**
 * @author loreti
 *
 */
public class EscapeFormula implements UnaryFormula, SpatialFormula {

	private final String distanceFunctionId;
	private final Formula argument;
	

	/**
	 * @param distanceFunctionId
	 * @param argument
	 */
	public EscapeFormula(String distanceFunctionId, Formula argument) {
		super();
		this.distanceFunctionId = distanceFunctionId;
		this.argument = argument;
	}

	@Override
	public <T, R> R accept(FormulaVisitor<T, R> visitor, T parameters) {
		return visitor.visit(this, parameters);
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
