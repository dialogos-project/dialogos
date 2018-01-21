package com.clt.util;

import java.util.concurrent.Callable;

import com.clt.event.ProgressListener;

/**
 * @author dabo
 *
 */
public interface LongCallable<V> extends Callable<V> {

    /**
     * Returns a description of this action
     */
    public String getDescription();

    /**
     * Register the given ProgressListener
     */
    public void addProgressListener(ProgressListener l);

    /**
     * Unregister the given ProgressListener
     */
    public void removeProgressListener(ProgressListener l);
}
