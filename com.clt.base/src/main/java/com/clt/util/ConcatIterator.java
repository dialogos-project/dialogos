/*
 * @(#)ConcatIterator.java
 * Created on 04.11.04
 *
 * Copyright (c) 2004 CLT Sprachtechnologie GmbH.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CLT Sprachtechnologie GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CLT Sprachtechnologie GmbH.
 */

package com.clt.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A "meta" iterator that returns the concatenated elements of several
 * iterators.
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */
public class ConcatIterator<T>
    implements Iterator<T> {

  private Iterator<? extends Iterator<? extends T>> iterators;

  private Iterator<? extends T> currentIterator;

  private Iterator<? extends T> lastIterator;


  public ConcatIterator(Iterator<? extends T>... iterators) {

    this(Arrays.asList(iterators).iterator());
  }


  public ConcatIterator(Collection<? extends Iterator<? extends T>> iterators) {

    this(iterators.iterator());
  }


  public ConcatIterator(Iterator<? extends Iterator<? extends T>> iterators) {

    this.iterators = iterators;
    this.currentIterator = null;
    this.lastIterator = null;
    this.selectNextIterator();
  }


  public boolean hasNext() {

    while (this.currentIterator != null) {
      if (this.currentIterator.hasNext()) {
        return true;
      }
      else {
        this.selectNextIterator();
      }
    }
    return false;
  }


  public T next() {

    if (!this.hasNext()) {
      throw new NoSuchElementException();
    }
    this.lastIterator = this.currentIterator;
    return this.currentIterator.next();
  }


  public void remove() {

    if (this.lastIterator == null) {
      throw new IllegalStateException(
        "remove() called without preceding next()");
    }
    this.lastIterator.remove();
    this.lastIterator = null;
  }


  private void selectNextIterator() {

    // possible cast exception
    if (this.iterators.hasNext()) {
      this.currentIterator = this.iterators.next();
    }
    else {
      this.currentIterator = null;
    }
  }
}
