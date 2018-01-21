package com.clt.srgf;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.clt.util.ConcatIterator;

/**
 * An expansion that consists of zero or more consecutive subexpansions.
 *
 * @author Daniel Bobbert
 * @version 1.0
 */
public class Sequence extends NonTerminal {

    private List<Expansion> symbols;

    public Sequence() {

        this(new Expansion[0]);
    }

    public Sequence(Expansion[] elements) {

        this.symbols = new ArrayList<Expansion>(elements.length);

        for (int i = 0; i < elements.length; i++) {
            this.add(elements[i]);
        }
    }

    public Sequence add(Expansion s) {

        this.symbols.add(s);
        s.setOwner(this);
        s.setRule(this.getRule());
        this.makeDirty();
        return this;
    }

    @Override
    public void check(Collection<String> visibleIDs,
            Collection<Exception> warnings) {

        for (int i = 0; i < this.size(); i++) {
            this.get(i).check(visibleIDs, warnings);
        }
    }

    @Override
    Iterator<StringBuilder> generate_(StringBuilder prefix,
            final GenerationOptions options) {

        if (this.size() == 0) {
            return Collections.singleton(prefix).iterator();
        } else {
            Iterator<StringBuilder> result = this.get(0).generate(prefix, options);
            for (int i = 1; i < this.size(); i++) {
                final int n = i;
                final Iterator<StringBuilder> it = result;

                result
                        = new ConcatIterator<StringBuilder>(
                                new Iterator<Iterator<StringBuilder>>() {

                            public boolean hasNext() {

                                return it.hasNext();
                            }

                            public Iterator<StringBuilder> next() {

                                return Sequence.this.get(n).generate(it.next(), options);
                            }

                            public void remove() {

                                throw new UnsupportedOperationException();
                            }
                        });
            }

            return result;
        }
        /*
     * Collection results = Collections.singleton(prefix); for (int i=0;
     * i<size(); i++) { Collection c = new LinkedList(); for (Iterator it =
     * results.iterator(); it.hasNext(); )
     * c.addAll(get(i).generate((StringBuilder) it.next(), options)); results =
     * c; } return results;
         */
    }

    @Override
    Expansion optimize_(boolean removeTags) {

        if (this.size() == 1) {
            Expansion e = this.get(0).optimize(removeTags);

            int min = e.getRepeatMin() * this.getRepeatMin();
            int max = e.getRepeatMax() * this.getRepeatMax();
            if ((max != 0)
                    && ((e.getRepeatMax() == -1) || (this.getRepeatMax() == -1))) {
                e.setRepeat(min, -1);
            } else {
                e.setRepeat(min, max);
            }

            return e;
        } else {
            Sequence result = new Sequence();
            result.setRepeat(this.getRepeatMin(), this.getRepeatMax());

            // flatten simple sequences, removing NULL expansions (repeatMax == 0)
            for (int i = 0; i < this.size(); i++) {
                Expansion e = this.get(i).optimize(removeTags);

                if (e.getRepeatMax() != 0) {
                    if ((e instanceof Sequence) && (e.getRepeatMin() == 1)
                            && (e.getRepeatMax() == 1)) {
                        Sequence s = (Sequence) e;
                        for (int j = 0; j < s.size(); j++) {
                            result.add(s.get(j));
                        }
                    } else {
                        Set<Token<?>> s = this.get(i).getPossibleTokens();
                        if (!Token.onlyEmptyToken(s, removeTags)) {
                            result.add(e);
                        }
                    }
                }
            }

            result.collateTerminals();

            if (result.size() == 1) {
                // don't call optimize recursively because we have not computed the
                // possible tokens of result
                Expansion e = result.get(0);
                int min = e.getRepeatMin() * result.getRepeatMin();
                int max = e.getRepeatMax() * result.getRepeatMax();
                if ((max != 0)
                        && ((e.getRepeatMax() == -1) || (result.getRepeatMax() == -1))) {
                    e.setRepeat(min, -1);
                } else {
                    e.setRepeat(min, max);
                }

                return e;
            } else {
                if (result.size() == 0) {
                    result.setRepeat(0, 0);
                }

                return result;
            }
        }
    }

