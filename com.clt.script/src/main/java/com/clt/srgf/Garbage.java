package com.clt.srgf;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.tree.TreeNode;

import com.clt.util.MetaCollection;

/**
 *
 *
 * @author Daniel Bobbert
 * @version 1.0
 */
final class Garbage extends Expansion {

    public static final Token<Class<Garbage>> garbageToken = new Token<Class<Garbage>>(Garbage.class) {

        @Override
        public boolean match(Input input) {

            return !input.isEmpty();
        }

        @Override
        public String toString() {

            return "*";
        }
    };

    Garbage() {
        this.setRepeat(1, 1);
    }

    @Override
    Item createInstance() {

        return new GarbageItem(this.getRepeatMin(), this.getRepeatMax());
    }

    @SuppressWarnings("unchecked")
    @Override
    Collection<TreeMatch> matchImpl(TreeMatch match, Grammar grammar,
            ParseOptions options) {

        if (!options.includeGarbageInParseTree) {
            return Collections.singleton(match);
        } else if (!match.hasMoreInput()) {
            return Collections.emptyList();
        } else {
            LinkedList<TreeNode> input = match.getInputNodes();
            TreeNode n = input.getFirst();
            if (n.getAllowsChildren()) {
                return Collections.emptyList();
            } else {
                input.removeFirst();
                if (options.includeFillerWordsInResult) {
                    match.appendText(n.toString());
                }
                /* +++ */
                Collection<TreeMatch> c
                        = match.createSparseBranches(grammar, options, true, null);
                if (!c.isEmpty()) {
                    return new MetaCollection<>(Collections.singleton(match), c);
                } else {
                    return Collections.singleton(match);
                }
            }
        }
    }

    @Override
    boolean eq(Expansion e) {

        return e instanceof Garbage;
    }

    @Override
    public void check(Collection<String> visibleIDs,
            Collection<Exception> warnings) {
        // nothing to be checked
    }

    @Override
    Iterator<StringBuilder> generate_(StringBuilder prefix,
            GenerationOptions options) {

        if (prefix.length() > 0) {
            prefix.append(" ");
        }
        if (options.classFilter != null) {
            prefix.append(Grammar.substituteName(options.classFilter, "GARBAGE"));
        } else {
            prefix.append("...");
        }
        return Collections.singleton(prefix).iterator();
    }

    @Override
    Expansion optimize_(boolean removeTags) {

        Expansion g = new Garbage();
        g.setRepeat(this.getRepeatMin(), this.getRepeatMax());
        return g;
    }

    @Override
    void write(PrintWriter w, Grammar.Format format) {

    }

    @Override
    WordGraph.Node[] createWordGraphImpl(WordGraph.Node[] predecessors,
            boolean mergePrivateRules,
            Tokenizer tokenizer) {
        return predecessors;
    }

    @Override
    boolean computePossibleTokens_(boolean recompute) {
        return this.addPossibleToken(Garbage.garbageToken);
    }

    static class GarbageItem
            extends TerminalItem {

        private int penalty = 1;

        public GarbageItem(int repeatMin, int repeatMax) {

            super(new Terminal("..."), repeatMin, repeatMax);
        }

        private GarbageItem(GarbageItem item) {

            super(item);
        }

        @Override
        public Item copy() {

            return new GarbageItem(this);
        }

        @Override
        public List<Parse> shift_(Parse p) {

            this.penalty = 1;
            Input input = p.getInput();
            if (!input.isEmpty()) {
                Word w = input.removeFirst();
                if (p.getOptions().includeGarbageInParseTree) {
                    // Include garbage terminals in the parse tree.
                    p.addTerminals(new Word[]{w});
                }

                if (p.getOptions().includeFillerWordsInResult) {
                    p.appendText(w.getWord());
                }
                p.addInterval(Parse.getStart(w), Parse.getEnd(w));

                this.setDone(true);
                // matching explicit recognition garbage should not have a penalty
                if (w.getWord().equals("<...>")) {
                    this.penalty = 0;
                }
                return Item.itemList(p);
            } else {
                return Collections.emptyList();
            }
        }

        @Override
        public int getPenalty() {

            return this.penalty;
        }
    }
}
