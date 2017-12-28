package com.clt.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.tree.TreeNode;

import com.clt.util.PropertyContainer;

public class TreeView
    extends JPanel {

  private static final int ipadx = 15, ipady = 45;

  private TreeNode root;

  private int tpos;

  private Map<TreeNode, Node> uimap;

  private boolean showProperties;


  public TreeView(boolean showProperties) {

    this(showProperties, null);
  }


  public TreeView(boolean showProperties, TreeNode root) {

    this.showProperties = showProperties;
    this.setLayout(null);
    this.setBackground(Color.white);
    this.setOpaque(true);
    this.setRoot(root);
  }


  public void setRoot(TreeNode root) {

    this.removeAll();

    this.root = root;
    this.tpos = TreeView.ipadx;
    this.uimap = new HashMap<TreeNode, Node>();

    Node rootui = this.init(root);
    int offset = TreeView.ipady - rootui.getY();
    int maxy = 0;
    for (Node n : this.uimap.values()) {
      n.setLocation(n.getX(), n.getY() + offset);
      maxy = Math.max(maxy, n.getY() + n.getHeight());
    }

    Dimension d = new Dimension(this.tpos, maxy + TreeView.ipady);
    this.setSize(d);
    this.setPreferredSize(d);
    this.setMinimumSize(d);
    this.setMaximumSize(d);
  }


  private Node init(TreeNode n) {

    Node ui = new Node(n, this.showProperties);
    this.uimap.put(n, ui);
    if (n != this.root) {
      this.add(ui);
    }

    if (n.isLeaf()) {
      ui.setLocation(this.tpos, 0);
      this.tpos += ui.getWidth() + TreeView.ipadx;
    }
    else {
      int maxx = 0;
      int minx = Integer.MAX_VALUE;
      int miny = Integer.MAX_VALUE;

      for (int i = 0; i < n.getChildCount(); i++) {
        Node nui = this.init(n.getChildAt(i));
        minx = Math.min(minx, nui.getX() + nui.getWidth() / 2);
        maxx = Math.max(maxx, nui.getX() + nui.getWidth() / 2);
        miny = Math.min(miny, nui.getY());
      }

      ui.setLocation((minx + maxx + 1 - ui.getWidth()) / 2, miny
        - TreeView.ipady);

      for (int i = 0; i < n.getChildCount(); i++) {
        if (!n.getChildAt(i).isLeaf()) {
          Node nui = this.uimap.get(n.getChildAt(i));
          nui.setLocation(nui.getX(), ui.getY() + TreeView.ipady);
        }
      }
    }

    return ui;
  }

  private static boolean straight_edges = true;


  @Override
  protected void paintComponent(Graphics g) {

    g.setColor(this.getBackground());
    Rectangle r = g.getClipBounds();
    if (r == null) {
      r = new Rectangle(0, 0, this.getWidth(), this.getHeight());
    }
    g.fillRect(r.x, r.y, r.width, r.height);
    g.setColor(this.getForeground());

    if (this.root != null) {
      for (TreeNode n : this.uimap.keySet()) {
        if (!n.isLeaf() && (n != this.root)) {
          Node ui = this.uimap.get(n);
          int left = Integer.MAX_VALUE;
          int right = 0;
          int top = ui.getY() + ui.getHeight() / 2 + TreeView.ipady / 2;
          for (int i = 0; i < n.getChildCount(); i++) {
            Node cui = this.uimap.get(n.getChildAt(i));

            left = Math.min(left, cui.getX() + cui.getWidth() / 2);
            right = Math.max(right, cui.getX() + cui.getWidth() / 2);

            if (TreeView.straight_edges) {
              g.drawLine(cui.getX() + cui.getWidth() / 2, cui.getY(), cui
                .getX()
                                  + cui.getWidth() / 2, top);
            }
            else {
              g.drawLine(cui.getX() + cui.getWidth() / 2, cui.getY(), ui.getX()
                                  + ui.getWidth() / 2, ui.getY()
                + ui.getHeight());
            }
          }

          if (TreeView.straight_edges) {
            g.drawLine(left, top, right, top);
            g.drawLine((left + right) / 2, ui.getY() + ui.getHeight(),
                            (left + right) / 2, top);
          }
        }
      }
    }
  }

  private static class Node
        extends JPanel {

    static NumberFormat floatFormat;

    static {
      Node.floatFormat = NumberFormat.getPercentInstance();
      // floatFormat = NumberFormat.getInstance();
      Node.floatFormat.setMinimumFractionDigits(2);
      Node.floatFormat.setMaximumFractionDigits(2);
    }

    TreeNode n;


    public Node(TreeNode n, boolean showProperties) {

      this.setLayout(new BorderLayout());
      JLabel l = new JLabel(n.toString(), SwingConstants.CENTER);
      l.setFont(l.getFont().deriveFont(Font.BOLD));
      this.add(l, BorderLayout.CENTER);
      if (showProperties && (n instanceof PropertyContainer)) {
        JPanel properties = new JPanel(new GridBagLayout());
        properties.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        PropertyContainer<?> pc = (PropertyContainer<?>)n;
        Font font = l.getFont().deriveFont(l.getFont().getSize2D() - 2.0f);
        for (String name : pc.propertyNames()) {
          Object property = pc.getProperty(name);
          if ((property instanceof Float) || (property instanceof Double)) {
            l =
              new JLabel(Node.floatFormat.format(property), SwingConstants.LEFT);
          }
          else {
            l = new JLabel(property.toString(), SwingConstants.LEFT);
          }
          l.setFont(font);
          gbc.gridx = 0;
          properties.add(new JLabel(name + ": ", SwingConstants.RIGHT), gbc);
          gbc.gridx++;
          properties.add(l, gbc);
          gbc.gridy++;
        }
        this.add(properties, BorderLayout.SOUTH);
      }

      this.setOpaque(true);
      this.setBackground(Color.lightGray);
      this.setBorder(new javax.swing.border.CompoundBorder(
                BorderFactory.createLineBorder(Color.black), BorderFactory
                  .createEmptyBorder(0, 2,
                    0, 2)));
      this.setSize(this.getPreferredSize());

      this.n = n;
    }


    @Override
    public Dimension getPreferredSize() {

      Dimension d = super.getPreferredSize();
      d.width = Math.max(d.width, 40);
      return d;
    }
  }
}