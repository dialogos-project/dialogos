package com.clt.srgf;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.clt.util.ConcatIterator;
import com.clt.util.MetaCollection;

/**
 * An expansion that consists of zero or more alternative subexpansions.
 *
 * @author Daniel Bobbert
 * @version 1.0
 */
public class Alternatives extends NonTerminal {

    private List<Expansion> alternatives;
    /** list of "source" probabilities; typically, they do not sum to 1, despite the name */
    private List<Double> probabilities;

    public Alternatives() {

        this(new Expansion[0]);
    }

    public Alternatives(Expansion[] alternatives) {

        this.alternatives = new ArrayList<Expansion>(alternatives.length);
        this.probabilities = new ArrayList<Double>(alternatives.length);

        for (int i = 0; i < alternatives.length; i++) {
            this.add(alternatives[i]);
        }
    }

    public Alternatives add(Expansion s) {

        return this.add(s, 1.0);
    }

    public Alternatives add(Expansion s, double probability) {

        this.alternatives.add(s);
        this.probabilities.add(new Double(probability));
        s.setOwner(this);
        s.setRule(this.getRule());
        this.makeDirty();
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void check(Collection<String> visibleIDs,
            Collection<Exception> warnings) {

        Collection<String>[] ids = new Collection[this.size()];

        for (int i = 0; i < this.size(); i++) {
            ids[i] = new HashSet<String>(visibleIDs);
            this.get(i).check(ids[i], warnings);
        }

        // now build the intersection of all visible ids
        for (int i = 0; i < this.size(); i++) {
            visibleIDs.addAll(ids[i]);
        }

        for (int i = 0; i < this.size(); i++) {
            visibleIDs.retainAll(ids[i]);
        }
    }

    @Override
    Iterator<StringBuilder> generate_(final StringBuilder prefix,
            final GenerationOptions options) {
        final List<Expansion> orderedBranches = new ArrayList<>();

        final List<Integer> ordering = new ArrayList<>();
        for (int i = 0; i < Alternatives.this.size(); i++) {
            ordering.add(i);
        }
        if (options.randomWalk) {
            Collections.shuffle(ordering);
        }

        return new ConcatIterator<StringBuilder>(
                new Iterator<Iterator<StringBuilder>>() {
            int n = 0;

            public boolean hasNext() {
                return this.n < Alternatives.this.size();
            }

            public Iterator<StringBuilder> next() {
                StringBuilder b;
                if (this.n < Alternatives.this.size() - 1) {
                    b = new StringBuilder(prefix.toString());
                } else {
                    b = prefix;
                }
                return Alternatives.this.get(ordering.get(this.n++)).generate(b, options);
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        });
    }

    @Override
    Expansion optimize_(boolean removeTags) {

        if ((this.size() == 1) && (this.getWeight(0) == 1.0)) {
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
            boolean containsEmptyRule = false;

            Alternatives result = new Alternatives();
            result.setRepeat(this.getRepeatMin(), this.getRepeatMax());

            // optimize elements, flattening simple alternatives
            // An empty sequence or NULL will cause this expansion to become optional
            for (int i = 0; i < this.size(); i++) {
                Expansion e = this.get(i).optimize(removeTags);

                if (this.get(i).getPossibleTokens().isEmpty()) {
                    // ignore unmatchable alternatives
                } else if ((e instanceof Alternatives) && (e.getRepeatMin() == 1)
                        && (e.getRepeatMax() == 1)) {
                    Alternatives a = (Alternatives) e;
                    double prob = this.getWeight(i);
                    for (int j = 0; j < a.size(); j++) {
                        result.add(a.get(j), prob * a.getWeight(j));
                    }
                } else if ((e.getRepeatMax() == 0)
                        || ((e instanceof Sequence) && (((Sequence) e).size() == 0))) {
                    if ((this.getWeight(i) == 1.0) && (result.getRepeatMin() <= 1)) {
                        result.setRepeat(0, result.getRepeatMax());
                    } else {
                        Expansion s = new Sequence();
                        s.setRepeat(0, 0);
                        result.add(s, this.getWeight(i));
                    }
                } else {
                    if ((this.get(i) instanceof Rulename)
                            && (e instanceof Rulename)
                            && ((Rulename) this.get(i)).resolve()
                                    .getPossibleTokens().contains(
                                            Token.EMPTY)) {
                        containsEmptyRule = true;
                    }
                    result.add(e, this.getWeight(i));
                }
            }

            // if one alternative is optional, the whole expansion is optional
            if (result.getRepeatMin() == 1) {
                for (int i = 0; (i < result.size()) && (result.getRepeatMin() > 0); i++) {
                    if (result.get(i).getRepeatMin() == 0) {
                        result.setRepeat(0, result.getRepeatMax());
                    }
                }
            }

            // if this expansion is already optional, each alternative need not be
            // marked
            // optional (this would only cause identical parse trees)
            if (result.getRepeatMin() == 0) {
                for (int i = 0; i < result.size(); i++) {
                    Expansion exp = result.get(i);
                    if (exp.getRepeatMin() == 0) {
                        exp.setRepeat(1, exp.getRepeatMax());
                    }
                }
            }

            if ((result.getRepeatMin() == 0) && containsEmptyRule) {
                result.setRepeat(1, result.getRepeatMax());
            }

            result.compress();

            if ((result.size() == 1) && (result.getWeight(0) == 1.0)) {
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
                return result;
            }
        }
    }

    private boolean subsumes(Expansion exp, Expansion exp2) {

        return exp.eq(exp2)
                && (exp.getRepeatMin() <= exp2.getRepeatMin())
                && ((exp.getRepeatMax() == -1) || ((exp.getRepeatMax() >= exp2
                .getRepeatMax()) && (exp2.getRepeatMax() != -1)));
    }

    public void compact() {

        // combine several single terminals into one.
        List<Expansion> terminals
                = new ArrayList<Expansion>(this.alternatives.size());
        for (int i = 0; i < this.alternatives.size(); i++) {
            Expansion exp = this.get(i);
            if ((exp instanceof Terminal) && (exp.getRepeatMin() == 1)
                    && (exp.getRepeatMax() == 1)
                    && (this.getWeight(i) == 1.0)) {
                terminals.add(exp);
                this.alternatives.remove(i);
                this.probabilities.remove(i);
                i--;
            }
        }
        if (terminals.size() == 1) {
            this.add(terminals.get(0));
        } else if (terminals.size() > 1) {
            this.add(new Terminal(terminals.toArray(new Terminal[terminals.size()])));
        }
    }

    void compress() {

        this.compact();

        // remove duplicate alternatives
        for (int i = 0; i < this.alternatives.size(); i++) {
            Expansion exp = this.alternatives.get(i);
            for (int j = 0; j < this.alternatives.size(); j++) {
                if (j != i) {
                    Expansion exp2 = this.alternatives.get(j);
                    if (this.subsumes(exp, exp2)) {
                        this.probabilities.set(i, new Double(this.getWeight(i)
                                + this.getWeight(j)));
                        this.probabilities.remove(j);
                        this.alternatives.remove(j);
                        j--;
                    } else if (this.subsumes(exp2, exp)) {
                        this.probabilities.set(j, new Double(this.getWeight(i)
                                + this.getWeight(j)));
                        this.probabilities.remove(i);
                        this.alternatives.remove(i);
                        i--;
                        break;
                    }
                }
            }
        }

        /*
     * // factor out common prefix sequences // At this stage, this alternatives
     * can only contain non optional sequences of length >= 2 and atoms for (int
     * i=0; i<alternatives.size(); i++) { Expansion exp = (Expansion)
     * alternatives.get(i); Expansion head = exp; if (head instanceof Sequence)
     * head = ((Sequence) head).get(0); for (int j=i+1; j<alternatives.size();
     * j++) { Expansion exp2 = (Expansion) alternatives.get(j); Expansion head2
     * = exp2; if (head2 instanceof Sequence) head2 = ((Sequence) head2).get(0);
     * 
     * 
     * } }
         */
    }

    @Override
    public int size() {
        return this.alternatives.size();
    }

    @Override
    public Expansion get(int i) {
        return this.alternatives.get(i);
    }

    public double getWeight(int i) {
        return this.probabilities.get(i).doubleValue();
    }

    public double getProbability(int i) {
        double sum = probabilities.stream().mapToDouble(Double::doubleValue).sum();
        assert sum != 0.0;
        return sum != 0.0 ? getWeight(i) / sum : getWeight(i);
    }

    @Override
    boolean eq(Expansion e) {

        if (e instanceof Alternatives) {
            Alternatives a = (Alternatives) e;
            if (this.size() == a.size()) {
                for (int i = 0; i < this.size(); i++) {
                    Expansion e1 = this.get(i);
                    Expansion e2 = a.get(i);
                    if ((e1.getRepeatMin() != e2.getRepeatMin())
                            || (e1.getRepeatMax() != e2.getRepeatMax())
                            || (this.getWeight(i) != a.getWeight(i))
                            || !e1.eq(e2)) {
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
    Item createInstance() {

        return new AlternativesItem(this, this.getRepeatMin(), this.getRepeatMax());
    }

    @Override
    Collection<TreeMatch> matchImpl(TreeMatch match, Grammar grammar,
            ParseOptions options) {

        List<Collection<TreeMatch>> continuations
                = new ArrayList<Collection<TreeMatch>>(this.size());
        for (int i = 0; i < this.size(); i++) {
            TreeMatch m = i < this.size() - 1 ? match.branch() : match;
            Collection<TreeMatch> c = this.get(i).match(m, grammar, options);
            if (c.size() > 0) {
                continuations.add(c);
            }
        }
        if (continuations.size() == 0) {
            return Collections.emptyList();
        } else if (continuations.size() == 1) {
            return continuations.get(0);
        } else {
            return new MetaCollection<TreeMatch>(continuations);
        }
    }

    @Override
    boolean computePossibleTokens_(boolean recompute) {

        boolean changed = false;

        for (int i = 0; i < this.size(); i++) {
            if (this.get(i).computePossibleTokens(this.getCompileID())) {
                changed = true;
            }
        }

        for (int i = 0; i < this.alternatives.size(); i++) {
            for (Iterator<Token<?>> it = this.get(i).getPossibleTokens().iterator(); it
                    .hasNext();) {
                if (this.addPossibleToken(it.next())) {
                    changed = true;
                }
            }
        }

        return changed;
    }



    /** an alternative has rule weights if not all of the rule weights are the same */
    private boolean hasRuleWeights() {
        double weight = getWeight(0);
        for (int i = 1; i < size(); i++) {
            if (getWeight(i) != weight)
                return true;
        }
        return false;
    }

    @Override
    void write(PrintWriter w, Grammar.Format format) {
        boolean jsgfNeedsWeight = format == Grammar.Format.JSGF && hasRuleWeights();
        switch (format) {
            case SRGF:
            case JSGF:
            case JSGFwithGarbage:
            case TEMIC:
            case LH:
            case VOCON:
            case VOCON_G:
                if ((this.size() == 0)
                        && ((format == Grammar.Format.VOCON) || (format == Grammar.Format.VOCON_G))) {
                    w.print("<VOID>");
                } else {
                    if (this.getOwner() == null) {
                        w.println();
                        w.print("   ");
                    }
                    for (int i = 0; i < this.size(); i++) {
                        if (i > 0) {
                            w.print(" | ");
                        }
                        if ((format == Grammar.Format.SRGF) && (this.getWeight(i) != 1.0)) {
                            w.print("(");
                        }
                        if ((format == Grammar.Format.SRGF) && (this.getWeight(i) != 1.0)
                         || jsgfNeedsWeight) {
                            w.print("/" + this.getWeight(i) + "/ ");
                        }
                        this.get(i).export(w, format);
                        if ((format == Grammar.Format.SRGF) && (this.getWeight(i) != 1.0)) {
                            w.print(")");
                        }
                        if (this.getOwner() == null) {
                            w.println();
                        }
                    }
                }
                break;
            case GRXML:
                com.clt.xml.XMLWriter xml = (com.clt.xml.XMLWriter) w;
                if (this.size() == 0) {
                    xml.printElement("ruleref", new String[]{"special"},
                            new String[]{"VOID"});
                } else {
                    xml.openElement("one-of");
                    for (int i = 0; i < this.size(); i++) {
                        if (this.getWeight(i) == 1.0) {
                            xml.openElement("item");
                        } else {
                            xml.openElement("item", new String[]{"weight"},
                                    new String[]{String.valueOf(this
                                                .getWeight(i))});
                        }
                        this.get(i).export(xml, format);
                        xml.closeElement("item");
                    }
                    xml.closeElement("one-of");
                }
                break;
            case NGSL:
                if (this.getOwner() == null) {
                    w.println("[");
                } else {
                    w.print("[");
                }
                for (int i = 0; i < this.size(); i++) {
                    if (this.getOwner() == null) {
                        w.print("   ");
                    } else {
                        w.print(" ");
                    }
                    this.get(i).export(w, format);
                    if (this.getWeight(i) != 1.0) {
                        w.print("~" + this.getWeight(i));
                    }
                    if (this.getOwner() == null) {
                        w.println();
                    }
                }
                if (this.getOwner() == null) {
                    w.println("]");
                } else {
                    w.print(" ]");
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown export format");
        }
    }

    @Override
    WordGraph.Node[] createWordGraphImpl(WordGraph.Node predecessors[],
            boolean mergePrivateRules,
            Tokenizer tokenizer) {

        List<WordGraph.Node> nodes = new LinkedList<WordGraph.Node>();
        for (int i = 0; i < this.size(); i++) {
            WordGraph.Node[] successors
                    = this.get(i).createWordGraph(predecessors, mergePrivateRules,
                            tokenizer);
            nodes.addAll(Arrays.asList(successors));
        }
        return nodes.toArray(new WordGraph.Node[nodes.size()]);
    }
}
