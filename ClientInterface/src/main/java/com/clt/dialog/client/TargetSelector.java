/*
 * @(#)MultipleTargetsException.java
 * Created on 20.10.04
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

package com.clt.dialog.client;

/**
 * 
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

public interface TargetSelector {

  public <T> T choose(T[] options, T defaultOption);
}
