package com.clt.util;

import java.util.List;

import com.clt.event.ProgressEvent;
import com.clt.event.ProgressListener;
import java.util.ArrayList;

/**
 * @author dabo
 *
 */
public abstract class AbstractLongCallable<V> implements LongCallable<V>, Cancellable {

    private List<ProgressListener> listeners = new ArrayList<ProgressListener>();

    public void addProgressListener(ProgressListener l) {
        this.listeners.add(l);
    }

    public void removeProgressListener(ProgressListener l) {
        this.listeners.remove(l);
    }

    public abstract String getDescription();

    protected abstract V call(ProgressListener l) throws Exception;

    final public V call() throws Exception {
        return this.call(new ProgressListener() {
            public void progressChanged(ProgressEvent e) {
                for (ProgressListener l : AbstractLongCallable.this.listeners) {
                    l.progressChanged(e);
                }
            }
        });
    }

    public void cancel() {

    }

    public boolean canCancel() {

        return false;
    }

    public String getCancelConfirmationPrompt() {

        return null;
    }
}
