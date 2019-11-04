package com.clt.srgf;

import com.clt.script.exp.Type;
import com.clt.script.exp.Value;
import com.clt.script.exp.values.IntValue;
import com.clt.script.exp.values.StringValue
import com.clt.script.parser.ParseException
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*


class GrammarTest {

    /*
    // this test fails. It's sad: Sphinx supports left-recursive rules, just the DialogOS parser fails here.
    
    // AK commented this out instead of using @Ignore, because @Ignore causes Netbeans to
    // fold together all tests of this class as "skipped". Commenting out has the same effect.
    @Test public void testLeftRecursion() throws ParseException {
        Grammar g = gr(LEFT_RECURSIVE);
        Value v = g.match("one minus two plus three", null);
        assertNotNull(v);
        assert v.getType().equals(Type.Int);
        assertEquals(2, ((IntValue) v).getInt());
    }
    /* */

    @Test
    public void testLeftRecursionFails() {
        Grammar g = gr(LEFT_RECURSIVE);
        Collection<Exception> issues = g.check(true);
        assert issues.size() == 1;
        assert issues.iterator().next().toString().equals("java.lang.Exception: input is left recursive");
    }

    @Test
    public void testRightRecursion() throws ParseException {
        Grammar g = gr(RIGHT_RECURSIVE);
        Value v = g.match("one minus two plus three", null);
        assertNotNull(v);
        assert v.getType().equals(Type.Int);
        assertEquals(2, ((IntValue) v).getInt());
    }

    // this test does not automatically check its output. You'll have to do that yourself.
    @Test
    public void testJSGFwithGarbage() throws ParseException {
        Grammar g = gr(RIGHT_RECURSIVE);
        //System.err.println(g.toString(Grammar.Format.JSGFwithGarbage));
    }
    
    @Test
    public void test131b() {
        Grammar g = gr(GRAMMAR_131_B);
        Value v = g.match("Wie viel Milch brauche ich", null)
        assertEquals "Wie viel Milch brauche ich", ((StringValue) v).getString()
    }
    
    @Test
    public void test131bFixed() {
        Grammar g = gr(GRAMMAR_131_B_FIXED);
        Value v = g.match("Wie viel Milch brauche ich", null)
        assertEquals "Milch", ((StringValue) v).getString()
    }
    
    @Test
    public void testDocsIncorrect() {
        Grammar g = gr(DOCS_INCORRECT);
        Value v = g.match("one", null)
        assertEquals "one", ((StringValue) v).getString()
    }
    
    @Test
    public void testDocsCorrect() {
        Grammar g = gr(DOCS_CORRECT);
        Value v = g.match("one", null)
        assertEquals "1", ((StringValue) v).getString()
    }
    

    private static final String DOCS_INCORRECT = '''
root $input;\n\
$input = $number;\n\
$number = one {$ = "1"};
''';
    
    private static final String DOCS_CORRECT = '''
root $input;\n\
$input = $number { $ = $number };\n\
$number = one {$ = "1"};
''';
    
    private static final String GRAMMAR_131_B = '''\n\
root $input;
$input = $zutaten | Hallo Welt ;
$zutaten = Wie viel $term_z brauche ich {$=$term_z} ;
$term_z = Milch | Zucker ;\n\
''';
    
    private static final String GRAMMAR_131_B_FIXED = '''\n\
root $input;
 $input = $zutaten {$ = $zutaten} | Hallo Welt ;
$zutaten = Wie viel $term_z brauche ich {$=$term_z} ;
$term_z = Milch | Zucker ;\n\
''';

    
    
    
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
// rule on subtraction, which reverses operation in following nodes (until another subtraction is found).
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
 
    private static final String ABNF_HEADER = '''    #ABNF 1.0;

language "English (US)";
tag-format <semantics/1.0>;
''';
    
    
    
    // returns a Grammar for the given string
    private static Grammar gr(String s) {
        return Grammar.create(new StringReader(s));
    }
    
    // Returns a Grammar for the given string, prefixed with
    // #ABNF 1.0 etc. This is for copy & pasting a grammar from
    // a DialogOS window, which would also get prefixed like this
    // automatically.
    private static Grammar gra(String s) {
        String completeGrammar = ABNF_HEADER + s;
        return gr(completeGrammar);
    }
    
    
    
}