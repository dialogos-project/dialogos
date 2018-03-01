package com.clt.speech.recognition;

import com.clt.speech.SpeechException;
import com.clt.srgf.Grammar;
import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.Context;
import edu.cmu.sphinx.linguist.dictionary.TextDictionary;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import static com.clt.srgf.Grammar.Format.JSGF;

/**
 * Created by timo on 13.10.17.
 */
public class SphinxContext extends RecognitionContext {
    public Configuration configuration;
    protected InputStream audioSource;
    SphinxLanguageSettings sls;

    /**
     * interpolate will interpolate the grammar with a background LM (at the cost of harder-to-identify)
     */
    public SphinxContext(String name, Grammar grammar, SphinxLanguageSettings sls) {
        super(name, null, null, grammar);
        assert sls != null;
        this.sls = sls;

        this.configuration = sls.getBaseConfiguration();
        if (grammar != null)
            this.configuration.setGrammarPath(encodeData(grammar.toString(JSGF)));
    }

    public void setAudioSource(InputStream audioSource) {
        this.audioSource = audioSource;
    }

    public ConfigurableSpeechRecognizer getRecognizer() throws SpeechException {
        ConfigurableSpeechRecognizer csr;
        try {
            Context context = new Context(Sphinx.class.getResource("sphinx/dos-sphinx.config.xml").toString(), configuration);
            ExtensibleDictionary dic = context.getInstance(ExtensibleDictionary.class);
            dic.loadExceptions(sls.getG2PList());
            csr = new ConfigurableSpeechRecognizer(context, audioSource);
        } catch (IOException e) {
            e.printStackTrace();
            throw new SpeechException(e);
        }
        return csr;
    }

    private static String encodeData(String data) {
        return "data:" + data;
    }

    @Override
    public String toString() {
        return getName();
    }

}