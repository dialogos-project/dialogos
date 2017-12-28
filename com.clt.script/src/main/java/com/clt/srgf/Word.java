/*
 * @(#)Word.java
 * Created on Thu Sep 23 2004
 *
 * Copyright (c) 2004 CLT Sprachtechnologie GmbH.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CLT Sprachtechnologie GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CLT Sprachtechnologie GmbH.
 */

package com.clt.srgf;

/**
 * A single word of a sentence to parse.
 * 
 * @see Grammar
 * @see Terminal
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

public interface Word {

  public String getWord();


  public float getConfidence();
}
