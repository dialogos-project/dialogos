package com.clt.speech.recognition;

import com.clt.speech.SpeechException;
import com.clt.srgf.Grammar;
import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.Context;
import edu.cmu.sphinx.jsgf.JSGFGrammar;
import edu.cmu.sphinx.jsgf.JSGFGrammarException;
import edu.cmu.sphinx.jsgf.JSGFGrammarParseException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static com.clt.srgf.Grammar.Format.JSGF;

/**
 * Created by timo on 13.10.17.
 */
public class SphinxContext extends RecognitionContext {
    public Configuration configuration;
    protected InputStream audioSource;
    SphinxLanguageSettings sls;
    Grammar grammar;
    ConfigurableSpeechRecognizer csr;
    ExtensibleDictionary dic;
    JSGFGrammar jsgfGrammar;

    /**
     * interpolate will interpolate the grammar with a background LM (at the cost of harder-to-identify)
     */
    public SphinxContext(String name, Grammar grammar, SphinxLanguageSettings sls) {
        super(name, null, null, grammar);
        assert sls != null;
        this.sls = sls;
        this.configuration = sls.getBaseConfiguration();
        this.grammar = grammar;
    }

    public void setGrammar(Grammar grammar) {
        this.grammar = grammar;
        this.configuration.setGrammarPath(encodeData(grammar.toString(JSGF)));
    }

    public void setAudioSource(InputStream audioSource) {
        this.audioSource = audioSource;
    }

    public ConfigurableSpeechRecognizer getRecognizer() throws SpeechException {
        try {
            if (csr == null) {
                Context context = new Context(Sphinx.class.getResource("sphinx/dos-sphinx.config.xml").toString(), configuration);
                dic = context.getInstance(ExtensibleDictionary.class);
                jsgfGrammar = context.getInstance(JSGFGrammar.class);
                csr = new ConfigurableSpeechRecognizer(context, audioSource);
            }
            dic.loadExceptions(sls.getG2PList());
            jsgfGrammar.setBaseURL(new URL(encodeData(grammar.toString(JSGF))));
            jsgfGrammar.loadJSGF("");
            return csr;
        } catch (IOException | JSGFGrammarException | JSGFGrammarParseException e) {
            e.printStackTrace();
            throw new SpeechException(e);
        }
    }

    private static String encodeData(String data) {
        return "data:" + data;
    }

    @Override
    public String toString() {
        return getName();
    }

}