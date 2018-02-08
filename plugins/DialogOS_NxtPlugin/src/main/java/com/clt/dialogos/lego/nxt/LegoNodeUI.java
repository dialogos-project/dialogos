package com.clt.dialogos.lego.nxt;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.event.MouseInputListener;

import com.clt.diamant.graph.Node;
import com.clt.diamant.graph.ui.GraphUI;
import com.clt.diamant.graph.ui.NodeUI;

/**
 * @author dabo
 *
 */
public class LegoNodeUI extends NodeUI<Node> {

    private static final int nibbleHeight = 3;
    private static final int nibbleWidth = 10;
    private static final int nibbleSpacing = 10;

    private static final int h_offset = 0;
    private static final int v_offset = 0;

    public LegoNodeUI(GraphUI graph, Node n, MouseInputListener scroller) {

        super(graph, n, scroller);
    }

    @Override
    protected Border createDefaultBorder() {

        final Border b = super.createDefaultBorder();
        return new CompoundBorder(new Border() {

            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {

                Insets originalBorder = b.getBorderInsets(c);
                Insets legoBorder = this.getBorderInsets(c);

                Insets insets
                        = new Insets(originalBorder.top + legoBorder.top, originalBorder.left
                                + legoBorder.left, originalBorder.bottom
                                + legoBorder.bottom,
                                originalBorder.right + legoBorder.right);

                int brickWidth = width - (insets.left + insets.right);
                int brickHeight = LegoNodeUI.this.getBody().getHeight(); // height -
                // (insets.top
                // +
                // insets.bottom);
                int numNibbles
                        = (brickWidth - LegoNodeUI.nibbleWidth - 10)
                        / (LegoNodeUI.nibbleWidth + LegoNodeUI.nibbleSpacing);

                int leftOffset = x + insets.left;
                int topOffset = y + insets.top - 1;
                g.setColor(LegoNodeUI.this.getNode().getColor());
                for (int i = 0; i < LegoNodeUI.v_offset; i++) {
                    g.drawLine(leftOffset + i + 1, topOffset - i, leftOffset + brickWidth
                            + i,
                            topOffset - i);
                }

                leftOffset = x + insets.left + brickWidth;
                for (int i = 0; i < LegoNodeUI.h_offset; i++) {
                    g.drawLine(leftOffset + i, topOffset + 1 - i, leftOffset + i,
                            topOffset
                            + brickHeight - 1 - i);
                }

                leftOffset
                        = x
                        + insets.left
                        + (brickWidth - LegoNodeUI.nibbleWidth)
                        / 2
                        - (numNibbles * (LegoNodeUI.nibbleWidth + LegoNodeUI.nibbleSpacing))
                        / 2;
                int v = y + Math.max(0, insets.top - LegoNodeUI.nibbleHeight);
                for (int i = 0; i <= numNibbles; i++) {
                    int h
                            = leftOffset + i
                            * (LegoNodeUI.nibbleWidth + LegoNodeUI.nibbleSpacing);
                    g.setColor(LegoNodeUI.this.getNode().getColor());
                    g.fillRect(h, v, LegoNodeUI.nibbleWidth, LegoNodeUI.nibbleHeight);
                    g.setColor(LegoNodeUI.this.getNode().getColor().darker());
                    g.drawRect(h, v, LegoNodeUI.nibbleWidth - 1, LegoNodeUI.nibbleHeight);
                }
            }

            public Insets getBorderInsets(Component c) {

                Insets innerBorder = b.getBorderInsets(c);

                return new Insets(Math.max(0, LegoNodeUI.nibbleHeight
                        + LegoNodeUI.v_offset - innerBorder.top), 0, 0,
                        Math.max(0, LegoNodeUI.h_offset - innerBorder.right));
            }

            public boolean isBorderOpaque() {

                return false;
            }
        }, b);
    }
}
