/*
 * @(#)DefaultStringProperty.java
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

public class DefaultStringProperty
    extends StringProperty {

  private String value;
  private String name;
  private String description;


  public DefaultStringProperty(String id, String name, String description) {

    this(id, name, description, null);
  }


  public DefaultStringProperty(String id, String name, String description,
      String value) {

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
