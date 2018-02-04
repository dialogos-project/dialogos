package com.clt.diamant.graph.ui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.clt.diamant.Mapping;
import com.clt.diamant.graph.Edge;
import com.clt.diamant.graph.GraphOwner;
import com.clt.diamant.graph.Node;
import com.clt.diamant.graph.VisualGraphElement;

class NodeCopy implements Transferable {

    public static final DataFlavor dataflavour = new DataFlavor(Set.class, "collection of graph elements");

    private Set<VisualGraphElement> elements;
    private Mapping map;
    private GraphOwner owner;

    public NodeCopy(GraphOwner owner, Collection<VisualGraphElement> selection) {

        this.owner = owner;
        this.map = new Mapping();
        this.elements = new HashSet<VisualGraphElement>(selection.size());

        Collection<Node> nodes = new HashSet<Node>();

        // copy nodes
        for (VisualGraphElement elem : selection) {
            VisualGraphElement n = elem.clone(this.map);
            if (n == null) {
                throw new IllegalArgumentException(
                        "Error in NodeCopy: Attempt to clone " + elem + " returned <null>");
            }
            this.elements.add(n);
            if (n instanceof Node) {
                nodes.add((Node) n);
            }
        }

        // remove edges to nodes that are not in the selection
        for (Node n : nodes) {
            for (int j = 0; j < n.numEdges(); j++) {
                Edge e = n.getEdge(j);
                Node target = e.getTarget();
                if (this.map.getNode(target) == target) {
                    e.setTarget(null);
                }
            }
        }

        // update nodes to adjust edges within the selection
        for (Node n : nodes) {
            n.update(this.map);
        }
    }

    public DataFlavor[] getTransferDataFlavors() {

        return new DataFlavor[]{NodeCopy.dataflavour};
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {

        return flavor.equals(NodeCopy.dataflavour);
    }

    public Object getTransferData(DataFlavor flavor)
            throws UnsupportedFlavorException {

        if (!this.isDataFlavorSupported(flavor)) {
            throw new UnsupportedFlavorException(flavor);
        }
        return this.getElements();
    }

    public Collection<VisualGraphElement> getElements() {

        return this.elements;
    }

    public Mapping getMapping() {

        return this.map;
    }

    public GraphOwner getOwner() {

        return this.owner;
    }
}
