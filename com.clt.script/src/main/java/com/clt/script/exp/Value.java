package com.clt.script.exp;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import com.clt.script.exp.values.*;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * The base class for all values that the script engine works on.
 *
 * @author Daniel Bobbert
 * @version 3.0
 */
public abstract class Value {

    private Map<String, PrimitiveValue> attributes;

    protected Value() {

        this.attributes = new HashMap<String, PrimitiveValue>();
    }

    /**
     * Compare this value to another value.
     *
     * @return true, if the value <code>v</code> denotes the same value The test
     * should not take the value's attributes into account.
     */
    @Override
    public abstract boolean equals(Object o);

    /**
     * Compare this value's attributes to another value's attributes
     *
     * @return true, if this value and value <code>v</code> have the same
     * attributes
     */
    public boolean equalsAttributes(Value v) {

        return this.attributes.equals(v.attributes);
    }

    /**
     * Converts this Value into a Java object (IntValue to Integer;
     * StringValue to String; etc.).
     *
     * @return object with readable value
     */
    public abstract Object getReadableValue();

    /**
     * Return the hashCode of this value.
     *
     * <p>
     * The general contract of <code>hashCode</code> is:
     * <ul>
     * <li>Whenever it is invoked on the same object more than once during an
     * execution of a Java application, the <code>hashCode</code> method must
     * consistently return the same integer, provided no information used in
     * <code>equals</code> comparisons on the object is modified. This integer need
     * not remain consistent from one execution of an application to another
     * execution of the same application.
     * <li>If two objects are equal according to the <code>equals(Object)</code>
     * method, then calling the <code>hashCode</code> method on each of the two
     * objects must produce the same integer result.
     * <li>It is <em>not</em> required that if two objects are unequal according
     * to the {@link java.lang.Object#equals(java.lang.Object)} method, then
     * calling the <code>hashCode</code> method on each of the two objects must
     * produce distinct integer results. However, the programmer should be aware
     * that producing distinct integer results for unequal objects may improve
     * the performance of hashtables.
     * </ul>
     * <p>
     */
    @Override
    public abstract int hashCode();

    /**
     * Make a copy of this value.
     *
     * This method calls <code>copyValue</code> to copy the value itself and
     * then automatically copies all attributes of the value.
     */
    public final Value copy() {

        Value v = this.copyValue();
        for (String attr : this.getAttributes()) {
            v.setAttribute(attr, this.getAttribute(attr));
        }
        return v;
    }

    /**
     * Make a copy of this value. This method is called by <code>copy</code> to
     * produce an exact copy of the value. Complex values need to copy their
     * elements recursively by calling <code>copy</code> on them. Your code does
     * not have to deal with the value's attributes, these are handled
     * automatically by <code>copy</code>. Subclasses should never return
     * <code>this</code> since values can differ in their attributes, even if
     * they are otherwise immutable.
     *
     * @return A copy of this value.
     */
    protected abstract Value copyValue();

    /**
     * Produce a string representation of this value. The string should only
     * display the raw value, without the value's attributes.
     *
     * @return The string representation of the value.
     */
    @Override
    public abstract String toString();

    /**
     * Produce a JSON-conforming string representation of this value.
     * @return the string representation of the value in JSON format
     */
    public String toJson() { 
        return toString(); 
    }

    public static Value fromJson(JSONObject o) {
        return parse(o);
    }

    public static Value fromJson(JSONArray o) {
        return parse(o);
    }

    public static Value fromJson(String s) {
        Object json = (s.startsWith("[")) ? new JSONArray(s) : new JSONObject(s);
        return parse(json);
    }

    private static Value parse(Object obj) {
        if (obj instanceof JSONObject) {
            JSONObject jsonObj = (JSONObject) obj;
            Object[] keys = jsonObj.keySet().toArray();
            String[] stringKeys = new String[keys.length];
            Value[] values = new Value[jsonObj.length()];
            for (int i = 0; i < jsonObj.length(); i++) {
                values[i] = parse(jsonObj.get(keys[i].toString()));
                stringKeys[i] = keys[i].toString();
            }
            return new StructValue(stringKeys, values);
        } else if (obj instanceof JSONArray) {
            JSONArray jsonArr = (JSONArray) obj;
            Value[] values = new Value[jsonArr.length()];
            for (int i = 0; i < jsonArr.length(); i++) {
                values[i] = parse(jsonArr.get(i));
            }
            return new ListValue(values);
        } else if (obj instanceof String) {
            return new StringValue((String) obj);
        } else if (obj instanceof Boolean) {
            return new BoolValue((Boolean) obj);
        } else if (obj instanceof Integer) {
            return new IntValue((Integer) obj);
        } else if (obj instanceof Long) {
            return new IntValue((Long) obj);
        } else if (obj instanceof Double) {
            return new RealValue((Double) obj);
        } else if (obj == JSONObject.NULL) {
            return new Undefined();
        } else {
            System.err.println("Unknown type passed to parse: " + obj);
            return null;
        }
    }

    public final void prettyPrint(PrintWriter w) {

        this.prettyPrint(w, 0);
    }

    public void prettyPrint(PrintWriter w, int inset) {

        w.print(this.toString());
        this.printAttributes(w);
    }

