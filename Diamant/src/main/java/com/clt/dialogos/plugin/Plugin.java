package com.clt.dialogos.plugin;

import com.clt.diamant.graph.Node;
import com.clt.script.exp.ExecutableFunctionDescriptor;

import javax.swing.*;
import java.util.Collections;
import java.util.List;

/**
 * The interface for DialogOS plugins. A plugin provides a set of custom node
 * types, which can then be used in DialogOS dialogs.<p>
 *
 * See the <a href="https://github.com/dialogos-project/dialogos/wiki/Plugins">
 * Wiki page on plugins</a> for more details.
 *
 * @author dabo, 2mfriedr
 *
 */
public interface Plugin {

    /**
     * A unique ID to identify this plugin, e.g. com.example.myplugin
     * @return 
     */
    String getId();

    /**
     * A user readable name for this plugin. This name is displayed
     * in the plugin's entry in the "Dialog" menu of DialogOS.
     * 
     * @return 
     */
    String getName();

    /**
     * An icon for this plugin. This icon is displayed alongside
     * the name returned by {@link #getName() } in the plugin list
     * of the DialogOS settings window, which appears when any of
     * the plugins or devices in the "Dialog" menu is selected.
     * 
     * @return 
     */
    Icon getIcon();

    /**
     * The version string, for the settings screen. See {@link #getIcon() }.
     *
     * @return 
     */
    String getVersion();

    /**
     * This method is called when a plugin is loaded. This usually happens when
     * DialogOS is started, i.e. on the startup screen.<p>
     *
     * Plugins should load instantly; so the constructor of a plugin should not
     * perform any costly operations. Instead, these operations should be
     * deferred to the initialize method.<p>
     * 
     * The most important function of {@link #initialize() } is to register
     * the node types of the plugin with DialogOS, using {@link Node#registerNodeTypes(java.lang.Object, java.util.Collection) }.<p>
     *
     * The default implementation of initialize does nothing.
     */
    default void initialize() {
    }

    /**
     * This method is called when a plugin should terminate. It should be used
     * to clean up any resources. An empty implementation is provided by
     * default.
     */
    default void terminate() {
    }

    /**
     * This method is called later from the document. Plugins need to return
     * their settings (including a settings GUI component and reading and
     * writing from XML).
     *
     * @return the plugin settings
     */
    PluginSettings createDefaultSettings();

    /**
     * enables a plugin to register script functions
     * so that they become available to the scripting language
     * @return a list of executable function descriptions for the scripting language
     */
    default List<ExecutableFunctionDescriptor> registerScriptFunctions() {
        return Collections.emptyList();
    }
}
