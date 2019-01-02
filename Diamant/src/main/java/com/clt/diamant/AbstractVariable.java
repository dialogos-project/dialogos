package com.clt.diamant;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeListener;

import com.clt.xml.XMLWriter;
import javax.swing.event.ChangeEvent;

/**
 * Abstract variable class to combine Slots and Groovy-only variables
 *
 * @author Till Kollenda
 */
public abstract class AbstractVariable<ValueClass, TypeClass> implements IdentityObject {

    private String id;
    private String name;
    private boolean export;

    private List<ChangeListener> listeners;

    protected AbstractVariable(String name, boolean export) {
        this.name = name;
        this.export = export;
        listeners = new ArrayList<ChangeListener>();
    }

    /**
     * Adds a ChangeListener to the variable, that gets notified if the variable
     * changes.
     *
     * @param c the ChangeLister to be added
     */
    public void addChangeListener(ChangeListener c) {
        listeners.add(c);
    }

    /**
     * Removes the ChangeListener from the variable
     *
     * @param c the ChangeListener to be removed
     */
    public void removeChangeListener(ChangeListener c) {
        listeners.remove(c);
    }

    /**
     * Returns the name of the variable.
     *
     * @return name of the variable
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the variable.
     *
     * @param name of the variable
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public boolean isExport() {
        return export;
    }

    public void setExport(boolean _export) {
        this.export = _export;
    }

    protected void notifyChangeListeners() {
        for (ChangeListener l : listeners) {
            l.stateChanged(new ChangeEvent(this));
        }
    }

    public abstract AbstractVariable clone();

    /**
     * Writes the content of the Variable to the projects XML file
     *
     * @param out
     * @param uid_map
     * @param tag
     */
    public abstract void write(XMLWriter out, IdMap uid_map, String tag);

    /**
     * Sets the value of the Variable.
     *
     * @param v new value of the variable
     */
    public abstract void setValue(ValueClass v);

    /**
     * Gets the Value of the Variable.
     *
     * @return value of the variable
     */
    public abstract ValueClass getValue();

    /**
     * Gets the type of the variable.
     *
     * @return Type of the variable
     */
    public abstract TypeClass getType();
}
