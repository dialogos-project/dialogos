/*
 * @(#)Wrapper.java
 * Created on 23.04.2007 by dabo
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

package com.clt.util;

/**
 * @author dabo
 * 
 */
public class Wrapper<T> {

  private T value;


  public Wrapper() {

    this(null);
  }


  public Wrapper(T value) {

    this.value = value;
  }


  public T get() {

    return this.value;
  }


  public void set(T value) {

    this.value = value;
  }


  @Override
  public String toString() {

    return String.valueOf(this.value);
  }
}
