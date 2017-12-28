/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.saar.coli.dialogos.pocketsphinx.plugin;

import com.clt.resources.DynamicResourceBundle;
import com.clt.util.StringTools;

/**
 *
 * @author koller
 */
public class Resources {

      private static DynamicResourceBundle resources = new DynamicResourceBundle(
        Resources.class.getPackage().getName() + ".Resources", null,
        Resources.class.getClassLoader());
      

  public static String getString(String key) {
    return Resources.resources.getString(key);
  }


  public static String format(String key, Object... param) {
      System.err.println(StringTools.format(Resources.getString(key), param));
    return StringTools.format(Resources.getString(key), param);
  }


  public static DynamicResourceBundle getResources() {
    return Resources.resources;
  }
}

