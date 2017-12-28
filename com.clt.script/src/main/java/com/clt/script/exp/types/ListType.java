/*
 * @(#)ListType.java
 * Created on Wed Oct 02 2002
 *
 * Copyright (c) 2002 CLT Sprachtechnologie GmbH.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CLT Sprachtechnologie GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CLT Sprachtechnologie GmbH.
 */

package com.clt.script.exp.types;

import com.clt.script.exp.Type;
import com.clt.script.exp.values.ListValue;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */

public class ListType
    extends Type {

  private Type elementType;


  public ListType() {

    this(new TypeVariable());
  }


  public ListType(Type elementType) {

    if (elementType == null) {
      throw new IllegalArgumentException("null base type for ListType");
    }
    this.elementType = elementType;
  }


  @Override
  public Object clone() {

    return new ListType((Type)this.elementType.clone());
  }


  public Type getElementType() {

    Type rType = this.elementType.resolve();
    if (rType != this.elementType) {
      this.elementType = rType;
    }
    return rType;
  }


  @Override
  public boolean equals(Object type) {

    if (type instanceof ListType) {
      return this.getElementType().equals(((ListType)type).getElementType());
    }
    else {
      return false;
    }
  }


  @Override
  protected Type unify(Type type) {

    if (type instanceof ListType) {
      ListType t = (ListType)type;
      Type.unify(this.elementType, t.elementType);
      return this;
    }
    else {
      return this.throwUnificationException(type);
    }
  }


  @Override
  public String toString() {

    if (this.elementType.resolve() instanceof TypeVariable) {
      return "list";
    }
    else {
      return this.elementType.toString() + " list";
    }
  }


  @Override
  public Class<ListValue> getObjectClass() {

    return ListValue.class;
  }
}
