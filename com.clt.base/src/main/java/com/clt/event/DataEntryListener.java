package com.clt.event;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */
public interface DataEntryListener {

    /**
     * Called when the data of a data entry has changed
     */
    public void dataChanged(DataEntryEvent evt);
}
