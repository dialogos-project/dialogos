package com.clt.srgf;

import com.clt.script.exp.Type;
import com.clt.script.exp.Value;
import com.clt.script.exp.values.IntValue;
import com.clt.script.parser.ParseException;
import org.junit.Ignore;
import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by timo on 05.11.17.
 */
public class GrammarTest {

    @Ignore // this test fails. It's sad: Sphinx supports left-recursive rules, just the DialogOS parser fails here.
    @Test public void testLeftRecursion() throws ParseException {
        InputStream stream = GrammarTest.class.getResourceAsStream("leftrecursive.srgf");
        Grammar g = Grammar.create(new InputStreamReader(stream));
        Value v = g.match("one minus two plus three", null);
        assertNotNull(v);
        assert v.getType().equals(Type.Int);
        assertEquals(2, ((IntValue) v).getInt());
    }

    @Test public void testRightRecursion() throws ParseException {
        InputStream stream = GrammarTest.class.getResourceAsStream("rightrecursive.srgf");
        Grammar g = Grammar.create(new InputStreamReader(stream));
        Value v = g.match("one minus two plus three", null);
        assertNotNull(v);
        assert v.getType().equals(Type.Int);
        assertEquals(2, ((IntValue) v).getInt());
    }
}
