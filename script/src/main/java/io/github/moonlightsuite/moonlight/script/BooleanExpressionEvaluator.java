package io.github.moonlightsuite.moonlight.script;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class BooleanExpressionEvaluator extends MoonLightScriptBaseVisitor<Boolean> {
    private final ExpressionEvaluator expressionEvaluator;
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

    public BooleanExpressionEvaluator(ExpressionEvaluator expressionEvaluator) {
        this.expressionEvaluator = expressionEvaluator;
    }

    public boolean eval(MoonLightScriptParser.ExpressionContext expressionContext) {
        return expressionContext.accept(this);
    }

    @Override
    public Boolean visitImplyExpression(MoonLightScriptParser.ImplyExpressionContext ctx) {
        return (!ctx.left.accept(this))||(ctx.right.accept(this));
    }

    @Override
    public Boolean visitNotExpression(MoonLightScriptParser.NotExpressionContext ctx) {
        return !ctx.arg.accept(this);
    }

    @Override
    public Boolean visitFalseExpression(MoonLightScriptParser.FalseExpressionContext ctx) {
        return false;
    }

    @Override
    public Boolean visitBracketExpression(MoonLightScriptParser.BracketExpressionContext ctx) {
        return ctx.expression().accept(this);
    }

    @Override
    public Boolean visitAndExpression(MoonLightScriptParser.AndExpressionContext ctx) {
        return ctx.left.accept(this)&&ctx.right.accept(this);
    }

    @Override
    public Boolean visitOrExpression(MoonLightScriptParser.OrExpressionContext ctx) {
        return ctx.left.accept(this)||ctx.right.accept(this);
    }

    @Override
    public Boolean visitTrueExpression(MoonLightScriptParser.TrueExpressionContext ctx) {
        return true;
    }

    @Override
    public Boolean visitIfThenElseExpression(MoonLightScriptParser.IfThenElseExpressionContext ctx) {
        if (ctx.guard.accept(this)) {
            return ctx.thenExpression.accept(this);
        } else {
            return ctx.elseExpression.accept(this);
        }
    }


    @Override
    protected Boolean defaultResult() {
        return false;
    }

    @Override
    public Boolean visitRelationExpression(MoonLightScriptParser.RelationExpressionContext ctx) {
        return relationsMap.get(ctx.op.getText()).apply(
                ctx.left.accept(expressionEvaluator),
                ctx.right.accept(expressionEvaluator)
        );
    }

    @Override
    public Boolean visitReferenceExpression(MoonLightScriptParser.ReferenceExpressionContext ctx) {
        return ctx.accept(expressionEvaluator)>0;
    }
}
