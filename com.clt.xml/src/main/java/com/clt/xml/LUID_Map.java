package com.clt.xml;

public class LUID_Map<VALUE>
    extends UID_Map<VALUE> {

  private int id;


  public LUID_Map() {

    super();
    this.id = 1;
  }


  @Override
  public synchronized String add(VALUE o, String uid) {

    uid = super.add(o, uid);
    try {
      int id = Integer.parseInt(uid);
      if (id >= this.id) {
        this.id = id + 1;
      }
    } catch (Exception ignore) {
    }
    return uid;
  }


  @Override
  protected String createUID() {

    return String.valueOf(this.id++);
  }
}