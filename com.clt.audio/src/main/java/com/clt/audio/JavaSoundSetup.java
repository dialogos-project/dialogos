package com.clt.audio;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.clt.gui.border.GroupBorder;
import com.clt.properties.BooleanProperty;
import com.clt.properties.DefaultBooleanProperty;
import com.clt.properties.DefaultEnumProperty;
import com.clt.properties.EnumProperty;
import com.clt.properties.Property;

public class JavaSoundSetup extends JPanel {

    EnumProperty<AudioFormat.Encoding> encoding;
    EnumProperty<Integer> sampleSize;
    EnumProperty<Integer> sampleRate;
    EnumProperty<Integer> numChannels;
    BooleanProperty bigEndian;
    BooleanProperty signed;

    Map<Property<?>, JComponent> editors;

    Mixer device;

    public JavaSoundSetup() {

        this.setLayout(new BorderLayout());

        this.initProperties();

        JPanel settings = new JPanel(new GridBagLayout());
        settings.setBorder(new GroupBorder(Audio.getString("InputSettings")));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);

        Property<?> properties[] = {
                    this./* encoding, */numChannels, this.sampleSize, this.sampleRate,
                    this.signed, this.bigEndian};

        this.editors = new HashMap<Property<?>, JComponent>();
        gbc.gridx = gbc.gridy = 0;
        for (int i = 0; i < properties.length; i++) {
            gbc.anchor = GridBagConstraints.EAST;
            JComponent c = properties[i].createEditor(false);
            JLabel l = new JLabel(properties[i].getName());
            l.setLabelFor(c);
            settings.add(l, gbc);
            gbc.gridx++;
            gbc.anchor = GridBagConstraints.WEST;
            this.editors.put(properties[i], c);
            settings.add(c, gbc);
            gbc.gridx = 0;
            gbc.gridy++;
        }

        // Container devices = new JPanel(new GridLayout(0, 1, 6, 6));
        Container devices = Box.createVerticalBox();

        ButtonGroup bg = new ButtonGroup();
        Mixer.Info[] mi = AudioSystem.getMixerInfo();

        AbstractButton noAudio = this.createDeviceButton(null);
        bg.add(noAudio);

        this.device = null;
        devices.add(noAudio);
        for (int i = 0; i < mi.length; i++) {
            final Mixer m = AudioSystem.getMixer(mi[i]);
            if (m.getTargetLineInfo(new Line.Info(TargetDataLine.class)).length > 0) {
                AbstractButton b = this.createDeviceButton(mi[i]);
                bg.add(b);
                devices.add(b);
            }
        }

        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new GroupBorder(Audio.getString("RecordingDevice")));
        p.add(devices);
        this.add(p, BorderLayout.WEST);
        this.add(settings, BorderLayout.CENTER);

        noAudio.setSelected(true);
        this.device = null;
        this.updateSettings();
    }

    private AbstractButton createDeviceButton(Mixer.Info mi) {
        final AbstractButton b = new JRadioButton(mi == null ? "None" : mi.getName());
        final Mixer m = mi == null ? null : AudioSystem.getMixer(mi);
        
        if (mi != null) {
            b.setToolTipText("<html>" + "<b>" + mi.getName() + "</b> v"
                            + mi.getVersion() + "<br>"
                            + mi.getVendor() + "<br>" + mi.getDescription() + "</html>");
        }
        
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (b.isSelected()) {
                    JavaSoundSetup.this.device = m;
                    JavaSoundSetup.this.updateSettings();
                }
            }

        });
        return b;
    }

    private void updateSettings() {
        if (this.device == null) {
            for (Property<?> editor : this.editors.keySet()) {
                editor.setEditable(false);
            }
        } else {
            for (Property<?> editor : this.editors.keySet()) {
                editor.setEditable(true);
            }
        }
    }

    public Mixer getDevice() {

        return this.device;
    }

    public AudioFormat getFormat() {

        return new AudioFormat(this.sampleRate.getValue().floatValue(),
                this.sampleSize.getValue().intValue(), this.numChannels.getValue()
                .intValue(), this.signed.getValue(),
                this.bigEndian.getValue());
    }

    private void initProperties() {

        this.encoding = new DefaultEnumProperty<AudioFormat.Encoding>("Encoding",
                        Audio.getString("Encoding"), Audio.getString("EncodingDesc"),
                        new AudioFormat.Encoding[]{AudioFormat.Encoding.PCM_SIGNED,
                            AudioFormat.Encoding.PCM_UNSIGNED,
                            AudioFormat.Encoding.ULAW,
                            AudioFormat.Encoding.ALAW},
                        AudioFormat.Encoding.PCM_SIGNED);
        this.encoding.setEditType(Property.EDIT_TYPE_COMBOBOX);

        this.sampleSize = new DefaultEnumProperty<Integer>("SampleSize", Audio
                        .getString("SampleSize"),
                        Audio.getString("SampleSizeDesc"), new Integer[]{new Integer(8),
            new Integer(16)},
                        new Integer(16));
        this.sampleSize.setEditType(Property.EDIT_TYPE_RADIOBUTTONS_HORIZONTAL);

        this.sampleRate = new DefaultEnumProperty<Integer>("SampleRate", Audio
                        .getString("SampleRate"),
                        Audio.getString("SampleRateDesc"), new Integer[]{
            new Integer(11025),
            new Integer(16000), new Integer(22050), new Integer(32000),
            new Integer(44100),
            new Integer(48000)}, new Integer(44100));
        this.sampleSize.setEditType(Property.EDIT_TYPE_COMBOBOX);

        this.numChannels = new DefaultEnumProperty<Integer>("Channels", Audio.getString("Channels"),
                        Audio.getString("ChannelsDesc"), new Integer[]{new Integer(1), new Integer(2)},
                        new Integer(2));
        this.numChannels.setEditType(Property.EDIT_TYPE_RADIOBUTTONS_HORIZONTAL);

        this.signed = new DefaultBooleanProperty("Signed", Audio.getString("Signed"),
                        Audio.getString("SignedDesc"), true);
        this.bigEndian = new DefaultBooleanProperty("BigEndian", Audio.getString("BigEndian"),
                        Audio.getString("BigEndianDesc"), false);
    }
}
