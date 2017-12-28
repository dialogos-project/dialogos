package com.clt.audio;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Vector;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

public class PortAudioInput extends JPanel {

  JPortAudio pa;

  AbstractButton[] devices;
  JRadioButton sampleSize8, sampleSize16;
  JComboBox sampleRate;
  JComboBox numChannels;
  JPanel settings_panel;


  public PortAudioInput() {

    this.setLayout(new GridBagLayout());

    try {
      this.pa = new JPortAudio();
      if (this.pa.numDevices() <= 0) {
        throw new Exception(Audio.getString("NeedASIO"));
      }
    } catch (Exception exn) {
      this.setLayout(new GridLayout(2, 1));
      this.add(new JLabel(Audio.getString("NoAudioDeviceErr")));
      this.add(new JLabel(exn.getLocalizedMessage()));
      this.pa = null;
      return;
    }

    // nicht einfach eine Gruppe. Man kann auch buttons einfach wieder
    // abschalten
    this.devices = new AbstractButton[this.pa.numDevices()];
    ActionListener groupmanager = new ActionListener() {

      public void actionPerformed(ActionEvent e) {

        for (int i = 0; i < PortAudioInput.this.devices.length; i++) {
          if (PortAudioInput.this.devices[i] == e.getSource()) {
            PortAudioInput.this.adjustSettings(i);
          }
          else if (PortAudioInput.this.devices[i].isSelected()) {
            PortAudioInput.this.devices[i].setSelected(false);
          }
        }
      }
    };
    for (int i = 0; i < this.devices.length; i++) {
      JPortAudio.DeviceInfo info = this.pa.getDeviceInfo(i);
      this.devices[i] = new JCheckBox(info.getName());
      if (info.getInputs() == 0) {
        this.devices[i].setEnabled(false);
      }
      this.devices[i].addActionListener(groupmanager);
    }

    GridBagConstraints gbc = new GridBagConstraints();

    gbc.gridx = gbc.gridy = 0;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTH;

    JPanel device_panel = new JPanel();
    device_panel.setBorder(BorderFactory.createTitledBorder(Audio
      .getString("RecordingDevice") + ':'));
    Box devs = Box.createVerticalBox();
    for (int i = 0; i < this.devices.length; i++) {
      devs.add(this.devices[i]);
    }
    device_panel.add(devs);
    this.add(device_panel, gbc);

    gbc.gridx++;
    this.add(this.createSettingsPanel(), gbc);

    this.sampleSize16.setSelected(true);
    if ((this.devices.length > 0) && this.devices[0].isEnabled()) {
      this.devices[0].setSelected(true);
      this.adjustSettings(0);
      ComboBoxModel m = this.sampleRate.getModel();
      for (int i = 0; i < m.getSize(); i++) {
        if (m.getElementAt(i).equals(new Double(16000.0))) {
          this.sampleRate.setSelectedIndex(i);
          break;
        }
      }
      if (this.numChannels.getModel().getSize() > 0) {
        this.numChannels.setSelectedIndex(Math.min(3, this.numChannels
          .getModel().getSize()) - 1);
      }
    }
    else {
      this.adjustSettings(-1);
    }
  }


