package com.clt.diamant;


import com.clt.xml.XMLWriter;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Class for the Groovy-only variables
 *
 * @author Bri Burr
 */
public class GroovyVariable extends AbstractVariable<Object,String> {
    private final String type;
    private Object value;

    /**
     * Creates a untitled GroovyVariable with a undefined value.
     */
    public GroovyVariable() {
        this(Resources.getString("UntitledIdentifier"), "undefined", true);
    }

    /**
     * Creates a new Groovy Variable.
     *
     * @param name Name of the variable
     * @param initValue initial Value of the Variable
     * @param export true if the variable should be exported
     */
    public GroovyVariable(String name, Object initValue, boolean export) {
        super(name, export);
        this.value = initValue;
        this.type = "GroovyObject";
    }

    @Override
    public GroovyVariable clone() {
        GroovyVariable v = new GroovyVariable(getName(), this.getValue().toString(), this.isExport());
        return v;
    }

    @Override
    public void setValue(Object v) {
        value = v;
        notifyChangeListeners();
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public void write(XMLWriter out, IdMap uid_map, String tag) {
        out.openElement(tag, new String[]{"uid"}, new Object[]{uid_map.groovyVariables.put(this)});

        out.printElement("name", this.getName());
        out.printElement("value", this.value);
        if (isExport()) {
            out.printElement("export", null);
        }

        out.closeElement(tag);
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String toDetailedString() {
        return String.format("<GroovyVar[%s:%s:%s] %s>", getId(), getName(), type, getValue());
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
        final GroovyVariable other = (GroovyVariable) obj;
        if (!Objects.equals(this.type, other.type)) {
            return false;
        }
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        return true;
    }
    
    
    
    @Override
    public JsonElement toJsonElement() {
        Gson gson = new Gson();
        JsonObject ret = new JsonObject();

        // first, encode entire GroovyVariable
        JsonElement encodedGroovyVariable = gson.toJsonTree(this);
        ret.add("groovy_variable", encodedGroovyVariable);
        
        // Then encode just the value itself again, with its class.
        // This is needed to ints can be decoded correctly as ints, and not as doubles.
        JsonElement encodedValue = gson.toJsonTree(value);
        ret.add("value", encodedValue);
        ret.add("value_class", new JsonPrimitive(value.getClass().getName()));
        
        // boilerplate for AbstractVariable#fromJson
        ret.add(VARIABLE_CLASS_KEY, new JsonPrimitive(JSON_TYPE_GROOVY));
        
        return ret;
    }
    
    public static GroovyVariable fromJsonImpl(JsonObject parsedJson) throws ClassNotFoundException {
        Gson gson = new Gson();

        // deserialize a first version of the GroovyVariable
        JsonElement groovyVariableElement = parsedJson.get("groovy_variable");
        GroovyVariable ret = gson.fromJson(groovyVariableElement, GroovyVariable.class);
        
        // then determine the actual class of the value and
        // overwrite the GroovyVariable's value with it
        String valueClassName = parsedJson.get("value_class").getAsString();
        Class valueClazz = GroovyVariable.class.getClassLoader().loadClass(valueClassName);
        JsonElement valueElement = parsedJson.get("value");
        Object decodedValue = gson.fromJson(valueElement, valueClazz);
        ret.setValue(decodedValue);
        
        return ret;
    }
}
