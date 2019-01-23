/**
 * 
 */
package eu.quanticol.moonlight.io;

import eu.quanticol.moonlight.formula.AndFormula;
import eu.quanticol.moonlight.formula.AtomicFormula;
import eu.quanticol.moonlight.formula.EventuallyFormula;
import eu.quanticol.moonlight.formula.Formula;
import eu.quanticol.moonlight.formula.FormulaVisitor;
import eu.quanticol.moonlight.formula.GloballyFormula;
import eu.quanticol.moonlight.formula.HystoricallyFormula;
import eu.quanticol.moonlight.formula.Interval;
import eu.quanticol.moonlight.formula.NegationFormula;
import eu.quanticol.moonlight.formula.OnceFormula;
import eu.quanticol.moonlight.formula.OrFormula;
import eu.quanticol.moonlight.formula.SinceFormula;
import eu.quanticol.moonlight.formula.UntilFormula;

/**
 * @author loreti
 *
 */
public class FormulaToBreach implements FormulaVisitor<String,String> {

	@Override
	public String visit(AtomicFormula atomicFormula, String parameters) {
		return atomicFormula.getAtomicId();
	}

	@Override
	public String visit(AndFormula andFormula, String parameters) {
        return "( " + andFormula.getFirstArgument().accept(this, parameters) + 
        		" /\\ " + andFormula.getSecondArgument().accept(this, parameters) + " )";
	}

	
	
	@Override
	public String visit(NegationFormula negationFormula, String parameters) {
        return "!" + negationFormula.accept(this, parameters);
	}

	@Override
	public String visit(OrFormula orFormula, String parameters) {
        return "( " + orFormula.accept(this, parameters) + " \\/ " + orFormula.accept(this, parameters) + " )";
	}

	@Override
	public String visit(EventuallyFormula eventuallyFormula, String parameters) {
        return " <>_" + intervalToTaliro(eventuallyFormula.getInterval()) + " " + eventuallyFormula.getArgument().accept(this, parameters);
	}

	private String intervalToTaliro(Interval interval) {
        return "{" + interval.getStart() + "," + interval.getEnd() + "}";
	}

	@Override
	public String visit(GloballyFormula globallyFormula, String parameters) {
        return "( []_" + intervalToTaliro(globallyFormula.getInterval()) + " " + globallyFormula.getArgument().accept(this, parameters) + " )";
	}

	@Override
	public String visit(UntilFormula untilFormula, String parameters) {
        return "( " + untilFormula.getFirstArgument().accept(this, parameters) + " U_" + intervalToTaliro(untilFormula.getInterval()) + " " + untilFormula.getSecondArgument().accept(this, parameters) + " )";
	}


	@Override
	public String visit(SinceFormula sinceFormula, String parameters) {
		throw new IllegalArgumentException("Past formulas are not compatible with Breach!");
	}

	@Override
	public String visit(HystoricallyFormula hystoricallyFormula, String parameters) {
		throw new IllegalArgumentException("Past formulas are not compatible with Breach!");
	}

	@Override
	public String visit(OnceFormula onceFormula, String parameters) {
		throw new IllegalArgumentException("Past formulas are not compatible with Breach!");
	}

	public String toBreach(Formula formula) {
		return formula.accept(this, null);
	}


}
