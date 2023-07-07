package io.github.moonlightsuite.moonlight.script;

import org.antlr.v4.runtime.ParserRuleContext;

import java.util.List;

public class TypeChecker extends MoonLightScriptBaseVisitor<MoonLightType> {


    private final boolean isSpatial;

    public static MoonLightType inferType(List<MoonLightParseError> errors, TypeEnvironment typeEnvironment, MoonLightScriptParser.ExpressionContext value) {
        TypeChecker tc = new TypeChecker(errors, typeEnvironment, EvaluationContext.NONE, false);
        return value.accept(tc);
    }

    public static boolean checkType(List<MoonLightParseError> errors, TypeEnvironment typeEnvironment, MoonLightType expected, MoonLightScriptParser.ExpressionContext value) {
        TypeChecker tc = new TypeChecker(errors, typeEnvironment, EvaluationContext.NONE, false);
        return tc.checkType(expected, value);
    }

    public enum EvaluationContext {
        NONE,
        INTERVAL,
        EDGE,
        STATE
    }

    private final List<MoonLightParseError> errors;
    private final TypeEnvironment typeEnvironment;
    private final EvaluationContext evaluationContext;

    public TypeChecker(List<MoonLightParseError> errors,
                       TypeEnvironment typeEnvironment,
                       EvaluationContext evaluationContext,
                       boolean isSpatial) {
        this.errors = errors;
        this.evaluationContext = evaluationContext;
        this.typeEnvironment = typeEnvironment;
        this.isSpatial = isSpatial;
    }

    @Override
    public MoonLightType visitBinaryMathCallExpression(MoonLightScriptParser.BinaryMathCallExpressionContext ctx) {
        checkReal(ctx);
        checkReal(ctx);
        return MoonLightType.REAL;
    }

    public void checkReal(ParserRuleContext ctx) {
        checkType(MoonLightType.REAL,ctx);
    }

    public void checkBoolean(ParserRuleContext ctx) {
        checkType(MoonLightType.BOOLEAN,ctx);
    }

    public boolean checkType(MoonLightType expected, ParserRuleContext ctx) {
        MoonLightType type = ctx.accept(this);
        if (!expected.isCompatible(type)) {
            errors.add(MoonLightParseError.getTypeError(expected,type,ctx));
            return false;
        } else {
            return true;
        }
    }

    public MoonLightType infer(MoonLightScriptParser.ExpressionContext value) {
        return value.accept(this);
    }

    @Override
    public MoonLightType visitImplyExpression(MoonLightScriptParser.ImplyExpressionContext ctx) {
        checkBoolean(ctx.left);
        checkBoolean(ctx.right);
        return MoonLightType.BOOLEAN;
    }

    @Override
    public MoonLightType visitNotExpression(MoonLightScriptParser.NotExpressionContext ctx) {
        checkBoolean(ctx.arg);
        return MoonLightType.BOOLEAN;
    }

    @Override
    public MoonLightType visitFalseExpression(MoonLightScriptParser.FalseExpressionContext ctx) {
        return MoonLightType.BOOLEAN;
    }

    @Override
    public MoonLightType visitRelationExpression(MoonLightScriptParser.RelationExpressionContext ctx) {
        MoonLightType leftType = ctx.left.accept(this);
        MoonLightType rightType = ctx.right.accept(this);
        if (!leftType.canBeComparedWith(ctx.op.getText(),rightType)) {
            errors.add(MoonLightParseError.illegalComparison(leftType,rightType,ctx));
        }
        return MoonLightType.BOOLEAN;
    }

    @Override
    public MoonLightType visitOnceExpression(MoonLightScriptParser.OnceExpressionContext ctx) {
        return checkUnaryTemporalFormula(ctx, ctx.interval(),ctx.argument);
    }

    private MoonLightType checkUnaryTemporalFormula(ParserRuleContext parent, MoonLightScriptParser.IntervalContext interval, MoonLightScriptParser.ExpressionContext argument) {
        if (this.evaluationContext != EvaluationContext.STATE) {
            errors.add(MoonLightParseError.illegalUseOfTemporalOperators(parent));
        } else {
            checkBoolean(argument);
            checkTimeInterval(interval);
        }
        return MoonLightType.BOOLEAN;
    }

    private MoonLightType checkUnarySpatialFormula(ParserRuleContext parent, MoonLightScriptParser.IntervalContext interval, MoonLightScriptParser.ExpressionContext distance, MoonLightScriptParser.ExpressionContext argument) {
        if (!this.isSpatial) {
            errors.add(MoonLightParseError.spaceFormulasInTemporalMonitors(parent));
        } else {
            if (this.evaluationContext != EvaluationContext.STATE) {
                errors.add(MoonLightParseError.illegalUseOfTemporalOperators(parent));
            } else {
                checkBoolean(argument);
                checkSpaceInterval(interval);
                checkDistance(distance);
            }
        }
        return MoonLightType.BOOLEAN;
    }

