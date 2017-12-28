package com.clt.speech.recognition.simpleresult;

import com.clt.speech.recognition.AbstractRecognitionResult;
import com.clt.speech.recognition.Utterance;

/**
 * A recognizer result which is based on a string. This is useful
 * if your speech recognizer either returns the whole string, without
 * confidence values, or if the string was read from the keyboard
 * ("dummy mode"). The given string is split into words at whitespace.
 * 
 * @author koller
 */
public class SimpleRecognizerResult extends AbstractRecognitionResult {
    private SimpleRecognizerUtterance utt = null;

    public SimpleRecognizerResult(String input) {
        String[] words = input.split("\\s+");
        utt = new SimpleRecognizerUtterance(words);
    }

    @Override
    public int numAlternatives() {
        return 1;
    }

    @Override
    public Utterance getAlternative(int index) {
        if (index == 0) {
            return utt;
        } else {
            return null;
        }
    }
}
