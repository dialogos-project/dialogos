/*
 * @(#)Functions.java
 * Created on 04.04.2007 by dabo
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

package com.clt.diamant.graph;

public class Functions {

  private String name;
  private String script;


  public Functions(String name, String script) {

    this.name = name;
    this.script = script;
  }


  public String getName() {

    return this.name;
  }


  public void setName(String name) {

    this.name = name;
  }


  public String getScript() {

    return this.script;
  }


  public void setScript(String script) {

    this.script = script;
  }


  @Override
  public String toString() {

    return this.name;
  }
}