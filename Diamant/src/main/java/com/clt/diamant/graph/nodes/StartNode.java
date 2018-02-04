package com.clt.diamant.graph.nodes;

import java.awt.Color;
import java.util.Map;

import javax.swing.JComponent;

import com.clt.diamant.ExecutionLogger;
import com.clt.diamant.IdMap;
import com.clt.diamant.InputCenter;
import com.clt.diamant.Resources;
import com.clt.diamant.WozInterface;
import com.clt.diamant.graph.Node;
import com.clt.xml.XMLWriter;

public class StartNode extends Node {

    public StartNode() {

        super();

        this.addEdge();

        this.setTitle(Resources.getString("Start"));
    }

    public static Color getDefaultColor() {

        return new Color(153, 153, 255);
    }

    @Override
    protected JComponent createEditorComponent(Map<String, Object> properties) {

        return null;
    }

    @Override
    public Node execute(WozInterface comm, InputCenter input, ExecutionLogger logger) {
        logger.logNode(this);
        Node target = this.getEdge(0).getTarget();
        comm.transition(this, target, 0, null);
        return target;
    }

    @Override
    protected void writeVoiceXML(XMLWriter w, IdMap uid_map) {

        this.writeVoiceXMLGoto(w, uid_map, 0);
    }
}
