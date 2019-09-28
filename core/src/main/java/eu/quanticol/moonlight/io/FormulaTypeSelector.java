package eu.quanticol.moonlight.io;

import static eu.quanticol.moonlight.io.FormulaType.*;

public class FormulaTypeSelector {

    public static FormulaType[] getPastFormulas() {
        return new FormulaType[]{ATOMIC, AND, NOT, OR, SINCE, HYSTORICALLY, ONCE};
    }

    public static FormulaType[] getFuturePastFormulas() {
        return new FormulaType[]{ATOMIC, AND, NOT, OR, EVENTUALLY, GLOBALLY, UNTIL};
    }

    public static FormulaType[] getBothFormulas() {
        return new FormulaType[]{ATOMIC, AND, NOT, OR, EVENTUALLY, GLOBALLY, UNTIL, SINCE, HYSTORICALLY, ONCE};
    }
}
