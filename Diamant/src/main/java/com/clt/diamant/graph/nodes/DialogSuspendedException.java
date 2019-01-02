/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.clt.diamant.graph.nodes;

import com.clt.diamant.graph.DialogState;

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

    public DialogSuspendedException(DialogState dialogState) {
        this.dialogState = dialogState;
    }

    public DialogState getDialogState() {
        return dialogState;
    }
}
