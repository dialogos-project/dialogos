package com.clt.properties;

import java.awt.Color;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */
public class DefaultColorProperty extends ColorProperty {

    private Color value;
    private String name;
    private String description;

    public DefaultColorProperty(String id, String name, String description) {
        this(id, name, description, Color.black);
    }
    
    public DefaultColorProperty(String id, String name, String description, Color value) {
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
    public Color getValue() {

        return this.value;
    }

    @Override
    protected void setValueImpl(Color value) {

        this.value = value;
    }
}
