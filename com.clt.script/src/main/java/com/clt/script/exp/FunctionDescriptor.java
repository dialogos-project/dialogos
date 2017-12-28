/*
 * @(#)FunctionDescriptor.java
 * Created on Tue Aug 03 2004
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

package com.clt.script.exp;

/**
 * 
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

public abstract class FunctionDescriptor {

  public abstract String getName();


  public abstract Type getReturnType();


  public abstract Type[] getParameterTypes();


  public abstract boolean isVarArg();


  @Override
  public String toString() {

    return this.getDescription(false);
  }


  public String getDescription(boolean html) {

    StringBuilder b = new StringBuilder();
    if (html) {
      b.append("<html><body>");
    }
    b.append(this.getReturnType());
    b.append(' ');
    if (html) {
      b.append("<b>");
    }
    b.append(this.getName());
    if (html) {
      b.append("</b>");
    }
    b.append(" (");

    Type[] params = this.getParameterTypes();
    for (int j = 0; j < params.length; j++) {
      if (j == 0) {
        b.append(" ");
      }
      else {
        b.append(", ");
      }
      b.append(params[j]);
    }

    if (this.isVarArg()) {
      if (params.length > 0) {
        b.append(", ");
      }
      b.append("...");
    }

    b.append(" )");
    if (html) {
      b.append("</body></html>");
    }
    return b.toString();
  }
}