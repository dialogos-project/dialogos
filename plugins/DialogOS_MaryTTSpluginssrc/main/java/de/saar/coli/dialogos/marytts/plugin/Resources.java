package de.saar.coli.dialogos.marytts.plugin;

import java.util.Locale;
import java.util.ResourceBundle;

import com.clt.resources.DynamicResourceBundle;
import com.clt.util.StringTools;

/**
 * @author dabo
 *
 */
public class Resources {

    private Resources() {
        throw new IllegalStateException("Utility class must not be instantiated");
    }

    private static DynamicResourceBundle resources = new DynamicResourceBundle(
            Resources.class.getPackage().getName() + ".Resources", null,
            Resources.class.getClassLoader());

    public static String getString(String key) {

        return Resources.resources.getString(key);
    }

    public static String format(String key, Locale locale, Object... param) {

        ResourceBundle bundle = ResourceBundle.getBundle(Resources.resources.getBaseName(), locale,
                        Resources.class.getClassLoader());
        return StringTools.format(bundle.getString(key), param);
    }

    public static String format(String key, Object... param) {

        return StringTools.format(Resources.getString(key), param);
    }

    public static DynamicResourceBundle getResources() {

        return Resources.resources;
    }
}
