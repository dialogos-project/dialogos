/*
 * @(#)DefaultIntegerProperty.java
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

public class DefaultIntegerProperty
    extends IntegerProperty {

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