    private void checkDistance(MoonLightScriptParser.ExpressionContext distance) {
        if (distance != null) {
            TypeChecker tc = new TypeChecker(errors,typeEnvironment,EvaluationContext.EDGE,isSpatial);
            tc.checkReal(distance);
        }
    }

    private MoonLightType checkBinaryTemporalFormula(ParserRuleContext parent, MoonLightScriptParser.IntervalContext interval, MoonLightScriptParser.ExpressionContext left, MoonLightScriptParser.ExpressionContext right) {
        if (this.evaluationContext != EvaluationContext.STATE) {
            errors.add(MoonLightParseError.illegalUseOfTemporalOperators(parent));
        } else {
            checkBoolean(left);
            checkBoolean(right);
            checkTimeInterval(interval);
        }
        return MoonLightType.BOOLEAN;
    }

    private MoonLightType checkBinarySpatialFormula(ParserRuleContext parent, MoonLightScriptParser.IntervalContext interval, MoonLightScriptParser.ExpressionContext distance, MoonLightScriptParser.ExpressionContext left, MoonLightScriptParser.ExpressionContext right) {
        if (!this.isSpatial) {
            errors.add(MoonLightParseError.spaceFormulasInTemporalMonitors(parent));
        } else {
            if (this.evaluationContext != EvaluationContext.STATE) {
                errors.add(MoonLightParseError.illegalUseOfTemporalOperators(parent));
            } else {
                checkBoolean(left);
                checkBoolean(right);
                checkSpaceInterval(interval);
                checkDistance(distance);
            }
        }
        return MoonLightType.BOOLEAN;
    }

    private void checkTimeInterval(MoonLightScriptParser.IntervalContext interval) {
        checkInterval(interval,false);
    }

    private void checkSpaceInterval(MoonLightScriptParser.IntervalContext interval) {
        checkInterval(interval,true);
    }

    private void checkInterval(MoonLightScriptParser.IntervalContext interval, boolean isSpatial) {
        if (interval != null) {
            TypeChecker tc = new TypeChecker(errors,typeEnvironment,EvaluationContext.INTERVAL,isSpatial);
            tc.checkReal(interval.from);
            tc.checkReal(interval.to);
        }
    }

    @Override
    public MoonLightType visitBracketExpression(MoonLightScriptParser.BracketExpressionContext ctx) {
        return ctx.expression().accept(this);
    }

    @Override
    public MoonLightType visitUntilExpression(MoonLightScriptParser.UntilExpressionContext ctx) {
        return checkBinaryTemporalFormula(ctx,ctx.interval(),ctx.left,ctx.right);
    }

    @Override
    public MoonLightType visitEverywhereExpression(MoonLightScriptParser.EverywhereExpressionContext ctx) {
        return checkUnarySpatialFormula(ctx,ctx.interval(),ctx.distanceExpression,ctx.argument);
    }

    @Override
    public MoonLightType visitAndExpression(MoonLightScriptParser.AndExpressionContext ctx) {
        checkBoolean(ctx.left);
        checkBoolean(ctx.right);
        return MoonLightType.BOOLEAN;
    }

    @Override
    public MoonLightType visitTrueExpression(MoonLightScriptParser.TrueExpressionContext ctx) {
        return MoonLightType.BOOLEAN;
    }

    @Override
    public MoonLightType visitUnaryMathCallExpression(MoonLightScriptParser.UnaryMathCallExpressionContext ctx) {
        checkReal(ctx.argument);
        return MoonLightType.REAL;
    }

    @Override
    public MoonLightType visitNextExpression(MoonLightScriptParser.NextExpressionContext ctx) {
        return checkUnarySpatialFormula(ctx,null,ctx.distanceExpression,ctx.argument);
    }

    @Override
    public MoonLightType visitRealExpression(MoonLightScriptParser.RealExpressionContext ctx) {
        return MoonLightType.REAL;
    }

    @Override
    public MoonLightType visitUnaryExpression(MoonLightScriptParser.UnaryExpressionContext ctx) {
        return checkNumberType(ctx.arg);
    }

    private MoonLightType checkNumberType(MoonLightScriptParser.ExpressionContext arg) {
        MoonLightType argumentType = arg.accept(this);
        if (!argumentType.isANumber()) {
            errors.add(MoonLightParseError.numericalValueExpected(arg,argumentType));
            return MoonLightType.INT;
        }
        return argumentType;
    }

