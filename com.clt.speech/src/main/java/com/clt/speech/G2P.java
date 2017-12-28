/*
 * @(#)G2P.java
 * Created on 26.06.2006 by dabo
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

package com.clt.speech;

import java.util.Collection;
import java.util.Map;

/**
 * @author dabo
 * 
 */
public interface G2P {

  /** Return an array of supported languages */
  public Language[] getLanguages()
      throws SpeechException;


  /** Transcribe a single word. */
  public String[] transcribe(String word, Language language)
      throws SpeechException;


  /**
   * Transcribe a collection of words. This may be more efficient as calling
   * {@link #transcribe(String, Language)} repeatedly.
   */
  public Map<String, String[]> transcribe(Collection<String> words,
      Language language)
        throws SpeechException;
}
