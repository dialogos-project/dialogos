package de.saar.coli.dialogos.marytts.plugin;

import java.awt.*;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;

import com.clt.diamant.graph.nodes.AbstractOutputNode;
import de.saar.coli.dialogos.marytts.MaryTTS;

import com.clt.diamant.gui.NodePropertiesDialog;
import com.clt.speech.SpeechException;
import com.clt.speech.tts.Synthesizer;
import com.clt.speech.tts.Voice;
import com.clt.speech.tts.VoiceName;
import com.clt.util.StringTools;
import marytts.util.dom.DomUtils;
import org.xml.sax.SAXException;

/**
 * This is the implementation of the text-to-speech node for the
 * Mary-synthesizer. It includes the following configurations: + prompt-type:
 * Either text, expression or maryxml. (Enum PromptType, GUI: Radio buttons) +
 * prompt: User text to be spoken; evaluated according to prompt-type. (String,
 * GUI: Text field) + voice (Voicename, GUI: ComboBox) + wait: Wait for that to
 * be spoken to evaluate further TTSNodes (Boolean, GUI: checkbox) + xml-area:
 * XML that is generated from what's in the prompt and Settings configs.
 * (String, GUI: Text field) The configurations are stored in a property map
 * (See: DefaultPropertyContainer, which is extended by TTSNode). The GUIs are
 * created with the help of the NodePropertiesDialog. Note: When the prompt is
 * of type MaryXML the configurations in Settings are ignored.
 *
 * @author Nicolas and Phil (taking over the TTSNode class of the old Realspeak
 * Plugin by Daniel Boobert)
 * @author Till and Bri (Groovy functionality) TODO : This recasting to MaryTTS
 * every time we dont know how to call it doesnt make sense! Change that!
 */
public class TTSNode extends AbstractOutputNode {

    public static String getNodeTypeName(Class<?> c) {
        return Resources.getString("TTS");
    }

    private static final String XMLAREA = "xmlarea";

    public String getResourceString(String key) {
        return Resources.getString(key);
    }

    public TTSNode() {
        this.setProperty(TTSNode.XMLAREA, "");
    }

    @Override
    public List<VoiceName> getAvailableVoices() {
        List<VoiceName> voices = Plugin.getAvailableVoices();
        voices.add(0, new VoiceName("", null));

        if (!voices.contains(properties.get(VOICE))) {
            voices.add(1, (VoiceName) properties.get(VOICE));
        }
        return voices;
    }

    @Override
    protected JTabbedPane addMoreTabsToEditorComponent(JTabbedPane jtp) {
        //Create second tab for maryXML
        JPanel p2 = new JPanel(new GridBagLayout());
        p2.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.gridx = gbc2.gridy = 0;
        gbc2.weightx = 0.0;
        gbc2.weighty = 0.0;
        gbc2.fill = GridBagConstraints.HORIZONTAL;
        gbc2.insets = new Insets(3, 3, 3, 3);
        p2.add(new JLabel(Resources.getString("Prompt") + ':'), gbc2);

        gbc2.gridx++;
        gbc2.weightx = 1.0;
        gbc2.weighty = 1.0;
        gbc2.fill = GridBagConstraints.BOTH;

        // We will create our own JScrollPane instead of taking one from NodePropertiesDialog
        // because we need a reference on the textArea contained in JScrollPane. (Text area in XML tab).
        JTextArea jta = NodePropertiesDialog.createTextAreaWithoutJScrollPane(properties, TTSNode.XMLAREA);

        JScrollPane jspane = new JScrollPane(jta, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jspane.setMinimumSize(new Dimension(300, 150));
        jspane.setPreferredSize(jspane.getMinimumSize());

        p2.add(jspane, gbc2);

        gbc2.gridx++;
        gbc2.weightx = 1.0;
        gbc2.weighty = 1.0;
        gbc2.fill = GridBagConstraints.BOTH;
        final JButton refreshB = new JButton(Resources.getString("Prompt to XML"));
        refreshB.addActionListener(actionEvent -> {
            String text = (String) properties.get(TTSNode.XMLAREA);
            String strXML = Plugin.getSynthesizer().createMaryXMLStr(text);
            properties.replace(XMLAREA, strXML);
            jta.setText(strXML);
        });
        //Add buttons
        JPanel border2 = new JPanel(new BorderLayout());
        border2.add(refreshB, BorderLayout.CENTER);
        border2.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        GridBagConstraints gbc = new GridBagConstraints();
        p2.add(border2, gbc);
        jtp.addTab("MaryXML", p2);
        return jtp;
    }


    /*
  * stopSynthesis: Stops the Synthesis midway.
     */
    public void stopSynthesis() {
        Plugin.getSynthesizer().stop();
    }

    /*
  * speak: Given a properties map (configurations of the node) speaks what's
  * in the prompt (possibly according to the settings).
  * */
    public void speak(String prompt, Map<String, Object> properties) throws SpeechException {

        Settings settings = (Settings) this.getGraph().getOwner().getPluginSettings(Plugin.class);

        // Till and Bri's (from the merge)
        //    VoiceName voicename = (VoiceName)properties.get(TTSNode.VOICE);
        //    if ((voicename == null) || StringTools.isEmpty(voicename.getName())) {
        //      voicename = settings.getDefaultVoice();
        //    }
        //    TTSNode.speak(settings, voicename, prompt);
        MaryTTS synthesizer = (MaryTTS) Plugin.getSynthesizer();
        VoiceName voicename = (VoiceName) properties.get(VOICE);
        setVoice(settings, synthesizer, voicename);
        ((MaryTTS) synthesizer).setProsody2MaryXML(settings.getStrVolume(), settings.getPitch(), settings.getSpeed());
        //Should the audio be played uninterrumped?
        boolean wait = ((Boolean) properties.get(WAIT)).booleanValue();
        boolean awaitSilence = ((Boolean) properties.get(AWAIT_SILENCE)).booleanValue();
        if (awaitSilence)
            synthesizer.awaitEndOfSpeech();
        else
            synthesizer.stop();
        //Prompt is maryxml or text?
        boolean isMaryXML = true;
        try {
            DomUtils.parseDocument(new StringReader(prompt), false);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            isMaryXML = false;
        }
        if (isMaryXML) {
            //Note: Regardless of the configurations in Settings it will
            //speak according to the configs in the prompt (xml)
            synthesizer.speakStrMaryXML(prompt, wait);
        } else { //text
            synthesizer.speak(prompt, wait);
        }
    }

    /*
  * speak: Speaks whats in prompt according to the given settings and voicename.
  * */
    static void speak(Settings settings, VoiceName voicename, String prompt)
            throws SpeechException {
        Synthesizer synthesizer = Plugin.getSynthesizer();
        setVoice(settings, synthesizer, voicename);
        ((MaryTTS) synthesizer).setProsody2MaryXML(settings.getStrVolume(), settings.getPitch(), settings.getSpeed());
        synthesizer.speak(prompt);
    }

    /**
     * setVoice: Sets the given voicename to the given synthesizer.
   *
     */
    private static void setVoice(Settings settings, Synthesizer synthesizer, VoiceName voicename)
            throws SpeechException {

        if ((voicename == null) || StringTools.isEmpty(voicename.getName())) {
            voicename = settings.getDefaultVoice();
        }
        //Set up voice:
        Voice voice = synthesizer.findVoice(voicename.getName());
        if (voice == null) {
            throw new SpeechException(Resources.format("VoiceNotFound",
                    voicename.getNormalizedName()));
        }
        synthesizer.setVoice(voice);
    }

}
