/*
 * @(#)Sensor.java
 * Created on 11.07.2007 by dabo
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
public class Sensor {

  public enum Port {
        S1(0, "1"),
        S2(1, "2"),
        S3(2, "3");

    private int id;
    private String name;


    private Port(int id, String name) {

      this.id = id;
      this.name = name;
    }


    public int getID() {

      return this.id;
    }


    @Override
    public String toString() {

      return this.name;
    }
  }

  public enum Mode {
        /** Value in 0..1023. */
        RAW(0),

        /** Either 0 or 1. */
        BOOLEAN(1),

        /** Number of boolean transitions. */
        EDGE(2),

        /** Number of boolean transitions divided by two. */
        PULSE(3),

        /** Raw value scaled to 0..100. */
        PERCENTAGE(4),

        /** 1/10ths of a degree, -19.8..69.5. */
        CELSIUS(5),

        /** 1/10ths of a degree, -3.6..157.1. */
        FAHRENHEIT(6),

        /** 1/16ths of a rotation, represented as a signed short. */
        ANGLE(7);

    private int value;


    private Mode(int value) {

      this.value = value;
    }


    public int getValue() {

      return this.value;
    }
  }

  public enum Type {
        RAW(0),
        TOUCH(1),
        TEMPERATURE(2),
        LIGHT(3),
        ROTATION(4);

    private int value;


    private Type(int value) {

      this.value = value;
    }


    public int getValue() {

      return this.value;
    }
  }

  private Rcx brick;
  private Port port;


  public Sensor(Rcx brick, Port port) {

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


  public void setType(Type type, Mode mode)
      throws IOException {

    this.brick.setSensorType(this.port.id, type, mode, 0);
  }


  public int getValue()
      throws IOException {

    return this.brick.getSensorValue(this.port.id);
  }


  public int getRawValue()
      throws IOException {

    return this.brick.getSensorRawValue(this.port.id);
  }
}
