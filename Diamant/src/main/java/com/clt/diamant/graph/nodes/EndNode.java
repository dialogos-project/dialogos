package com.clt.diamant.graph.nodes;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;

import com.clt.diamant.ExecutionLogger;
import com.clt.diamant.IdMap;
import com.clt.diamant.InputCenter;
import com.clt.diamant.Slot;
import com.clt.diamant.WozInterface;
import com.clt.diamant.graph.Edge;
import com.clt.diamant.graph.Graph;
import com.clt.diamant.graph.Node;
import com.clt.diamant.graph.search.SearchResult;
import com.clt.xml.XMLWriter;

public class EndNode extends Node {

    public EndNode() {

        super();

        // setTitle(Resources.getString("End"));
    }

    public static Color getDefaultColor() {

        return new Color(153, 153, 255);
    }

    @Override
    public JComponent createEditorComponent(Map<String, Object> properties) {
        return null;
    }

    public Edge addEdge(Node target, String condition) {

        throw new Error("Can't add edges to an EndNode");
    }

    @Override
    public Node execute(WozInterface comm, InputCenter input, ExecutionLogger logger) {
        logNode(logger);
        return null;
    }

    @Override
    public void validate(Collection<SearchResult> errors) {

        super.validate(errors);
        // there is nothing else to do
    }

    @Override
    public void writeVoiceXML(XMLWriter w, IdMap uid_map) {
        Graph superGraph = this.getGraph().getSuperGraph();
        List<Slot> vars = superGraph == null ? new ArrayList<Slot>()
                : superGraph.getAllVariables(Graph.GLOBAL);

        StringBuffer varnames = new StringBuffer();
        for (int i = 0; i < vars.size(); i++) {
            if (i > 0) {
                varnames.append(" ");
            }
            varnames.append(vars.get(i).getName());
        }

        w.printElement("return", new String[]{"namelist"},
                new String[]{varnames.toString()});
    }
}
