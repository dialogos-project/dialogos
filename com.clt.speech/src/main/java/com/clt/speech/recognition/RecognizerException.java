/*
 * @(#)RecognizerException.java
 * Created on Tue Nov 12 2002
 *
 * Copyright (c) 2002 CLT Sprachtechnologie GmbH.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CLT Sprachtechnologie GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CLT Sprachtechnologie GmbH.
 */

package com.clt.speech.recognition;

import com.clt.speech.SpeechException;

/**
 * Speech recognizers should throw this Exception or a subclass of it in case of
 * an internal error.
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

public class RecognizerException extends SpeechException {

  public RecognizerException(String message) {

    super(message);
  }


  public RecognizerException(String message, Throwable cause) {

    super(message, cause);
  }

}