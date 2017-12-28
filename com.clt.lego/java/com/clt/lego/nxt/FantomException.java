/*
 * @(#)FantomException.java
 * Created on 13.04.2007 by dabo
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
public class FantomException
    extends BrickException {

  private static final int kStatusOffset = 0xFFFDD550;

  // ! Error: Bluetooth pairing operation failed.
  // ! Warning: You have already paired with that Bluetooth device.
  public static final int kStatusPairingFailed =
    (FantomException.kStatusOffset + -5); // 0x54B

  // ! Error: Bluetooth search failed.
  public static final int kStatusBluetoothSearchFailed =
    (FantomException.kStatusOffset + -6); // 0x54A

  // ! Error: System library not found.
  public static final int kStatusSystemLibraryNotFound =
    (FantomException.kStatusOffset + -7); // 0x549

  // ! Error: Bluetooth unpairing operation failed.
  public static final int kStatusUnpairingFailed =
    (FantomException.kStatusOffset + -8); // 0x548

  // ! Error: Invalid filename specified.
  public static final int kStatusInvalidFilename =
    (FantomException.kStatusOffset + -9); // 0x547

  // ! Error: Invalid iterator dereference. (No object to get.)
  public static final int kStatusInvalidIteratorDereference =
    (FantomException.kStatusOffset + -10); // 0x546

  // ! Error: Resource locking operation failed.
  public static final int kStatusLockOperationFailed =
    (FantomException.kStatusOffset + -11); // 0x545

  // ! Error: Could not determine the requested size.
  public static final int kStatusSizeUnknown =
    (FantomException.kStatusOffset + -12); // 0x544

  // ! Error: Cannot open two objects at once.
  public static final int kStatusDuplicateOpen =
    (FantomException.kStatusOffset + -13); // 0x543

  // ! Error: File is empty.
  // ! Warning: The requested file is empty.
  public static final int kStatusEmptyFile =
    (FantomException.kStatusOffset + -14); // 0x542

  // ! Error: Firmware download failed.
  public static final int kStatusFirmwareDownloadFailed =
    (FantomException.kStatusOffset + -15); // 0x541

  // ! Error: Could not locate virtual serial port.
  public static final int kStatusPortNotFound =
    (FantomException.kStatusOffset + -16); // 0x540

  // ! Error: No more items found.
  public static final int kStatusNoMoreItemsFound =
    (FantomException.kStatusOffset + -17); // 0x53F

  // ! Error: Too many unconfigured devices.
  public static final int kStatusTooManyUnconfiguredDevices =
    (FantomException.kStatusOffset + -18); // 0x53E

  // ! Error: Command mismatch in firmware response.
  public static final int kStatusCommandMismatch =
    (FantomException.kStatusOffset + -19); // 0x53D

  // ! Error: Illegal operation.
  public static final int kStatusIllegalOperation =
    (FantomException.kStatusOffset + -20); // 0x53C

  // ! Error: Could not update local Bluetooth cache with new name.
  // ! Warning: Could not update local Bluetooth cache with new name.
  public static final int kStatusBluetoothCacheUpdateFailed =
    (FantomException.kStatusOffset + -21); // 0x53B

  // ! Error: Selected device is not an NXT.
  public static final int kStatusNonNXTDeviceSelected =
    (FantomException.kStatusOffset + -22); // 0x53A

  // ! Error: Communication error. Retry the operation.
  public static final int kStatusRetryConnection =
    (FantomException.kStatusOffset + -23); // 0x539

  // ! Error: Could not connect to NXT. Turn the NXT off and then back on before
  // continuing.
  public static final int kStatusPowerCycleNXT =
    (FantomException.kStatusOffset + -24); // 0x538

  // ! Error: This feature is not yet implemented.
  public static final int kStatusFeatureNotImplemented =
    (FantomException.kStatusOffset + -99); // 0x4ED

  // ! Error: Firmware reported an illegal handle.
  public static final int kStatusFWIllegalHandle =
    (FantomException.kStatusOffset + -189); // 0x493

  // ! Error: Firmware reported an illegal file name.
  public static final int kStatusFWIllegalFileName =
    (FantomException.kStatusOffset + -190); // 0x492

  // ! Error: Firmware reported an out of bounds reference.
  public static final int kStatusFWOutOfBounds =
    (FantomException.kStatusOffset + -191); // 0x491

  // ! Error: Firmware could not find module.
  public static final int kStatusFWModuleNotFound =
    (FantomException.kStatusOffset + -192); // 0x490

  // ! Error: Firmware reported that the file already exists.
  public static final int kStatusFWFileExists =
    (FantomException.kStatusOffset + -193); // 0x48F

  // ! Error: Firmware reported that the file is full.
  public static final int kStatusFWFileIsFull =
    (FantomException.kStatusOffset + -194); // 0x48E

  // ! Error: Firmware reported the append operation is not possible.
  public static final int kStatusFWAppendNotPossible =
    (FantomException.kStatusOffset + -195); // 0x48D

  // ! Error: Firmware has no write buffers available.
  public static final int kStatusFWNoWriteBuffers =
    (FantomException.kStatusOffset + -196); // 0x48C

  // ! Error: Firmware reported that file is busy.
  public static final int kStatusFWFileIsBusy =
    (FantomException.kStatusOffset + -197); // 0x48B

  // ! Error: Firmware reported the undefined error.
  public static final int kStatusFWUndefinedError =
    (FantomException.kStatusOffset + -198); // 0x48A

  // ! Error: Firmware reported that no linear space is available.
  public static final int kStatusFWNoLinearSpace =
    (FantomException.kStatusOffset + -199); // 0x489

  // ! Error: Firmware reported that handle has already been closed.
  public static final int kStatusFWHandleAlreadyClosed =
    (FantomException.kStatusOffset + -200); // 0x488

  // ! Error: Firmware could not find file.
  public static final int kStatusFWFileNotFound =
    (FantomException.kStatusOffset + -201); // 0x487

  // ! Error: Firmware reported that the requested file is not linear.
  public static final int kStatusFWNotLinearFile =
    (FantomException.kStatusOffset + -202); // 0x486

  // ! Error: Firmware reached the end of the file.
  public static final int kStatusFWEndOfFile =
    (FantomException.kStatusOffset + -203); // 0x485

  // ! Error: Firmware expected an end of file.
  public static final int kStatusFWEndOfFileExpected =
    (FantomException.kStatusOffset + -204); // 0x484

  // ! Error: Firmware cannot handle more files.
  public static final int kStatusFWNoMoreFiles =
    (FantomException.kStatusOffset + -205); // 0x483

  // ! Error: Firmware reported the NXT is out of space.
  public static final int kStatusFWNoSpace =
    (FantomException.kStatusOffset + -206); // 0x482

  // ! Error: Firmware could not create a handle.
  public static final int kStatusFWNoMoreHandles =
    (FantomException.kStatusOffset + -207); // 0x481

  // ! Error: Firmware reported an unknown error code.
  public static final int kStatusFWUnknownErrorCode =
    (FantomException.kStatusOffset + -208); // 0x480

  private static DynamicResourceBundle resources = new DynamicResourceBundle(
        FantomException.class.getPackage().getName() + ".Fantom", null,
        FantomException.class.getClassLoader());


  public FantomException(int error) {

    super(FantomException.getErrorMessage(error));
  }


  protected static String getErrorMessage(int error) {

    String key = "0x" + StringTools.toHexString(error, 8).toUpperCase();
    String msg = FantomException.resources.getString(key);
    if ((msg != null) && !msg.equals(key)) {
      return msg;
    }
    else {
      return "Unknown Fantom error " + key;
    }
  }

}
