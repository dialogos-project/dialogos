package de.saar.coli.dialogos.pocketsphinx.plugin;

import com.clt.script.exp.Match;

/**
 * @author dabo
 *
 */
public class MatchResult {

    private Match match;
    private int edge;

    public MatchResult(Match match, int edge) {

        this.match = match;
        this.edge = edge;
    }

    public int getEdge() {

        return this.edge;
    }

    public Match getMatch() {

        return this.match;
    }
}
