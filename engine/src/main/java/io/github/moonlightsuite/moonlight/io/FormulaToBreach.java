/**
 *
 */
package io.github.moonlightsuite.moonlight.io;

import io.github.moonlightsuite.moonlight.formula.classic.AndFormula;
import io.github.moonlightsuite.moonlight.formula.AtomicFormula;
import io.github.moonlightsuite.moonlight.formula.temporal.EventuallyFormula;
import io.github.moonlightsuite.moonlight.core.formula.Formula;
import io.github.moonlightsuite.moonlight.core.formula.FormulaVisitor;
import io.github.moonlightsuite.moonlight.formula.temporal.GloballyFormula;
import io.github.moonlightsuite.moonlight.formula.temporal.HistoricallyFormula;
import io.github.moonlightsuite.moonlight.core.formula.Interval;
import io.github.moonlightsuite.moonlight.formula.classic.NegationFormula;
import io.github.moonlightsuite.moonlight.formula.temporal.OnceFormula;
import io.github.moonlightsuite.moonlight.formula.classic.OrFormula;
import io.github.moonlightsuite.moonlight.formula.temporal.SinceFormula;
import io.github.moonlightsuite.moonlight.formula.temporal.UntilFormula;

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
