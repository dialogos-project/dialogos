package com.clt.util;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DefaultPropertyContainer<ValueType> implements PropertyContainer<ValueType> {

    protected Map<String, ValueType> properties;

    private PropertyChangeSupport changeSupport;

    private boolean allowDuplicateListeners;

    public DefaultPropertyContainer() {

	this(true);
    }

    public DefaultPropertyContainer(boolean allowDuplicateListeners) {

	this.properties = new HashMap<String, ValueType>();
	this.changeSupport = new PropertyChangeSupport(this);
	this.allowDuplicateListeners = allowDuplicateListeners;
    }

    public Set<String> propertyNames() {

	return this.properties.keySet();
    }

    /**
     * Puts a key with the associated value in the PropertyMap.
     * 
     * @param key the key of the property 
     * @param value the value of the property
     */
    public void setProperty(String key, ValueType value) {
	Object oldValue = null;
	if (value == null) {
	    oldValue = this.properties.remove(key);
	} else {
	    oldValue = this.properties.put(key, value);
	}

	if (oldValue != value) {
	    if ((oldValue == null) || (value == null)) {
		this.firePropertyChange(key, oldValue, value);
	    } else if (!value.equals(oldValue)) {
		this.firePropertyChange(key, oldValue, value);
	    }
	}
    }

    /**
     * Gets a property from the PropertyMap.
     * 
     * @param key the key of the property
     * @return the property of the key
     */
    public ValueType getProperty(String key) {

	return this.properties.get(key);
    }

    public boolean getBooleanProperty(String key) {

	Object o = this.getProperty(key);
	if (o instanceof Boolean) {
	    return ((Boolean) o).booleanValue();
	} else {
	    return false;
	}
    }

    public final void addPropertyChangeListener(PropertyChangeListener listener) {

	if (!this.allowDuplicateListeners) {
	    PropertyChangeListener[] listeners = this.changeSupport.getPropertyChangeListeners();
	    for (PropertyChangeListener l : listeners) {
		if (l == listener) {
		    throw new IllegalArgumentException("This PropertyChangeListener is already registered");
		}
	    }
	}
	this.changeSupport.addPropertyChangeListener(listener);
    }

    public final void removePropertyChangeListener(PropertyChangeListener listener) {

	this.changeSupport.removePropertyChangeListener(listener);
    }

    public final void firePropertyChange(String propertyName, Object oldValue, Object newValue) {

	this.changeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

}