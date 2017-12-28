/*
 * @(#)Word.java
 * Created on 06.10.2006 by dabo
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

package com.clt.speech.recognition;

import java.beans.PropertyChangeListener;
import java.util.LinkedHashSet;
import java.util.Set;

import com.clt.util.PropertyContainer;

/**
 * @author Daniel Bobbert
 * 
 */
public abstract class Word
    implements com.clt.srgf.Word, PropertyContainer<Object> {

  // return the start offset in the audio stream, as returned by the recognizer
  public abstract long getStart();


  // return the end offset in the audio stream, as returned by the recognizer
  public abstract long getEnd();


  @Override
  public String toString() {

    return this.getWord();
  }


  public Object getProperty(String key) {

    if (key.equals("word")) {
      return this.getWord();
    }
    else if (key.equals("confidence")) {
      return new Float(this.getConfidence());
    }
    else if (key.equals("start")) {
      return new Long(this.getStart());
    }
    else if (key.equals("end")) {
      return new Long(this.getEnd());
    }
    else {
      return null;
    }
  }


  public Set<String> propertyNames() {

    Set<String> names = new LinkedHashSet<String>();
    names.add("word");
    names.add("confidence");
    names.add("start");
    names.add("end");
    return names;
  }


  public void removePropertyChangeListener(PropertyChangeListener listener) {

    // no-op, because properties are read only
  }


  public void addPropertyChangeListener(PropertyChangeListener listener) {

    // no-op, because properties are read only
  }


  public void setProperty(String key, Object value) {

    throw new UnsupportedOperationException();
  }

}
