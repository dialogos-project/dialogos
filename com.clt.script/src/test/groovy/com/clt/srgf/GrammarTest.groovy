package com.clt.srgf;

import com.clt.script.exp.Type;
import com.clt.script.exp.Value;
import com.clt.script.exp.values.IntValue;
import com.clt.script.parser.ParseException;
import org.junit.Ignore;
import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


class GrammarTest {
    @Ignore // this test fails. It's sad: Sphinx supports left-recursive rules, just the DialogOS parser fails here.
    @Test public void testLeftRecursion() throws ParseException {
        InputStream stream = is(LEFT_RECURSIVE);
        Grammar g = Grammar.create(new InputStreamReader(stream));
        Value v = g.match("one minus two plus three", null);
        assertNotNull(v);
        assert v.getType().equals(Type.Int);
        assertEquals(2, ((IntValue) v).getInt());
    }

    @Test public void testRightRecursion() throws ParseException {
        InputStream stream = is(RIGHT_RECURSIVE);
        Grammar g = Grammar.create(new InputStreamReader(stream));
        Value v = g.match("one minus two plus three", null);
        assertNotNull(v);
        assert v.getType().equals(Type.Int);
        assertEquals(2, ((IntValue) v).getInt());
    }

    @Ignore // this test does not automatically check its output. You'll have to do that yourself.
    @Test public void testJSGFwithGarbage() throws ParseException {
        InputStream stream = is(RIGHT_RECURSIVE);
        Grammar g = Grammar.create(new InputStreamReader(stream));
        System.err.println(g.toString(Grammar.Format.JSGFwithGarbage));
    }
    
    
    
    
    // returns an InputStream for the given string
    private static InputStream is(String s) {
        return new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
    }
    
    
    private static final String LEFT_RECURSIVE = '''
    #ABNF 1.0;

language "English (US)";
tag-format <semantics/1.0>;
root $input;

$input =
   $zahl { $zahl }
 | $input plus  $zahl { $input + $zahl }
 | $input minus $zahl { $input - $zahl }
;

$zahl =
   zero { 0 }
 | one { 1 }
 | two { 2 }
 | three { 3 }
 | four { 4 }
 | five { 5 }
 | six { 6 }
 | seven { 7 }
 | eight { 8 }
 | nine { 9 }
;

''';
    
    private static final String RIGHT_RECURSIVE = '''\n\
#ABNF 1.0;

language "English (US)";
tag-format <semantics/1.0>;
root $input;

// right recursion for chain calculations is annoying because semantic evaluation will be right-to-left
// and as a result subtraction will be wrong if implemented naively. The solution is to change to a separate
// rule on subtraction, which reverses operation in following nodes (until another subtraction is found.
$input =
   $zahl { $zahl }
 | $zahl plus  $input    { $zahl + $input    }
 | $zahl minus $inputNeg { $zahl - $inputNeg }
;
$inputNeg =
   $zahl { $zahl }
 | $zahl plus  $inputNeg { $zahl - $inputNeg }
 | $zahl minus $input    { $zahl + $input    }
;

$zahl =
   zero { 0 }
 | one { 1 }
 | two { 2 }
 | three { 3 }
 | four { 4 }
 | five { 5 }
 | six { 6 }
 | seven { 7 }
 | eight { 8 }
 | nine { 9 }
;
    ''';
    
}