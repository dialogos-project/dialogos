/*
 * @(#)SRec.java
 * Created on 03.07.2007 by dabo
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

package com.clt.lego.rcx;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import com.clt.io.FileFormatException;

/**
 * @author dabo
 * 
 */
public class SRec {

  private static final int ltab[] = { 4, 4, 6, 8, 0, 4, 0, 8, 6, 4 };
  private static final int SREC_DATA_SIZE = 32;

  private static final int SEGMENT_BREAK = 1024;
  private static final int IMAGE_START = 0x8000;
  private static final int IMAGE_MAX_LENGTH = 0x8000;

  private int fLength;
  private int fStart;
  private byte[] fData;


  public SRec(byte[] data)
      throws IOException {

    this.fData = new byte[SRec.IMAGE_MAX_LENGTH];
    this.fStart = 0;
    this.fLength = 0;

    BufferedReader r =
      new BufferedReader(new InputStreamReader(new ByteArrayInputStream(data)));

    boolean strip = false;

    Segment segments[] = new Segment[2];
    for (int i = 0; i < segments.length; i++) {
      segments[i] = new Segment();
    }

    int prevAddr = -SRec.SEGMENT_BREAK;
    int prevCount = SRec.SEGMENT_BREAK;
    int segIndex = -1;
    int segStartAddr = 0;
    int imageIndex = -SRec.SEGMENT_BREAK;

    int lineNo = 0;
    String s;
    while ((s = r.readLine()) != null) {
      lineNo++;

      // remove blanks
      s = s.trim();

      // skip empty lines
      if (s.length() == 0) {
        continue;
      }

      Line line = this.decodeLine(s, lineNo);

      if (line.type == 0) {
        // Detect Firm0309.lgo header, set strip if found
        if (new String(line.data).equals("?LIB_VERSION_L00")) {
          strip = true;
        }
      }
      else if (line.type == 1) {
        // Process S-Record
        /* Start of a new segment? */
        if (line.addr - prevAddr >= SRec.SEGMENT_BREAK) {
          segIndex++;
          if (segIndex >= segments.length) {
            throw new FileFormatException("Too many segments");
          }

          segments[segIndex].length = 0;
          segStartAddr = line.addr;
          prevAddr = line.addr - prevCount;
          segments[segIndex].offset = imageIndex + prevCount;
        }

        if ((line.addr < SRec.IMAGE_START)
                        || (line.addr + line.data.length > SRec.IMAGE_START
                          + this.fData.length)) {
          throw new IOException("Image too large");
        }

        // Data is not necessarily contiguous so can't just accumulate
        // srec.counts.
        segments[segIndex].length = line.addr - segStartAddr + line.data.length;

        imageIndex += line.addr - prevAddr;
        System
          .arraycopy(line.data, 0, this.fData, imageIndex, line.data.length);
        prevAddr = line.addr;
        prevCount = line.data.length;
      }
      else if (line.type == 9) {
        // Process image entry point
        if ((line.addr < SRec.IMAGE_START)
          || (line.addr > SRec.IMAGE_START + SRec.IMAGE_MAX_LENGTH)) {
          throw new IOException("Illegal entry address");
        }

        this.fStart = line.addr;
      }
    }

    if (strip) {
      int pos;
      for (pos = SRec.IMAGE_MAX_LENGTH - 1; (pos >= 0)
        && (this.fData[pos] == 0); pos--) {
        ; // do nothing
      }
      segments[segIndex].length = pos + 1;
    }

    int length = 0;
    for (int i = 0; i <= segIndex; i++) {
      length += segments[segIndex].length;
    }

    if (length == 0) {
      throw new FileFormatException("Empty image");
    }

    this.fLength = segments[segIndex].offset + segments[segIndex].length;
  }


  private Line decodeLine(String line, int lineNo)
      throws FileFormatException {

    if (line.length() < 4) {
      throw new FileFormatException("Invalid SREC line at line " + lineNo);
    }

    if (line.charAt(0) != 'S') {
      throw new FileFormatException("Invalid SREC header in line " + lineNo);
    }

    Line srecLine = new Line();

    srecLine.type = SRec.readNum(line, 1, 1);
    int length = SRec.readNum(line, 2, 2);

    if (srecLine.type > 9) {
      throw new FileFormatException("Invalid SREC type in line " + lineNo);
    }

    int alen = SRec.ltab[srecLine.type];
    if (alen == 0) {
      throw new FileFormatException("Invalid SREC type in line " + lineNo);
    }
    if (line.length() < alen + 6) {
      throw new FileFormatException("SREC line too short in line " + lineNo);
    }
    if (length > alen + SRec.SREC_DATA_SIZE + 2) {
      throw new FileFormatException("SREC line too long in line " + lineNo);
    }
    if (line.length() != length * 2 + 4) {
      throw new FileFormatException("Invalid SREC line length in line "
        + lineNo
                  + ". Expected " + line.length() + " but got "
        + (length * 2 + 4)
                  + " characters.");
    }

    int sum = length;

    int pos = 4;

    srecLine.addr = SRec.readNum(line, pos, alen);
    for (int i = 0; i < alen; i += 2) {
      sum += SRec.readNum(line, pos + i, 2);
    }
    pos += alen;

    srecLine.data = new byte[(line.length() - pos - 2) / 2];
    for (int i = 0; i < srecLine.data.length; i++) {
      int value = SRec.readNum(line, pos + 2 * i, 2);
      srecLine.data[i] = (byte)value;
      sum += value;
    }

    sum += SRec.readNum(line, line.length() - 2, 2);

    if ((sum & 0xff) != 0xff) {
      throw new FileFormatException("Invalid SREC checksum in line " + lineNo);
    }

    return srecLine;
  }


  public byte[] getData() {

    return this.fData;
  }


  public int getLength() {

    return this.fLength;
  }


  public int getStart() {

    return this.fStart;
  }


  private static int readNum(String line, int offset, int length)
      throws FileFormatException {

    int sum = 0;

    for (int i = offset; i < offset + length; i++) {
      int n = line.charAt(i);
      sum = sum << 4;
      if ((n >= '0') && (n <= '9')) {
        sum += (n - '0');
      }
      else if ((n >= 'a') && (n <= 'f')) {
        sum += (n - 'a' + 10);
      }
      else if ((n >= 'A') && (n <= 'F')) {
        sum += (n - 'A' + 10);
      }
      else {
        throw new FileFormatException("Invalid character");
      }
    }

    return sum;
  }

  private static class Line {

    int type;
    int addr;
    byte[] data;
  }

  private static class Segment {

    int offset;
    int length;
  }
}
