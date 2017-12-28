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

import java.io.File;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */

public class DefaultFileProperty extends FileProperty {

  private File value;
  private String name;
  private String description;


  /**
   * @param name
   *          Name of the default file property. Will be displayed by
   *          PropertySet.createPropertyPanel as a label in front of the field,
   *          where the name of the file has to be entered.
   */
  public DefaultFileProperty(String id, String name, String description) {

    this(id, name, description, new File(System.getProperty("user.dir")));
  }


  public DefaultFileProperty(String id, String name, String description,
      File value) {

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
  public File getValue() {

    return this.value;
  }


  @Override
  protected void setValueImpl(File value) {

    this.value = value;
  }
}
