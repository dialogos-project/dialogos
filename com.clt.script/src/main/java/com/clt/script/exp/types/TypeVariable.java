/*
 * @(#)TypeVariable.java
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
import com.clt.script.exp.Value;

/**
 * 
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

public class TypeVariable extends Type {

  private Type target;
  private boolean generalized;


  public TypeVariable() {

    this(false);
  }


  public TypeVariable(boolean generalized) {

    this.generalized = generalized;
    this.target = null;
  }


  @Override
  public Object clone() {

    if (this.target != null) {
      return this.target.clone();
    }
    else {
      return new TypeVariable(this.generalized);
    }
  }


  private void bind(Type t) {

    // We don't generalize and instantiate yet. So we don't bind either.
    // target = t;
  }


  public void generalize() {

    if (this.target != null) {
      throw new IllegalStateException(
        "Type variable is already bound. Cannot generalize.");
    }
    this.generalized = true;
  }


  public TypeVariable instantiate() {

    if (!this.generalized) {
      throw new IllegalStateException(
        "Type variable is not generalized. Cannot instantiate.");
    }
    return new TypeVariable();
  }


  @Override
  public Type resolve() {

    if (this.target != null) {
      return this.target.resolve();
    }
    else {
      return this;
    }
  }


  @Override
  public boolean equals(Object type) {

    return this == type;
  }


  @Override
  public Type unify(Type t) {

    if (this.generalized) {
      return this.instantiate().unify(t);
    }
    else {
      this.bind(t);
      return t;
    }
  }


  @Override
  public String toString() {

    return "any";
  }


  @Override
  public Class<Value> getObjectClass() {

    return Value.class;
  }
}