    @Override
    public MoonLightType visitSinceExpression(MoonLightScriptParser.SinceExpressionContext ctx) {
        return checkBinaryTemporalFormula(ctx,ctx.interval(),ctx.left,ctx.right);
    }

    @Override
    public MoonLightType visitReferenceExpression(MoonLightScriptParser.ReferenceExpressionContext ctx) {
        String name = ctx.name.getText();
        if (!typeEnvironment.exists(name)) {
            errors.add(MoonLightParseError.useOfAnUnknownName(name, ctx));
            return MoonLightType.NONE;
        }
        if (!typeEnvironment.isValidIn(name, evaluationContext)) {
            errors.add(MoonLightParseError.illegalUseOfSymbol(name, ctx));
        }
        checkArguments(ctx, typeEnvironment.getArguments(name),ctx.args);
        return typeEnvironment.getTypeOf(name);
    }

    private void checkArguments(MoonLightScriptParser.ExpressionContext parent, MoonLightType[] expected, List<MoonLightScriptParser.ExpressionContext> args) {
        MoonLightScriptParser.ExpressionContext[] actual = new MoonLightScriptParser.ExpressionContext[0];
        if (args != null) {
            actual = args.toArray(actual);
        }
        if (expected.length != actual.length) {
            errors.add(MoonLightParseError.wrongNumberOfParameters(expected.length, actual.length, parent));
        } else {
            for(int i=0 ; i<expected.length ; i++) {
                checkType(expected[i],actual[i]);
            }
        }

    }

    @Override
    public MoonLightType visitIntExpression(MoonLightScriptParser.IntExpressionContext ctx) {
        return MoonLightType.INT;
    }

    @Override
    public MoonLightType visitGloballyExpression(MoonLightScriptParser.GloballyExpressionContext ctx) {
        return checkUnaryTemporalFormula(ctx, ctx.interval(), ctx.argument);
    }

    @Override
    public MoonLightType visitReachExpression(MoonLightScriptParser.ReachExpressionContext ctx) {
        return checkBinarySpatialFormula(ctx,ctx.interval(),ctx.distanceExpression,ctx.left,ctx.right);
    }

    @Override
    public MoonLightType visitIfThenElseExpression(MoonLightScriptParser.IfThenElseExpressionContext ctx) {
        checkBoolean(ctx.guard);
        MoonLightType thenType = ctx.thenExpression.accept(this);
        MoonLightType elseType = ctx.elseExpression.accept(this);
        if (thenType.isCompatible(elseType)) {
            return MoonLightType.mix(thenType,elseType);
        } else {
            errors.add(MoonLightParseError.getTypeError(thenType,elseType,ctx.elseExpression));
            return thenType;
        }
    }

    @Override
    public MoonLightType visitOrExpression(MoonLightScriptParser.OrExpressionContext ctx) {
        checkBoolean(ctx.left);
        checkBoolean(ctx.right);
        return MoonLightType.BOOLEAN;
    }

    @Override
    public MoonLightType visitEscapeExpression(MoonLightScriptParser.EscapeExpressionContext ctx) {
        return checkUnarySpatialFormula(ctx, ctx.interval(), ctx.distanceExpression, ctx.argument);
    }

    @Override
    public MoonLightType visitSomewhereExpression(MoonLightScriptParser.SomewhereExpressionContext ctx) {
        return checkUnarySpatialFormula(ctx, ctx.interval(), ctx.distanceExpression, ctx.argument);
    }

    @Override
    public MoonLightType visitMulDivExpression(MoonLightScriptParser.MulDivExpressionContext ctx) {
        return checkBinaryArithmeticExpression(ctx.left,ctx.right);
    }

    private MoonLightType checkBinaryArithmeticExpression(MoonLightScriptParser.ExpressionContext left, MoonLightScriptParser.ExpressionContext right) {
        MoonLightType leftType = checkNumberType(left);
        MoonLightType rightType = checkNumberType(right);
        return MoonLightType.mix(leftType,rightType);
    }

    @Override
    public MoonLightType visitSumDifExpression(MoonLightScriptParser.SumDifExpressionContext ctx) {
        return checkBinaryArithmeticExpression(ctx.left, ctx.right);
    }

    @Override
    public MoonLightType visitHistoricallyExpression(MoonLightScriptParser.HistoricallyExpressionContext ctx) {
        return checkUnaryTemporalFormula(ctx,ctx.interval(),ctx.argument);
    }

    @Override
    public MoonLightType visitEventuallyExpression(MoonLightScriptParser.EventuallyExpressionContext ctx) {
        return checkUnaryTemporalFormula(ctx,ctx.interval(),ctx.argument);
    }

    @Override
    public MoonLightType visitInfinityExpression(MoonLightScriptParser.InfinityExpressionContext ctx) {
        return MoonLightType.REAL;
    }

}
