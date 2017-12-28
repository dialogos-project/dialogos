/*
 * @(#)EvaluationException.java
 * Created on Wed Nov 27 2002
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

package com.clt.script.exp;

/**
 * 
 * 
 * @author Daniel Bobbert
 * @version 2.0
 */

public class EvaluationException
    extends RuntimeException {

  public EvaluationException() {

    super();
  }


  public EvaluationException(String s) {

    super(s);
  }


  public EvaluationException(String s, Throwable cause) {

    super(s, cause);
  }
}
