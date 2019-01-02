package com.clt.diamant.graph.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.Box;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.MouseInputListener;

import com.clt.diamant.Preferences;
import com.clt.diamant.Version;
import com.clt.diamant.graph.Edge;
import com.clt.diamant.graph.Node;
import com.clt.gui.GUI;

public class NodeUI<N extends Node> extends UIElement implements PropertyChangeListener {

    private static final int BORDER_WIDTH = 3;

    private MouseInputListener parent_ml = null;

    private MouseInputListener ml = null;
    private GraphUI graph;

    /**
     * Reference to the associated node.
     */
    private N node = null;
    private NodeComponent<N> body;
    private Map<Edge, EdgeUI> ui = new Hashtable<Edge, EdgeUI>();

    public NodeUI(GraphUI graph, N node, final MouseInputListener scroller) {
        this.setLayout(new GridBagLayout());
        this.setOpaque(false);

        this.setFont(GUI.getSmallSystemFont());

        this.graph = graph;
        this.node = node;

        this.init();

        node.addPropertyChangeListener(this);

        GUI.addMouseInputListener(this, scroller);
        this.parent_ml = new MouseInputListener() {

            public void mouseClicked(MouseEvent e) {

                scroller.mouseClicked(e);
                if (NodeUI.this.ml != null) {
                    NodeUI.this.ml.mouseClicked(e);
                }
            }

            public void mousePressed(MouseEvent e) {

                scroller.mousePressed(e);
                if (NodeUI.this.ml != null) {
                    NodeUI.this.ml.mousePressed(e);
                }
            }

            public void mouseReleased(MouseEvent e) {

                scroller.mouseReleased(e);
                if (NodeUI.this.ml != null) {
                    NodeUI.this.ml.mouseReleased(e);
                }
            }

            public void mouseEntered(MouseEvent e) {

                scroller.mouseEntered(e);
                if (NodeUI.this.ml != null) {
                    NodeUI.this.ml.mouseEntered(e);
                }
            }

            public void mouseExited(MouseEvent e) {

                scroller.mouseExited(e);
                if (NodeUI.this.ml != null) {
                    NodeUI.this.ml.mouseExited(e);
                }
            }

            public void mouseMoved(MouseEvent e) {

                scroller.mouseMoved(e);
                if (NodeUI.this.ml != null) {
                    NodeUI.this.ml.mouseMoved(e);
                }
            }

            public void mouseDragged(MouseEvent e) {

                scroller.mouseDragged(e);
                if (NodeUI.this.ml != null) {
                    NodeUI.this.ml.mouseDragged(e);
                }
            }
        };
        this.setAutoscrolls(true);
    }

    @Override
    public void dispose() {

        this.body.dispose();
        this.node.removePropertyChangeListener(this);
        for (EdgeUI eui : this.ui.values()) {
            eui.dispose();
        }
        this.ui.clear();
    }

    @Override
    public void setLocation(int x, int y) {
        if (this.getParent() != null) {
            x = Math.max(0, Math.min(x, this.getParent().getWidth() - this.getWidth()));
            y = Math.max(0, Math.min(y, this.getParent().getHeight() - this.getHeight()));
        }

        if ((x != this.getX()) || (y != this.getY())) {
            super.setLocation(x, y);
            // __location
            // getNode().setProperty("location", new Point(x + getWidth()/2, y +
            // getHeight()/2));
            this.getNode().setLocation(x, y);

        }
    }

    @Override
    public void setSize(int width, int height) {

        if ((width != this.getWidth()) || (height != this.getHeight())) {
            super.setSize(width, height);
            this.getNode().setSize(width, height);
        }
    }

