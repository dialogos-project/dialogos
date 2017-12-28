/*
 * @(#)GrammarContext.java
 * Created on 25.01.2007 by dabo
 *
 * Copyright (c) CLT Sprachtechnologie GmbH.
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import com.clt.util.NamedEntity;

/**
 * @author dabo
 * 
 */
public class GrammarContext {

  private String name;
  private Map<String, Collection<Grammar>> localNames;
  private Map<String, Grammar> qualifiedNames;


  public GrammarContext() {

    this(null);
  }


  public GrammarContext(String name) {

    this.name = name;
    this.localNames = new HashMap<String, Collection<Grammar>>();
    this.qualifiedNames = new HashMap<String, Grammar>();
  }


  public String getName() {

    return this.name;
  }


  public void add(Grammar grammar)
      throws DuplicateNameException {

    String name = grammar.getName();
    if (name != null) {
      Grammar g = this.qualifiedNames.get(name);
      if (g != null) {
        if (g == grammar) {
          return;
        }
        else {
          throw new DuplicateNameException(name);
        }
      }
      this.qualifiedNames.put(name, grammar);
      String localName = GrammarContext.localName(name);
      if (!localName.equals(name)) {
        Collection<Grammar> locals = this.localNames.get(localName);
        if (locals == null) {
          locals = new LinkedList<Grammar>();
          this.localNames.put(localName, locals);
        }
        locals.add(grammar);
      }
    }

    grammar.setContext(this);
  }


  public void remove(Grammar grammar) {

    grammar.setContext(null);

    String name = grammar.getName();
    if (name != null) {
      this.qualifiedNames.remove(name);
      String localName = GrammarContext.localName(name);
      if (!localName.equals(name)) {
        Collection<Grammar> locals = this.localNames.get(localName);
        if (locals != null) {
          locals.remove(grammar);
        }
      }
    }
  }


  public void clear() {

    for (Grammar g : new ArrayList<Grammar>(this.qualifiedNames.values())) {
      this.remove(g);
    }
  }


  public void check()
      throws SemanticException {

    for (Grammar grammar : this.qualifiedNames.values()) {
      grammar.check();
    }
  }


  public void optimize(boolean removeTags)
      throws SemanticException {

    for (Grammar grammar : this.qualifiedNames.values()) {
      try {
        grammar.optimizeInPlace(removeTags);
      } catch (Exception exn) {
        throw new SemanticException(grammar, exn.getLocalizedMessage());
      }
    }
  }


  Collection<Grammar> getGrammars() {

    return Collections.unmodifiableCollection(this.qualifiedNames.values());
  }


  private Grammar getGrammar(Grammar resolver, String name)
      throws AmbiguousNameException {

    Grammar grammar = this.qualifiedNames.get(name);
    if (grammar != null) {
      return grammar;
    }
    else {
      String localName = GrammarContext.localName(name);
      if (!localName.equals(name)) {
        Collection<Grammar> locals = this.localNames.get(localName);
        if (locals != null) {
          if (locals.size() == 1) {
            return locals.iterator().next();
          }
          else {
            throw new AmbiguousNameException(resolver, localName, locals);
          }
        }
      }

      // no grammar found
      return null;
    }
  }


  static String localName(String name) {

    int pos = name.lastIndexOf('.');
    if (pos >= 0) {
      return name.substring(pos);
    }
    else {
      return name;
    }
  }


  Rule resolve(Grammar resolver, String name, Collection<Import> imports)
        throws SemanticException {

    int pos = name.lastIndexOf('.');
    if (pos >= 0) {
      // qualified name. Find grammar.
      String grammarName = name.substring(0, pos);
      Grammar grammar = this.getGrammar(resolver, grammarName);
      if (grammar == null) {
        throw new SemanticException(resolver, "Reference to unknown grammar '"
                      + grammarName + "'");
      }
      else {
        String ruleName = name.substring(pos + 1);
        Rule r = grammar.getRule(ruleName);
        if (r != null) {
          if (r.isPublic() || (grammar == resolver)) {
            return r;
          }
          else {
            throw new SemanticException(resolver, "Rule '" + name
              + "' is not visible");
          }
        }
        else {
          return null;
        }
      }
    }
    else {
      Collection<ImportedRule> candidates = new HashSet<ImportedRule>();
      // unqualified name. Check imports.
      for (Import i : imports) {
        Grammar grammar = this.getGrammar(resolver, i.getGrammarName());
        if (grammar == null) {
          throw new SemanticException(resolver, "Import of unknown grammar '"
                          + i.getGrammarName() + "'");
        }

        if (i.isWildcard()) {
          Rule r = grammar.getRule(name);
          if (r != null) {
            if (r.isPublic() || (grammar == resolver)) {
              candidates.add(new ImportedRule(grammar, r));
            }
          }
        }
        else if (i.getRuleName().equals(name)) {
          Rule r = grammar.getRule(name);
          if (r != null) {
            if (r.isPublic() || (grammar == resolver)) {
              candidates.add(new ImportedRule(grammar, r));
            }
            else {
              throw new SemanticException(resolver, "Error in import: Rule '"
                                  + i.getGrammarName() + "." + i.getRuleName()
                                  + "' is not visible");
            }
          }
          else {
            throw new SemanticException(resolver, "Import of unknown rule '"
                              + i.getGrammarName() + "." + i.getRuleName()
              + "'");
          }
        }
      }

      if (candidates.isEmpty()) {
        return null;
      }
      else if (candidates.size() == 1) {
        return candidates.iterator().next().getRule();
      }
      else {
        throw new AmbiguousNameException(resolver, name, candidates);
      }
    }
  }

  private static class ImportedRule
        implements NamedEntity {

    private Grammar grammar;
    private Rule rule;


    public ImportedRule(Grammar grammar, Rule rule) {

      this.grammar = grammar;
      this.rule = rule;
    }


    public Rule getRule() {

      return this.rule;
    }


    @Override
    public int hashCode() {

      return this.grammar.hashCode() ^ this.rule.hashCode();
    }


    @Override
    public boolean equals(Object o) {

      if (o instanceof ImportedRule) {
        ImportedRule r = (ImportedRule)o;
        return (r.grammar == this.grammar) && (r.rule == this.rule);
      }
      else {
        return false;
      }
    }


    public String getName() {

      return this.grammar.getName() + "." + this.rule.getName();
    }


    @Override
    public String toString() {

      return this.getName();
    }
  }
}
