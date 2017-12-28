/*
 * @(#)Parse.java
 * Created on Mon Oct 21 2002
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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.clt.script.exp.Value;
import com.clt.util.PropertyContainer;

/**
 * A parse represents the result of parsing an input string with a
 * {@link Grammar}. The parse object can be used to retrieve the root of the
 * corrensponding {@link ParseTree} or the {@link com.clt.script.exp.Value} that
 * results from the interpretation of the semantic tags contained in the
 * grammar. There is no public constructor. Parse objects are created by the
 * {@link Grammar} while parsing.
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

public class Parse {

  private int penalty;
  private double probability;
  private Input input;

  private boolean sparse;
  private Stack<ParseItem> items;

  private RuleItem currentRule;
  private ParseTree parseTree;
  private Grammar grammar;
  private ParseOptions options;

  private boolean lastWordWasSparseGarbage = false;
  private boolean insideFillerRule = false;

  @SuppressWarnings("unused")
  private transient Word[] matchedWords;


  private Parse(Grammar grammar, Input input, ParseOptions options,
      int stackSize) {

    this.grammar = grammar;
    this.input = input;
    this.options = options;
    this.penalty = 0;
    this.probability = 1.0;
    this.items = new Stack<ParseItem>();
    if (stackSize > this.items.capacity()) {
      this.items.ensureCapacity(stackSize + 1);
    }
    this.currentRule = null;
    this.sparse = false;
  }


  Parse(Grammar grammar, Rule start, Input input, ParseOptions options) {

    this(grammar, input, options, 1);

    this.push(new RuleItem(start, 1, 1));
  }


  void dispose() {

    this.input = null;
    this.items = null;
    if (this.parseTree != null) {
      this.parseTree.dispose();
      this.parseTree = null;
    }
  }


  /*
   * Return the penalty associated with this parse. Penaties are used with
   * sparse parsing and the garbage model. The fewer words in the input can be
   * matched by the grammar (i.e. the more words have to recognized as garbage),
   * the higher is the penalty of the resulting parse.
   */
  public int getPenalty() {

    return this.penalty;
  }


  void addPenalty(int penalty) {

    if (penalty < 0) {
      throw new IllegalArgumentException("penalty must be >= 0");
    }
    this.penalty += penalty;
  }


  public double getProbability() {

    return this.probability;
  }


  void setProbability(double probability) {

    this.probability = probability;
  }


  boolean success() {

    return this.isDone() && this.input.isEmpty();
  }


  boolean failure() {

    return this.isDone() && !this.input.isEmpty();
  }


  boolean isInsideFillerRule() {

    return this.insideFillerRule;
  }


  ParseOptions getOptions() {

    return this.options;
  }


  public void postProcess() {

    if (this.parseTree != null) {
      this.parseTree.postProcess(this.options);
    }
  }

  public static long branchTime = 0;


  Parse branch() {

    Parse p =
      new Parse(this.grammar, this.input.clone(), this.options, this.items
        .size());
    for (Iterator<ParseItem> it = this.items.iterator(); it.hasNext();) {
      ParseItem item = it.next().copy();
      p.pushItem(item);
    }
    p.penalty = this.penalty;
    p.probability = this.probability;
    p.sparse = this.sparse;
    p.lastWordWasSparseGarbage = this.lastWordWasSparseGarbage;
    p.insideFillerRule = this.insideFillerRule;

    if (this.parseTree != null) {
      p.parseTree = this.parseTree.copy();
    }

    return p;
  }


  boolean isDone() {

    return (this.items.size() == 1) && this.items.peek().getItem().isDone();
  }


  private Item pushItem(ParseItem pitem) {

    // System.out.println("Pushing " + pitem);
    this.items.push(pitem);
    Item item = pitem.getItem();
    if (item instanceof RuleItem) {
      this.currentRule = (RuleItem)item;
    }
    return item;
  }


  private void push(ParseItem pitem) {

    Item item = this.pushItem(pitem);
    if (item instanceof RuleItem) {
      if (((RuleItem)item).isAutomatic()) {
        this.insideFillerRule = true;
      }

      if (this.options.buildParseTree) {
        Rule r = ((RuleItem)item).getRule();
        if ((this.parseTree == null) || r.isPublic()
          || this.options.includePrivateRulesInParseTree) {
          RuleParseNode n =
            new RuleParseNode(r, this.options, ((RuleItem)item).isAutomatic());
          if (this.parseTree == null) {
            this.parseTree = new ParseTree(n);
          }
          else {
            this.parseTree.add(n, this.options);
          }
        }
      }
    }
  }


  void push(Item item) {

    this.push(new ParseItem(item, this.input.size()));
  }


  boolean isEmptyRecursion(RuleItem r) {

    for (int i = this.items.size() - 1; i >= 0; i--) {
      ParseItem pitem = this.items.get(i);
      if ((pitem.getItem() instanceof RuleItem) && (pitem.getItem() != r)
                    && (((RuleItem)pitem.getItem()).getRule() == r.getRule())
                    && (pitem.getInputSize() == this.getInputSize())) {
        return true;
      }
    }

    return false;
  }


  Item pop() {

    ParseItem pitem = this.items.pop();
    final Item item = pitem.getItem();

    if (item.isDone()) {
      int itemCost = item.getPenalty();
      this.addPenalty(itemCost);

      if ((item instanceof TerminalItem) || (item instanceof Rule.DynVocItem)) {
        this.lastWordWasSparseGarbage = false;
      }
    }

    // If this parse thread has not shifted any tokens, prevent its
    // repetition
    // in order to avoid infinite parse loops of $NULL expansions
    if (pitem.getInputSize() == this.getInputSize()) {
      // System.out.println("Preventing repetition of " + pitem);
      item.preventRepetition();
    }

    if (item == this.currentRule) {
      RuleItem finishedRule = this.currentRule;

      if (finishedRule.isAutomatic()) {
        this.insideFillerRule = false;
      }

      // System.out.println("Finished rule " + finishedRule + " with text
      // <" + finishedRule.getText() + ">");
      this.currentRule = null;
      for (int i = this.items.size() - 1; (i >= 0)
        && (this.currentRule == null); i--) {
        Item ritem = this.items.get(i).getItem();
        if (ritem instanceof RuleItem) {
          this.currentRule = (RuleItem)ritem;
          if (this.options.buildParseTree) {
            if (finishedRule.getRule().isPublic()
                                || this.options.includePrivateRulesInParseTree) {
              this.parseTree.finishNode(
                !this.options.includeEmptyRulesInParseTree
                                  || (item.getRepeatCount() == 0),
                this.options.preventFillersAtRoot);
            }
          }
        }
      }

      Value result = finishedRule.getResult();
      result.setAttribute("text", finishedRule.getText());
      result.setAttribute("start", finishedRule.getStart());
      result.setAttribute("end", finishedRule.getEnd());
      this.currentRule.addBinding(finishedRule.getRule().getName(), result);
      this.currentRule.appendText(finishedRule.getText());
      this.currentRule.addInterval(finishedRule.getStart(), finishedRule
        .getEnd());
    }

    return item;
  }


  Collection<Parse> shift() {

    try {
      ParseItem pitem = this.items.peek();
      Item item = pitem.getItem();

      /*
       * The following code no longer works, because GARBAGE was changed to
       * accept exactly one word.
       * 
       * // If the last token was an OOV and we now get to an explicit garbage
       * model, then discard this // parse. This works because there will be
       * another parse without the OOV that is happy to // continue using
       * GARBAGE. This way explicit garbage will be preferred to sparse parses
       * using OOV.
       * 
       * if (item instanceof RuleItem && ((RuleItem) item).getRule() ==
       * grammar.getRule(Grammar.GARBAGE) && (lastWordWasSparseGarbage ||
       * ((RuleItem) item).automatic)) return Collections.EMPTY_LIST; else
       */
      return item.shift(this);
    } catch (RuntimeException exn) {
      System.err.println("Current parse stack:");
      for (int i = this.items.size() - 1; i >= 0; i--) {
        System.err.println("- " + this.items.get(i).getItem());
      }
      throw exn;
    }
  }


  void addTerminals(Word[] words) {

    if (this.options.buildParseTree) {
      for (int i = 0; i < words.length; i++) {
        this.parseTree.add(words[i], this.options);
      }
    }
  }


  void skipWord() {

    // System.out.println("Skipping " + input.getFirst().getWord() + " with
    // item: " + items.peek());
    final Word w = this.input.removeFirst();
    if (this.options.buildParseTree && this.options.includeGarbageInParseTree) {
      this.parseTree.addGarbage(w, this.grammar.getRule(Grammar.GARBAGE),
        this.options);
    }

    if (this.options.includeFillerWordsInResult) {
      this.appendText(w.getWord());
    }
    this.currentRule.addInterval(Parse.getStart(w), Parse.getEnd(w));

    this.addPenalty(1);
    this.sparse = true;
    this.lastWordWasSparseGarbage = true;
  }


  List<Parse> createSparseBranches(boolean lastWasGarbage, Rule exception) {

    List<Parse> continuations = new LinkedList<Parse>();

    if ((this.options.fillerRules != null) && !this.insideFillerRule) {
      for (int i = 0; i < this.options.fillerRules.length; i++) {
        Rule r = this.grammar.resolveRule(this.options.fillerRules[i]);
        if ((r != null) && (r != exception)) {
          Parse branch = this.branch();
          branch.push(RuleItem.automaticRule(r));
          continuations.add(branch);
        }
      }
    }

    if (this.options.allowSparseParses && !lastWasGarbage) {
      /*
       * Parse branch = this; while (branch.hasMoreInput()) { Parse sparseParse
       * = branch.branch(); sparseParse.skipWord();
       * continuations.add(sparseParse); branch = sparseParse; }
       */
      if (this.hasMoreInput()) {
        Parse branch = this.branch();
        branch.skipWord();
        continuations.add(branch);
        continuations.addAll(branch.createSparseBranches(true, null));
      }
    }

    return continuations;
  }


  /*
   * Return the root node of the parse tree.
   */
  public ParseNode getParseTree() {

    return this.parseTree == null ? null : this.parseTree.getRoot();
  }


  void appendText(String s) {

    this.currentRule.appendText(s);
  }


  void addInterval(long start, long end) {

    this.currentRule.addInterval(start, end);
  }


  static long getStart(Word w) {

    if (w instanceof PropertyContainer) {
      try {
        return ((Number)((PropertyContainer)w).getProperty("start"))
          .longValue();
      } catch (Exception ignore) {
      }
    }
    return 0;
  }


  static long getEnd(Word w) {

    if (w instanceof PropertyContainer) {
      try {
        return ((Number)((PropertyContainer)w).getProperty("end")).longValue();
      } catch (Exception ignore) {
      }
    }
    return 0;
  }


  /*
   * Return the matched input as a string.
   */
  public String getText() {

    return this.currentRule.getText();
  }


  /*
   * Return the result of the interpretation of the semantic tags in the
   * grammar. If the path matched by this parse did not contain any tags, the
   * matched input will be returned as a {@link
   * com.clt.script.exp.values.StringValue}.
   */
  public Value getResult() {

    return this.currentRule.getResult();
  }


  Value getValue() {

    return this.currentRule.getValue();
  }


  void setValue(Value result) {

    this.currentRule.setValue(result);
  }


  RuleItem getCurrentRule() {

    return this.currentRule;
  }


  boolean hasMoreInput() {

    return !this.input.isEmpty();
  }


  int getInputSize() {

    return this.input.size();
  }


  public Input getInput() {

    return this.input;
  }


  Map<String, Value> getBinding() {

    return this.currentRule.getBinding();
  }

  private static class ParseItem {

    private Item item;
    private int inputSize;


    public ParseItem(Item item, int inputSize) {

      this.item = item;
      this.inputSize = inputSize;
    }


    public Item getItem() {

      return this.item;
    }


    public int getInputSize() {

      return this.inputSize;
    }


    public ParseItem copy() {

      return new ParseItem(this.item.copy(), this.inputSize);
    }


    @Override
    public String toString() {

      return this.item.toString() + " (" + this.inputSize + " words left)";
    }
  }

  private static class ParseTree {

    private RuleParseNode root;
    private RuleParseNode currentNode;
    private ReferenceCount refCount;


    private ParseTree() {

    }


    public ParseTree(RuleParseNode root) {

      this.root = root;
      this.currentNode = root;
      this.refCount = new ReferenceCount();
    }


    public ParseTree copy() {

      ParseTree t = new ParseTree();
      t.root = this.root;
      t.currentNode = this.currentNode;
      t.refCount = this.refCount;
      this.refCount.increase();
      return t;
    }


    private void prepareModification() {

      if (this.refCount.get() > 1) {
        Map<ParseNode, ParseNode> mapping = new HashMap<ParseNode, ParseNode>();
        this.root = (RuleParseNode)this.root.clone(mapping);
        this.currentNode = (RuleParseNode)mapping.get(this.currentNode);

        // decrease the refCount of all other copies
        this.refCount.decrease();
        // give ourselves a new fresh refCount
        this.refCount = new ReferenceCount();
      }
    }


    public ParseNode getRoot() {

      return this.root;
    }


    public void add(RuleParseNode n, ParseOptions options) {

      this.prepareModification();

      this.currentNode.addChild(n);
      this.currentNode = n;
    }


    public void add(Word w, ParseOptions options) {

      this.prepareModification();

      this.currentNode.addChild(new TerminalParseNode(w));
    }


    public void dispose() {

      this.root = null;
      this.currentNode = null;
      this.refCount.decrease();
      this.refCount = null;
    }


    public void addGarbage(Word w, Rule garbage, ParseOptions options) {

      this.prepareModification();
      TerminalParseNode tpn = new TerminalParseNode(w);
      RuleParseNode n = this.currentNode;
      // special case when the last token added was also an OOV
      if ((n.numChildren() > 0) && (n.getLastChild() instanceof RuleParseNode)
                    && (((RuleParseNode)n.getLastChild()).getRule() == garbage)) {
        ((RuleParseNode)n.getLastChild()).addChild(tpn);
      }
      else {
        RuleParseNode oov = new RuleParseNode(garbage, options, true);
        oov.addChild(tpn);
        n.addChild(oov);
      }
    }


    public void finishNode(boolean removeEmptyRules,
        boolean preventFillersAtRoot) {

      this.prepareModification();
      this.currentNode = (RuleParseNode)this.currentNode.getParent();

      // if the last rule's expansion was an empty sequence, remove it
      // from the parse tree
      if (removeEmptyRules) {
        while (this.currentNode.removeLastChildIfEmpty()) {
          ;
        }
      }

      // make sure trailing OOVs are moved up in the tree
      if ((this.currentNode.numChildren() > 0)
                    && (this.currentNode.getLastChild() instanceof RuleParseNode)
                    && ((this.currentNode != this.root) || !preventFillersAtRoot)) {
        LinkedList<RuleParseNode> oovs = new LinkedList<RuleParseNode>();
        while ((this.currentNode.getLastChild() instanceof RuleParseNode)
                        && this
                          .isAutomaticRule(((RuleParseNode)this.currentNode
                            .getLastChild()).getLastChild())) {
          ParseNode gn =
            ((RuleParseNode)this.currentNode.getLastChild()).removeLastChild();
          oovs.addFirst((RuleParseNode)gn);
        }
        for (RuleParseNode n : oovs) {
          this.currentNode.addChild(n);
        }
      }
    }


    private boolean isAutomaticRule(ParseNode n) {

      return (n instanceof RuleParseNode) && ((RuleParseNode)n).isAutomatic();
    }


    public void postProcess(ParseOptions options) {

      this.prepareModification();

      if (options.preventFillersAtRoot) {
        // try to rewrite tree, so that fillers do not occur at the top
        // level
        for (int i = 0; i < this.root.getChildCount(); i++) {
          ParseNode n = this.root.getChild(i);
          if (this.isAutomaticRule(n)) {
            int j = i + 1;
            while ((j < this.root.getChildCount())
              && this.isAutomaticRule(this.root.getChild(j))) {
              j++;
            }
            if ((j < this.root.getChildCount())
              && (this.root.getChildAt(j) instanceof RuleParseNode)) {
              RuleParseNode rn = (RuleParseNode)this.root.getChildAt(j);
              while (j > i) {
                rn.insertChild(this.root.removeChildAt(--j), 0);
              }
              i = 1;
            }
            else {
              i = j;
            }
          }
        }
      }
    }
  }

  private static class ReferenceCount {

    int count;


    public ReferenceCount() {

      this.count = 1;
    }


    public void increase() {

      this.count++;
    }


    public void decrease() {

      this.count--;
    }


    public int get() {

      return this.count;
    }
  }
}
