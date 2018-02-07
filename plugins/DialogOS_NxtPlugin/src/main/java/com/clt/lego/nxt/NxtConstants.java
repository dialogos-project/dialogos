package com.clt.lego.nxt;

/**
 *
 * @author koller
 */
public class NxtConstants {
    // system commands
    public static final byte FIND_FIRST = (byte) 0x86;
    public static final byte FIND_NEXT = (byte) 0x87;
    public static final byte GET_FIRMWARE_VERSION = (byte) 0x88;
    public static final byte GET_DEVICE_INFO = (byte) 0x9B;
    
    // error codes
    public static final byte SUCCESS = (byte) 0x00;
    public static final byte FILE_NOT_FOUND = (byte) 0x87;
}
