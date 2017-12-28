/*
 * @(#)RecognitionResult.java
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

/**
 * Speech recognizers must return recognition results that conform to this
 * interface.
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

public interface RecognitionResult extends Iterable<Utterance> {

  /** Return the number of recognition alternatives */
  public int numAlternatives();


  /** Return the nth alternative */
  public Utterance getAlternative(int index);


  /** Return a textual representation of the recognition result */
  public String toString();
}