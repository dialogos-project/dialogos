/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.saar.coli.dialogos.pocketsphinx.plugin;

import com.clt.dialogos.plugin.PluginRuntime;

/**
 *
 * @author koller
 */
public class Runtime implements PluginRuntime {

    @Override
    public void dispose() {
        // Vocon:
//        try {
//            Plugin.recognizer.stopRecognition();
//        } catch (SpeechException exn) {
//            exn.printStackTrace();
//        }
    }

}
