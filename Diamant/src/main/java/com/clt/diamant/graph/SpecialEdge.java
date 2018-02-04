package com.clt.diamant.graph;

/**
 * A tagging interface to indicate that an edge is automatically generated from
 * special external data (like global input handlers)
 *
 * @author Daniel Bobbert
 * @version 1.0
 */
public abstract class SpecialEdge extends Edge {

    public SpecialEdge(Node source) {

        super(source);
    }

    public SpecialEdge(Node source, Node target) {

        super(source, target);
    }

    public abstract String getType();
}
