/*
 * @(#)Resources.java
 * Created on 05.03.2007 by dabo
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
package com.clt.dialogos.lego.nxt;

import com.clt.resources.DynamicResourceBundle;
import com.clt.util.StringTools;

/**
 * @author Daniel Bobbert
 *
 */
public class Resources {

    private static DynamicResourceBundle resources
            = new DynamicResourceBundle("com.clt.dialogos.lego.nxt.Resource", null, Resources.class.getClassLoader());

    public static String getString(String key) {

        return Resources.resources.getString(key);
    }

    public static String format(String key, Object... param) {

        return StringTools.format(Resources.getString(key), param);
    }

    public static DynamicResourceBundle getResources() {

        return Resources.resources;
    }
}
