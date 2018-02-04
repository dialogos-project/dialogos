package com.clt.diamant.graph;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.clt.diamant.IdMap;
import com.clt.util.DefaultPropertyContainer;
import com.clt.xml.XMLWriter;

public class Edge extends DefaultPropertyContainer<Object> implements GraphElement {

    private Node source;
    private Node target;

    /**
     * Creates an edge with a source node without a target and no
     * condition/label.
     *
     * @param source the source node
     */
    public Edge(Node source) {

        this(source, null);
    }

    /**
     * Creates an edge from a source to a target node without a condition/label.
     *
     * @param source the source node
     * @param target the target node
     */
    public Edge(Node source, Node target) {

        this(source, target, null);
    }

    /**
     * Creates an edge from a source to a target node with a condition/label.
     *
     * @param source the source node
     * @param target the target node
     * @param cond the label of the edge
     */
    public Edge(Node source, Node target, String cond) {

        if (source == null) {
            throw new IllegalArgumentException("null source in edge creation");
        }

        this.source = source;
        this.target = target;

        this.setCondition(cond);
        this.setColor(Color.blue.darker());
    }

    public Edge clone(Node newSource) {

        Edge e = new Edge(newSource, this.getTarget());
        e.setCondition(this.getCondition());
        return e;
    }

    public Node getSource() {

        return this.source;
    }

    public Node getTarget() {

        return this.target;
    }

    public String getCondition() {

        return (String) this.getProperty("condition");
    }

    public void setCondition(String c) {

        this.setProperty("condition", c);
    }

    public void setTarget(Node n) {

        if (n != this.target) {
            if (this.target != null) {
                this.target.unregisterInEdge(this);
            }
            this.target = n;
            // register at the new target, but only if we are really connected to
            // source node
            if (this.target != null) {
                if (!this.source.edges().contains(this)) {
                    System.err.println("ERROR: edge source is not owner of edge");
                    System.err.println("Edge        = " + this);
                    System.err.println("Edge source = " + this.source);
                    System.err.println("Source edges:");
                    for (Edge e : this.source.edges()) {
                        System.err.println("  " + e);
                    }
                    System.err.flush();
                    throw new IllegalStateException("edge source is not owner of edge");
                } else {
                    this.target.registerInEdge(this);
                }
            }

        }
    }

    public void setColor(Color c) {

        this.setProperty("color", c);
    }

    public Color getColor() {

        return (Color) this.getProperty("color");
    }

    public boolean isSelected() {

        return this.getBooleanProperty("selected");
    }

    public void setSelected(boolean selected) {

        this.setProperty("selected", selected ? Boolean.TRUE : Boolean.FALSE);
    }

    protected void writeAttributes(XMLWriter out, IdMap uid_map) {

        if (this.getCondition() != null) {
            Graph.printAtt(out, "condition", this.getCondition());
        }
    }

    public void write(XMLWriter out, IdMap uid_map) {

        String sourceUID = uid_map.nodes.getKey(this.getSource());

        List<String> keys = new ArrayList<String>();
        List<Object> values = new ArrayList<Object>();

        if (this instanceof SpecialEdge) {
            keys.add("type");
            values.add(((SpecialEdge) this).getType());
        }

        keys.add("src");
        values.add(sourceUID);

        if (this.getTarget() != null) {
            String targetUID = uid_map.nodes.getKey(this.getTarget());
            keys.add("tgt");
            values.add(targetUID);
        }

        out.openElement("edge", keys.toArray(new String[keys.size()]), values
                .toArray());

        this.writeAttributes(out, uid_map);
        out.closeElement("edge");
    }

    public void dispose() {

    }

    @Override
    public String toString() {

        return this.getClass().getName() + "@" + System.identityHashCode(this)
                + ": " + this.getCondition();
    }
}
