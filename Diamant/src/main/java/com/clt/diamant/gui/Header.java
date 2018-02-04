package com.clt.diamant.gui;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * @author dabo
 *
 */
public class Header extends JPanel {

    private Collection<LinkListener> listeners;

    public Header() {

        this(null);
    }

    public Header(String text) {

        super(new FlowLayout(FlowLayout.LEFT, 6, 6));

        this.listeners = new ArrayList<LinkListener>();

        this.setBackground(new Color(48, 64, 78));
        this.setOpaque(true);

        this.setText(text);
    }

    public void addLinkListener(LinkListener l) {

        this.listeners.add(l);
    }

    public void removeLinkListener(LinkListener l) {

        this.listeners.remove(l);
    }

    public void clear() {

        this.setText(new String[0], false);
    }

    public void setText(Object item) {

        String text = item == null ? null : item.toString();
        if ((text == null) || (text.trim().length() == 0)) {
            this.setText(new String[0], false);
        } else {
            this.setText(new Object[]{text}, false);
        }
    }

    public void setText(Object[] items, boolean link) {

        this.removeAll();

        if (items.length == 0) {
            this.addLabel(" ", false);
        } else {
            for (int i = 0; i < items.length; i++) {
                if (i > 0) {
                    this.addLabel(":", false);
                }
                this.addLabel(items[i], link);
            }
        }
    }

    private void addLabel(final Object item, boolean link) {

        JLabel label
                = new JLabel(item == null ? null : item.toString(), null,
                        SwingConstants.LEFT);
        label.setForeground(Color.WHITE);
        this.add(label);

        if (link) {
            label.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {

                    for (LinkListener l : Header.this.listeners) {
                        l.linkClicked(item);
                    }
                }
            });
        }
    }

    public static interface LinkListener {

        public void linkClicked(Object link);
    }
}
