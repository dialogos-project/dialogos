package com.clt.script.parser;

import com.clt.script.exp.Expression;
import com.clt.script.exp.values.StringValue;
import org.junit.Test;

import static org.junit.Assert.*;

public class ParserTest {

    @Test public void testStringParser() throws Exception {
        String testString = " s  e ";
        Expression expr = Parser.parseExpression("\"" + testString + "\"", null);
        assertTrue(expr.evaluate() instanceof StringValue);
        assertEquals(new StringValue(testString), expr.evaluate());
        assertEquals(testString, ((StringValue) expr.evaluate()).getString());
    }
}
