/*
 * @(#)PrefixedReader.java
 * Created on Tue Jun 22 2004
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

package com.clt.script.parser;

import java.io.IOException;
import java.io.Reader;

/**
 * 
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

public class PrefixedReader extends Reader {

  Reader r;
  String prefix;

  int headerRead;
  int headerLength;


  public PrefixedReader(Reader r, String prefix) {

    this.r = r;
    this.prefix = prefix;

    this.headerRead = 0;
    this.headerLength = prefix == null ? 0 : prefix.length();
  }


  @Override
  public void close()
      throws IOException {

    this.r.close();
  }


  @Override
  public int read(char cbuf[], int off, int len)
      throws IOException {

    if (this.headerRead < this.headerLength) {
      int n = Math.min(this.headerLength - this.headerRead, len);
      this.prefix.getChars(this.headerRead, this.headerRead + n, cbuf, off);
      this.headerRead += n;

      if (n == len) {
        return n;
      }
      else {
        return n + this.r.read(cbuf, off + n, len - n);
      }
    }
    else {
      return this.r.read(cbuf, off, len);
    }
  }


  @Override
  public long skip(long skipChars)
      throws IOException {

    if (this.headerRead < this.headerLength) {
      long n = Math.min(this.headerLength - this.headerRead, skipChars);
      this.headerRead += skipChars;
      if (n == skipChars) {
        return n;
      }
      else {
        return n + this.r.skip(skipChars - n);
      }
    }
    else {
      return this.r.skip(skipChars);
    }
  }


  @Override
  public boolean ready()
      throws IOException {

    if (this.headerRead < this.headerLength) {
      return true;
    }
    else {
      return this.r.ready();
    }
  }
}
