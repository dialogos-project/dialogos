package edu.cmu.lti.dialogos.sphinx.client;

/**
 * Created by timo on 13.11.17.
 */
public class G2PEntry {
    String graphemes = "";
    String phonemes = "";

    public G2PEntry() {}

    public G2PEntry(String g, String p) {
        graphemes = g;
        phonemes = p;
    }

    public String getGraphemes() {
        return graphemes;
    }

    public String getPhonemes() {
        return phonemes;
    }

    public void setGraphemes(String graphemes) {
        this.graphemes = graphemes;
    }

    public void setPhonemes(String phonemes) {
        this.phonemes = phonemes;
    }
}
