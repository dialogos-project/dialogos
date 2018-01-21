package com.clt.srgf;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.clt.util.ConcatIterator;

/**
 * The base class for all expansions. An expansion is the right hand side of a
 * {@link Rule}.
 *
 * @see Rule
 * @author Daniel Bobbert
 * @version 1.0
 */
public abstract class Expansion {
    private Expansion owner;
    private Rule rule;
    private Set<Token<?>> possibleTokens = new HashSet<Token<?>>();
    private Object compileID = null;
    private int repeatMin;
    private int repeatMax; // an expansion with repeatMax==0 is equivalent to NULL

    public Expansion() {

        this(null);
    }

    public Expansion(Expansion owner) {

        this.owner = owner;

        // default: expansion is evaluated exactly once
        this.repeatMin = 1;
        this.repeatMax = 1;
    }

    public final Rule getRule() {

        return this.rule;
    }

    void setRule(Rule rule) {

        this.rule = rule;
    }

    public void setRepeat(int min, int max) {

        if ((max != -1) && (min > max)) {
            throw new IllegalArgumentException("must be min <= max or max == -1");
        }
        this.repeatMin = min;
        this.repeatMax = max;
    }

    public int getRepeatMin() {

        return this.repeatMin;
    }

    public int getRepeatMax() {

        return this.repeatMax;
    }

    protected Expansion getOwner() {

        return this.owner;
    }

    protected void setOwner(Expansion owner) {

        this.owner = owner;
    }

    boolean isDirty() {

        return this.compileID == null;
    }

    protected void makeDirty() {

        this.compileID = null;
        if (this.owner != null) {
            this.owner.makeDirty();
        }
    }

    static <T> Set<T> createSet(int size) {

        return new HashSet<T>(size * 4 / 3 + 1, 0.75f);
    }

    final boolean canMatch(Input input) {

        for (Iterator<Token<?>> it = this.possibleTokens.iterator(); it.hasNext();) {
            Token<?> t = it.next();
            if (t.match(input)) {
                return true;
            }
        }
        return false;
    }

    final boolean addPossibleToken(Token<?> t) {

        return this.possibleTokens.add(t);
    }

    final boolean computePossibleTokens(Object id) {

        boolean changed = false;

        if (this.compileID != id) {
            this.possibleTokens = new HashSet<Token<?>>();
            this.compileID = id;
            changed = true;
        }

        if (this.computePossibleTokens_(changed)) {
            changed = true;
        }

        if (this.repeatMin == 0) {
            if (this.addPossibleToken(Token.EMPTY)) {
                changed = true;
            }
        }

        return changed;
    }

    abstract boolean computePossibleTokens_(boolean recompute);

    abstract Iterator<StringBuilder> generate_(StringBuilder prefix,
            GenerationOptions options);

    final Iterator<StringBuilder> generate(StringBuilder prefix,
            final GenerationOptions options) {

        int min = this.getRepeatMin();
        int max = this.getRepeatMax();
        if (max == -1) {
            max = min + options.maxRepeats;
        }

        if (max == 0) {
            List<StringBuilder> empty = Collections.emptyList();
            return empty.iterator();
        } else {
            Iterator<StringBuilder> result = Collections.singleton(prefix).iterator();
            for (int i = 1; i <= min; i++) {
                final Iterator<StringBuilder> it = result;

                result
                        = new ConcatIterator<StringBuilder>(
                                new Iterator<Iterator<StringBuilder>>() {

                            public boolean hasNext() {

                                return it.hasNext();
                            }

                            public Iterator<StringBuilder> next() {

                                return Expansion.this.generate_(it.next(), options);
                            }

                            public void remove() {

                                throw new UnsupportedOperationException();
                            }
                        });
            }

            result = this.generateRepetitions(result, options, max - min);

            return result;
        }
    }

    private Iterator<StringBuilder> generateRepetitions(
            final Iterator<StringBuilder> source,
            final GenerationOptions options, final int n) {

        if (n == 0) {
            return source;
        } else {
            return new ConcatIterator<StringBuilder>(
                    new Iterator<Iterator<StringBuilder>>() {

                public boolean hasNext() {

                    return source.hasNext();
                }

                @SuppressWarnings("unchecked")
                public Iterator<StringBuilder> next() {

                    StringBuilder b = source.next();

                    return new ConcatIterator<StringBuilder>(Collections
                            .singleton(b).iterator(),
                            Expansion.this.generateRepetitions(Expansion.this
                                    .generate_(new StringBuilder(b.toString()), options),
                                    options, n - 1));
                }

                public void remove() {

                    throw new UnsupportedOperationException();
                }
            });
        }
    }

