/*
 * @(#)Voice.java
 * Created on 10.03.2007 by dabo
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

package com.clt.speech.tts;

import com.clt.speech.Language;

/**
 * @author dabo
 * 
 */
public interface Voice {

  public String getName();


  public Language getLanguage();
}
