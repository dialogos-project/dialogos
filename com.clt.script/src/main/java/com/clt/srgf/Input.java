/*
 * @(#)Input.java
 * Created on Tue Oct 08 2002
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

package com.clt.srgf;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * 
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

public class Input implements Cloneable {

  private Word[] words;
  private int position = 0;
  private Patterns[] patterns;


  public Input(String s) {

    this(Grammar.tokenize(s));
  }


  public Input(Word[] tokens) {

    this(Arrays.asList(tokens));
  }


  public Input(List<?> tokens) {

    this.words = new Word[tokens.size()];
    int i = 0;
    for (Iterator<?> it = tokens.iterator(); it.hasNext(); i++) {
      final Object o = it.next();
      if (o instanceof Word) {
        this.words[i] = (Word)o;
      }
      else if (o != null) {
        this.words[i] = new Word() {

          public String getWord() {

            return o.toString();
          }


          public float getConfidence() {

            return 1.0f;
          }
        };
      }
    }

    this.position = 0;
    this.patterns = new Patterns[this.words.length];
    for (i = 0; i < this.words.length; i++) {
      this.patterns[i] = new Patterns(this.words, i);
    }
  }


  private Input(Word[] words, int position, Patterns[] patterns) {

    this.words = words;
    this.position = position;
    this.patterns = patterns;
  }


  @Override
  public Input clone() {

    return new Input(this.words, this.position, this.patterns);
  }


  boolean isEmpty() {

    return this.position >= this.words.length;
  }


  int size() {

    return this.words.length - this.position;
  }


  Word getFirst() {

    return this.words[this.position];
  }


  String getWords(int n) {

    if ((n < 1) || (n > this.size())) {
      throw new IllegalArgumentException();
    }
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < n; i++) {
      if (b.length() > 0) {
        b.append(" ");
      }
      b.append(this.words[this.position + i].getWord());
    }
    return b.toString();
  }


  String getFirstPattern() {

    return this.patterns[this.position].getFirstPattern();
  }


  Word removeFirst() {

    return this.words[this.position++];
  }


  /** Returns the list of matched words */
  List<Match> match(Terminal t) {

    if (this.position >= this.patterns.length) {
      return Collections.emptyList();
    }
    else {
      return this.patterns[this.position].match(t);
    }
  }

  private static class Patterns {

    private String[] pattern;


    public Patterns(Word[] words, int offset) {

      // patterns[0] is the current word, patterns[1] the current and the next
      // ...
      // This way patterns are automtically sorted shortest to longest, just as
      // Terminal.matchOneOf() expects
      this.pattern = new String[words.length - offset];
      for (int i = 0; i < this.pattern.length; i++) {
        StringBuilder b = new StringBuilder();
        for (int j = 0; j <= i; j++) {
          if (j > 0) {
            b.append(' ');
          }
          b.append(words[j + offset].getWord());
        }
        this.pattern[i] = Terminal.normalize(b.toString());
      }
    }


    public String getFirstPattern() {

      String p = this.pattern[0];
      int pos = p.indexOf(' ');
      if (pos >= 0) {
        return p.substring(0, pos);
      }
      else {
        return p;
      }
    }


    public List<Match> match(Terminal t) {

      return t.match(this.pattern);
    }
  }

  static class Match {

    String s;
    int numWords;


    public Match(String s, int numWords) {

      this.s = s;
      this.numWords = numWords;
    }


    public int numWords() {

      return this.numWords;
    }


    public String getString() {

      return this.s;
    }
  }
}