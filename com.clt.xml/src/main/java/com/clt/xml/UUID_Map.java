package com.clt.xml;

import java.util.UUID;

public class UUID_Map<VALUE>
    extends UID_Map<VALUE> {

  @Override
  protected String createUID() {
    return UUID.randomUUID().toString();

  }
}