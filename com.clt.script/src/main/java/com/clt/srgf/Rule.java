package com.clt.srgf;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.tree.TreeNode;

import com.clt.util.ConcatIterator;
import com.clt.util.MetaCollection;
import com.clt.util.NamedEntity;

/**
 * A rule in a context free {@link Grammar}. Note: this class has a natural
 * ordering that is inconsistent with equals.
 *
 * @see Grammar
 * @author Daniel Bobbert
 * @version 1.0
 */
public class Rule implements Comparable<Rule>, NamedEntity {

    private Grammar grammar;
    private boolean isPublic;
    private boolean isClass;
    private String name;
    private Expansion expansion;

    private boolean compilationStable = false;

    /**
     * Create a new rule
     *
     * @param isPublic whether this is a public rule
     * @param isClass whether this is a word class that can be filled at runtime
     * @param name the name of the rule
     * @param expansion the right hand side of the rule
     * @see Expansion
     */
    public Rule(boolean isPublic, boolean isClass, String name,
            Expansion expansion) {

        this.grammar = null;
        this.name = name;
        this.isPublic = isPublic;
        this.isClass = isClass;

        this.expansion = expansion;
        this.expansion.setOwner(null);
        this.expansion.setRule(this);
    }

    public final Grammar getGrammar() {

        return this.grammar;
    }

    void setGrammar(Grammar grammar) {

        this.grammar = grammar;
    }

    public void check(Collection<Exception> warnings) {

        this.expansion.check(new HashSet<String>(), warnings);
        if (this.getPossibleTokens().contains(new RuleToken(this))) {
            warnings.add(new Exception(this + " is left recursive"));
        }
    }

    public int compareTo(Rule r) {

        int result = this.getName().compareTo(r.getName());
        if (this.getGrammar() == r.getGrammar()) {
            return result;
        } else if (this.getGrammar() == null) {
            return 1;
        } else if (r.getGrammar() == null) {
            return -1;
        } else {
            return System.identityHashCode(this.getGrammar())
                    - System.identityHashCode(r.getGrammar());
        }
    }

    public Rule optimize(boolean removeTags) {

        return new Rule(this.isPublic(), this.isClass(), this.getName(),
                this.expansion.optimize(removeTags));
    }

    @SuppressWarnings("unchecked")
    public Iterator<StringBuilder> generate(final StringBuilder prefix,
            final GenerationOptions options) {

        if (this.isClass() && (options.classFilter != null)) {
            String s = Grammar.substituteName(options.classFilter, this.getName());

            if (prefix.length() > 0) {
                prefix.append(' ');
            }
            prefix.append(s);
            return Collections.singleton(prefix).iterator();
        }

        Collection<String> words = options.getDynamicVocabulary(this.getName());
        if (words != null) {
            final Iterator<String> it = words.iterator();
            Iterator<StringBuilder> dynVocIterator = new Iterator<StringBuilder>() {

                public boolean hasNext() {

                    return it.hasNext();
                }

                public StringBuilder next() {

                    StringBuilder b;
                    if (this.hasNext()
                            || !options.dynamicVocabularyReplacesOriginalContent) {
                        b = new StringBuilder(prefix.toString());
                    } else {
                        b = prefix;
                    }
                    String word = it.next();
                    word
                            = options.getDynamicVocabularySubstitution(Rule.this.getName(), word);
                    if ((b.length() > 0) && (word.length() > 0)) {
                        b.append(' ');
                    }
                    b.append(word);
                    return b;
                }

                public void remove() {

                    throw new UnsupportedOperationException();
                }
            };

            if (options.dynamicVocabularyReplacesOriginalContent) {
                return dynVocIterator;
            } else {
                return new ConcatIterator<StringBuilder>(dynVocIterator, this.expansion
                        .generate(prefix,
                                options));
            }
        }

        return this.expansion.generate(prefix, options);
    }

    public boolean isPublic() {

        return this.isPublic;
    }

    public void setPublic(boolean isPublic) {

        this.isPublic = isPublic;
    }

    public boolean isClass() {

        return this.isClass;
    }

    public String getName() {

        return this.name;
    }

    public String getGlobalName() {

        if ((this.getGrammar() != null) && (this.getGrammar().getName() != null)) {
            return this.getGrammar().getName() + "." + this.getName();
        } else {
            return this.getName();
        }
    }

    public String getNormalizedRuleName(Grammar.Format format) {

        if (format == Grammar.Format.VOCON_G) {
            // return getGlobalName().replace('.', '_');
            return this.getGlobalName();
            // return getName();
        } else {
            return this.getName();
        }
    }

    Expansion getExpansion() {

        return this.expansion;
    }

    public boolean isVoid() {

        return this.isVoid(this.expansion);
    }

