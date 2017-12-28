/*
 * @(#)DynamicLabel.java
 * Created on 26.07.2007 by dabo
 *
 * Copyright (c) CLT Sprachtechnologie GmbH.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CLT Sprachtechnologie GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CLT Sprachtechnologie GmbH.
 */

package com.clt.resources;

import java.util.Arrays;
import java.util.ResourceBundle;

import com.clt.util.StringTools;

/**
 * @author dabo
 * 
 */
public class DynamicLabel {

  private ResourceBundle bundle;
  private String key;
  private Object[] args;


  public DynamicLabel(ResourceBundle bundle, String key, Object... args) {

    if ((bundle == null) || (key == null)) {
      throw new IllegalArgumentException();
    }

    this.bundle = bundle;
    this.key = key;
    this.args = args;
    if (this.args == null) {
      this.args = new String[0];
    }
  }


  @Override
  public int hashCode() {

    int hashCode = this.key.hashCode();

    for (int i = 0; i < this.args.length; i++) {
      hashCode = hashCode * 31 + this.args[i].hashCode();
    }

    return hashCode;
  }


  @Override
  public boolean equals(Object o) {

    if (o instanceof DynamicLabel) {
      DynamicLabel l = (DynamicLabel)o;
      return this.key.equals(l.key) && Arrays.equals(this.args, l.args);
    }
    else {
      return false;
    }
  }


  @Override
  public String toString() {

    if (this.args.length == 0) {
      return this.bundle.getString(this.key);
    }
    else {
      return StringTools.format(this.bundle.getString(this.key), this.args);
    }
  }
}
