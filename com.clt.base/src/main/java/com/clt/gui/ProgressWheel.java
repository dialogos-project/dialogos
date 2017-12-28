/*
 * @(#)ProgressWheel.java
 * Created on 11.12.04
 *
 * Copyright (c) 2004 CLT Sprachtechnologie GmbH.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CLT Sprachtechnologie GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CLT Sprachtechnologie GmbH.
 */

package com.clt.gui;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JProgressBar;

import com.clt.gui.plaf.ProgressWheelUI;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */

public class ProgressWheel
    extends JProgressBar {

  public ProgressWheel() {

    this(0, 0);
  }


  public ProgressWheel(int min, int max) {

    this(new DefaultBoundedRangeModel(min, 0, min, max));
  }


  public ProgressWheel(BoundedRangeModel model) {

    super(model);

    this.setUI(ProgressWheelUI.createUI(this));
  }
}
