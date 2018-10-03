package edu.cmu.lti.dialogos.sphinx.plugin;

import com.clt.diamant.Device;
import com.clt.diamant.Resources;
import com.clt.diamant.graph.nodes.AbstractInputNode;
import com.clt.diamant.graph.nodes.NodeExecutionException;
import com.clt.speech.SpeechException;
import com.clt.speech.recognition.LanguageName;
import com.clt.speech.recognition.RecognitionExecutor;
import edu.cmu.lti.dialogos.sphinx.client.Sphinx;

import javax.sound.sampled.AudioFormat;
import java.util.List;

public class SphinxNode extends AbstractInputNode {

    private static Device sphinxDevice = new Device(Resources.getString("Sphinx"));

    @Override
    public AudioFormat getAudioFormat() {
        return Sphinx.getAudioFormat();
    }

    private Sphinx getRecognizer() {
        return Plugin.getRecognizer();
    }

    @Override 
    public RecognitionExecutor createRecognitionExecutor(com.clt.srgf.Grammar recGrammar) {
        try {
            this.getRecognizer().stopRecognition();
        } catch (SpeechException exn) {
            throw new NodeExecutionException(this, Resources.getString("RecognizerError") + ".", exn);
        }
        recGrammar.requestRobustness(Boolean.TRUE == getProperty(ENABLE_GARBAGE));
        return new SphinxRecognitionExecutor(getRecognizer());
    }

    @Override
    public Device getDevice() {
        return sphinxDevice;
    }

    @Override
    public List<LanguageName> getAvailableLanguages() {
        return getSettings() != null ? getSettings().getLanguages() : Plugin.getAvailableLanguages();
    }

    @Override
    public LanguageName getDefaultLanguage() {
        assert this.getGraph() != null : "must not query default language when plugin settings are unreachable";
        return getSettings().getDefaultLanguage();
    }

    /** retrieve the settings from the dialog graph (which is where they are stored -- not within Plugin!) */
    private Settings getSettings() {
        if (getGraph() != null && getGraph().getOwner() != null)
            return ((Settings) getGraph().getOwner()
                .getPluginSettings(Plugin.class));
        else
            return null;
    }
}
