/*
 * @(#)Grammar.java
 * Created on Tue Oct 08 2002
 *
 * Copyright (c) 2002 CLT Sprachtechnologie GmbH.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CLT Sprachtechnologie GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CLT Sprachtechnologie GmbH.
 */
package com.clt.srgf;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.swing.tree.TreeNode;

import com.clt.script.Script;
import com.clt.script.exp.Value;
import com.clt.util.NamedEntity;

/**
 * A context free grammar parser.
 * <p>
 * The Grammar class can read a SRGF (W3C Speech Recognition Grammar Format)
 * representation with semantic tags from a string or file. The resulting
 * grammar object can be used to parse input (from a string or from an array of
 * {@link Word}s) and return either the parse tree of the best parse or the
 * result of an interpretation of the semantic tags in the best parse.
 * </p>
 * <p>
 * A typical usage looks like this, assuming that the file
 * <code>grammar.jsgf</code> contains the specification of a grammar with a rule
 * <code>main</code><br>
 *
 * <pre>
 * <code>
 * Grammar g = Grammar.create(new File(&quot;grammar.jsgf&quot;));
 * Value v = g.match(&quot;This is a test sentence&quot;, &quot;main&quot;);
 * ParseNode tree_root = g.getBestParseTree(&quot;This is a test sentence&quot;, &quot;main&quot;);
 * </code>
 * </pre>
 *
 * @author Daniel Bobbert
 * @version 1.0
 */
public class Grammar implements NamedEntity {

    /**
     * The special rule NULL matches an empty sequence. Thus,
     * <code>($a | $NULL)</code> is equivalent to <code>[$a]</code>
     */
    public static final String NULL = "NULL";

    /**
     * The special rule VOID never matches This is useful to temporarily
     * deactivate certain productions
     */
    public static final String VOID = "VOID";

    /**
     * The special rule GARBAGE matches exactly one arbitrary word. Garbage
     * models are needed for partial parses.
     */
    public static final String GARBAGE = "GARBAGE";

    public enum Format {
        /**
         * SRGF (W3C Speech Recognition Grammar Format - Text)
         */
        SRGF("W3C Speech Recognition Grammar Format - Text"),
        /**
         * GrXML (W3C Speech Recognition Grammar Format - XML)
         */
        GRXML("W3C Speech Recognition Grammar Format - XML"),
        /**
         * JSGF (Java Speech Grammar Format)
         */
        JSGF("Java Speech Grammar Format"),
        /**
         * Temic StarRec grammar format
         */
        TEMIC("Temic StarRec grammar format"),
        /**
         * L&amp;H ASR 1600 grammar format
         */
        LH("L&H ASR 1600 grammar format"),
        /**
         * Nuance GSL grammar format
         */
        NGSL("Nuance GSL grammar format"),
        /**
         * SRNadia extended lattice format
         */
        EXTLAT("SRNadia extended lattice format"),
        /**
         * ScanSoft Vocon 3200 grammar format
         */
        VOCON("ScanSoft Vocon 3200 grammar format"),
        /**
         * ScanSoft Vocon 3200 grammar format
         */
        VOCON_G("ScanSoft Vocon 3200 grammar format with global rule names");

        private String description;

        private Format(String description) {

            this.description = description;
        }

        public String getDescription() {

            return this.description;
        }
    }

    private GrammarContext context = null;
    private String name = null;
    private String root = null;
    private Map<String, Rule> defaultRules;
    private Map<String, Rule> rules;
    private Collection<Import> imports;
    private boolean compiled = false;

    private Map<String, String> features;
    private Map<String, String> metadata;
    private List<String> lexica;
    private String tagFormat = "semantics/1.0";

    private Script script;

    /**
     * Default constructor. Construct a new grammar with default rules NULL,
     * GARBAGE and VOID
     */
    public Grammar() {

        this(null);
    }

    public Grammar(String name) {

        this.name = name;
        this.rules = new HashMap<String, Rule>();
        this.defaultRules = new HashMap<String, Rule>();
        this.features = new HashMap<String, String>();
        this.metadata = new HashMap<String, String>();
        this.lexica = new ArrayList<String>();
        this.script = new Script();
        this.imports = new LinkedHashSet<Import>();

        // NULL = always recognized. "Empty sequence"
        this.addDefaultRule(new Rule(false, false, Grammar.NULL, new Sequence()) {

            @Override
            public String getGlobalName() {

                return this.getName();
            }

            @Override
            void export(PrintWriter w, Format format) {

                switch (format) {
                    case SRGF:
                    case TEMIC:
                    case JSGF:
                    case GRXML:
                    case VOCON:
                    case VOCON_G:
                        // do nothing. <NULL> is predefined
                        break;
                    case LH:
                        w.println("<NULL>: () ;");
                        break;
                    case NGSL:
                        w.println("NULL ()");
                        break;
                    case EXTLAT:
                        w.println("# name");
                        w.println("SUBLAT=NULL");
                        w.println("SUBLATTYPE=VP-WordClass");
                        w.println("# number of nodes and links");
                        w.println("N=2 L=1");
                        w.println("# nodes");
                        w.println("I=0 W=!NULL");
                        w.println("I=1 W=!NULL");
                        w.println("# links");
                        w.println("J=0 S=0 E=1 l=0.00 C=0.00");
                        break;
                    default:
                        throw new IllegalArgumentException(
                                "Unknown export format");
                }
            }
        });

        // VOID = never recognized. "Empty alternative"
        this
                .addDefaultRule(new Rule(false, false, Grammar.VOID, new Alternatives()) {

                    @Override
                    public String getGlobalName() {

                        return this.getName();
                    }

                    @Override
                    public boolean isVoid() {

                        return true;
                    }

                    void export(PrintWriter w, Grammar.Format format) {

                        switch (format) {
                            case SRGF:
                            case TEMIC:
                            case JSGF:
                            case GRXML:
                            case VOCON:
                            case VOCON_G:
                                // do nothing. <VOID> is predefined
                                break;
                            case LH:
                                w.println("<VOID>: ;");
                                break;
                            case NGSL:
                                w.println("VOID []");
                                break;
                            case EXTLAT:
                                w.println("# name");
                                w.println("SUBLAT=VOID");
                                w.println("SUBLATTYPE=VP-WordClass");
                                w.println("# number of nodes and links");
                                w.println("N=3 L=2");
                                w.println("# nodes");
                                w.println("I=0 W=!NULL");
                                w.println("I=1 W=<nonsp>");
                                w.println("I=2 W=!NULL");
                                w.println("# links");
                                w.println("J=0 S=0 E=1 l=-1E10 C=0.00");
                                w.println("J=0 S=1 E=2 l=0.00 C=0.00");
                                break;
                            default:
                                throw new IllegalArgumentException(
                                        "Unknown export format");
                        }
                    }
                });

        // $Garbage is a special rule that matches any token or token sequence
        // Priorities are used to prefer parses without garbage
        this.addDefaultRule(new GarbageRule());
    }

