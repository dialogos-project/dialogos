package com.clt.diamant.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.clt.diamant.Resources;
import com.clt.diamant.graph.Node;
import com.clt.diamant.graph.ui.NodeComponent;
import com.clt.diamant.graph.ui.NodeTransferable;
import com.clt.gui.Images;
import com.clt.properties.BooleanProperty;

/**
 * The toolbox shown on the right of the DialogOS main window,
 * from which nodes can be dragged into the graph.
 * 
 * @author Daniel Bobbert
 *
 */
public class NodeToolbox extends Toolbox {

    private static final boolean macStyle = false;

    private BooleanProperty structured;

    private ChangeListener changeListener = new ChangeListener() {

        public void stateChanged(ChangeEvent e) {

            NodeToolbox.this.initToolbox();
            NodeToolbox.this.update();
        }
    };

    public NodeToolbox(BooleanProperty structured) {

        super(SwingConstants.VERTICAL);

        this.setName(Resources.getString("Nodes"));
        this.setFloatable(false);
        // setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

        this.structured = structured;

        if (NodeToolbox.macStyle) {
            this.setBackground(new Color(213, 220, 228));
        }
    }

    @Override
    public void addNotify() {

        super.addNotify();

        this.structured.addChangeListener(this.changeListener);
        this.initToolbox();
    }

    @Override
    public void removeNotify() {

        this.structured.removeChangeListener(this.changeListener);

        super.removeNotify();
    }

    private void initToolbox() {
        this.removeAll();

        Map<Object, List<Class<Node>>> allNodeTypes = Node.getAvailableNodeTypes();
        
        for (Iterator<Object> keys = allNodeTypes.keySet().iterator(); keys.hasNext();) {
            final Object key = keys.next();
            final List<Class<Node>> nodeTypes = allNodeTypes.get(key);
            
            if (this.structured.getValue()) {
                final GroupHeader header = new GroupHeader(key, nodeTypes);
                this.add(header);
                header.setExpanded(true);
            } else {
                this.insertNodes(nodeTypes, 0, -1);
            }

            if (!this.structured.getValue() && keys.hasNext()) {
                this.addSeparator();
            }
        }
    }

    private void insertNodes(List<Class<Node>> nodeTypes, int inset, int position) {

        for (final Class<Node> nodeType : nodeTypes) {
            if (nodeType == null) {
                this.addSeparator();
            } else {
                final JLabel button = 
                        new JLabel(Node.getLocalizedNodeTypeName(nodeType), 
                                NodeComponent.getNodeIcon(nodeType), 
                                SwingConstants.CENTER) {
                    @Override
                    public String getText() {
                        return Node.getLocalizedNodeTypeName(nodeType);
                    }
                };
                
                button.setHorizontalAlignment(SwingConstants.LEFT);
                button.setHorizontalTextPosition(SwingConstants.RIGHT);
                button.setVerticalTextPosition(SwingConstants.CENTER);
                button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                button.setBorder(BorderFactory.createEmptyBorder(3, 6 + inset, 3, 6));

                // need to wrap into a panel in order to appease the toolbar's BoxLayout
                JPanel p = new JPanel(new BorderLayout(0, 0)) {
                    @Override
                    public Dimension getMaximumSize() {
                        return new Dimension(super.getMaximumSize().width, this.getPreferredSize().height);
                    }
                };
                p.setOpaque(false);
                p.add(button);
                this.add(p, position);
                // add(button, position);
                if (position >= 0) {
                    position++;
                }

                DragGestureListener dragGestureListener = new DragGestureListener() {

                    public void dragGestureRecognized(DragGestureEvent e) {

                        try {
                            e.startDrag(null, new NodeTransferable(nodeType.newInstance()),
                                    null);
                        } catch (Exception exn) {
                            // ignore
                        }
                    }
                };
                DragSource dragSource = new DragSource();
                dragSource.createDefaultDragGestureRecognizer(button,
                        DnDConstants.ACTION_COPY,
                        dragGestureListener);
            }
        }
    }

    @Override
    public void notifyState() {

    }

