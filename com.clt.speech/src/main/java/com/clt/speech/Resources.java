package com.clt.speech;

import java.util.ResourceBundle;

import com.clt.resources.DynamicResourceBundle;
import com.clt.util.StringTools;

/**
 * @author dabo
 *
 */
public class Resources {

    private static DynamicResourceBundle resources = new DynamicResourceBundle(
            Resources.class.getPackage().getName() + ".Resources", null,
            Resources.class.getClassLoader());

    public static String getString(String key) {

        return Resources.resources.getString(key);
    }

    public static String format(String key, Object... param) {

        return StringTools.format(Resources.getString(key), param);
    }

    public static ResourceBundle getResources() {

        return Resources.resources;
    }
}
