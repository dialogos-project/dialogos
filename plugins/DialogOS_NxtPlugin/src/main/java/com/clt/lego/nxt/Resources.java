package com.clt.lego.nxt;

import com.clt.resources.DynamicResourceBundle;

/**
 * @author dabo
 *
 */
class Resources {

    private static DynamicResourceBundle resources
            = new DynamicResourceBundle(Resources.class.getPackage().getName() + ".NXT", null, 
                    Resources.class.getClassLoader());

    /**
     * Return a localized version of the given string.
     */
    public static String getString(String key) {

        return Resources.resources.getString(key);
    }
}