    protected NodeComponent<N> getBody() {

        return this.body;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String property = evt.getPropertyName();
        
        if (property.equals("numEdges")) {
            this.init();
        } else if (property.equals("active")) {
            boolean active = evt.getNewValue().equals(Boolean.TRUE);
            this.initBorder(active);
            this.firePropertyChange("nodeActive", evt.getOldValue(), evt.getNewValue());
        } else if (property.equals("location")) {
            // __location
            Point p = (Point) evt.getNewValue();
            // setLocation(p.x - getWidth()/2, p.y - getHeight()/2);
            this.setLocation(p);
            this.firePropertyChange("nodeMoved", evt.getOldValue(), evt.getNewValue());
        } else if (property.equals("selected")) {
            if (Preferences.getPrefs().supportMultiView.getValue()) {
                if (this.getParent() != null) {
                    this.getParent().repaint();
                }
            }
        } else if (property.equals("breakpoint")) {
            this.repaint();
        }
    }

    private class BouncingBorder extends LineBorder {

        private final Color HI_COLOR = new Color(150, 190, 245);

        private final Color LO_COLOR = new Color(50, 90, 145);

        final private int STEPS = 10;

        private Timer timer;

        private int step;

        private boolean increase;

        public BouncingBorder() {

            super(Color.black, NodeUI.BORDER_WIDTH);
            final Color origColor = NodeUI.this.body.getBackground();

            NodeUI.this.addPropertyChangeListener(new PropertyChangeListener() {

                public void propertyChange(PropertyChangeEvent evt) {

                    if (evt.getPropertyName().equals("border")
                            && (evt.getNewValue() != BouncingBorder.this)) {
                        BouncingBorder.this.timer.stop();
                        BouncingBorder.this.setColor(origColor);
                        // remove ourselves, otherwise we will stack up
                        // PropertyChangeListeners
                        NodeUI.this.removePropertyChangeListener(this);
                    }
                }
            });
            this.setColor(this.HI_COLOR);
            this.step = 0;
            this.increase = true;
            this.timer = new Timer(80, new ActionListener() {

                public void actionPerformed(ActionEvent evt) {

                    if (NodeUI.this.isShowing()) {
                        BouncingBorder.this.bounce();
                    }
                }
            });
            this.timer.setInitialDelay(500);
            if (Version.ANIMATE) {
                this.timer.start();
            }
        }

        private void bounce() {

            this.setColor(new Color(this.HI_COLOR.getRed()
                    - ((this.HI_COLOR.getRed() - this.LO_COLOR.getRed()) * this.step)
                    / this.STEPS, this.HI_COLOR.getGreen()
                    - ((this.HI_COLOR.getGreen() - this.LO_COLOR.getGreen()) * this.step)
                    / this.STEPS, this.HI_COLOR
                            .getBlue()
                    - ((this.HI_COLOR.getBlue() - this.LO_COLOR.getBlue()) * this.step)
                    / this.STEPS));
            if (this.increase) {
                this.step++;
                if (this.step == this.STEPS) {
                    this.increase = false;
                }
            } else {
                this.step--;
                if (this.step == 0) {
                    this.increase = true;
                }
            }
        }

        private void setColor(Color c) {

            // direkte Manipulation der geerbten Instanzvariable "lineColor" von
            // LineBorder
            this.lineColor = c;
            NodeUI.this.body.setBackground(c);
            NodeUI.this.repaint();
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            super.paintBorder(c, g, x, y, width, height);
        }
    }

