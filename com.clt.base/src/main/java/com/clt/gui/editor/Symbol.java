/*
 * @(#)Symbols.java
 * Created on Tue Jun 22 2004
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

package com.clt.gui.editor;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */

public interface Symbol {

  /**
   * Get the style of this symbol. The id of the default style must always be 0.
   */
  public int getStyle();


  /**
   * Get the starting location of this symbol.
   */
  public int getStart();
}
