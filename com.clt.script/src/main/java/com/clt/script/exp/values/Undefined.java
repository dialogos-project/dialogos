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

import com.clt.script.exp.Type;
import com.clt.script.exp.Value;
import com.clt.script.exp.types.TypeVariable;

/**
 * Undefined is a <code>null</code>-value that matches any type.
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

public class Undefined extends PrimitiveValue {

  Type type;


  public Undefined() {

    this.type = new TypeVariable();
  }


  @Override
  protected Value copyValue() {

    return new Undefined();
  }


  @Override
  public Type getType() {

    return this.type;
  }


  @Override
  public boolean equals(Object v) {

    return v instanceof Undefined;
  }


  @Override
  public int hashCode() {

    return 0x0815;
  }


  @Override
  public String toString() {

    return "undefined";
  }


@Override
public Object getReadableValue()
{
	return null;
}
}