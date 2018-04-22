package com.clt.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JComponent;

/**
 * Wraps a JComponent an puts it in the middle of this JComponent (Note that
 * Passpartout extends JComponent). The wrapped JComponent is redrawn
 * automatically then the passpartout object is resized. Supplementary, a
 * background-color can be specified and whether a border should be drawn around
 * the wrapped component.
 */
public class Passpartout extends JComponent {

    /**
     * Serial Version ID.
     */
    private static final long serialVersionUID = -8403870890392102011L;

    /**
     * Wrapped component.
     */
    private Component content;

    /**
     * Reference on the GridBagConstraints which lays out the wrapped component
     * in the middle of this component.
     */
    private GridBagConstraints contentPlacement;

    /**
     * Indicates if a border should be drawed.
     */
    private boolean paintBorder;

    /**
     * Constructor taking as argument the jcomponent to be wrapped.
     *
     * @param content JComponent to be wrapped.
     */
    public Passpartout(JComponent content) {

        // this(content, Color.lightGray);
        this(content, null, true);
    }

    /**
     * Constructor taking as argument the JComponent to be wrapped. Optionally,
     * a background-color can be specified, and whether a border should be
     * painted around the wrapped component. If the Color is null, not
     * background color is set.
     *
     * @exception NullPointerException if {@code content} is {@code null}
     * @param content JComponent to be wrapped.
     * @param backgroundColor Color of the background. If {@code backgrundColor}
     * is {@code null} , no background-color is set.
     * @param paintBorder If true, a border is paint around the wrapped
     * Component. Else, no boarder is painted.
     */
    public Passpartout(JComponent content, Color backgroundColor, boolean paintBorder) {
        this.content = null;

        this.setLayout(new GridBagLayout());

        this.contentPlacement = new GridBagConstraints();
        this.contentPlacement.gridx = 0;
        this.contentPlacement.gridy = 0;
        this.contentPlacement.fill = GridBagConstraints.NONE;
        this.contentPlacement.anchor = GridBagConstraints.CENTER;

        this.setOpaque(true);
        if (backgroundColor != null) {
            this.setBackground(backgroundColor);
        }

        this.paintBorder = paintBorder;

        this.add(content);
    }

    /**
     * Returns true if this component is completely opaque.
     */
    @Override
    public boolean isOpaque() {
        return super.isOpaque() && (this.getBackground().getAlpha() == 255);
    }

    @Override
    protected void addImpl(Component content, Object constraints, int index) {
        if (this.content != null) {
            this.content.removeComponentListener(this.contentListener);
            this.remove(this.content);
        }

        this.content = content;
        content.addComponentListener(this.contentListener);
        super.addImpl(content, this.contentPlacement, 0);
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(this.getBackground());
        g.fillRect(0, 0, this.getWidth(), this.getHeight());

        super.paintComponent(g);

        if (this.paintBorder) {
            Rectangle frame = this.content.getBounds();
            g.setColor(Color.black);
            g.drawRect(frame.x - 1, frame.y - 1, frame.width + 1, frame.height + 1);
        }
    }

    /**
     * When ever this Graphical component is resized, it is repaint
     * automatically. Shown and Hidden properties are simply forwarded to the
     * wrapped component.
     */
    private ComponentAdapter contentListener = new ComponentAdapter() {

        /**
         * Invoked when the component's size changes.
         *
         * @param e Reference to the passed ComponentEvent.
         */
        @Override
        public void componentResized(ComponentEvent e) {

            if (e.getComponent() == Passpartout.this.content) {
                Passpartout.this.revalidate();
                Passpartout.this.repaint();
            }
        }

        /**
         * Invoked when the component has been made visible.
         *
         * @param e Reference to the passed ComponentEvent.
         */
        @Override
        public void componentShown(ComponentEvent e) {

            if (e.getComponent() == Passpartout.this.content) {
                Passpartout.this.setVisible(true);
            }
        }

        /**
         * Invoked when the component has been made invisible.
         *
         * @param e Reference to the passed ComponentEvent.
         */
        @Override
        public void componentHidden(ComponentEvent e) {

            if (e.getComponent() == Passpartout.this.content) {
                Passpartout.this.setVisible(false);
            }
        }
    };

}
