package edu.cmu.lti.dialogos.sphinx.client;

import com.clt.properties.Property;
import com.clt.speech.Language;
import com.clt.speech.SpeechException;
import com.clt.speech.recognition.AbstractRecognizer;
import com.clt.speech.recognition.Domain;
import com.clt.speech.recognition.RecognitionContext;
import com.clt.speech.recognition.RecognizerException;

import javax.sound.sampled.AudioFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by timo on 18.11.17.
 */
public abstract class SingleDomainRecognizer extends AbstractRecognizer {

    private Domain domain = new DefaultDomain();

    @Override public Domain[] getDomains() {
        return new Domain[] { domain };
    }

    @Override public Domain createDomain(String name) {
        return new DefaultDomain();
    }

    @Override public void setDomain(Domain domain) {}

    @Override public Domain getDomain() {
        return domain;
    }

    private class DefaultDomain extends Domain {
        private List<RecognitionContext> contexts;

        public DefaultDomain() {
            super("default domain");
            contexts = new ArrayList<>();
        }

        @Override
        public RecognitionContext[] getContexts() {
            return contexts.toArray(new RecognitionContext[0]);
        }

        @Override
        public boolean addContext(RecognitionContext ctx)  {
            contexts.add(ctx);
            return true;
        }

        @Override
        public void removeContext(RecognitionContext ctx) {
            contexts.remove(ctx);
        }

        @Override
        public Language[] getLanguages() throws RecognizerException {
            try {
                return SingleDomainRecognizer.this.getLanguages();
            } catch (SpeechException se) {
                throw new RecognizerException("" + se);
            }
        }

    }
}
