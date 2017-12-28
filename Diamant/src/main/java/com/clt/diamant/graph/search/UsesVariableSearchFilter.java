/*
 * @(#)UsesVariableSearchFilter.java
 * Created on Tue Jul 19 2005
 *
 * Copyright (c) 2004 CLT Sprachtechnologie GmbH.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CLT Sprachtechnologie GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CLT Sprachtechnologie GmbH.
 */

package com.clt.diamant.graph.search;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Vector;

import com.clt.diamant.Device;
import com.clt.diamant.DialogOutput;
import com.clt.diamant.Resources;
import com.clt.diamant.Slot;
import com.clt.diamant.graph.Edge;
import com.clt.diamant.graph.Functions;
import com.clt.diamant.graph.Graph;
import com.clt.diamant.graph.InputHandler;
import com.clt.diamant.graph.Node;
import com.clt.diamant.graph.Procedure;
import com.clt.diamant.graph.SpecialEdge;
import com.clt.diamant.graph.nodes.CallNode;
import com.clt.diamant.graph.nodes.ConditionalNode;
import com.clt.diamant.graph.nodes.InputNode;
import com.clt.diamant.graph.nodes.OutputNode;
import com.clt.diamant.graph.nodes.OwnerNode;
import com.clt.diamant.graph.nodes.ScriptNode;
import com.clt.diamant.graph.nodes.SetVariableNode;
import com.clt.diamant.graph.nodes.SleepNode;
import com.clt.diamant.graph.nodes.TestVariableNode;
import com.clt.script.Environment;
import com.clt.script.Script;
import com.clt.script.exp.Expression;
import com.clt.script.exp.Type;
import com.clt.script.exp.Variable;
import com.clt.util.Counter;
import com.clt.util.StringTools;

