package com.clt.diamant.graph.nodes;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.clt.diamant.AbstractVariable;
import com.clt.diamant.ExecutionLogger;
import com.clt.diamant.IdMap;
import com.clt.diamant.InputCenter;
import com.clt.diamant.WozInterface;
import com.clt.diamant.graph.Edge;
import com.clt.diamant.graph.EdgeEvent;
import com.clt.diamant.graph.EdgeListener;
import com.clt.diamant.graph.Node;
import com.clt.diamant.graph.SubGraph;
import com.clt.xml.XMLWriter;

public class GraphNode extends OwnerNode {

    public GraphNode() {

        this(new SubGraph(null));
    }

    public GraphNode(SubGraph ownedGraph) {

        super(ownedGraph);

        for (EndNode n : this.getEndNodes()) {
            this.addEdge(n.getTitle());
        }

        this.addEdgeListener(new EdgeListener() {

            public void edgeAdded(EdgeEvent e) {

                final Node node = GraphNode.this.getEndNode(e.getIndex());
                GraphNode.this.addEdge(node.getTitle());
            }

            public void edgeRemoved(EdgeEvent e) {

                GraphNode.this.removeEdge(e.getIndex());
            }

            public void edgeUpdated(EdgeEvent e) {

                Edge edge = GraphNode.this.getEdge(e.getIndex());
                // edge.setCondition(edge.getSource().getTitle());
                edge.setCondition(GraphNode.this.getPortName(e.getIndex()));
                edge.setColor(GraphNode.this.getPortColor(e.getIndex()));
            }
        });

        // setTitle(Resources.getString("Subgraph") + ' ' + graphCounter++);
    }

    public static Color getDefaultColor() {

        return Color.ORANGE;
    }

    @Override
    public Node execute(WozInterface comm, InputCenter input, ExecutionLogger logger) {
        logNode(logger);
        Node target;
        try {
            comm.subgraph(this, true);
            target = this.getOwnedGraph().execute(comm, input, logger);
        } finally {
            comm.subgraph(this, false);
            this.setActive(false);
        }

        int index = this.getEndNodeIndex(target);
        if (index >= 0) {
            target = this.getEdge(index).getTarget();
            comm.transition(this, target, index, null);
        }

        return target;
    }

    @Override
    protected void writeVoiceXML(XMLWriter w, IdMap uid_map)
            throws IOException {

        w.printElement("subdialog", new String[]{"name"}, new String[]{"graph"
            + uid_map.graphs.put(this.getOwnedGraph())});

        this.getOwnedGraph().exportVoiceXML(w, uid_map);
    }

    /**
     * Returns a List of all Groovy-specific and non-Groovy variables
     *
     * @return list of variables
     */
    public List<AbstractVariable> getAllGlobalVariables() {
        List<AbstractVariable> allVariables = new ArrayList<AbstractVariable>(this.getOwnedGraph().getVariables());
        allVariables.addAll(this.getOwnedGraph().getGroovyVariables());
        return allVariables;
    }

}
