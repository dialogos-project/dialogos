/*
 * @(#)Protocol.java
 * Created on Mon Aug 16 2005
 *
 * Copyright (c) 2003 CLT Sprachtechnologie GmbH.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CLT Sprachtechnologie GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CLT Sprachtechnologie GmbH.
 */

package com.clt.dialog.client;

import java.io.IOException;

/**
 * 
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

interface Protocol {

  public static final int XML =
    ('X' << 24) | ('M' << 16) | ('L' << 8) | (' ' << 0);
  public static final int RAW_XML =
    ('<' << 24) | ('?' << 16) | ('x' << 8) | ('m' << 0);
  public static final int BINARY =
    ('B' << 24) | ('I' << 16) | ('N' << 8) | (' ' << 0);

  public static final String XML_MAIN = "messages";
  public static final String XML_NAME = "name";
  public static final String XML_VERSION = "version";
  public static final String XML_START = "start";
  public static final String XML_RESET = "reset";
  public static final String XML_PAUSE = "pause";
  public static final String XML_TIMEOUT = "timeout";
  public static final String XML_PING = "ping";
  public static final String XML_LOG = "log";
  public static final String XML_VALUE = "value";
  public static final String XML_RPC = "rpc";
  public static final String XML_RPC_ERR = "rpc_error";
  public static final String XML_RPC_RES = "rpc_result";
  public static final String XML_RPC_PROC = "procedure";
  public static final String XML_RPC_SRC = "source";
  public static final String XML_TIMEOUT_SIGNAL = "timeout_signal";

  public static final byte BIN_NAME = 1;
  public static final byte BIN_VERSION = 2;
  public static final byte BIN_START = 3;
  public static final byte BIN_RESET = 4;
  public static final byte BIN_PAUSE = 5;
  public static final byte BIN_TIMEOUT = 6;
  public static final byte BIN_PING = 7;
  public static final byte BIN_LOG = 8;
  public static final byte BIN_VALUE = 9;
  public static final byte BIN_RPC = 10;
  public static final byte BIN_RPC_ERR = 11;
  public static final byte BIN_RPC_RES = 12;
  public static final byte BIN_TIMEOUT_SIGNAL = 13;

  public static class Exception
        extends IOException {

    public Exception(String message) {

      super(message);
    }
  }
}