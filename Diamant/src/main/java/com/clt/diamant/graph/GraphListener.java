package com.clt.diamant.graph;

/**
 *
 *
 * @author Daniel Bobbert
 * @version 1.0
 */
public interface GraphListener {

    public void elementAdded(Graph g, VisualGraphElement element);

    public void elementRemoved(Graph g, VisualGraphElement element);

    public void sizeChanged(Graph g, int width, int height);

    public void graphRenamed(Graph g, String name);
}
