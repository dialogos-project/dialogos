package com.clt.util;

/**
 * @author dabo
 *
 */
public class Wrapper<T> {

    private T value;

    public Wrapper() {

        this(null);
    }

    public Wrapper(T value) {

        this.value = value;
    }

    public T get() {

        return this.value;
    }

    public void set(T value) {

        this.value = value;
    }

    @Override
    public String toString() {

        return String.valueOf(this.value);
    }
}
