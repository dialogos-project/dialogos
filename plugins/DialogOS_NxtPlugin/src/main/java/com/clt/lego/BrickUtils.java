/*
 * @(#)BrickUtils.java
 * Created on 26.04.2007 by dabo
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
package com.clt.lego;

import com.clt.resources.DynamicResourceBundle;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author dabo
 *
 */
public class BrickUtils {

    private static DynamicResourceBundle resources = new DynamicResourceBundle(
            BrickUtils.class.getPackage().getName() + ".Resources", null,
            BrickUtils.class.getClassLoader());

    /**
     * Return a localized version of the given string.
     */
    public static String getString(String key) {
        return BrickUtils.resources.getString(key);
    }
    
    public static long readNum(InputStream is, int length, boolean bigEndian) throws IOException {
        byte[] data = new byte[length];
        is.read(data);
        
        return readNum(data, 0, length, bigEndian);
    }

    public static long readNum(byte[] bytes, int offset, int length, boolean bigEndian) {
        long sum = 0;
        if (bigEndian) {
            for (int i = offset; i < offset + length; i++) {
                int n = bytes[i];
                if (n < 0) {
                    n += 256;
                }
                sum = sum << 8;
                sum += n;
            }
        } else {
            for (int i = offset + length - 1; i >= offset; i--) {
                int n = bytes[i];
                if (n < 0) {
                    n += 256;
                }
                sum = sum << 8;
                sum += n;
            }
        }

        return sum;
    }

    public static String readString(byte[] answer, int offset, int maxLength) {

        int length = 0;
        while ((length < maxLength) && (answer[offset + length] != 0)) {
            length++;
        }

        return new String(answer, offset, length);
    }
    
    public static void writeString(String s, byte[] buffer, int offset) {
        byte[] sb = s.getBytes();
        System.arraycopy(sb, 0, buffer, offset, sb.length);
    }
}