  private JPanel createSettingsPanel() {

    this.settings_panel = new JPanel(new GridBagLayout());
    this.settings_panel.setBorder(BorderFactory.createTitledBorder(Audio
      .getString("InputSettings") + ':'));

    GridBagConstraints gbc = new GridBagConstraints();

    this.sampleSize8 = new JRadioButton("8 bit");
    this.sampleSize16 = new JRadioButton("16 bit");
    PortAudioInput.createButtonGroup(new AbstractButton[] { this.sampleSize8,
      this.sampleSize16 });

    gbc.gridx = gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.ipadx = 12;

    this.settings_panel.add(new JLabel(Audio.getString("SampleSize")), gbc);
    gbc.gridx++;
    this.settings_panel.add(this.sampleSize8, gbc);
    gbc.gridx++;
    this.settings_panel.add(this.sampleSize16, gbc);

    gbc.gridy++;

    gbc.gridx = 0;
    this.settings_panel.add(new JLabel(Audio.getString("SampleRate")), gbc);
    gbc.gridx++;
    gbc.gridwidth = 2;

    JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    this.sampleRate = new JComboBox();

    p.add(this.sampleRate);
    p.add(Box.createHorizontalStrut(6));
    p.add(new JLabel("Hz"));
    this.settings_panel.add(p, gbc);

    gbc.gridy++;

    gbc.gridx = 0;
    this.settings_panel.add(new JLabel(Audio.getString("Channels")), gbc);
    gbc.gridx++;
    gbc.gridwidth = 2;
    this.numChannels = new JComboBox();
    this.settings_panel.add(this.numChannels, gbc);

    return this.settings_panel;
  }


  private void adjustSettings(int index) {

    if ((index < 0) || (index >= this.devices.length)
      || !this.devices[index].isSelected()) {
      this.sampleSize8.setEnabled(false);
      this.sampleSize16.setEnabled(false);
      this.sampleRate.setEnabled(false);
      this.numChannels.setEnabled(false);
    }
    else {
      try {
        JPortAudio.DeviceInfo info = this.pa.getDeviceInfo(index);
        double[] sr = info.getSampleRates();
        Vector<Double> srs = new Vector<Double>();
        for (int i = 0; i < sr.length; i++) {
          srs.add(new Double(sr[i]));
        }
        this.sampleRate.setModel(new DefaultComboBoxModel(srs));

        Vector<Integer> ch = new Vector<Integer>();
        for (int i = 0; i < info.getInputs(); i++) {
          ch.addElement(new Integer(i + 1));
        }
        this.numChannels.setModel(new DefaultComboBoxModel(ch));

        this.sampleSize8.setEnabled(true);
        this.sampleSize16.setEnabled(true);
        this.sampleRate.setEnabled(true);
        this.numChannels.setEnabled(true);
      } catch (ThreadDeath d) {
        throw d;
      } catch (Throwable exn) {
        exn.printStackTrace();
      }
    }
  }


  private static void createButtonGroup(AbstractButton[] buttons) {

    ButtonGroup g = new ButtonGroup();
    for (int i = 0; i < buttons.length; i++) {
      g.add(buttons[i]);
    }
  }


  private int getSelectedMixer() {

    for (int i = 0; i < this.devices.length; i++) {
      if (this.devices[i].isSelected()) {
        return i;
      }
    }

    return -1;
  }


  public AudioFormat getSettings() {

    if (true) {
      return new AudioFormat(16000.0f, 16, 3, true, false);
    }

    int device = this.getSelectedMixer();
    int nChannels = this.numChannels.getSelectedIndex() + 1;
    int ss = this.sampleSize16.isSelected() ? 16 : 8;
    float sr =
      this.sampleRate.getSelectedItem() == null ? AudioSystem.NOT_SPECIFIED
        : ((Double)this.sampleRate.getSelectedItem()).floatValue();

    return new AudioFormat(sr, ss, nChannels, true, false);
  }


  public void startRecording(Integer device, AudioFormat settings, File f) {

    try {
      if ((this.pa != null) && (settings != null)) {
        if ((device.intValue() != -1) && (settings.getChannels() > 0)
          && (settings.getSampleRate() != AudioSystem.NOT_SPECIFIED)) {
          if (f.exists()) {
            f.delete();
          }

          this.pa.startRecording(device, settings, f);
        }
      }
    } catch (Exception exn) {
      exn.printStackTrace();
    }
  }


  public void stopRecording(Integer device) {

    try {
      if ((this.pa != null) && (device != null)) {
        this.pa.stopRecording(device);
      }
    } catch (Exception exn) {
      exn.printStackTrace();
    }
  }

}
