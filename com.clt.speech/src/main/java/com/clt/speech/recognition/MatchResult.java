/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.clt.speech.recognition;


import com.clt.script.exp.Match;

/**
 * The result of matching an utterance against a grammar and an array of patterns:
 */
public class MatchResult {
  /** the utterance that we originally matched against */
  private String utterance;
  /** the match within the grammar: either the matched text or a set of attribute-value pairs */
  private Match match;
  /** the index into the array of patterns that matches (i.e., the edge to continue to the next node) */
  private int edge;


  public MatchResult(String utterance, Match match, int edge) {
    this.utterance = utterance;
    this.match = match;
    this.edge = edge;
  }
  public String getUtterance() {
    return utterance;
  }
  public Match getMatch() {
    return match;
  }
  public int getEdge() {
    return edge;
  }
}
