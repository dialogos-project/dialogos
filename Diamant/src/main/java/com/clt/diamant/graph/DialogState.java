/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.clt.diamant.graph;

import com.clt.diamant.GroovyVariable;
import com.clt.diamant.Slot;
import java.util.List;

/**
 * A state from which execution of a dialog graph can be resumed.
 *
 * @author koller
 */
public class DialogState {
    private Node suspendedNode;
    private List<Slot> variables;
    private List<GroovyVariable> groovyVariables;

    public DialogState(Node suspendedNode, List<Slot> variables, List<GroovyVariable> groovyVariables) {
        this.suspendedNode = suspendedNode;
        this.variables = variables;
        this.groovyVariables = groovyVariables;
    }

    public Node getSuspendedNode() {
        return suspendedNode;
    }

    public List<Slot> getVariables() {
        return variables;
    }

    public List<GroovyVariable> getGroovyVariables() {
        return groovyVariables;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        
        buf.append(String.format("Suspended dialog at node %s\n", suspendedNode));
        
        for( Slot var : variables ) {
            buf.append("  ");
            buf.append(var.toDetailedString());
            buf.append("\n");
        }
        
        for( GroovyVariable var : groovyVariables ) {
            buf.append("  ");
            buf.append(var.toDetailedString());
            buf.append("\n");
        }
        
        return buf.toString();
    }

    
    
}
