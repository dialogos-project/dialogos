package de.saar.coli.dialogos.pocketsphinx;

import com.clt.speech.Language;
import com.clt.speech.recognition.Domain;
import com.clt.speech.recognition.RecognitionContext;
import com.clt.speech.recognition.RecognizerException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PocketSphinxDefaultDomain extends Domain {
    public static final Language US_ENGLISH = new Language(new Locale("en", "US"), "English (US)");
    private List<RecognitionContext> contexts;

    public PocketSphinxDefaultDomain() {
        super("Sphinx default domain");
        contexts = new ArrayList<>();
    }

    @Override
    public RecognitionContext[] getContexts() throws RecognizerException {
        return contexts.toArray(new RecognitionContext[0]);
    }

    @Override
    public boolean addContext(RecognitionContext ctx)  {
        contexts.add(ctx);
        return true;
    }

    @Override
    public void removeContext(RecognitionContext ctx) throws RecognizerException {
        // TODO implement this
    }

    @Override
    public Language[] getLanguages() throws RecognizerException {
        return new Language[] {US_ENGLISH };
    }
}
