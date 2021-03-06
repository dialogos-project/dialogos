package com.clt.srgf;

public abstract class NonTerminal extends Expansion {

  public abstract int size();


  public abstract Expansion get(int index);


  @Override
  void setRule(Rule rule) {

    super.setRule(rule);
    for (int i = this.size() - 1; i >= 0; i--) {
      this.get(i).setRule(rule);
    }
  }
}
