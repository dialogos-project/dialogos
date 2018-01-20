/*
 * @(#)SystemEventAdapter.java
 * Created on 04.04.05
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

package com.clt.mac;

/**
 * 
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

interface SystemEventAdapter {
  public void register(RequiredEventHandler handler) throws Exception;
}
