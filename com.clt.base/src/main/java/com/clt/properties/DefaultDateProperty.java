/*
 * @(#)DefaultDateProperty.java
 * Created on 07.02.2006 by dabo
 *
 * Copyright (c) 2006 CLT Sprachtechnologie GmbH.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CLT Sprachtechnologie GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CLT Sprachtechnologie GmbH.
 */

package com.clt.properties;

import java.util.Date;

/**
 * @author dabo
 * 
 */
public class DefaultDateProperty
    extends DateProperty {

  private Date value;
  private String name;
  private String description;


  public DefaultDateProperty(String id, String name, String description) {

    this(id, name, description, new Date());
  }


  public DefaultDateProperty(String id, String name, String description,
      Date value) {

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
  public Date getValue() {

    return this.value;
  }


  @Override
  protected void setValueImpl(Date value) {

    this.value = value;
  }
}
