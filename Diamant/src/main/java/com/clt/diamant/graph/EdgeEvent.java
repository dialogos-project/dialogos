package com.clt.diamant.graph;

import java.util.EventObject;

public class EdgeEvent extends EventObject {

  public static final int
      EDGE_ADDED = 0,
      EDGE_REMOVED = 1,
      EDGE_UPDATED = 2;

  int index;
  int id;


  public EdgeEvent(Object source, int index, int id) {

    super(source);

    this.index = index;
    this.id = id;
  }


  public int getIndex() {

    return this.index;
  }
}