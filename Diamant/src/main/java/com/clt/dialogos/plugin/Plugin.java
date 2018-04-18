package com.clt.dialogos.plugin;

import javax.swing.*;

/**
 * The interface for DialogOS plugins. Plugin classes should load instantly, any
 * costly operations should be deferred to {@link #initialize()}.
 *
 * @author dabo, 2mfriedr
 *
 */
public interface Plugin {

    /**
     * A unique ID to identify this plugin, e.g. com.example.myplugin
     */
    String getId();

    /**
     * A user readable name for this plugin.
     */
    String getName();

    /**
     * The icon for this plugin, for the settings screen.
     */
    Icon getIcon();

    /**
     * The version string, for the settings screen.
     *
     */
    String getVersion();

    /**
     * This method is called when a plugin is loaded. Plugins can override it
     * e.g. to register their node types or to perform other costly operations.
     * An empty implementation is provided by default.
     */
    default void initialize() {
    }

  /**
   * This method is called when a plugin should terminate. It should be used to clean up any resources. An empty
   * implementation is provided by default.
   */
    default void terminate() {
    }

  /**
   * This method is called later from the document. Plugins need to return their settings (including a settings GUI
   * component and reading and writing from XML).
   * @return the plugin settings
   */
    PluginSettings createDefaultSettings();
}
