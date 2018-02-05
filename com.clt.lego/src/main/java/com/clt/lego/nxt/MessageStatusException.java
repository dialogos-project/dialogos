/*
 * @(#)MessageStatusException.java
 * Created on 12.04.2007 by dabo
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

package com.clt.lego.nxt;

import com.clt.lego.BrickException;
import com.clt.resources.DynamicResourceBundle;
import com.clt.util.StringTools;

/**
 * @author dabo
 * 
 */
public class MessageStatusException
    extends BrickException {

  public static final byte TRANSACTION_IN_PROGRESS = (byte)0x20;
  public static final byte MAILBOX_QUEUE_EMPTY = (byte)0x40;
  public static final byte REQUEST_FAILED = (byte)0xBD;
  public static final byte UNKNOWN_COMMAND_OPCODE = (byte)0xBE;
  public static final byte INSANE_PACKET = (byte)0xBF;
  public static final byte VALUE_OUT_OF_RANGE = (byte)0xC0;
  public static final byte COMMUNICATION_BUS_ERROR = (byte)0xDD;
  public static final byte COMMUNICATION_BUFFER_FULL = (byte)0xDE;
  public static final byte INVALID_CONNECTION = (byte)0xDF;
  public static final byte CHANNEL_BUSY = (byte)0xE0;
  public static final byte NO_ACTIVE_PROGRAM = (byte)0xEC;
  public static final byte ILLEGAL_SIZE = (byte)0xED;
  public static final byte ILLEGAL_MAILBOX_ID = (byte)0xEE;
  public static final byte INVALID_FIELD = (byte)0xEF;
  public static final byte BAD_IO = (byte)0xF0;
  public static final byte OUT_OF_MEMORY = (byte)0xFB;
  public static final byte BAD_ARGUMENTS = (byte)0xFF;

  private static DynamicResourceBundle resources = new DynamicResourceBundle(
        MessageStatusException.class.getPackage().getName() + ".Message", null,
        MessageStatusException.class.getClassLoader());


  public MessageStatusException(byte status) {

    super(MessageStatusException.getErrorMessage(status));
  }


  protected static String getErrorMessage(byte status) {

    String key = "0x" + StringTools.toHexString(status, 2).toUpperCase();
    String msg = MessageStatusException.resources.getString(key);
    if ((msg != null) && !msg.equals(key)) {
      return msg;
    }
    else {
      return "Unknown message status error " + key;
    }
  }
}
