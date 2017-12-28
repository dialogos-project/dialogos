/*
 * @(#)Reference.java
 * Created on Mon Oct 06 2003
 *
 * Copyright (c) 2003 CLT Sprachtechnologie GmbH.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CLT Sprachtechnologie GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CLT Sprachtechnologie GmbH.
 */

package com.clt.script.exp.values;

import com.clt.script.exp.Value;

/**
 * Common interface for classes that map strings to values.
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

public interface Reference {

  public Value getValue(String label);


  public void setValue(String label, Value value);
}
