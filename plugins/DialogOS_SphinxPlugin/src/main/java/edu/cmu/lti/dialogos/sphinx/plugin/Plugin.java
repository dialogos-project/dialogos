package edu.cmu.lti.dialogos.sphinx.plugin;

import com.clt.dialogos.plugin.PluginSettings;
import com.clt.diamant.graph.Node;
import com.clt.gui.Images;
import javax.swing.Icon;
import com.clt.speech.Language;
import com.clt.speech.recognition.LanguageName;
import edu.cmu.lti.dialogos.sphinx.client.Sphinx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Plugin implements com.clt.dialogos.plugin.Plugin {

    @Override
    public void initialize() {
        getRecognizer(); // to perform one-time initialization work at DialogOS startup time
        Node.registerNodeTypes(com.clt.speech.Resources.getResources().createLocalizedString("IONode"),
                Arrays.asList(SphinxNode.class));
    }

    /** turn the List<Language> (given by com.clt.dialogos.sphinx.Sphinx) into a List<LanguageName> */
    static List<LanguageName> getAvailableLanguages() {
        List<LanguageName> languages = new ArrayList<>();
        if (getRecognizer() != null) {
            for (Language l : getRecognizer().getLanguages()) {
                languages.add(new LanguageName(l.getName(), l));
            }
        }
        Collections.sort(languages);
        return languages;
    }

    @Override
    public String getId() {
        return "dialogos.plugin.sphinx";
    }

    @Override
    public String getName() {
        return "CMU Sphinx-4";
    }

    @Override
    public Icon getIcon() {
        return Images.load(this, "asr/asr.png");
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public PluginSettings createDefaultSettings() {
        return new Settings();
    }

    // this mechanism below defers loading SPHINX from startup (Plugin invocation) to first access
    private static class SphinxHolder {
        static final Sphinx SPHINX = new Sphinx();
    }

    public static Sphinx getRecognizer() {
        return SphinxHolder.SPHINX;
    }
}
