package com.clt.dialog.client;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import com.clt.util.LongAction;

public interface ConnectionChooser {

  /** Prepare the chooser for the given number of devices. */
  public void init(ServerDevice[] device);


  /**
   * Execute the specified action. The chooser should use an appropriate method
   * to display the progress of the action. If exceptions thrown by the action
   * aren't handled by the chooser, they must be wrapped into an
   * InvocationTargetException.
   */
  public void start(LongAction action)
      throws InvocationTargetException;


  /**
   * Resolve ambiguities in case several clients were found for a device.
   * 
   * @param ambiguities
   *          A map from ServerDevice to Object[], where the object array
   *          contains descriptions of the possible choices
   * @param defaults
   *          A map from ServerDevice to Object, where the object represents the
   *          choice that should be selected as the default
   * @return true, if disambiguation was successful, or false if the user
   *         cancelled
   */
  public boolean resolve(Map<ServerDevice, Object[]> ambiguities,
            Map<ServerDevice, Object> defaults);


  /**
   * Signal, that the given device changed its state. This is a hint to improve
   * feedback for the user. The chooser implementation is allowed to ignore this
   * hint.
   */
  public void stateChanged(ServerDevice d);


  /**
   * Signal, that a certai protocal was chosen for a device. This is a hint to
   * improve feedback for the user. The chooser implementation is allowed to
   * ignore this hint.
   */
  public void protocolChanged(ServerDevice d, String protocol);


  /**
   * Signal, that multiple clients were found for a device. This is a hint to
   * improve feedback for the user, even before resolve() is called. The chooser
   * implementation is allowed to ignore this hint.
   */
  public void ambiguityDetected(ServerDevice d,
      Map<ServerDevice, Object[]> ambiguities);
}