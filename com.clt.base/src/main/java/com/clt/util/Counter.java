/*
 * @(#)Counter.java
 * Created on 21.02.05
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

package com.clt.util;

/**
 * A Counter is a modifiable integer value.
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

public class Counter {

  int count;


  /** Create a new Counter with a default value of <code>0</code>. */
  public Counter() {

    this(0);
  }


  /** Create a new Counter with the given value. */
  public Counter(int count) {

    this.count = count;
  }


  /** Get the current value of the counter. */
  public int get() {

    return this.count;
  }


  /** Set the current value of the counter. */
  public void set(int count) {

    this.count = count;
  }


  /** Increase the current value of the counter by <code>1</code>. */
  public void increase() {

    this.count++;
  }


  /** Decrease the current value of the counter by <code>1</code>. */
  public void decrease() {

    this.count--;
  }


  /** Add the given amount to the current value. */
  public void add(int amount) {

    this.count += amount;
  }


  /** Return the current value of the counter as a string. */
  @Override
  public String toString() {

    return String.valueOf(this.count);
  }
}
