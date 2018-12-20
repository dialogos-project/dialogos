package com.clt.diamant;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.clt.xml.XMLWriter;

/**
 * Class for the Groovy-only variables
 *
 * @author Bri Burr
 */
public class GroovyVariable extends AbstractVariable<Object,String> {

    private final String _type;
    private Object _value;

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
        this._value = initValue;
        this._export = export;
        this._type = "GroovyObject";
    }

    @Override
    public GroovyVariable clone() {
        GroovyVariable v = new GroovyVariable(_name, this.getValue().toString(), this._export);
        return v;
    }

    @Override
    public void setValue(Object v) {
        _value = v;
        for (ChangeListener l : _listeners) {
            l.stateChanged(new ChangeEvent(this));
        }
    }

    @Override
    public Object getValue() {
        return _value;
    }

    @Override
    public void write(XMLWriter out, IdMap uid_map, String tag) {
        out.openElement(tag, new String[]{"uid"}, new Object[]{uid_map.groovyVariables.put(this)});

        out.printElement("name", this.getName());
        out.printElement("value", this._value);
        if (this._export) {
            out.printElement("export", null);
        }

        out.closeElement(tag);
    }

    @Override
    public String getType() {
        return _type;
    }
}
