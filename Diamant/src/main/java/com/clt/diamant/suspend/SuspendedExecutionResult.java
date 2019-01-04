/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.clt.diamant.suspend;

/**
 *
 * @author koller
 */
public class SuspendedExecutionResult<FromDialogos> {
    private DialogState dialogState;
    private FromDialogos prompt;

    public SuspendedExecutionResult(DialogState dialogState, FromDialogos prompt) {
        this.dialogState = dialogState;
        this.prompt = prompt;
    }

    public DialogState getDialogState() {
        return dialogState;
    }

    public FromDialogos getPrompt() {
        return prompt;
    }
}
