package com.clt.srgf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 *
 *
 * @author Daniel Bobbert
 * @version 1.0
 */
class TerminalItem extends Item {

    private Terminal t;

    public TerminalItem(Terminal t, int repeatMin, int repeatMax) {

        super(repeatMin, repeatMax);
        this.t = t;
    }

    protected TerminalItem(TerminalItem item) {

        super(item);
        this.t = item.t;
    }

    @Override
    public Item copy() {

        return new TerminalItem(this);
    }

    @Override
    public List<Parse> shift_(Parse p) {

        Input input = p.getInput();
        // int numMatchedWords = t.match(input);
        List<Input.Match> matches = input.match(this.t);
        if (matches.isEmpty()) {
            return Collections.emptyList();
        } else {
            this.setDone(true);
            List<Parse> continuations = new ArrayList<Parse>(matches.size());
            for (Iterator<Input.Match> it = matches.iterator(); it.hasNext();) {
                Input.Match m = it.next();
                Parse cont = it.hasNext() ? p.branch() : p;
                Input in = cont.getInput();
                Word[] matchedWords = new Word[m.numWords()];
                long start = 0;
                long end = 0;
                for (int i = 0; i < m.numWords(); i++) {
                    matchedWords[i] = in.removeFirst();

                    if (i == 0) {
                        start = Parse.getStart(matchedWords[i]);
                    }
                    if (i == m.numWords() - 1) {
                        end = Parse.getEnd(matchedWords[i]);
                    }
                }
                // Include the terminals in the parse tree.
                cont.addTerminals(matchedWords);
                if (!p.isInsideFillerRule()
                        || p.getOptions().includeFillerWordsInResult) {
                    cont.appendText(m.getString());
                }
                cont.addInterval(start, end);
                continuations.add(cont);
            }
            return continuations;
            /*
       * matchedWords = new Word[numMatchedWords]; for (int i=0;
       * i<numMatchedWords; i++) matchedWords[i] = input.removeFirst();
       * p.appendText(t.getMatchedText()); setDone(true); return itemList(p);
             */
        }
    }
}
