package com.clt.diamant.graph.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Comparator;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.clt.diamant.SingleDocument;
import com.clt.diamant.graph.Graph;
import com.clt.diamant.graph.GraphListener;
import com.clt.diamant.graph.GraphOwner;
import com.clt.diamant.graph.Node;
import com.clt.diamant.graph.Procedure;
import com.clt.diamant.graph.VisualGraphElement;
import com.clt.diamant.gui.GraphEditor;
import com.clt.diamant.gui.GraphEditorFactory;
import com.clt.diamant.gui.SingleDocumentWindow;
import com.clt.gui.GradientTree;
import com.clt.gui.Images;

public class ProcTree extends GradientTree {

    private DefaultTreeModel model;
    private SingleDocumentWindow<? extends SingleDocument> window;
    private boolean singleEditor;

    public ProcTree(final SingleDocumentWindow<? extends SingleDocument> window, boolean singleEditor) {

        super();

        this.window = window;
        this.singleEditor = singleEditor;
        this.setMainGraph(null);

        this.addTreeSelectionListener(new TreeSelectionListener() {

            public void valueChanged(TreeSelectionEvent evt) {

                GraphTreeNode n = (GraphTreeNode) evt.getPath().getLastPathComponent();

                window.setMainView(n.getGraph());
            }
        });

        window.addViewListener(new ComponentAdapter() {

            @Override
            public void componentShown(ComponentEvent e) {

                GraphUI ui = window.getMainView();
                if (ui == null) {
                    ProcTree.this.setSelectionPath(null);
                } else {
                    ProcTree.this.selectNode(ui);
                }
            }
        });

        TreeCellRenderer renderer = new TreeCellRenderer() {

            JLabel label = new JLabel();

            Icon procIcon = Images.load("nodes/ProcNode.png");
            Icon graphIcon = Images.load("nodes/GraphNode.png");

            public Component getTreeCellRendererComponent(JTree tree, Object value,
                    boolean selected, boolean expanded, boolean leaf, int row,
                    boolean hasFocus) {

                this.label.setText(String.valueOf(value));

                if (((GraphTreeNode) value).getGraph() instanceof Procedure) {
                    this.label.setIcon(this.procIcon);
                } else {
                    this.label.setIcon(this.graphIcon);
                }
                this.label.setDisabledIcon(this.label.getIcon());
                this.label.setOpaque(false);
                this.label.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
                this.label.setSize(this.label.getPreferredSize());

                return this.label;
            }
        };

        this.setRootVisible(true);
        this.setShowsRootHandles(true);
        this.setCellRenderer(renderer);
        this.setRowHeight(0);

        this.setMinimumSize(new Dimension(100, 100));
    }

    @Override
    public void addNotify() {

        super.addNotify();

        this.setMainGraph(this.window.getDocument().getOwnedGraph());
    }

    @Override
    public void removeNotify() {

        this.setMainGraph(null);

        super.removeNotify();
    }

    private GraphTreeNode getRoot() {

        Object root = null;
        TreeModel model = this.getModel();
        if (model != null) {
            root = model.getRoot();
        }

        if (root instanceof GraphTreeNode) {
            return (GraphTreeNode) root;
        } else {
            return null;
        }
    }

    public void setMainGraph(Graph g) {

        GraphTreeNode oldRoot = this.getRoot();
        if (oldRoot != null) {
            oldRoot.dispose();
        }

        this.model
                = new DefaultTreeModel(new GraphTreeNode(g != null ? g.getOwner() : null));
        this.setModel(this.model);
    }

    private GraphUI showGraph(GraphTreeNode node) {

        TreePath path = new TreePath(node.getPath());
        this.scrollPathToVisible(path);

        this.getSelectionModel().setSelectionPath(path);

        return this.window.getMainView();
    }

    private void selectNode(GraphUI ui) {

        GraphTreeNode root = this.getRoot();
        if (root != null) {
            this.selectNode(ui.getGraph().getOwner(), root);
        }
    }

    private boolean selectNode(GraphOwner owner, GraphTreeNode node) {

        if (node.getGraphOwner() == owner) {
            this.showGraph(node);
            return true;
        } else {
            for (int i = 0; i < node.getChildCount(); i++) {
                if (this.selectNode(owner, (GraphTreeNode) node.getChildAt(i))) {
                    return true;
                }
            }
            return false;
        }
    }

