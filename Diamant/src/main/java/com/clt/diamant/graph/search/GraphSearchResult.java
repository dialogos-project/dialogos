/*
 * @(#)GraphSearchResult.java
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

import javax.swing.JComponent;

import com.clt.diamant.SingleDocument;
import com.clt.diamant.graph.Graph;
import com.clt.diamant.graph.GraphOwner;
import com.clt.diamant.graph.ui.GraphUI;
import com.clt.diamant.gui.GraphEditor;
import com.clt.diamant.gui.GraphEditorFactory;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */

public class GraphSearchResult
    extends SearchResult {

  private Graph graph;
  private String source;


  public GraphSearchResult(Graph g, String source, String message) {

    this(g, source, message, Type.INFO);
  }


  public GraphSearchResult(Graph g, String source, String message, Type type) {

    super(null, message, type);

    this.graph = g;
    this.source = source;
  }


  @Override
  public String getDocumentName() {

    if (this.getGraph() == null) {
      return "";
    }
    else {
      GraphOwner owner = this.getGraph().getOwner();
      while (owner.getSuperGraph() != null) {
        owner = owner.getSuperGraph().getOwner();
      }
      return owner.getGraphName();
    }
  }


  @Override
  public String getSource() {

    if (this.getGraph() != null) {
      return this.getGraph().graphPath(false).toString() + ":" + this.source;
    }
    else {
      return this.source;
    }
  }


  public Graph getGraph() {

    return this.graph;
  }


  @Override
  public boolean isRelevant() {

    if (this.getGraph() == null) {
      return true;
    }
    else {
      GraphOwner doc = this.getGraph().getMainOwner();
      return (doc instanceof SingleDocument ? GraphEditorFactory.isShowing(doc)
        : false);
    }
  }


  @Override
  public GraphUI showResult(JComponent parent) {

    if (this.getGraph() == null) {
      this.getToolkit().beep();
      return null;
    }
    else {
      Graph g = this.getGraph();

      GraphUI gui;
      if (parent != null) {
        gui = new GraphUI(g);
        parent.removeAll();
        parent.add(gui.getScrollPane());
        parent.validate();
      }
      else {
        GraphEditor e = GraphEditorFactory.show(g.getOwner());
        gui = e.getGraphUI();
      }

      return parent != null ? gui : null;
    }
  }
}