    // This method shouldn't be called directly. Use GrammarContext.add() instead.
    void setContext(GrammarContext context) {

        if (context != this.context) {
            if (this.context != null) {
                GrammarContext oldContext = this.context;
                this.context = null;
                oldContext.remove(this);
            }
            this.context = context;
            this.compiled = false;
        }
    }

    private void addDefaultRule(Rule r) {

        r.setGrammar(this);
        this.defaultRules.put(r.getName(), r);
        r.precompileTokens(new Object());
    }

    public void setName(String name) {

        this.name = name;
    }

    /**
     * Return the name of this grammar.
     */
    public final String getName() {

        return this.name;
    }

    public String getLanguage() {

        return this.getProperty("language");
    }

    public void setLanguage(String language) {

        this.setProperty("language", language);
    }

    public void setRoot(String root) {

        this.root = root;
    }

    public String getRoot() {

        return this.root;
    }

    public void addImport(Import importTarget) {

        this.imports.add(importTarget);
    }

    public void setTagFormat(String tagFormat) {

        this.tagFormat = tagFormat;
    }

    public String getTagFormat() {

        return this.tagFormat;
    }

    public void setProperty(String key, String value) {

        if (key.equals("grammar")) {
            this.setName(value);
        } else {
            this.features.put(key, value);
        }
    }

    public String getProperty(String key) {

        return this.features.get(key);
    }

    public void setMetaData(String key, String value) {

        this.metadata.put(key, value);
    }

    public String getMetaData(String key) {

        return this.metadata.get(key);
    }

    public void addLexicon(String lexicon) {

        this.lexica.add(lexicon);
    }

    public List<String> getLexica() {

        return Collections.unmodifiableList(this.lexica);
    }

    public Script getScript() {

        return this.script;
    }

    /**
     * Check that all rules, especially the tags contained in rule expansions,
     * are valid.
     */
    public Collection<Exception> check()
            throws SemanticException {

        return this.check(false);
    }

    public Collection<Exception> check(boolean forceRoot)
            throws SemanticException {

        Collection<Exception> warnings = new ArrayList<Exception>();
        this.compile();
        for (Rule r : this.getRules()) {
            r.check(warnings);
        }

        if (this.getRoot() != null) {
            if (this.getRule(this.getRoot()) == null) {
                throw new SemanticException(this, "Root rule '" + this.getRoot()
                        + "' is not defined.");
            }
        } else if (forceRoot) {
            throw new SemanticException(this, "You must define a root rule.");
        }

        return warnings;
    }

    /**
     * Return an optimized copy of this grammar
     */
    @SuppressWarnings("unused")
    private Grammar optimize(boolean removeTags) {

        this.check();

        Grammar g = new Grammar();

        for (String key : this.features.keySet()) {
            g.setProperty(key, this.getProperty(key));
        }

        for (String key : this.metadata.keySet()) {
            g.setMetaData(key, this.getMetaData(key));
        }

        for (Import imp : this.imports) {
            g.addImport(imp);
        }

        g.setName(this.getName());
        g.setRoot(this.getRoot());

        for (Rule r : this.getRules()) {
            g.add(r.optimize(removeTags));
        }

        return g;
    }

    /**
     * Optimize this grammar by modifying its rules in place
     */
    public void optimizeInPlace(boolean removeTags) {

        this.check();

        /*
     * for (Rule r : new ArrayList<Rule>(getRules())) { Rule optimized =
     * r.optimize(removeTags); remove(r); add(optimized); }
     * 
     * if (context != null) { for (Grammar g : context.getGrammars()) g.compiled
     * = false; }
         */
    }

    public void removeUnusedRules(Rule[] entryPoints, ParseOptions options) {

        Set<Rule> rules = new HashSet<Rule>();
        for (int i = 0; i < entryPoints.length; i++) {
            rules.add(entryPoints[i]);
        }

        boolean changed = false;
        do {
            changed = false;
            for (Rule r : new HashSet<Rule>(rules)) {
                if (r.collectRules(rules, options)) {
                    changed = true;
                }
            }
        } while (changed);

        for (Iterator<Rule> it = this.rules.values().iterator(); it.hasNext();) {
            Rule r = it.next();
            // never remove classes
            if (!r.isClass() && !rules.contains(r)) {
                // do not remove rules that are marked as fillerRules
                boolean isFiller = false;
                if (options != null) {
                    if (options.fillerRules != null) {
                        for (int i = 0; (i < options.fillerRules.length) && !isFiller; i++) {
                            if (this.resolveRule(options.fillerRules[i]) == r) {
                                isFiller = true;
                            }
                        }
                    }
                }

                if (!isFiller) {
                    it.remove();
                }
            }
        }
    }

    public Rule[] getRecursiveRules() {

        Collection<Rule> rules = new LinkedList<Rule>();
        ParseOptions options = new ParseOptions();
        for (Rule r : this.getRules()) {
            Set<Rule> s = this.getUsedRules(r, options);
            if (s.contains(r)) {
                rules.add(r);
            }
        }
        return rules.toArray(new Rule[rules.size()]);
    }

