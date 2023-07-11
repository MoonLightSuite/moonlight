package io.github.moonlightsuite.moonlight.offline;

public class TestParser {
    public static void main(String[] args) {
        Formula f = new AndFormula(new AtomicFormula("a"), new AtomicFormula("b"));
        System.out.println(f);
    }
}

interface SetFormula { }

sealed interface Formula extends SetFormula { }

sealed interface BinaryFormula extends Formula {
    Formula getFirstArgument();
    Formula getSecondArgument();
}

sealed interface CompositeFormula extends Formula {
    Formula getFormula();
}

record ImpliesFormula(Formula firstArgument, Formula secondArgument) implements CompositeFormula {
    public Formula getFormula() {
        return new OrFormula(new NotFormula(firstArgument), secondArgument);
    }
}

record AndFormula(Formula firstArgument, Formula secondArgument) implements BinaryFormula {

    @Override
    public Formula getFirstArgument() {
        return null;
    }

    @Override
    public Formula getSecondArgument() {
        return null;
    }
}

record OrFormula(Formula firstArgument, Formula secondArgument) implements BinaryFormula {
    @Override
    public Formula getFirstArgument() {
        return null;
    }

    @Override
    public Formula getSecondArgument() {
        return null;
    }
}

sealed interface UnaryFormula extends Formula {
    Formula getArgument();
}

record NotFormula(Formula argument) implements UnaryFormula {
    @Override
    public Formula getArgument() {
        return argument;
    }
}

record AtomicFormula(String atomId) implements Formula {}