    @Override
    public void update() {

        this.invalidate();
        Container parent = this.getParent();
        if (parent != null) {
            parent.validate();
            parent.repaint();
        }
    }

    private static final ImageIcon iconDisclosureOpened = Images.loadBuiltin("DisclosureDown.png");
    private static final ImageIcon iconDisclosureClosed = Images.loadBuiltin("DisclosureRight.png");

    private class GroupHeader extends JComponent {

        private Color gradientStart = new Color(173, 187, 208);
        private Color gradientEnd = new Color(152, 170, 196);
        private Color gradientBorder = new Color(143, 156, 181);

        private Object key;
        private List<Class<Node>> nodeTypes;
        private boolean open;
        private JLabel text;
        private JLabel icon;

        public GroupHeader(final Object key, List<Class<Node>> nodeTypes) {

            this.key = key;
            this.nodeTypes = nodeTypes;
            this.open = false;
            this.icon = new JLabel(NodeToolbox.iconDisclosureClosed);
            this.text = new JLabel() {

                @Override
                public String getText() {

                    if (key == null) {
                        return null;
                    } else {
                        String s = key.toString();
                        if (NodeToolbox.macStyle) {
                            s = s.toUpperCase();
                        }
                        return s;
                    }
                }
            };
            if (NodeToolbox.macStyle) {
                this.text.setFont(this.text.getFont().deriveFont(Font.BOLD));
                this.text.setForeground(new Color(105, 113, 126));
            }

            this.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            this.setLayout(new BorderLayout(6, 6));
            this.add(this.icon, BorderLayout.WEST);
            this.add(this.text, BorderLayout.CENTER);

            this.icon.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent evt) {

                    GroupHeader.this.setExpanded(!GroupHeader.this.open);
                }
            });
            this.text.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent evt) {

                    if (evt.getClickCount() == 2) {
                        GroupHeader.this.setExpanded(!GroupHeader.this.open);
                    }
                }
            });

            if (!NodeToolbox.macStyle) {
                this.setOpaque(true);
            }
        }

        public Object getKey() {

            return this.key;
        }

        public synchronized void setExpanded(boolean expanded) {

            if (expanded != this.open) {
                int headerPosition = NodeToolbox.this.getComponentIndex(this);
                if (expanded) {
                    this.icon.setIcon(NodeToolbox.iconDisclosureOpened);
                    NodeToolbox.this.insertNodes(this.nodeTypes, 12, headerPosition + 1);
                } else {
                    this.icon.setIcon(NodeToolbox.iconDisclosureClosed);
                    for (int i = 0; i < this.nodeTypes.size(); i++) {
                        NodeToolbox.this.remove(headerPosition + 1);
                    }
                }
                this.open = expanded;
                NodeToolbox.this.update();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (this.isOpaque()) {
                Graphics2D gfx = (Graphics2D) g;
                boolean focused = true;

                int width = NodeToolbox.this.getWidth();
                Container parent = NodeToolbox.this.getParent();
                if (parent != null) {
                    width = Math.min(width, parent.getWidth());
                }

                gfx.setClip(-this.getX(), 0, width, this.getHeight());
                if (focused) {
                    Paint oldPaint = gfx.getPaint();
                    gfx.setPaint(new GradientPaint(0, 0, this.gradientStart, 0, this .getHeight() - 1, this.gradientEnd));
                    gfx.fillRect(-this.getX(), 0, width, this.getHeight() - 1);
                    gfx.setPaint(oldPaint);

                    gfx.setColor(this.gradientBorder);
                    gfx.drawLine(-this.getX(), this.getHeight() - 1, -this.getX() + width - 1, this.getHeight() - 1);
                }
            }

            if (NodeToolbox.macStyle) {
                JLabel shadow = new JLabel(this.text.getText());
                shadow.setFont(this.text.getFont());
                shadow.setForeground(new Color(241, 248, 255));
                shadow.setSize(shadow.getPreferredSize());
                Graphics shadow_g = g.create();
                shadow_g.translate(this.text.getX(), this.text.getY() + 1);
                shadow.paint(shadow_g);
                shadow_g.dispose();
            }
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(super.getMaximumSize().width, this.getPreferredSize().height);
        }
    }
}
