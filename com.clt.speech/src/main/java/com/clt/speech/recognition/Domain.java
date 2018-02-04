package com.clt.speech.recognition;

import com.clt.speech.Language;
import com.clt.speech.SpeechException;

public abstract class Domain {

    private String name;

    public Domain(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public void dispose() throws RecognizerException {
        RecognitionContext[] contexts = this.getContexts();
        for (int i = 0; i < contexts.length; i++) {
            contexts[i].dispose();
            this.removeContext(contexts[i]);
        }
    }

    public Language findLanguage(String name) throws SpeechException {
        Language languages[] = this.getLanguages();
        for (int i = 0; i < languages.length; i++) {
            if (languages[i].getName().equals(name)) {
                return languages[i];
            }
        }

        return null;
    }

    public RecognitionContext findContext(String name) throws RecognizerException {
        RecognitionContext contexts[] = this.getContexts();
        for (int i = 0; i < contexts.length; i++) {
            if (contexts[i].getName().equals(name)) {
                return contexts[i];
            }
        }

        return null;
    }

    public abstract RecognitionContext[] getContexts() throws RecognizerException;

    public abstract boolean addContext(RecognitionContext ctx) throws RecognizerException;

    public abstract void removeContext(RecognitionContext ctx) throws RecognizerException;

    public abstract Language[] getLanguages() throws RecognizerException;
}
