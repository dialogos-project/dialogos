/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.clt.dialogos.modelcache;

import com.clt.diamant.Preferences;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author koller
 */
public class ModelCache {
    private String plugin;
    private File pluginDirectory;

    public ModelCache(String plugin) {
        this.plugin = plugin;
        pluginDirectory = new File(Preferences.getBaseDirectory(), plugin);
        if (!pluginDirectory.exists()) {
	    pluginDirectory.mkdirs();
	}
    }
    
    public boolean modelExists(String modelId) {
        return getModelDirectory(modelId).exists();
    }
    
    public File getModelDirectory(String modelId) {
        return new File(pluginDirectory, modelId);
    }
    
    public File createModelDirectory(String modelId) {
        File ret = getModelDirectory(modelId);
        ret.mkdirs();
        return ret;
    }
    
    public List<String> getAllModelIds() {
        List<String> ret = new ArrayList<>();
        
        for( File modelDir : pluginDirectory.listFiles() ) {
            if( modelDir.isDirectory() ) {
                ret.add(modelDir.getName());
            }
        }
        
        return ret;
    }
}
