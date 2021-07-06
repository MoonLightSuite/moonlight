package eu.quanticol.moonlight.script;

import org.antlr.v4.runtime.ParserRuleContext;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ExpressionEvaluator extends MoonLightScriptBaseVisitor<Double> {

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

    private final NameResolver      nameResolver;

    private BooleanExpressionEvaluator booleanExpressionEvaluator ;

    public ExpressionEvaluator(List<MoonLightParseError> errors, NameResolver nameResolver) {
        this.errors = errors;
        this.nameResolver = nameResolver;
    }

    public ExpressionEvaluator(NameResolver nameResolver) {
        this(new LinkedList<>(), nameResolver);
    }

    @Override
    protected Double defaultResult() {
        return Double.NaN;
    }

    @Override
    public Double visitBinaryMathCallExpression(MoonLightScriptParser.BinaryMathCallExpressionContext ctx) {
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

    private Double doApply(ParserRuleContext ctx, BiFunction<Double,Double,Double> fun, Double arg1, Double arg2) {
        if (Double.isNaN(arg1)||Double.isNaN(arg2)) {
            return Double.NaN;
        }
        Double result = fun.apply(arg1,arg2);
        if (Double.isNaN(result)) {
            errors.add(MoonLightParseError.realExpressionEvaluationError(ctx));
        }
        return result;
    }


    @Override
    public Double visitBracketExpression(MoonLightScriptParser.BracketExpressionContext ctx) {
        return ctx.expression().accept(this);
    }

    @Override
    public Double visitUnaryMathCallExpression(MoonLightScriptParser.UnaryMathCallExpressionContext ctx) {
        Function<Double,Double> fun = getUnaryFunction(ctx.fun);
        return doApply(ctx,fun,ctx.argument.accept(this));
    }

    private Double doApply(ParserRuleContext ctx, Function<Double, Double> fun, Double arg) {
        if (Double.isNaN(arg)) {
            return arg;
        }
        Double result = fun.apply(arg);
        if (Double.isNaN(result)) {
            errors.add(MoonLightParseError.realExpressionEvaluationError(ctx));
        }
        return result;
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
    public Double visitRealExpression(MoonLightScriptParser.RealExpressionContext ctx) {
        return Double.parseDouble(ctx.getText());
    }

    @Override
    public Double visitUnaryExpression(MoonLightScriptParser.UnaryExpressionContext ctx) {
        Function<Double,Double> fun;
        if (ctx.op.getText().equals("-")) {
            fun = x -> -x;
            return doApply(ctx,fun,ctx.arg.accept(this));
        } else {
            return ctx.arg.accept(this);
        }
    }

    @Override
    public Double visitReferenceExpression(MoonLightScriptParser.ReferenceExpressionContext ctx) {
        String name = ctx.name.getText();
        double value = nameResolver.get(name);
        if (!Double.isNaN(value)) {
            return value;
        }
        errors.add(MoonLightParseError.useOfAnUnknownName(name,ctx));
        return Double.NaN;
    }

    @Override
    public Double visitIntExpression(MoonLightScriptParser.IntExpressionContext ctx) {
        return (double) Integer.parseInt(ctx.getText());
    }

    @Override
    public Double visitIfThenElseExpression(MoonLightScriptParser.IfThenElseExpressionContext ctx) {
        BooleanExpressionEvaluator booleanEvaluator = getBooleanExpressionEvaluator();
        if (booleanEvaluator.eval(ctx.guard)) {
            return ctx.thenExpression.accept(this);
        } else {
            return ctx.elseExpression.accept(this);
        }
    }

    public synchronized BooleanExpressionEvaluator getBooleanExpressionEvaluator() {
        if (booleanExpressionEvaluator == null) {
            booleanExpressionEvaluator = new BooleanExpressionEvaluator(this);
        }
        return booleanExpressionEvaluator;
    }

    @Override
    public Double visitMulDivExpression(MoonLightScriptParser.MulDivExpressionContext ctx) {
        BiFunction<Double,Double,Double> fun;
        if (ctx.op.getText().equals("*")) {
            fun = (x,y) -> x*y;
        } else {
            fun = (x,y) -> x/y;
        }
        return doApply(ctx,fun,ctx.left.accept(this),ctx.right.accept(this));
    }

    @Override
    public Double visitSumDifExpression(MoonLightScriptParser.SumDifExpressionContext ctx) {
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
    public Double visitInfinityExpression(MoonLightScriptParser.InfinityExpressionContext ctx) {
        return Double.POSITIVE_INFINITY;
    }

    public double eval(MoonLightScriptParser.ExpressionContext value) {
        return value.accept(this);
    }
}
