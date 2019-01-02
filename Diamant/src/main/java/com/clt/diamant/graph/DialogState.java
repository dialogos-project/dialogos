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

    private Node suspendedNode;
    private List<AbstractVariable> variables;

    public DialogState(Node suspendedNode) {
        this.suspendedNode = suspendedNode;
        variables = new ArrayList<>();
    }

    public void addVariable(AbstractVariable var) {
        variables.add(var);
    }

    public void addVariables(Collection<? extends AbstractVariable> vars) {
        variables.addAll(vars);
    }

    public Node getSuspendedNode() {
        return suspendedNode;
    }

    public List<AbstractVariable> getVariables() {
        return variables;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();

        buf.append(String.format("Suspended dialog at node %s\n", suspendedNode));

        for (AbstractVariable var : variables) {
            buf.append(String.format("  <%s %s:%s:%s>\n", var.getClass().getSimpleName(), var.getName(), var.getType(), var.getValue()));
        }

        return buf.toString();
    }

    public JSONObject toJson() {
        JSONObject ret = new JSONObject();
        ret.put("nodeId", suspendedNode.getId());

        JSONArray jsonVariables = new JSONArray();
        for (AbstractVariable v : variables) {
            jsonVariables.put(v.encodeForSerialization());
        }

        ret.put("variables", jsonVariables);

        return ret;
    }

    public static DialogState fromJson(JSONObject json, Graph graph) {
        Node suspendedNode = graph.findNodeById(json.getString("nodeId"));
        DialogState ret = new DialogState(suspendedNode);

        JSONArray varlist = json.getJSONArray("variables");
        for (Object jsonVar : varlist) {
            AbstractVariable v = AbstractVariable.decodeJson((JSONObject) jsonVar);
            ret.addVariable(v);
        }

        return ret;
    }
}
