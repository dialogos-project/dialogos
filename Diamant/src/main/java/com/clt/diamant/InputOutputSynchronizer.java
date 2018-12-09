/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.clt.diamant;

import java.util.concurrent.ExecutionException;

/**
 *
 * @author koller
 */
public interface InputOutputSynchronizer<ToDialogos,FromDialogos> {
    public void sendToDialogos(ToDialogos value);
    public ToDialogos receiveFromCaller() throws InterruptedException, ExecutionException;

    public void sendToCaller(FromDialogos value);
    public FromDialogos receiveFromDialogos() throws InterruptedException, ExecutionException;
}
