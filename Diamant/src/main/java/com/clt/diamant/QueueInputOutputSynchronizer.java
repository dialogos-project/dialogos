/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.clt.diamant;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;

/**
 *
 * @author koller
 */
public class QueueInputOutputSynchronizer<ToDialogos, FromDialogos> implements InputOutputSynchronizer<ToDialogos, FromDialogos> {

    private static interface Channel<E> {
        public void send(E value);

        public E receive() throws InterruptedException, ExecutionException;
    }

    private static class QueueChannel<E> implements Channel<E> {
        private BlockingQueue<E> q = new ArrayBlockingQueue<>(1);

        @Override
        public void send(E value) {
            q.offer(value);
        }

        @Override
        public E receive() throws InterruptedException {
            return q.take();
        }
    }

    private Channel<FromDialogos> dialogosToCallerChannel = new QueueChannel<>();
    private Channel<ToDialogos> callerToDialogosChannel = new QueueChannel<>();

    @Override
    public void sendToDialogos(ToDialogos value) {
        callerToDialogosChannel.send(value);
    }

    @Override
    public ToDialogos receiveFromCaller() throws InterruptedException, ExecutionException {
        return callerToDialogosChannel.receive();
    }

    @Override
    public void sendToCaller(FromDialogos value) {
        dialogosToCallerChannel.send(value);
    }

    @Override
    public FromDialogos receiveFromDialogos() throws InterruptedException, ExecutionException {
        return dialogosToCallerChannel.receive();
    }
}