    Object getCompileID() {

        return this.compileID;
    }

    final Set<Token<?>> getPossibleTokens() {

        return this.possibleTokens;
    }

    boolean collectRules(Collection<Rule> s, ParseOptions options) {

        return false;
    }

    boolean collectWords(Collection<String> s, ParseOptions options) {

        return false;
    }

    final Expansion optimize(boolean removeTags) {

        Set<Token<?>> s = this.getPossibleTokens();
        if ((this.getRepeatMax() == 0) || Token.onlyEmptyToken(s, removeTags)) {
            Expansion e = new Sequence();
            e.setRepeat(0, 0);
            return e;
        } else if (s.isEmpty()) {
            return new Alternatives();
        } else {
            return this.optimize_(removeTags);
        }
    }

    abstract Expansion optimize_(boolean removeTags);

    final Collection<TreeMatch> match(TreeMatch match, Grammar grammar,
            ParseOptions options) {

        Collection<TreeMatch> continuations = Collections.singleton(match);

        // Idee zur Behandlung von n* et.al.:
        // Begrenzung nach unten: Fuer min >= 1 muss die expansion zunaechst min
        // mal matchen
        // Begrenzung nach oben: Wenn (x x x) nicht matched, tut es (x x x x)
        // erst recht nicht.
        for (int i = 1; (i <= this.getRepeatMin()) && !continuations.isEmpty(); i++) {
            List<TreeMatch> c = new LinkedList<TreeMatch>();
            for (TreeMatch m : continuations) {
                c.addAll(this.matchImpl(m, grammar, options));
            }
            continuations = c;
        }

        // fuer jede weitere Iteration werden die Ergebnisse zur Gesamtmenge
        // hinzugefuegt.
        if (!continuations.isEmpty()) {
            LinkedList<TreeMatch> results = new LinkedList<TreeMatch>(continuations);
            int max = this.getRepeatMax();
            if (max == -1) {
                max = Integer.MAX_VALUE;
            }

            for (int i = this.getRepeatMin() + 1; (i <= max)
                    && !continuations.isEmpty(); i++) {
                List<TreeMatch> c = new LinkedList<TreeMatch>();
                for (TreeMatch m : continuations) {
                    int size = m.getInputSize();
                    Collection<TreeMatch> cs
                            = this.matchImpl(m.branch(), grammar, options);
                    // only accept additional productions, when they actually
                    // used up a word.
                    // Otherwise we could get into an infinite loop of empty
                    // productions
                    for (TreeMatch continuation : cs) {
                        if (continuation.getInputSize() != size) {
                            c.add(continuation);
                        }
                    }
                }
                continuations = c;
                results.addAll(continuations);
            }
            continuations = results;
        }

        return continuations;
    }

    abstract Collection<TreeMatch> matchImpl(TreeMatch match, Grammar grammar,
            ParseOptions options);

    abstract Item createInstance();

    abstract void check(Collection<String> visibleIDs,
            Collection<Exception> warnings);

    abstract boolean eq(Expansion e);

    private void write(PrintWriter w, Grammar.Format format, boolean parens) {

        if (format == Grammar.Format.GRXML) {
            if (parens) {
                ((com.clt.xml.XMLWriter) w).openElement("item");
                this.write(w, format);
                ((com.clt.xml.XMLWriter) w).closeElement("item");
            } else {
                this.write(w, format);
            }
        } else {
            if (parens) {
                w.print('(');
                this.write(w, format);
                w.print(')');
            } else {
                this.write(w, format);
            }
        }
    }

