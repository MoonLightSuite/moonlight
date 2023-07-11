package io.github.moonlightsuite.moonlight.script;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TestExpressionEvaluation {



    private double evalDoubleExpression(Map<String,Double> context, String code) {
        ExpressionEvaluator evaluator = new ExpressionEvaluator(new DefaultNameResolver(new MoonLightEnumerationRepository(),context));
        return evaluator.eval(getExpression(code));
    }

    private boolean evalBooleanExpression(Map<String,Double> context, String code) {
        ExpressionEvaluator evaluator = new ExpressionEvaluator(new DefaultNameResolver(new MoonLightEnumerationRepository(),context));
        return evaluator.getBooleanExpressionEvaluator().eval(getExpression(code));
    }

    private MoonLightScriptParser.ExpressionContext getExpression(String code) {
        MoonLightScriptLexer lexer = new MoonLightScriptLexer(CharStreams.fromString(code));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MoonLightScriptParser parser = new MoonLightScriptParser(tokens);
        return parser.expression();
    }

    @Test
    public void shouldCorrectlyEvalInfinity() {
        assertEquals(Double.POSITIVE_INFINITY, evalDoubleExpression(new HashMap<>(),"inf"));
        assertEquals(Double.NEGATIVE_INFINITY, evalDoubleExpression(new HashMap<>(),"-inf"));
    }

    @Test
    public void shouldCorrectlyEvalConstant() {
        Map<String, Double> map = new HashMap<>();
        map.put("x", 10.0);
        assertEquals(10.0, evalDoubleExpression(map,"x"));
    }


    @Test
    public void shouldCorrectlyEvalConstants() {
        assertEquals(10.0, evalDoubleExpression(new HashMap<>(),"10.0"));
        assertEquals(10.0, evalDoubleExpression(new HashMap<>(),"10"));
    }

    @Test
    public void shouldCorrectlyEvalBracketExpressions() {
        assertEquals(10.0, evalDoubleExpression(new HashMap<>(),"( 10.0 )"));
    }

    @Test
    public void shouldCorrectlyEvalConditionalExpressionTrue() {
        assertEquals(10.0, evalDoubleExpression(new HashMap<>(),"( 2<3?10.0:20.0 )"));
    }

    @Test
    public void shouldCorrectlyEvalConditionalExpressionFalse() {
        assertEquals(20.0, evalDoubleExpression(new HashMap<>(),"( 2>3?10.0:20.0 )"));
    }

    @Test
    public void shouldCorrectlyEvalImplicationTrue() {
        assertTrue(evalBooleanExpression(new HashMap<>(),"(2<3) -> (4<5)"));
    }

    @Test
    public void shouldCorrectlyEvalImplicationTrueWithFalsePremises() {
        assertTrue(evalBooleanExpression(new HashMap<>(),"(3<2) -> (5<4)"));
    }

    @Test
    public void shouldCorrectlyEvalImplicationFalse() {
        assertFalse(evalBooleanExpression(new HashMap<>(),"(2<3) -> (5<4)"));
    }

    @Test
    public void shouldCorrectlyEvalOr() {
        assertTrue(evalBooleanExpression(new HashMap<>(),"(2<3) | (5<4)"));
        assertTrue(evalBooleanExpression(new HashMap<>(),"(2<3) | (3<4)"));
        assertTrue(evalBooleanExpression(new HashMap<>(),"(5<3) | (3<4)"));
        assertFalse(evalBooleanExpression(new HashMap<>(),"(5<3) | (7<4)"));
    }

    @Test
    public void shouldCorrectlyEvalAnd() {
        assertFalse(evalBooleanExpression(new HashMap<>(),"(2<3) & (5<4)"));
        assertTrue(evalBooleanExpression(new HashMap<>(),"(2<3) & (3<4)"));
        assertFalse(evalBooleanExpression(new HashMap<>(),"(5<3) & (3<4)"));
        assertFalse(evalBooleanExpression(new HashMap<>(),"(5<3) & (7<4)"));
    }

    @Test
    public void shouldCorrectlyEvalRelation() {
        assertTrue(evalBooleanExpression(new HashMap<>(),"(2<3)"));
        assertFalse(evalBooleanExpression(new HashMap<>(),"(3<2)"));
        assertFalse(evalBooleanExpression(new HashMap<>(),"(3<3)"));
        assertTrue(evalBooleanExpression(new HashMap<>(),"(2<=3)"));
        assertFalse(evalBooleanExpression(new HashMap<>(),"(3<=2)"));
        assertTrue(evalBooleanExpression(new HashMap<>(),"(2<=3)"));
        assertTrue(evalBooleanExpression(new HashMap<>(),"(3<=3)"));
        assertFalse(evalBooleanExpression(new HashMap<>(),"(3==2)"));
        assertFalse(evalBooleanExpression(new HashMap<>(),"(2==3)"));
        assertTrue(evalBooleanExpression(new HashMap<>(),"(3==3)"));
        assertTrue(evalBooleanExpression(new HashMap<>(),"(3>2)"));
        assertFalse(evalBooleanExpression(new HashMap<>(),"(2>3)"));
        assertFalse(evalBooleanExpression(new HashMap<>(),"(3>3)"));
        assertTrue(evalBooleanExpression(new HashMap<>(),"(3=>2)"));
        assertFalse(evalBooleanExpression(new HashMap<>(),"(2>=3)"));
        assertTrue(evalBooleanExpression(new HashMap<>(),"(3>=3)"));
    }

    @Test
    public void shouldCorrectlyEvalSum() {
        assertEquals(3,evalDoubleExpression(new HashMap<>(),"2+1"));
        assertEquals(3,evalDoubleExpression(new HashMap<>(),"1+2"));
    }

    @Test
    public void shouldCorrectlyEvalDiff() {
        assertEquals(1,evalDoubleExpression(new HashMap<>(),"2-1"));
        assertEquals(-1,evalDoubleExpression(new HashMap<>(),"1-2"));
    }

    @Test
    public void shouldCorrectlyEvalModulo() {
        assertEquals(5%2, evalDoubleExpression(new HashMap<>(),"5 % 2"));
        assertEquals(2%5, evalDoubleExpression(new HashMap<>(),"2 % 5"));
    }

    @Test
    public void shouldCorrectlyEvalDiv() {
        assertEquals(5.0/2.0,evalDoubleExpression(new HashMap<>(),"5 / 2"));
        assertEquals(2.0/5.0,evalDoubleExpression(new HashMap<>(),"2 / 5"));
    }

    @Test
    public void shouldCorrectlyEvalMul() {
        assertEquals(5.0 * 2.0,evalDoubleExpression(new HashMap<>(),"5 * 2"));
        assertEquals(2.0 * 5.0,evalDoubleExpression(new HashMap<>(),"2 * 5"));
    }

    @Test
    public void shouldCorrectlyEvalUnary() {
        assertEquals(5.0,evalDoubleExpression(new HashMap<>(),"--5.0"));
        assertEquals(-5.0,evalDoubleExpression(new HashMap<>(),"-5.0"));
    }

    @Test
    public void shouldCorrectlyEvalBinaryMath() {
        assertEquals(Math.atan2(5,2),evalDoubleExpression(new HashMap<>(),"atan2(5,2)"));
        assertEquals(Math.hypot(5,2),evalDoubleExpression(new HashMap<>(),"hypot(5,2)"));
        assertEquals(Math.max(5,2),evalDoubleExpression(new HashMap<>(),"max(5,2)"));
        assertEquals(Math.min(5,2),evalDoubleExpression(new HashMap<>(),"min(5,2)"));
        assertEquals(Math.pow(5,2),evalDoubleExpression(new HashMap<>(),"pow(5,2)"));
    }

    @Test
    public void shouldCorrectlyEvalUnaryMath() {
        assertEquals(Math.acos(5),evalDoubleExpression(new HashMap<>(),"acos(5)"));
        assertEquals(Math.asin(5),evalDoubleExpression(new HashMap<>(),"asin(5)"));
        assertEquals(Math.atan(5),evalDoubleExpression(new HashMap<>(),"atan(5)"));
        assertEquals(Math.cbrt(5),evalDoubleExpression(new HashMap<>(),"cbrt(5)"));
        assertEquals(Math.ceil(5),evalDoubleExpression(new HashMap<>(),"ceil(5)"));
        assertEquals(Math.cos(5),evalDoubleExpression(new HashMap<>(),"cos(5)"));
        assertEquals(Math.exp(5),evalDoubleExpression(new HashMap<>(),"exp(5)"));
        assertEquals(Math.expm1(5),evalDoubleExpression(new HashMap<>(),"expm1(5)"));
        assertEquals(Math.floor(5),evalDoubleExpression(new HashMap<>(),"floor(5)"));
        assertEquals(Math.log(5),evalDoubleExpression(new HashMap<>(),"log(5)"));
        assertEquals(Math.log10(5),evalDoubleExpression(new HashMap<>(),"log10(5)"));
        assertEquals(Math.log1p(5),evalDoubleExpression(new HashMap<>(),"log1p(5)"));
        assertEquals(Math.signum(5),evalDoubleExpression(new HashMap<>(),"signum(5)"));
        assertEquals(Math.sin(5),evalDoubleExpression(new HashMap<>(),"sin(5)"));
        assertEquals(Math.sinh(5),evalDoubleExpression(new HashMap<>(),"sinh(5)"));
        assertEquals(Math.sqrt(5),evalDoubleExpression(new HashMap<>(),"sqrt(5)"));
        assertEquals(Math.tan(5),evalDoubleExpression(new HashMap<>(),"tan(5)"));
    }


}
