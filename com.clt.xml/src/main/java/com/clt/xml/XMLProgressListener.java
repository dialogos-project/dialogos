/*
 * @(#)XMLProgressListener.java
 * Created on 20.01.05
 *
 * Copyright (c) 2005 CLT Sprachtechnologie GmbH.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CLT Sprachtechnologie GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CLT Sprachtechnologie GmbH.
 */

package com.clt.xml;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */

public interface XMLProgressListener {

  public void percentComplete(float percent);
}
