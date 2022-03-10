package eu.quanticol.moonlight.script;

import eu.quanticol.moonlight.core.base.MoonLightRecord;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ParametricBooleanExpressionEvaluator extends MoonLightScriptBaseVisitor<Function<MoonLightRecord,Boolean>> {
    private final ParametricExpressionEvaluator realExpressionEvaluator;
    private static final Map<String, BiFunction<Double,Double,Boolean>> relationsMap = new HashMap<>();

    static {
        relationsMap.put("<",(x,y) -> x<y);
        relationsMap.put("<=",(x,y) -> x<=y);
        relationsMap.put("=<",(x,y) -> x<=y);
        relationsMap.put("==", Double::equals);
        relationsMap.put(">",(x,y) -> x>y);
        relationsMap.put(">=",(x,y) -> x>=y);
        relationsMap.put("=>",(x,y) -> x>=y);
    }

    public ParametricBooleanExpressionEvaluator(ParametricExpressionEvaluator realExpressionEvaluator) {
        this.realExpressionEvaluator = realExpressionEvaluator;
    }

    public boolean eval(MoonLightScriptParser.ExpressionContext guard) {
        return false;
    }

    @Override
    public Function<MoonLightRecord,Boolean> visitImplyExpression(MoonLightScriptParser.ImplyExpressionContext ctx) {
        Function<MoonLightRecord,Boolean> left = ctx.left.accept(this);
        Function<MoonLightRecord,Boolean> right = ctx.right.accept(this);
        return r -> (!left.apply(r))||(right.apply(r));
    }

    @Override
    public Function<MoonLightRecord,Boolean> visitNotExpression(MoonLightScriptParser.NotExpressionContext ctx) {
        Function<MoonLightRecord,Boolean> arg = ctx.arg.accept(this);
        return r -> !arg.apply(r);
    }

    @Override
    public Function<MoonLightRecord,Boolean> visitFalseExpression(MoonLightScriptParser.FalseExpressionContext ctx) {
        return r -> false;
    }

    @Override
    public Function<MoonLightRecord,Boolean> visitBracketExpression(MoonLightScriptParser.BracketExpressionContext ctx) {
        return ctx.expression().accept(this);
    }

    @Override
    public Function<MoonLightRecord,Boolean> visitAndExpression(MoonLightScriptParser.AndExpressionContext ctx) {
        Function<MoonLightRecord,Boolean> left = ctx.left.accept(this);
        Function<MoonLightRecord,Boolean> right = ctx.right.accept(this);
        return r -> (!left.apply(r))||(right.apply(r));
    }

    @Override
    public Function<MoonLightRecord,Boolean> visitTrueExpression(MoonLightScriptParser.TrueExpressionContext ctx) {
        return r -> true;
    }

    @Override
    public Function<MoonLightRecord,Boolean> visitIfThenElseExpression(MoonLightScriptParser.IfThenElseExpressionContext ctx) {
        Function<MoonLightRecord,Boolean> guard = ctx.guard.accept(this);
        Function<MoonLightRecord,Boolean> thenBranch = ctx.guard.accept(this);
        Function<MoonLightRecord,Boolean> elseBranch = ctx.guard.accept(this);
        return r -> (guard.apply(r)?thenBranch.apply(r):elseBranch.apply(r));
    }

    @Override
    protected Function<MoonLightRecord,Boolean> defaultResult() {
        return r -> false;
    }

    @Override
    public Function<MoonLightRecord,Boolean> visitRelationExpression(MoonLightScriptParser.RelationExpressionContext ctx) {
        Function<MoonLightRecord,Double> left = ctx.left.accept(realExpressionEvaluator);
        Function<MoonLightRecord,Double> right = ctx.right.accept(realExpressionEvaluator);
        return r -> relationsMap.get(ctx.op.getText()).apply(left.apply(r),right.apply(r));
    }

    @Override
    public Function<MoonLightRecord,Boolean>  visitReferenceExpression(MoonLightScriptParser.ReferenceExpressionContext ctx) {
        Function<MoonLightRecord,Double> fun = ctx.accept(realExpressionEvaluator);
        return r -> fun.apply(r)>0;
    }
}
