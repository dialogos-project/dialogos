/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.clt.diamant.suspend;

import com.clt.diamant.suspend.DialogSuspendedException;
import com.clt.diamant.ExecutionLogger;
import com.clt.diamant.InputCenter;
import com.clt.diamant.WozInterface;
import com.clt.diamant.graph.Node;

/**
 * A node at which the execution of the dialog can be suspended and then
 * later resumed. Typical examples of suspending nodes are input nodes
 * for Alexa and Google Home: When such a node is executed, the graph
 * is suspended (i.e. its state serialized), and control is returned
 * to Alexa. Alexa will then at some later time send a request that
 * will cause us to resume execution at the suspending node in the serialized 
 * graph state.<p>
 * 
 * The type parameters FromDialogos and ToDialogos represent the types
 * of the data that is sent from DialogOS to Alexa and from Alexa to DialogOS,
 * respectively. The input node suspends because it wants a value of type
 * ToDialogos, which is supplied by Alexa. When it suspends, the node
 * sends a prompt of type FromDialogos to Alexa.
 * 
 * @author koller
 */
public abstract class SuspendingNode<FromDialogos,ToDialogos> extends Node {
    private ToDialogos inputValue = null;
    
    
    @Override
    abstract public Node execute(WozInterface comm, InputCenter input, ExecutionLogger logger) throws DialogSuspendedException;
    
    /**
     * Makes the given inputValue available to the suspending node.
     * The inputValue comes from a request from Alexa (or similar).
     * It will be received by the suspending node on its next call of
     * {@link #receiveAsynchronousInput(java.lang.Object) }.
     * 
     * @param inputValue 
     */
    public void resume(ToDialogos inputValue) {
        this.inputValue = inputValue;
    }
    
    private ToDialogos consumeInputValue() {
        ToDialogos ret = inputValue;
        inputValue = null;
        return ret;
    }
    
    private void suspend(FromDialogos prompt) throws DialogSuspendedException {
        getGraph().suspend(this, prompt);
    }
    
    /**
     * Suspends execution of the node until input has been made available
     * asynchronously. The first time this method is called, it sends the
     * given prompt to Alexa (or similar) and suspends the dialog. Outside
     * of the execution of the dialog, Alexa will then make a return value
     * (of type ToDialogos) available by calling {@link #resume(java.lang.Object) }.
     * The second call to receiveAsynchronousInput will then resume execution
     * of the node and supply the given value. It will consume this value,
     * such that the third call to this method will once again suspend the
     * dialog.
     * 
     * @param prompt
     * @return
     * @throws DialogSuspendedException 
     */
    protected ToDialogos receiveAsynchronousInput(FromDialogos prompt) throws DialogSuspendedException {
        if( inputValue == null ) {
            suspend(prompt);
            return null; // unreachable
        } else {
            return consumeInputValue();
        }
    }
}
