package edu.cmu.lti.dialogos.sphinx.client;

import com.clt.speech.recognition.Word;
import edu.cmu.sphinx.result.WordResult;

public class SphinxWord extends Word {
    private WordResult sphinxword;

    public SphinxWord(WordResult sphinxword) {
        this.sphinxword = sphinxword;
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
        return sphinxword.getWord().getSpelling();
    }

    @Override
    public float getConfidence() {
        return (float) Math.exp(sphinxword.getScore());
    }

    @Override
    public String toString() {
        return getWord();
    }
}
