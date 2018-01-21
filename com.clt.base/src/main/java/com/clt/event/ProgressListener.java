package com.clt.event;

import java.util.EventListener;

/**
 * The listener interface for receiving progress events. The class that is
 * interested in processing a progress event implements this interface.
 *
 * @see ProgressEvent
 * @author Ronald Bieber
 */
public interface ProgressListener extends EventListener {

    /**
     * Invoked when the progress status has changed
     */
    public void progressChanged(ProgressEvent e);
}
