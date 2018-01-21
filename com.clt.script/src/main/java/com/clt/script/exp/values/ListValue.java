package com.clt.script.exp.values;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.clt.script.exp.Type;
import com.clt.script.exp.Value;
import com.clt.script.exp.types.ListType;

/**
 * A list of values.
 *
 * @author Daniel Bobbert
 * @version 1.0
 */
public class ListValue extends Value implements Iterable<Value> {

    private Value[] elements;

    public ListValue(Value[] elements) {

        if (elements == null) {
            throw new IllegalArgumentException();
        }

        this.elements = new Value[elements.length];
        for (int i = 0; i < elements.length; i++) {
            this.elements[i] = elements[i];
        }
    }

    public ListValue(Collection<? extends Value> elements) {

        this.elements = elements.toArray(new Value[elements.size()]);
    }

    @Override
    protected Value copyValue() {

        Value elems[] = new Value[this.elements.length];
        for (int i = 0; i < elems.length; i++) {
            elems[i] = this.elements[i].copy();
        }
        return new ListValue(elems);
    }

    @Override
    public Type getType() {

        return new ListType();
    }

    @Override
    public boolean equals(Object v) {

        if (v == this) {
            return true;
        } else if (v instanceof ListValue) {
            ListValue l = (ListValue) v;
            if (l.size() == this.size()) {
                for (int i = 0; i < this.size(); i++) {
                    if (!this.get(i).equals(l.get(i))) {
                        return false;
                    }
                }
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {

        int hash = 0;
        for (int i = 0; i < this.size(); i++) {
            hash = hash ^ this.get(i).hashCode();
        }
        return hash;
    }

    public int size() {

        return this.elements.length;
    }

    /**
     * Return the nth element of the list. A list of length n has elements 0 ..
     * n-1
     */
    public Value get(int n) {

        if ((n < 0) || (n >= this.elements.length)) {
            throw new IndexOutOfBoundsException();
        }
        return this.elements[n];
    }

    /**
     * Return an iteration of all list elements.
     */
    public Iterator<Value> iterator() {

        return new Iterator<Value>() {

            int index = 0;

            public boolean hasNext() {

                return this.index < ListValue.this.size();
            }

            public Value next() {

                if (!this.hasNext()) {
                    throw new NoSuchElementException();
                }
                return ListValue.this.get(this.index++);
            }

            public void remove() {

                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Return a sublist consisting of elements <code>begin</code> to
     * <code>end-1</code>. A list of length n has elements 0 .. n-1.
     * <code>subList(1, 3)</code> thus returns a sublist containing the second
     * and third element of the list.
     */
    public ListValue subList(int begin, int end) {

        if (end < begin) {
            throw new IllegalArgumentException("end < begin in sublist()");
        } else if ((begin < 0) || (end > this.size())) {
            throw new IndexOutOfBoundsException();
        }

        Value v[] = new Value[end - begin];
        for (int i = 0; i < v.length; i++) {
            v[i] = this.get(begin + i);
        }
        return new ListValue(v);
    }

    @Override
    public String toString() {

        StringBuilder b = new StringBuilder();
        b.append("[ ");
        for (int i = 0; i < this.size(); i++) {
            if (i > 0) {
                b.append(", ");
            }
            b.append(this.get(i).toString());
        }
        b.append(" ]");
        return b.toString();
    }

    @Override
    public void prettyPrint(PrintWriter w, int inset) {

        w.println("[");
        for (int n = 0; n < this.size(); n++) {
            for (int i = 0; i < inset + 2; i++) {
                w.print(' ');
            }
            this.get(n).prettyPrint(w, inset + 2);
            if (n < this.size() - 1) {
                w.println(",");
            }
        }
        for (int i = 0; i < inset; i++) {
            w.print(' ');
        }
        w.print("]");
        this.printAttributes(w);
    }

    @Override
    public Object getReadableValue() {
        ArrayList<Object> newList = new ArrayList<Object>();
        for (Value element : elements) {
            newList.add(element.getReadableValue());
        }

        return newList;
    }
}
