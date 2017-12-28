/*
 * @(#)PList.java
 * Created on 20.01.05
 *
 * Copyright (c) 2005 CLT Sprachtechnologie GmbH.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CLT Sprachtechnologie GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CLT Sprachtechnologie GmbH.
 */

package com.clt.xml;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */

public class PList {

  private static DateFormat dateFormat =
    new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'Z'");

  Object content;


  public PList(Object content) {

    this.content = content;
  }


  private PList() {

    this(null);
  }


  public Object getContent() {

    return this.content;
  }


  public static PList read(File f)
      throws IOException {

    InputStream in = new BufferedInputStream(new FileInputStream(f));
    PList p = null;
    try {
      p = PList.read(in);
    } finally {
      in.close();
    }
    return p;
  }


  public static PList read(InputStream in)
      throws IOException {

    return PList.read(in, null);
  }


  public static PList read(InputStream in, XMLProgressListener progress)
      throws IOException {

    final PList p = new PList();
    final XMLReader r = new XMLReader(false);
    if (progress != null) {
      r.addProgressListener(progress);
    }
    r.parse(in, new AbstractHandler("plist") {

      @Override
      protected void start(String name, Attributes atts) {

        if (!name.equals("plist")) {
          r.setHandler(new PListHandler(r, name, new Notifier() {

            public void contentAvailable(Object o) {

              p.content = o;
            }
          }));
        }
      }
    });
    if (progress != null) {
      r.removeProgressListener(progress);
    }
    return p;
  }


  public void write(OutputStream out)
      throws IOException {

    XMLWriter w = new XMLWriter(new BufferedOutputStream(out));
    try {
      w
        .println("<!DOCTYPE plist PUBLIC \"-//Apple Computer//DTD PLIST 1.0//EN\" "
                    + "\"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">");
      w
        .openElement("plist", new String[] { "version" },
          new String[] { "1.0" });
      this.write(w, this.content);
      w.closeElement("plist");
    } finally {
      w.close();
    }
  }


  private void write(XMLWriter w, Object value) {

    if ((value instanceof Integer) || (value instanceof Long)) {
      w.printElement("integer", value);
    }
    if ((value instanceof Float) || (value instanceof Double)) {
      w.printElement("real", value);
    }
    else if (value instanceof String) {
      w.printElement("string", value);
    }
    else if (value instanceof Date) {
      w.printElement("date", PList.dateFormat.format((Date)value));
    }
    else if (value instanceof Boolean) {
      if (value.equals(Boolean.TRUE)) {
        w.printElement("true", null);
      }
      else {
        w.printElement("false", null);
      }
    }
    else if (value instanceof List) {
      w.openElement("array");
      for (Iterator<?> it = ((List<?>)value).iterator(); it.hasNext();) {
        this.write(w, it.next());
      }
      w.closeElement("array");
    }
    else if (value instanceof Map) {
      w.openElement("dict");
      for (Iterator<?> it = ((Map<?, ?>)value).keySet().iterator(); it
        .hasNext();) {
        Object key = it.next();
        w.printElement("key", key);
        this.write(w, ((Map<?, ?>)value).get(key));
      }
      w.closeElement("dict");
    }
    else if (value instanceof Serializable) {
      w.printElement("data", Base64.encodeObject((Serializable)value));
    }
    else if (value != null) {
      throw new IllegalArgumentException("Can't write objects of type "
                  + value.getClass().getName());
    }
  }

  private static class PListHandler
        extends AbstractHandler {

    private XMLReader r;
    private Notifier notifier;
    private String type;
    private String key = null;
    private Object value;


    public PListHandler(XMLReader r, String type, Notifier notifier) {

      super(type);
      this.r = r;
      this.type = type;
      this.notifier = notifier;
      if (type.equals("dict")) {
        this.value = new HashMap<String, Object>();
      }
      else if (type.equals("array")) {
        this.value = new LinkedList<Object>();
      }
    }


    @Override
    protected void start(String name, Attributes atts) {

      if (this.type.equals("dict")) {
        if (!name.equals("key")) {
          this.r.setHandler(new PListHandler(this.r, name, new Notifier() {

            @SuppressWarnings("unchecked")
            public void contentAvailable(Object o) {

              ((Map)PListHandler.this.value).put(PListHandler.this.key, o);
              PListHandler.this.key = null;
            }
          }));
        }
      }
      else if (this.type.equals("array")) {
        this.r.setHandler(new PListHandler(this.r, name, new Notifier() {

          @SuppressWarnings("unchecked")
          public void contentAvailable(Object o) {

            ((Collection)PListHandler.this.value).add(o);
          }
        }));
      }
    }


    @Override
    protected void end(String name)
        throws SAXException {

      if (name.equals("key")) {
        if (!this.type.equals("dict")) {
          throw new SAXException("<key> encountered in " + this.type);
        }
        this.key = this.getValue();
      }
      else if (name.equals("string")) {
        this.value = this.getValue();
      }
      else if (name.equals("integer")) {
        this.value = new Long(this.getValue());
      }
      else if (name.equals("real")) {
        this.value = new Double(this.getValue());
      }
      else if (name.equals("true")) {
        this.value = Boolean.TRUE;
      }
      else if (name.equals("false")) {
        this.value = Boolean.FALSE;
      }
      else if (name.equals("date")) {
        String date = this.getValue();
        try {
          this.value = PList.dateFormat.parse(date);
        } catch (ParseException exn) {
          throw new SAXException(exn);
        }
      }
      else if (name.equals("data")) {
        this.value = Base64.decode(this.getValue());
      }

      if (name.equals(this.type)) {
        this.notifier.contentAvailable(this.value);
      }
    }

  }

  private static interface Notifier {

    public void contentAvailable(Object o);
  }
}
