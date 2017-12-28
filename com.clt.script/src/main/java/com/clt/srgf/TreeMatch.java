/*
 * @(#)TreeMatch.java
 * Created on 15.02.05
 *
 * Copyright (c) 2005 CLT Sprachtechnologie GmbH.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CLT Sprachtechnologie GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CLT Sprachtechnologie GmbH.
 */

package com.clt.srgf;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.tree.TreeNode;

import com.clt.script.exp.Value;
import com.clt.script.exp.values.StringValue;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */

class TreeMatch implements ParserState {

  private TreeNode currentNode;
  private Value value;
  private Map<String, Value> binding;
  private LinkedList<TreeNode> input;
  private StringBuilder content;
  private boolean insideFillerRule = false;
  private long start = 0;
  private long end = 0;


  TreeMatch(TreeNode currentNode, LinkedList<TreeNode> input,
      boolean insideFillerRule) {

    this(currentNode, input, new RuleItem.Unassigned(),
      new HashMap<String, Value>(),
            new StringBuilder(), insideFillerRule, 0, 0);
  }


  private TreeMatch(TreeNode currentNode, LinkedList<TreeNode> input,
      Value value,
                      Map<String, Value> binding, StringBuilder content,
      boolean insideFillerRule,
                      long start, long end) {

    this.currentNode = currentNode;
    this.input = input;
    this.value = value;
    this.binding = binding;
    this.content = content;
    this.insideFillerRule = insideFillerRule;
    this.start = start;
    this.end = end;
  }


  public TreeMatch branch() {

    TreeMatch m =
      new TreeMatch(this.currentNode, new LinkedList<TreeNode>(this.input),
        this.value.copy(),
            new HashMap<String, Value>(this.binding), new StringBuilder(
              this.content.toString()),
            this.insideFillerRule, this.start, this.end);
    return m;
  }


  public Value getValue() {

    return this.value;
  }


  public void setValue(Value value) {

    this.value = value;
  }


  public Value getResult() {

    Value v = this.getValue();
    if (v instanceof RuleItem.Unassigned) {
      v = new StringValue(this.getText());
    }
    return v;
  }


  public Map<String, Value> getBinding() {

    return this.binding;
  }


  public TreeNode getCurrentNode() {

    return this.currentNode;
  }


  public LinkedList<TreeNode> getInputNodes() {

    return this.input;
  }


  public void setInputNodes(LinkedList<TreeNode> input) {

    this.input = input;
  }


  public void appendText(String s) {

    if (s.length() > 0) {
      if (this.content.length() > 0) {
        this.content.append(' ');
      }
      this.content.append(s);
    }
  }


  public String getText() {

    return this.content.toString();
  }


  boolean hasMoreInput() {

    return !this.input.isEmpty();
  }


  int getInputSize() {

    return this.input.size();
  }


  boolean isInsideFillerRule() {

    return this.insideFillerRule;
  }


  public Collection<TreeMatch> createSparseBranches(Grammar grammar,
      ParseOptions options,
            boolean lastWasGarbage, Rule exception) {

    List<TreeMatch> continuations = new LinkedList<TreeMatch>();

    // only try if there is something to match
    if (this.hasMoreInput()) {
      if (options.fillerRules != null) {
        for (int i = 0; i < options.fillerRules.length; i++) {
          Rule r = grammar.resolveRule(options.fillerRules[i]);
          if ((r != null) && (r != exception)) {
            TreeMatch branch = this.branch();
            branch.insideFillerRule = true;
            Collection<TreeMatch> results =
              new Rulename(r.getName()).match(branch,
                            grammar, options);
            for (Iterator<TreeMatch> it = results.iterator(); it.hasNext();) {
              TreeMatch m = it.next();
              m.insideFillerRule = false;
              continuations.add(m);
            }
          }
        }
      }

      if (options.allowSparseParses && options.includeGarbageInParseTree) { // &&
        // !lastWasGarbage)
        // {
        TreeMatch branch = this.branch();
        Collection<TreeMatch> results =
          new Rulename(Grammar.GARBAGE).matchImpl(
                    branch, grammar, options, true);
        for (Iterator<TreeMatch> it = results.iterator(); it.hasNext();) {
          TreeMatch m = it.next();
          continuations.add(m);
        }
      }
    }

    // if (!continuations.isEmpty())
    // System.out.println("Created " + continuations.size() + " sparse
    // branches.");

    return continuations;
  }


  public long getStart() {

    return this.start;
  }


  public long getEnd() {

    return this.end;
  }
}
