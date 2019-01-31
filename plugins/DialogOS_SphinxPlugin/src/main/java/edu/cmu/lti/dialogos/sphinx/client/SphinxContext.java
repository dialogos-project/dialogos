package edu.cmu.lti.dialogos.sphinx.client;

import com.clt.speech.SpeechException;
import com.clt.speech.recognition.RecognitionContext;
import com.clt.srgf.Grammar;
import com.stanfy.enroscar.net.DataStreamHandler;
import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.Context;
import edu.cmu.sphinx.jsgf.JSGFGrammar;
import edu.cmu.sphinx.jsgf.JSGFGrammarException;
import edu.cmu.sphinx.jsgf.JSGFGrammarParseException;
import edu.cmu.sphinx.linguist.dflat.DynamicFlatLinguist;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static com.clt.srgf.Grammar.Format.JSGF;
import static com.clt.srgf.Grammar.Format.JSGFwithGarbage;
import static edu.cmu.lti.dialogos.sphinx.client.DataURLHelper.encodeData;

/**
 * Created by timo on 13.10.17.
 */
public class SphinxContext extends RecognitionContext {

    static {
        try {
            URL.setURLStreamHandlerFactory(protocol -> "data".equals(protocol) ? new DataStreamHandler() : null);
        } catch (Error e) {
            if (!"factory already defined".equals(e.getMessage())) {
                throw e;
            }
        }
    }


    Configuration configuration;
    protected InputStream audioSource;
    SphinxLanguageSettings sls;
    Grammar grammar;
    ConfigurableSpeechRecognizer csr;
    ExtensibleDictionary dic;
    JSGFGrammar jsgfGrammar;
    VADListener vadListener;
    DynamicFlatLinguist dflat;
    private Double threshold;

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

    @Override public Grammar getGrammar() {
        return this.grammar;
    }

    public void setAudioSource(InputStream audioSource) {
        this.audioSource = audioSource;
    }

    public ConfigurableSpeechRecognizer getRecognizer() throws SpeechException {
        try {
            if (csr == null) {
                String config = SphinxContext.class.getResource("dos-sphinx.config.xml").toString();
                Context context = new Context(config, configuration);
                dic = context.getInstance(ExtensibleDictionary.class);
                dic.loadExceptions(sls.getG2PList());
                jsgfGrammar = context.getInstance(JSGFGrammar.class);
                vadListener = context.getInstance(VADListener.class);
                dflat = context.getInstance(DynamicFlatLinguist.class);
                csr = new ConfigurableSpeechRecognizer(context, audioSource);
            }
            if (sls.revalidateG2P) {
                csr.resetRecognition();
                dic.deallocate();
                dic.loadExceptions(sls.getG2PList());
                sls.revalidateG2P = false;
            }
            dflat.setAddOutOfGrammarBranch(threshold != null);
            if (threshold != null)
                dflat.setOutOfGrammarProbability(threshold);
            String grammarString;
            if (grammar.requestsRobustness()) {
                grammarString = grammar.toString(JSGFwithGarbage);
                grammarString += sls.getGarbageRulesText();
            } else {
                grammarString = grammar.toString(JSGF);
            }
            jsgfGrammar.setBaseURL(new URL(encodeData(grammarString)));
            jsgfGrammar.loadJSGF("");
            return csr;
        } catch (IOException | JSGFGrammarException | JSGFGrammarParseException e) {
            e.printStackTrace();
            throw new SpeechException(e);
        }
    }

    @Override
    public String toString() {
        return getName();
    }

    public VADListener getVadListener() {
        return vadListener;
    }

    /**
     * set the threshold on a range 0 .. 100 %
     * @param threshold
     *  (0 map anything to grammar, 100 reject if it's not a perfect match)
     */
    public void setThreshold(Float threshold) {
        /*
        thresholds must be converted to be useful in Sphinx.
        TIMO tested this with a bunch of files containing digits
        except for one or two and tested a grammar that only allowed
        those two digits. The range of false positives varied within
        a range of 10e-25 and 10e-50 (anything accepted as one|two at 10e-50).

        thus 0 -> 10e-50
        and  1 -> 10e-25
        */
        if (threshold == null)
            this.threshold = null;
        else {
            assert 0 <= threshold && 1 >= threshold : "out of bounds, I only like >= 0 & <= 1";
            this.threshold = Math.pow(10., -50+(25*threshold));
        }

    }
}