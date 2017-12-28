/*
 * @(#)Constant.java
 * Created on 20.02.2007 by dabo
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
public class Constant
    implements Values {

  private int value;


  public Constant(int value) {

    this.value = value;
  }


  public int[] getValues() {

    return new int[] { this.value };
  }

}
