package com.clt.properties.ui;

import java.awt.LayoutManager;

import javax.swing.JPanel;

/**
 * @author dabo
 *
 */
public class PPanel extends JPanel {

    public PPanel(LayoutManager layout) {
        super(layout);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        for (int i = 0; i < this.getComponentCount(); i++) {
            this.getComponent(i).setEnabled(enabled);
        }
    }
}
