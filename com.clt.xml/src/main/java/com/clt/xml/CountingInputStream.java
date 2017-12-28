/*
 * @(#)CountingInputStream.java
 * Created on 15.09.2007 by dabo
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

package com.clt.xml;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Daniel Bobbert
 * 
 */
public class CountingInputStream
    extends FilterInputStream {

  private long position = 0;
  private long length = 0;


  public CountingInputStream(InputStream in) {

    this(in, -1);
  }


  public CountingInputStream(InputStream in, long length) {

    super(in);

    try {
      if (length == -1) {
        length = in.available();
      }
    } catch (IOException exn) {
      // ignore
    }
    if (length >= 0) {
      this.length = length;
    }
  }


  @Override
  public int read()
      throws IOException {

    int result = super.read();
    if (result >= 0) {
      this.position++;
    }
    return result;
  }


  @Override
  public int read(byte[] b, int off, int len)
      throws IOException {

    int bytesRead = super.read(b, off, len);
    if (bytesRead >= 0) {
      this.position += bytesRead;
    }
    return bytesRead;
  }


  @Override
  public int read(byte[] b)
      throws IOException {

    int bytesRead = super.read(b);
    if (bytesRead >= 0) {
      this.position += bytesRead;
    }
    return bytesRead;
  }


  @Override
  public long skip(long n)
      throws IOException {

    long bytesSkipped = super.skip(n);
    if (bytesSkipped >= 0) {
      this.position += bytesSkipped;
    }
    return bytesSkipped;
  }


  public long getLength() {

    return this.length;
  }


  public long getPosition() {

    return this.position;
  }
}
