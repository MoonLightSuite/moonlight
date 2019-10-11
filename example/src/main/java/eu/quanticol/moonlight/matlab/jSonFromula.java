package eu.quanticol.moonlight.matlab;


import eu.quanticol.moonlight.formula.Formula;
import eu.quanticol.moonlight.io.FormulaJSonIO;
import eu.quanticol.moonlight.util.BothFormulaGenerator;
import eu.quanticol.moonlight.util.FormulaGenerator;

public class jSonFromula {


    public static void main(String[] args) throws Exception {
        FormulaGenerator generator = new BothFormulaGenerator("a","b","c");
        FormulaJSonIO fJSonIO = FormulaJSonIO.getInstance();
        Formula f1 = generator.getFormula();
        String code = fJSonIO.toJson( f1 );
        System.out.print(code);
    }


}
