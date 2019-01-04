package eu.quanticol.moonlight.util;

import eu.quanticol.moonlight.io.FormulaType;
import eu.quanticol.moonlight.io.FormulaTypeSelector;

import java.util.Random;

public class FutureFormulaGenerator extends FormulaGenerator {
    FutureFormulaGenerator(Random random, double maxTime, String... ids) {
        super(random, maxTime, ids);
    }

    @Override
    public FormulaType[] getFormulaType() {
        return FormulaTypeSelector.getFuturePastFormulas();
    }
}
