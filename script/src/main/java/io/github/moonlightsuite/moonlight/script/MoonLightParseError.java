package io.github.moonlightsuite.moonlight.script;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

public class MoonLightParseError {

    public static final String WRONG_TYPE_MESSAGE = "Illegal type error: expected %s is %s.";
    public static final String ILLEGAL_COMPARISON_MESSAGE = "A value of type %s cannot be compared with a value of type %s.";
    public static final String ILLEGAL_FORMULA_OPERATOR = "Formula operators are not allowed in this context!";
    public static final String ILLEGAL_SPACE_FORMULA = "Illegal formula: space formulas are not allowed without a space definition.";
    private static final String A_NUMERICAL_VALUE_IS_EXPECTED = "A numerical value is expected while it is %s.";
    private static final String AN_UNKNOWN_NAME_IS_USED = "Symbol %s is unknown.";
    public static final String ILLEGAL_USE_OF_NAME = "The use of symbol %s is not allowed in this context.";
    public static final String WRONG_NUMBER_OF_PARAMETERS = "Wrong number of parameters: expected %d, are %d.";
    public static final String DUPLICATED_NAME = "Symbol %s already declared at line %d (char %d).";
    public static final String ILLEGAL_FUNCTION_NAME = "Function %s at line %d (char %d) is unknown.";
    public static final String EXPRESSION_EVALUATION_ERROR = "An error occurred in the evaluation of expression at line %d (char %d).";

    private final String message;

    private final int line;

    private final int charPositionInLine;

    public MoonLightParseError(String message, Token start) {
        this(message, start.getLine(), start.getCharPositionInLine());
    }

    public MoonLightParseError(String message, int line, int charPositionInLine) {
        this.message = message;
        this.line = line;
        this.charPositionInLine = charPositionInLine;
    }

    public static MoonLightParseError illegalComparison(MoonLightType leftType, MoonLightType rightType, MoonLightScriptParser.RelationExpressionContext ctx) {
        return new MoonLightParseError(String.format(ILLEGAL_COMPARISON_MESSAGE,leftType,rightType),ctx.start);
    }

    public static MoonLightParseError illegalUseOfTemporalOperators(ParserRuleContext ctx) {
        return new MoonLightParseError(ILLEGAL_FORMULA_OPERATOR,ctx.start);
    }

    public static MoonLightParseError spaceFormulasInTemporalMonitors(ParserRuleContext ctx) {
        return new MoonLightParseError(ILLEGAL_SPACE_FORMULA,ctx.start);
    }

    public static MoonLightParseError numericalValueExpected(MoonLightScriptParser.ExpressionContext ctx, MoonLightType type) {
        return new MoonLightParseError(String.format(A_NUMERICAL_VALUE_IS_EXPECTED,type),ctx.start);
    }

    public static MoonLightParseError useOfAnUnknownName(String name, MoonLightScriptParser.ExpressionContext ctx) {
        return new MoonLightParseError(String.format(AN_UNKNOWN_NAME_IS_USED, name), ctx.start);
    }

    public static MoonLightParseError illegalUseOfSymbol(String name, MoonLightScriptParser.ExpressionContext ctx) {
        return new MoonLightParseError(String.format(ILLEGAL_USE_OF_NAME,name), ctx.start);
    }

    public static MoonLightParseError wrongNumberOfParameters(int expectedArgs, int actualArgs, MoonLightScriptParser.ExpressionContext parent) {
        return new MoonLightParseError(String.format(WRONG_NUMBER_OF_PARAMETERS,expectedArgs,actualArgs),parent.start);
    }

    public static MoonLightParseError nameAlreadyDeclared(String name, ParserRuleContext existing, ParserRuleContext duplicated) {
        return new MoonLightParseError(String.format(DUPLICATED_NAME,name,existing.start.getLine(),existing.start.getCharPositionInLine()),duplicated.start);
    }

    public static MoonLightParseError illegalFunctionName(String name, Token start) {
        return new MoonLightParseError(String.format(ILLEGAL_FUNCTION_NAME,name, start.getLine(), start.getCharPositionInLine()),start);
    }

    public static MoonLightParseError realExpressionEvaluationError(ParserRuleContext ctx) {
        return new MoonLightParseError(String.format(EXPRESSION_EVALUATION_ERROR,ctx.start.getLine(),ctx.start.getCharPositionInLine()),ctx.start);
    }

    public String getMessage() {
        return message;
    }

    public static MoonLightParseError getTypeError(MoonLightType expected, MoonLightType actual, ParserRuleContext ctx) {
        return new MoonLightParseError(
                String.format(WRONG_TYPE_MESSAGE,expected,actual),
                ctx.start
        );
    }
}
