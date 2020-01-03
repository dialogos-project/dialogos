package de.saar.coli.dialogos.marytts.plugin;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.Icon;

import com.clt.dialogos.plugin.PluginSettings;
import com.clt.diamant.graph.Node;
import com.clt.gui.Images;
import com.clt.script.exp.ExecutableFunctionDescriptor;
import com.clt.script.exp.Type;
import com.clt.script.exp.Value;
import com.clt.script.exp.values.RealValue;
import com.clt.speech.tts.VoiceName;
import de.saar.coli.dialogos.marytts.MaryTTS;

/**
 * @author dabo
 * 
 */
public class Plugin implements com.clt.dialogos.plugin.Plugin {

  @Override
  public void initialize() {
    // Load the synthesizer here (on the loading screen) even though it's not used yet. It takes a couple of seconds
    // that we should better spend waiting here than after clicking "New Dialog"
    getSynthesizer();

    Node.registerNodeTypes(com.clt.speech.Resources.getResources().createLocalizedString("IONode"),
        Arrays.asList(TTSNode.class));
  }

  @Override
  public List<ExecutableFunctionDescriptor> registerScriptFunctions() {
    return Arrays.asList(new ExecutableFunctionDescriptor("tts_awaitEndOfSpeech", Type.Void, new Type[]{}) {
                           @Override
                           public Value eval(Value[] args) {
                             getSynthesizer().awaitEndOfSpeech();
                             return Value.Void;
                           }
                         }, new ExecutableFunctionDescriptor("tts_expectedDurationToSay", Type.Real, new Type[] {Type.Any}) {
                           @Override
                           public Value eval(Value[] args) {
                             return new RealValue(getSynthesizer().estimateDurationToSay(args[0].toString()));
                           }
                         }
    );
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
