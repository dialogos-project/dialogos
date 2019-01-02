package com.clt.diamant;


import com.clt.xml.XMLWriter;

import java.util.HashMap;
import java.util.Map;

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
    public Map<String, String> encodeForSerialization() {
        Map<String,String> ret = new HashMap<>();

        ret.put("id", getId());
        ret.put("name", getName());
        ret.put("variable_class", getClass().getSimpleName());
        ret.put("type", getType().getClass().getSimpleName());
        ret.put("value", getValue().toString());

        return ret;
    }

    public String toDetailedString() {
        return String.format("<GroovyVar[%s:%s:%s] %s>", getId(), getName(), type, getValue());
    }
}
