package com.clt.dialogos.plugin;

import com.clt.event.ProgressListener;
import com.clt.event.ProgressEvent;

import java.io.File;
import java.util.*;

/**
 * @author 2mfriedr
 */
public class PluginLoader {

    private static ServiceLoader<Plugin> pluginLoader = ServiceLoader.load(Plugin.class);

    /**
     *
     * @return a collection of plugins
     */
    public static Collection<Plugin> getPlugins() {
        List<Plugin> plugins = new ArrayList<>();
        pluginLoader.iterator().forEachRemaining(plugins::add);
        return plugins;
    }

    /**
     *
     * @return the number of available plugins
     */
    public static int getNumberOfPlugins() {
        return getPlugins().size();
    }

    public static void loadPlugins(File appDir, ProgressListener progressListener) {
        int numberOfPlugins = getNumberOfPlugins();
        int loadingPluginIndex = 0;
        for (Plugin plugin : pluginLoader) {
            loadingPluginIndex++;
            ProgressEvent event = new ProgressEvent(plugin, plugin.getName(), 0, numberOfPlugins, loadingPluginIndex);
            progressListener.progressChanged(event);
            plugin.initialize();
        }
    }

    public static void terminatePlugins() {
        for (Plugin plugin : pluginLoader) {
            plugin.terminate();
        }
    }
}
