package eu.quanticol.moonlight.script;

import eu.quanticol.moonlight.TemporalMonitorProducer;
import eu.quanticol.moonlight.formula.Interval;
import eu.quanticol.moonlight.signal.MoonLightRecord;
import eu.quanticol.moonlight.signal.RecordHandler;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TemporalMonitoringGenerator extends MoonLightScriptBaseVisitor<TemporalMonitorProducer> {

    private final RecordHandler   inputHandler;
    private final RecordHandler   signalHandler;
    private final NameResolver    resolver;
    private final Map<String,TemporalMonitorProducer> producers;
    private final Map<String, RecordHandler> formulaParameters;


    public TemporalMonitoringGenerator(Map<String, TemporalMonitorProducer> producers, NameResolver resolver, Map<String, RecordHandler> formulaParameters, RecordHandler inputHandler, RecordHandler signalHandler) {
        this.inputHandler = inputHandler;
        this.signalHandler = signalHandler;
        this.resolver = resolver;
        this.producers = producers;
        this.formulaParameters = formulaParameters;
    }

    @Override
    public TemporalMonitorProducer visitImplyExpression(MoonLightScriptParser.ImplyExpressionContext ctx) {
        TemporalMonitorProducer left = ctx.left.accept(this);
        TemporalMonitorProducer right = ctx.right.accept(this);
        return TemporalMonitorProducer.produceImplication(left,right);
    }

    @Override
    public TemporalMonitorProducer visitNotExpression(MoonLightScriptParser.NotExpressionContext ctx) {
        TemporalMonitorProducer arg = ctx.arg.accept(this);
        return TemporalMonitorProducer.produceNegation(arg);
    }

    @Override
    public TemporalMonitorProducer visitFalseExpression(MoonLightScriptParser.FalseExpressionContext ctx) {
        return TemporalMonitorProducer.produceFalse();
    }

    @Override
    public TemporalMonitorProducer visitOnceExpression(MoonLightScriptParser.OnceExpressionContext ctx) {
        TemporalMonitorProducer arg = ctx.argument.accept(this);
        if (ctx.interval() == null) {
            return TemporalMonitorProducer.produceOnce(arg);
        } else {
            return TemporalMonitorProducer.produceOnce(arg,generateIntervalFunction(ctx.interval()));
        }
    }

    private Function<MoonLightRecord, Interval> generateIntervalFunction(MoonLightScriptParser.IntervalContext interval) {
        ParametricExpressionEvaluator evaluator = new ParametricExpressionEvaluator(resolver,inputHandler);
        Function<MoonLightRecord,Double> from = interval.from.accept(evaluator);
        Function<MoonLightRecord,Double> to = interval.to.accept(evaluator);
        return r -> new Interval(from.apply(r), to.apply(r));
    }

    @Override
    public TemporalMonitorProducer visitBracketExpression(MoonLightScriptParser.BracketExpressionContext ctx) {
        return ctx.expression().accept(this);
    }

    @Override
    public TemporalMonitorProducer visitUntilExpression(MoonLightScriptParser.UntilExpressionContext ctx) {
        TemporalMonitorProducer left = ctx.left.accept(this);
        TemporalMonitorProducer right = ctx.right.accept(this);
        if (ctx.interval() == null) {
            return TemporalMonitorProducer.produceUntil(left,right);
        } else {
            Function<MoonLightRecord,Interval> interval = generateIntervalFunction(ctx.interval());
            return TemporalMonitorProducer.produceUntil(left,interval,right);
        }
    }

    @Override
    public TemporalMonitorProducer visitSinceExpression(MoonLightScriptParser.SinceExpressionContext ctx) {
        TemporalMonitorProducer left = ctx.left.accept(this);
        TemporalMonitorProducer right = ctx.right.accept(this);
        if (ctx.interval() == null) {
            return TemporalMonitorProducer.produceSince(left,right);
        } else {
            Function<MoonLightRecord,Interval> interval = generateIntervalFunction(ctx.interval());
            return TemporalMonitorProducer.produceSince(left,interval,right);
        }
    }

    @Override
    public TemporalMonitorProducer visitAndExpression(MoonLightScriptParser.AndExpressionContext ctx) {
        TemporalMonitorProducer left = ctx.left.accept(this);
        TemporalMonitorProducer right = ctx.right.accept(this);
        return TemporalMonitorProducer.produceAnd(left,right);
    }

    @Override
    public TemporalMonitorProducer visitTrueExpression(MoonLightScriptParser.TrueExpressionContext ctx) {
        return TemporalMonitorProducer.produceTrue();
    }

    @Override
    public TemporalMonitorProducer visitReferenceExpression(MoonLightScriptParser.ReferenceExpressionContext ctx) {
        String name = ctx.name.getText();
        if (inputHandler.isAVariable(name)) {
            int idx = inputHandler.getVariableIndex(name);
            return TemporalMonitorProducer.produceAtomic((r,s) -> r.getDoubleOf(idx) );
        }
        if (signalHandler.isAVariable(name)) {
            int idx = signalHandler.getVariableIndex(name);
            return TemporalMonitorProducer.produceAtomic((r,s) -> s.getDoubleOf(idx) );
        }
        if (producers.containsKey(name)) {
            RecordHandler callee = formulaParameters.get(name);
            ParametricExpressionEvaluator evaluator = new ParametricExpressionEvaluator(resolver,inputHandler);
            List<Function<MoonLightRecord,Double>> args = ctx.args.stream().map(e -> e.accept(evaluator)).collect(Collectors.toList());
            return TemporalMonitorProducer.produceCall(producers.get(name),callee,args);
        }
        return defaultResult();
    }

    @Override
    protected TemporalMonitorProducer defaultResult() {
        return TemporalMonitorProducer.produceFalse();
    }

    @Override
    public TemporalMonitorProducer visitGloballyExpression(MoonLightScriptParser.GloballyExpressionContext ctx) {
        TemporalMonitorProducer arg = ctx.argument.accept(this);
        if (ctx.interval() == null) {
            return TemporalMonitorProducer.produceGlobally(arg);
        } else {
            Function<MoonLightRecord, Interval> interval = generateIntervalFunction(ctx.interval());
            return TemporalMonitorProducer.produceGlobally(arg,interval);
        }
    }

    @Override
    public TemporalMonitorProducer visitOrExpression(MoonLightScriptParser.OrExpressionContext ctx) {
        TemporalMonitorProducer left = ctx.left.accept(this);
        TemporalMonitorProducer right = ctx.right.accept(this);
        return TemporalMonitorProducer.produceOr(left,right);
    }

    @Override
    public TemporalMonitorProducer visitHistoricallyExpression(MoonLightScriptParser.HistoricallyExpressionContext ctx) {
        TemporalMonitorProducer arg = ctx.argument.accept(this);
        if (ctx.interval() == null) {
            return TemporalMonitorProducer.produceHistorically(arg);
        } else {
            Function<MoonLightRecord, Interval> interval = generateIntervalFunction(ctx.interval());
            return TemporalMonitorProducer.produceHistorically(arg,interval);
        }
    }

    @Override
    public TemporalMonitorProducer visitEventuallyExpression(MoonLightScriptParser.EventuallyExpressionContext ctx) {
        TemporalMonitorProducer arg = ctx.argument.accept(this);
        if (ctx.interval() == null) {
            return TemporalMonitorProducer.produceEventually(arg);
        } else {
            Function<MoonLightRecord, Interval> interval = generateIntervalFunction(ctx.interval());
            return TemporalMonitorProducer.produceEventually(arg,interval);
        }
    }

    @Override
    public TemporalMonitorProducer visitRelationExpression(MoonLightScriptParser.RelationExpressionContext ctx) {
        BiParametricExpressionEvaluator expressionEvaluator = new BiParametricExpressionEvaluator(resolver,inputHandler,signalHandler);
        BiFunction<MoonLightRecord,MoonLightRecord,Double>  left = expressionEvaluator.eval(ctx.left);
        BiFunction<MoonLightRecord,MoonLightRecord,Double>  right = expressionEvaluator.eval(ctx.right);
        return TemporalMonitorProducer.produceAtomic(left,ctx.op.getText(),right);
    }


    public TemporalMonitorProducer eval(MoonLightScriptParser.ExpressionContext formula) {
        return formula.accept(this);
    }
}
