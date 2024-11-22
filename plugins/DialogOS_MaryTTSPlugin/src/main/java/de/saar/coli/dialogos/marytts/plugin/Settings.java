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
import com.clt.diamant.graph.Graph;
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

    private static final String DEFAULT_VOICE = "defaultVoice";
    private final EnumProperty<VoiceName> defaultVoice;
    private static final String DEFAULT_SPEED = "defaultTempo";
    private static final int DEFAULT_SPEED_VALUE = 100;
    private final IntegerProperty speed;
    private static final String DEFAULT_PITCH = "defaultRegister";
    private static final int DEFAULT_PITCH_VALUE = 0;
    private final IntegerProperty pitch;
    private static final String DEFAULT_VOLUME = "defaultLoudness";
    private static final int DEFAULT_VOLUME_VALUE = 80;
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

        this.speed = new DefaultIntegerProperty("speed", Resources.getString("Speed"), null, 0, 200, DEFAULT_SPEED_VALUE) {
            @Override
            public String getName() {
                return Resources.getString("Speed");
            }
        };

        this.pitch = new DefaultIntegerProperty("pitch", Resources.getString("Pitch"), null,
                        -200, 200, DEFAULT_PITCH_VALUE) {
            @Override
            public String getName() {
                return Resources.getString("Pitch");
            }
        };

        this.volume = new DefaultIntegerProperty("volume", Resources.getString("Volume"), null,
                        0, 100, DEFAULT_VOLUME_VALUE) {
            @Override
            public String getName() {
                return Resources.getString("Volume");
            }
        };

        if (!voices.isEmpty()) {
            this.defaultVoice.setValue(voices.get(0));
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
        if (!this.getDefaultVoice().equals(Plugin.getAvailableVoices().get(0))) {
            Graph.printAtt(out, DEFAULT_VOICE, this.getDefaultVoice().toString());
        }
        if (this.getSpeed() != DEFAULT_SPEED_VALUE) {
            Graph.printAtt(out, DEFAULT_SPEED, this.getSpeed());
        }
        if (this.getVolume() != DEFAULT_VOLUME_VALUE) {
            Graph.printAtt(out, DEFAULT_PITCH, this.getPitch());
        }
        if (this.getVolume() != DEFAULT_VOLUME_VALUE) {
            Graph.printAtt(out, DEFAULT_VOLUME, this.getVolume());
        }
    }

    @Override
    protected void readAttribute(XMLReader r, String name, String value, IdMap uidMap) {
        if (name.equals(DEFAULT_VOICE)) {
            this.defaultVoice.setValueFromString(value);
        }
        if (name.equals(DEFAULT_SPEED)) {
            this.speed.setValueFromString(value);
        }
        if (name.equals(DEFAULT_PITCH)) {
            this.pitch.setValueFromString(value);
        }
        if (name.equals(DEFAULT_VOLUME)) {
            this.volume.setValueFromString(value);
        }
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
