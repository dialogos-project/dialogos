package edu.cmu.lti.dialogos.sphinx.client;

import com.clt.script.parser.ParseException;
import com.clt.speech.SpeechException;
import com.clt.speech.recognition.RecognitionResult;
import com.clt.srgf.Grammar;
import com.stanfy.enroscar.net.DataStreamHandler;
import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.Context;
import edu.cmu.sphinx.frontend.util.StreamDataSource;
import edu.cmu.sphinx.jsgf.*;
import edu.cmu.sphinx.jsgf.parser.JSGFParser;
import edu.cmu.sphinx.linguist.dictionary.*;
import edu.cmu.sphinx.recognizer.*;
import edu.cmu.sphinx.util.props.PropertyException;
import edu.cmu.sphinx.util.props.PropertySheet;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by timo on 30.10.17.
 */
public class SphinxTest {

    /** startImpl should warn if Sphinx is not properly set up */
    @Test(expected = NullPointerException.class, timeout=10000)
    public void invalidRecognitionSetupTest() throws SpeechException {
        Sphinx sphinx = new Sphinx();
        try {
            sphinx.startImpl();
        } catch (AssertionError | NullPointerException e) {
            throw new NullPointerException();
        }
    }

    static {
        try {
            URL.setURLStreamHandlerFactory(protocol -> "data".equals(protocol) ? new DataStreamHandler() : null);
        } catch (Error e) {
            System.err.println("SphinxTest");
            if (!"factory already defined".equals(e.getMessage())) {
                throw e;
            }
        }
    }

    /** test our data URL type */
    @Test(timeout = 10000) public void testDataURL() throws IOException {
        final String urlContent = "someText";
        String urlText = "data:text/plain;base64," + DatatypeConverter.printBase64Binary(urlContent.getBytes(StandardCharsets.UTF_8));
        URL url = new URL(null, urlText, new DataStreamHandler());
        String urlResult = IOUtils.toString(url, StandardCharsets.UTF_8);
        assertEquals(urlContent, urlResult);
        // test that a trailing "/.gram" does not change the outcome (this is special behaviour needed to workaround Sphinx
        urlText = "data:text/plain;base64," + DatatypeConverter.printBase64Binary(urlContent.getBytes(StandardCharsets.UTF_8)) + "/.gram";
        url = new URL(null, urlText, new DataStreamHandler());
        String urlGramResult = IOUtils.toString(url, StandardCharsets.UTF_8);
        assertEquals(urlContent, urlGramResult);
        // test that no-encoding does not change the outcome
        urlText = "data:" + urlContent;
        url = new URL(null, urlText, new DataStreamHandler());
        String plainUrlGramResult = IOUtils.toString(url, StandardCharsets.UTF_8);
        assertEquals(urlContent, plainUrlGramResult);
    }

    /** test the JSGF parsing in Sphinx via normal and via data-URL-encoding */
    @Test(timeout = 10000) public void testJSGFParser() throws IOException, JSGFGrammarParseException {
        // load a minimal jsgf grammar from file
        String fromFileURL = JSGFParser.newGrammarFromJSGF(SphinxTest.class.getResource("minimaljsgf.gram"), new JSGFRuleGrammarFactory(new JSGFRuleGrammarManager())).toString();
        // check that we actually produce a JSGF grammar
        assertTrue(fromFileURL.startsWith("#JSGF"));
        // construct a grammar based in a string-URL and check that it is the same:
        String grammarString = "#JSGF V1.0 UTF-8 en;\ngrammar null;\npublic <rule> = a;\n";
        InputStream stream = new ByteArrayInputStream(grammarString.getBytes(StandardCharsets.UTF_8));
        new JSGFParser(stream);
        stream = new ByteArrayInputStream(grammarString.getBytes(StandardCharsets.UTF_8));
        String fromString = JSGFParser.newGrammarFromJSGF(stream, new JSGFRuleGrammarFactory(new JSGFRuleGrammarManager())).toString();
        assertEquals(fromString, fromFileURL);
        // construct a grammar from a data-URL and check that it's the same
        String urlText = "data:text/plain;base64," + DatatypeConverter.printBase64Binary(grammarString.getBytes(StandardCharsets.UTF_8));
        URL url = new URL(null, urlText, new DataStreamHandler());
        String fromDataURL = JSGFParser.newGrammarFromJSGF(url, new JSGFRuleGrammarFactory(new JSGFRuleGrammarManager())).toString();
        assertEquals(fromString, fromDataURL);
        // construct one moregrammar from a data-URL that is not base64-encoded and check that it's the same
        urlText = "data:" + grammarString;
        url = new URL(null, urlText, new DataStreamHandler());
        String fromPlainDataURL = JSGFParser.newGrammarFromJSGF(url, new JSGFRuleGrammarFactory(new JSGFRuleGrammarManager())).toString();
        assertEquals(fromString, fromPlainDataURL);
    }

