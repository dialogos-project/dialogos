package com.clt.speech.recognition.simpleresult;

import com.clt.speech.htk.MlfNode;
import com.clt.speech.recognition.Utterance;
import com.clt.speech.recognition.Word;

import java.util.ArrayList;
import java.util.List;

import com.clt.util.StringTools;

/**
 *
 * @author koller
 */
public class SimpleRecognizerUtterance implements Utterance {

    private List<SimpleRecognizerWord> words;
    private float logConfidence;

    public SimpleRecognizerUtterance(String[] rawWords) {
        words = new ArrayList<>();
        logConfidence = 0;

        for (int i = 0; i < rawWords.length; i++) {
            words.add(new SimpleRecognizerWord(rawWords[i]));
        }
    }

    @Override
    public int length() {
        return words.size();
    }

    @Override
    public float getConfidence() {
        return (float) Math.exp(logConfidence);
    }

    @Override
    public Word getWord(int index) {
        return words.get(index);
    }

    @Override
    public String getWords() {
        return StringTools.join(words, " ");
    }

    @Override
    public MlfNode getTree() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String toString() {
        return "DummyRecognizerUtterance{"
                + "words=" + words
                + ", logConfidence=" + logConfidence
                + ", confidence=" + getConfidence()
                + '}';
    }
}
