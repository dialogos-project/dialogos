package edu.cmu.lti.dialogos.sphinx.client;

import com.clt.speech.Language;
import com.clt.speech.SpeechException;
import com.clt.speech.recognition.AbstractRecognizer;
import com.clt.speech.recognition.Domain;
import com.clt.speech.recognition.RecognitionContext;
import com.clt.speech.recognition.RecognizerException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by timo on 18.11.17.
 */
public abstract class SingleDomainRecognizer extends AbstractRecognizer {

    private Domain domain = new DefaultDomain();

    @Override public Domain[] getDomains() throws SpeechException {
        return new Domain[] { domain };
    }

    @Override public Domain createDomain(String name) throws SpeechException {
        return new DefaultDomain();
    }

    @Override public void setDomain(Domain domain) throws SpeechException {}

    @Override public Domain getDomain() throws SpeechException {
        return domain;
    }

    private static class DefaultDomain extends Domain {
        private List<RecognitionContext> contexts;

        public DefaultDomain() {
            super("default domain");
            contexts = new ArrayList<>();
        }

        @Override
        public RecognitionContext[] getContexts() throws RecognizerException {
            return contexts.toArray(new RecognitionContext[0]);
        }

        @Override
        public boolean addContext(RecognitionContext ctx)  {
            System.err.println("sphinx default domain: add context " + ctx.toString());
            contexts.add(ctx);
            return true;
        }

        @Override
        public void removeContext(RecognitionContext ctx) throws RecognizerException {
            contexts.remove(ctx);
        }

        @Override
        public Language[] getLanguages() throws RecognizerException {
            return null;
        }

    }
}
