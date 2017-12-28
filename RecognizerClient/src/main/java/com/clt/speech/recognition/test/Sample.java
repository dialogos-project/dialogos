/*
 * @(#)Sample.java
 * Created on 19.02.2007 by dabo
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

package com.clt.speech.recognition.test;

import com.clt.speech.recognition.AbstractRecognizer;
import com.clt.xml.XMLWriter;

/**
 * @author dabo
 * 
 */
public abstract class Sample {

  public abstract void collect(AbstractRecognizer recognizer);


  public abstract void print(XMLWriter out);

}
