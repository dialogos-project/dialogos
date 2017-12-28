/*
 * @(#)XML.java
 * Created on 20.06.2002
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

package com.clt.xml;

import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;

/**
 * A helper class that provides localization for the XML package.
 * 
 * @author Daniel Bobbert
 */
class XML {

  private static ResourceBundle resources = new ResourceBundle() {

    private String basename = "com.clt.xml.Resources";

    ResourceBundle resources = null;
    Locale locale = Locale.getDefault();


    private void loadResources() {

      try {
        this.locale = Locale.getDefault();
        this.resources = ResourceBundle.getBundle(this.basename, this.locale);
        if (this.resources == null) {
          throw new MissingResourceException(this.basename, this.getClass()
            .getName(),
                      this.locale.toString());
        }
      }
            catch (Exception exn) {
              this.resources = this.emptyResourceBundle;
            }
          }


    @Override
    public Enumeration<String> getKeys() {

      if ((this.resources == null) || !this.locale.equals(Locale.getDefault())) {
        this.loadResources();
      }

      return this.resources.getKeys();
    }


    @Override
    protected Object handleGetObject(String key) {

      if ((this.resources == null) || !this.locale.equals(Locale.getDefault())) {
        this.loadResources();
      }

      try {
        Object o = this.resources.getObject(key);
        return o != null ? o : key;
      }
            catch (Exception exn) {
              return key;
            }
          }

    private ResourceBundle emptyResourceBundle = new ResourceBundle() {

      Enumeration<String> emptyEnumeration = new Enumeration<String>() {

        public boolean hasMoreElements() {

          return false;
        }


        public String nextElement() {

          throw new NoSuchElementException();
        }
      };


      @Override
      protected Object handleGetObject(String key) {

        return key;
      }


      @Override
      public Enumeration<String> getKeys() {

        return this.emptyEnumeration;
      }
    };

  };


  static String getString(String key) {

    return XML.resources.getString(key);
  }

}