    @Test(timeout = 10000) public void testJSGFGrammar() throws JSGFGrammarParseException, JSGFGrammarException, IOException, ClassNotFoundException {
        JSGFGrammar jsgfg = new JSGFGrammar("", "", false, false, false, false,
                new Dictionary(){
                    @Override public void newProperties(PropertySheet ps) throws PropertyException {}
                    @Override public edu.cmu.sphinx.linguist.dictionary.Word getWord(String text) { return null; }
                    @Override public edu.cmu.sphinx.linguist.dictionary.Word getSentenceStartWord() { return null; }
                    @Override public edu.cmu.sphinx.linguist.dictionary.Word getSentenceEndWord() { return null; }
                    @Override public edu.cmu.sphinx.linguist.dictionary.Word getSilenceWord() { return null; }
                    @Override public edu.cmu.sphinx.linguist.dictionary.Word[] getFillerWords() { return new edu.cmu.sphinx.linguist.dictionary.Word[0]; }
                    @Override public void allocate() throws IOException { }
                    @Override public void deallocate() { }
                }
        );
        System.err.println(SphinxTest.class.getResource("minimaljsgf.gram"));
        URL baseURL = new URL("file:" + SphinxTest.class.getResource("minimaljsgf.gram").getPath().replaceFirst("minimaljsgf.gram", ""));
        System.err.println(baseURL);
        jsgfg.setBaseURL(baseURL);
        jsgfg.loadJSGF("minimaljsgf");

        String grammarString = "#JSGF V1.0 UTF-8 en;\ngrammar null;\npublic <rule> = a;\n";
        String urlText = "data:" + grammarString;
        baseURL = new URL(null, urlText, new DataStreamHandler());
        jsgfg.setBaseURL(baseURL);
        jsgfg.loadJSGF("");
    }

    /** check out that Sphinx ASR works at all */
    @Test(timeout = 10000) public void testSpeechRecognitionFunctionality() throws IOException, JSGFGrammarParseException, JSGFGrammarException, UnsupportedAudioFileException {
        Configuration configuration = new Configuration();
        configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
        configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
        configuration.setUseGrammar(true);
        configuration.setGrammarPath("");
        configuration.setGrammarName("");

        Context context = new Context(Sphinx.class.getResource("dos-sphinx.config.xml").toString(), configuration);
        AudioInputStream ais = AudioSystem.getAudioInputStream(SphinxTest.class.getResource("one.wav"));
        context.getInstance(StreamDataSource.class).setInputStream(ais);

        edu.cmu.sphinx.recognizer.Recognizer recognizer = context.getInstance(edu.cmu.sphinx.recognizer.Recognizer.class);
        recognizer.addStateListener(new StateListener() {
            @Override public void statusChanged(edu.cmu.sphinx.recognizer.Recognizer.State status) {
                System.err.println(status);
            }
            @Override public void newProperties(PropertySheet ps) throws PropertyException { }
        });
        ((TextDictionary) context.getInstance(TextDictionary.class)).allocate();
        JSGFGrammar jsgfGrammar = context.getInstance(JSGFGrammar.class);
        URL baseURL = new URL("file:" + SphinxTest.class.getResource("onetwo.gram").getPath().replaceFirst("onetwo.gram", ""));
        jsgfGrammar.setBaseURL(baseURL);
        jsgfGrammar.loadJSGF("onetwo");

        recognizer.allocate();
        assertEquals(recognizer.recognize().toString(), "<sil> one <sil>");
    }

    @Test(timeout = 10000) // 10 seconds should be enough to fail
    public void grammarRecognitionTest() throws ParseException, IOException, SpeechException {
        Sphinx sphinx = new Sphinx();
        Grammar grammar = Grammar.create(new InputStreamReader(SphinxTest.class.getResourceAsStream("onetwo.gram")));
        grammar.setRoot("main"); // -> it would be nice if Grammar.create would itself keep the public marker for the root rule
        grammar.setLanguage("english");
        SphinxContext context = sphinx.createTemporaryContext(grammar, null);
        context.setAudioSource(SphinxTest.class.getResourceAsStream("one.wav"));
        sphinx.setContext(context);
        RecognitionResult rr = sphinx.startImpl();
        "one".equals(rr.getAlternative(0).getWords());
    }


/*    @Test(timeout = 10000) // 10 seconds should be enough to fail
    public void robustRecognitionTest() throws SpeechException {
        Sphinx sphinx = new Sphinx();
        SphinxContext context = sphinx.createTemporaryContext(,null);

    }
*/
}
