package com.clt.speech.recognition;

import com.clt.speech.htk.MlfNode;

/**
 * @author Daniel Bobbert
 *
 */
public interface Utterance {

    /**
     * @return the number of words in this utterance
     */
    public int length();

    /**
     * @return the overall sentence confidence of this utterance
     */
    public float getConfidence();

    /**
     * @return the nth word in this utterance, starting at <code>0</code>
     */
    public Word getWord(int index);

    /**
     * Return the words of this utterance as a string
     */
    public String getWords();

    public MlfNode getTree();
}