    private Rule[] sortByDependencies(boolean onlyPublicRules)
            throws IllegalStateException {

        if (this.getRoot() == null) {
            throw new IllegalStateException("No root specified");
        }
        if (this.getRule(this.getRoot()) == null) {
            throw new IllegalStateException("Root rule \"" + this.getRoot()
                    + "\" not found.");
        }

        ParseOptions options = new ParseOptions();
        options.includePrivateRulesInParseTree = !onlyPublicRules;
        Map<Rule, Collection<Rule>> usedRulesMap
                = new HashMap<Rule, Collection<Rule>>();
        Set<Rule> usedRules
                = new HashSet<Rule>(this
                        .getUsedRules(this.getRule(this.getRoot()), options));
        if (usedRules.contains(this.getRule(this.getRoot()))) {
            throw new IllegalStateException("root rule is recursive.");
        }

        for (Rule r : usedRules) {
            Set<Rule> s = this.getUsedRules(r, options);
            if (r.isPublic() || options.includePrivateRulesInParseTree) {
                if (s.contains(r)) {
                    throw new IllegalStateException((r.isPublic() ? "Public rule "
                            : "Rule ")
                            + r.getName() + " is recursive.");
                } else {
                    usedRulesMap.put(r, s);
                }
            }
        }

        final Map<Rule, Integer> priority = new HashMap<Rule, Integer>();
        for (Rule r : usedRulesMap.keySet()) {
            priority.put(r, new Integer(0));
        }

        boolean changed;
        do {
            changed = false;

            for (Map.Entry<Rule, Integer> e : priority.entrySet()) {
                Rule rule = e.getKey();
                Integer p = e.getValue();
                for (Rule usedRule : usedRulesMap.get(rule)) {
                    Integer p2 = priority.get(usedRule);
                    if (p2 != null) {
                        if (p2.intValue() >= p.intValue()) {
                            p = new Integer(p2.intValue() + 1);
                            e.setValue(p);
                            changed = true;
                        }
                    }
                }
            }

        } while (changed);

        Collection<Rule> sorted = new TreeSet<Rule>(new Comparator<Rule>() {

            public int compare(Rule r1, Rule r2) {

                int result = priority.get(r1).intValue() - priority.get(r2).intValue();
                if (result == 0) {
                    return r1.getName().compareTo(r2.getName());
                } else {
                    return result;
                }
            }
        });
        sorted.addAll(priority.keySet());

        return sorted.toArray(new Rule[sorted.size()]);
    }

    /**
     * Return a set of the names of all local rules used (directly or
     * indirectly) by <code>r</code>. If <code>r/code> is <code>null</code> the
     * set of rules used by ALL rules in this grammar is returned. (This is
     * really only useful to determine the set of externally linked rules).
     */
    private Set<Rule> getUsedRules(Rule r, ParseOptions options) {

        Collection<Rule> s = new HashSet<Rule>();
        if (r != null) {
            s.add(r);
        } else {
            s.addAll(this.getRules());
        }
        Set<Rule> usedRules = new HashSet<Rule>();
        boolean changed = false;
        do {
            changed = false;
            for (Rule usedRule : s) {
                if (usedRule.collectRules(usedRules, options)) {
                    changed = true;
                }
            }
            if (changed) {
                s = new ArrayList<Rule>(usedRules);
            }
        } while (changed);
        return usedRules;
    }

    /**
     * Return a set of all terminal words in the grammar. Each element of the
     * set is a string.
     */
    public Set<String> getWords(ParseOptions options) {

        Set<String> s = new HashSet<String>();
        for (Rule r : this.getRules()) {
            r.collectWords(s, options);
        }
        return s;
    }

    /**
     * Return a set of all terminal words in the grammar reachable from the
     * root. Each element of the set is a string.
     */
    public Set<String> getUsedWords(ParseOptions options) {

        if (this.getRoot() == null) {
            return this.getWords(options);
        } else {
            Rule root = this.getRule(this.getRoot());
            Set<String> s = new HashSet<String>();
            root.collectWords(s, options);
            for (Rule rule : this.getUsedRules(root, options)) {
                rule.collectWords(s, options);
            }

            if (options != null) {
                if (options.fillerRules != null) {
                    for (int i = 0; i < options.fillerRules.length; i++) {
                        Rule r = this.resolveRule(options.fillerRules[i]);
                        if (r != null) {
                            r.collectWords(s, options);
                        }
                    }
                }
            }
            return s;
        }
    }

    /**
     * Add a rule to this grammar.
     *
     * @throws SemanticException if the grammar already contains a rule with the
     * same name
     * @see Rule
     */
    public void add(Rule r)
            throws SemanticException {

        if (this.getRule(r.getName()) != null) {
            throw new SemanticException(this,
                    "Duplicate definition of grammar rule '"
                    + r.getName() + "'");
        }
        this.rules.put(r.getName(), r);
        r.setGrammar(this);

        this.compiled = false;
    }

    /**
     * Remove a rule from this grammar. You cannot remove the builtin rules
     * NULL, VOID and GARBAGE.
     *
     * @see Rule
     */
    public void remove(Rule r)
            throws SemanticException {

        this.rules.remove(r.getName());
        r.setGrammar(null);

        this.compiled = false;
    }

    /**
     * Return the local {@link Rule} named <code>name</code> or
     * <code>null</code> if no such rule exists.
     */
    public Rule getRule(String name) {

        Rule r = this.rules.get(name);
        if (r == null) {
            r = this.defaultRules.get(name);
        }
        return r;
    }

    public Rule resolveRule(String name) {

        Rule r = this.getRule(name);

        if (r == null) {
            if (this.context != null) {
                r = this.context.resolve(this, name, this.imports);
            }
        }

        return r;
    }

    public boolean isSpecialRule(String name) {

        return this.defaultRules.containsKey(name);
    }

    /**
     * Return an iterator of all {@link Rule}s of this grammar.
     */
    public Collection<Rule> getRules() {

        return Collections.unmodifiableCollection(this.rules.values());
    }

