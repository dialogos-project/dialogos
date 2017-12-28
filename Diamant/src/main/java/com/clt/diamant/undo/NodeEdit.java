package com.clt.diamant.undo;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.clt.diamant.Resources;
import com.clt.diamant.graph.Edge;
import com.clt.diamant.graph.Graph;
import com.clt.diamant.graph.Group;
import com.clt.diamant.graph.Node;
import com.clt.undo.AbstractEdit;

public class NodeEdit
    extends AbstractEdit {

  private Node n;
  private Group group;
  private Collection<Edge> in_edges;
  private List<Node> out_targets;
  private Graph graph;
  private Point location;
  private boolean insert;


  public NodeEdit(Graph graph, Node n, boolean insert) {

    super(insert ? Resources.getString("InsertNode") : Resources
      .getString("DeleteNode"));

    this.graph = graph;
    this.n = n;
    this.group = n.getGroup();
    this.location = (Point)n.getProperty("location");
    this.insert = insert;

    this.in_edges = new ArrayList<Edge>(n.in_edges());
    this.out_targets = new ArrayList<Node>(n.numEdges());
    for (Edge e : n.edges()) {
      this.out_targets.add(e.getTarget());
    }
  }


  @Override
  public void unrun() {

    if (this.insert) {
      this.delete();
    }
    else {
      this.insert();
    }
  }


  @Override
  public void run() {

    if (this.insert) {
      this.insert();
    }
    else {
      this.delete();
    }
  }


  private void delete() {

    this.graph.remove(this.n);
  }


  private void insert() {

    this.n.setLocation(this.location.x, this.location.y);
    this.graph.add(this.n);
    if (this.group != null) {
      this.group.add(this.n);
    }
    if (this.in_edges != null) {
      for (Edge e : this.in_edges) {
        e.setTarget(this.n);
      }
    }
    if (this.out_targets != null) {
      int i = 0;
      for (Edge e : this.n.edges()) {
        e.setTarget(this.out_targets.get(i++));
      }
    }
  }
}