    protected void init() {
        this.removeAll();

        for (EdgeUI eui : this.ui.values()) {
            eui.dispose();
        }
        this.ui.clear();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gbc.gridy = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;

        /*
     * // in edges add(new EdgeUI(this, new Edge(getNode())), gbc); gbc.gridy++;
         */
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        this.body = createBody(this.graph, this.getNode());
        this.add(this.body, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        this.add(Box.createHorizontalGlue(), gbc);

        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        this.add(Box.createHorizontalStrut(10), gbc);

        for (Edge edge : this.node.edges()) {
            gbc.gridx++;
            EdgeUI eui = new EdgeUI(this, edge);
            this.ui.put(edge, eui);
            this.add(eui, gbc);
            gbc.gridx++;
            this.add(Box.createHorizontalStrut(10), gbc);
        }

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        this.add(Box.createHorizontalGlue(), gbc);

        this.initBorder(this.node.isActive());

        this.setSize(this.getPreferredSize());
        this.revalidate();

        this.body.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int w = NodeUI.this.getWidth();
                int h = NodeUI.this.getHeight();

                int oldY = NodeUI.this.getY();

                NodeUI.this.setSize(NodeUI.this.getPreferredSize());
                NodeUI.this.setLocation(
                        NodeUI.this.getX() + (w - NodeUI.this.getWidth()) / 2,
                        NodeUI.this.getY() + (h - NodeUI.this.getHeight()) / 2);
                NodeUI.this.revalidate();
            }
        });
    }

    protected NodeComponent<N> createBody(GraphUI graph, N node) {
        NodeComponent<N> body = new NodeComponent<N>(graph, node);

        body.setFont(this.getFont());
        return body;
    }

    protected void initBorder(boolean nodeActive) {

        if (nodeActive) {
            this.setBorder(new BouncingBorder());
        } else {
            this.setBorder(this.createDefaultBorder());
        }
    }

    protected Border createDefaultBorder() {

        return new Border() {

            public void paintBorder(Component c, Graphics g, int x, int y, int width,
                    int height) {

                int d
                        = NodeUI.this.node.getSelectionDistance(NodeUI.this.graph.getSelectionModel(), 1);
                if (Preferences.getPrefs().showSelectionNeighbours.getValue() ? d <= 1
                        : d == 0) {
                    Color color;
                    if (d == 0) {
                        color = Preferences.getPrefs().selectionColor.getValue();
                    } else {
                        color = Preferences.getPrefs().neighbourColor.getValue();
                    }
                    color = new Color(color.getRed(), color.getGreen(), color.getBlue(), 238);
                    g.setColor(color);
                    int n = Math.min(NodeUI.BORDER_WIDTH, 2);
                    for (int i = 0; i < n; i++) {
                        g.drawRect(x + i, y + i, width - 2 * i - 1, height - 2 * i - 1);
                    }
                    g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 60));
                    g.fillRect(x + n, y + n, width - 2 * n, height - 2 * n);
                }
            }

            public Insets getBorderInsets(Component c) {

                return new Insets(NodeUI.BORDER_WIDTH, NodeUI.BORDER_WIDTH,
                        NodeUI.BORDER_WIDTH, NodeUI.BORDER_WIDTH);
            }

            public boolean isBorderOpaque() {

                return false;
            }
        };
    }

    public N getNode() {

        return this.node;
    }

    @Override
    public void revalidate() {

        super.revalidate();
        if (this.getParent() != null) {
            this.getParent().repaint();
        }
    }

    public void setMouseInputListener(MouseInputListener m) {

        if (this.ml != null) {
            this.removeMouseListener(this.ml);
            this.removeMouseMotionListener(this.ml);
        }
        this.ml = m;
        this.addMouseListener(this.ml);
        this.addMouseMotionListener(this.ml);
    }

    public MouseInputListener getMouseInputListener() {

        return this.parent_ml;
    }

    public EdgeUI getEdgeUI(Edge edge) {

        return this.ui.get(edge);
    }

    @Override
    public Rectangle getVisibleBounds() {

        return GUI.getRelativeBounds(this.body, this.getParent());
    }

    public Point getNodeInput() {

        Rectangle bounds = GUI.getRelativeBounds(this.body, this.getParent());
        return new Point(bounds.x + bounds.width / 2, bounds.y);
    }

    public Point getOutput(Edge edge, Container parent) {

        EdgeUI ui = this.getEdgeUI(edge);
        if (ui == null) {
            System.err.println("Edge " + edge + " is not an outgoing edge of node "
                    + this.getNode()
                    + ". edge.getSource() = " + edge.getSource());
            System.err.println("Edges are:");
            for (Edge e : edge.getSource().edges()) {
                System.err.println("  " + e);
            }
            System.err.flush();
            throw new IllegalArgumentException("Not owner of edge");
        }
        return ui.getOutputRelativeTo(parent);
    }

    @Override
    public String toString() {
        String id = getClass().getName() + '@' + Integer.toHexString(hashCode());
        return id + "::" + super.toString();
    }

    
}