    public void export(PrintWriter w, Grammar.Format format) {

        boolean multiToken
                = ((this instanceof NonTerminal) && (((NonTerminal) this).size() > 1))
                || ((this instanceof Terminal) && (((Terminal) this).size() > 1));

        boolean alternative
                = (((this instanceof Alternatives) && (((Alternatives) this).size() > 1)) || ((this instanceof Terminal) && (((Terminal) this)
                .size() > 1)))
                && (this.owner != null)
                && !(this.owner instanceof Alternatives);

        switch (format) {
            case SRGF:
            case TEMIC:
            case JSGF:
                if ((this.getRepeatMin() == 1) && (this.getRepeatMax() == -1)) {
                    this.write(w, format, multiToken);
                    w.print("+");
                } else if ((this.getRepeatMin() == 0) && (this.getRepeatMax() == -1)) {
                    this.write(w, format, multiToken);
                    w.print("*");
                } else if ((this.getRepeatMin() == 0) && (this.getRepeatMax() == 1)) {
                    w.print("[");
                    this.write(w, format, false);
                    w.print("]");
                } else {
                    if (format == Grammar.Format.SRGF) {
                        if ((this.getRepeatMin() != 1) || (this.getRepeatMax() != 1)) {
                            this.write(w, format, multiToken);
                            w.print("<" + this.getRepeatMin() + "-" + this.getRepeatMax()
                                    + ">");
                        } else {
                            this.write(w, format, alternative);
                        }
                    } else if (this.getRepeatMin() == this.getRepeatMax()) {
                        for (int i = 0; i < this.getRepeatMin(); i++) {
                            if (i > 0) {
                                w.print(" ");
                            }
                            this.write(w, format, alternative);
                        }
                    } else {
                        w.print("(");
                        for (int i = this.getRepeatMin(); i <= this.getRepeatMax(); i++) {
                            if (i > this.getRepeatMin()) {
                                w.print(" | ");
                            }
                            for (int j = 0; j < i; j++) {
                                if (j > 0) {
                                    w.print(" ");
                                }
                                this.write(w, format, alternative);
                            }
                        }
                    }
                }
                break;
            case GRXML:
                com.clt.xml.XMLWriter xml = (com.clt.xml.XMLWriter) w;
                if ((this.getRepeatMin() == 1) && (this.getRepeatMax() == 1)) {
                    this.write(w, format, false);
                } else if (this.getRepeatMax() == -1) {
                    xml.openElement("item", new String[]{"repeat"},
                            new String[]{this.getRepeatMin() + "-"});
                    this.write(w, format, multiToken);
                    xml.closeElement("item");
                } else {
                    xml.openElement("item", new String[]{"repeat"},
                            new String[]{this.getRepeatMin() + "-"
                                + this.getRepeatMax()});
                    this.write(w, format, multiToken);
                    xml.closeElement("item");
                }
                break;
            case LH:
                if (this.getRepeatMax() == 0) {
                    w.print("()");
                } else if ((this.getRepeatMin() == 0) && (this.getRepeatMax() == 1)) {
                    w.print("!optional(");
                    this.write(w, format, false);
                    w.print(")");
                } else if ((this.getRepeatMin() == 1) && (this.getRepeatMax() == 1)) {
                    this.write(w, format, alternative);
                } else {
                    w.print("!repeat(");
                    this.write(w, format, false);
                    w.print(",");
                    w.print(this.getRepeatMin());
                    w.print(",");
                    if (this.getRepeatMax() == -1) {
                        w.print("*");
                    } else {
                        w.print(this.getRepeatMax());
                    }
                    w.print(")");
                }
                break;
            case VOCON:
            case VOCON_G:
                if (this.getRepeatMax() == 0) {
                    w.print("<VOID>");
                } else if ((this.getRepeatMin() == 0) && (this.getRepeatMax() == 1)) {
                    // w.print("!optional(");
                    w.print("[");
                    this.write(w, format, false);
                    w.print("]");
                    // w.print(")");
                } else if ((this.getRepeatMin() == 1) && (this.getRepeatMax() == 1)) {
                    this.write(w, format, alternative);
                } else if ((this.getRepeatMin() == 0) && (this.getRepeatMax() == -1)) {
                    w.print("(");
                    this.write(w, format, false);
                    w.print(")*");
                } else if ((this.getRepeatMin() == 1) && (this.getRepeatMax() == -1)) {
                    // write(w, format, false);
                    w.print(" (");
                    this.write(w, format, false);
                    w.print(")+");
                    // w.print(")*");
                } else {
                    w.print("!repeat(");
                    this.write(w, format, false);
                    w.print(",");
                    w.print(this.getRepeatMin());
                    w.print(",");
                    if (this.getRepeatMax() == -1) {
                        w.print("*");
                    } else {
                        w.print(this.getRepeatMax());
                    }
                    w.print(")");
                }
                break;
            case NGSL:
                if (this.getRepeatMax() == 0) {
                    w.print("()");
                } else if ((this.getRepeatMin() == 0) && (this.getRepeatMax() == 1)) {
                    w.print("?");
                    this.write(w, format, false);
                } else if ((this.getRepeatMin() == 0) && (this.getRepeatMax() == -1)) {
                    w.print("*");
                    this.write(w, format, false);
                } else if ((this.getRepeatMin() == 1) && (this.getRepeatMax() == -1)) {
                    w.print("+");
                    this.write(w, format, false);
                } else if ((this.getRepeatMin() == 1) && (this.getRepeatMax() == 1)) {
                    this.write(w, format, false);
                } else if (this.getRepeatMin() == this.getRepeatMax()) {
                    w.print("(");
                    for (int i = 0; i < this.getRepeatMin(); i++) {
                        if (i > 0) {
                            w.print(" ");
                        }
                        this.write(w, format, false);
                    }
                    w.print(")");
                } else {
                    w.print("[");
                    for (int i = this.getRepeatMin(); i <= this.getRepeatMax(); i++) {
                        if (i > this.getRepeatMin()) {
                            w.print(" ");
                        }
                        w.print("(");
                        for (int j = 0; j < i; j++) {
                            if (j > 0) {
                                w.print(" ");
                            }
                            this.write(w, format, false);
                        }
                        w.print(")");
                    }
                    w.print("]");
                }
                break;
            case EXTLAT:
                new WordGraph(this, false, null).print(w);
                break;
            default:
                throw new IllegalArgumentException("Unknown export format");
        }
    }

