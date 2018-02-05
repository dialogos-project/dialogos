/*
 * @(#)NiVisaStatus.java
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

public class NiVisaStatusException
    extends BrickException {

  public static final int VI_SUCCESS = 0x00000000; // Operation completed
                                                   // successfully.

  public static final int VI_SUCCESS_EVENT_EN = 0x3FFF0002; // Specified event
                                                            // is already
                                                            // enabled for at
                                                            // least one of the
                                                            // specified
                                                            // mechanisms.

  public static final int VI_SUCCESS_EVENT_DIS = 0x3FFF0003; // Specified event
                                                             // is already
                                                             // disabled for at
                                                             // least one of the
                                                             // specified
                                                             // mechanisms.

  public static final int VI_SUCCESS_QUEUE_EMPTY = 0x3FFF0004; // Operation
                                                               // completed
                                                               // successfully,
                                                               // but queue was
                                                               // already empty.

  public static final int VI_SUCCESS_TERM_CHAR = 0x3FFF0005; // The specified
                                                             // termination
                                                             // character was
                                                             // read.

  public static final int VI_SUCCESS_MAX_CNT = 0x3FFF0006; // The number of
                                                           // bytes read is
                                                           // equal to the input
                                                           // count.

  public static final int VI_WARN_QUEUE_OVERFLOW = 0x3FFF000C; // VISA received
                                                               // more event
                                                               // information of
                                                               // the specified
                                                               // type than the
                                                               // configured
                                                               // queue size
                                                               // could hold.

  public static final int VI_WARN_CONFIG_NLOADED = 0x3FFF0077; // The specified
                                                               // configuration
                                                               // either does
                                                               // not exist or
                                                               // could not be
                                                               // loaded; using
                                                               // VISA-specified
                                                               // defaults.

  public static final int VI_SUCCESS_DEV_NPRESENT = 0x3FFF007D; // Session
                                                                // opened
                                                                // successfully,
                                                                // but the
                                                                // device at the
                                                                // specified
                                                                // address is
                                                                // not
                                                                // responding.

  public static final int VI_SUCCESS_TRIG_MAPPED = 0x3FFF007E; // The path from
                                                               // trigSrc to
                                                               // trigDest is
                                                               // already
                                                               // mapped.

  public static final int VI_SUCCESS_QUEUE_NEMPTY = 0x3FFF0080; // Wait
                                                                // terminated
                                                                // successfully
                                                                // on receipt of
                                                                // an event
                                                                // notification.
                                                                // There is
                                                                // still at
                                                                // least one
                                                                // more event
                                                                // occurrence of
                                                                // the requested
                                                                // type(s)
                                                                // available for
                                                                // this session.

  public static final int VI_WARN_NULL_OBJECT = 0x3FFF0082; // The specified
                                                            // object reference
                                                            // is uninitialized.

  public static final int VI_WARN_NSUP_ATTR_STATE = 0x3FFF0084; // Although the
                                                                // specified
                                                                // state of the
                                                                // attribute is
                                                                // valid, it is
                                                                // not supported
                                                                // by this
                                                                // resource
                                                                // implementation.

  public static final int VI_WARN_UNKNOWN_STATUS = 0x3FFF0085; // The status
                                                               // code passed to
                                                               // the operation
                                                               // could not be
                                                               // interpreted.

  public static final int VI_WARN_NSUP_BUF = 0x3FFF0088; // The specified buffer
                                                         // is not supported.

  public static final int VI_SUCCESS_NCHAIN = 0x3FFF0098; // Event handled
                                                          // successfully. Do
                                                          // not invoke any
                                                          // other handlers on
                                                          // this session for
                                                          // this event.

  public static final int VI_SUCCESS_NESTED_SHARED = 0x3FFF0099; // Operation
                                                                 // completed
                                                                 // successfully,
                                                                 // and this
                                                                 // session has
                                                                 // nested
                                                                 // shared
                                                                 // locks.

  public static final int VI_SUCCESS_NESTED_EXCLUSIVE = 0x3FFF009A; // Operation
                                                                    // completed
                                                                    // successfully,
                                                                    // and this
                                                                    // session
                                                                    // has
                                                                    // nested
                                                                    // exclusive
                                                                    // locks.

  public static final int VI_SUCCESS_SYNC = 0x3FFF009B; // Asynchronous
                                                        // operation request was
                                                        // actually performed
                                                        // synchronously.

  public static final int VI_WARN_EXT_FUNC_NIMPL = 0x3FFF00A9; // The operation
                                                               // succeeded, but
                                                               // a lower level
                                                               // driver did not
                                                               // implement the
                                                               // extended
                                                               // functionality.

  public static final int VI_ERROR_SYSTEM_ERROR = 0xBFFF0000; // Unknown system
                                                              // error
                                                              // (miscellaneous
                                                              // error).

  public static final int VI_ERROR_INV_OBJECT = 0xBFFF000E; // The given session
                                                            // or object
                                                            // reference is
                                                            // invalid.

  public static final int VI_ERROR_RSRC_LOCKED = 0xBFFF000F; // Specified type
                                                             // of lock cannot
                                                             // be obtained or
                                                             // specified
                                                             // operation cannot
                                                             // be performed,
                                                             // because the
                                                             // resource is
                                                             // locked.

  public static final int VI_ERROR_INV_EXPR = 0xBFFF0010; // Invalid expression
                                                          // specified for
                                                          // search.

  public static final int VI_ERROR_RSRC_NFOUND = 0xBFFF0011; // Insufficient
                                                             // location
                                                             // information or
                                                             // the device or
                                                             // resource is not
                                                             // present in the
                                                             // system.

  public static final int VI_ERROR_INV_RSRC_NAME = 0xBFFF0012; // Invalid
                                                               // resource
                                                               // reference
                                                               // specified.
                                                               // Parsing error.

  public static final int VI_ERROR_INV_ACC_MODE = 0xBFFF0013; // Invalid access
                                                              // mode.

  public static final int VI_ERROR_TMO = 0xBFFF0015; // Timeout expired before
                                                     // operation completed.

  public static final int VI_ERROR_CLOSING_FAILED = 0xBFFF0016; // Unable to
                                                                // deallocate
                                                                // the
                                                                // previously
                                                                // allocated
                                                                // data
                                                                // structures
                                                                // corresponding
                                                                // to this
                                                                // session or
                                                                // object
                                                                // reference.

  public static final int VI_ERROR_INV_DEGREE = 0xBFFF001B; // Specified degree
                                                            // is invalid.

  public static final int VI_ERROR_INV_JOB_ID = 0xBFFF001C; // Specified job
                                                            // identifier is
                                                            // invalid.

  public static final int VI_ERROR_NSUP_ATTR = 0xBFFF001D; // The specified
                                                           // attribute is not
                                                           // defined or
                                                           // supported by the
                                                           // referenced
                                                           // session, event, or
                                                           // find list.

  public static final int VI_ERROR_NSUP_ATTR_STATE = 0xBFFF001E; // The
                                                                 // specified
                                                                 // state of the
                                                                 // attribute is
                                                                 // not valid,
                                                                 // or is not
                                                                 // supported as
                                                                 // defined by
                                                                 // the session,
                                                                 // event, or
                                                                 // find list.

  public static final int VI_ERROR_ATTR_READONLY = 0xBFFF001F; // The specified
                                                               // attribute is
                                                               // Read Only.

  public static final int VI_ERROR_INV_LOCK_TYPE = 0xBFFF0020; // The specified
                                                               // type of lock
                                                               // is not
                                                               // supported by
                                                               // this resource.

  public static final int VI_ERROR_INV_ACCESS_KEY = 0xBFFF0021; // The access
                                                                // key to the
                                                                // resource
                                                                // associated
                                                                // with this
                                                                // session is
                                                                // invalid.

  public static final int VI_ERROR_INV_EVENT = 0xBFFF0026; // Specified event
                                                           // type is not
                                                           // supported by the
                                                           // resource.

  public static final int VI_ERROR_INV_MECH = 0xBFFF0027; // Invalid mechanism
                                                          // specified.

  public static final int VI_ERROR_HNDLR_NINSTALLED = 0xBFFF0028; // A handler
                                                                  // is not
                                                                  // currently
                                                                  // installed
                                                                  // for the
                                                                  // specified
                                                                  // event.

  public static final int VI_ERROR_INV_HNDLR_REF = 0xBFFF0029; // The given
                                                               // handler
                                                               // reference is
                                                               // invalid.

  public static final int VI_ERROR_INV_CONTEXT = 0xBFFF002A; // Specified event
                                                             // context is
                                                             // invalid.

  public static final int VI_ERROR_QUEUE_OVERFLOW = 0xBFFF002D; // The event
                                                                // queue for the
                                                                // specified
                                                                // type has
                                                                // overflowed
                                                                // (usually due
                                                                // to previous
                                                                // events not
                                                                // having been
                                                                // closed).

  public static final int VI_ERROR_NENABLED = 0xBFFF002F; // The session must be
                                                          // enabled for events
                                                          // of the specified
                                                          // type in order to
                                                          // receive them.

  public static final int VI_ERROR_ABORT = 0xBFFF0030; // The operation was
                                                       // aborted.

  public static final int VI_ERROR_RAW_WR_PROT_VIOL = 0xBFFF0034; // Violation
                                                                  // of raw
                                                                  // write
                                                                  // protocol
                                                                  // occurred
                                                                  // during
                                                                  // transfer.

  public static final int VI_ERROR_RAW_RD_PROT_VIOL = 0xBFFF0035; // Violation
                                                                  // of raw read
                                                                  // protocol
                                                                  // occurred
                                                                  // during
                                                                  // transfer.

  public static final int VI_ERROR_OUTP_PROT_VIOL = 0xBFFF0036; // Device
                                                                // reported an
                                                                // output
                                                                // protocol
                                                                // error during
                                                                // transfer.

  public static final int VI_ERROR_INP_PROT_VIOL = 0xBFFF0037; // Device
                                                               // reported an
                                                               // input protocol
                                                               // error during
                                                               // transfer.

  public static final int VI_ERROR_BERR = 0xBFFF0038; // Bus error occurred
                                                      // during transfer.

  public static final int VI_ERROR_IN_PROGRESS = 0xBFFF0039; // Unable to queue
                                                             // the asynchronous
                                                             // operation
                                                             // because there is
                                                             // already an
                                                             // operation in
                                                             // progress.

  public static final int VI_ERROR_INV_SETUP = 0xBFFF003A; // Unable to start
                                                           // operation because
                                                           // setup is invalid
                                                           // (due to attributes
                                                           // being set to an
                                                           // inconsistent
                                                           // state).

  public static final int VI_ERROR_QUEUE_ERROR = 0xBFFF003B; // Unable to queue
                                                             // asynchronous
                                                             // operation.

  public static final int VI_ERROR_ALLOC = 0xBFFF003C; // Insufficient system
                                                       // resources to perform
                                                       // necessary memory
                                                       // allocation.

  public static final int VI_ERROR_INV_MASK = 0xBFFF003D; // Invalid buffer mask
                                                          // specified.

  public static final int VI_ERROR_IO = 0xBFFF003E; // Could not perform
                                                    // operation because of I/O
                                                    // error.

  public static final int VI_ERROR_INV_FMT = 0xBFFF003F; // A format specifier
                                                         // in the format string
                                                         // is invalid.

  public static final int VI_ERROR_NSUP_FMT = 0xBFFF0041; // A format specifier
                                                          // in the format
                                                          // string is not
                                                          // supported.

  public static final int VI_ERROR_LINE_IN_USE = 0xBFFF0042; // The specified
                                                             // trigger line is
                                                             // currently in
                                                             // use.

  public static final int VI_ERROR_NSUP_MODE = 0xBFFF0046; // The specified mode
                                                           // is not supported
                                                           // by this VISA
                                                           // implementation.

  public static final int VI_ERROR_SRQ_NOCCURRED = 0xBFFF004A; // Service
                                                               // request has
                                                               // not been
                                                               // received for
                                                               // the session.

  public static final int VI_ERROR_INV_SPACE = 0xBFFF004E; // Invalid address
                                                           // space specified.

  public static final int VI_ERROR_INV_OFFSET = 0xBFFF0051; // Invalid offset
                                                            // specified.

  public static final int VI_ERROR_INV_WIDTH = 0xBFFF0052; // Invalid source or
                                                           // destination width
                                                           // specified.

  public static final int VI_ERROR_NSUP_OFFSET = 0xBFFF0054; // Specified offset
                                                             // is not
                                                             // accessible from
                                                             // this hardware.

  public static final int VI_ERROR_NSUP_VAR_WIDTH = 0xBFFF0055; // Cannot
                                                                // support
                                                                // source and
                                                                // destination
                                                                // widths that
                                                                // are
                                                                // different.

  public static final int VI_ERROR_WINDOW_NMAPPED = 0xBFFF0057; // The specified
                                                                // session is
                                                                // not currently
                                                                // mapped.

  public static final int VI_ERROR_RESP_PENDING = 0xBFFF0059; // A previous
                                                              // response is
                                                              // still pending,
                                                              // causing a
                                                              // multiple query
                                                              // error.

  public static final int VI_ERROR_NLISTENERS = 0xBFFF005F; // No Listeners
                                                            // condition is
                                                            // detected (both
                                                            // NRFD and NDAC are
                                                            // deasserted).

  public static final int VI_ERROR_NCIC = 0xBFFF0060; // The interface
                                                      // associated with this
                                                      // session is not
                                                      // currently the
                                                      // controller in charge.

  public static final int VI_ERROR_NSYS_CNTLR = 0xBFFF0061; // The interface
                                                            // associated with
                                                            // this session is
                                                            // not the system
                                                            // controller.

  public static final int VI_ERROR_NSUP_OPER = 0xBFFF0067; // The given session
                                                           // or object
                                                           // reference does not
                                                           // support this
                                                           // operation.

  public static final int VI_ERROR_INTR_PENDING = 0xBFFF0068; // An interrupt is
                                                              // still pending
                                                              // from a previous
                                                              // call.

  public static final int VI_ERROR_ASRL_PARITY = 0xBFFF006A; // A parity error
                                                             // occurred during
                                                             // transfer.

  public static final int VI_ERROR_ASRL_FRAMING = 0xBFFF006B; // A framing error
                                                              // occurred during
                                                              // transfer.

  public static final int VI_ERROR_ASRL_OVERRUN = 0xBFFF006C; // An overrun
                                                              // error occurred
                                                              // during
                                                              // transfer. A
                                                              // character was
                                                              // not read from
                                                              // the hardware
                                                              // before the next
                                                              // character
                                                              // arrived.

  public static final int VI_ERROR_TRIG_NMAPPED = 0xBFFF006E; // The path from
                                                              // trigSrc to
                                                              // trigDest is not
                                                              // currently
                                                              // mapped.

  public static final int VI_ERROR_NSUP_ALIGN_OFFSET = 0xBFFF0070; // The
                                                                   // specified
                                                                   // offset is
                                                                   // not
                                                                   // properly
                                                                   // aligned
                                                                   // for the
                                                                   // access
                                                                   // width of
                                                                   // the
                                                                   // operation.

  public static final int VI_ERROR_USER_BUF = 0xBFFF0071; // A specified user
                                                          // buffer is not valid
                                                          // or cannot be
                                                          // accessed for the
                                                          // required size.

  public static final int VI_ERROR_RSRC_BUSY = 0xBFFF0072; // The resource is
                                                           // valid, but VISA
                                                           // cannot currently
                                                           // access it.

  public static final int VI_ERROR_NSUP_WIDTH = 0xBFFF0076; // Specified width
                                                            // is not supported
                                                            // by this hardware.

  public static final int VI_ERROR_INV_PARAMETER = 0xBFFF0078; // The value of
                                                               // some parameter
                                                               // - which
                                                               // parameter is
                                                               // not known - is
                                                               // invalid.

  public static final int VI_ERROR_INV_PROT = 0xBFFF0079; // The protocol
                                                          // specified is
                                                          // invalid.

  public static final int VI_ERROR_INV_SIZE = 0xBFFF007B; // Invalid size of
                                                          // window specified.

  public static final int VI_ERROR_WINDOW_MAPPED = 0xBFFF0080; // The specified
                                                               // session
                                                               // currently
                                                               // contains a
                                                               // mapped window.

  public static final int VI_ERROR_NIMPL_OPER = 0xBFFF0081; // The given
                                                            // operation is not
                                                            // implemented.

  public static final int VI_ERROR_INV_LENGTH = 0xBFFF0083; // Invalid length
                                                            // specified.

  public static final int VI_ERROR_INV_MODE = 0xBFFF0091; // The specified mode
                                                          // is invalid.

  public static final int VI_ERROR_SESN_NLOCKED = 0xBFFF009C; // The current
                                                              // session did not
                                                              // have any lock
                                                              // on the
                                                              // resource.

  public static final int VI_ERROR_MEM_NSHARED = 0xBFFF009D; // The device does
                                                             // not export any
                                                             // memory.

  public static final int VI_ERROR_LIBRARY_NFOUND = 0xBFFF009E; // A code
                                                                // library
                                                                // required by
                                                                // VISA could
                                                                // not be
                                                                // located or
                                                                // loaded.

  public static final int VI_ERROR_NSUP_INTR = 0xBFFF009F; // The interface
                                                           // cannot generate an
                                                           // interrupt on the
                                                           // requested level or
                                                           // with the requested
                                                           // statusID value.

  public static final int VI_ERROR_INV_LINE = 0xBFFF00A0; // The value specified
                                                          // by the line
                                                          // parameter is
                                                          // invalid.

  public static final int VI_ERROR_FILE_ACCESS = 0xBFFF00A1; // An error
                                                             // occurred while
                                                             // trying to open
                                                             // the specified
                                                             // file. Possible
                                                             // reasons include
                                                             // an invalid path
                                                             // or lack of
                                                             // access rights.

  public static final int VI_ERROR_FILE_IO = 0xBFFF00A2; // An error occurred
                                                         // while performing I/O
                                                         // on the specified
                                                         // file.

  public static final int VI_ERROR_NSUP_LINE = 0xBFFF00A3; // One of the
                                                           // specified lines
                                                           // (trigSrc or
                                                           // trigDest) is not
                                                           // supported by this
                                                           // VISA
                                                           // implementation, or
                                                           // the combination of
                                                           // lines is not a
                                                           // valid mapping.

  public static final int VI_ERROR_NSUP_MECH = 0xBFFF00A4; // The specified
                                                           // mechanism is not
                                                           // supported by the
                                                           // given event type.

  public static final int VI_ERROR_INTF_NUM_NCONFIG = 0xBFFF00A5; // The
                                                                  // interface
                                                                  // type is
                                                                  // valid but
                                                                  // the
                                                                  // specified
                                                                  // interface
                                                                  // number is
                                                                  // not
                                                                  // configured.

  public static final int VI_ERROR_CONN_LOST = 0xBFFF00A6; // The connection for
                                                           // the given session
                                                           // has been lost.

  public static final int VI_ERROR_MACHINE_NAVAIL = 0xBFFF00A7; // The remote
                                                                // machine does
                                                                // not exist or
                                                                // is not
                                                                // accepting any
                                                                // connections.

  public static final int VI_ERROR_NPERMISSION = 0xBFFF00A8; // Access to the
                                                             // remote machine
                                                             // is denied.

  private static DynamicResourceBundle resources = new DynamicResourceBundle(
        NiVisaStatusException.class.getPackage().getName() + ".NiVisa", null,
        NiVisaStatusException.class.getClassLoader());


  public NiVisaStatusException(int status) {

    super(NiVisaStatusException.getErrorMessage(status));
  }


  protected static String getErrorMessage(int status) {

    String key = "0x" + StringTools.toHexString(status, 8).toUpperCase();
    String msg = NiVisaStatusException.resources.getString(key);
    if ((msg != null) && !msg.equals(key)) {
      return msg;
    }
    else {
      return "Unknown NI_VISA status " + key;
    }
  }
}
