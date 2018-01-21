package com.clt.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JComponent;
import javax.swing.JViewport;
import javax.swing.SwingConstants;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */
public class VerticalScroller extends JComponent implements SwingConstants {

    private static final int fadeWidth = 24;

    private boolean fading = true;

    private JViewport content;

    private javax.swing.Timer timer;

    public VerticalScroller() {

        this(40);
    }

    public VerticalScroller(JComponent content) {

        this(content, 40);
    }

    public VerticalScroller(int delay) {

        this(null, delay);
    }

    public VerticalScroller(JComponent content, int delay) {

        this.setLayout(new GridLayout(1, 1));

        this.content = new JViewport() {

            @Override
            public void paint(Graphics g) {

                if (!VerticalScroller.this.timer.isRunning()) {
                    return;
                }

                super.paint(g);

                if (VerticalScroller.this.fading) {
                    int width = this.getWidth() - 1;
                    int height = this.getHeight() - 1;

                    Container parent;
                    if (this.getView() instanceof Container) {
                        parent = (Container) this.getView();
                    } else {
                        parent = this;
                    }
                    while (!parent.isOpaque() && (parent.getParent() != null)) {
                        parent = parent.getParent();
                    }
                    if (parent.isOpaque()) {
                        Color back = parent.getBackground();
                        Insets insets = this.getInsets();
                        for (int i = 0; i < VerticalScroller.fadeWidth; i++) {
                            Color c
                                    = new Color(back.getRed(), back.getGreen(), back.getBlue(), 255
                                            - (255 * i) / VerticalScroller.fadeWidth);
                            g.setColor(c);
                            g.drawLine(insets.left, insets.top + i, width - insets.right,
                                    insets.top + i);
                            g.drawLine(insets.left, height - insets.bottom - i, width
                                    - insets.right, height - insets.bottom - i);
                        }
                    }
                }
            }
        };
        this.content.setOpaque(false);
        this.content.setDoubleBuffered(true);

        this.add(this.content);
        this.setOpaque(false);
        this.setDoubleBuffered(true);

        ComponentListener cl = new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {

                VerticalScroller.this.resetPosition();
            }
        };

        this.addComponentListener(cl);
        content.addComponentListener(cl);

        this.setContent(content);

        this.timer = new javax.swing.Timer(delay, new ActionListener() {

            private Component oldView = null;

            public void actionPerformed(ActionEvent evt) {

                VerticalScroller vs = VerticalScroller.this;
                Component view = vs.content.getView();
                if (view != null) {
                    Dimension d = view.getSize();
                    if (view != this.oldView) {
                        this.oldView = view;
                        VerticalScroller.this.resetPosition();
                    } else {
                        Point p = vs.content.getViewPosition();
                        p.y++;
                        if (p.y > d.height) {
                            VerticalScroller.this.resetPosition();
                        } else {
                            vs.content.setViewPosition(p);
                        }
                    }
                }
            }
        });
        this.timer.setInitialDelay(1000);
    }

    private void resetPosition() {

        this.content.setViewPosition(new Point(0, -this.content.getHeight()));
    }

    public void start() {

        this.resetPosition();
        this.timer.start();
    }

    @Override
    public void removeNotify() {

        this.timer.stop();
        super.removeNotify();
    }

    public void setContent(JComponent content) {

        this.content.setView(content);
        this.resetPosition();
    }

    @Override
    public void setBackground(Color c) {

        super.setBackground(c);
        this.setOpaque(true);
    }
}
