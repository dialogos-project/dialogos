/*
 * @(#)Utterance.java
 * Created on 27.07.2006 by dabo
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

import com.clt.speech.htk.MlfNode;

/**
 * @author Daniel Bobbert
 * 
 */
public interface Utterance {

  /** @return the number of words in this utterance */
  public int length();


  /** @return the overall sentence confidence of this utterance */
  public float getConfidence();


  /** @return the nth word in this utterance, starting at <code>0</code> */
  public Word getWord(int index);


  /** Return the words of this utterance as a string */
  public String getWords();


  public MlfNode getTree();
}
