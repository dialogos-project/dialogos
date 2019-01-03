/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.clt.diamant.graph;

import com.clt.diamant.AbstractVariable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * A state from which execution of a dialog graph can be resumed.
 *
 * @author koller
 */
public class DialogState {
    private final String suspendedNodeId;
    private final List<AbstractVariable> variables;

    public DialogState(String suspendedNodeId) {
        this.suspendedNodeId = suspendedNodeId;
        variables = new ArrayList<>();
    }
    
    public void addVariable(AbstractVariable var) {
        variables.add(var);
    }

    public void addVariables(Collection<? extends AbstractVariable> vars) {
        variables.addAll(vars);
    }

    public String getSuspendedNodeId() {
        return suspendedNodeId;
    }
    
    

    public List<AbstractVariable> getVariables() {
        return variables;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();

        buf.append(String.format("Suspended dialog at node %s\n", suspendedNodeId));

        for (AbstractVariable var : variables) {
            buf.append(String.format("  <%s %s:%s:%s>\n", var.getClass().getSimpleName(), var.getName(), var.getType(), var.getValue()));
        }

        return buf.toString();
    }

    public JSONObject toJson() {
        JSONObject ret = new JSONObject();
        ret.put("nodeId", suspendedNodeId);

        JSONArray jsonVariables = new JSONArray();
        for (AbstractVariable v : variables) {
            jsonVariables.put(v.encodeForSerialization());
        }

        ret.put("variables", jsonVariables);

        return ret;
    }
    
    public SuspendingNode lookupNode(Graph graph) {
        Node n = graph.findNodeById(suspendedNodeId);
        
        if( n == null ) {
            return null;
        } else {
            return (SuspendingNode) n;
        }        
    }

    public static DialogState fromJson(JSONObject json) {
        DialogState ret = new DialogState(json.getString("nodeId"));

        JSONArray varlist = json.getJSONArray("variables");
        for (Object jsonVar : varlist) {
            AbstractVariable v = AbstractVariable.decodeJson((JSONObject) jsonVar);
            ret.addVariable(v);
        }

        return ret;
    }
}
