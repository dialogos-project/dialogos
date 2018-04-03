package com.clt.diamant.gui;

import javax.swing.JToolBar;
import javax.swing.SwingConstants;

public abstract class Toolbox extends JToolBar {

    public Toolbox() {
        this(SwingConstants.HORIZONTAL);
    }

    public Toolbox(int orientation) {
        super(orientation);
    }

    public abstract void notifyState();

    public abstract void update();
}
