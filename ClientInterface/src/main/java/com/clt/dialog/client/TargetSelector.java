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
