/*
 * @(#)NxtDescription.java
 * Created on 13.04.2007 by dabo
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

package com.clt.lego;

import java.awt.Component;
import java.io.IOException;

import com.clt.io.InterfaceType;
import com.clt.util.StringTools;
import com.clt.util.UserCanceledException;

/**
 * @author dabo
 * 
 */
public abstract class BrickDescription<T extends Brick>
    implements Comparable<BrickDescription<T>> {

  protected String uri;

  private BrickInfo brickInfo;
  private InterfaceType type;
  private String port;


  public BrickDescription(String uri, BrickInfo brickInfo, InterfaceType type,
      String port) {

    if (uri == null) {
      throw new IllegalArgumentException();
    }
    this.uri = uri;
    this.brickInfo = brickInfo;
    this.type = type;
    this.port = port;
  }


  public String getBrickName() {

    return this.brickInfo != null ? this.brickInfo.getName() : null;
  }


  public InterfaceType getInterfaceType() {

    return this.type;
  }


  public String getPort() {

    return this.port;
  }


  public String getURI() {

    return this.uri;
  }


  public int compareTo(BrickDescription<T> o) {

    int result = this.getClass().getName().compareTo(o.getClass().getName());
    if (result != 0) {
      return result;
    }
    else {
      return this.getURI().compareTo(o.getURI());
    }
  }


  @Override
  public boolean equals(Object o) {

    if (o instanceof BrickDescription) {
      BrickDescription<?> desc = (BrickDescription)o;
      return this.getClass().equals(desc.getClass())
        && this.getURI().equals(desc.getURI());
    }
    else {
      return false;
    }
  }


  @Override
  public int hashCode() {

    return this.getClass().hashCode() ^ this.uri.hashCode();
  }


  @Override
  public String toString() {

    if ((this.uri.length() == 0) || (this.brickInfo == null)) {
      return "<" + BrickUtils.getString("None") + ">";
    }
    else {
      return this.getBrickName()
        + " ("
        + this.getInterfaceType()
                  + (StringTools.isEmpty(this.getPort()) ? ""
                    : (" Port " + this.getPort())) + ")";
    }
  }


  public T createBrick(Component parent)
      throws IOException, UserCanceledException {

    T brick = this.createBrickImpl(parent);
    if (brick != null) {
      this.uri = brick.getResourceString();
      this.port = brick.getPort();
    }
    return brick;
  }


  protected abstract T createBrickImpl(Component parent)
        throws IOException, UserCanceledException;
}
