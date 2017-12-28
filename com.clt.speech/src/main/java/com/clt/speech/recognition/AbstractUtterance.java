/*
 * @(#)AbstractUtterance.java
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

import java.util.List;

import com.clt.speech.htk.MlfNode;
import com.clt.speech.htk.MlfNonterminalNode;
import com.clt.speech.htk.MlfTerminalNode;
import com.clt.srgf.Grammar;

/**
 * @author dabo
 * 
 */
public class AbstractUtterance
    implements Utterance {

  private float confidence;
  private Word[] words;


  public AbstractUtterance(Word words[], float confidence) {

    this.words = words;
    this.confidence = confidence;
  }


  public AbstractUtterance(List<? extends Word> words, float confidence) {

    this.words = words.toArray(new Word[words.size()]);
    this.confidence = confidence;
  }


  public AbstractUtterance(String words, float confidence) {

    com.clt.srgf.Word[] ws = Grammar.splitWords(words);
    this.words = new Word[ws.length];
    for (int i = 0; i < ws.length; i++) {
      final com.clt.srgf.Word word = ws[i];
      this.words[i] = new Word() {

        @Override
        public long getStart() {

          return 0;
        }


        @Override
        public long getEnd() {

          return 0;
        }


        public String getWord() {

          return word.getWord();
        }


        public float getConfidence() {

          return word.getConfidence();
        }
      };
    }

    this.confidence = confidence;
  }


  public float getConfidence() {

    return this.confidence;
  }


  public int length() {

    return this.words.length;
  }


  public Word getWord(int index) {

    return this.words[index];
  }


  /** Return the words of this utterance as a String */
  public String getWords() {

    StringBuilder b = new StringBuilder();
    for (int i = 0; i < this.words.length; i++) {
      if (i > 0) {
        b.append(' ');
      }
      b.append(this.getWord(i).getWord());
    }
    return b.toString();
  }


  @Override
  public String toString() {

    return this.getWords();
  }


  public MlfNode getTree() {

    MlfNonterminalNode root =
      new MlfNonterminalNode(null, "<S>", Math.log(this.getConfidence()));

    for (int i = 0; i < this.length(); i++) {
      Word word = this.getWord(i);
      root.addChild(new MlfTerminalNode(root, word.getStart(), word.getEnd(),
        word.getWord(),
                Math.log(word.getConfidence())));
    }

    return root;
  }
}
