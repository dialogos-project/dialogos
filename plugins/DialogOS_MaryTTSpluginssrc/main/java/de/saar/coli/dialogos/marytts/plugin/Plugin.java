

package de.saar.coli.dialogos.marytts.plugin;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.Icon;

import com.clt.dialogos.plugin.PluginSettings;
import com.clt.diamant.graph.Node;
import com.clt.gui.Images;
import de.saar.coli.dialogos.marytts.MaryTTS;

/**
 * @author dabo
 * 
 */
public class Plugin
    implements com.clt.dialogos.plugin.Plugin {

  @Override
  public void initialize() {
    // Load the synthesizer here (on the loading screen) even though it's not used yet. It takes a couple of seconds
    // that we should better spend waiting here than after clicking "New Dialog"
    getSynthesizer();

    Node.registerNodeTypes(Resources.getResources()
            .createLocalizedString("TTS"),
        Arrays.asList(new Class<?>[] { TTSNode.class }));
  }

  public static List<VoiceName> getAvailableVoices() {
    return Arrays.stream(getSynthesizer().getAvailableVoices())
        .map(voice -> new VoiceName(voice.getName(), voice))
        .collect(Collectors.toList());
}

  @Override
  public String getId() {
    return "dialogos.plugin.tts";
  }


  @Override
  public String getName() {
    return Resources.getString("TTS");
  }


  @Override
  public Icon getIcon() {
    return Images.load(this, "TTS.png");
  }


  @Override
  public String getVersion() {
    return "222";
  }

  @Override
  public PluginSettings createDefaultSettings() {
    return new Settings();
  }

  /**
   * Initialization-on-demand holder for the MaryTTS synthesizer. This is necessary because plugin classes should have
   * minimal class load overhead and MaryHolder is initialized later.
   */
  private static class MaryHolder {
    static final MaryTTS MARY = new MaryTTS();
  }

  /**
   * Returns the (interface to) MaryTTS-synthesizer
   */
  public static MaryTTS getSynthesizer() {
    return MaryHolder.MARY;
  }
}
