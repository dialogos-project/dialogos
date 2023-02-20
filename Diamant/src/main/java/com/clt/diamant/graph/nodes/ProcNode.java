package com.clt.diamant.graph.nodes;

import java.awt.Color;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.clt.diamant.ExecutionLogger;
import com.clt.diamant.IdMap;
import com.clt.diamant.InputCenter;
import com.clt.diamant.Slot;
import com.clt.diamant.WozInterface;
import com.clt.diamant.graph.Node;
import com.clt.diamant.graph.Procedure;
import com.clt.script.exp.Value;
import com.clt.xml.XMLWriter;

public class ProcNode extends OwnerNode {

    // private static int procCounter = 1;
    public ProcNode() {

        this(new Procedure(null));
    }

    public ProcNode(Procedure ownedGraph) {

        super(ownedGraph);

        // setTitle(Resources.getString("Procedure") + ' ' + procCounter++);
    }

    public static Color getDefaultColor() {

        return Color.ORANGE.darker();
    }

    @Override
    public boolean supportsEdges() {

        return false;
    }

    @Override
    public Node execute(WozInterface comm, InputCenter input, ExecutionLogger logger) {

        return this.execute(comm, null, null, input, logger);
    }

    public Node execute(WozInterface comm, Map<Slot, Value> arguments,
            Map<Slot, Value> returns,
            InputCenter input, ExecutionLogger logger) {
        logNode(logger);

        Node n;
        try {
            comm.subgraph(this, true);
            n
                    = ((Procedure) this.getOwnedGraph()).execute(comm, arguments, returns,
                            input, logger);
        } finally {
            comm.subgraph(this, false);
        }

        return n;
    }

    public List<Slot> getParameters() {

        return ((Procedure) this.getOwnedGraph()).getParameters();
    }

    public List<Slot> getReturnVariables() {

        return ((Procedure) this.getOwnedGraph()).getReturnVariables();
    }

    // wichtig fuer ComboBox im EditDialog fuer CallNode
    @Override
    public String toString() {

        return this.getTitle();
    }
}
