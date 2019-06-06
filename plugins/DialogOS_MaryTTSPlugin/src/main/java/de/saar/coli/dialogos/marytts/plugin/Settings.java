package de.saar.coli.dialogos.marytts.plugin;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.clt.dialogos.plugin.PluginRuntime;
import com.clt.dialogos.plugin.PluginSettings;
import com.clt.diamant.IdMap;
import com.clt.diamant.graph.Node;
import com.clt.gui.GUI;
import com.clt.gui.OptionPane;
import com.clt.properties.DefaultEnumProperty;
import com.clt.properties.DefaultIntegerProperty;
import com.clt.properties.EnumProperty;
import com.clt.properties.IntegerProperty;
import com.clt.properties.Property;
import com.clt.properties.PropertySet;
import com.clt.speech.tts.VoiceName;
import com.clt.xml.XMLReader;
import com.clt.xml.XMLWriter;

/**
 *
 * @author Nicolas (taking over the Settings class of the old Realspeak Plugin
 * by Daniel Bobbert)
 *
 */
public class Settings extends PluginSettings {

    private final EnumProperty<VoiceName> defaultVoice;
    private final IntegerProperty speed;
    private final IntegerProperty pitch;
    private final IntegerProperty volume;

    public Settings() {
        List<VoiceName> voices = Plugin.getAvailableVoices();

        this.defaultVoice = new DefaultEnumProperty<VoiceName>("voice",
                        Resources.getString("DefaultVoice"), null, voices
                        .toArray(new VoiceName[voices.size()]), voices.get(0)) {

            @Override
            public String getName() {

                return Resources.getString("DefaultVoice");
            }

            @Override
            public void setValueFromString(String value) {

                for (VoiceName n : this.getPossibleValues()) {
                    if (n.toString().equals(value) || n.getName().equals(value)) {
                        this.setValue(n);
                        break;
                    }
                }
            }
        };

        this.speed = new DefaultIntegerProperty("speed", Resources.getString("Speed"), null, 0, 200, 100) {
            @Override
            public String getName() {
                return Resources.getString("Speed");
            }
        };

        this.pitch
                = new DefaultIntegerProperty("pitch", Resources.getString("Pitch"), null,
                        -200, 200, 0) {

            @Override
            public String getName() {

                return Resources.getString("Pitch");
            }
        };

        this.volume
                = new DefaultIntegerProperty("volume", Resources.getString("Volume"), null,
                        0, 100,
                        80) {

            @Override
            public String getName() {

                return Resources.getString("Volume");
            }
        };

        if (!voices.isEmpty()) {
            this.defaultVoice.setValue(voices.iterator().next());
        }

    }

    public VoiceName getDefaultVoice() {
        return this.defaultVoice.getValue();
    }

    public int getSpeed() {

        return this.speed.getValue();
    }

    public int getPitch() {

        return this.pitch.getValue();
    }

    public int getVolume() {
        return this.volume.getValue();
    }

    public String getStrVolume() {//TODO
        return "loud";
    }

    @Override
    public JComponent createEditor() {
        JPanel p = new JPanel(new BorderLayout(12, 12));

        p.add(new PropertySet<Property<?>>(this.defaultVoice,
                this.speed, this.pitch, this.volume).createPropertyPanel(false),
                BorderLayout.NORTH);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));

        final JButton tryPrompt = new JButton(Resources.getString("Try"));
        tryPrompt.addActionListener(new TryPromptActionListener(tryPrompt));
        bottom.add(tryPrompt);
        p.add(bottom, BorderLayout.SOUTH);
        return p;
    }

    @Override
    public void writeAttributes(XMLWriter out, IdMap uidMap) {
        // nothing to be written
    }

    @Override
    protected void readAttribute(XMLReader r, String name, String value, IdMap uidMap) {
        // nothing to be read
    }

    @Override
    public boolean isRelevantForNodes(Set<Class<? extends Node>> nodeTypes) {
        return nodeTypes.contains(TTSNode.class);
    }

    @Override
    protected PluginRuntime createRuntime(Component parent) {
        return () -> {
            // no runtime
        };
    }

    private class TryPromptActionListener implements ActionListener {
        JButton tryPrompt;
        boolean speaking = false;

        TryPromptActionListener(JButton tryPrompt) {
            this.tryPrompt = tryPrompt;
        }

        private void reset() {
            Plugin.getSynthesizer().stop();//Only MaryTTS now
            speaking = false;
            tryPrompt.setText(Resources.getString("Try"));
        }

        public synchronized void actionPerformed(ActionEvent e) {

            if (speaking) {
                this.reset();
            } else {
                new Thread(() -> {
                    try {
                        speaking = true;
                        tryPrompt.setText(GUI.getString("Cancel"));

                        Locale language
                                = Settings.this.defaultVoice.getValue()
                                .getVoice()
                                .getLanguage().getLocale();
                        if (language.equals(Locale.UK) || language.equals(Locale.US)) {
                            language = new Locale("", "");
                        }
                        String prompt
                                = Resources.format("VoiceSample", language,
                                Settings.this.defaultVoice.getValue()
                                        .getNormalizedName());
                        TTSNode.speak(Settings.this, Settings.this.defaultVoice
                                .getValue(), prompt);
                    } catch (Exception exn) {
                        String msg = exn.getLocalizedMessage();
                        if ((msg == null) || (msg.length() == 0)) {
                            msg = exn.getClass().getName();
                        }
                        OptionPane.error(tryPrompt, msg);
                    }
                    reset();
                }).start();
            }
        }

    }
}
