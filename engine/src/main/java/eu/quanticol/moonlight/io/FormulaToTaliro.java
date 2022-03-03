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

import java.util.Arrays;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * @author loreti
 *
 */
public class FormulaToTaliro implements FormulaVisitor<String,String> {

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
        return "! (" + negationFormula.getArgument().accept(this, parameters) +")";
	}

	@Override
	public String visit(OrFormula orFormula, String parameters) {
        return "( " + orFormula.getFirstArgument().accept(this, parameters) + " \\/ " + orFormula.getSecondArgument().accept(this, parameters) + " )";
	}

	@Override
	public String visit(EventuallyFormula eventuallyFormula, String parameters) {
        return "( <>_" + intervalToTaliro(eventuallyFormula.getInterval()) + " " + eventuallyFormula.getArgument().accept(this, parameters)+ " )";
	}

	private String intervalToTaliro(Interval interval) {
        return "[" + interval.getStart() + "," + interval.getEnd() + "]";
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
		throw new IllegalArgumentException("Past formulas are not compatible with Taliro!");
	}

	@Override
	public String visit(HistoricallyFormula historicallyFormula, String parameters) {
		throw new IllegalArgumentException("Past formulas are not compatible with Taliro!");
	}

	@Override
	public String visit(OnceFormula onceFormula, String parameters) {
		throw new IllegalArgumentException("Past formulas are not compatible with Taliro!");
	}

	public String toTaliro(Formula formula) {
		return "psi ='"+formula.accept(this, null)+"';";
	}

	public String createPrefix(Map<String,Integer> variables){
		BiFunction<String, Integer, String> prefix = (name,index) -> "pred("+index+").str = \'"+name+"\';\npred("+index+").A   =  "+ createPredicateMAtrix(variables.size(), index-1)+";\npred("+index+").b   =  0;\n";
		StringBuffer buffer = new StringBuffer();
		buffer.append("pred = struct();\n");
//		String[] names = variables.getVariableNames();
//		for (int i = 0; i < names.length; i++) {
//			buffer.append(prefix.apply(names[i],i+1));
//		}
		variables.forEach((v,i) -> {
			buffer.append(prefix.apply(v,i+1));			
		});
		buffer.append("taliro=@(X,T) fw_taliro(psi,pred,X,T);\n");
		return buffer.toString();
	}

	private String createPredicateMAtrix(int n, int index){
		if (n==1){
			return "1";}
		int[] array = new int[n];
		array[index]=-1;
		return Arrays.toString(array);
	}

}