    private class GraphTreeNode
            extends DefaultMutableTreeNode {

        private GraphOwner graphOwner;
        private GraphListener gl;

        private Comparator<GraphTreeNode> comparator
                = new Comparator<GraphTreeNode>() {

            public int compare(GraphTreeNode n1, GraphTreeNode n2) {

                GraphOwner go1 = n1.getGraphOwner();
                GraphOwner go2 = n2.getGraphOwner();

                if ((go1.getOwnedGraph() instanceof Procedure)
                        && !(go2.getOwnedGraph() instanceof Procedure)) {
                    return -1;
                } else if ((go2.getOwnedGraph() instanceof Procedure)
                        && !(go1.getOwnedGraph() instanceof Procedure)) {
                    return 1;
                } else {
                    int result = go1.getGraphName().compareTo(go2.getGraphName());
                    if (result != 0) {
                        return result;
                    } else {
                        return System.identityHashCode(go2)
                                - System.identityHashCode(go1);
                    }
                }
            }
        };

        public GraphTreeNode(final GraphOwner graphOwner) {

            super(graphOwner != null ? graphOwner.getOwnedGraph() : null);
            this.graphOwner = graphOwner;

            if (ProcTree.this.singleEditor && (graphOwner != null)) {
                GraphEditor editor = GraphEditorFactory.get(graphOwner);
                if (editor != null) {
                    editor.closeEditor();
                }

                GraphEditorFactory.register(graphOwner, new GraphEditor() {

                    private GraphUI ui;

                    public GraphOwner getGraphOwner() {

                        return graphOwner;
                    }

                    public GraphUI getGraphUI() {

                        return this.ui;
                    }

                    public void showEditor() {

                        this.ui = ProcTree.this.showGraph(GraphTreeNode.this);
                    }

                    public void closeEditor() {

                        this.ui = null;
                    }

                    public boolean isShowing() {

                        return (this.ui != null)
                                && (ProcTree.this.window.getMainView() == this.ui);
                    }
                });
            }

            if (graphOwner != null) {
                for (Node n : graphOwner.getOwnedGraph().getNodes()) {
                    if (n instanceof GraphOwner) {
                        this.insertNode((GraphOwner) n);
                    }
                }

                this.gl = new GraphListener() {

                    public void elementAdded(Graph g, VisualGraphElement n) {

                        if (n instanceof GraphOwner) {
                            GraphTreeNode.this.insertNode((GraphOwner) n);
                        }
                    }

                    public void elementRemoved(Graph g, VisualGraphElement n) {

                        if (n instanceof GraphOwner) {
                            for (int i = 0; i < GraphTreeNode.this.getChildCount(); i++) {
                                GraphTreeNode gtn
                                        = (GraphTreeNode) GraphTreeNode.this.getChildAt(i);
                                if (gtn.getGraphOwner() == n) {
                                    GraphTreeNode.this.removeNode(i);
                                    break;
                                }
                            }
                        }
                    }

                    public void sizeChanged(Graph g, int width, int height) {

                    }

                    public void graphRenamed(Graph g, String name) {

                        // model.nodeChanged(GraphTreeNode.this);
                        GraphTreeNode parent
                                = (GraphTreeNode) GraphTreeNode.this.getParent();
                        int index = parent.getIndex(GraphTreeNode.this);
                        parent.removeNode(index);
                        parent.insertNode(GraphTreeNode.this.getGraphOwner());
                    }
                };
                this.getGraph().addGraphListener(this.gl);
            }
        }

        private GraphTreeNode insertNode(GraphOwner n) {

            int index = 0;
            GraphTreeNode node = new GraphTreeNode(n);
            while ((index < this.getChildCount())
                    && (this.comparator.compare(node, (GraphTreeNode) this
                            .getChildAt(index)) > 0)) {
                index++;
            }

            this.insert(node, index);
            if (ProcTree.this.model != null) {
                ProcTree.this.model.nodesWereInserted(GraphTreeNode.this,
                        new int[]{index});
            }
            ProcTree.this.revalidate();
            return node;
        }

        private void removeNode(int index) {

            GraphTreeNode n = (GraphTreeNode) this.getChildAt(index);
            this.remove(index);
            n.dispose();
            ProcTree.this.model.nodesWereRemoved(GraphTreeNode.this,
                    new int[]{index}, new TreeNode[]{n});
            ProcTree.this.revalidate();
        }

        private void dispose() {

            for (int i = this.getChildCount() - 1; i >= 0; i--) {
                GraphTreeNode gtn = (GraphTreeNode) this.getChildAt(i);
                this.remove(i);
                gtn.dispose();
            }

            if (this.graphOwner != null) {
                this.getGraph().removeGraphListener(this.gl);
                if (ProcTree.this.singleEditor) {
                    GraphEditorFactory.unregister(this.graphOwner);
                }
            }
        }

        public Graph getGraph() {

            return this.graphOwner != null ? this.graphOwner.getOwnedGraph() : null;
        }

        public GraphOwner getGraphOwner() {

            return this.graphOwner;
        }

        @Override
        public String toString() {

            if (this.graphOwner != null) {
                return this.getGraph().getName();
            } else {
                return "<empty>";
            }
        }
    }
}
