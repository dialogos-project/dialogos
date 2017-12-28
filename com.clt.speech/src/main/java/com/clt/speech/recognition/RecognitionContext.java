/*
 * @(#)RecognitionContext.java
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

import com.clt.speech.Language;
import com.clt.speech.SpeechException;
import com.clt.srgf.Grammar;

/**
 * Speech recognizers need a context that is based on an interpretation grammar.
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

public class RecognitionContext {

  private String name;
  private Domain domain;
  private Language language;
  private Grammar grammar;


  public RecognitionContext(String name, Domain domain, Language language, Grammar grammar) {

    this.name = name;
    this.domain = domain;
    this.language = language;
    this.grammar = grammar;
  }


  public String getName() {

    return this.name;
  }


  public Grammar getGrammar() {

    return this.grammar;
  }


  public Domain getDomain() {

    return this.domain;
  }


  public Language getLanguage() {

    return this.language;
  }


  @Override
  public String toString() {

    return this.name;
  }


  @SuppressWarnings("unused")
  public void dispose()
      throws RecognizerException {

  }

  public abstract static class Info {

    private String name;
    private long timestamp;


    public Info(String name, long timestamp) {

      this.name = name;
      this.timestamp = timestamp;
    }


    public String getName() {

      return this.name;
    }


    public long getTimestamp() {

      return this.timestamp;
    }


    @Override
    public String toString() {

      return this.getName();
    }


    public abstract RecognitionContext createContext(Grammar grammar,
        Domain domain)
            throws SpeechException;
  }
}