package eu.quanticol.moonlight.util;

import eu.quanticol.moonlight.io.FormulaType;
import eu.quanticol.moonlight.io.FormulaTypeSelector;

public class PastFormulaGenerator extends FormulaGenerator {


    @Override
    public FormulaType[] getFormulaType() {
        return FormulaTypeSelector.getPastFormulas();
    }
}
