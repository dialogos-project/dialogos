/*
 * @(#)PhonemeConverter.java
 * Created on 29.01.2007 by dabo
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

import java.util.Locale;

/**
 * @author dabo
 * 
 */
public abstract class PhonemeConverter {

  private String sourceEngine;
  private Locale sourceLocale;
  private String targetEngine;
  private Locale targetLocale;


  public PhonemeConverter(String sourceEngine, Locale sourceLocale,
      String targetEngine,
                            Locale targetLocale) {

    this.sourceEngine = sourceEngine;
    this.sourceLocale = sourceLocale;
    this.targetEngine = targetEngine;
    this.targetLocale = targetLocale;
  }


  public String getSourceEngine() {

    return this.sourceEngine;
  }


  public Locale getSourceLocale() {

    return this.sourceLocale;
  }


  public String getTargetEngine() {

    return this.targetEngine;
  }


  public Locale getTargetLocale() {

    return this.targetLocale;
  }


  public abstract String convert(String phonemes);
}
