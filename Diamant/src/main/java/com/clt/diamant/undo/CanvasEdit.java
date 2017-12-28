package com.clt.diamant.undo;

import java.awt.Dimension;

import com.clt.diamant.Resources;
import com.clt.diamant.graph.Graph;
import com.clt.undo.AbstractEdit;

public class CanvasEdit
    extends AbstractEdit {

  private Graph g;
  private Dimension oldSize, newSize;


  public CanvasEdit(Graph g, Dimension oldSize, Dimension newSize) {

    super(Resources.getString("CanvasSize"));

    this.g = g;
    this.newSize = newSize;
    this.oldSize = oldSize;
  }


  @Override
  public void unrun() {

    this.g.setSize(this.oldSize.width, this.oldSize.height);
  }


  @Override
  public void run() {

    this.g.setSize(this.newSize.width, this.newSize.height);
  }
}