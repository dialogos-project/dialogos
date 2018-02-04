package com.clt.diamant.log;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.clt.diamant.Resources;
import com.clt.diamant.graph.Edge;
import com.clt.diamant.graph.GraphOwner;
import com.clt.diamant.graph.Node;
import com.clt.diamant.graph.nodes.InputNode;
import com.clt.diamant.graph.nodes.OutputNode;
import com.clt.diamant.gui.GraphEditorFactory;
import com.clt.diamant.gui.LogPlayerWindow;
import com.clt.gui.GUI;
import com.clt.gui.OptionPane;
import com.clt.util.StringTools;

/**
 * @author dabo
 *
 */
public class ExecutionTree extends JTree {

    public ExecutionTree(TreeModel model, final LogPlayerWindow window) {

        super(model);

        this.setRootVisible(false);
        this.setShowsRootHandles(true);
        this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        this.addTreeSelectionListener(new TreeSelectionListener() {

            public void valueChanged(TreeSelectionEvent evt) {

                TreePath path = evt.getPath();
                Object o = path.getLastPathComponent();
                if (!(o instanceof ParentNode)) {
                    OptionPane.error(window, "Illegal selection!");
                } else {
                    Node n = ((ParentNode) o).getNode();
                    if (n.getGraph() == null) {
                        OptionPane.error(ExecutionTree.this,
                                "This node was deleted from the model.");
                        return;
                    }

                    GraphOwner owner = n.getGraph().getOwner();

                    GraphEditorFactory.show(owner);
                    n.getGraph().activateNode(n);

                    int inputs = ((ParentNode) o).getInputs();
                    int outputs = ((ParentNode) o).getOutputs();

                    Edge e = ((ParentNode) o).getActiveEdge();
                    long time = ((ParentNode) o).getTime();
                    Font f = GUI.getSmallSystemFont();
                    window.setInfo("<html>"
                            + "<head><style type=text/css>\n"
                            + "<!--\n"
                            + "body { font-size:"
                            + f.getSize()
                            + "pt; font-family:"
                            + f.getName()
                            + "; }\n"
                            + "p { font-size:"
                            + f.getSize()
                            + "pt; font-family:"
                            + f.getName()
                            + "; margin-top:4pt; margin-bottom:4pt; }\n"
                            + "li { font-size:"
                            + f.getSize()
                            + "pt; font-family:"
                            + f.getName()
                            + "; }\n"
                            + "td { font-size:"
                            + f.getSize()
                            + "pt; font-family:"
                            + f.getName()
                            + "; white-space:nowrap; }\n"
                            + "-->\n"
                            + "</style></head>"
                            + "<body>"
                            + "<p><b>"
                            + StringTools.toHTML(Resources.getString("Time"))
                            + " : </b>"
                            + TimeLine.timeString(time / 1000)
                            + // String.valueOf(time/60000) + ':' +
                            // String.valueOf(time/1000%60/10) +
                            // String.valueOf(time/1000%10) +
                            " ("
                            + time
                            + "ms)</p>\n"
                            + (o instanceof LeafNode ? ""
                                    : ("<p><b>"
                                    + StringTools.toHTML(Resources
                                            .getString("Turns"))
                                    + " :</b>\n"
                                    + "<blockquote>"
                                    + outputs
                                    + ' '
                                    + StringTools.toHTML(Resources
                                            .getString("systemPrompts"))
                                    + "<br>\n"
                                    + inputs
                                    + ' '
                                    + StringTools.toHTML(Resources
                                            .getString("userInputs"))
                                    + "\n"
                                    + "</blockquote></p>\n"
                                    + "<p><b>"
                                    + StringTools.toHTML(Resources
                                            .getString("Duration"))
                                    + " : </b>"
                                    + TimeLine
                                            .timeString(((ParentNode) o)
                                                    .getDuration() / 1000)
                                    + " ("
                                    + ((ParentNode) o).getDuration()
                                    + "ms)</tt></p>\n" + "</p>\n"))
                            + n.getDescription(e)
                            + "</body></html>");

                    n.getGraph().activateNode(n);

                    // dabo; n.getGraph().getUI().selectEdge(e);
                }
            }
        });
    }