    private void collateTerminals() {

        // Collating will disable the ability to insert OOVs. We don't want that!
        if (true) {
            return;
        }

        for (int n = 0; n < this.size(); n++) {
            Expansion e = this.symbols.get(n);
            if ((e instanceof Terminal) && (((Terminal) e).size() == 1)) {
                StringBuilder b = new StringBuilder(((Terminal) e).get(0));
                int k = 0;
                while (n + 1 < this.size()) {
                    Expansion e2 = this.symbols.get(n + 1);
                    if ((e2 instanceof Terminal) && (((Terminal) e2).size() == 1)) {
                        b.append(" ");
                        b.append(((Terminal) e2).get(0));
                        this.symbols.remove(n + 1);
                        k++;
                    } else {
                        break;
                    }
                }
                if (k > 0) {
                    this.symbols.remove(n);
                    this.symbols.add(n, new Terminal(b.toString()));
                }
            }
        }
    }

    @Override
    public int size() {

        return this.symbols.size();
    }

    @Override
    public Expansion get(int i) {

        return this.symbols.get(i);
    }

    @Override
    Item createInstance() {

        return new SequenceItem(this, this.getRepeatMin(), this.getRepeatMax());
    }

    @Override
    Collection<TreeMatch> matchImpl(TreeMatch match, Grammar grammar,
            ParseOptions options) {

        Collection<TreeMatch> continuations = Collections.singleton(match);
        for (int i = 0; (i < this.size()) && !continuations.isEmpty(); i++) {
            List<TreeMatch> c = new LinkedList<TreeMatch>();
            for (TreeMatch m : continuations) {
                c.addAll(this.get(i).match(m, grammar, options));
            }
            continuations = c;
        }
        return continuations;
    }

    private int compiledElement = 0;

    @Override
    boolean computePossibleTokens_(boolean recompute) {

        if (recompute) {
            this.compiledElement = 0;
        }

        boolean changed = false;
        for (int i = 0; i < this.size(); i++) {
            if (this.get(i).computePossibleTokens(this.getCompileID())) {
                changed = true;
            }
        }

        // an empty sequence is empty and has no tags
        boolean prefixContainsTag = false;
        boolean prefixIsEmpty = true;

        for (int i = 0; (i <= this.compiledElement) && (i < this.size()); i++) {
            boolean containsEmpty = false;
            boolean containsTag = false;
            for (Token<?> t : this.get(i).getPossibleTokens()) {
                if (t == Token.EMPTY) {
                    if (i == this.compiledElement) {
                        this.compiledElement++;
                        changed = true;
                    }
                    // a sequence is only empty, if the prefix is empty and we have
                    // an empty token in the current item
                    if (prefixIsEmpty) {
                        containsEmpty = true;
                    }
                    if (prefixContainsTag) {
                        containsTag = true;
                    }
                } else if (t == Token.TAG) {
                    if (i == this.compiledElement) {
                        this.compiledElement++;
                        changed = true;
                    }
                    // a sequence consists of tags only if the prefix was empty or had
                    // tags and
                    // we have a tag in the current item
                    if (prefixIsEmpty || prefixContainsTag) {
                        containsTag = true;
                    }
                } else {
                    if (this.addPossibleToken(t)) {
                        changed = true;
                    }
                }
            }
            // the new prefix is only empty, if we found an empty token in the current
            // item
            prefixIsEmpty = containsEmpty;
            prefixContainsTag = containsTag;
        }

        if (!changed) {
            if (prefixContainsTag) {
                if (this.addPossibleToken(Token.TAG)) {
                    changed = true;
                }
            }
            if (prefixIsEmpty) {
                if (this.addPossibleToken(Token.EMPTY)) {
                    changed = true;
                }
            }
        }

        return changed;
    }