    public Collection<Rule> getClasses() {

        Collection<Rule> classes = new ArrayList<Rule>();
        for (Rule r : this.getRules()) {
            if (r.isClass()) {
                classes.add(r);
            }
        }
        return classes;
    }

    public Collection<Rule> getImportedRules() {

        ParseOptions options = new ParseOptions();
        Set<Rule> rules = new HashSet<Rule>();
        rules.addAll(this.getRules());
        boolean changed = false;
        do {
            changed = false;
            for (Rule usedRule : new ArrayList<Rule>(rules)) {
                if (usedRule.getGrammar() == this) {
                    if (usedRule.collectRules(rules, options)) {
                        changed = true;
                    }
                }
            }
        } while (changed);

        rules.removeAll(this.getRules());
        rules.removeAll(this.defaultRules.values());

        return rules;
    }

    public Collection<Grammar> getExternalGrammars() {

        Set<Rule> usedRules = new HashSet<Rule>();
        ParseOptions options = new ParseOptions();

        usedRules.addAll(this.getUsedRules(null, options));

        Set<Grammar> grammars = new HashSet<Grammar>();

        for (Rule r : usedRules) {
            if (r.getGrammar() == null) {
                throw new SemanticException(this, "Link to external rule " + r
                        + ", which does not belong to any grammar");
            } else if (r.getGrammar() != this) {
                grammars.add(r.getGrammar());
            }
        }

        return grammars;
    }

    /**
     * Compile this grammar. Compilation is necessary before using this grammar
     * to parse any input.
     */
    public void compile() {

        Object id = new Object();

        Collection<Rule> rules = new HashSet<Rule>(this.getRules());

        boolean changed = false;
        do {
            changed = false;
            for (Rule r : this.defaultRules.values()) {
                if (r.precompileTokens(id)) {
                    changed = true;
                }
            }

            for (Rule r : new HashSet<Rule>(rules)) {
                if (r.precompileTokens(id)) {
                    changed = true;
                }

                for (Token<?> t : r.getPossibleTokens()) {
                    if (t instanceof Rule.RuleToken) {
                        Rule usedRule = ((Rule.RuleToken) t).getSource();
                        if (rules.add(usedRule)) {
                            changed = true;
                        }
                    }
                }
            }

        } while (changed);

        this.compiled = true;
    }

    public void printPossibleTokens(PrintWriter w) {

        if (!this.compiled) {
            this.compile();
        }

        w.println("Possible first terminal symbols for rule...");
        for (Rule r : this.getRules()) {
            Set<Token<?>> ts = new TreeSet<Token<?>>(new Comparator<Token<?>>() {

                public int compare(Token<?> o1, Token<?> o2) {

                    return o1.toString().compareTo(o2.toString());
                }
            });
            ts.addAll(r.getPossibleTokens());

            w.print(r + " : ");
            for (Iterator<Token<?>> tokens = ts.iterator(); tokens.hasNext();) {
                w.print(tokens.next());
                if (tokens.hasNext()) {
                    w.print(", ");
                }
            }
            w.println();
        }
        w.flush();
    }

    public Iterator<String> generate(GenerationOptions options) {

        Rule root = this.getRule(this.getRoot());
        final Iterator<StringBuilder> it
                = root.generate(new StringBuilder(), options);
        return new Iterator<String>() {

            public boolean hasNext() {

                return it.hasNext();
            }

            public String next() {

                return it.next().toString();
            }

            public void remove() {

                it.remove();
            }
        };
    }

    /**
     * Use this grammar to parse the given input, using the root
     * <code>rulename</code>.
     *
     * @return The result of the evaluation of the grammars semantic tags
     */
    public Value match(String input, String rulename)
            throws SemanticException {

        return this.match(input, rulename, null);
    }

    /**
     * Use this grammar to parse the given input, using the root
     * <code>rulename</code>.
     *
     * @return The result of the evaluation of the grammars semantic tags
     */
    public Value match(String input, String rulename, ParseOptions optns)
            throws SemanticException {

        ParseOptions options;
        if (optns == null) {
            options = new ParseOptions();
        } else {
            options = optns.clone();
        }
        options.maxParses = ParseOptions.BEST_PARSE;
        options.buildParseTree = false;
        Parse[] p = this.parse(new Input(input), rulename, options);
        if (p.length == 0) {
            return null;
        } else {
            return p[0].getResult();
        }
    }

    /**
     * Use this grammar to parse the given input, using the root
     * <code>rulename</code>.
     *
     * @return The result of the evaluation of the grammars semantic tags
     */
    public Value match(Word[] input, String rulename)
            throws SemanticException {

        return this.match(input, rulename, null);
    }

    public Value match(Word[] input, String rulename, ParseOptions options)
            throws SemanticException {

        if (options == null) {
            options = new ParseOptions();
        } else {
            options = options.clone();
        }
        options.maxParses = ParseOptions.BEST_PARSE;
        options.buildParseTree = false;
        Parse[] p = this.parse(new Input(input), rulename, options);
        if (p.length == 0) {
            return null;
        } else {
            return p[0].getResult();
        }
    }

    /**
     * Use this grammar to match the given parse tree, using the root
     * <code>rulename</code>.
     *
     * @return The result of the evaluation of the grammars semantic tags
     * @see ParseOptions
     */
    public Value match(TreeNode tree, String rulename, ParseOptions options) throws SemanticException {
        if (!this.compiled) {
            this.compile();
        }

        if (rulename == null) {
            rulename = this.getRoot();
        }
        if (rulename == null) {
            throw new IllegalArgumentException("You must specify a root");
        }

        Rule start = this.getRule(rulename);
        if (start == null) {
            throw new SemanticException(this, "Could not find definition of rule '"
                    + rulename
                    + "'");
        }

        Collection<TreeMatch> results = start.match(tree, this, options, false);
        if (results.isEmpty()) {
            return null;
        }

        if (results.size() == 1) {
            return results.iterator().next().getResult();
        } else {
            // warn, if results differ
            Iterator<TreeMatch> it = results.iterator();
            Value result = it.next().getResult();
            while (it.hasNext()) {
                if (!result.equals(it.next().getResult())) {
                    System.err.println("WARNING: Grammar is ambiguous.");
                    break;
                }
            }
            return result;
        }
    }

