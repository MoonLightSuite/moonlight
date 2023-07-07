package io.github.moonlightsuite.moonlight.script;

import io.github.moonlightsuite.moonlight.core.base.MoonLightRecord;
import io.github.moonlightsuite.moonlight.offline.signal.RecordHandler;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ParametricExpressionEvaluator extends MoonLightScriptBaseVisitor<Function<MoonLightRecord,Double>> {

    private static final Map<String,BiFunction<Double,Double,Double>> binaryFunctionMap = new HashMap<>();

    static {
        binaryFunctionMap.put("atan2", Math::atan2);
        binaryFunctionMap.put("hypot", Math::hypot);
        binaryFunctionMap.put("max", Math::max);
        binaryFunctionMap.put("min", Math::min);
        binaryFunctionMap.put("pow", Math::pow);
    }

    private static final Map<String,Function<Double,Double>> unaryFunctionMap = new HashMap<>();

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

    private final NameResolver      nameResolver;
    private final RecordHandler     inputHandler;

    private ParametricBooleanExpressionEvaluator booleanExpressionEvaluator ;

    public ParametricExpressionEvaluator(List<MoonLightParseError> errors, NameResolver nameResolver, RecordHandler inputHandler) {
        this.errors = errors;
        this.nameResolver = nameResolver;
        this.inputHandler = inputHandler;
    }
    public ParametricExpressionEvaluator(NameResolver nameResolver, RecordHandler inputHandler) {
        this(new LinkedList<>(), nameResolver, inputHandler);
    }

    public ParametricExpressionEvaluator(NameResolver nameResolver) {
        this(nameResolver, new RecordHandler());
    }

    @Override
    protected Function<MoonLightRecord,Double> defaultResult() {
        return r -> Double.NaN;
    }

    @Override
    public Function<MoonLightRecord,Double> visitBinaryMathCallExpression(MoonLightScriptParser.BinaryMathCallExpressionContext ctx) {
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

    private Function<MoonLightRecord,Double> doApply(ParserRuleContext ctx, BiFunction<Double,Double,Double> fun, Function<MoonLightRecord,Double> arg1, Function<MoonLightRecord,Double> arg2) {
        return r -> fun.apply(arg1.apply(r),arg2.apply(r));
    }


    @Override
    public Function<MoonLightRecord,Double> visitBracketExpression(MoonLightScriptParser.BracketExpressionContext ctx) {
        return ctx.expression().accept(this);
    }

    @Override
    public Function<MoonLightRecord,Double> visitUnaryMathCallExpression(MoonLightScriptParser.UnaryMathCallExpressionContext ctx) {
        Function<Double,Double> fun = getUnaryFunction(ctx.fun);
        return doApply(ctx,fun,ctx.argument.accept(this));
    }

    private Function<MoonLightRecord,Double> doApply(ParserRuleContext ctx, Function<Double, Double> fun, Function<MoonLightRecord,Double> arg) {
        return r -> fun.apply(arg.apply(r));
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
    public Function<MoonLightRecord,Double> visitRealExpression(MoonLightScriptParser.RealExpressionContext ctx) {
        double v = Double.parseDouble(ctx.getText());
        return r  -> v;
    }

    @Override
    public Function<MoonLightRecord,Double> visitUnaryExpression(MoonLightScriptParser.UnaryExpressionContext ctx) {
        if (ctx.op.getText().equals("-")) {
            return doApply(ctx,x -> -x,ctx.arg.accept(this));
        } else {
            return ctx.arg.accept(this);
        }
    }

    @Override
    public Function<MoonLightRecord,Double> visitReferenceExpression(MoonLightScriptParser.ReferenceExpressionContext ctx) {
        String name = ctx.name.getText();
        double value = nameResolver.get(name);
        if (!Double.isNaN(value)) {
            return r -> value;
        }
        if (inputHandler.isAVariable(name)) {
            int idx = inputHandler.getVariableIndex(name);
            return r -> r.getDoubleOf(idx);
        }
        return r -> Double.NaN;
    }

    @Override
    public Function<MoonLightRecord,Double>  visitIntExpression(MoonLightScriptParser.IntExpressionContext ctx) {
        double value = Integer.parseInt(ctx.getText());
        return r -> value;
    }

    @Override
    public Function<MoonLightRecord,Double> visitIfThenElseExpression(MoonLightScriptParser.IfThenElseExpressionContext ctx) {
        ParametricBooleanExpressionEvaluator booleanEvaluator = getParametricBooleanExpressionEvaluator();
        if (booleanEvaluator.eval(ctx.guard)) {
            return ctx.thenExpression.accept(this);
        } else {
            return ctx.elseExpression.accept(this);
        }
    }

    private synchronized ParametricBooleanExpressionEvaluator getParametricBooleanExpressionEvaluator() {
        if (booleanExpressionEvaluator == null) {
            booleanExpressionEvaluator = new ParametricBooleanExpressionEvaluator(this);
        }
        return booleanExpressionEvaluator;
    }

    @Override
    public Function<MoonLightRecord,Double> visitMulDivExpression(MoonLightScriptParser.MulDivExpressionContext ctx) {
        BiFunction<Double,Double,Double> fun;
        if (ctx.op.getText().equals("*")) {
            fun = (x,y) -> x*y;
        } else {
            fun = (x,y) -> x/y;
        }
        return doApply(ctx,fun,ctx.left.accept(this),ctx.right.accept(this));
    }

    @Override
    public Function<MoonLightRecord,Double> visitSumDifExpression(MoonLightScriptParser.SumDifExpressionContext ctx) {
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
    public Function<MoonLightRecord,Double> visitInfinityExpression(MoonLightScriptParser.InfinityExpressionContext ctx) {
        return r -> Double.POSITIVE_INFINITY;
    }

    public Function<MoonLightRecord,Double> eval(MoonLightScriptParser.ExpressionContext value) {
        return value.accept(this);
    }
}
