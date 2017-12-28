/*
 * @(#)Buttons.java
 * Created on 10.07.2007 by dabo
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

import java.io.IOException;

/**
 * @author dabo
 * 
 */
public class Buttons
    extends Module {

  enum Button {
        LEFT(1),
        RIGHT(2),
        CENTER(3),
        EXIT(0);

    private int id;


    private Button(int id) {

      this.id = id;
    }
  }

  public static final int EVENT_PRESSED = 0x01;
  public static final int EVENT_PRESSED_LONG = 0x04;
  public static final int EVENT_RELEASED = 0x02;
  public static final int EVENT_RELEASED_LONG = 0x08;
  public static final int PRESSED_STATE = 0x80;


  public Buttons(Nxt brick)
      throws IOException {

    super(brick, "Button.mod");
  }


  public boolean isPressed(Button button)
      throws IOException {

    return (this.getState(button) & Buttons.PRESSED_STATE) != 0;
  }


  public int getState(Button button)
      throws IOException {

    return this.readByte(32 + button.id);
  }


  public int getPressCount(Button button)
      throws IOException {

    return this.readByte(8 * button.id + 0);
  }


  public int getLongPressCount(Button button)
      throws IOException {

    return this.readByte(8 * button.id + 1);
  }


  public int getShortReleaseCount(Button button)
      throws IOException {

    return this.readByte(8 * button.id + 2);
  }


  public int getLongReleaseCount(Button button)
      throws IOException {

    return this.readByte(8 * button.id + 3);
  }


  public int getReleaseCount(Button button)
      throws IOException {

    return this.readByte(8 * button.id + 4);
  }


  private int readByte(int offset)
      throws IOException {

    byte[] data = this.read(offset, 1);
    int result = data[0];
    if (result < 0) {
      result += 256;
    }
    return result;
  }
}
