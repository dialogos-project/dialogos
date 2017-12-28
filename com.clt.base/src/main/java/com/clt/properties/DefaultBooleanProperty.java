/*
 * @(#)DefaultBooleanProperty.java
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

public class DefaultBooleanProperty extends BooleanProperty {

  private boolean value;
  private String name;
  private String description;


  public DefaultBooleanProperty(String id, String name, String description) {
    this(id, name, description, false);
  }


  public DefaultBooleanProperty(String id, String name, String description, boolean value) {
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
  public boolean getValue() {
    return this.value;
  }


  @Override
  protected void setValueImpl(boolean value) {
    this.value = value;
  }
}
