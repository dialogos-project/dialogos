package com.clt.speech.recognition;

/**
 * Speech recognizers must return recognition results that conform to this
 * interface.
 *
 * @author Daniel Bobbert
 * @version 1.0
 */
public interface RecognitionResult extends Iterable<Utterance> {

    /**
     * Return the number of recognition alternatives
     */
    public int numAlternatives();

    /**
     * Return the nth alternative
     */
    public Utterance getAlternative(int index);

    /**
     * Return a textual representation of the recognition result
     */
    public String toString();
}