    private boolean isVoid(Expansion e) {

        if ((e instanceof Rulename) && ((Rulename) e).getRulename().equals("VOID")) {
            return true;
        } else if ((e instanceof Alternatives) && (((Alternatives) e).size() == 0)
                && (e.getRepeatMin() >= 1)) {
            return true;
        } else if ((e instanceof NonTerminal) && (((NonTerminal) e).size() == 1)
                && (e.getRepeatMin() == 1)
                && (e.getRepeatMax() == 1)) {
            return this.isVoid(((NonTerminal) e).get(0));
        } else {
            return false;
        }
    }

    List<Item> createInstance(Input input, ParseOptions options) {

        List<Item> instances = new ArrayList<Item>(2);
        Collection<String> words = options.getDynamicVocabularyKeys(this.getName());
        if (words != null) {
            // System.out.println("DynVoc for rule " + getName());
            final List<Input.Match> matches
                    = new ArrayList<Input.Match>(input.size());
            for (int i = 1; i <= input.size(); i++) {
                String s = input.getWords(i);
                if (words.contains(s)) {
                    matches.add(new Input.Match(options.getDynamicVocabularySubstitution(
                            this.getName(),
                            s), i));
                }
            }

            instances.add(new DynVocItem(matches));

            if (options.dynamicVocabularyReplacesOriginalContent) {
                return instances;
            }
        }

        boolean expansionCanMatch = this.expansion.canMatch(input);
        if (!expansionCanMatch && (options.dynamicVocabulary != null)) {
            Collection<Token<?>> ts = this.getPossibleTokens();
            for (Iterator<Token<?>> it = ts.iterator(); it.hasNext()
                    && !expansionCanMatch;) {
                Token<?> t = it.next();
                if (t instanceof RuleToken) {
                    Rule r = ((RuleToken) t).getSource();
                    Collection<String> entry
                            = options.getDynamicVocabularyKeys(r.getName());
                    if (entry != null) {
                        // System.out.println("Found DynVoc for child rule " +
                        // r.getName());
                        for (int i = 1; (i <= input.size()) && !expansionCanMatch; i++) {
                            String s = input.getWords(i);
                            // System.out.println("Trying \"" + s + "\"");
                            if (entry.contains(s)) {
                                expansionCanMatch = true;
                            }
                        }
                        // System.out.println("But no match was found");
                    }
                }
            }
        }

        if (expansionCanMatch) {
            instances.add(this.expansion.createInstance());
        }

        return instances;
    }

    Collection<TreeMatch> match(TreeNode node, Grammar grammar,
            ParseOptions options,
            boolean insideFillerRule) {

        LinkedList<TreeNode> children = new LinkedList<TreeNode>();
        for (int i = 0; i < node.getChildCount(); i++) {
            TreeNode n = node.getChildAt(i);
            children.add(n);
        }

        TreeMatch match = new TreeMatch(node, children, insideFillerRule);
        Collection<TreeMatch> sparseParses
                = match.createSparseBranches(grammar, options, false,
                        this);

        Collection<Collection<TreeMatch>> results
                = new ArrayList<Collection<TreeMatch>>(
                        sparseParses.size() + 1);
        results.add(this.matchImpl(match, grammar, options));
        for (TreeMatch m : sparseParses) {
            results.add(this.matchImpl(m, grammar, options));
        }

        Collection<TreeMatch> l = new MetaCollection<TreeMatch>(results);

        Collection<TreeMatch> continuations = new ArrayList<TreeMatch>(l.size());
        // Accept only parses that have matched all children
        for (TreeMatch m : l) {
            if (!m.hasMoreInput()) {
                continuations.add(m);
            }
        }

        return continuations;
    }

    Collection<TreeMatch> match(TreeNode parent, LinkedList<TreeNode> children,
            Grammar grammar,
            ParseOptions options, boolean insideFillerRule) {

        return this.matchImpl(new TreeMatch(parent, children, insideFillerRule),
                grammar, options);
    }

    private Collection<TreeMatch> matchImpl(TreeMatch match, Grammar grammar,
            ParseOptions options) {

        List<TreeMatch> l = new LinkedList<TreeMatch>();
        Collection<String> words = options.getDynamicVocabularyKeys(this.getName());
        if (words != null) {
            TreeMatch m
                    = options.dynamicVocabularyReplacesOriginalContent ? match : match
                                    .branch();
            StringBuilder b = new StringBuilder();

            while (m.hasMoreInput()) {
                TreeNode n = m.getInputNodes().removeFirst();
                if (n.getAllowsChildren()) {
                    break;
                }
                if (b.length() > 0) {
                    b.append(' ');
                }
                b.append(n.toString());

                String word = b.toString();
                if (words.contains(word)) {
                    TreeMatch dynMatch = m.branch();
                    word = options.getDynamicVocabularySubstitution(this.getName(), word);
                    dynMatch.appendText(word);
                    // if (isPublic() || options.includePrivateRulesInParseTree)
                    // m.setValue(new StringValue(word));

                    l.add(dynMatch);
                    /* +++ */
                    l
                            .addAll(dynMatch
                                    .createSparseBranches(grammar, options, false, this));
                }
            }
        }

        if ((words == null) || !options.dynamicVocabularyReplacesOriginalContent) {
            Collection<TreeMatch> results
                    = this.expansion.match(match, grammar, options);
            l.addAll(results);
        }

        return l;
    }

