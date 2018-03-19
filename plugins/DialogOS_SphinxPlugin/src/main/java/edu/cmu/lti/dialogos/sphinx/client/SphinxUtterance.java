package edu.cmu.lti.dialogos.sphinx.client;

import com.clt.speech.htk.MlfNode;

import java.util.ArrayList;
import java.util.List;

import com.clt.speech.recognition.Utterance;
import com.clt.speech.recognition.Word;
import com.clt.util.StringTools;
import edu.cmu.sphinx.result.WordResult;
import edu.cmu.sphinx.util.LogMath;

/**
 *
 * @author koller
 */
public class SphinxUtterance implements Utterance {
    private List<SphinxWord> words;
    private float logConfidence;

    public SphinxUtterance(List<WordResult> words) {
        this.words = new ArrayList<>();
        this.logConfidence = 0;

        for( WordResult wr : words ) {
            if( ! wr.isFiller() ) { // skip silence tokens etc.
                this.words.add(new SphinxWord(wr));
                this.logConfidence += (float) wr.getScore();
            }
        }
    }

    @Override
    public int length() {
        return words.size();
    }

    @Override
    public float getConfidence() {
        return (float) LogMath.getLogMath().logToLinear(logConfidence);
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
        return "SphinxUtterance{" +
                "words=" + words +
                ", logConfidence=" + logConfidence +
                ", confidence=" + getConfidence() +
                '}';
    }
}
