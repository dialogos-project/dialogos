package com.clt.diamant.graph.nodes;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;

import org.xml.sax.SAXException;

import com.clt.diamant.GroovyVariable;
import com.clt.diamant.ExecutionLogger;
import com.clt.diamant.IdMap;
import com.clt.diamant.InputCenter;
import com.clt.diamant.Resources;
import com.clt.diamant.Slot;
import com.clt.diamant.WozInterface;
import com.clt.diamant.graph.Edge;
import com.clt.diamant.graph.Graph;
import com.clt.diamant.graph.Node;
import com.clt.diamant.graph.search.SearchResult;
import com.clt.diamant.graph.ui.EdgeConditionModel;
import com.clt.diamant.gui.NodePropertiesDialog;
import com.clt.script.exp.EvaluationException;
import com.clt.script.exp.Value;
import com.clt.xml.XMLReader;
import com.clt.xml.XMLWriter;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

/**
 * GroovyNode, allows to execute GroovyScripts through this node.
 *
 * @author Daniel Bobbert / Bri Burr / Till Kollenda
 * @version 1.0
 */
public class GroovyNode extends Node {

    // do not change. These are written to XML.
    public static final String SCRIPT = "script";
    public final String _defaultEdgeName;
    //public EdgeConditionModel edgeModel;

    public GroovyNode() {
        super();

        // adding a default edge to the node
        _defaultEdgeName = Resources.getString("default");
        // A place to keep the unsaved edges
        this.setProperty("temp edges", this.getOutEdges());
        this.addEdge(_defaultEdgeName);
        this.setProperty(GroovyNode.SCRIPT, "");
    }

    public static Color getDefaultColor() {

        return new Color(153, 255, 255);
    }

    @Override
    protected JComponent createEditorComponent(Map<String, Object> properties) {

        JTabbedPane tp = new JTabbedPane();

        tp.addTab(Resources.getString("GroovyScript"), NodePropertiesDialog.createGroovyScriptEditor(properties, "script"));

        tp.addTab(Resources.getString("OutgoingEdges"), NodePropertiesDialog.createOutgoingEdgesEditor(properties, "edge_model", this));

        return tp;
    }

    /**
     * Gets the Outgoing edges from the table and adds them to the node.
     */
    @Override
    public boolean editProperties(Component parent) {

        this.setProperty(EdgeConditionModel.EDGE_PROPERTY, this.getOutEdges());

        if (super.editProperties(parent)) {
            List<Edge> es = this.getEdgeModel();

            this.removeAllEdges();
            for (Edge e : es) {
                this.addEdge(e);
            }

            return true;
        } else {
            return false;
        }
    }

    /**
     * Executes the GroovyNode: Create GroovyShell with a Binding, give
     * variables to Binding, evaluate the script and update the variables.
     *
     * @return target node
     */
    @Override
    public Node execute(WozInterface comm, InputCenter input, ExecutionLogger logger) {
        logNode(logger);

        Node target = this.getEdge(0).getTarget();

        // Create new Binding and GroovyShell
        Binding sharedData = new Binding();
        GroovyShell gShell = new GroovyShell(sharedData);

        // get list of all variables(slots) from graph
        List<Slot> allVars = this.getGraph().getAllVariables(Graph.LOCAL);
        List<GroovyVariable> groovyVars = this.getGraph().getAllGroovyVariables();

        // put pre existing global variables in the binding
        for (Slot var : allVars) {
            String varName = var.getName();
            Object varValue = var.getValue().getReadableValue();
            sharedData.setProperty(varName, varValue);
        }
        for (GroovyVariable var : groovyVars) {
            String varName = var.getName();
            Object varValue = var.getValue();
            sharedData.setProperty(varName, varValue);
        }

        String functions = this.getGraph().getGroovyScript();
        functions = functions + "\n";
        // the return value of the script
        Object result;
        try {
            String script = (String) this.getProperty(GroovyNode.SCRIPT);
            // Appends global groovy functions to the top of the script,
            // so they can be used by the script in this node
            script = functions + script;
            // execute the script
            result = gShell.evaluate(script);
        } catch (Exception exn) {
            throw new NodeExecutionException(this, "Illegal script", exn, logger);
        }

        // The return value of the script determines which edge to tranverse
        String targetEdgeLabel;
        try {
            targetEdgeLabel = result.toString();
        } catch (Exception e) {
            targetEdgeLabel = _defaultEdgeName;
        }

        List<Edge> edges = getOutEdges();
        for (Edge e : edges) {
            if (e.getCondition().equals(targetEdgeLabel)) {
                target = e.getTarget();
            }
        }

        // get variables from Binding
        @SuppressWarnings("unchecked")
        Map<String, Object> newVars = (Map<String, Object>) sharedData.getVariables();
        // update global variables
        try {
            for (Slot slot : allVars) {
                if (!slot.getValue().equals(Value.of(newVars.get(slot.getName())))) {
                    slot.setValue(Value.of(newVars.get(slot.getName())));
                }
            }
            for (GroovyVariable entry : groovyVars) {
                if (!entry.getValue().equals(newVars.get(entry.getName()))) {
                    entry.setValue(newVars.get(entry.getName()));
                }
            }
        } catch (EvaluationException e) {
            //TODO localize Exception
            throw new NodeExecutionException(this, "Can't change type of global variables in Groovy script", e, logger);
        } catch (Exception e) {
            throw new NodeExecutionException(this, "Can't change type of global variables in Groovy script", e, logger);
        }

        comm.transition(this, target, 0, null);
        return target;
    }

    @Override
    public void validate(Collection<SearchResult> errors) {

        super.validate(errors);
    }

    @Override
    protected void writeAttributes(XMLWriter out, IdMap uid_map) {

        super.writeAttributes(out, uid_map);

        Graph.printTextAtt(out, GroovyNode.SCRIPT, (String) this.getProperty(GroovyNode.SCRIPT));
    }

    @Override
    protected void readAttribute(XMLReader r, String name, String value, IdMap uid_map) throws SAXException {

        if (name.equals(GroovyNode.SCRIPT)) {
            this.setProperty(name, value);
        } else {
            super.readAttribute(r, name, value, uid_map);
        }
    }

    @Override
    protected void writeVoiceXML(XMLWriter w, IdMap uid_map) {

        w.openElement("script");
        w.println("<![CDATA[");
        w.println((String) this.getProperty(GroovyNode.SCRIPT));
        w.println("]]>");
        w.closeElement("script");
    }

    @SuppressWarnings("unchecked")
    private List<Edge> getEdgeModel() {
        return (List<Edge>) this.getProperty(EdgeConditionModel.EDGE_PROPERTY);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean acceptableToSave() {
        // check the unsaved edges
        return !existDuplicateEdgesOrEmptyLabels((List<Edge>) properties.get("temp edges"));
    }

    /**
     *
     * @param edgeList
     * @return true if edge labels are empty or duplicate
     */
    private boolean existDuplicateEdgesOrEmptyLabels(List<Edge> edgeList) {
        ArrayList<String> labels = new ArrayList<>(edgeList.size());
        for (Edge edge : edgeList) {
            String label = edge.getCondition();
            if (label.equals("")) {
                return true;
            } else if (labels.contains(label)) {
                return true;
            } else {
                labels.add(label);
            }
        }
        return false;
    }
}
