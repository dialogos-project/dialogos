package com.clt.diamant.graph.nodes;

import com.clt.diamant.Resources;
import com.clt.diamant.graph.Node;
import com.clt.diamant.graph.SpecialEdge;

/**
 * @author dabo
 *
 */
public class CatchAllEdge extends SpecialEdge {

    // do not modifiy!
    public static final String TYPE = "catch all";

    public CatchAllEdge(Node source) {

        super(source);
    }

    public CatchAllEdge(Node source, Node target) {

        super(source, target);
    }

    @Override
    public CatchAllEdge clone(Node newSource) {

        return new CatchAllEdge(newSource, this.getTarget());
    }

    @Override
    public String getCondition() {

        return Resources.getString("OtherValues");
    }

    @Override
    public void setCondition(String c) {

    }

    @Override
    public String getType() {

        return CatchAllEdge.TYPE;
    }
}
