package com.clt.diamant.undo;

import com.clt.diamant.Resources;
import com.clt.diamant.graph.Edge;
import com.clt.diamant.graph.Node;
import com.clt.undo.AbstractEdit;

public class EdgeEdit
    extends AbstractEdit {

  private Edge e;
  private Node old_target;
  private Node new_target;


  public EdgeEdit(Edge e, Node new_target) {

    super(new_target == null ? Resources.getString("DeleteEdge")
                : Resources.getString("InsertEdge"));

    this.e = e;
    this.old_target = e.getTarget();
    this.new_target = new_target;
  }


  @Override
  public void unrun() {

    this.e.setTarget(this.old_target);
  }


  @Override
  public void run() {

    this.e.setTarget(this.new_target);
  }
}