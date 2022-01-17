package eu.quanticol.moonlight.script;

import eu.quanticol.moonlight.SpatialTemporalMonitorProducer;
import eu.quanticol.moonlight.domain.DistanceDomain;
import eu.quanticol.moonlight.domain.DoubleDistance;
import eu.quanticol.moonlight.domain.Interval;
import eu.quanticol.moonlight.space.DistanceStructure;
import eu.quanticol.moonlight.io.MoonLightRecord;
import eu.quanticol.moonlight.signal.RecordHandler;
import eu.quanticol.moonlight.space.SpatialModel;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SpatialTemporalMonitoringGenerator extends MoonLightScriptBaseVisitor<SpatialTemporalMonitorProducer> {

    private final RecordHandler   inputHandler;
    private final RecordHandler   signalHandler;
    private final RecordHandler   edgeHandler;
    private final NameResolver    resolver;
    private final Map<String,SpatialTemporalMonitorProducer> producers;
    private final Map<String, RecordHandler> formulaParameters;


    public SpatialTemporalMonitoringGenerator(Map<String,SpatialTemporalMonitorProducer> producers, NameResolver resolver, Map<String, RecordHandler> formulaParameters, RecordHandler inputHandler, RecordHandler signalHandler, RecordHandler edgeHandler) {
        this.inputHandler = inputHandler;
        this.signalHandler = signalHandler;
        this.resolver = resolver;
        this.producers = producers;
        this.edgeHandler = edgeHandler;
        this.formulaParameters = formulaParameters;
    }

    @Override
    public SpatialTemporalMonitorProducer visitImplyExpression(MoonLightScriptParser.ImplyExpressionContext ctx) {
        SpatialTemporalMonitorProducer left = ctx.left.accept(this);
        SpatialTemporalMonitorProducer right = ctx.right.accept(this);
        return SpatialTemporalMonitorProducer.produceImplication(left,right);
    }

    @Override
    public SpatialTemporalMonitorProducer visitNotExpression(MoonLightScriptParser.NotExpressionContext ctx) {
        SpatialTemporalMonitorProducer arg = ctx.arg.accept(this);
        return SpatialTemporalMonitorProducer.produceNegation(arg);
    }

    @Override
    public SpatialTemporalMonitorProducer visitFalseExpression(MoonLightScriptParser.FalseExpressionContext ctx) {
        return SpatialTemporalMonitorProducer.produceFalse();
    }

    @Override
    public SpatialTemporalMonitorProducer visitOnceExpression(MoonLightScriptParser.OnceExpressionContext ctx) {
        SpatialTemporalMonitorProducer arg = ctx.argument.accept(this);
        if (ctx.interval() == null) {
            return SpatialTemporalMonitorProducer.produceOnce(arg);
        } else {
            return SpatialTemporalMonitorProducer.produceOnce(arg,generateIntervalFunction(ctx.interval()));
        }
    }

    private Function<MoonLightRecord, Interval> generateIntervalFunction(MoonLightScriptParser.IntervalContext interval) {
        ParametricExpressionEvaluator evaluator = new ParametricExpressionEvaluator(resolver,inputHandler);
        Function<MoonLightRecord,Double> from = interval.from.accept(evaluator);
        Function<MoonLightRecord,Double> to = interval.to.accept(evaluator);
        return r -> new Interval(from.apply(r), to.apply(r));
    }

    @Override
    public SpatialTemporalMonitorProducer visitBracketExpression(MoonLightScriptParser.BracketExpressionContext ctx) {
        return ctx.expression().accept(this);
    }

    @Override
    public SpatialTemporalMonitorProducer visitUntilExpression(MoonLightScriptParser.UntilExpressionContext ctx) {
        SpatialTemporalMonitorProducer left = ctx.left.accept(this);
        SpatialTemporalMonitorProducer right = ctx.right.accept(this);
        if (ctx.interval() == null) {
            return SpatialTemporalMonitorProducer.produceUntil(left,right);
        } else {
            Function<MoonLightRecord,Interval> interval = generateIntervalFunction(ctx.interval());
            return SpatialTemporalMonitorProducer.produceUntil(left,interval,right);
        }
    }

    @Override
    public SpatialTemporalMonitorProducer visitSinceExpression(MoonLightScriptParser.SinceExpressionContext ctx) {
        SpatialTemporalMonitorProducer left = ctx.left.accept(this);
        SpatialTemporalMonitorProducer right = ctx.right.accept(this);
        if (ctx.interval() == null) {
            return SpatialTemporalMonitorProducer.produceSince(left,right);
        } else {
            Function<MoonLightRecord,Interval> interval = generateIntervalFunction(ctx.interval());
            return SpatialTemporalMonitorProducer.produceSince(left,interval,right);
        }
    }

    @Override
    public SpatialTemporalMonitorProducer visitAndExpression(MoonLightScriptParser.AndExpressionContext ctx) {
        SpatialTemporalMonitorProducer left = ctx.left.accept(this);
        SpatialTemporalMonitorProducer right = ctx.right.accept(this);
        return SpatialTemporalMonitorProducer.produceAnd(left,right);
    }

    @Override
    public SpatialTemporalMonitorProducer visitTrueExpression(MoonLightScriptParser.TrueExpressionContext ctx) {
        return SpatialTemporalMonitorProducer.produceTrue();
    }

    @Override
    public SpatialTemporalMonitorProducer visitReferenceExpression(MoonLightScriptParser.ReferenceExpressionContext ctx) {
        String name = ctx.name.getText();
        if (inputHandler.isAVariable(name)) {
            int idx = inputHandler.getVariableIndex(name);
            return SpatialTemporalMonitorProducer.produceAtomic((r,s) -> r.getDoubleOf(idx) );
        }
        if (signalHandler.isAVariable(name)) {
            int idx = signalHandler.getVariableIndex(name);
            return SpatialTemporalMonitorProducer.produceAtomic((r,s) -> s.getDoubleOf(idx) );
        }
        if (producers.containsKey(name)) {
            RecordHandler callee = formulaParameters.get(name);
            ParametricExpressionEvaluator evaluator = new ParametricExpressionEvaluator(resolver,inputHandler);
            List<Function<MoonLightRecord,Double>> args = ctx.args.stream().map(e -> e.accept(evaluator)).collect(Collectors.toList());
            return SpatialTemporalMonitorProducer.produceCall(producers.get(name),callee,args);
        }

        return defaultResult();
    }

    @Override
    protected SpatialTemporalMonitorProducer defaultResult() {
        return SpatialTemporalMonitorProducer.produceFalse();
    }

    @Override
    public SpatialTemporalMonitorProducer visitGloballyExpression(MoonLightScriptParser.GloballyExpressionContext ctx) {
        SpatialTemporalMonitorProducer arg = ctx.argument.accept(this);
        if (ctx.interval() == null) {
            return SpatialTemporalMonitorProducer.produceGlobally(arg);
        } else {
            Function<MoonLightRecord, Interval> interval = generateIntervalFunction(ctx.interval());
            return SpatialTemporalMonitorProducer.produceGlobally(arg,interval);
        }
    }

    @Override
    public SpatialTemporalMonitorProducer visitOrExpression(MoonLightScriptParser.OrExpressionContext ctx) {
        SpatialTemporalMonitorProducer left = ctx.left.accept(this);
        SpatialTemporalMonitorProducer right = ctx.right.accept(this);
        return SpatialTemporalMonitorProducer.produceOr(left,right);
    }

    @Override
    public SpatialTemporalMonitorProducer visitHistoricallyExpression(MoonLightScriptParser.HistoricallyExpressionContext ctx) {
        SpatialTemporalMonitorProducer arg = ctx.argument.accept(this);
        if (ctx.interval() == null) {
            return SpatialTemporalMonitorProducer.produceHistorically(arg);
        } else {
            Function<MoonLightRecord, Interval> interval = generateIntervalFunction(ctx.interval());
            return SpatialTemporalMonitorProducer.produceHistorically(arg,interval);
        }
    }

    @Override
    public SpatialTemporalMonitorProducer visitEventuallyExpression(MoonLightScriptParser.EventuallyExpressionContext ctx) {
        SpatialTemporalMonitorProducer arg = ctx.argument.accept(this);
        if (ctx.interval() == null) {
            return SpatialTemporalMonitorProducer.produceEventually(arg);
        } else {
            Function<MoonLightRecord, Interval> interval = generateIntervalFunction(ctx.interval());
            return SpatialTemporalMonitorProducer.produceEventually(arg,interval);
        }
    }

    @Override
    public SpatialTemporalMonitorProducer visitRelationExpression(MoonLightScriptParser.RelationExpressionContext ctx) {
        BiParametricExpressionEvaluator expressionEvaluator = new BiParametricExpressionEvaluator(resolver,inputHandler,signalHandler);
        BiFunction<MoonLightRecord,MoonLightRecord,Double>  left = expressionEvaluator.eval(ctx.left);
        BiFunction<MoonLightRecord,MoonLightRecord,Double>  right = expressionEvaluator.eval(ctx.right);
        return SpatialTemporalMonitorProducer.produceAtomic(left,ctx.op.getText(),right);
    }

    @Override
    public SpatialTemporalMonitorProducer visitEverywhereExpression(MoonLightScriptParser.EverywhereExpressionContext ctx) {
        SpatialTemporalMonitorProducer arg = ctx.argument.accept(this);
        Function<MoonLightRecord,Function<SpatialModel<MoonLightRecord>, DistanceStructure<MoonLightRecord,?>>> distance = getDistance(ctx.distanceExpression,ctx.interval());
        return SpatialTemporalMonitorProducer.produceEverywhere(arg,distance);
    }

    private Function<MoonLightRecord,Function<SpatialModel<MoonLightRecord>, DistanceStructure<MoonLightRecord,?>>> getDistance(MoonLightScriptParser.ExpressionContext distanceExpression, MoonLightScriptParser.IntervalContext interval) {
        DistanceDomain<Double> distance = new DoubleDistance();
        BiFunction<MoonLightRecord,MoonLightRecord,Double> distanceFunction = getDistanceFunction(distanceExpression);
        ParametricExpressionEvaluator evaluator = new ParametricExpressionEvaluator(resolver,inputHandler);
        Function<MoonLightRecord,Double> from = interval.from.accept(evaluator);
        Function<MoonLightRecord,Double> to = interval.to.accept(evaluator);
        return r -> (m -> DistanceStructure.buildDistanceStructure(m,e -> distanceFunction.apply(r,e),from.apply(r),to.apply(r)));
    }

    private BiFunction<MoonLightRecord, MoonLightRecord, Double> getDistanceFunction(MoonLightScriptParser.ExpressionContext distanceExpression) {
        if (distanceExpression == null) {
            return (r,e) -> 1.0;
        }
        BiParametricExpressionEvaluator evaluator = new BiParametricExpressionEvaluator(resolver,inputHandler,edgeHandler);
        return distanceExpression.accept(evaluator);
    }

    @Override
    public SpatialTemporalMonitorProducer visitReachExpression(MoonLightScriptParser.ReachExpressionContext ctx) {
        SpatialTemporalMonitorProducer left = ctx.left.accept(this);
        SpatialTemporalMonitorProducer right = ctx.right.accept(this);
        Function<MoonLightRecord,Function<SpatialModel<MoonLightRecord>, DistanceStructure<MoonLightRecord,?>>> distance = getDistance(ctx.distanceExpression,ctx.interval());
        return SpatialTemporalMonitorProducer.produceReach(left,distance,right);
    }

    @Override
    public SpatialTemporalMonitorProducer visitEscapeExpression(MoonLightScriptParser.EscapeExpressionContext ctx) {
        SpatialTemporalMonitorProducer arg = ctx.argument.accept(this);
        Function<MoonLightRecord,Function<SpatialModel<MoonLightRecord>, DistanceStructure<MoonLightRecord,?>>> distance = getDistance(ctx.distanceExpression,ctx.interval());
        return SpatialTemporalMonitorProducer.produceEscape(arg,distance);
    }

    @Override
    public SpatialTemporalMonitorProducer visitSomewhereExpression(MoonLightScriptParser.SomewhereExpressionContext ctx) {
        SpatialTemporalMonitorProducer arg = ctx.argument.accept(this);
        Function<MoonLightRecord,Function<SpatialModel<MoonLightRecord>, DistanceStructure<MoonLightRecord,?>>> distance = getDistance(ctx.distanceExpression,ctx.interval());
        return SpatialTemporalMonitorProducer.produceSomewhere(arg,distance);
    }

    public SpatialTemporalMonitorProducer eval(MoonLightScriptParser.ExpressionContext formula) {
        return formula.accept(this);
    }
}