    /**
     * Use this grammar to parse the given input, using the root
     * <code>rulename</code>.
     *
     * @return An array containing the root nodes of the
     * {@link ParseOptions#maxParses} best parse trees
     * @see ParseOptions
     */
    public ParseNode[] getParseTrees(String input, String rulename,
            ParseOptions options)
            throws SemanticException {

        return this.getParseTrees(new Input(input), rulename, options);
    }

    /**
     * Use this grammar to parse the given input, using the root
     * <code>rulename</code>.
     *
     * @return An array containing the root nodes of the
     * {@link ParseOptions#maxParses} best parse trees
     * @see ParseOptions
     */
    public ParseNode[] getParseTrees(Word[] input, String rulename,
            ParseOptions options)
            throws SemanticException {

        return this.getParseTrees(new Input(input), rulename, options);
    }

    private ParseNode[] getParseTrees(Input input, String rulename,
            ParseOptions options)
            throws SemanticException {

        Parse[] p = this.parse(input, rulename, options);
        ParseNode[] result = new ParseNode[p.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = p[i].getParseTree();
        }
        return result;
    }

    public Parse[] parse(Input in, String rulename, final ParseOptions options) throws SemanticException {
        if (!this.compiled) {
            this.compile();
        }

        if (rulename == null) {
            rulename = this.getRoot();
        }
        if (rulename == null) {
            throw new IllegalArgumentException("You must specify a root");
        }

        Rule start = this.getRule(rulename);
        if (start == null) {
            throw new SemanticException(this, "Could not find definition of rule '" + rulename + "'");
        }

        Parse mainParse = new Parse(this, start, in, options);

        Map<Integer, PenaltyList> penaltyLists = new HashMap<Integer, PenaltyList>();
        Integer lowestPenalty = new Integer(mainParse.getPenalty());
        PenaltyList p0 = new PenaltyList();
        p0.add(mainParse);
        penaltyLists.put(lowestPenalty, p0);

        List<Parse> sparseParses = new LinkedList<Parse>();

        if (options.fillerRules != null) {
            for (int i = 0; i < options.fillerRules.length; i++) {
                Rule r = this.resolveRule(options.fillerRules[i]);
                if (r != null) {
                    Parse branch = mainParse.branch();
                    branch.push(RuleItem.automaticRule(r));
                    sparseParses.add(branch);
                }
            }
        }

        Parse mp = mainParse;
        if (options.allowSparseParses) {
            while (mp.hasMoreInput()) {
                mp = mp.branch();
                mp.skipWord();
                sparseParses.add(mp);

                if (options.fillerRules != null) {
                    for (int i = 0; i < options.fillerRules.length; i++) {
                        Rule r = this.getRule(options.fillerRules[i]);
                        if (r != null) {
                            Parse branch = mp.branch();
                            branch.push(RuleItem.automaticRule(r));
                            sparseParses.add(branch);
                        }
                    }
                }
            }
        }

        for (Parse p : sparseParses) {
            Integer penalty = new Integer(p.getPenalty());
            PenaltyList l = penaltyLists.get(penalty);
            if (l == null) {
                l = new PenaltyList();
                penaltyLists.put(penalty, l);
            }
            l.add(p);
        }

        final Comparator<Parse> penaltyComparator = new Comparator<Parse>() {

            public int compare(Parse p1, Parse p2) {

                // sort lower penalty or greater confidence first
                int result = p1.getPenalty() - p2.getPenalty();
                if (result == 0) {
                    if (p1.getProbability() > p2.getProbability()) {
                        result = -1;
                    } else if (p1.getProbability() < p2.getProbability()) {
                        result = 1;
                    } else {
                        result = 0;
                    }
                }
                return result;
            }
        };

        Collection<Parse> results = new TreeSet<Parse>(new Comparator<Parse>() {

            public int compare(Parse p1, Parse p2) {

                int result = penaltyComparator.compare(p1, p2);
                if (result == 0) {
                    return System.identityHashCode(p1) - System.identityHashCode(p2);
                } else {
                    return result;
                }
            }
        }) {

            Map<ParseNode, Parse> parseTrees = new HashMap<ParseNode, Parse>();

            public boolean add(Parse parse) {

                parse.postProcess();

                if (options.buildParseTree && options.eliminateDuplicateParseTrees) {
                    ParseNode t = parse.getParseTree();
                    if (this.parseTrees.containsKey(t)) {
                        Parse p = this.parseTrees.get(t);
                        if (penaltyComparator.compare(parse, p) < 0) { // o is
                            // better
                            // then p
                            this.remove(p);
                            this.parseTrees.put(t, parse);
                            return super.add(parse);
                        } else {
                            return false;
                        }
                    } else {
                        this.parseTrees.put(t, parse);
                        return super.add(parse);
                    }
                } else {
                    return super.add(parse);
                }
            }
        };

        int maxAllowedPenalty = Integer.MAX_VALUE; // will eventually be
        // reduced if looking for
        // BEST_PARSES

        for (;;) {
            PenaltyList bestParses = penaltyLists.get(lowestPenalty);

            Parse p = bestParses.removeFirst();

            if (p.success()) {
                // really done, so add to list of successful parses
                if (options.maxParses == ParseOptions.BEST_PARSES) {
                    if (results.isEmpty()) {
                        results.add(p);
                        // The first match. At this point we can discard all
                        // parses that already have a higher penalty.
                        for (Iterator<Integer> it = penaltyLists.keySet().iterator(); it
                                .hasNext();) {
                            Integer penalty = it.next();
                            if (penalty.intValue() > p.getPenalty()) {
                                PenaltyList parses = penaltyLists.get(penalty);
                                while (!parses.isEmpty()) {
                                    parses.removeFirst().dispose();
                                }
                                it.remove();
                            }
                        }

                        // We also note, that future parses may not exceed this
                        // penalty.
                        // This and the above optimization will reduce memory
                        // comsumption considerably.
                        maxAllowedPenalty = p.getPenalty();
                    } else {
                        // with maxAllowedPenalty set, only legal results can
                        // get here
                        results.add(p);
                    }
                } else {
                    results.add(p);
                    if (results.size() >= options.maxParses) {
                        return results.toArray(new Parse[results.size()]);
                    }
                }
            } else if (!p.failure()) {
                Collection<Parse> continuations = p.shift();

                for (Iterator<Parse> it = continuations.iterator(); it.hasNext();) {
                    p = it.next();

                    Integer penalty = new Integer(p.getPenalty());
                    if (penalty.intValue() <= maxAllowedPenalty) {
                        PenaltyList s = penaltyLists.get(penalty);
                        if (s == null) {
                            s = new PenaltyList();
                            penaltyLists.put(penalty, s);
                        }

                        s.add(p);
                    }
                }
            }

            if (bestParses.isEmpty()) {
                penaltyLists.remove(lowestPenalty);
                if (penaltyLists.isEmpty()) {
                    return results.toArray(new Parse[results.size()]);
                } else {
                    // suche den neuen kleinsten Wert
                    lowestPenalty = new Integer(Integer.MAX_VALUE);
                    for (Iterator<Integer> it = penaltyLists.keySet().iterator(); it
                            .hasNext();) {
                        Integer penalty = it.next();
                        if (penalty.intValue() < lowestPenalty.intValue()) {
                            lowestPenalty = penalty;
                        }
                    }
                    // System.out.println(lowestPenalty);
                }
            }
            /*
       * int totalParses = 0; for (Iterator it =
       * penaltyLists.values().iterator(); it.hasNext(); ) totalParses +=
       * ((Collection) it.next()).size(); System.out.println("Total parses:
       * " + totalParses + ". With minimum penalty " + lowestPenalty + ": " +
       * ((Collection) penaltyLists.get(lowestPenalty)).size());
             */
        }
    }

