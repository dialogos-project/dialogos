package com.clt.diamant.graph.search;

import javax.swing.JComponent;

import com.clt.diamant.SingleDocument;
import com.clt.diamant.graph.Graph;
import com.clt.diamant.graph.GraphOwner;
import com.clt.diamant.graph.Node;
import com.clt.diamant.graph.ui.GraphUI;
import com.clt.diamant.graph.ui.NodeComponent;
import com.clt.diamant.gui.GraphEditor;
import com.clt.diamant.gui.GraphEditorFactory;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */
public class NodeSearchResult extends SearchResult {

    private Node node;

    public NodeSearchResult(Node node) {

        this(node, null);
    }

    public NodeSearchResult(Node node, String message) {

        this(node, message, Type.INFO);
    }

    public NodeSearchResult(Node node, String message, Type type) {

        super(NodeComponent.getNodeIcon(node), message, type);

        if (node == null) {
            throw new IllegalArgumentException();
        }
        this.node = node;
    }

    @Override
    public GraphUI showResult(final JComponent parent) {

        Node n = this.getNode();
        Graph g = n.getGraph();

        GraphUI gui;
        if (parent != null) {
            gui = new GraphUI(g);
            parent.removeAll();
            parent.add(gui.getScrollPane());
            parent.validate();
        } else {
            GraphEditor e = GraphEditorFactory.show(g.getOwner());
            gui = e.getGraphUI();
        }
        gui.selectAndShowNode(n);
        return parent != null ? gui : null;
    }

    @Override
    public boolean isRelevant() {

        Node n = this.getNode();
        GraphOwner doc = n.getMainOwner();
        return (doc instanceof SingleDocument ? GraphEditorFactory.isShowing(doc)
                : false);
    }

    @Override
    public String getDocumentName() {

        Graph g = this.getNode().getGraph();
        GraphOwner owner = g.getOwner();
        while (owner.getSuperGraph() != null) {
            owner = owner.getSuperGraph().getOwner();
        }
        return owner.getGraphName();
    }

    @Override
    public String getSource() {

        return this.getNode().nodePath(false).toString();
    }

    public Node getNode() {

        return this.node;
    }

}
