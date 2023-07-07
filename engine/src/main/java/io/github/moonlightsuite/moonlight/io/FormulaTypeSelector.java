package io.github.moonlightsuite.moonlight.io;

import static io.github.moonlightsuite.moonlight.io.FormulaType.*;

public class FormulaTypeSelector {

    public static FormulaType[] getPastFormulas() {
        return new FormulaType[]{ATOMIC, AND, NOT, OR, SINCE, HISTORICALLY, ONCE};
    }

    public static FormulaType[] getFuturePastFormulas() {
        return new FormulaType[]{ATOMIC, AND, NOT, OR, EVENTUALLY, GLOBALLY, UNTIL};
    }

    public static FormulaType[] getBothFormulas() {
        return new FormulaType[]{ATOMIC, AND, NOT, OR, EVENTUALLY, GLOBALLY, UNTIL, SINCE, HISTORICALLY, ONCE};
    }
}
