package eu.quanticol.moonlight.script;

import eu.quanticol.moonlight.io.MoonLightRecord;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class BiParametricBooleanExpressionEvaluator extends MoonLightScriptBaseVisitor<BiFunction<MoonLightRecord,MoonLightRecord,Boolean>> {

    private final BiParametricExpressionEvaluator biParametricExpressionEvaluator;

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

    public BiParametricBooleanExpressionEvaluator(BiParametricExpressionEvaluator biParametricExpressionEvaluator) {
        this.biParametricExpressionEvaluator = biParametricExpressionEvaluator;
    }

    public boolean eval(MoonLightScriptParser.ExpressionContext guard) {
        return false;
    }

    @Override
    public BiFunction<MoonLightRecord,MoonLightRecord,Boolean>visitImplyExpression(MoonLightScriptParser.ImplyExpressionContext ctx) {
        BiFunction<MoonLightRecord,MoonLightRecord,Boolean> left = ctx.left.accept(this);
        BiFunction<MoonLightRecord,MoonLightRecord,Boolean> right = ctx.right.accept(this);
        return (r,s) -> !left.apply(r,s)||right.apply(r,s);
    }

    @Override
    public BiFunction<MoonLightRecord,MoonLightRecord,Boolean> visitNotExpression(MoonLightScriptParser.NotExpressionContext ctx) {
        BiFunction<MoonLightRecord,MoonLightRecord,Boolean> arg = ctx.arg.accept(this);
        return (r,s) -> !arg.apply(r,s);
    }

    @Override
    public BiFunction<MoonLightRecord,MoonLightRecord,Boolean> visitFalseExpression(MoonLightScriptParser.FalseExpressionContext ctx) {
        return (r,s) -> false;
    }

    @Override
    public BiFunction<MoonLightRecord,MoonLightRecord,Boolean> visitBracketExpression(MoonLightScriptParser.BracketExpressionContext ctx) {
        return ctx.expression().accept(this);
    }

    @Override
    public BiFunction<MoonLightRecord,MoonLightRecord,Boolean> visitAndExpression(MoonLightScriptParser.AndExpressionContext ctx) {
        BiFunction<MoonLightRecord,MoonLightRecord,Boolean> left = ctx.left.accept(this);
        BiFunction<MoonLightRecord,MoonLightRecord,Boolean> right = ctx.right.accept(this);
        return (r,s) -> left.apply(r,s)&&right.apply(r,s);
    }

    @Override
    public BiFunction<MoonLightRecord, MoonLightRecord, Boolean> visitOrExpression(MoonLightScriptParser.OrExpressionContext ctx) {
        BiFunction<MoonLightRecord,MoonLightRecord,Boolean> left = ctx.left.accept(this);
        BiFunction<MoonLightRecord,MoonLightRecord,Boolean> right = ctx.right.accept(this);
        return (r,s) -> left.apply(r,s)||right.apply(r,s);
    }

    @Override
    public BiFunction<MoonLightRecord,MoonLightRecord,Boolean> visitTrueExpression(MoonLightScriptParser.TrueExpressionContext ctx) {
        return (r,s) -> true;
    }

    @Override
    public BiFunction<MoonLightRecord,MoonLightRecord,Boolean> visitIfThenElseExpression(MoonLightScriptParser.IfThenElseExpressionContext ctx) {
        BiFunction<MoonLightRecord,MoonLightRecord,Boolean> guard = ctx.guard.accept(this);
        BiFunction<MoonLightRecord,MoonLightRecord,Boolean> thenBranch = ctx.thenExpression.accept(this);
        BiFunction<MoonLightRecord,MoonLightRecord,Boolean> elseBranch = ctx.elseExpression.accept(this);
        return (r,s) -> (guard.apply(r,s)?thenBranch.apply(r,s):elseBranch.apply(r,s));
    }

    @Override
    protected BiFunction<MoonLightRecord,MoonLightRecord,Boolean> defaultResult() {
        return (r,s) -> false;
    }

    @Override
    public BiFunction<MoonLightRecord,MoonLightRecord,Boolean> visitRelationExpression(MoonLightScriptParser.RelationExpressionContext ctx) {
        BiFunction<MoonLightRecord,MoonLightRecord,Double> left = ctx.left.accept(biParametricExpressionEvaluator);
        BiFunction<MoonLightRecord,MoonLightRecord,Double> right = ctx.right.accept(biParametricExpressionEvaluator);
        BiFunction<Double,Double,Boolean> op = relationsMap.get(ctx.op.getText());
        return (r,s) -> op.apply(left.apply(r,s), right.apply(r,s));
    }


    @Override
    public BiFunction<MoonLightRecord, MoonLightRecord, Boolean> visitReferenceExpression(MoonLightScriptParser.ReferenceExpressionContext ctx) {
        BiFunction<MoonLightRecord,MoonLightRecord,Double> arg = ctx.accept(biParametricExpressionEvaluator);
        return (r,s) -> arg.apply(r,s)>0;
    }
}
