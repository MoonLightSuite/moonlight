package eu.quanticol.moonlight.script;

import eu.quanticol.moonlight.io.MoonLightRecord;
import eu.quanticol.moonlight.signal.RecordHandler;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public class BiParametricExpressionEvaluator extends MoonLightScriptBaseVisitor<BiFunction<MoonLightRecord,MoonLightRecord,Double>> {

    private final static Map<String,BiFunction<Double,Double,Double>> binaryFunctionMap = new HashMap<>();

    static {
        binaryFunctionMap.put("atan2", Math::atan2);
        binaryFunctionMap.put("hypot", Math::hypot);
        binaryFunctionMap.put("max", Math::max);
        binaryFunctionMap.put("min", Math::min);
        binaryFunctionMap.put("pow", Math::pow);
    }

    private final static Map<String,Function<Double,Double>> unaryFunctionMap = new HashMap<>();

    static {
        unaryFunctionMap.put("abs", Math::abs);
        unaryFunctionMap.put("acos", Math::acos);
        unaryFunctionMap.put("asin", Math::asin);
        unaryFunctionMap.put("atan", Math::atan);
        unaryFunctionMap.put("cbrt", Math::cbrt);
        unaryFunctionMap.put("ceil", Math::ceil);
        unaryFunctionMap.put("cos", Math::cos);
        unaryFunctionMap.put("cosh", Math::cosh);
        unaryFunctionMap.put("exp", Math::exp);
        unaryFunctionMap.put("expm1", Math::expm1);
        unaryFunctionMap.put("floor", Math::floor);
        unaryFunctionMap.put("log", Math::log);
        unaryFunctionMap.put("log10", Math::log10);
        unaryFunctionMap.put("log1p", Math::log1p);
        unaryFunctionMap.put("signum", Math::signum);
        unaryFunctionMap.put("sin", Math::sin);
        unaryFunctionMap.put("sqrt", Math::sqrt);
        unaryFunctionMap.put("sinh", Math::sinh);
        unaryFunctionMap.put("tan", Math::tan);
    }

    private final List<MoonLightParseError> errors;
    private final NameResolver resolver;
    private final RecordHandler firstGroupHandler; //Record handler for formula parameters;
    private final RecordHandler secondGroupHandler; //Record handler for signal expressions;

    private BiParametricBooleanExpressionEvaluator signalPredicateEvaluator ;

    public BiParametricExpressionEvaluator(
            List<MoonLightParseError> errors,
            NameResolver resolver,
            RecordHandler firstGroupHandler,
            RecordHandler secondGroupHandler) {
        this.errors = errors;
        this.resolver = resolver;
        this.firstGroupHandler = firstGroupHandler;
        this.secondGroupHandler = secondGroupHandler;
    }

    public BiParametricExpressionEvaluator(
            NameResolver resolver,
            RecordHandler firstGroupHandler,
            RecordHandler secondGroupHandler) {
        this(new LinkedList<>(),resolver, firstGroupHandler, secondGroupHandler);
    }

    @Override
    protected BiFunction<MoonLightRecord,MoonLightRecord,Double> defaultResult() {
        return (r,s) -> Double.NaN;
    }

    @Override
    public BiFunction<MoonLightRecord,MoonLightRecord,Double> visitBinaryMathCallExpression(MoonLightScriptParser.BinaryMathCallExpressionContext ctx) {
        BiFunction<Double,Double,Double> opFunction = getBinaryFunction(ctx.binaryMathFunction());
        return doApply(ctx,opFunction,ctx.left.accept(this),ctx.right.accept(this));
    }

    private BiFunction<Double, Double, Double> getBinaryFunction(MoonLightScriptParser.BinaryMathFunctionContext binaryMathFunction) {
        String funName = binaryMathFunction.getText();
        BiFunction<Double,Double,Double> fun = binaryFunctionMap.get(funName);
        if (fun == null) {
            errors.add(MoonLightParseError.illegalFunctionName(funName,binaryMathFunction.start));
            return (x,y) -> Double.NaN;
        }
        return fun;
    }

    private BiFunction<MoonLightRecord,MoonLightRecord,Double> doApply(ParserRuleContext ctx, BiFunction<Double,Double,Double> fun, BiFunction<MoonLightRecord,MoonLightRecord,Double> arg1, BiFunction<MoonLightRecord,MoonLightRecord,Double> arg2) {
        return (r,s) -> fun.apply(arg1.apply(r,s), arg2.apply(r,s));
    }


    @Override
    public BiFunction<MoonLightRecord,MoonLightRecord,Double> visitBracketExpression(MoonLightScriptParser.BracketExpressionContext ctx) {
        return ctx.expression().accept(this);
    }

    @Override
    public BiFunction<MoonLightRecord,MoonLightRecord,Double> visitUnaryMathCallExpression(MoonLightScriptParser.UnaryMathCallExpressionContext ctx) {
        Function<Double,Double> fun = getUnaryFunction(ctx.fun);
        return doApply(ctx,fun,ctx.argument.accept(this));
    }

    private BiFunction<MoonLightRecord,MoonLightRecord,Double> doApply(ParserRuleContext ctx, Function<Double, Double> fun, BiFunction<MoonLightRecord,MoonLightRecord,Double> arg) {
        return (r,s) -> fun.apply(arg.apply(r,s));
    }

    private Function<Double, Double> getUnaryFunction(MoonLightScriptParser.UnaryMathFunctionContext unaryMathFunction) {
        String funName = unaryMathFunction.getText();
        Function<Double,Double> fun = unaryFunctionMap.get(funName);
        if (fun == null) {
            errors.add(MoonLightParseError.illegalFunctionName(funName,unaryMathFunction.start));
            return x -> Double.NaN;
        }
        return fun;

    }

    @Override
    public BiFunction<MoonLightRecord,MoonLightRecord,Double> visitRealExpression(MoonLightScriptParser.RealExpressionContext ctx) {
        return (r,s) -> Double.parseDouble(ctx.getText());
    }

    @Override
    public BiFunction<MoonLightRecord,MoonLightRecord,Double> visitUnaryExpression(MoonLightScriptParser.UnaryExpressionContext ctx) {
        Function<Double,Double> fun;
        if (ctx.op.getText().equals("-")) {
            fun = x -> -x;
            return doApply(ctx,fun,ctx.arg.accept(this));
        } else {
            return ctx.arg.accept(this);
        }
    }

    @Override
    public BiFunction<MoonLightRecord,MoonLightRecord,Double> visitReferenceExpression(MoonLightScriptParser.ReferenceExpressionContext ctx) {
        String name = ctx.name.getText();
        double value = resolver.get(name);
        if (!Double.isNaN(value)) {
            return (r,s) -> value;
        }
        if (firstGroupHandler.isAVariable(name)) {
            int idx = firstGroupHandler.getVariableIndex(name);
            return (r,s) -> r.getDoubleOf(idx);
        }
        if (secondGroupHandler.isAVariable(name)) {
            int vIndex = secondGroupHandler.getVariableIndex(name);
            return (r,s) -> s.getDoubleOf(vIndex);
        }
        errors.add(MoonLightParseError.useOfAnUnknownName(name,ctx));
        return (r,s) -> Double.NaN;
    }

    @Override
    public BiFunction<MoonLightRecord,MoonLightRecord,Double>  visitIntExpression(MoonLightScriptParser.IntExpressionContext ctx) {
        double i = (double) Integer.parseInt(ctx.getText());
        return (r,s) -> i;
    }

    @Override
    public BiFunction<MoonLightRecord,MoonLightRecord,Double>  visitIfThenElseExpression(MoonLightScriptParser.IfThenElseExpressionContext ctx) {
        BiParametricBooleanExpressionEvaluator signalPredicateEvaluator = getSignalPredicateEvaluator();
        BiFunction<MoonLightRecord, MoonLightRecord, Boolean> guard = ctx.guard.accept(signalPredicateEvaluator);
        BiFunction<MoonLightRecord,MoonLightRecord,Double>  thenBranch = ctx.thenExpression.accept(this);
        BiFunction<MoonLightRecord,MoonLightRecord,Double>  elseBranch = ctx.elseExpression.accept( this);
        return (r,s) -> (guard.apply(r,s)?thenBranch.apply(r,s):elseBranch.apply(r,s));
    }

    private synchronized BiParametricBooleanExpressionEvaluator getSignalPredicateEvaluator() {
        if (signalPredicateEvaluator == null) {
            signalPredicateEvaluator = new BiParametricBooleanExpressionEvaluator(this);
        }
        return signalPredicateEvaluator;
    }

    @Override
    public BiFunction<MoonLightRecord,MoonLightRecord,Double> visitMulDivExpression(MoonLightScriptParser.MulDivExpressionContext ctx) {
        BiFunction<Double,Double,Double> fun;
        if (ctx.op.getText().equals("*")) {
            fun = (x,y) -> x*y;
        } else {
            fun = (x,y) -> x/y;
        }
        return doApply(ctx,fun,ctx.left.accept(this),ctx.right.accept(this));
    }

    @Override
    public BiFunction<MoonLightRecord,MoonLightRecord,Double> visitSumDifExpression(MoonLightScriptParser.SumDifExpressionContext ctx) {
        BiFunction<Double,Double,Double> fun;
        if (ctx.op.getText().equals("+")) {
            fun = Double::sum;
        } else {
            if (ctx.op.getText().equals("-")) {
                fun = (x,y) -> x-y;
            } else {
                fun = (x,y) -> x%y;
            }
        }
        return doApply(ctx,fun,ctx.left.accept(this),ctx.right.accept(this));
    }

    @Override
    public BiFunction<MoonLightRecord,MoonLightRecord,Double>  visitInfinityExpression(MoonLightScriptParser.InfinityExpressionContext ctx) {
        return (r,s) -> Double.POSITIVE_INFINITY;
    }

    public BiFunction<MoonLightRecord,MoonLightRecord,Double>  eval(MoonLightScriptParser.ExpressionContext value) {
        return value.accept(this);
    }
}
