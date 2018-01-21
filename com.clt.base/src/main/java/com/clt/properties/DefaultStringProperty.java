package com.clt.properties;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */
public class DefaultStringProperty extends StringProperty {

    private String value;
    private String name;
    private String description;

    public DefaultStringProperty(String id, String name, String description) {
        this(id, name, description, null);
    }

    public DefaultStringProperty(String id, String name, String description, String value) {
        super(id);
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
    public String getValue() {

        return this.value;
    }

    @Override
    protected void setValueImpl(String value) {

        this.value = value;
    }
}
