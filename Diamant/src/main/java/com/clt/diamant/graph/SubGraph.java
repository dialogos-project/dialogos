package com.clt.diamant.graph;

import com.clt.diamant.Mapping;
import com.clt.diamant.graph.nodes.ReturnNode;

/**
 *
 *
 * @author Daniel Bobbert
 * @version 1.0
 */
public class SubGraph extends Graph {

    public SubGraph(GraphOwner owner) {

        super(owner);
    }

    @Override
    public Graph clone(Mapping map) {

        SubGraph g = new SubGraph(null);
        g.copy(this, map);
        return g;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<Node>[] supportedEndNodes() {

        return new Class[]{ReturnNode.class};
    }
}
