package com.clt.speech;

import java.util.Collection;
import java.util.Map;

/**
 * @author dabo
 *
 */
public interface G2P {

    /**
     * Return an array of supported languages
     */
    public Language[] getLanguages() throws SpeechException;

    /**
     * Transcribe a single word.
     */
    public String[] transcribe(String word, Language language) throws SpeechException;

    /**
     * Transcribe a collection of words. This may be more efficient as calling
     * {@link #transcribe(String, Language)} repeatedly.
     */
    public Map<String, String[]> transcribe(Collection<String> words, Language language) throws SpeechException;
}
