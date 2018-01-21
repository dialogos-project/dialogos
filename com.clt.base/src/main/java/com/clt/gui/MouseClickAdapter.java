package com.clt.gui;

import java.awt.Color;
import java.awt.event.MouseEvent;

import javax.swing.event.MouseInputListener;

public abstract class MouseClickAdapter implements MouseInputListener {

    Color activeColor, inactiveColor;

    public MouseClickAdapter() {

        this(null, null);
    }

    public MouseClickAdapter(Color active, Color inactive) {

        this.activeColor = active;
        this.inactiveColor = inactive;
    }

    public abstract void mouseClicked(MouseEvent e);

    public void mousePressed(MouseEvent e) {

        if (this.activeColor != null) {
            e.getComponent().setForeground(this.activeColor);
        }
    }

    public void mouseReleased(MouseEvent e) {

        if (this.inactiveColor != null) {
            e.getComponent().setForeground(this.inactiveColor);
        }
        if (e.getComponent().contains(e.getPoint())) {
            this.mouseClicked(new MouseEvent(e.getComponent(),
                    MouseEvent.MOUSE_CLICKED, e.getWhen(), e
                    .getModifiers(), e.getX(), e.getY(), e.getClickCount(), e
                    .isPopupTrigger()));
        }
    }

    public void mouseEntered(MouseEvent e) {

        // nothing to do
    }

    public void mouseExited(MouseEvent e) {

        // nothing to do
    }

    public void mouseMoved(MouseEvent e) {

        // nothing to do
    }

    public void mouseDragged(MouseEvent e) {

        if (e.getComponent().contains(e.getPoint())) {
            if (this.activeColor != null) {
                e.getComponent().setForeground(this.activeColor);
            }
        } else {
            if (this.inactiveColor != null) {
                e.getComponent().setForeground(this.inactiveColor);
            }
        }
    }

}
