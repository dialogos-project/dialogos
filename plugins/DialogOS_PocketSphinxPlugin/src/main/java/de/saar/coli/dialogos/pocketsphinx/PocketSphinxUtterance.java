package de.saar.coli.dialogos.pocketsphinx;

import com.clt.speech.htk.MlfNode;
import com.clt.speech.recognition.Utterance;
import com.clt.speech.recognition.Word;
import com.clt.speech.recognition.simpleresult.SimpleRecognizerWord;

import java.util.ArrayList;
import java.util.List;

import com.clt.util.StringTools;

/**
 *
 * @author koller
 */
public class PocketSphinxUtterance implements Utterance {

    private List<SimpleRecognizerWord> words;
    private int score;

    public PocketSphinxUtterance(String hypothesis, int score) {
        this.words = new ArrayList<>();
        this.score = score;

        if (hypothesis != null) {
            for (String wr : hypothesis.split("\\s+")) {
                this.words.add(new SimpleRecognizerWord(wr));
            }
        }
    }

    @Override
    public int length() {
        return words.size();
    }

    @Override
    public float getConfidence() {
        // TODO - this can probably be done better
        return (float) Math.exp(score);
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
        return "SphinxUtterance{"
                + "words=" + words
                + ", score=" + score
                + ", confidence=" + getConfidence()
                + '}';
    }
}
