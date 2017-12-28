/*
 * @(#)DefaultEnumProperty.java
 * Created on 08.06.05
 *
 * Copyright (c) 2005 CLT Sprachtechnologie GmbH.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CLT Sprachtechnologie GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CLT Sprachtechnologie GmbH.
 */

package com.clt.properties;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */

public class DefaultEnumProperty<E> extends EnumProperty<E> {
  private E[] values;
  private E value;
  private String name;
  private String description;


  /**
   * Creates a new Enum Property.
   * 
   * @param name
   *          label of the field
   * @param values
   *          values of the defined enum property.
   */
  public DefaultEnumProperty(String id, String name, String description, E[] values) {
    this(id, name, description, values, null);
  }

  public DefaultEnumProperty(String id, String name, String description, E[] values, E value) {
    super(id);
    this.value = value;
    this.name = name;
    this.description = description;

    if (values == null) {
      throw new IllegalArgumentException();
    }
    
    this.values = values;
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
  public E getValue() {
    return this.value;
  }


  @Override
  protected void setValueImpl(E value) {
    this.value = value;
  }


  @Override
  public E[] getPossibleValues() {

    return this.values;
  }


  public void setPossibleValues(E[] values) {
    if (values == null) {
      throw new IllegalArgumentException();
    }
    
    this.values = values;
    E value = this.getValue();
    if (value != null) {
      boolean found = false;
      for (int i = 0; (i < values.length) && !found; i++) {
        if (value.equals(values[i])) {
          found = true;
        }
      }
      if (!found) {
        this.setValueImpl(null);
      }
    }
    this.fireChange();
  }
}
