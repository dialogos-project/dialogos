/*
 * @(#)Cancellable.java
 * Created on Tue Mar 04 2003
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

package com.clt.util;

/**
 * Instances of Cancellable describe actions that can be canceled.
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

public interface Cancellable {

  public void cancel();


  public boolean canCancel();


  public String getCancelConfirmationPrompt();
}
