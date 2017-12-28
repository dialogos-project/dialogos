package com.clt.speech.htk;

import com.clt.srgf.Word;

public class MlfTerminalNode extends MlfNode implements Word {

  long start;
  long end;


  public MlfTerminalNode(MlfNonterminalNode parent, String label) {

    super(parent, label);
    this.start = 0;
    this.end = 0;
  }


  public MlfTerminalNode(MlfNonterminalNode parent, long start, long end,
      String label, double confidence) {

    super(parent, label, confidence);
    this.start = start;
    this.end = end;
  }


  @Override
  public long getStart() {

    return this.start;
  }


  @Override
  public long getEnd() {

    return this.end;
  }


  @Override
  public boolean getAllowsChildren() {

    return false;
  }


  public String getWord() {

    return this.getLabel();
  }
}