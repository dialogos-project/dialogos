/*
 * @(#)DecoderInputStream.java
 * Created on Mon Dec 02 2002
 *
 * Copyright (c) 2002 CLT Sprachtechnologie GmbH.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CLT Sprachtechnologie GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CLT Sprachtechnologie GmbH.
 */

package com.clt.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */

public class DecoderInputStream
    extends FilterInputStream {

  static byte[] keys =
    {
            120, -77, -108, 57, -48, 5, -46, 121, 13, -80, -78, 6, 12, -114,
      -99,
            -10, 16, 59, -97, 19, 8, -61, 115, -27, -41, -30, 26, -25, 20, -36,
      -9, -89, 80, -26,
            34, 45, -124, 37, -37, 43, -73, 11, 88, -82, 124, 17, -118, 113,
      -93, 49, -127, 84, 52,
            53, 96, 86, -1, 25, -121, 67, -34, 63, 36, -95, 64, 4, 90, 97,
      -115, 33, -56, -88,
            -111, -11, 100, 75, -86, 65, -53, 79, -6, 118, 73, -60, 0, 85, 108,
      87, -122, -106, 55,
            42, 81, 35, -23, -47, 54, 10, -45, 104, -58, -33, 102, -87, -75,
      -40, 106, -12, -90,
            -65, 110, -28, 95, 123, 27, -123, 116, -72, -59, -102, -39, -74,
      -112, 1, -44, -98, 66,
            -79, -32, 76, 105, 61, -94, -49, -92, -31, 46, -21, 2, 47, 112, 68,
      103, -113, 69, 92,
            50, -109, -126, 91, -119, -105, -104, 122, -19, 125, 119, 126, 28,
      18, -96, -117, 51,
            74, 82, -91, -62, 38, 111, -22, -107, 9, -8, -83, 39, 89, -54, -63,
      7, -16, 70, 94, 48,
            40, 117, -120, -70, 71, 72, -67, 98, 56, 31, 127, 29, -110, -64,
      -101, -2, -57, 78,
            -71, 15, -76, -3, -51, -50, 22, -52, 14, 62, -66, 41, -43, -42, 3,
      44, -29, 58, 93,
            -85, -35, 60, -125, 83, -38, -69, 21, 32, -103, 114, 30, -24, 107,
      -128, -81, -20, 101,
            -18, -17, 99, -15, -116, -13, 24, -84, -14, 77, -100, -7, -68, -5,
      -4, 109, -55, 23 };


  public static byte getKey(int index) {

    return DecoderInputStream.keys[(index < 0 ? index + 256 : index)
      % DecoderInputStream.keys.length];
  }

  private int index;

  private byte lastbyte;

  private byte[] seed;

  private int mark_index;

  private byte mark_lastbyte;


  public DecoderInputStream(InputStream in) {

    this(in, null);
  }


  public DecoderInputStream(InputStream in, int seed) {

    this(in, new byte[] { (byte)(seed >> 24), (byte)(seed >> 16),
      (byte)(seed >> 8),
                (byte)(seed >> 0) });
  }


  public DecoderInputStream(InputStream in, byte[] seed) {

    super(in);

    this.index = 0;
    this.lastbyte = 0;
    this.seed = seed;
    if ((seed == null) || (seed.length == 0)) {
      this.seed = new byte[] { 0 };
    }

    this.mark_index = this.index;
    this.mark_lastbyte = this.lastbyte;
  }


  private byte step() {

    int result =
      DecoderInputStream.getKey(this.index)
        ^ this.seed[this.index % this.seed.length];
    this.index =
      (this.index + 1) % (DecoderInputStream.keys.length * this.seed.length);
    return (byte)result;
  }


  @Override
  public synchronized int read()
      throws IOException {

    int i = this.in.read();
    if (i != -1) {
      int temp =
        DecoderInputStream.getKey(((byte)i) ^ this.lastbyte ^ this.step());
      this.lastbyte = (byte)i;
      i = temp;

      // aus historischen Gruenden gibt read einen int zurueck. Dabei muss
      // das gelesene byte
      // als int zwischen 0 und 255 interpretiert werden.
      if (i < 0) {
        i += 256;
      }
    }

    return i;
  }


  @Override
  public synchronized int read(byte b[])
      throws IOException {

    return this.read(b, 0, b.length);
  }


  @Override
  public synchronized int read(byte b[], int offset, int length)
      throws IOException {

    int bytesRead = this.in.read(b, offset, length);
    if (bytesRead <= 0) {
      return bytesRead;
    }

    for (int i = 0; i < bytesRead; i++) {
      byte temp =
        DecoderInputStream.getKey(b[offset + i] ^ this.lastbyte ^ this.step());
      this.lastbyte = b[offset + i];
      b[offset + i] = temp;
    }

    return bytesRead;
  }

  private static final long skipBufferSize = 4096;


  @Override
  public synchronized long skip(long n)
      throws IOException {

    if (n < 0) {
      throw new IllegalArgumentException("Can't skip less than 0 bytes");
    }

    // wir lesen in ein anonymes Array, damit die Dekodierung nicht stolpert
    long bytesSkipped = 0;
    int size = (int)Math.min(n, DecoderInputStream.skipBufferSize);
    byte[] b = new byte[size];
    while (size > 0) {
      int read = this.read(b, 0, size);
      bytesSkipped += read;
      n -= read;
      size = (int)Math.min(n, DecoderInputStream.skipBufferSize);
    }
    return bytesSkipped;
  }


  @Override
  public synchronized void mark(int readlimit) {

    super.mark(readlimit);
    this.mark_index = this.index;
    this.mark_lastbyte = this.lastbyte;
  }


  @Override
  public synchronized void reset()
      throws IOException {

    super.reset();
    this.index = this.mark_index;
    this.lastbyte = this.mark_lastbyte;
  }


  // we support mark, if the underlying stream supports it.
  // All we have to do, is remember to lastbast and index at the mark.
  @Override
  public boolean markSupported() {

    return super.markSupported();
  }
}