    public static class ParentNode
            implements TreeNode {

        protected ParentNode parent;
        protected LogEvent<?> event;
        protected Node node = null;

        private List<TreeNode> children = new ArrayList<TreeNode>();
        private long time = 0;
        private long duration = 0;

        public ParentNode(ParentNode parent, Node n, long time) {

            this(parent, n, time, null);
        }

        public ParentNode(ParentNode parent, Node n, long time, LogEvent<?> evt) {

            this(parent, n, time, true, evt);
        }

        protected ParentNode(ParentNode parent, Node n, long time, boolean dst,
                LogEvent<?> evt) {

            this.parent = parent;
            this.node = n;
            this.time = time;
            this.event = evt;
            this.init(dst);
        }

        void init(boolean dst) {

            if (this.parent != null) {
                this.parent.add(this);
            }
        }

        public TreeNode getChildAt(int childIndex) {

            return this.children.get(childIndex);
        }

        public int getChildCount() {

            return this.children.size();
        }

        public TreeNode getParent() {

            return this.parent;
        }

        public int getIndex(TreeNode node) {

            return this.children.indexOf(node);
        }

        public boolean getAllowsChildren() {

            return !this.isLeaf();
        }

        public boolean isLeaf() {

            return false;
        }

        public Enumeration<TreeNode> children() {

            return Collections.enumeration(this.children);
        }

        void add(TreeNode n) {

            if (this.getAllowsChildren()) {
                this.children.add(n);
            }
        }

        Node getNode() {

            return this.node;
        }

        long getTime() {

            return this.time;
        }

        long getDuration() {

            return this.duration;
        }

        void setDuration(long duration) {

            this.duration = duration;
        }

        public Edge getActiveEdge() {

            if (this.parent == null) {
                return null;
            }
            int index = this.parent.getIndex(this);
            if (index >= this.parent.getChildCount() - 1) {
                return null;
            }

            Node n = ((ParentNode) this.parent.getChildAt(index + 1)).getNode();
            for (int i = 0; i < this.node.numEdges(); i++) {
                Edge e = this.node.getEdge(i);
                if (e.getTarget() == n) {
                    return e;
                }
            }
            return null;
        }

        @Override
        public String toString() {

            if (this.node == null) {
                return "Execution";
            } else {
                String cls = this.node.getClass().getName();
                if (cls.endsWith("Node")) {
                    cls = cls.substring(0, cls.length() - 4);
                }
                int index = cls.lastIndexOf('.');
                if (index >= 0) {
                    cls = cls.substring(index + 1);
                }
                return cls + ": " + this.node.getTitle();
            }
        }

        public final TreePath findEvent(LogEvent<?> e) {

            return this.findEvent(e, null);
        }

        public TreePath findEvent(LogEvent<?> e, TreePath prefix) {

            TreePath newPrefix;
            if (prefix != null) {
                newPrefix = prefix.pathByAddingChild(this);
            } else {
                newPrefix = new TreePath(this);
            }

            if (e == this.event) {
                return newPrefix;
            }

            for (int i = 0; i < this.getChildCount(); i++) {
                TreePath p = ((ParentNode) this.getChildAt(i)).findEvent(e, newPrefix);
                if (p != null) {
                    return p;
                }
            }
            return null;
        }

        public final TreePath findTime(long time) {

            return this.findTime(time, null);
        }

        public TreePath findTime(long time, TreePath prefix) {

            TreePath newPrefix;
            if (prefix != null) {
                newPrefix = prefix.pathByAddingChild(this);
            } else {
                newPrefix = new TreePath(this);
            }

            for (int i = this.getChildCount() - 1; i >= 0; i--) {
                TreePath p = ((ParentNode) this.getChildAt(i)).findTime(time, newPrefix);
                if (p != null) {
                    return p;
                }
            }

            if (this.getTime() <= time) {
                return newPrefix;
            } else {
                return null;
            }
        }

        public int getInputs() {

            int n = 0;
            for (int i = 0; i < this.getChildCount(); i++) {
                n += ((ParentNode) this.getChildAt(i)).getInputs();
            }
            return n;
        }

        public int getOutputs() {

            int n = 0;
            for (int i = 0; i < this.getChildCount(); i++) {
                n += ((ParentNode) this.getChildAt(i)).getOutputs();
            }
            return n;
        }
    }

    static class LeafNode
            extends ParentNode {

        public LeafNode(ParentNode parent, Node n, long time, boolean dst,
                LogEvent<?> evt) {

            super(parent, n, time, dst, evt);
        }

        @Override
        void init(boolean dst) {

            if (this.parent != null) {
                int children = this.parent.getChildCount();
                if ((children == 0) || dst) {
                    this.parent.add(this);
                } else {
                    TreeNode lastChild = this.parent.getChildAt(children - 1);
                    if (lastChild instanceof LeafNode) {
                        Node prevNode = ((LeafNode) lastChild).getNode();
                        if (prevNode != this.node) {
                            this.parent.add(this);
                        }
                    } else {
                        this.parent.add(this);
                    }
                }
            }
        }

        @Override
        public boolean isLeaf() {

            return true;
        }

        @Override
        public TreePath findEvent(LogEvent<?> e, TreePath prefix) {

            if (e == this.event) {
                return prefix.pathByAddingChild(this);
            } else {
                return null;
            }
        }

        @Override
        public TreePath findTime(long time, TreePath prefix) {

            if (this.getTime() <= time) {
                return prefix.pathByAddingChild(this);
            } else {
                return null;
            }
        }

        @Override
        public int getInputs() {

            return (this.getNode() instanceof InputNode) ? 1 : 0;
        }

        @Override
        public int getOutputs() {

            Node n = this.getNode();
            if (n instanceof OutputNode) {
                return ((OutputNode) n).hasOutput() ? 1 : 0;
            } else {
                return 0;
            }
        }
    }
}
