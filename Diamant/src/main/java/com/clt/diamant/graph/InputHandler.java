/*
 * @(#)InputHandler.java
 * Created on Mon Aug 23 2004
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

package com.clt.diamant.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.xml.sax.Attributes;

import com.clt.diamant.ExecutionLogger;
import com.clt.diamant.IdMap;
import com.clt.diamant.InputCenter;
import com.clt.diamant.Mapping;
import com.clt.diamant.Resources;
import com.clt.diamant.WozInterface;
import com.clt.diamant.graph.nodes.ContinueNode;
import com.clt.diamant.graph.nodes.LoopNode;
import com.clt.diamant.graph.nodes.OwnerNode;
import com.clt.diamant.graph.search.GraphSearchResult;
import com.clt.diamant.graph.search.SearchResult;
import com.clt.script.exp.Expression;
import com.clt.xml.AbstractHandler;
import com.clt.xml.XMLReader;
import com.clt.xml.XMLWriter;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */

public class InputHandler
    extends OwnerNode {

  public static final int BEFORE_LOCAL = 0;
  public static final int AFTER_LOCAL = 1;
  public static final int BEFORE_ALL = 2;
  public static final int AFTER_ALL = 3;

  public static final String[] TYPENAMES =
    { Resources.getString("BeforeLocal"),
            Resources.getString("AfterLocal"),
      Resources.getString("BeforeAll"),
            Resources.getString("AfterAll") };

  private String pattern;
  private int type;


  public InputHandler(Graph supergraph) {

    this(supergraph, "Input handler", "", InputHandler.BEFORE_LOCAL);
  }


  public InputHandler(final Graph supergraph, String name, String pattern,
      int type) {

    super(new Graph(null) {

      @Override
      @SuppressWarnings("unchecked")
      public Class<Node>[] supportedEndNodes() {

        return new Class[] { ContinueNode.class, LoopNode.class };
      }


      @Override
      public boolean supportsHandlers() {

        return false;
      }


      @Override
      public List<InputHandler> getPrefixHandlers() {

        List<InputHandler> v = new ArrayList<InputHandler>();
        for (InputHandler h : supergraph.getPrefixHandlers()) {
          if (h != this.getOwner()) {
            v.add(h);
          }
        }
        return v;
      }


      @Override
      public List<InputHandler> getPostfixHandlers() {

        List<InputHandler> v = new ArrayList<InputHandler>();
        for (InputHandler h : supergraph.getPostfixHandlers()) {
          if (h != this.getOwner()) {
            v.add(h);
          }
        }
        return v;
      }
    });
    System.out.println("inputhandler");
    this.setGraph(supergraph);
    this.setTitle(name);

    this.pattern = pattern;
    this.type = type;

    // initGraph();
  }


  @Override
  protected void determineEndNodes() {

    super.determineEndNodes();

    if (this.getGraph() != null) {
      this.getGraph().updateEdges();
    }
  }


  public InputHandler clone(Graph newSupergraph, Mapping map) {

    InputHandler h =
      new InputHandler(newSupergraph, this.getTitle(), this.getPattern(), this
        .getType());
    map.addHandler(this, h);
    h.getOwnedGraph().copy(this.getOwnedGraph(), map);
    return h;
  }


  public String getPattern() {

    return this.pattern;
  }


  public void setPattern(String pattern) {

    this.pattern = pattern;
  }


  public int getType() {

    return this.type;
  }


  public void setType(int type) {

    this.type = type;
  }


  @Override
  public String toString() {

    return this.getTitle() + ": " + this.getPattern();
  }


  public boolean hasContinuation() {

    return this.getOwnedGraph().getNodes(ContinueNode.class, true).size() > 0;
  }


  public Collection<ContinueNode> getContinuations() {

    return this.getOwnedGraph().getNodes(ContinueNode.class, true);
  }


  public boolean hasLoop() {

    return this.getOwnedGraph().getNodes(LoopNode.class, true).size() > 0;
  }


  @Override
  public Node execute(WozInterface comm, InputCenter input, ExecutionLogger logger) {

    comm.subgraph(this, true);
    Node result = this.getOwnedGraph().execute(comm, input, logger);
    comm.subgraph(this, false);
    return result;
  }


  @Override
  public void validate(Collection<SearchResult> errors) {

    try {
      // TODO. Check types of bound variables
      Expression.parsePattern(this.getPattern()).getType(null);
    } catch (Exception exn) {
      errors.add(new GraphSearchResult(this.getGraph(), Resources.format(
        "InputHandlerX",
                this.getTitle()), this.getTitle()
                    + Resources.format("containsIllegalInputPattern", exn
                      .getLocalizedMessage()),
                SearchResult.Type.WARNING));
    }
    super.validate(errors);
  }


  public void save(XMLWriter out, IdMap uid_map) {

    out.openElement("handler", new String[] { "uid" },
            new Object[] { uid_map.inputHandlers.put(this) });
    out.printElement("name", this.getTitle());
    out.printElement("pattern", this.getPattern());
    out.printElement("type", String.valueOf(this.getType()));
    this.getOwnedGraph().save(out, uid_map);
    out.closeElement("handler");
  }


  public static InputHandler read(Graph supergraph, final XMLReader r,
      final IdMap uid_map) {

    final InputHandler h = new InputHandler(supergraph);
    r.setHandler(new AbstractHandler("handler") {

      @Override
      public void start(String name, Attributes atts) {

        if (name.equals("graph")) {
          h.getOwnedGraph().read(r, null, uid_map);
        }
      }


      @Override
      public void end(String name) {

        if (name.equals("name")) {
          h.setTitle(this.getValue());
        }
        else if (name.equals("pattern")) {
          h.setPattern(this.getValue());
        }
        else if (name.equals("type")) {
          h.setType(Integer.parseInt(this.getValue()));
        }
      }
    });
    return h;
  }


  @Override
  protected void writeVoiceXML(XMLWriter w, IdMap uid_map) {

  }

}
