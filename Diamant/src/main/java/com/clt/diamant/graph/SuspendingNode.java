/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.clt.diamant.graph;

import com.clt.diamant.ExecutionLogger;
import com.clt.diamant.InputCenter;
import com.clt.diamant.WozInterface;
import com.clt.diamant.graph.nodes.DialogSuspendedException;

/**
 *
 * @author koller
 */
public abstract class SuspendingNode<FromDialogos,ToDialogos> extends Node {
    private ToDialogos inputValue = null;
    
    
    @Override
    abstract public Node execute(WozInterface comm, InputCenter input, ExecutionLogger logger) throws DialogSuspendedException;
    
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
    
    protected ToDialogos receiveAsynchronousInput(FromDialogos prompt) throws DialogSuspendedException {
        if( inputValue == null ) {
            suspend(prompt);
            return null; // unreachable
        } else {
            return consumeInputValue();
        }
    }
}