    protected void printAttributes(PrintWriter w) {

        Iterator<String> atts = this.getAttributes().iterator();
        if (atts.hasNext()) {
            w.print(" (");
            while (atts.hasNext()) {
                String att = atts.next();
                w.print(att);
                w.print("=");
                w.print(this.getAttribute(att).toString());
                if (atts.hasNext()) {
                    w.print(", ");
                }
            }

            w.print(")");
        }
    }

    /**
     * Return the type of this value.
     */
    public abstract Type getType();

    /**
     * Set the value of attribute <code>name</code>. The value is internally
     * converted to a {@link com.clt.script.exp.values.BoolValue}.
     */
    public void setAttribute(String name, boolean value) {

        this.setAttribute(name, new BoolValue(value));
    }

    /**
     * Set the value of attribute <code>name</code>. The value is internally
     * converted to an {@link com.clt.script.exp.values.IntValue}.
     */
    public void setAttribute(String name, long value) {

        this.setAttribute(name, new IntValue(value));
    }

    /**
     * Set the value of attribute <code>name</code>. The value is internally
     * converted to a {@link com.clt.script.exp.values.RealValue}.
     */
    public void setAttribute(String name, double value) {

        this.setAttribute(name, new RealValue(value));
    }

    /**
     * Set the value of attribute <code>name</code>. The value is internally
     * converted to a {@link com.clt.script.exp.values.StringValue}.
     */
    public void setAttribute(String name, String value) {

        if (value == null) {
            throw new IllegalArgumentException(
                    "<null> String in setAttribute(String, String)");
        }
        this.setAttribute(name, new StringValue(value));
    }

    /**
     * Set the value of attribute <code>name</code>.
     */
    public void setAttribute(String name, PrimitiveValue value) {

        if (value == null) {
            throw new IllegalArgumentException(
                    "<null> String in setAttribute(String, PrimitiveValue)");
        }
        this.attributes.put(name, value);
    }

    /**
     * Return the value of attribute <code>name</code>. The attribute value is a
     * subclass of {@link com.clt.script.exp.values.PrimitiveValue}.
     *
     */
    public PrimitiveValue getAttribute(String name) {

        return this.attributes.get(name);
    }

    /**
     * Return an iteration of all attribute names of this value.
     *
     * @deprecated Use {@link #getAttributes} instead
     */
    @Deprecated
    public Iterator<String> attributes() {

        return this.attributes.keySet().iterator();
    }

    /**
     * Return a read only collection of all attribute names of this value.
     */
    public Collection<String> getAttributes() {

        return Collections.unmodifiableSet(this.attributes.keySet());
    }

    public static final Value Void = new Value() {

        @Override
        public boolean equals(Object v) {

            return v == this;
        }

        @Override
        public int hashCode() {

            return 0x0816;
        }

        @Override
        protected Value copyValue() {

            throw new EvaluationException("Cannot copy 'void'");
        }

        @Override
        public void setAttribute(String name, PrimitiveValue value) {

            throw new EvaluationException("Cannot set attributes of 'void'");
        }

        @Override
        public String toString() {

            return "void";
        }

        @Override
        public Type getType() {

            return Type.Void;
        }

        @Override
        public Object getReadableValue() {
            return null;
        }
    };

    public static Value of(Object o) {

        if (o instanceof Value) {
            return (Value) o;
        } else if (o == null) {
            return new Undefined();
        } else if (o instanceof Collection) {
            Value elements[] = new Value[((Collection) o).size()];
            int n = 0;
            for (Object elem : (Collection) o) {
                elements[n++] = Value.of(elem);
            }
            return new ListValue(elements);
        } else if ((o instanceof Iterator) || (o instanceof Iterable)) {
            Collection<Value> elements = new LinkedList<Value>();
            Iterator<?> it;
            if (o instanceof Iterator) {
                it = (Iterator) o;
            } else {
                it = ((Iterable) o).iterator();
            }
            while (it.hasNext()) {
                elements.add(Value.of(it.next()));
            }
            return new ListValue(elements);
        } else if (o instanceof Enumeration) {
            Collection<Value> elements = new LinkedList<Value>();
            while (((Enumeration) o).hasMoreElements()) {
                elements.add(Value.of(((Enumeration) o).nextElement()));
            }
            return new ListValue(elements);
        } else if (o.getClass().isArray()) {
            Object elems[] = (Object[]) o;
            Value elements[] = new Value[elems.length];
            for (int i = 0; i < elems.length; i++) {
                elements[i] = Value.of(elems[i]);
            }
            return new ListValue(elements);
        } else if (o instanceof Map) {
            String labels[] = new String[((Map) o).size()];
            Value values[] = new Value[labels.length];
            int n = 0;
            for (Object key : ((Map) o).keySet()) {
                labels[n] = key.toString();
                values[n] = Value.of(((Map) o).get(key));
                n++;
            }
            return new StructValue(labels, values);
        } else if (o instanceof Boolean) {
            return new BoolValue(((Boolean) o).booleanValue());
        } else if ((o instanceof Float) || (o instanceof Double)) {
            return new RealValue(((Number) o).doubleValue());
        } else if (o instanceof Number) {
            return new IntValue(((Number) o).intValue());
        } else {
            return new StringValue(o.toString());
        }
    }
}
