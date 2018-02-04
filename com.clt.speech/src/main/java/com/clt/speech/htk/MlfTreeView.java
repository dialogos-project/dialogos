package com.clt.speech.htk;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class MlfTreeView extends JPanel {

    private static final int ipadx = 15;
    private static final int ipady = 45;

    private MlfNode root;
    private int tpos;
    private Map<MlfNode, Node> uimap;
    private boolean showConfidences;
    private boolean showMarkers;
    private boolean showRoot;

    public MlfTreeView(boolean showRoot, boolean showConfidences) {

        this(showRoot, showConfidences, null);
    }

    public MlfTreeView(boolean showRoot, boolean showConfidences, MlfNode root) {

        this(showRoot, showConfidences, false, root);
    }

    public MlfTreeView(boolean showRoot, boolean showConfidences,
            boolean showMarkers, MlfNode root) {

        this.showRoot = showRoot;
        this.showConfidences = showConfidences;
        this.showMarkers = showMarkers;
        this.setLayout(null);
        this.setBackground(Color.white);
        this.setOpaque(true);
        this.setRoot(root);
    }

    public void setRoot(MlfNode root) {

        this.removeAll();

        this.root = root;
        this.tpos = MlfTreeView.ipadx;
        this.uimap = new HashMap<MlfNode, Node>();

        Node rootui = this.init(root);
        int offset = MlfTreeView.ipady - rootui.getY();
        int maxy = 0;
        for (Iterator<Node> it = this.uimap.values().iterator(); it.hasNext();) {
            Node n = it.next();
            n.setLocation(n.getX(), n.getY() + offset);
            maxy = Math.max(maxy, n.getY() + n.getHeight());
        }

        Dimension d = new Dimension(this.tpos, maxy + MlfTreeView.ipady);
        this.setSize(d);
        this.setPreferredSize(d);
        this.setMinimumSize(d);
        this.setMaximumSize(d);
    }

    private Node init(MlfNode n) {

        Node ui = new Node(n, this.showConfidences, this.showMarkers);
        this.uimap.put(n, ui);
        if ((n != this.root) || this.showRoot) {
            this.add(ui);
        }

        if (n instanceof MlfNonterminalNode) {
            int maxx = 0;
            int minx = Integer.MAX_VALUE;
            int miny = Integer.MAX_VALUE;

            MlfNonterminalNode nt = (MlfNonterminalNode) n;
            for (int i = 0; i < nt.numChildren(); i++) {
                Node nui = this.init(nt.getChild(i));
                minx = Math.min(minx, nui.getX() + nui.getWidth() / 2);
                maxx = Math.max(maxx, nui.getX() + nui.getWidth() / 2);
                miny = Math.min(miny, nui.getY());
            }

            ui.setLocation((minx + maxx + 1 - ui.getWidth()) / 2, miny
                    - MlfTreeView.ipady);

            for (int i = 0; i < nt.numChildren(); i++) {
                if (nt.getChild(i) instanceof MlfNonterminalNode) {
                    Node nui = this.uimap.get(nt.getChild(i));
                    nui.setLocation(nui.getX(), ui.getY() + MlfTreeView.ipady);
                }
            }
        } else {
            @SuppressWarnings("unused")
            MlfTerminalNode t = (MlfTerminalNode) n;
            ui.setLocation(this.tpos, 0);
            this.tpos += ui.getWidth() + MlfTreeView.ipadx;
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
            for (MlfNode n : this.uimap.keySet()) {
                if ((n instanceof MlfNonterminalNode)
                        && ((n != this.root) || this.showRoot)) {
                    MlfNonterminalNode nt = (MlfNonterminalNode) n;
                    Node ui = this.uimap.get(nt);
                    int left = Integer.MAX_VALUE;
                    int right = 0;
                    int top = ui.getY() + ui.getHeight() / 2 + MlfTreeView.ipady / 2;
                    for (int i = 0; i < nt.numChildren(); i++) {
                        Node cui = this.uimap.get(nt.getChild(i));

                        left = Math.min(left, cui.getX() + cui.getWidth() / 2);
                        right = Math.max(right, cui.getX() + cui.getWidth() / 2);

                        if (MlfTreeView.straight_edges) {
                            g.drawLine(cui.getX() + cui.getWidth() / 2, cui.getY(), cui
                                    .getX()
                                    + cui.getWidth() / 2, top);
                        } else {
                            g.drawLine(cui.getX() + cui.getWidth() / 2, cui.getY(), ui.getX()
                                    + ui.getWidth() / 2, ui.getY()
                                    + ui.getHeight());
                        }
                    }

                    if (MlfTreeView.straight_edges) {
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

        static NumberFormat confidenceFormat;

        static {
            Node.confidenceFormat = NumberFormat.getPercentInstance();
            // confidenceFormat = NumberFormat.getInstance();
            Node.confidenceFormat.setMinimumFractionDigits(2);
            Node.confidenceFormat.setMaximumFractionDigits(2);
        }

        MlfNode n;

        public Node(MlfNode n, boolean showConfidences, boolean showMarkers) {

            this.setLayout(new BorderLayout());
            JLabel l = new JLabel(n.getLabel(), SwingConstants.CENTER);
            l.setFont(l.getFont().deriveFont(Font.BOLD));
            this.add(l, BorderLayout.CENTER);
            JPanel properties = new JPanel(new GridLayout(0, 1));
            properties.setOpaque(false);
            if (showConfidences) {
                l
                        = new JLabel(Node.confidenceFormat.format(n.getConfidence()),
                                SwingConstants.CENTER);
                l.setFont(l.getFont().deriveFont(l.getFont().getSize2D() - 2.0f));
                properties.add(l);
            }

            if (showMarkers) {
                if ((n.getStart() != 0) || (n.getEnd() != 0)) {
                    l
                            = new JLabel(n.getStart() + "-" + n.getEnd(), SwingConstants.CENTER);
                    l.setFont(l.getFont().deriveFont(l.getFont().getSize2D() - 2.0f));
                    properties.add(l);
                }
            }

            this.add(properties, BorderLayout.SOUTH);

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
