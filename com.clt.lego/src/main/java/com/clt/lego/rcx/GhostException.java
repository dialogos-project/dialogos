/*
 * @(#)GhostException.java
 * Created on 26.06.2007 by dabo
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

import com.clt.lego.BrickException;
import com.clt.resources.DynamicResourceBundle;
import com.clt.util.StringTools;

/**
 * @author dabo
 * 
 */
public class GhostException
    extends BrickException {

  /** Generic error */
  public static final int PBK_ERR_GENERIC = 0xE1000000;
  /** Bad parameters */
  public static final int PBK_ERR_BADPARM = 0xE1000001;
  /** Method not implemented */
  public static final int PBK_ERR_NOTIMPLEMENTED = 0xE1000002;
  /** Insufficient memory to perform the requested operation */
  public static final int PBK_ERR_NOMEMORY = 0xE1000003;
  /** Memory overflow - buffer size insufficient to hold requested data */
  public static final int PBK_ERR_OVERFLOW = 0xE1000004;

  /** Port (or protocol, or session) already open */
  public static final int PBK_ERR_ALREADYOPEN = 0xE1000101;
  /** Port (or protocol, or session) already closed */
  public static final int PBK_ERR_CLOSED = 0xE1000102;
  /** Requested service is incompatible with the current state */
  public static final int PBK_ERR_BUSY = 0xE1000103;
  /** No more items or devices could be found */
  public static final int PBK_ERR_NOMORE = 0xE1000104;
  /** Communication timeout occurred. Couldn't complete the requested operation */
  public static final int PBK_ERR_TIMEOUT = 0xE1000105;
  /** The object is in an invalid state */
  public static final int PBK_ERR_INVALIDSTATE = 0xE1000106;
  /** Item not found */
  public static final int PBK_ERR_NOTFOUND = 0xE1000107;
  /** Abort requested for the specified command */
  public static final int PBK_ERR_ABORT = 0xE1000108;

  /** Error opening port (or protocol, or session) */
  public static final int PBK_ERR_OPEN = 0xE1000110;
  /** Transmission error */
  public static final int PBK_ERR_SEND = 0xE1000111;
  /** Receive error */
  public static final int PBK_ERR_RECV = 0xE1000112;
  /** Error in <om IPbkPort::devicecontrol> */
  public static final int PBK_ERR_DEVICECONTROL = 0xE1000113;
  /** Error in <om IPbkPort::getthroughput> */
  public static final int PBK_ERR_GETTHROUGHPUT = 0xE1000114;
  /** Error in <om IPbkPort::settimeout> */
  public static final int PBK_ERR_SETTIMEOUT = 0xE1000115;
  /** Error in <om IPbkPort::gettimeout> */
  public static final int PBK_ERR_GETTIMEOUT = 0xE1000116;
  /** Error while creating a thread */
  public static final int PBK_ERR_CREATETHREAD = 0xE1000117;
  /** No port object was bound to protocol object */
  public static final int PBK_ERR_NOPORTSET = 0xE1000118;
  /** No protocol object was bound to session object */
  public static final int PBK_ERR_NOPROTOCOLSET = 0xE1000119;
  /** Error in <om IPbkPort::flush> */
  public static final int PBK_ERR_CLEAR = 0xE100011A;
  /** Error in <om IPbkPort::abort> */
  public static final int PBK_ERR_CANTABORT = 0xE100011B;
  /** Error in <om IPbkProtocol::sendcommand> */
  public static final int PBK_ERR_SENDCOMMAND = 0xE100011C;
  /** Device has been disconnected or other unrecoverable error */
  public static final int PBK_ERR_DISCONNECTED = 0xE100011D;
  /** Resource is already in use. */
  public static final int PBK_ERR_INUSE = 0xE100011E;

  // Protocol-specific errors

  /** Bad or missing protocol packet header */
  public static final int PBK_ERR_PROT_BADHEADER = 0xE1000201;
  /** Bad or protocol packet checksum */
  public static final int PBK_ERR_PROT_BADCHECKSUM = 0xE1000202;
  /** Bad packet length */
  public static final int PBK_ERR_PROT_BADPACKETLEN = 0xE1000203;
  /** Retry count exceeded */
  public static final int PBK_ERR_PROT_EXCEEDRETRIES = 0xE1000204;
  /** Negative acknowledge received */
  public static final int PBK_ERR_PROT_NACK = 0xE1000205;
  /** Statistics are currently disabled */
  public static final int PBK_ERR_PROT_STATSDISABLED = 0xE1000206;

  // Session-specific errors

  /** Request queue empty */
  public static final int PBK_ERR_SESS_NOREQUEST = 0xE1000301;

  /** Abort already requested for the specified PBK_HREQUEST */
  public static final int PBK_ERR_ABORT_ALREADYREQUESTED = 0xE1000302;
  /** No tower seems to be connected to the currently open protocol/port */
  public static final int PBK_ERR_DIAG_NOTOWER = 0xE1000340;

  private static DynamicResourceBundle resources = new DynamicResourceBundle(
        GhostException.class.getPackage().getName() + ".Ghost", null,
        GhostException.class.getClassLoader());


  public GhostException(int status) {

    super(GhostException.getErrorMessage(status));
  }


  protected static String getErrorMessage(int status) {

    String key = "0x" + StringTools.toHexString(status, 8).toUpperCase();
    String msg = GhostException.resources.getString(key);
    if ((msg != null) && !msg.equals(key)) {
      return msg;
    }
    else {
      return "Unknown Ghost "
        + ((status & 0xC0000000) != 0 ? "error " : "warning ") + key;
    }
  }

}
