/*
 * @(#)Motor.java
 * Created on 02.07.2007 by dabo
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

package com.clt.lego.rcx;

import java.io.IOException;

/**
 * @author dabo
 * 
 */
public class Motor {

  public enum Port {
        A(0x01),
        B(0x02),
        C(0x04);

    private int id;


    private Port(int id) {

      this.id = id;
    }


    public int getID() {

      return this.id;
    }
  }

  public enum Direction {
        FORWARD(0x80),
        BACKWARD(0x00),
        FLIP(0x40);

    private int value;


    private Direction(int value) {

      this.value = value;
    }


    public int getValue() {

      return this.value;
    }
  }

  public enum State {
        ON(0x80),
        OFF(0x40),
        FLOAT(0x00);

    private int value;


    private State(int value) {

      this.value = value;
    }


    public int getValue() {

      return this.value;
    }
  }

  private Rcx brick;
  private Port port;


  public Motor(Rcx brick, Port port) {

    if (brick == null) {
      throw new IllegalArgumentException();
    }
    if (port == null) {
      throw new IllegalArgumentException();
    }
    this.brick = brick;
    this.port = port;
  }


  public Port getPort() {

    return this.port;
  }


  public void setPower(int power)
      throws IOException {

    this.brick.setMotorPower(power, this.getPort());
  }


  public void forward()
      throws IOException {

    this.brick.setMotorDirection(Direction.FORWARD, this.getPort());
    this.brick.setMotorState(State.ON, this.getPort());
  }


  public void backward()
      throws IOException {

    this.brick.setMotorDirection(Direction.BACKWARD, this.getPort());
    this.brick.setMotorState(State.ON, this.getPort());
  }


  public void stop()
      throws IOException {

    this.brick.setMotorState(State.OFF, this.getPort());
  }


  public void drift()
      throws IOException {

    this.brick.setMotorState(State.FLOAT, this.getPort());
  }
}
