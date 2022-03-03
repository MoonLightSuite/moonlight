/**
 * 
 */
package eu.quanticol.moonlight.formula.spatial;

import eu.quanticol.moonlight.core.formula.Formula;
import eu.quanticol.moonlight.core.formula.FormulaVisitor;

/**
 * @author loreti
 *
 */
public class ReachFormula implements Formula {
	
	private final String distanceFunctionId;
	private final Formula firstArgument;
	private final Formula secondArgument;
	
	
	/**
	 * @param distanceFunctionId
	 * @param firstArgument
	 * @param secondArgument
	 */
	public ReachFormula(Formula firstArgument, String distanceFunctionId, Formula secondArgument) {
		super();
		this.distanceFunctionId = distanceFunctionId;
		this.firstArgument = firstArgument;
		this.secondArgument = secondArgument;
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
	 * @return the firstArgument
	 */
	public Formula getFirstArgument() {
		return firstArgument;
	}


	/**
	 * @return the secondArgument
	 */
	public Formula getSecondArgument() {
		return secondArgument;
	}

}
