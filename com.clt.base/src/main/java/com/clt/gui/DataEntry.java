package com.clt.gui;

import com.clt.event.DataEntryListener;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */
public interface DataEntry {

    public void requestFocus();

    public boolean dataEntered();

    public void addDataEntryListener(DataEntryListener listener);

    public void removeDataEntryListener(DataEntryListener listener);
}
