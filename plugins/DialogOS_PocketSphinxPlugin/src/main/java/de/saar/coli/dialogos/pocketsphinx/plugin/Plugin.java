/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.saar.coli.dialogos.pocketsphinx.plugin;

import com.clt.dialogos.modelcache.ModelCache;
import com.clt.dialogos.plugin.PluginSettings;
import com.clt.diamant.graph.Node;
import com.clt.gui.Images;
import javax.swing.Icon;
import com.clt.speech.Language;
import com.clt.speech.SpeechException;
import de.saar.coli.dialogos.pocketsphinx.PocketSphinx;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.json.simple.parser.ParseException;

/**
 *
 * @author koller
 */
public class Plugin implements com.clt.dialogos.plugin.Plugin {
    private static ModelCache modelCache = new ModelCache(getPluginId());

    @Override
    public void initialize() {
        getRecognizer(); // to perform one-time initialization work at DialogOS startup time

        Node.registerNodeTypes("Speech Recognition", Arrays.asList(new Class<?>[]{PocketSphinxNode.class}));
    }

    static List<LanguageName> getAvailableLanguages() {
        List<LanguageName> languages = new ArrayList<LanguageName>();
        
        if (getRecognizer() != null) {
            try {
                for (Language l : getRecognizer().getLanguages()) {
                    languages.add(new LanguageName(l.getName(), l));
                }
            } catch (SpeechException ex) {
                // this should never happen - AK
                System.err.println("SpeechException in DialogOS_PocketSphinxPlugin.Plugin#getAvailableLanguages: " + ex);
                System.exit(1);
            }
        }

        Collections.sort(languages);

        return languages;
    }
    
    public static void main(String[] args) {
        System.err.println(getAvailableModels());
    }
    
    public static Model[] getAvailableModels() {
        List<String> ids = modelCache.getAllModelIds();
        List<Model> ret = new ArrayList<>();
        
        for( String id : ids ) {
            try {
                Model model = Model.fromModelCache(id, modelCache.getModelDirectory(id));
                ret.add(model);
            } catch (IOException ex) {
                System.err.printf("WARNING: No metadata in PocketSphinx model directory for '%s'. Ignoring it.\n", id);
            } catch (ParseException ex) {
                System.err.printf("WARNING: Invalid metadata in PocketSphinx model directory for '%s'. Ignoring it.\n", id);
            }
        }
        
        return ret.toArray(new Model[0]);
    }
    
    
    public static String getPluginId() {
        return "dialogos.plugin.pocketsphinx";
    }

    @Override
    public String getId() {
        return getPluginId();
    }
    
    public static String getPluginName() {
        return "CMU PocketSphinx";
    }
    
    public static ModelCache getModelCache() {
        return modelCache;
    }

    @Override
    public String getName() {
        return getPluginName();
    }

    @Override
    public Icon getIcon() {
        return Images.load(this, "asr.png");
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public PluginSettings createDefaultSettings() {
        return new Settings();
    }

    private static class SphinxHolder {
        static final PocketSphinx SPHINX = new PocketSphinx();
    }

    /**
     * Returns the (singleton) instance of the Sphinx recognizer
     * used by this plugin.
     * 
     * The recognizer can be set to "dummy mode" in the settings
     * pane. Because we do not have access to the settings here,
     * the recognizer instance is set to dummy/non-dummy mode
     * each time {@link PocketSphinxNode#getRecognizer() } is called,
     * updating the dummy mode as required.
     * 
     * @return 
     */
    public static PocketSphinx getRecognizer() {
        return SphinxHolder.SPHINX;
    }
}
