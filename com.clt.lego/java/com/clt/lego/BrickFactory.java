/*
 * @(#)NxtFactory.java
 * Created on 16.04.2007 by dabo
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

import com.clt.util.UserCanceledException;

/**
 * @author dabo
 * 
 */
public interface BrickFactory<T extends Brick> {

  public String[] getAvailablePorts()
      throws IOException;


  public BrickDescription<T> getBrickInfo(Component parent, String port)
        throws IOException, UserCanceledException;
}
