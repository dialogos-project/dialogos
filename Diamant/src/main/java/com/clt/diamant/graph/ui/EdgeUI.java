package com.clt.diamant.graph.ui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.event.MouseInputListener;

import com.clt.diamant.graph.Edge;
import com.clt.gui.GUI;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */
public class EdgeUI extends JComponent implements PropertyChangeListener {

    private static final int portWidth = 5, portHeight = EdgeUI.portWidth;

    private NodeUI<?> nui;
    private Edge edge;

    public EdgeUI(NodeUI<?> nui, Edge edge) {

        this.nui = nui;
        this.edge = edge;

        this.setToolTipText(edge.getCondition());

        this.initMouseInputListener();

        this.setAutoscrolls(true);

        edge.addPropertyChangeListener(this);
    }

    public void dispose() {

        this.edge.removePropertyChangeListener(this);
    }

    public void propertyChange(PropertyChangeEvent evt) {

        if (evt.getSource() == this.edge) {
            String property = evt.getPropertyName();
            if (property.equals("condition")) {
                this.setToolTipText((String) evt.getNewValue());
            }
        }
    }

    public Edge getEdge() {

        return this.edge;
    }


    /*
   * Dieser ganze Kram hier ist notwendig, weil das setToolTipText einen
   * MouseListener registriert, mit dem Effekt, dass Edges selbst MouseEvents
   * behandeln und der Handler in NodeUI nicht mehr greift. Deshalb muessen wir
   * das Event uebersetzen und weitergeben.
     */
    private void initMouseInputListener() {

        MouseInputListener ml = new MouseInputListener() {

            private MouseEvent transform(MouseEvent e) {

                Rectangle r = GUI.getRelativeBounds(EdgeUI.this, EdgeUI.this.nui);
                return new MouseEvent(EdgeUI.this.nui, e.getID(), e.getWhen(), e
                        .getModifiers(),
                        e.getX() + r.x, e.getY() + r.y, e.getClickCount(), e
                        .isPopupTrigger());
            }

            private boolean hasMIL() {

                return EdgeUI.this.nui == null ? false : EdgeUI.this.nui
                        .getMouseInputListener() != null;
            }

            public void mouseClicked(MouseEvent e) {

                if (this.hasMIL()) {
                    EdgeUI.this.nui.getMouseInputListener().mouseClicked(
                            this.transform(e));
                }
            }

            public void mousePressed(MouseEvent e) {

                if (this.hasMIL()) {
                    EdgeUI.this.nui.getMouseInputListener().mousePressed(
                            this.transform(e));
                }
            }

            public void mouseReleased(MouseEvent e) {

                if (this.hasMIL()) {
                    EdgeUI.this.nui.getMouseInputListener().mouseReleased(
                            this.transform(e));
                }
            }

            public void mouseEntered(MouseEvent e) {

                if (this.hasMIL()) {
                    EdgeUI.this.nui.getMouseInputListener().mouseEntered(
                            this.transform(e));
                }
            }

            public void mouseExited(MouseEvent e) {

                if (this.hasMIL()) {
                    EdgeUI.this.nui.getMouseInputListener()
                            .mouseExited(this.transform(e));
                }
            }

            public void mouseDragged(MouseEvent e) {

                if (this.hasMIL()) {
                    EdgeUI.this.nui.getMouseInputListener().mouseDragged(
                            this.transform(e));
                }
            }

            public void mouseMoved(MouseEvent e) {

                if (this.hasMIL()) {
                    EdgeUI.this.nui.getMouseInputListener().mouseMoved(this.transform(e));
                }
            }
        };
        this.addMouseListener(ml);
        this.addMouseMotionListener(ml);
    }

    @Override
    public boolean isOpaque() {

        return false;
    }

    @Override
    public void paintComponent(Graphics g) {

        Polygon p = new Polygon();

        p.addPoint(0, 0);
        p.addPoint(2 * EdgeUI.portWidth, 0);
        p.addPoint(EdgeUI.portWidth, EdgeUI.portHeight);

        Color color = this.edge.getColor();

        Color saveColor = g.getColor();
        g.setColor(color);
        g.fillPolygon(p);
        g.setColor(Color.black);
        g.drawPolygon(p);
        g.setColor(Color.lightGray);
        g
                .drawLine(EdgeUI.portWidth + 1, EdgeUI.portHeight, 2 * EdgeUI.portWidth,
                        1);
        g.setColor(color.darker());
        g.drawLine(EdgeUI.portWidth, EdgeUI.portHeight - 1,
                2 * EdgeUI.portWidth - 3, 2);
        g.setColor(color.brighter());
        g.drawLine(2, 1, 2 * EdgeUI.portWidth - 3, 1);
        g.setColor(saveColor);
    }

    @Override
    public Dimension getPreferredSize() {

        return new Dimension(2 * EdgeUI.portWidth + 1, EdgeUI.portHeight + 1);
    }

    @Override
    public Dimension getMinimumSize() {

        return this.getPreferredSize();
    }

    @Override
    public Dimension getMaximumSize() {

        return this.getPreferredSize();
    }

    public Point getOutputRelativeTo(Container c) {

        Rectangle bounds = GUI.getRelativeBounds(this, c);
        return new Point(bounds.x + (bounds.width + 1) / 2, bounds.y
                + bounds.height);
    }
}
