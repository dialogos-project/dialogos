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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.tree.TreeNode;

import com.clt.script.exp.Value;
import com.clt.script.exp.values.ListValue;
import com.clt.script.exp.values.StringValue;
import com.clt.script.exp.values.StructValue;
import com.clt.util.Counter;
import com.clt.util.PropertyContainer;

public class Rulename
    extends Expansion {

  private String rulename;


  public Rulename(String rulename) {

    this.rulename = rulename;
  }


  public String getRulename() {

    return this.rulename;
  }


  public Rule resolve() {

    Rule r = this.getRule();
    if (r != null) {
      Grammar g = r.getGrammar();
      if (g != null) {
        return g.resolveRule(this.rulename);
      }
    }
    return null;
  }


  @Override
  public void check(Collection<String> visibleIDs,
      Collection<Exception> warnings) {

    if (this.resolve() == null) {
      throw new SemanticException(this.getRule().getGrammar(),
        "Reference to unknown rule '"
                  + this.rulename + "'");
    }
    visibleIDs.add("$" + this.getRulename());
  }


  @Override
  Iterator<StringBuilder> generate_(StringBuilder prefix,
      GenerationOptions options) {

    Rule r = this.resolve();
    if (r == null) {
      throw new SemanticException(this.getRule().getGrammar(),
        "Reference to unknown rule '"
                  + this.rulename + "'");
    }
    return r.generate(prefix, options);
  }


  @Override
  Expansion optimize_(boolean removeTags) {

    Rulename r = new Rulename(this.rulename);

    // if the rule can produce an empty expansion, don't make this item
    // optional
    // (would only produce duplicate parses)
    try {
      if ((this.getRepeatMin() == 0)
        && this.resolve().getPossibleTokens().contains(Token.EMPTY)) {
        r.setRepeat(1, this.getRepeatMax());
      }
      else {
        r.setRepeat(this.getRepeatMin(), this.getRepeatMax());
      }
    } catch (Exception exn) {
      // ignore optimization of the rulename cannot be resolved yet
    }
    return r;
  }


  @Override
  boolean eq(Expansion e) {

    if (e instanceof Rulename) {
      Rulename r = (Rulename)e;
      return this.getRulename().equals(r.getRulename());
    }
    else {
      return false;
    }
  }


  @Override
  Item createInstance() {

    Rule r = this.resolve();
    if (r == null) {
      throw new SemanticException(this.getRule().getGrammar(),
        "Reference to unknown rule '"
                  + this.rulename + "'");
    }
    else {
      return new RuleItem(r, this.getRepeatMin(), this.getRepeatMax());
    }
  }


  @Override
  Collection<TreeMatch> matchImpl(TreeMatch match, Grammar grammar,
      ParseOptions options) {

    return this.matchImpl(match, grammar, options, false);
  }


  Collection<TreeMatch> matchImpl(TreeMatch match, Grammar grammar,
      ParseOptions options,
            boolean forcePublic) {

    Rule r = this.resolve();
    if (r == null) {
      throw new SemanticException(this.getRule().getGrammar(),
        "Reference to unknown rule '"
                  + this.rulename + "'");
    }
    if (r.isPublic() || options.includePrivateRulesInParseTree || forcePublic) {
      Collection<Token<?>> tokens = r.getPossibleTokens();

      String name = r.getName();
      int len = name.length();
      while ((len > 0) && (name.charAt(len - 1) == options.contextSuffix)) {
        len--;
      }
      name = name.substring(0, len);

      Collection<TreeMatch> continuations;
      TreeNode n = null;
      boolean addSparseBranches = false;
      if (!match.hasMoreInput()) {
        if ((tokens.contains(Token.EMPTY) || tokens.contains(Token.TAG))
                        && !options.includeEmptyRulesInParseTree) {
          continuations =
            r.match(match.getCurrentNode(), new LinkedList<TreeNode>(),
                      grammar, options, match.isInsideFillerRule());
        }
        else {
          continuations = Collections.emptyList();
        }
      }
      else {
        LinkedList<TreeNode> nodes = match.getInputNodes();

        n = nodes.getFirst();
        if (!n.getAllowsChildren() || !name.equals(n.toString())) {
          // System.out.println("Rulename is \"" + name + "\" but next
          // node is called " + n.toString());
          if (tokens.contains(Token.EMPTY) || tokens.contains(Token.TAG)) {
            // System.out.println("Can continue because " +
            // r.getName() + " may be empty");
            continuations =
              r.match(match.getCurrentNode(), new LinkedList<TreeNode>(),
                            grammar, options, match.isInsideFillerRule());
          }
          else {
            // System.out.println("Parse failed");
            continuations = Collections.emptyList();
          }
        }
        else {
          /* +++ */
          nodes.removeFirst();
          addSparseBranches = true;
          continuations =
            r.match(n, grammar, options, match.isInsideFillerRule());
        }
      }

      Collection<TreeMatch> results = new LinkedList<TreeMatch>();
      for (Iterator<TreeMatch> it = continuations.iterator(); it.hasNext();) {
        TreeMatch m;
        if (it.hasNext()) {
          m = match.branch();
        }
        else {
          m = match;
        }
        TreeMatch ruleMatch = it.next();
        Value v = ruleMatch.getValue();
        if (v instanceof RuleItem.Unassigned) {
          v = new StringValue(ruleMatch.getText());
        }

        if (n != null) {
          Rulename.copyAttributes("WordConfidence", n, v);
        }

        m.getBinding().put(r.getName(), v);
        m.appendText(ruleMatch.getText());
        results.add(m);

        if (addSparseBranches) {
          results.addAll(m.createSparseBranches(grammar, options, false, r));
        }
      }
      return results;
    }
    else {
      Collection<TreeMatch> continuations = new LinkedList<TreeMatch>();
      Collection<String> words = options.getDynamicVocabularyKeys(r.getName());
      if (words != null) {
        StringBuilder b = new StringBuilder();
        int n = 1;
        for (Iterator<TreeNode> it =
          new ArrayList<TreeNode>(match.getInputNodes()).iterator(); it
          .hasNext(); n++) {
          b.append(it.next().toString());
          String word = b.toString();
          if (words.contains(word)) {
            TreeMatch m = it.hasNext() ? match.branch() : match;
            for (int i = 0; i < n; i++) {
              m.getInputNodes().removeFirst();
            }
            word = options.getDynamicVocabularySubstitution(r.getName(), word);
            m.appendText(word);
            m.getBinding().put(r.getName(), new StringValue(word));
            continuations.add(m);
            /* +++ */
            continuations.addAll(m.createSparseBranches(grammar, options,
              false, r));
          }

          if (it.hasNext()) {
            b.append(" ");
          }
        }

        if (options.dynamicVocabularyReplacesOriginalContent) {
          return continuations;
        }
      }

      Collection<TreeMatch> results = r.getExpansion().match(
                new TreeMatch(match.getCurrentNode(), match.getInputNodes(),
                    match.isInsideFillerRule()), grammar, options);
      for (Iterator<TreeMatch> it = results.iterator(); it.hasNext();) {
        TreeMatch m = it.next();
        Value v = m.getValue();
        if (v instanceof RuleItem.Unassigned) {
          v = new StringValue(m.getText());
        }
        TreeMatch result = it.hasNext() ? match.branch() : match;
        result.appendText(m.getText());
        result.getBinding().put(r.getName(), v);
        result.setInputNodes(m.getInputNodes());
        continuations.add(result);
      }
      return continuations;
    }
  }


  private static float getWordConfidence(TreeNode n) {

    Counter c = new Counter(0);
    float sum = Rulename.addWordConfidences(n, c);
    return sum / c.get();
  }


  private static float addWordConfidences(TreeNode n, Counter c) {

    if (n.getAllowsChildren()) {
      float sum = 0.0f;
      for (int i = 0; i < n.getChildCount(); i++) {
        sum += Rulename.addWordConfidences(n.getChildAt(i), c);
      }
      return sum;
    }
    else {
      c.increase();
      if (((PropertyContainer)n).getProperty("Confidence") instanceof Number) {
        return ((Number)((PropertyContainer)n).getProperty("Confidence"))
          .floatValue();
      }
      else {
        return 1.0f;
      }
    }
  }


  private static void setAttribute(Value v, String prop, TreeNode n) {

    if (prop.equals("WordConfidence")) {
      if (v.getAttribute("WordConfidence") == null) {
        v.setAttribute("WordConfidence", Rulename.getWordConfidence(n));
      }
    }
    else if (prop.equals("SlotConfidence")) {
      if ((v.getAttribute("SlotConfidence") == null)
                    && (((PropertyContainer)n).getProperty("Confidence") instanceof Number)) {
        v.setAttribute("SlotConfidence",
                  ((Number)((PropertyContainer)n).getProperty("Confidence"))
                    .floatValue());
      }
    }
    else {
      if (v.getAttribute(prop) == null) {
        Object value = ((PropertyContainer)n).getProperty(prop);
        if (value instanceof Boolean) {
          v.setAttribute(prop, ((Boolean)value).booleanValue());
        }
        else if ((value instanceof Float) || (value instanceof Double)) {
          // number
          v.setAttribute(prop, ((Number)value).doubleValue());
        }
        else if (value instanceof Number) {
          // number
          v.setAttribute(prop, ((Number)value).intValue());
        }
        else if (value != null) {
          v.setAttribute(prop, value.toString());
        }
      }
    }
  }


  static void copyAttributes(String property, TreeNode n, Value v) {

    if (n instanceof PropertyContainer) {
      Rulename.setAttribute(v, property, n);

      // recursively copy attributes to children of complex values
      // The fact, that attributes are only copied if they don't exist
      // yet, preserves
      // existing attributes of the children.
      if (v instanceof ListValue) {
        ListValue lv = (ListValue)v;
        for (Iterator<Value> it = lv.iterator(); it.hasNext();) {
          Rulename.copyAttributes(property, n, it.next());
        }
      }
      else if (v instanceof StructValue) {
        StructValue lv = (StructValue)v;
        for (Iterator<String> it = lv.labels(); it.hasNext();) {
          Rulename.copyAttributes(property, n, lv.getValue(it.next()));
        }
      }
    }
  }


  @Override
  boolean computePossibleTokens_(boolean recompute) {

    Rule r = this.resolve();
    if (r == null) {
      throw new SemanticException(this.getRule().getGrammar(),
        "Reference to unknown rule '"
                  + this.rulename + "'");
    }

    boolean changed = this.addPossibleToken(new Rule.RuleToken(r));

    // if (r.isDirty())
    // changed = true;

    for (Token<?> t : r.getPossibleTokens()) {
      if (this.addPossibleToken(t)) {
        changed = true;
      }
    }

    return changed;
  }


  @Override
  boolean collectRules(Collection<Rule> s, ParseOptions options) {

    Rule r = this.resolve();
    if (r == null) {
      throw new SemanticException(this.getRule().getGrammar(),
        "Reference to unknown rule '"
                  + this.rulename + "'");
    }

    return s.add(r);
  }


  @Override
  public void export(PrintWriter w, Grammar.Format format) {

    // special case: Garbage for Vocon
    if (((format == Grammar.Format.VOCON) || (format == Grammar.Format.VOCON_G))
                && this.rulename.equals(Grammar.GARBAGE)) {
      if (this.getRepeatMin() == 0) {
        w.print("[<...>]");
      }
      else {
        w.print("<...>");
      }
    }
    else {
      super.export(w, format);
    }
  }


  @Override
  void write(PrintWriter w, Grammar.Format format) {

    switch (format) {
      case SRGF:
        w.print('$');
        w.print(this.rulename);
        break;
      case GRXML:
        ((com.clt.xml.XMLWriter)w).printElement("ruleref",
                    new String[] { this.getRule().getGrammar().isSpecialRule(
                      this.rulename) ? "special"
                            : "uri" }, new String[] { "#" + this.rulename });
        break;
      case TEMIC:
      case JSGF:
      case LH:
      case VOCON:
      case VOCON_G:
        w.print('<');
        w.print(this.resolve().getNormalizedRuleName(format));
        w.print('>');
        break;
      case NGSL:
        w.print(this.rulename.toUpperCase());
        break;
      default:
        throw new IllegalArgumentException("Unknown export format");
    }
  }


  @Override
  WordGraph.Node[] createWordGraphImpl(WordGraph.Node predecessors[],
      boolean mergePrivateRules,
            Tokenizer tokenizer) {

    Rule r = this.resolve();
    if (mergePrivateRules && !r.isPublic()) {
      /*
       * WordGraph.Node start = new WordGraph.WordNode("!NULL"); for (int i=0;
       * i<predecessors.length; i++) predecessors[i].addEdge(start); return
       * getRule().getExpansion().createWordGraph(new WordGraph.Node[] { start
       * }, mergePrivateRules, tokenizer);
       */
      return r.getExpansion().createWordGraph(predecessors, mergePrivateRules,
        tokenizer);
    }
    else {
      WordGraph.Node n = new WordGraph.RuleNode(r);
      for (int i = 0; i < predecessors.length; i++) {
        predecessors[i].addEdge(n);
      }
      return new WordGraph.Node[] { n };
    }
  }
}
