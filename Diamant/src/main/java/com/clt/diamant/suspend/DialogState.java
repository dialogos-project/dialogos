/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.clt.diamant.suspend;

import com.clt.diamant.AbstractVariable;
import com.clt.diamant.graph.Graph;
import com.clt.diamant.graph.Node;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

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
    
    public SuspendingNode lookupNode(Graph graph) {
        Node n = graph.findNodeById(suspendedNodeId);
        
        if( n == null ) {
            return null;
        } else {
            return (SuspendingNode) n;
        }        
    }
    
    public static DialogState fromJson(String jsonString) throws AbstractVariable.VariableParsingException {
        JsonParser p = new JsonParser();
        JsonObject json = (JsonObject) p.parse(jsonString);
        
        DialogState ret = new DialogState(json.get("nodeId").getAsString());
        
        JsonArray variables = (JsonArray) json.get("variables");
        for( JsonElement var : variables ) {
            ret.variables.add(AbstractVariable.fromJsonElement((JsonObject) var));
        }
        
        return ret;
    }

    public String toJson() {
        JsonObject json = new JsonObject();
        
        JsonArray variables = new JsonArray();
        json.add("variables", variables);
        for( AbstractVariable v : this.variables) {
            variables.add(v.toJsonElement());
        }
        
        json.add("nodeId", new JsonPrimitive(suspendedNodeId));
        
        return json.toString();
        
        
//        
//        
//        JSONObject ret = new JSONObject();
//        ret.put("nodeId", suspendedNodeId);
//
//        JSONArray jsonVariables = new JSONArray();
//        for (AbstractVariable v : variables) {
//            jsonVariables.put(v.encodeForSerialization());
//        }
//
//        ret.put("variables", jsonVariables);
//
//        return ret;
    }
}
