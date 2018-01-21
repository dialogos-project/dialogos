package com.clt.srgf;

import java.util.Collection;
import java.util.Map;

/**
 * ParseOptions can be used to customize the set of {@link Parse}s that a
 * {@link Grammar} returns.
 *
 * @author Daniel Bobbert
 * @version 1.0
 */
public final class ParseOptions implements Cloneable {

    /**
     * Return only the best parse tree
     */
    public static final int BEST_PARSE = 1;

    /**
     * Return all parse trees that have the same lowest penalty
     */
    public static final int BEST_PARSES = 0;

    /**
     * Return all possible parse trees
     */
    public static final int ALL_PARSES = Integer.MAX_VALUE;

    /**
     * Allow the annotation of parts of the input as unrecognized garbage *
     */
    public boolean allowSparseParses = false;

    /**
     * Evaluate the semantic tags contained in the grammar
     */
    public boolean evaluateTags = true;

    /**
     * Build a parse tree during parse If you are only interested in the result
     * of evaluating semantic tags, setting this option to false will speed up
     * parsing and reduce memory usage.
     */
    public boolean buildParseTree = true;

    /**
     * If set to false, the parse tree will only contain the words that were
     * recognized.
     */
    public boolean includeGarbageInParseTree = false;

    /**
     * If set to false, only public rules will be represented as nodes in the
     * parse tree.
     */
    public boolean includePrivateRulesInParseTree = false;

    /**
     * If set to true, empty rules (whose production contains no terminal) will
     * be included in the parse tree.
     */
    public boolean includeEmptyRulesInParseTree = false;

    /**
     * Eliminate duplicates in the list of returned parse trees. A grammar that
     * contains empty productions can produce multiple parses that differ only
     * in their usage of empty productions, thus yielding identical parse trees.
     * Set this option to false, if you want all parse trees, even if they are
     * identical.
     */
    public boolean eliminateDuplicateParseTrees = true;

    /**
     * Set this flag to include words inside filler rules or designated as
     * GARBAGE in the result value of a rule.
     */
    public boolean includeFillerWordsInResult = false;

    /**
     * Set this flag to true, to prevent fill-ins to be added directly under the
     * root of the parse tree
     */
    public boolean preventFillersAtRoot = false;

    /**
     * The suffix that is appended to context specific rule variants
     */
    public char contextSuffix = '\u0000';

    /**
     * A dictionary mapping rule names to collections of strings
     */
    public Map<String, Map<String, String>> dynamicVocabulary = null;

    public boolean dynamicVocabularyReplacesOriginalContent = true;

    /**
     * Return a maximum of <code>maxParses</code> parses.
     *
     * @see #BEST_PARSE
     * @see #BEST_PARSES
     * @see #ALL_PARSES
     */
    public int maxParses = ParseOptions.BEST_PARSE;

    /**
     * These rules may appear anywhere in the parse (like "known" garbage)
     */
    public String fillerRules[] = null;

    @Override
    public ParseOptions clone() {

        try {
            ParseOptions options = (ParseOptions) super.clone();
            if (this.fillerRules != null) {
                options.fillerRules = new String[this.fillerRules.length];
                System.arraycopy(this.fillerRules, 0, options.fillerRules, 0,
                        this.fillerRules.length);
            }
            return options;
        } catch (Exception exn) {
            return null;
        }
    }

    public Collection<String> getDynamicVocabularyKeys(String wordClass) {

        if (this.dynamicVocabulary == null) {
            return null;
        } else {
            Map<String, String> entry = this.dynamicVocabulary.get(wordClass);
            if (entry == null) {
                return null;
            } else {
                return entry.keySet();
            }
        }
    }

    public String getDynamicVocabularySubstitution(String wordClass, String word) {

        if (this.dynamicVocabulary == null) {
            return word;
        } else {
            Map<String, String> entry = this.dynamicVocabulary.get(wordClass);
            if (entry == null) {
                return word;
            } else {
                return entry.get(word);
            }
        }
    }
}
