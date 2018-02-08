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
