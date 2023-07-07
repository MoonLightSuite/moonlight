package io.github.moonlightsuite.moonlight.util;

import io.github.moonlightsuite.moonlight.io.FormulaType;
import io.github.moonlightsuite.moonlight.io.FormulaTypeSelector;

import java.util.Random;

public class BothFormulaGenerator extends FormulaGenerator {
    public BothFormulaGenerator(String... ids) {
        super(ids);
    }

    BothFormulaGenerator(Random random, double maxTime, String... ids) {
        super(random, maxTime, ids);
    }

    @Override
    public FormulaType[] getFormulaType() {
        return FormulaTypeSelector.getBothFormulas();
    }
}
