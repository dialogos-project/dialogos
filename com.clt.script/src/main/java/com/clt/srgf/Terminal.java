//
//  Terminal.java
//  DialogManager
//
//  Created by Daniel Bobbert on Tue Jul 30 2002.
//  Copyright (c) 2002 CLT Sprachtechnologie GmbH. All rights reserved.
//

package com.clt.srgf;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.tree.TreeNode;

import com.clt.script.exp.values.StringValue;

/**
 * An expansion that matches one or more words.
 * 
 * @see Word
 * @author Daniel Bobbert
 * @version 1.0
 */

public class Terminal
    extends Expansion {

  private Entry[] entries;
  private Map<String, Entry> entryMap;
  private int maxPatternLength;
  private Token<?> token;


  public Terminal(String s) {

    this.entries = new Entry[] { new Entry(s) };
    this.init();
  }


  public Terminal(String[] s) {

    this.entries = new Entry[s.length];
    for (int i = 0; i < s.length; i++) {
      this.entries[i] = new Entry(s[i]);
    }
    this.init();
  }


  public Terminal(Terminal[] terminals) {

    Collection<Entry> entries = new ArrayList<Entry>(terminals.length);
    for (int i = 0; i < terminals.length; i++) {
      for (int j = 0; j < terminals[i].entries.length; j++) {
        entries.add(terminals[i].entries[j]);
      }
    }
    this.entries = entries.toArray(new Entry[entries.size()]);
    this.init();
  }


  private Terminal(Entry[] entries) {

    if (entries.length == 0) {
      throw new IllegalArgumentException(
        "Terminal must have at least one entry");
    }
    this.entries = entries;
    this.init();
  }


  private void init() {

    this.maxPatternLength = 0;
    this.entryMap = new HashMap<String, Entry>(3 * this.entries.length / 2);
    for (int i = 0; i < this.entries.length; i++) {
      this.entryMap.put(this.entries[i].getPattern(), this.entries[i]);
      this.maxPatternLength =
        Math.max(this.maxPatternLength, this.entries[i].getPattern().length());
    }

    if (this.entries.length == 1) {
      this.token = this.entries[0].getToken();
    }
    else {
      final Set<String> firstWordMap = new HashSet<String>();
      for (String pattern : this.entryMap.keySet()) {
        int pos = pattern.indexOf(' ');
        if (pos >= 0) {
          pattern = pattern.substring(0, pos);
          if (!this.entryMap.containsKey(pattern)) {
            firstWordMap.add(pattern);
          }
        }
      }

      this.token = new Token<Set<String>>(this.entryMap.keySet()) {

        @Override
        boolean match(Input input) {

          if (!input.isEmpty()) {
            String pattern = input.getFirstPattern();
            return Terminal.this.entryMap.containsKey(pattern)
              || firstWordMap.contains(pattern);
          }
          else {
            return false;
          }
        }


        @Override
        public String toString() {

          StringBuilder b = new StringBuilder();
          if (Terminal.this.entries.length <= 4) {
            for (int i = 0; i < Terminal.this.entries.length; i++) {
              if (i > 0) {
                b.append(" | ");
              }
              b.append(Terminal.this.entries[i].toString());
            }
          }
          else {
            b.append(Terminal.this.entries[0].toString());
            b.append(" | ");
            b.append(Terminal.this.entries[1].toString());
            b.append(" | ... | ");
            b.append(Terminal.this.entries[Terminal.this.entries.length - 2]
              .toString());
            b.append(" | ");
            b.append(Terminal.this.entries[Terminal.this.entries.length - 1]
              .toString());
          }
          return b.toString();
        }
      };
    }
  }


  @Override
  public void check(Collection<String> visibleIDs,
      Collection<Exception> warnings) {

  }


  @Override
  boolean collectWords(Collection<String> s, ParseOptions options) {

    boolean changed = false;
    for (int i = 0; i < this.entries.length; i++) {
      if (s.add(this.entries[i].toString())) {
        changed = true;
      }
    }
    return changed;
  }


  @Override
  Iterator<StringBuilder> generate_(final StringBuilder prefix,
      GenerationOptions options) {

    if (prefix.length() > 0) {
      prefix.append(" ");
    }
    if (options.groupDictionaries) {
      if (this.entries.length == 1) {
        prefix.append(this.entries[0]);
      }
      else {
        prefix.append("(");
        for (int i = 0; i < this.entries.length; i++) {
          if (i > 0) {
            prefix.append("|");
          }
          prefix.append(this.entries[i]);
        }
        prefix.append(")");
      }
      return Collections.singleton(prefix).iterator();
    }
    else {
      return new Iterator<StringBuilder>() {

        int n = 0;


        public boolean hasNext() {

          return this.n < Terminal.this.size();
        }


        public StringBuilder next() {

          StringBuilder b;
          if (this.n < Terminal.this.size() - 1) {
            b = new StringBuilder(prefix.toString());
          }
          else {
            b = prefix;
          }
          b.append(Terminal.this.entries[this.n++].toString());
          return b;
        }


        public void remove() {

          throw new UnsupportedOperationException();
        }
      };
    }
  }


  int size() {

    return this.entries.length;
  }


  String get(int index) {

    return this.entries[index].toString();
  }


  @Override
  Expansion optimize_(boolean removeTags) {

    Terminal t = new Terminal(this.entries);
    t.setRepeat(this.getRepeatMin(), this.getRepeatMax());
    return t;
  }


  @Override
  Item createInstance() {

    return new TerminalItem(this, this.getRepeatMin(), this.getRepeatMax());
  }


  @Override
  Collection<TreeMatch> matchImpl(TreeMatch match, Grammar grammar,
      ParseOptions options) {

    Collection<TreeMatch> results = new LinkedList<TreeMatch>();
    LinkedList<TreeNode> nodes = match.getInputNodes();
    StringBuilder s = new StringBuilder();
    while (!nodes.isEmpty() && !nodes.getFirst().getAllowsChildren()) {
      if (s.length() > 0) {
        s.append(' ');
      }
      String text = nodes.removeFirst().toString();
      s.append(Terminal.normalize(text));
      Entry e = this.entryMap.get(s.toString());
      // match.appendText(text);
      if (e != null) {
        // results.add(match.branch());
        TreeMatch m = match.branch();
        // System.out.println(e.toString() + " is " +
        // (match.isInsideFillerRule() ? "" : "NOT ") + "in filler
        // rule");
        if (!match.isInsideFillerRule() || options.includeFillerWordsInResult) {
          m.appendText(e.toString());
        }
        results.add(m);
        /* +++ */
        results.addAll(m.createSparseBranches(grammar, options, false, null));
      }
    }
    return results;
  }


  @Override
  boolean computePossibleTokens_(boolean recompute) {

    /*
     * boolean changed = false; for (int i=0; i<entries.length; i++) if
     * (addPossibleToken(entries[i].getToken())) changed = true; return changed;
     */
    return this.addPossibleToken(this.token);
  }


  /**
   * Returns a list of matches. Patterns must be sorted shortest to longest.
   */
  List<Input.Match> match(String[] patterns) {

    List<Input.Match> matches = new ArrayList<Input.Match>(patterns.length);
    for (int i = 0; (i < patterns.length)
      && (patterns[i].length() <= this.maxPatternLength); i++) {
      Entry match = this.entryMap.get(patterns[i]);
      if (match != null) {
        // the number of words is index+1
        matches.add(new Input.Match(match.toString(), i + 1));
      }
    }
    return matches;
  }


  @Override
  void write(PrintWriter w, Grammar.Format format) {

    if (this.entries.length == 1) {
      this.write(w, format, 0);
    }
    else {
      if (format == Grammar.Format.NGSL) {
        w.print("[");
        for (int i = 0; i < this.entries.length; i++) {
          if (i > 0) {
            w.print(" ");
          }
          this.write(w, format, i);
        }
        w.print("]");
      }
      else {
        for (int i = 0; i < this.entries.length; i++) {
          if (i > 0) {
            w.print(" | ");
          }
          this.write(w, format, i);
        }
      }
    }
  }


  private void write(PrintWriter w, Grammar.Format format, int index) {

    String s = this.entries[index].toString();
    boolean ascii_nowhitespace = true;
    for (int i = 0; (i < s.length()) && ascii_nowhitespace; i++) {
      char c = s.charAt(i);
      if (!(((c >= 'A') && (c <= 'Z')) || ((c >= 'a') && (c <= 'z')) || ((c >= '0') && (c <= '9')))) {
        ascii_nowhitespace = false;
      }
    }
    if (ascii_nowhitespace) {
      if (format == Grammar.Format.NGSL) {
        w.print(s.toLowerCase());
      }
      else {
        w.print(s);
      }
    }
    else {
      switch (format) {
        case SRGF:
        case JSGF:
          w.print(StringValue.toSourceString(s, true));
          break;
        case GRXML:
          w.println(com.clt.xml.XMLWriter.encode(s));
          break;
        case TEMIC:
          w.print('\"');
          w
            .print(Terminal
              .normalize(s, new char[] { '\u00df', '\u00e4', '\u00f6',
                '\u00fc',
                            '\u00c4', '\u00d6', '\u00dc' }, new String[] {
                "\\\"s", "\\\"a",
                            "\\\"o", "\\\"u", "\\\"A", "\\\"O", "\\\"U" }, null));
          w.print('\"');
          break;
        case LH:
        case VOCON:
        case VOCON_G:
          w.print('\"');
          w.print(s);
          w.print('\"');
          break;
        case NGSL:
          w.print("(");
          StringBuilder gslNormalized = new StringBuilder(s.length());
          for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isLetterOrDigit(c)) {
              gslNormalized.append(Character.toLowerCase(c));
            }
            else if ("-_@.'".indexOf(c) >= 0) {
              gslNormalized.append(c);
            }
            else {
              gslNormalized.append(' ');
            }
          }
          w.print(gslNormalized.toString());
          w.print(")");
          break;
        default:
          throw new IllegalArgumentException("Unknown export format");
      }
    }
  }


  @Override
  WordGraph.Node[] createWordGraphImpl(WordGraph.Node predecessors[],
      boolean mergePrivateRules,
            Tokenizer tokenizer) {

    List<WordGraph.Node> nodes =
      new ArrayList<WordGraph.Node>(this.entries.length
                + predecessors.length);
    boolean empty = false;
    for (int i = 0; i < this.entries.length; i++) {
      WordGraph.Node first = null;
      if (tokenizer != null) {
        String s[] = tokenizer.tokenize(this.entries[i].toString());
        WordGraph.Node end = null;
        for (int j = 0; j < s.length; j++) {
          WordGraph.Node n = new WordGraph.WordNode(s[j]);
          // nodes.add(n);
          if (j == 0) {
            first = n;
          }
          else {
            end.addEdge(n);
          }
          end = n;
        }
        if (end != null) {
          nodes.add(end);
        }
      }
      else {
        nodes.add(first = new WordGraph.WordNode(this.entries[i].toString()));
      }
      if (first == null) {
        if (!empty) {
          empty = true;
          nodes.addAll(Arrays.asList(predecessors));
        }
      }
      else {
        for (int j = 0; j < predecessors.length; j++) {
          predecessors[j].addEdge(first);
        }
      }
    }
    return nodes.toArray(new WordGraph.Node[nodes.size()]);
  }


  @Override
  boolean eq(Expansion e) {

    if (e instanceof Terminal) {
      Terminal t = (Terminal)e;
      return this.entryMap.keySet().equals(t.entryMap.keySet());
    }
    else {
      return false;
    }
  }


  static String normalize(String s) {

    s =
      Terminal.normalize(s.toLowerCase(), new char[] { '\u00df', '\u00e4',
        '\u00f6', '\u00fc',
                '\u00c4', '\u00d6', '\u00dc' }, new String[] { "ss", "ae",
        "oe", "ue", "ae", "oe",
                "ue" }, "()-_.,?!<>");
    /*
     * s = Expression.replace(s, "\\\"s", "ss"); s = Expression.replace(s,
     * "\\\"a", "ae"); s = Expression.replace(s, "\\\"o", "oe"); s =
     * Expression.replace(s, "\\\"u", "ue"); s = Expression.replace(s, "\\\"A",
     * "Ae"); s = Expression.replace(s, "\\\"O", "Oe"); s =
     * Expression.replace(s, "\\\"U", "Ue");
     */
    return s;
  }


  private static String normalize(String s, char[] src, String[] dst,
      String spacify) {

    StringBuilder b = new StringBuilder(s.length());
    boolean wasSpace = false;
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (s.startsWith("<...>", i)) {
        b.append("<...>");
        i += "<...>".length() - 1;
      }
      else if ((spacify != null) && (spacify.indexOf(c) >= 0)) {
        if (!wasSpace) {
          b.append(' ');
          wasSpace = true;
        }
      }
      else {
        boolean found = false;
        for (int j = 0; (j < src.length) && !found; j++) {
          if (src[j] == c) {
            found = true;
            b.append(dst[j]);
          }
        }
        if (!found) {
          if (Character.isWhitespace(c)) {
            if (!wasSpace) {
              b.append(' ');
              wasSpace = true;
            }
          }
          else {
            b.append(c);
            wasSpace = false;
          }
        }
      }

    }
    return b.toString().trim();
  }

  private static class Entry {

    private String[] patterns;
    private String normalized_pattern;
    private TerminalToken token;


    public Entry(String s) {

      List<String> words = Grammar.tokenize(s);
      this.patterns = new String[words.size()];
      int i = 0;
      for (Iterator<String> it = words.iterator(); it.hasNext(); i++) {
        this.patterns[i] = it.next();
      }

      this.init();
    }


    private void init() {

      StringBuilder b = new StringBuilder();
      for (int i = 0; i < this.patterns.length; i++) {
        if (i > 0) {
          b.append(' ');
        }
        b.append(this.patterns[i]);
      }
      this.normalized_pattern = Terminal.normalize(b.toString());
      int pos = this.normalized_pattern.indexOf(' ');
      if (pos >= 0) {
        this.token =
          new TerminalToken(this.normalized_pattern.substring(0, pos));
      }
      else {
        this.token = new TerminalToken(this.normalized_pattern);
      }
    }


    public TerminalToken getToken() {

      return this.token;
    }


    public String getPattern() {

      return this.normalized_pattern;
    }


    @Override
    public int hashCode() {

      return this.normalized_pattern.hashCode();
    }


    @Override
    public boolean equals(Object o) {

      if (o instanceof Entry) {
        return ((Entry)o).normalized_pattern.equals(this.normalized_pattern);
      }
      else {
        return false;
      }
    }


    @Override
    public String toString() {

      StringBuilder b = new StringBuilder();
      for (int i = 0; i < this.patterns.length; i++) {
        if (i > 0) {
          b.append(' ');
        }
        b.append(this.patterns[i]);
      }
      return b.toString();
    }
  }
}
