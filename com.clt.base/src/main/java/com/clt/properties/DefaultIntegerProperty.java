package com.clt.properties;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */
public class DefaultIntegerProperty extends IntegerProperty {

    private int value;
    private String name;
    private String description;

    public DefaultIntegerProperty(String id, String name, String description) {

        this(id, name, description, 0);
    }

    public DefaultIntegerProperty(String id, String name, String description, int value) {

        this(id, name, description, 0, 0, value);
    }

    public DefaultIntegerProperty(String id, String name, String description, int min, int max, int value) {
        super(id, min, max);
        this.value = value;
        this.name = name;
        this.description = description;
    }

    @Override
    public String getName() {

        return this.name != null ? this.name : super.getName();
    }

    @Override
    public String getDescription() {

        return this.description;
    }

    @Override
    public int getValue() {

        return this.value;
    }

    @Override
    protected void setValueImpl(int value) {

        this.value = value;
    }
}
