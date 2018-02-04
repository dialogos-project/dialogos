package com.clt.diamant.graph.search;

import java.util.Collection;
import java.util.Collections;

import com.clt.diamant.Resources;
import com.clt.diamant.graph.Node;
import com.clt.diamant.graph.nodes.GraphNode;
import com.clt.diamant.graph.nodes.ProcNode;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */
public class NodeTypeSearchFilter extends NodeSearchFilter {

    public static final String NAME = "NodeType";

    public static final Relation IS = new Relation("is");

    public static Object[] getRelations() {

        return new Relation[]{NodeTypeSearchFilter.IS};
    }

    private Class<? extends Node> nodeType;

    public NodeTypeSearchFilter(Class<? extends Node> nodeType) {

        this.nodeType = nodeType;
    }

    @Override
    public Collection<? extends SearchResult> match(Node n) {

        if (n.getClass().equals(this.nodeType)) {
            return Collections.singleton(new NodeSearchResult(n, this.toString()));
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public String toString() {

        return Resources.getString(NodeTypeSearchFilter.NAME) + " "
                + NodeTypeSearchFilter.IS + " " + new NodeClass(this.nodeType);
    }

    public static class NodeClass {

        Class<? extends Node> cls;

        public NodeClass(Class<? extends Node> cls) {

            this.cls = cls;
        }

        public Class<? extends Node> getNodeType() {

            return this.cls;
        }

        @Override
        public String toString() {

            if (this.cls == ProcNode.class) {
                return Resources.getString("Procedure");
            } else if (this.cls == GraphNode.class) {
                return Resources.getString("Subgraph");
            } else {
                return Node.getLocalizedNodeTypeName(this.cls);
            }
        }
    }
}
