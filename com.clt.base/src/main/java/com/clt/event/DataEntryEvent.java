package com.clt.event;

import java.util.EventObject;

import com.clt.gui.DataEntry;

/**
 * A semantic event that the data in some data entry has changed.
 *
 * @author Daniel Bobbert
 * @version 1.0
 */
public class DataEntryEvent extends EventObject {
    /**
     * Construct a new event from the given source.
     *
     * @param source
     */
    public DataEntryEvent(DataEntry source) {
        super(source);
    }

    /**
     * Get the source of this event.
     */
    public DataEntry getDataEntry() {
        return (DataEntry) this.getSource();
    }
}
