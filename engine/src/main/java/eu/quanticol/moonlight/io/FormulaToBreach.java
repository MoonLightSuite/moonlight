/**
 * 
 */
package eu.quanticol.moonlight.io;

import eu.quanticol.moonlight.formula.classic.AndFormula;
import eu.quanticol.moonlight.formula.AtomicFormula;
import eu.quanticol.moonlight.formula.temporal.EventuallyFormula;
import eu.quanticol.moonlight.core.formula.Formula;
import eu.quanticol.moonlight.core.formula.FormulaVisitor;
import eu.quanticol.moonlight.formula.temporal.GloballyFormula;
import eu.quanticol.moonlight.formula.temporal.HistoricallyFormula;
import eu.quanticol.moonlight.core.formula.Interval;
import eu.quanticol.moonlight.formula.classic.NegationFormula;
import eu.quanticol.moonlight.formula.temporal.OnceFormula;
import eu.quanticol.moonlight.formula.classic.OrFormula;
import eu.quanticol.moonlight.formula.temporal.SinceFormula;
import eu.quanticol.moonlight.formula.temporal.UntilFormula;

/**
 * @author loreti
 *
 */
public class FormulaToBreach implements FormulaVisitor<String,String> {

	@Override
	public String visit(AtomicFormula atomicFormula, String parameters) {
		return "("+atomicFormula.getAtomicId()+"[t]>=0)";
	}

	@Override
	public String visit(AndFormula andFormula, String parameters) {
        return "( " + andFormula.getFirstArgument().accept(this, parameters) + 
        		" and " + andFormula.getSecondArgument().accept(this, parameters) + " )";
	}

	
	
	@Override
	public String visit(NegationFormula negationFormula, String parameters) {
        return "not" + negationFormula.getArgument().accept(this, parameters);
	}

	@Override
	public String visit(OrFormula orFormula, String parameters) {
        return "( " + orFormula.getFirstArgument().accept(this, parameters) + " or " + orFormula.getSecondArgument().accept(this, parameters) + " )";
	}

	@Override
	public String visit(EventuallyFormula eventuallyFormula, String parameters) {
        return " ( ev_" + intervalToTaliro(eventuallyFormula.getInterval()) + " " + eventuallyFormula.getArgument().accept(this, parameters)+")";
	}

	private String intervalToTaliro(Interval interval) {
        return "[" + interval.getStart() + "," + interval.getEnd() + "]";
	}

	@Override
	public String visit(GloballyFormula globallyFormula, String parameters) {
        return "( alw_" + intervalToTaliro(globallyFormula.getInterval()) + " " + globallyFormula.getArgument().accept(this, parameters) + " )";
	}

	@Override
	public String visit(UntilFormula untilFormula, String parameters) {
        return "( " + untilFormula.getFirstArgument().accept(this, parameters) + " until_" + intervalToTaliro(untilFormula.getInterval()) + " " + untilFormula.getSecondArgument().accept(this, parameters) + " )";
	}


	@Override
	public String visit(SinceFormula sinceFormula, String parameters) {
		throw new IllegalArgumentException("Past formulas are not compatible with Breach!");
	}

	@Override
	public String visit(HistoricallyFormula historicallyFormula, String parameters) {
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
