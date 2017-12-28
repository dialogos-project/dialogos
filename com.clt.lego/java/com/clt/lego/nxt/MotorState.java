/*
 * @(#)MotorState.java
 * Created on 20.07.2007 by dabo
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

package com.clt.lego.nxt;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

/**
 * @author Daniel Bobbert
 * 
 */
public class MotorState {

  private Motor.State state;
  private Collection<Motor.Mode> modes;
  private Motor.Regulation regulation;
  private int power;
  private int turnRatio;
  private long tachoLimit;
  private int tachoCount;
  private int blockTachoCount;
  private int rotationCount;


  MotorState(int state, int mode, int regulation, int power, int turnRatio,
      long tachoLimit,
               int tachoCount, int blockTachoCount, int rotationCount) {

    for (Motor.State s : Motor.State.values()) {
      if (s.getValue() == state) {
        this.state = s;
      }
    }

    this.modes = new HashSet<Motor.Mode>();
    for (Motor.Mode m : Motor.Mode.values()) {
      if ((m.getValue() & mode) != 0) {
        this.modes.add(m);
      }
    }
    for (Motor.Regulation r : Motor.Regulation.values()) {
      if (r.getValue() == regulation) {
        this.regulation = r;
      }
    }

    this.power = power;
    this.turnRatio = turnRatio;
    this.tachoLimit = tachoLimit;
    this.tachoCount = tachoCount;
    this.blockTachoCount = blockTachoCount;
    this.rotationCount = rotationCount;
  }


  public Motor.State getState() {

    return this.state;
  }


  public Collection<Motor.Mode> getMode() {

    return this.modes;
  }


  public Motor.Regulation getRegulation() {

    return this.regulation;
  }


  public int getPower() {

    return this.power;
  }


  public int getTurnRatio() {

    return this.turnRatio;
  }


  public long getTachoLimit() {

    return this.tachoLimit;
  }


  public int getTachoCount() {

    return this.tachoCount;
  }


  public int getBlockTachoCount() {

    return this.blockTachoCount;
  }


  public int getRotationCount() {

    return this.rotationCount;
  }


  @Override
  public String toString() {

    StringBuilder b = new StringBuilder();

    b.append("[State: " + this.state);
    b.append(", Mode: ");
    if (this.modes.isEmpty()) {
      b.append("<none>");
    }
    else {
      for (Iterator<Motor.Mode> mode = this.modes.iterator(); mode.hasNext();) {
        b.append(mode.next());
        if (mode.hasNext()) {
          b.append("+");
        }
      }
    }
    b.append(", Regulation: " + this.getRegulation());
    b.append(", Power: " + this.getPower());
    b.append(", TurnRatio: " + this.getTurnRatio());
    b.append(", TachoLimit: " + this.getTachoLimit());
    b.append(", TachoCount: " + this.getTachoCount());
    b.append(", BlockTachoCount: " + this.getBlockTachoCount());
    b.append(", RotationCount: " + this.getRotationCount());
    b.append("]");

    return b.toString();
  }
}
