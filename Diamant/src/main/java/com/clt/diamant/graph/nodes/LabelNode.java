package com.clt.diamant.graph.nodes;

import java.awt.Color;
import java.util.Map;

import javax.swing.JComponent;

import com.clt.diamant.ExecutionLogger;
import com.clt.diamant.IdMap;
import com.clt.diamant.InputCenter;
import com.clt.diamant.WozInterface;
import com.clt.diamant.graph.Node;
import com.clt.xml.XMLWriter;

public class LabelNode extends Node {

    public LabelNode() {

        super();

        this.addEdge();
    }

    public static Color getDefaultColor() {

        return new Color(76, 76, 128);
    }

    @Override
    public JComponent createEditorComponent(Map<String, Object> properties) {
        return null;
    }

    @Override
    public Node execute(WozInterface comm, InputCenter input, ExecutionLogger logger) {
        logNode(logger);
        Node target = this.getEdge(0).getTarget();
        comm.transition(this, target, 0, null);
        return target;
    }


    // wichtig fuer ComboBox im EditDialog fuer GotoNode
    @Override
    public String toString() {

        return this.getTitle();
    }
}
