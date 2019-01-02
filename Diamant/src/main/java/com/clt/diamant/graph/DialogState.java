/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.clt.diamant.graph;

import com.clt.diamant.AbstractVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
        
        for( AbstractVariable var : variables ) {
            buf.append(String.format("  <%s %s:%s:%s>\n", var.getClass().getSimpleName(), var.getName(), var.getType(), var.getValue()));
        }
        
        return buf.toString();
    }

    
    
}
