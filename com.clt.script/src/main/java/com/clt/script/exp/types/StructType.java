package com.clt.script.exp.types;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import com.clt.script.exp.Type;
import com.clt.script.exp.TypeException;
import com.clt.script.exp.values.StructValue;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */
public class StructType extends Type        implements ReferenceType {

    // keep the elements sorted
    private Map<String, Type> slots = new TreeMap<String, Type>();
    private boolean underspecified;

    public StructType() {

        this(new String[0], new Type[0], true);
    }

    public StructType(StructType t) {

        for (Iterator<String> it = t.labels(); it.hasNext();) {
            String label = it.next();
            this.add(label, t.getType(label));
        }
        this.underspecified = t.underspecified;
    }

    public StructType(String[] labels, Type[] types, boolean underspecified) {

        if (labels.length != types.length) {
            throw new IllegalArgumentException(
                    "Number of labels does not match number of element types");
        }

        for (int i = 0; i < labels.length; i++) {
            this.add(labels[i], types[i]);
        }

        this.underspecified = underspecified;
    }

    protected void add(String name, Type type) {

        this.slots.put(name, type);
    }

    @Override
    public Object clone() {

        return new StructType(this);
    }

    public boolean isUnderspecified() {

        return this.underspecified;
    }

    /**
     * Get an iterator over all labels of this type
     */
    public Iterator<String> labels() {

        return this.slots.keySet().iterator();
    }

    public Type getType(String label) {

        Type t = this.slots.get(label);
        if (t == null) {
            if (this.underspecified) {
                return new TypeVariable();
            } else {
                throw new TypeException("Type '" + this
                        + "' does not contain an element '" + label
                        + "'");
            }
        } else {
            return t;
        }
    }

    @Override
    public boolean equals(Object t) {

        if (t instanceof StructType) {
            StructType type = (StructType) t;
            if (this.slots.size() != type.slots.size()) {
                return false;
            } else {
                for (Iterator<String> it = this.labels(); it.hasNext();) {
                    String label = it.next();
                    if (type.slots.containsKey(label)) {
                        return false;
                    } else if (!Type.equals(this.getType(label), type.getType(label))) {
                        return false;
                    }
                }
                return true;
            }
        } else {
            return false;
        }
    }

    @Override
    protected Type unify(Type t) {

        if (t instanceof StructType) {
            StructType type = (StructType) t;
            if (this.slots.size() == 0) {
                if ((type.slots.size() == 0) || this.underspecified) {
                    return type;
                } else {
                    return this.throwUnificationException(type);
                }
            } else if (type.slots.size() == 0) {
                if ((this.slots.size() == 0) || type.underspecified) {
                    return this;
                } else {
                    return this.throwUnificationException(type);
                }
            } else {
                for (Iterator<String> it = this.labels(); it.hasNext();) {
                    String label = it.next();
                    if (type.slots.containsKey(label)) {
                        this.slots.put(label, Type.unify(this.getType(label), type
                                .getType(label)));
                    } else if (type.underspecified) {
                        this.add(label, this.getType(label));
                    } else {
                        return this.throwUnificationException(t);
                    }
                }
                for (Iterator<String> it = this.labels(); it.hasNext();) {
                    String label = it.next();
                    if (type.slots.containsKey(label)) {
                        ;
                    } else if (this.underspecified) {
                        type.add(label, this.getType(label));
                    } else {
                        return this.throwUnificationException(t);
                    }
                }

                return this;
            }

        } else {
            return this.throwUnificationException(t);
        }
    }

    @Override
    public String toString() {

        if (this.slots.size() == 0) {
            if (this.underspecified) {
                return "struct";
            } else {
                return "{ }";
            }
        } else {
            StringBuilder b = new StringBuilder();
            b.append("{ ");
            for (Iterator<String> it = this.labels(); it.hasNext();) {
                String label = it.next();
                b.append(label);
                b.append(":");
                b.append(this.getType(label).toString());
                if (it.hasNext()) {
                    b.append(", ");
                } else if (this.underspecified) {
                    b.append(", ...");
                }
            }
            b.append(" }");
            return b.toString();
        }
    }

    @Override
    public Class<StructValue> getObjectClass() {

        return StructValue.class;
    }

}
