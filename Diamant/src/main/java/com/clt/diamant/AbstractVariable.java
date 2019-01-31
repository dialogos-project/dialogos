package com.clt.diamant;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeListener;

import com.clt.xml.XMLWriter;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Objects;
import javax.swing.event.ChangeEvent;

/**
 * Abstract variable class to combine Slots and Groovy-only variables
 *
 * @author Till Kollenda
 */
public abstract class AbstractVariable<ValueClass, TypeClass> implements IdentityObject {

    protected String id;
    protected String name;
    protected boolean export;

    private List<ChangeListener> listeners;

    protected static final String VARIABLE_CLASS_KEY = "variable_class";
    protected static final int JSON_TYPE_SLOT = 0;
    protected static final int JSON_TYPE_GROOVY = 1;
    protected static final String[] JSON_TYPENAMES = { "Slot", "GroovyVariable" };

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

    public abstract String toDetailedString();

    /**
     * Encodes this variable as a JSON element.
     * 
     * @return 
     */
    public abstract JsonElement toJsonElement();
    
    /**
     * Encodes this variable as a JSON string.
     * 
     * @return 
     */
    public String toJson() {
        return toJsonElement().toString();
    }

    /**
     * Decodes a JSON string representation back into an AbstractVariable.
     * 
     * @param jsonString
     * @return
     * @throws com.clt.diamant.AbstractVariable.VariableParsingException 
     */
    public static AbstractVariable fromJson(String jsonString) throws VariableParsingException {
        JsonParser p = new JsonParser();

        JsonObject el = (JsonObject) p.parse(jsonString);
        return fromJsonElement(el);
    }
    
    /**
     * Decodes a (parsed) JSON element into an AbstractVariable.
     * 
     * @param el
     * @return
     * @throws com.clt.diamant.AbstractVariable.VariableParsingException 
     */
    public static AbstractVariable fromJsonElement(JsonObject el) throws VariableParsingException {
        int variableType = el.get(VARIABLE_CLASS_KEY).getAsInt();

        try {
            switch (variableType) {
                case JSON_TYPE_SLOT:
                    return Slot.fromJsonImpl(el);
                case JSON_TYPE_GROOVY:
                    return GroovyVariable.fromJsonImpl(el);
            }
        } catch (ClassNotFoundException ex) {
            throw new VariableParsingException("An error occurred while decoding a JSON representation into variable class " + JSON_TYPENAMES[variableType], ex);
        }

        throw new VariableParsingException("Could not determine class of AbstractVariable: " + variableType);
    }

    public static class VariableParsingException extends Exception {

        public VariableParsingException(String message, Throwable cause) {
            super(message, cause);
        }

        public VariableParsingException(Throwable cause) {
            super(cause);
        }

        public VariableParsingException(String message) {
            super(message);
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + Objects.hashCode(this.id);
        hash = 83 * hash + Objects.hashCode(this.name);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AbstractVariable<?, ?> other = (AbstractVariable<?, ?>) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }
    
    
}
