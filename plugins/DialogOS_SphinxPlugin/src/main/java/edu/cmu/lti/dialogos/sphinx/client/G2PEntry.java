package edu.cmu.lti.dialogos.sphinx.client;

/**
 * Created by timo on 13.11.17.
 */
public class G2PEntry implements Comparable<G2PEntry> {
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

    @Override
    public int compareTo(G2PEntry o) {
        // let's primarily sort based on graphemes and then not worry much about the phonemes
        return graphemes.compareTo(o.graphemes) * 2^15 + phonemes.compareTo(o.phonemes);
    }

    @Override
    public int hashCode() {
        return graphemes.hashCode() * 17 + phonemes.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof G2PEntry && graphemes.equals(((G2PEntry) o).graphemes) && phonemes.equals(((G2PEntry) o).phonemes);
    }

}