    abstract void write(PrintWriter w, Grammar.Format format);

    WordGraph.Node[] createWordGraph(WordGraph.Node predecessors[],
            boolean mergePrivateRules,
            Tokenizer tokenizer) {

        int min = this.getRepeatMin();
        int max = this.getRepeatMax();
        if ((min == 1) && (max == 1)) {
            return this.createWordGraphImpl(predecessors, mergePrivateRules,
                    tokenizer);
        } else if (max == -1) {
            for (int i = 0; i < min - 1; i++) {
                predecessors
                        = this.createWordGraphImpl(predecessors, mergePrivateRules, tokenizer);
            }

            /*
       * WordGraph.Node start = new WordGraph.WordNode("!NULL"); for (int i=0;
       * i<predecessors.length; i++) predecessors[i].addEdge(start);
       * predecessors = createWordGraphImpl(new WordGraph.Node[] { start },
       * mergePrivateRules, tokenizer); for (int i=0; i<predecessors.length;
       * i++) if (predecessors[i] != start) predecessors[i].addEdge(start);
       * return new WordGraph.Node[] { start };
             */
            WordGraph.Node loop[] = predecessors;

            WordGraph.Node end[]
                    = this.createWordGraphImpl(loop, mergePrivateRules, tokenizer);
            for (int i = 0; i < end.length; i++) {
                for (int j = 0; j < loop.length; j++) {
                    if (end[i] != loop[j]) {
                        end[i].addEdge(loop[j]);
                    }
                }
            }
            if (min == 0) {
                return loop;
            } else {
                return end;
            }
        } else {
            Collection<WordGraph.Node> nodes = new HashSet<WordGraph.Node>();
            for (int i = 0; i < min; i++) {
                predecessors
                        = this.createWordGraphImpl(predecessors, mergePrivateRules, tokenizer);
            }
            nodes.addAll(Arrays.asList(predecessors));
            for (int i = min; i < max; i++) {
                predecessors
                        = this.createWordGraphImpl(predecessors, mergePrivateRules, tokenizer);
                nodes.addAll(Arrays.asList(predecessors));
            }
            return nodes.toArray(new WordGraph.Node[nodes.size()]);
        }
    }

    abstract WordGraph.Node[] createWordGraphImpl(WordGraph.Node predecessors[],
            boolean mergePrivateRules, Tokenizer tokenizer);

    @Override
    public String toString() {

        StringWriter w = new StringWriter();
        this.export(new PrintWriter(w), Grammar.Format.SRGF);
        return w.toString();
    }

    public static Expansion parse(String in)
            throws Exception {

        return com.clt.script.parser.Parser.parseRuleExpansion(in, null);
    }
}
