package com.clt.script.parser;

import com.clt.script.DefaultEnvironment;
import com.clt.script.Script;
import com.clt.script.debug.DefaultDebugger;
import com.clt.script.exp.Expression;
import com.clt.script.exp.Value;
import com.clt.script.exp.values.IntValue;
import com.clt.script.exp.values.ListValue;
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

    @Test public void testListAccessAndModification() throws Exception {
        // let's try to build a few expressions:
        Parser.parseExpression("[1, 2, 3]", null);
        Parser.parseExpression("l[0] = 4", null);
        Parser.parseExpression("l[0]", null);
        // list modification should yield the same as set()
        Script bracModif = Parser.parseScript("list l = [1, 2, 3]; l[0] = 4;", null);
//        Script funcModif = Parser.parseScript("list l = [1, 2, 3]; set(l, 0, 4);", null);
        Script bracAccess = Parser.parseScript("list l = [1, 2, 3]; int i; i = l[0];", null);
//        Script funcAccess = Parser.parseScript("list l = [1, 2, 3]; int i; i = get(l, 0);", null);
    }

    @Test public void testStructAccessAndModification() throws Exception {
        Parser.parseExpression("{a=1, b=2, c=3}", null);
        Parser.parseExpression("s.a = 4", null);
        Parser.parseExpression("s[\"d\"] = 5", null);
    }

    @Test(expected=ParseException.class) public void testStructNumericAccess()  throws Exception {
        Parser.parseExpression("s.1 = 4", null);
    }
}
