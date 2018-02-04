package com.clt.diamant.gui;

import com.clt.diamant.graph.GraphOwner;
import com.clt.diamant.graph.ui.GraphUI;

public interface GraphEditor {

    public void showEditor();

    public void closeEditor();

    public boolean isShowing();

    public GraphOwner getGraphOwner();

    public GraphUI getGraphUI();
}