    boolean collectRules(Collection<Rule> s, ParseOptions options) {

        return this.expansion.collectRules(s, options);
    }

    boolean collectWords(Collection<String> s, ParseOptions options) {

        boolean changed = false;
        if (options != null) {
            Collection<String> entry
                    = options.getDynamicVocabularyKeys(this.getName());
            if (entry != null) {
                changed = changed || s.addAll(entry);

                if (options.dynamicVocabularyReplacesOriginalContent) {
                    return changed;
                }
            }
        }

        if (this.expansion.collectWords(s, options)) {
            changed = true;
        }

        return changed;
    }

    boolean precompileTokens(Object id) {

        if (this.getGrammar() == null) {
            throw new IllegalStateException(this.getGlobalName()
                    + " is not part of a grammar");
        }

        boolean changed = this.expansion.computePossibleTokens(id);
        this.compilationStable = !changed;
        return changed;
    }

    Set<Token<?>> getPossibleTokens() {

        return this.expansion.getPossibleTokens();
    }

    boolean isDirty() {

        return this.expansion.isDirty() || !this.compilationStable;
    }

    void export(PrintWriter w, Grammar.Format format) {

        switch (format) {
            case SRGF:
                if (this.isPublic()) {
                    w.print("public ");
                }
                if (this.isClass()) {
                    w.print("class ");
                }
                w.print("$" + this.getName() + " = ");
                this.expansion.export(w, format);
                w.println(";");
                break;

            case GRXML:
                com.clt.xml.XMLWriter xml = (com.clt.xml.XMLWriter) w;
                xml.openElement("rule", new String[]{"id", "scope"}, new String[]{
                    this.getName(),
                    this.isPublic() ? "public" : "private"});
                this.expansion.export(xml, format);
                xml.closeElement("rule");
                break;

            case JSGF:
            case JSGFwithGarbage:
// TIMO: maybe better handled in Grammar.exportImpl and with slightly different if-expression (this.getRoot().equals(r.getName())); or maybe not.
/*                if (this.isPublic()) {
                    w.print("public ");
                } */
            // fall through

            case TEMIC:
            case LH:
            case VOCON:
            case VOCON_G:
                w.print("<" + this.getNormalizedRuleName(format) + ">");
                w.print(((format == Grammar.Format.LH) || (format == Grammar.Format.VOCON) || (format == Grammar.Format.VOCON_G))
                        ? " : "
                        : " = ");
                if (format == Grammar.Format.JSGFwithGarbage && getGrammar().getRoot().equals(getName())) {
                    w.print("( ");
                }
                this.expansion.export(w, format);
                if (format == Grammar.Format.JSGFwithGarbage && getGrammar().getRoot().equals(getName())) {
                    w.println(") <"+Grammar.GARBAGE+"> ");
                }
                w.println(";");
                break;
            case NGSL:
                w.print(this.getName().toUpperCase());
                w.print(" ");
                this.expansion.export(w, format);
                w.println();
                break;
            case EXTLAT:
                w.println("# name");
                w.println("SUBLAT=" + this.getName());
                new WordGraph(this.getExpansion(), false, null).print(w);
                break;
            default:
                throw new IllegalArgumentException("Unknown export format");
        }
    }

    @Override
    public String toString() {

        return this.getName();
    }

    // RuleTokens are only used to mark rules. They never match any input.
    static class RuleToken
            extends Token<Rule> {

        public RuleToken(Rule r) {

            super(r);
        }

        @Override
        boolean match(Input input) {

            return false;
        }

        @Override
        public String toString() {

            return "$" + this.getSource().getGlobalName();
        }
    }

    static class DynVocItem
            extends Item {

        Collection<Input.Match> matches;

        public DynVocItem(Collection<Input.Match> matches) {

            super(1, 1);
            this.matches = matches;
        }

        private DynVocItem(DynVocItem item) {

            super(item);
            this.matches = item.matches;
        }

        @Override
        public Item copy() {

            return new DynVocItem(this);
        }

        @Override
        protected List<Parse> shift_(Parse parse) {

            this.setDone(true);
            List<Parse> continuations = new ArrayList<Parse>(this.matches.size());
            for (Iterator<Input.Match> it = this.matches.iterator(); it.hasNext();) {
                Input.Match m = it.next();
                Parse p = it.hasNext() ? parse.branch() : parse;
                Word[] words = new Word[m.numWords()];
                long start = 0;
                long end = 0;
                for (int i = 0; i < m.numWords(); i++) {
                    words[i] = p.getInput().removeFirst();
                    if (i == 0) {
                        start = Parse.getStart(words[i]);
                    }
                    if (i == m.numWords() - 1) {
                        end = Parse.getEnd(words[i]);
                    }

                }
                p.addTerminals(words);
                p.appendText(m.getString());
                p.addInterval(start, end);
                // ???
                // p.setValue(new StringValue(m.getString()));
                continuations.add(p);
            }

            return continuations;
        }
    }
}
