/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.clt.diamant.graph;

/**
 * An exception that indicates that the execution of a dialog
 * graph was suspended by a node. The exception contains the
 * dialog state at the time of suspension, so that the dialog
 * can be seamlessly resumed from this point later on.
 * 
 * @author koller
 */
public class DialogSuspendedException extends RuntimeException {
    private DialogState dialogState;
    private Object prompt;

    public DialogSuspendedException(DialogState dialogState, Object prompt) {
        this.dialogState = dialogState;
        this.prompt = prompt;
    }

    public Object getPrompt() {
        return prompt;
    }

    public DialogState getDialogState() {
        return dialogState;
    }
}