/**
 * 
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

public class UsesVariableSearchFilter extends NodeSearchFilter {

  public static final String NAME = "UsesVariable";


  public static Object[] getRelations() {

    return null;
  }

  String name;
  Object relation;


  public UsesVariableSearchFilter(String name, Object relation) {

    this.name = name;
    this.relation = relation;
  }


  @Override
  public Collection<? extends SearchResult> match(Node n) {

    Collection<SearchResult> matches = new LinkedList<SearchResult>();

    if (n instanceof CallNode) {
      Map<Slot, String> arguments = ((CallNode)n).getArguments();
      for (Slot variable : arguments.keySet()) {
        try {
          if (this.checkExpression(n, arguments.get(variable))) {
            matches.add(new NodeSearchResult(n, "Argument " + variable + " = "
              + arguments.get(variable)));
          }
        } catch (Exception ignore) {
        }
      }

      Map<Slot, String> returnPatterns = ((CallNode)n).getReturnPatterns();
      for (Slot variable : returnPatterns.keySet()) {
        try {
          if (this.checkPattern(n, returnPatterns.get(variable))) {
            matches.add(new NodeSearchResult(n, "Return variable " + variable
              + " = " + arguments.get(variable)));
          }
        } catch (Exception ignore) {
        }
      }
    }
    else if (n instanceof ConditionalNode) {
      if (this.checkExpression(n, (String)n
        .getProperty(ConditionalNode.EXPRESSION))) {
        matches.add(new NodeSearchResult(n, (String)n
          .getProperty(ConditionalNode.EXPRESSION)));
      }
    }
    else if (n instanceof OutputNode) {
      Hashtable h = (Hashtable)n.getProperty(OutputNode.OUTPUT);
      if (h != null) {
        for (Enumeration devices = h.keys(); devices.hasMoreElements();) {
          final Device d = (Device)devices.nextElement();
          final DialogOutput output = (DialogOutput)h.get(d);
          if (output.size() > 0) {
            for (int i = 0; i < output.size(); i++) {
              if (this.checkExpression(n, output.getValue(i))) {
                matches.add(new NodeSearchResult(n, d + " <- "
                  + output.getValue(i)));
              }
            }
          }
        }
      }

      if (n instanceof InputNode) {
        for (int i = 0; i < n.numEdges(); i++) {
          Edge e = n.getEdge(i);
          if (!(e instanceof SpecialEdge)) {
            if (this.checkPattern(n, e.getCondition())) {
              matches.add(new NodeSearchResult(n, "Pattern: "
                + e.getCondition()));
            }
          }
        }

        String t = (String)n.getProperty(InputNode.TIMEOUT);
        if (this.checkExpression(n, t)) {
          matches.add(new NodeSearchResult(n, "Timeout after " + t
            + " milliseconds"));
        }

        Hashtable bargeinSend =
          (Hashtable)n.getProperty(InputNode.BARGE_IN_SEND);
        for (Iterator it = bargeinSend.keySet().iterator(); it.hasNext();) {
          final Device d = (Device)it.next();
          String exp = (String)bargeinSend.get(d);

          if (this.checkExpression(n, exp)) {
            matches.add(new NodeSearchResult(n, "Timeout completion: " + d
              + " <- " + exp));
          }
        }
      }
    }
    else if (n instanceof OwnerNode) {
      Graph g = ((OwnerNode)n).getOwnedGraph();

      Collection<InputHandler> handlers = g.getHandlers();
      for (InputHandler ih : handlers) {
        if (this.checkPattern(n, ih.getPattern())) {
          matches.add(new GraphSearchResult(g,
            "Input handler " + ih.getTitle(), ih.getPattern()));
        }
      }

      Collection<Functions> functions = g.getFunctions();
      for (Functions gf : functions) {
        if (this.checkScript(n, gf.getScript())) {
          matches
            .add(new GraphSearchResult(g, "Functions " + gf.getName(), ""));
        }
      }

      Collection<Slot> vars = g.getVariables();
      for (Slot s : vars) {
        if (this.checkExpression(n, s.getInitValue())) {
          matches.add(new GraphSearchResult(g, "Variable " + s.getName(), s
            .getInitValue()));
        }
      }

      if (g instanceof Procedure) {
        vars = ((Procedure)g).getParameters();
        for (Iterator it = vars.iterator(); it.hasNext();) {
          Slot s = (Slot)it.next();
          if (this.checkExpression(n, s.getInitValue())) {
            matches.add(new GraphSearchResult(g, "Parameter " + s.getName(), s
              .getInitValue()));
          }
        }
      }
    }
    else if (n instanceof ScriptNode) {
      if (this.checkScript(n, (String)n.getProperty(ScriptNode.SCRIPT))) {
        matches.add(new NodeSearchResult(n, "Script"));
      }
    }
    else if (n instanceof SleepNode) {
      if (this.checkExpression(n, (String)n.getProperty(SleepNode.SLEEPTIME))) {
        matches.add(new NodeSearchResult(n, "Sleep time: "
          + n.getProperty(SleepNode.SLEEPTIME)));
      }
    }
    else if (n instanceof SetVariableNode) {
      Vector assignments = (Vector)n.getProperty(SetVariableNode.ASSIGNMENTS);
      if (assignments != null) {
        for (Iterator it = assignments.iterator(); it.hasNext();) {
          SetVariableNode.VarAssignment va =
            (SetVariableNode.VarAssignment)it.next();
          if ((va.getVariable() != null) || !StringTools.isEmpty(va.getValue())) {
            Slot v = va.getVariable();
            if (((v != null) && v.getName().equals(this.name))
              || this.checkExpression(n, va.getValue())) {
              matches.add(new NodeSearchResult(n, v + " = " + va.getValue()));
            }
          }
        }
      }
    }
    else if (n instanceof TestVariableNode) {
      Slot v = (Slot)n.getProperty(TestVariableNode.VAR_NAME);
      if ((v != null) && v.getName().equals(this.name)) {
        matches.add(new NodeSearchResult(n, "Test variable: " + v));
      }

      boolean others = n.getBooleanProperty(TestVariableNode.ELSE_EDGE);
      for (int i = 0; i < (others ? n.numEdges() - 1 : n.numEdges()); i++) {
        Edge e = n.getEdge(i);

        if (this.checkPattern(n, e.getCondition())) {
          matches.add(new NodeSearchResult(n, "Pattern: " + e.getCondition()));
        }
      }
    }

    return matches;
  }


  @Override
  public String toString() {

    return Resources.getString(UsesVariableSearchFilter.NAME) + " " + this.name;
  }


  private boolean checkScript(Node n, String script) {

    if (!StringTools.isEmpty(script)) {
      CountingEnvironment env =
        new CountingEnvironment(n.getGraph().getEnvironment(Graph.LOCAL));
      try {
        Script.parseScript(script, env);
      } catch (Exception exn) {
      }
      return env.getOccurences() > 0;
    }
    return false;
  }


  private boolean checkExpression(Node n, String exp) {

    if (!StringTools.isEmpty(exp)) {
      CountingEnvironment env =
        new CountingEnvironment(n.getGraph().getEnvironment(Graph.LOCAL));
      try {
        Script.parseExpression(exp, env);
      } catch (Exception exn) {
      }
      return env.getOccurences() > 0;
    }
    return false;
  }


  private boolean checkPattern(Node n, String pattern) {

    try {
      if (!StringTools.isEmpty(pattern)) {
        if (n.parsePattern(pattern).getFreeVars().contains(this.name)) {
          return true;
        }
      }
    } catch (Exception ignore) {
    }
    return false;
  }

  private class CountingEnvironment implements Environment {

    Counter occurences = new Counter(0);
    Environment env;


    public CountingEnvironment(Environment env) {

      this.env = env;
    }


    public Variable createVariableReference(String name) {

      if (name.equals(UsesVariableSearchFilter.this.name)) {
        this.occurences.increase();
      }
      return this.env.createVariableReference(name);
    }


    public Expression createFunctionCall(final String name,
        final Expression[] arguments) {

      return this.env.createFunctionCall(name, arguments);
    }


    public Type getType(String typeName) {

      return this.env.getType(typeName);
    }


    public Reader include(String id)
            throws IOException {

      return this.env.include(id);
    }


    public int getOccurences() {

      return this.occurences.get();
    }
  }

}