/*
 * @(#)RuleItem.java
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.TreeNode;

import com.clt.script.exp.Type;
import com.clt.script.exp.Value;
import com.clt.script.exp.types.TypeVariable;
import com.clt.script.exp.values.StringValue;
import com.clt.util.Counter;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */

class RuleItem
    extends Item implements ParserState {

  private Value result;
  private StringBuilder text;
  private Counter text_mod;
  private Map<String, Value> binding;
  private long start;
  private long end;
  private Counter bind_mod;
  private Rule rule;
  private boolean automatic = false;


  static RuleItem automaticRule(Rule rule) {

    RuleItem ri = new RuleItem(rule, 1, 1);
    ri.automatic = true;
    return ri;
  }


  public RuleItem(Rule rule, int repeatMin, int repeatMax) {

    super(repeatMin, repeatMax);
    if (rule == null) {
      throw new IllegalArgumentException("null Rule for RuleItem");
    }

    this.rule = rule;
    this.result = new Unassigned();
    this.binding = null;
    this.text = null;
    this.text_mod = new Counter(0);
    this.bind_mod = new Counter(0);
    this.start = 0;
    this.end = 0;
  }


  private RuleItem(RuleItem item) {

    super(item);

    this.rule = item.rule;
    this.result = item.result;
    this.binding = item.binding;
    this.text = item.text;
    this.text_mod = item.text_mod;
    this.bind_mod = item.bind_mod;
    this.text_mod.increase();
    this.bind_mod.increase();
    this.automatic = item.automatic;
    this.start = item.start;
    this.end = item.end;
  }


  @Override
  public Item copy() {

    return new RuleItem(this);
  }


  @Override
  protected void reset() {

    super.reset();

    if (this.text_mod.get() > 0) {
      this.text_mod.decrease();
    }
    this.text = null;
    this.text_mod = new Counter(0);

    if (this.bind_mod.get() > 0) {
      this.bind_mod.decrease();
    }

    this.binding = null;
    this.bind_mod = new Counter(0);

    this.result = new Unassigned();

    this.start = 0;
    this.end = 0;
  }


  @Override
  public List<Parse> shift_(Parse p) {

    // avoid infinite recursions (recursive application of a rule without
    // eating any tokens)
    if (p.isEmptyRecursion(this)) {
      return Collections.emptyList();
    }
    else {
      this.setDone(true);

      List<Item> items = this.rule.createInstance(p.getInput(), p.getOptions());
      if (items.size() == 1) {
        p.push(items.get(0));
        return Item.itemList(p);
      }
      else {
        List<Parse> continuations = new ArrayList<Parse>(items.size());
        for (int i = 0; i < items.size(); i++) {
          Parse branch;
          if (i == items.size() - 1) {
            branch = p;
          }
          else {
            branch = p.branch();
          }
          branch.push(items.get(i));
          continuations.add(branch);
        }
        return continuations;
      }
    }
  }


  public boolean isAutomatic() {

    return this.automatic;
  }


  public Rule getRule() {

    return this.rule;
  }


  public Map<String, Value> getBinding() {

    if (this.binding == null) {
      return Collections.emptyMap();
    }
    else {
      return Collections.unmodifiableMap(this.binding);
    }
  }


  public void addBinding(String rule, Value value) {

    // copy on write
    if (this.bind_mod.get() > 0) {
      this.bind_mod.decrease();
      if (this.binding != null) {
        this.binding = new HashMap<String, Value>(this.binding);
      }
      this.bind_mod = new Counter(0);
    }

    if (this.binding == null) {
      this.binding = new HashMap<String, Value>();
    }

    this.binding.put(rule, value);
  }


  public Value getValue() {

    return this.result;
  }


  public Value getResult() {

    return this.getValue() instanceof Unassigned ? new StringValue(this
      .getText()) : this.getValue();
  }


  public void setValue(Value result) {

    this.result = result;
  }


  public String getText() {

    return this.text == null ? "" : this.text.toString();
  }


  public long getStart() {

    return this.start;
  }


  public long getEnd() {

    return this.end;
  }


  public void addInterval(long start, long end) {

    if (end > 0) {
      if (this.end <= 0) {
        this.start = start;
      }
      if (this.end >= end) {
        throw new IllegalArgumentException("Illegal audio offsets");
      }
      this.end = end;
    }
  }


  public void appendText(String s) {

    if (s.length() > 0) {
      // copy on write
      if (this.text_mod.get() > 0) {
        this.text_mod.decrease();
        if (this.text != null) {
          StringBuilder b =
            new StringBuilder(this.text.length() + s.length() + 1);
          b.append(this.text);
          this.text = b;
        }
        this.text_mod = new Counter(0);
      }

      if (this.text == null) {
        this.text = new StringBuilder(s);
      }
      else {
        if (this.text.length() > 0) {
          this.text.append(' ');
        }
        this.text.append(s);
      }
    }
  }

  static class Unassigned
        extends Value {

    @Override
    public String toString() {

      return "<unassigned>";
    }


    @Override
    public boolean equals(Object o) {

      return o instanceof Unassigned;
    }


    @Override
    public int hashCode() {

      return 0x0817;
    }


    @Override
    public Value copyValue() {

      return new Unassigned();
    }


    @Override
    public Type getType() {

      return new TypeVariable();
    }


	@Override
	public Object getReadableValue()
	{
		return null;
	}
  }


  public TreeNode getCurrentNode() {

    return null;
  }

}