    /**
     * Write out the grammar in the specified file format.
     *
     * @see Format#NGSL
     * @see Format#LH
     * @see Format#TEMIC
     * @see Format#JSGF
     * @see Format#SRGF
     */
    public void export(OutputStream out, Grammar.Format format) throws IOException {
        this.export(out, format, null);
    }

    public void export(OutputStream out, Grammar.Format format,
            Tokenizer tokenizer)
            throws IOException {

        PrintWriter w;
        switch (format) {
            case SRGF:
                w = new PrintWriter(new BufferedWriter(new OutputStreamWriter(out, "ISO-8859-1")));
                w.println("#ABNF 1.0 ISO-8859-1;");
                w.println();
                break;
            case GRXML:
                w = new com.clt.xml.XMLWriter(out, this.getProperty("encoding"));
                break;
            case TEMIC:
            case JSGF:
                w = new PrintWriter(new BufferedWriter(new OutputStreamWriter(out, "ISO-8859-1")));
                w.println("#JSGF V1.0 ISO-8859-1;");
                w.println();
                break;
            case VOCON:
            case VOCON_G:
                w = new PrintWriter(new BufferedWriter(new OutputStreamWriter(out, "UTF-16")));
                break;
            case LH:
            case NGSL:
                w = new PrintWriter(new BufferedWriter(new OutputStreamWriter(out, "ISO-8859-1")));
                w.println(";GSL2.0");
                break;
            case EXTLAT:
                w = new PrintWriter(new BufferedWriter(new OutputStreamWriter(out, "ISO-8859-1")));
                break;
            default:
                throw new IllegalArgumentException("Unknown export format");
        }

        this.exportImpl(w, format, tokenizer);
    }

    /**
     * Write out the grammar in the specified file format.
     *
     * @see Format#NGSL
     * @see Format#LH
     * @see Format#TEMIC
     * @see Format#JSGF
     * @see Format#SRGF
     */
    public void export(Writer out, Grammar.Format format) {

        this.export(out, format, null);
    }

