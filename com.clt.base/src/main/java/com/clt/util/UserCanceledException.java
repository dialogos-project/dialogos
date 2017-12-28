/*
 * @(#)UserCanceledException.java
 *
 * Copyright (c) 2001 CLT Sprachtechnologie GmbH.
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
 * Throwing a UserCanceledException indicates that the user has aborted an
 * action, in most cases by clicking a "Cancel" button in a dialog. The
 * exception doesn't carry any further information.
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

public class UserCanceledException
    extends Exception {

  public UserCanceledException() {

    super();
  }
}