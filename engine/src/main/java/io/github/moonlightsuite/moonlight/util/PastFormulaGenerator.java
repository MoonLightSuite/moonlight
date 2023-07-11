package io.github.moonlightsuite.moonlight.util;

import io.github.moonlightsuite.moonlight.io.FormulaType;
import io.github.moonlightsuite.moonlight.io.FormulaTypeSelector;

public class PastFormulaGenerator extends FormulaGenerator {


    @Override
    public FormulaType[] getFormulaType() {
        return FormulaTypeSelector.getPastFormulas();
    }
}