    public void export(Writer out, Grammar.Format format, Tokenizer tokenizer) {

        PrintWriter w;
        switch (format) {
            case SRGF:
                if (out instanceof PrintWriter) {
                    w = (PrintWriter) out;
                } else {
                    w = new PrintWriter(new BufferedWriter(out));
                }
                w.println("#ABNF 1.0;");
                w.println();
                break;
            case GRXML:
                if (out instanceof com.clt.xml.XMLWriter) {
                    w = (com.clt.xml.XMLWriter) out;
                } else {
                    w = new com.clt.xml.XMLWriter(out);
                }
                break;
            case TEMIC:
            case JSGF:
                if (out instanceof PrintWriter) {
                    w = (PrintWriter) out;
                } else {
                    w = new PrintWriter(new BufferedWriter(out));
                }
                w.println("#JSGF V1.0;");
                w.println();
                break;
            case LH:
            case NGSL:
            case EXTLAT:
            case VOCON:
            case VOCON_G:
                if (out instanceof PrintWriter) {
                    w = (PrintWriter) out;
                } else {
                    w = new PrintWriter(new BufferedWriter(out));
                }
                if ((format == Format.VOCON) || (format == Format.VOCON_G)) {
                    w.println("#BNF+EM V1.0;");
                } else if (format == Format.NGSL) {
                    w.println(";GSL2.0");
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown export format");
        }

        this.exportImpl(w, format, tokenizer);
    }

    /**
     * Write out the grammar in the specified file format.
     *
     * @see Format#NGSL
     * @see Format#LH
     * @see Format#TEMIC
     * @see Format#JSGF
     * @see Format#SRGF
     */
    private void exportImpl(PrintWriter w, Grammar.Format format, Tokenizer tokenizer) {
        Collection<Rule> allRules = new ArrayList<Rule>(this.rules.size() + this.defaultRules.size());
        allRules.addAll(this.rules.values());
        allRules.addAll(this.defaultRules.values());

        switch (format) {
            case SRGF:
                if (this.getName() != null) {
                    w.println("grammar " + this.getName() + ";");
                }

                for (String key : this.features.keySet()) {
                    String value = this.getProperty(key);
                    if (value.indexOf(' ') >= 0) {
                        w.println(key + " \"" + value + "\";");
                    } else {
                        w.println(key + " " + value + ";");
                    }
                }

                for (String key : this.metadata.keySet()) {
                    w.println("meta \"" + key + "\" is \"" + this.getMetaData(key)
                            + "\";");
                }

                if (this.getTagFormat() != null) {
                    w.println("tag-format <" + this.getTagFormat() + ">;");
                }
                for (String lexicon : this.getLexica()) {
                    w.println("lexicon <" + lexicon + ">;");
                }

                for (Import s : this.imports) {
                    w.println("import " + s.toString() + ";");
                }

                if (this.getRoot() != null) {
                    w.println("root $" + this.getRoot() + ";");
                }

                w.println();
                for (Rule rule : allRules) {
                    rule.export(w, format);
                    w.println();
                }
                break;
            case GRXML:
                Map<String, String> props = new HashMap<String, String>();
                props.putAll(this.features);
                if (this.getTagFormat() != null) {
                    props.put("tag-format", this.getTagFormat());
                }
                props.put("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
                props.put("xsi:schemaLocation", "http://www.w3.org/2001/06/grammar http://www.w3.org/TR/speech-grammar/grammar.xsd");
                props.put("xmlns", "http://www.w3.org/2001/06/grammar");
                props.remove("encoding");
                if (this.getRoot() != null) {
                    props.put("root", this.getRoot());
                }
                if (props.containsKey("base")) {
                    props.put("xml:base", this.getProperty("base"));
                    props.remove("base");
                }
                String[] keys = new String[props.size()];
                String[] values = new String[props.size()];
                int i = 0;
                for (Iterator<String> it = props.keySet().iterator(); it.hasNext(); i++) {
                    keys[i] = it.next();
                    values[i] = this.getProperty(keys[i]);
                }
                com.clt.xml.XMLWriter xml = (com.clt.xml.XMLWriter) w;
                xml
                        .println("<!DOCTYPE grammar PUBLIC \"-//W3C//DTD GRAMMAR 1.0//EN\" \"http://www.w3.org/TR/speech-grammar/grammar.dtd\">");
                xml.openElement("grammar", keys, values);

                for (String lexicon : this.getLexica()) {
                    xml.printElement("lexicon", new String[]{"uri"},
                            new String[]{lexicon});
                }

                for (String key : this.metadata.keySet()) {
                    xml.printElement("meta", new String[]{"name", "content"},
                            new String[]{
                                key, this.getMetaData(key)});
                }

                for (Rule r : allRules) {
                    r.export(w, format);
                }

                xml.closeElement("grammar");
                break;
            case TEMIC:
            case JSGF:
                w.println("grammar " + this.getName() + ";");
                w.println();
                for (Rule r : allRules) {
                    r.export(w, format);
                    w.println();
                }
                break;
            case LH:
            case VOCON:
            case VOCON_G:
                w.println("!grammar \"" + this.getName() + "\";");
                String language = this.getLanguage();
                if (language == null) {
                    if (format == Format.LH) {
                        language = "Deutsch";
                    } else {
                        language = "German";
                    }
                } else {
                    if (format == Format.LH) {
                        if (language.equals("German")) {
                            language = "Deutsch";
                        }
                    } else {
                        if (language.equals("Deutsch")) {
                            language = "German";
                        }
                    }
                }

                w.println("!language \"" + language + "\";");
                w.println();

                Rule rootRule = this.getRule(this.getRoot());

                if (format == Format.VOCON_G) {
                    // no !start symbol for VOCON_G
                    Collection<Rule> externalRules = this.getImportedRules();
                    for (Rule r : externalRules) {
                        w.println("!import <" + r.getNormalizedRuleName(format) + ">;");
                    }

                    Rule root = this.getRule(this.getRoot());
                    for (Rule r : allRules) {
                        if (r.isPublic()
                                || ((r == root) && !this.defaultRules.containsValue(r))) {
                            w.println("!export <" + r.getNormalizedRuleName(format) + ">;");
                        }
                    }
                } else {
                    if (this.getRoot() != null) {
                        if (format == Format.VOCON) {
                            if (!this.getRoot().equals(Grammar.VOID)) {
                                w.println("!start <" + rootRule.getNormalizedRuleName(format)
                                        + ">;");
                            }
                            for (Rule r : allRules) {
                                if (r.isPublic()) {
                                    w.println("!export <" + r.getNormalizedRuleName(format)
                                            + ">;");
                                }
                            }
                        } else {
                            w.println("!export <" + rootRule.getNormalizedRuleName(format)
                                    + ">;");
                        }
                    } else {
                        for (Rule r : allRules) {
                            if (r.isPublic()) {
                                w.println("!export <" + r.getNormalizedRuleName(format) + ">;");
                            }
                        }
                    }
                }
                w.println();
                for (Rule r : allRules) {
                    if (r.isClass()) {
                        if ((format == Format.VOCON) || (format == Format.VOCON_G)) {
                            w.println("!modifiable <" + r.getNormalizedRuleName(format)
                                    + ">;");
                        } else {
                            w.println("!class <" + r.getNormalizedRuleName(format) + ">;");
                        }
                    }
                }
                w.println();

                for (Rule r : allRules) {
                    r.export(w, format);
                    w.println();
                }
                break;

            case NGSL:
                w.println(";GSL 2.0");
                w.println();
                w.println();
                for (Rule r : allRules) {
                    r.export(w, format);
                    w.println();
                }
                if (this.getRoot() != null) {
                    w.println(".SENTENCE " + this.getRoot().toUpperCase());
                } else {
                    w.println(".SENTENCE [");
                    for (Rule r : allRules) {
                        if (r.isPublic()) {
                            w.println("   " + r.getName());
                        }
                    }
                    w.println("]");
                    w.println();
                }
                break;

            case EXTLAT:
                boolean onlyPublicRules = true;
                Rule[] rules = this.sortByDependencies(onlyPublicRules);
                for (int n = 0; n < rules.length; n++) {
                    w.println("# name");
                    w.println("SUBLAT=" + rules[n].getName());
                    new WordGraph(rules[n].getExpansion(), onlyPublicRules, tokenizer)
                            .print(w);
                    w.println(".");
                    w.println();
                }
                w.println("# Root");
                new WordGraph(this.getRule(this.getRoot()).getExpansion(),
                        onlyPublicRules, tokenizer, true).print(w);
                break;

            default:
                throw new IllegalArgumentException("Unknown export format");
        }

        w.flush();
    }

    public String toString() {

        return this.toString(Format.SRGF);
    }

    public String toString(Format format) {

        StringWriter w = new StringWriter();
        this.export(w, format);
        return w.toString();
    }

    /**
     * Split a string into a list of {@link Word}s.
     *
     * @see Word
     */
    static final List<String> tokenize(String s) {

        List<String> words = new LinkedList<String>();
        StringTokenizer tokenizer = new StringTokenizer(s, " \t\n", false);

        while (tokenizer.hasMoreTokens()) {
            words.add(tokenizer.nextToken());
        }

        return words;
    }

    public static final Word[] splitWords(String s) {

        List<String> words = Grammar.tokenize(s);
        Word[] result = new Word[words.size()];
        int i = 0;
        for (final String word : words) {
            result[i++] = new Word() {

                public String getWord() {

                    return word;
                }

                public float getConfidence() {

                    return 1.0f;
                }
            };
        }
        return result;
    }

    /**
     * Substitute occurences of $$ in <code>filter</code> by <code>name</code>
     * The filter may contains the escape sequence "\$" to generate a $ in the
     * result.
     */
    public static String substituteName(String filter, String name) {

        StringBuilder b = new StringBuilder(filter.length());
        for (int i = 0; i < filter.length(); i++) {
            char c = filter.charAt(i);
            if (c == '\\') {
                if (i < filter.length() - 1) {
                    switch (filter.charAt(++i)) {
                        case '\\':
                            b.append('\\');
                            break;
                        case '\'':
                            b.append('\'');
                            break;
                        case '\"':
                            b.append('\"');
                            break;
                        case 'b':
                            b.append('\b');
                            break;
                        case 't':
                            b.append('\t');
                            break;
                        case 'n':
                            b.append('\n');
                            break;
                        case 'r':
                            b.append('\r');
                            break;
                        case 'f':
                            b.append('\f');
                            break;
                        case '$':
                            b.append('$');
                            break;
                        case 'u':
                            try {
                                b
                                        .append((char) Integer
                                                .parseInt(filter.substring(i + 1, i + 5)));
                                i += 4;
                            } catch (Exception exn) {
                                throw new IllegalArgumentException(
                                        "Illegal unicode escape sequence");
                            }
                            break;
                        default:
                            throw new IllegalArgumentException("Illegal escape sequence");
                    }
                } else {
                    throw new IllegalArgumentException("Illegal escape sequence");
                }
            } else if (c == '$') {
                if ((i < filter.length() - 1) && (filter.charAt(i + 1) == '$')) {
                    b.append(name);
                    i++;
                } else {
                    b.append("$");
                }
            } else {
                b.append(c);
            }
        }

        return b.toString();
    }

    /**
     * Create a grammar from a string using the default environment.
     */
    public static Grammar create(String s) throws Exception {
        return Grammar.create(s, new com.clt.script.DefaultEnvironment());
    }

    /**
     * Create a grammar from a string using the specified Environment. The
     * environment is used to resolve variables and function calls in semantic
     * tags
     */
    public static Grammar create(String s, com.clt.script.Environment env) throws Exception {
        return com.clt.script.parser.Parser.parseSRGF(s, env);
    }

    /**
     * Create a grammar from a Reader source using the default environment.
     */
    public static Grammar create(Reader r) throws Exception {
        return com.clt.script.parser.Parser.parseSRGF(r,
                new com.clt.script.DefaultEnvironment());
    }

    /**
     * Create a grammar from a Reader source using the given environment.
     */
    public static Grammar create(Reader r, com.clt.script.Environment env) throws Exception {
        return com.clt.script.parser.Parser.parseSRGF(r, env);
    }
    
    public static void main(String[] args) throws Exception, Exception {
        String gr = "#ABNF 1.0 ISO-8859-1;\n" +
"\n" +
"// Default grammar language is US English\n" +
"language en-US;\n" +
                "root <request>;\n"+
"\n" +
"// Single language attachment to tokens\n" +
"// Note that \"fr-CA\" (Canadian French) is applied to only\n" +
"//  the word \"oui\" because of precedence rules\n" +
"$yes = yes ;\n" +
"\n" +
"// Single language attachment to an expansion\n" +
"$people1 = (Michel Tremblay | André Roy);\n" +
"\n" +
"// Handling language-specific pronunciations of the same word\n" +
"// A capable speech recognizer will listen for Mexican Spanish and\n" +
"//   US English pronunciations.\n" +
"$people2 = John {name=\"john\"};\n" +
"\n" +
"/**\n" +
" * Multi-lingual input possible\n" +
" * @example may I speak to André Roy\n" +
" * @example may I speak to Jose\n" +
" */\n" +
"public $request = may I speak to ($people1 | $people2) {name=people1.name};";

        Grammar g = Grammar.create(gr);
        g.setName("foo");
        System.out.println(g);
        g.export(System.out, Grammar.Format.JSGF);
        
        Value v = g.match("May I speak to John", "request"); //May I speak to 
        System.out.println(v);
        System.out.println(v.getClass());
        
        
//        ParseNode tree_root = g.getBestParseTree("May I speak to John", "request");
//        System.out.println(tree_root);
    }
}
