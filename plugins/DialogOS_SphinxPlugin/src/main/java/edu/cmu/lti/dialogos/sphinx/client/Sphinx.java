package edu.cmu.lti.dialogos.sphinx.client;

import com.clt.properties.Property;
import com.clt.speech.Language;
import com.clt.speech.SpeechException;
import com.clt.speech.recognition.Domain;
import com.clt.speech.recognition.RecognitionContext;
import com.clt.speech.recognition.RecognitionResult;
import com.clt.speech.recognition.RecognizerEvent;
import com.clt.srgf.Grammar;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.stanfy.enroscar.net.DataStreamHandler;
import edu.cmu.sphinx.api.*;

/**
 * @author koller, timo
 *
 * List of TODOs:
 * handle multiple languages (configurable for more than DE/EN?), exhibit a "default" language through Plugin
 *
 */
public class Sphinx extends SingleDomainRecognizer {
    static {
        try {
            URL.setURLStreamHandlerFactory(protocol -> "data".equals(protocol) ? new DataStreamHandler() : null);
        } catch (Error e) {
            if (!"factory already defined".equals(e.getMessage())) {
                throw e;
            }
        }
    }

    public static final Language US_ENGLISH = new Language(new Locale("en", "US"), "US English");
    public static final Language GERMAN = new Language(new Locale("de", "DE"), "Deutsch");
    private static final Language[] STANDARD_LANGUAGES = { US_ENGLISH, GERMAN };

    private Map<Language, SphinxLanguageSettings> languageSettings;
    private SphinxContext context;

    ConfigurableSpeechRecognizer csr;

    public Sphinx() {
        languageSettings = SphinxLanguageSettings.createDefault();
    }

    public SphinxLanguageSettings getLanguageSettings(Language l) {
        return languageSettings.get(l);
    }

    @Override protected RecognitionResult startImpl() throws SpeechException {
        assert context != null : "cannot start recognition without a context";
        // TODO2: integration of VAD (will require custom-made default.config.xml or other means of setting the frontend anyway)
        // TODO3: wire VAD information and readyness to GUI
        csr = context.getRecognizer();
        csr.startRecognition();
        fireRecognizerEvent(new RecognizerEvent(this, RecognizerEvent.RECOGNIZER_READY));
        System.err.println("***ready***");
        SpeechResult speechResult = csr.getResult();
        if (speechResult != null) {
            System.err.println("**** result: " + speechResult.getHypothesis());
            csr.stopRecognition();
            return new SphinxResult(speechResult);
        }
        return null;
        // FIXME: out-of-grammar input leads to "<unk>" triggering errors further down.
    }

    @Override protected void stopImpl() throws SpeechException {
        if (csr != null)
            csr.stopRecognition();
    }

    /** Return an array of supported languages */
    @Override public Language[] getLanguages() throws SpeechException {
        Collection<Language> langs = languageSettings.keySet();
        return langs.toArray(new Language[langs.size()]);
    }

    @Override public void setContext(RecognitionContext context) throws SpeechException {
        assert context instanceof SphinxContext : "you're feeding a context that I do not understand";
        this.context = (SphinxContext) context;
    }

    @Override public RecognitionContext getContext() throws SpeechException {
        return this.context;
    }

    @Override public SphinxContext createTemporaryContext(Grammar g, Domain domain) throws SpeechException {
        //TODO: ponder name, ponder timestamp
        return createContext("temp", g, domain, System.currentTimeMillis());
    }

    Map<Language, SphinxContext> contextCache = new HashMap<>();

    @Override protected SphinxContext createContext(String name, Grammar g, Domain domain, long timestamp) throws SpeechException {
        //TODO: figure out what to do if the grammar does not have a language
        assert g.getLanguage() != null;
        Language l = new Language(Language.findLocale(g.getLanguage()));
        assert l != null;
        if (!contextCache.containsKey(l)) {
            contextCache.put(l, new SphinxContext(name, g, this.languageSettings.get(l)));
        } else {
        }
        SphinxContext sc = contextCache.get(l);
        sc.setGrammar(g);
        return sc;
    }

    /** called during startup, possibly used to configure things via the GUI */
    @Override public Property<?>[] getProperties() {
        return null;
    }

    /** only ever called from TranscriptionWindow (and nobody seems to use that */
    @Override public String[] transcribe(String word, Language language) throws SpeechException {
        return null;
    }

}
