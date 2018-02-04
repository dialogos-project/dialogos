package com.clt.speech.recognition.simpleresult;

import com.clt.speech.recognition.Word;

public class SimpleRecognizerWord extends Word {

    private String word;

    public SimpleRecognizerWord(String word) {
        this.word = word;
    }

    @Override
    public long getStart() {
        return 0;
    }

    @Override
    public long getEnd() {
        return 0;
    }

    @Override
    public String getWord() {
        return word;
    }

    @Override
    public float getConfidence() {
        return 1;
    }

    @Override
    public String toString() {
        return getWord();
    }
}
