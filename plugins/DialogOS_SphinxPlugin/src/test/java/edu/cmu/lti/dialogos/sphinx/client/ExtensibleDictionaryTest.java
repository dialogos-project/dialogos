package edu.cmu.lti.dialogos.sphinx.client;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.Context;
import edu.cmu.sphinx.frontend.util.StreamDataSource;
import edu.cmu.sphinx.jsgf.JSGFGrammar;
import edu.cmu.sphinx.jsgf.JSGFGrammarException;
import edu.cmu.sphinx.jsgf.JSGFGrammarParseException;
import edu.cmu.sphinx.linguist.dictionary.TextDictionary;
import edu.cmu.sphinx.recognizer.StateListener;
import edu.cmu.sphinx.util.props.PropertyException;
import edu.cmu.sphinx.util.props.PropertySheet;
import org.junit.Test;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ExtensibleDictionaryTest {

    /** check out that extensible dictionary works at all (not yet worrying about how to feed it via DialogOS) */
    @Test(timeout = 10000) public void basicTest() throws IOException, JSGFGrammarParseException, JSGFGrammarException, UnsupportedAudioFileException {
        Configuration configuration = new Configuration();
        configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
        configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/en-us/noisedict"); // instead of reading the real dictionary, let's read the noise dictionary twice :-)
        configuration.setUseGrammar(true);
        configuration.setGrammarPath("");
        configuration.setGrammarName("");

        Context context = new Context(Sphinx.class.getResource("dos-sphinx.config.xml").toString(), configuration);
        AudioInputStream ais = AudioSystem.getAudioInputStream(SphinxTest.class.getResource("one.wav"));
        context.getInstance(StreamDataSource.class).setInputStream(ais);
        ExtensibleDictionary dic = context.getInstance(ExtensibleDictionary.class);

        List<G2PEntry> g2pList  = new ArrayList<>();
        g2pList.add(new G2PEntry("one", "W AH N"));
        g2pList.add(new G2PEntry("two", "T UW"));
        dic.loadExceptions(g2pList);

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


    @Test
    public void updateG2PTest() throws IOException, JSGFGrammarParseException, JSGFGrammarException, UnsupportedAudioFileException {
        Configuration configuration = new Configuration();
        configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
        configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/en-us/noisedict"); // instead of reading the real dictionary, let's read the noise dictionary twice :-)
        configuration.setUseGrammar(true);
        configuration.setGrammarPath("");
        configuration.setGrammarName("");

        Context context = new Context(Sphinx.class.getResource("dos-sphinx.config.xml").toString(), configuration);
        AudioInputStream ais = AudioSystem.getAudioInputStream(SphinxTest.class.getResource("one.wav"));
        context.getInstance(StreamDataSource.class).setInputStream(ais);
        ExtensibleDictionary dic = context.getInstance(ExtensibleDictionary.class);

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
        List<G2PEntry> g2pList  = new ArrayList<>();
        g2pList.add(new G2PEntry("one", "W AH N"));
        g2pList.add(new G2PEntry("two", "T UW"));
        dic.loadExceptions(g2pList);
        recognizer.deallocate();
        recognizer.allocate();

        assertEquals(recognizer.recognize().toString(), "<sil> one <sil>");
    }

}
