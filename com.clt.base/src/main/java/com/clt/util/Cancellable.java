package com.clt.util;

/**
 * Instances of Cancellable describe actions that can be canceled.
 *
 * @author Daniel Bobbert
 * @version 1.0
 */
public interface Cancellable {

    public void cancel();

    public boolean canCancel();

    public String getCancelConfirmationPrompt();
}
