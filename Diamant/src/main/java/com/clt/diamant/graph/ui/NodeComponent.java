package com.clt.diamant.graph.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import com.clt.diamant.Preferences;
import com.clt.diamant.Version;
import com.clt.diamant.graph.Node;
import com.clt.gui.GUI;
import com.clt.gui.Images;
import com.clt.gui.ParentMouseInputListener;
import com.clt.gui.border.FrameBorder;

public class NodeComponent<NodeType extends Node> extends JComponent implements PropertyChangeListener {

    private GraphUI graph;
    private JTextPane label;
    private NodeType n;

    private static AttributeSet centerAttributes;
    static {
        centerAttributes = new SimpleAttributeSet();
        ((SimpleAttributeSet) centerAttributes).addAttribute(StyleConstants.Alignment, StyleConstants.ALIGN_CENTER);
    }

    private static Map<String, Icon> iconCache = new HashMap<String, Icon>();

    public NodeComponent(GraphUI graph, final NodeType n) {
        this.setLayout(new GridLayout(1, 1));

        final JPanel c = new JPanel(new GridBagLayout());
        c.setOpaque(false);
        final GridBagConstraints gbc = new GridBagConstraints();

        this.graph = graph;
        this.n = n;

        gbc.gridy = gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;

        Icon im = NodeComponent.getNodeIcon(n);

        JLabel type;
        if (im != null) {
            type = new JLabel(im, SwingConstants.CENTER);
        } else {
            type = new JLabel(Node.getLocalizedNodeTypeName(n), SwingConstants.CENTER);
        }
        // type.setFont(GUI.getSmallSystemFont());
        type.setFont(GUI.getTinySystemFont());
        c.add(type, gbc);

        gbc.gridy++;
        gbc.weighty = 0.0;
        this.label = new JTextPane();
        this.label.setText(" ");
        this.label.setSize(new Dimension(70, 20));
        this.label.setParagraphAttributes(centerAttributes, true);
        c.add(this.label, gbc);
        c.setBorder(BorderFactory.createEmptyBorder(2, 2, 1, 2));
        this.add(c);
        this.setBorder(new FrameBorder(FrameBorder.Type.SQUARE) {
            @Override
            protected Color getForegroundColor(Component c) {
                return c.getBackground().darker().darker().darker();
            }
        });

        this.init();
        this.setDoubleBuffered(true);

        this.label.registerKeyboardAction(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                NodeComponent.this.label.setText(n.getTitle());
                NodeComponent.this.hideEditor(false);
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_FOCUSED);
        this.label.registerKeyboardAction(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                NodeComponent.this.hideEditor(true);
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_FOCUSED);
        this.label.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {

                NodeComponent.this.hideEditor(true);
            }
        });

        GUI.addMouseInputListener(this.label, new MouseInputListener() {

            boolean showEditor = false;

            public void mouseClicked(MouseEvent evt) {

                if (this.showEditor && !NodeComponent.this.label.isEnabled()
                        && !GUI.isPopupTrigger(evt)) {
                    new Thread(new Runnable() {

                        public void run() {

                            try {
                                Thread.sleep(1000);
                            } catch (Exception ignore) {
                            }
                            if (showEditor) {
                                showEditor = false;
                                NodeComponent.this.showEditor();
                            }
                        }
                    }).start();
                } else {
                    this.showEditor = false;
                }

                if (!NodeComponent.this.label.isEnabled()) {
                    ParentMouseInputListener.propagateEvent(evt, NodeComponent.this
                            .getParent());
                }
            }

            public void mousePressed(MouseEvent evt) {

                this.showEditor
                        = (evt.getClickCount() == 1) && !GUI.isPopupTrigger(evt);
                if (!NodeComponent.this.label.isEnabled()) {
                    ParentMouseInputListener.propagateEvent(evt, NodeComponent.this
                            .getParent());
                }
            }

            public void mouseReleased(MouseEvent evt) {

                if (!NodeComponent.this.label.isEnabled()) {
                    ParentMouseInputListener.propagateEvent(evt, NodeComponent.this
                            .getParent());
                }
            }

            public void mouseEntered(MouseEvent evt) {

                if (!NodeComponent.this.label.isEnabled()) {
                    ParentMouseInputListener.propagateEvent(evt, NodeComponent.this
                            .getParent());
                }
            }

            public void mouseExited(MouseEvent evt) {

                if (!NodeComponent.this.label.isEnabled()) {
                    ParentMouseInputListener.propagateEvent(evt, NodeComponent.this
                            .getParent());
                }
            }

            public void mouseDragged(MouseEvent evt) {

                if (!NodeComponent.this.label.isEnabled()) {
                    ParentMouseInputListener.propagateEvent(evt, NodeComponent.this
                            .getParent());
                }
            }

            public void mouseMoved(MouseEvent evt) {

                if (!NodeComponent.this.label.isEnabled()) {
                    ParentMouseInputListener.propagateEvent(evt, NodeComponent.this
                            .getParent());
                }
            }
        });
    }

    private void hideEditor(boolean acceptChanges) {
        synchronized (this.label) {
            if (!this.label.isEnabled()) {
                return;
            }
            
            this.label.setOpaque(false);
            this.label.setEditable(false);
            this.label.setEnabled(false);
            this.label.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
                        
            if (!this.n.getTitle().equals(this.label.getText()) && acceptChanges) {
                this.graph.renameNode(this.n, this.label.getText());
            }
            
            this.setLabel(this.n.getTitle());
        }
    }

    private void showEditor() {
        synchronized (this.label) {
            if (this.label.isEnabled()) {
                return;
            }
            this.label.setEnabled(true);
            this.label.setEditable(true);
            this.label.setOpaque(true);
            this.label.setBackground(Color.WHITE);
            this.label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.BLACK, 1), 
                    BorderFactory.createLineBorder(label.getBackground(), 1)));
            this.label.setText(this.n.getTitle());
            this.label.selectAll();
            this.label.requestFocus();
        }
    }

    @Override
    public void setFont(Font font) {
        super.setFont(font);
        this.label.setFont(font);
    }

    public static Icon getNodeIcon(Node n) {
        return NodeComponent.getNodeIcon(n.getClass());
    }

    public static Icon getNodeIcon(Class<?> c) {
        if (c == null) {
            return null;
        }

        Icon im = NodeComponent.iconCache.get(c.getName());
        if (im != null) {
            return im;
        } else {
            String path = c.getName().replace('.', '/');
            URL url = ClassLoader.getSystemResource(path + ".png");
            if (url == null) {
                url = ClassLoader.getSystemResource(path + ".jpg");
            }
            if (url == null) {
                url = ClassLoader.getSystemResource(path + ".gif");
            }

            if (url != null) {
                im = new ImageIcon(url);
            } else {
                String name = c.getName();
                // cut off package name
                name = name.substring(name.lastIndexOf('.') + 1);
                if (name.endsWith("Node")) {
                    name = name.substring(0, name.length() - 4);
                }

                im = NodeComponent.getNodeIcon(name);
            }

            NodeComponent.iconCache.put(c.getName(), im);
            return im;
        }
    }

    private static Icon getNodeIcon(String name) {
        Icon im = null;

        if (im == null) {
            im = NodeComponent.getNodeIcon(name, "Node.png");
        }
        if (im == null) {
            im = NodeComponent.getNodeIcon(name, ".png");
        }
        if (im == null) {
            im = NodeComponent.getNodeIcon(name, "Node.gif");
        }
        if (im == null) {
            im = NodeComponent.getNodeIcon(name, ".gif");
        }
        if (im == null) {
            im = NodeComponent.getNodeIcon(name, "Node.jpg");
        }
        if (im == null) {
            im = NodeComponent.getNodeIcon(name, ".jpg");
        }

        return im;
    }

    private static Icon getNodeIcon(String name, String suffix) {
        try {
            return Images.load("nodes/" + name + suffix);
        } catch (Exception ignore) {
        }
        return null;
    }

    private void init() {        
        this.setForeground(Color.BLACK);
        
        Color c = this.n.getColor();
        if (c != null) {
            this.setBackColor(c);
        }
        
        this.hideEditor(false);
        
        String s = this.n.getTitle();
        if (s != null) {
            this.setLabel(s);
        }
        
        this.n.addPropertyChangeListener(this);
    }

    public void dispose() {
        this.n.removePropertyChangeListener(this);
    }

    private void setBackColor(Color c) {
        // make nodes slightly transparent, if transparency is enabled
        // This will slow down node drawing considerably.
        if (Preferences.getPrefs().useTransparency.getValue()) {
            c = new Color(c.getRed(), c.getGreen(), c.getBlue(), 238);
        }
        this.setBackground(c);
        // use NTSC formula to determine "brightness"
        int gray
                = (int) (0.30 * c.getRed() + 0.59 * c.getGreen() + 0.11 * c.getBlue());
        if (gray < 96) {
            this.label.setDisabledTextColor(Color.WHITE);
        } else {
            this.label.setDisabledTextColor(Color.BLACK);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Insets insets = this.getInsets();

        g.setColor(this.getBackground());
        g.fillRect(insets.left, insets.top, this.getWidth() - insets.left
                - insets.right, this.getHeight()
                - insets.top - insets.bottom);

        if (this.getNode().isBreakpoint()) {
            int offset = 1;
            int size = 5;

            g.setColor(Color.WHITE);
            g.fillOval(insets.left + offset, insets.top + offset, size + 1, size + 1);
            g.setColor(GUI.slightlyDarker(Color.RED));
            g.fillOval(insets.left + offset, insets.top + offset, size, size);
            g.setColor(Color.BLACK);
            g.drawOval(insets.left + offset, insets.top + offset, size, size);
        }
    }

    @Override
    protected void paintChildren(Graphics g) {
        if (Version.HICOLOR) {
            // we do this in paintChildren instead of paintComponent in order
            // to include the (already drawn) border in our gradient
            NodeComponent.paintGradient(g, 1, 1, this.getWidth() - 2, this
                    .getHeight() - 2);
        }
        super.paintChildren(g);
    }

    public static void paintGradient(Graphics g, int x, int y, int width,
            int height) {
        int fill = 255;

        int mid = height / 2 - 1;

        for (int i = 0; i < height; i++) {
            int opacity = 0;
            if (i < mid) {
                opacity = 40 + (120 * (mid - i)) / mid;
            } else {
                opacity = (160 * (i - mid)) / (height - mid);
            }
            Color c = new Color(fill, fill, fill, opacity);
            g.setColor(c);
            g.drawLine(x, y + i, x + width - 1, y + i);
        }
    }

    public NodeType getNode() {
        return this.n;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("color")) {
            this.setBackColor((Color) evt.getNewValue());
        } else if (evt.getPropertyName().equals("title")) {
            this.setLabel((String) evt.getNewValue());
        }
    }

    private void setLabel(String s) {
        Dimension oldPrefsize = getPreferredSize();
        
        if (s.length() <= 16) {
            label.setText(s);
        } else {
            label.setText(s.substring(0, 13) + "...");
        }
        
        label.setCaretPosition(0);
        invalidate();
        
        // For some reason that I (AK) don't understand, changing the node label
        // sometimes (not always) increases the preferred height of the Swing component. 
        // This caused nodes to be moved down when handling componentResized events in
        // the NodeUI class, causing issue #134.
        // 
        // Forcing the height of the Swing component to
        // remain as it was before the label was modified fixes this problem.
        Dimension newPreferredSize = new Dimension(getPreferredSize().width, oldPrefsize.height);
        setPreferredSize(newPreferredSize);
        
        this.setSize(this.getPreferredSize());
    }

    @Override
    public boolean isOpaque() {
        return false;
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension pref = super.getPreferredSize();
        Dimension min = this.getMinimumSize();
        return new Dimension(Math.max(pref.width, min.width), Math.max(pref.height,
                min.height));
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(60, 40);
    }
}
