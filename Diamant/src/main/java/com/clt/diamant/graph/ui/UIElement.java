package com.clt.diamant.graph.ui;

import java.awt.Rectangle;

import javax.swing.JPanel;

public abstract class UIElement extends JPanel {

    public abstract void dispose();

    public abstract Rectangle getVisibleBounds();
}
