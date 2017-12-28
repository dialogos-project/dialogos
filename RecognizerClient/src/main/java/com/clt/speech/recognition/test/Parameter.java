/*
 * @(#)Parameter.java
 * Created on 19.02.2007 by dabo
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

package com.clt.speech.recognition.test;

/**
 * @author dabo
 * 
 */
public class Parameter {

  private String name;
  private Values values;


  public Parameter(String name, Values values) {

    this.name = name;
    this.values = values;
  }


  public String getName() {

    return this.name;
  }


  public int[] getValues() {

    return this.values.getValues();
  }
}
