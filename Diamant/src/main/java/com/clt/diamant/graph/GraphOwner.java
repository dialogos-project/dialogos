package com.clt.diamant.graph;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import com.clt.dialogos.plugin.Plugin;
import com.clt.dialogos.plugin.PluginSettings;
import com.clt.diamant.Device;
import com.clt.diamant.Grammar;
import com.clt.script.Environment;

public interface GraphOwner {

  public Graph getSuperGraph();


  public Graph getOwnedGraph();


  /**
   * Returns the devices configured for this graph.
   * 
   * @return a collection of devices.
   */
  public Collection<Device> getDevices();


  /**
   * Returns the plug-in settings for a given plug-in.
   * 
   * @param pluginClass
   *          Plugin for which the plugin-settings should be returned.
   * @return a PlugingSettings object corresponding to the plug-in.
   */
  public PluginSettings getPluginSettings(Class<? extends Plugin> pluginClass);


  /**
   * Returns the list of grammars of the graph.
   * 
   * @return A list of grammars.
   */
  public List<Grammar> getGrammars();


  public Environment getEnvironment(boolean local);


  public void setDirty(boolean dirty);


  public void export(Graph g, File f)
      throws IOException;


  public String getGraphName();


  public void setGraphName(String name);
}