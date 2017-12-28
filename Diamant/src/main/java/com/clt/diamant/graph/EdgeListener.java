package com.clt.diamant.graph;

public interface EdgeListener {

  public void edgeAdded(EdgeEvent e);


  public void edgeRemoved(EdgeEvent e);


  public void edgeUpdated(EdgeEvent e);
}