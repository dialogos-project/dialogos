package com.clt.diamant.graph.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.MouseInputAdapter;

import com.clt.diamant.Resources;
import com.clt.diamant.graph.Comment;
import com.clt.diamant.graph.GraphSelectionModel;
import com.clt.diamant.graph.VisualGraphElement;
import com.clt.event.DocumentChangeListener;
import com.clt.gui.GUI;
import com.clt.gui.ParentMouseInputListener;

/**
 * UIElement of the DialogOS-Node "Comment".
 *
 * @author Daniel Bobbert
 * @version 1.0
 */
public class CommentUI extends UIElement {

    private static final Color SELECTED_COLOR = new Color(255, 90, 90);

    private JTextArea content;

    private Comment comment;

    private GraphUI owner;

    private GraphSelectionModel selection;

    private CommentContentListener commentContentListener;

    /**
     * Constructor Takes a reference on its associated comment object, and on
     * the GraphUI owner.
     */
    public CommentUI(final Comment comment, final GraphUI owner) {

        this.comment = comment;
        this.owner = owner;
        this.selection = owner.getSelectionModel();

        this.setLayout(new BorderLayout());
        Header header = new Header();
        Footer footer = new Footer();
        this.add(header, BorderLayout.NORTH);
        this.add(footer, BorderLayout.SOUTH);

        this.content = new JTextArea();
        this.content.setLineWrap(true);
        this.content.setWrapStyleWord(true);
        this.content.setOpaque(false);
        this.content.setFont(GUI.getSmallSystemFont());

        this.content.addFocusListener(new FocusListener() {

            public void focusGained(FocusEvent e) {

                CommentUI.this.selection.clear();
                CommentUI.this.getParent().repaint();
            }

            public void focusLost(FocusEvent e) {

            }
        });

        this.add(this.content, BorderLayout.CENTER);
        this.setBorder(new DarkBorder(new Insets(1, 1, 1, 1)));

        this.content.setText(comment.getComment());
        this.setSize(comment.getSize());
        this.setLocation(comment.getLocation());
        this.setBackground(comment.getColor());
        this.doLayout();

        this.commentContentListener = new CommentContentListener();
        comment.addPropertyChangeListener(this.commentContentListener);
        GUI.addDocumentChangeListener(this.content, this.commentContentListener);

        MouseListener colorPopupListener = new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent evt) {

                if (GUI.isPopupTrigger(evt)) {
                    CommentUI.this.showColorPopup(evt.getX(), evt.getY());
                }
            }
        };
        this.addMouseListener(colorPopupListener);
        header.addMouseListener(colorPopupListener);
        footer.addMouseListener(colorPopupListener);
        this.content.addMouseListener(colorPopupListener);
    }

    private class CommentContentListener extends DocumentChangeListener implements
            PropertyChangeListener {

        boolean updating = false;

        @Override
        public void documentChanged(DocumentEvent evt) {

            if (!this.updating) {
                this.updating = true;
                CommentUI.this.comment.setComment(CommentUI.this.content.getText());
                this.updating = false;
            }
        }

        public void propertyChange(PropertyChangeEvent evt) {

            String p = evt.getPropertyName();
            if (p.equals(VisualGraphElement.COMMENT)) {
                if (!this.updating) {
                    CommentUI.this.content.setText((String) evt.getNewValue());
                }
            } else if (p.equals(VisualGraphElement.LOCATION)) {
                CommentUI.this.setLocation((Point) evt.getNewValue());
            } else if (p.equals(VisualGraphElement.SIZE)) {
                CommentUI.this.setSize((Dimension) evt.getNewValue());
                CommentUI.this.validate();
            } else if (p.equals(VisualGraphElement.COLOR)) {
                CommentUI.this.setBackground((Color) evt.getNewValue());
                CommentUI.this.repaint();
            }
        }

    }

    @Override
    public void dispose() {

        this.comment.removePropertyChangeListener(this.commentContentListener);
    }

    @Override
    public Rectangle getVisibleBounds() {

        return this.getBounds();
    }

    private void showColorPopup(int x, int y) {

        boolean selected = false;
        JPopupMenu menu = new JPopupMenu();
        for (String name : GraphUI.gNodeColors.keySet()) {
            final Color c = GraphUI.gNodeColors.get(name);
            JMenuItem item
                    = GraphUI.createColorItem(name, false, new ActionListener() {

                        public void actionPerformed(ActionEvent evt) {

                            CommentUI.this.comment.setColor(c);
                        }
                    });
            if (c.equals(this.comment.getColor())) {
                selected = true;
                item.setSelected(true);
            }
            item.setEnabled(!this.owner.isReadOnly());
            menu.add(item);
        }
        menu.addSeparator();
        JCheckBoxMenuItem item
                = new JCheckBoxMenuItem(Resources.getString("Other") + "...");
        item.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {

                Color c = JColorChooser.showDialog(CommentUI.this, Resources
                        .getString("ChooseColor"), CommentUI.this.comment.getColor());
                if (c != null) {
                    CommentUI.this.comment.setColor(c);
                }
            }
        });
        item.setSelected(!selected);
        menu.add(item);

        menu.show(this, x, y);
    }

    private Color darker(Color c, int level) {

        int diff = 20;
        for (int i = 0; i < level; i++) {
            c
                    = new Color(Math.max(c.getRed() - diff, 0), Math.max(c.getGreen() - diff,
                            0), Math
                                    .max(c.getBlue() - diff, 0), c.getAlpha());
        }
        return c;
    }

    @Override
    public boolean isOpaque() {

        return true;
    }

    private boolean isSelected() {

        return this.selection.contains(this.comment);
    }

    private void propagateEvent(MouseEvent evt) {

        ParentMouseInputListener.propagateEvent(evt);
    }

    private class Header extends JComponent {

        private final int HEIGHT = 12;

        public Header() {

            this.setLayout(null);
            final JComponent closeBox = new JComponent() {

                @Override
                public Dimension getPreferredSize() {

                    return new Dimension(7, 7);
                }

                @Override
                protected void paintComponent(Graphics g) {

                    if (CommentUI.this.content.hasFocus() || CommentUI.this.isSelected()) {
                        Color color = CommentUI.this.comment.getColor();

                        if (this.isOpaque()) {
                            g.setColor(CommentUI.this.darker(color, 2));
                            g.fillRect(0, 0, this.getWidth(), this.getHeight());
                        } else {
                            g.setColor(CommentUI.this.darker(color, 0));
                            g.fillRect(0, 0, this.getWidth(), this.getHeight());
                            g.setColor(CommentUI.this.darker(color, 2));
                            g.fillRect(1, 0, 3, 2);
                            g.fillRect(0, 1, 2, 3);
                            g.fillRect(5, 0, 2, 6);
                            g.fillRect(0, 5, 6, 2);
                            g.setColor(CommentUI.this.darker(color, 3));
                            g.fillRect(1, 1, 1, 1);
                            g.fillRect(5, 5, 1, 1);
                        }
                    }
                }
            };
            closeBox.setOpaque(false);
            this.add(closeBox);
            closeBox.setSize(closeBox.getPreferredSize());
            closeBox.setLocation(4, 3);
            GUI.addMouseInputListener(closeBox, new MouseInputAdapter() {

                boolean closing = false;

                @Override
                public void mouseReleased(MouseEvent evt) {

                    if (!this.closing) {
                        CommentUI.this.propagateEvent(evt);
                        CommentUI.this.selection.add(CommentUI.this.comment);
                    } else {
                        if (closeBox.isOpaque()) {
                            CommentUI.this.owner.deleteElements(Collections
                                    .singleton(CommentUI.this.comment));
                        }
                        closeBox.repaint();
                    }
                }

                @Override
                public void mousePressed(MouseEvent evt) {

                    if (CommentUI.this.content.hasFocus() || CommentUI.this.isSelected()) {
                        this.closing = true;
                        closeBox.setOpaque(true);
                        closeBox.repaint();
                    } else {
                        this.closing = false;
                        CommentUI.this.propagateEvent(evt);
                    }
                }

                @Override
                public void mouseDragged(MouseEvent evt) {

                    if (!this.closing) {
                        CommentUI.this.propagateEvent(evt);
                    } else {
                        closeBox.setOpaque(closeBox.contains(evt.getX(), evt.getY()));
                        closeBox.repaint();
                    }
                }
            });
            this.setBorder(new DarkBorder(new Insets(0, 0, 1, 0)));

            GUI.addMouseInputListener(this, new MouseInputAdapter() {

                @Override
                public void mousePressed(MouseEvent evt) {

                    CommentUI.this.propagateEvent(evt);
                }

                @Override
                public void mouseReleased(MouseEvent evt) {

                    CommentUI.this.propagateEvent(evt);
                }

                @Override
                public void mouseDragged(MouseEvent evt) {

                    CommentUI.this.propagateEvent(evt);
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {

            if (CommentUI.this.isSelected() || CommentUI.this.content.hasFocus()) {
                g.setColor(CommentUI.this.darker(CommentUI.this.comment.getColor(), 1));
            } else {
                g.setColor(CommentUI.this.darker(CommentUI.this.comment.getColor(), 0));
            }
            g.fillRect(0, 0, this.getWidth(), this.getHeight());
        }

        @Override
        public Dimension getMaximumSize() {

            return new Dimension(Integer.MAX_VALUE, this.HEIGHT);
        }

        @Override
        public Dimension getMinimumSize() {

            return new Dimension(this.HEIGHT, this.HEIGHT);
        }

        @Override
        public Dimension getPreferredSize() {

            return this.getMinimumSize();
        }

        @Override
        public boolean isOpaque() {

            return true;
        }
    }

    private class Footer extends JComponent {

        public Footer() {

            this.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            JComponent resizeBox = new JComponent() {

                @Override
                public Dimension getPreferredSize() {

                    return new Dimension(8, 8);
                }

                @Override
                protected void paintComponent(Graphics g) {

                    if (CommentUI.this.isSelected()) {
                        g.setColor(CommentUI.this.darker(CommentUI.this.comment.getColor(),
                                3));
                        int width = this.getWidth();
                        int height = this.getHeight();
                        g.drawLine(width / 2, 0, width - 1, 0);
                        g.drawLine(width - 1, 1, width - 1, height - 1);
                        g.drawLine(width - 2, height - 1, 0, height - 1);
                        g.drawLine(0, height - 2, 0, height / 2);
                        g.drawLine(1, height / 2, width / 2, height / 2);
                        g.drawLine(width / 2, height / 2 - 1, width / 2, 1);
                    }
                }
            };
            resizeBox.setSize(resizeBox.getPreferredSize());
            this.add(resizeBox);
            GUI.addMouseInputListener(resizeBox, new MouseInputAdapter() {

                Point dragStart = null;

                @Override
                public void mousePressed(MouseEvent evt) {

                    if (CommentUI.this.isSelected()) {
                        this.dragStart = evt.getPoint();
                    } else {
                        CommentUI.this.propagateEvent(evt);
                    }
                }

                @Override
                public void mouseReleased(MouseEvent evt) {

                    if (this.dragStart != null) {
                        this.dragStart = null;
                    } else {
                        CommentUI.this.propagateEvent(evt);
                    }
                }

                @Override
                public void mouseDragged(MouseEvent evt) {

                    if (this.dragStart != null) {
                        int ex = evt.getX() - this.dragStart.x;
                        int ey = evt.getY() - this.dragStart.y;

                        CommentUI.this.comment.setSize(Math.max(CommentUI.this.getWidth()
                                + ex, 40), Math.max(
                                        CommentUI.this.getHeight() + ey, 40));
                    } else {
                        CommentUI.this.propagateEvent(evt);
                    }
                }

            });
        }

        @Override
        public boolean isOpaque() {

            return false;
        }
    }

    class DarkBorder implements Border {

        Insets insets;

        public DarkBorder(Insets insets) {

            this.insets = insets;
        }

        public Insets getBorderInsets(Component c) {

            return this.insets;
        }

        public void paintBorder(Component c, Graphics g, int x, int y, int width,
                int height) {

            if (CommentUI.this.isSelected()) {
                g.setColor(CommentUI.SELECTED_COLOR);
            } else {
                g.setColor(CommentUI.this.darker(CommentUI.this.comment.getColor(), 3));
            }
            g.fillRect(0, 0, this.insets.left, height);
            g.fillRect(width - this.insets.right, 0, this.insets.right, height);
            g.fillRect(this.insets.left, 0, width
                    - (this.insets.left + this.insets.right), this.insets.top);
            g.fillRect(this.insets.left, height - this.insets.bottom, width
                    - (this.insets.left + this.insets.right),
                    this.insets.bottom);
        }

        public boolean isBorderOpaque() {

            return true;
        }
    }
}
