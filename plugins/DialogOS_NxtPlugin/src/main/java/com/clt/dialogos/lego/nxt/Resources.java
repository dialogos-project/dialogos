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