    @Override
    boolean collectRules(Collection<Rule> s, ParseOptions options) {

        boolean changed = false;
        for (int i = 0; i < this.size(); i++) {
            if (this.get(i).collectRules(s, options)) {
                changed = true;
            }
        }
        return changed;
    }

    @Override
    boolean collectWords(Collection<String> s, ParseOptions options) {

        boolean changed = false;
        for (int i = 0; i < this.size(); i++) {
            if (this.get(i).collectWords(s, options)) {
                changed = true;
            }
        }
        return changed;
    }

    @Override
    boolean eq(Expansion e) {

        if (e instanceof Sequence) {
            Sequence s = (Sequence) e;
            if (this.size() == s.size()) {
                for (int i = 0; i < this.size(); i++) {
                    Expansion e1 = this.get(i);
                    Expansion e2 = s.get(i);
                    if (!e1.eq(e2) || (e1.getRepeatMin() != e2.getRepeatMin())
                            || (e1.getRepeatMax() != e2.getRepeatMax())) {
                        return false;
                    }
                }
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    void write(PrintWriter w, Grammar.Format format) {

        if (this.symbols.size() == 0) {
            switch (format) {
                case SRGF:
                    w.print("$NULL");
                    break;
                case GRXML:
                    ((com.clt.xml.XMLWriter) w).printElement("item", null);
                    break;
                case JSGF:
                case TEMIC:
                case LH:
                case VOCON:
                case VOCON_G:
                    w.print("<NULL>");
                    break;
                case NGSL:
                    w.print("NULL");
                    break;
                default:
                    throw new IllegalArgumentException("Unknown export format");
            }
        } else {
            switch (format) {
                case SRGF:
                case JSGF:
                case TEMIC:
                case LH:
                case VOCON:
                case VOCON_G:
                    for (Iterator<Expansion> it = this.symbols.iterator(); it.hasNext();) {
                        it.next().export(w, format);
                        if (it.hasNext()) {
                            w.print(" ");
                        }
                    }
                    break;
                case GRXML:
                    com.clt.xml.XMLWriter xml = (com.clt.xml.XMLWriter) w;
                    if (this.size() > 1) {
                        xml.openElement("item");
                    }
                    for (Iterator<Expansion> it = this.symbols.iterator(); it.hasNext();) {
                        it.next().export(w, format);
                    }
                    if (this.size() > 1) {
                        xml.openElement("item");
                    }
                    break;
                case NGSL:
                    if (this.size() > 1) {
                        w.print("(");
                    }
                    for (Iterator<Expansion> it = this.symbols.iterator(); it.hasNext();) {
                        it.next().export(w, format);
                        if (it.hasNext()) {
                            w.print(" ");
                        }
                    }
                    if (this.size() > 1) {
                        w.print(")");
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unknown export format");
            }
        }
    }

    @Override
    WordGraph.Node[] createWordGraphImpl(WordGraph.Node[] predecessors,
            boolean mergePrivateRules,
            Tokenizer tokenizer) {

        for (int i = 0; i < this.size(); i++) {
            predecessors
                    = this.get(i).createWordGraph(predecessors, mergePrivateRules, tokenizer);
        }

        return predecessors;
    }

    @SuppressWarnings("unused")
    private static class Marker {

        Sequence sequence;
        int offset;
        boolean tag;

        Marker(Sequence sequence, int offset, boolean tag) {

            this.sequence = sequence;
            this.offset = offset;
            this.tag = tag;
        }

        @Override
        public int hashCode() {

            return this.sequence.hashCode() ^ (this.tag ? this.offset : -this.offset);
        }

        @Override
        public boolean equals(Object o) {

            if (o instanceof Marker) {
                return (((Marker) o).sequence == this.sequence)
                        && (((Marker) o).offset == this.offset)
                        && (((Marker) o).tag == this.tag);
            } else {
                return false;
            }
        }
    }
}
