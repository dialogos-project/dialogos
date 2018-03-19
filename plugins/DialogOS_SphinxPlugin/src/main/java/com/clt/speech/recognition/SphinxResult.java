package com.clt.speech.recognition;

import edu.cmu.sphinx.api.SpeechResult;

public class SphinxResult extends AbstractRecognitionResult {
    private SphinxUtterance utt = null;

    public SphinxResult(SpeechResult sr) {
        utt = new SphinxUtterance(sr.getWords());
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
