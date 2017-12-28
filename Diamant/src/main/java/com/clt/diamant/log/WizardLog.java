package com.clt.diamant.log;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import com.clt.audio.JavaSoundSetup;
import com.clt.audio.WaveFileOutputStream;
import com.clt.diamant.Document;
import com.clt.diamant.IdMap;
import com.clt.diamant.Resources;
import com.clt.diamant.Version;
import com.clt.diamant.WozInterface;
import com.clt.gui.CmdButton;
import com.clt.gui.Commander;
import com.clt.gui.Commands;
import com.clt.gui.FileChooser;
import com.clt.gui.GUI;
import com.clt.gui.OptionPane;
import com.clt.properties.BooleanProperty;
import com.clt.properties.DefaultBooleanProperty;
import com.clt.util.UserCanceledException;
import com.clt.xml.XMLWriter;

public class WizardLog {

  // Don't change. These are written to XML!
  public final static String[] infos = { "VP", "Kommentar" };

  public final static String[] infonames = { Resources.getString("User"),
            Resources.getString("Comment") };
  private static final String audioFileName = "log.wav";

  private XMLWriter out = null;
  private long base;

  private Component parent;
  private File dir;
  private JCanLog CanLog = null;
  private Mixer mixer = null;
  private AudioFormat audioFormat = null;
  private Map<String, String> values = null;
  private boolean showSubdialogsDuringExecution = false;

  transient TargetDataLine audioInput = null;

  private static FileChooser fileChooser = new FileChooser();


  public WizardLog(Component parent)
      throws UserCanceledException {

    this(parent, false);
  }


  public WizardLog(Component parent, boolean reduced)
      throws UserCanceledException {

    this.parent = parent;

    try {
      this.CanLog = new JCanLog();
    } catch (Exception exn) {
    }

    SetupDialog setup =
      new SetupDialog(parent, WizardLog.infonames, this.CanLog != null, reduced);
    setup.setVisible(true);
    if (setup.jss == null) {
      throw new UserCanceledException();
    }

    if (setup.recordCAN == false) {
      this.CanLog = null;
    }

    this.showSubdialogsDuringExecution =
      setup.showSubdialogsDuringExecution.getValue();

    this.mixer = setup.jss.getDevice();
    this.audioFormat = setup.jss.getFormat();
    this.values = new HashMap<String, String>();
    for (int i = 0; i < WizardLog.infos.length; i++) {
      this.values.put(WizardLog.infos[i], setup.values[i]);
    }

    Calendar now = Calendar.getInstance();
    now.setTime(new Date(System.currentTimeMillis()));

    this.dir =
      WizardLog.fileChooser.standardPutFile(parent, "Log_"
        + WizardLog.getDate(now) + "_" + WizardLog.getTime(now, '.'));
    if (this.dir == null) {
      throw new UserCanceledException();
    }

    this.dir.mkdir();
  }


  public boolean showSubdialogsDuringExecution() {

    return this.showSubdialogsDuringExecution;
  }


  public synchronized void setFile(String name)
      throws IOException {

    try {
      if (this.out != null) {
        this.out.close();
      }
    } catch (Exception exn) {
    }

    File f = new File(this.dir, name + "_log.xml");
    int i = 0;
    while (f.exists()) {
      f = new File(this.dir, name + "_log." + (++i) + ".xml");
    }
    this.out = new XMLWriter(f);
    this.out.println("<!DOCTYPE log SYSTEM \"Log.dtd\">");
    // DiskTools.setFileCreator(f, "R*ch");
    // DiskTools.setFileType(f, "TEXT");

    if (this.CanLog != null) {
      if (i == 0) {
        f = new File(this.dir, name + "_can.txt");
      }
      else {
        f = new File(this.dir, name + "_can." + i + ".txt");
      }
      this.CanLog.setFile(f);
    }
  }


  public void setState(WozInterface.State state) {

    if (this.CanLog != null) {
      this.CanLog.setState(state);
    }

    this.printEvent("state", "value", state.getName());
  }


  public XMLWriter getWriter() {

    return this.out;
  }


  public void startRecording()
      throws Exception {

    if (this.mixer != null) {
      try {
        this.audioInput =
          (TargetDataLine)this.mixer.getLine(new DataLine.Info(
            TargetDataLine.class,
                    this.audioFormat));
        this.audioInput.open(this.audioFormat);

        final AudioFormat format = this.audioInput.getFormat();
        this.audioInput.start();

        final TargetDataLine line = this.audioInput;
        new Thread(new Runnable() {

          public void run() {

            int chunkSize = 50; // number of milliseconds per sound buffer
            int bufferSize =
              (int)((chunkSize * format.getFrameSize() * format.getFrameRate()) / 1000l);
            bufferSize -= bufferSize % format.getFrameSize();
            byte[] data = new byte[bufferSize];
            try {
              WaveFileOutputStream out =
                new WaveFileOutputStream(new File(WizardLog.this.dir,
                                WizardLog.audioFileName), format);
              while (WizardLog.this.audioInput != null) {
                int bytesread = line.read(data, 0, data.length);
                out.write(data, 0, bytesread);
              }
              out.close();

              line.stop();
              line.close();
            }
                        catch (Exception exn) {
                          exn.printStackTrace();
                          OptionPane.error(WizardLog.this.parent, exn);
                        }
                      }
        }, Version.PRODUCT_NAME + " Audio Log").start();
      } catch (Exception exn) {
        this.stopRecording();
        throw exn;
      }
    }

    /*
     * try { AudioSystem.write(new AudioInputStream(audioInput),
     * AudioFileFormat.Type.WAVE, new File(dir, audioFileName));
     * System.out.println("done"); } catch (Exception exn) {
     * exn.printStackTrace(); OptionPane.error(parent, exn); }
     */

    // ...
    // setup.pa.startRecording(setup.settings, new File(dir, audioFileName));
  }


  public void stopRecording() {

    this.audioInput = null;

    /*
     * setup.pa.stopRecording(setup.settings);
     * 
     * final File f = new File(dir, audioFileName); if (setup.settings != null
     * && f.isFile()) { try { new ProgressDialog(parent).run(new
     * AbstractLongAction() { public String getDescription() { return
     * Resources.getString("ConvertingAudio") + "..."; }
     * 
     * public void run(ProgressListener l) { try { RawToWave.split(f,
     * setup.settings, l); } catch (Exception exn) { OptionPane.message(parent,
     * new String[] { Resources.getString("UnexpectedError"), exn.toString() });
     * } } }); } catch (Exception exn) { if (Version.DEBUG)
     * exn.printStackTrace(); } } setup.pa.dispose(); setup = null;
     */
  }


  public synchronized IdMap start(Document d) {

    try {
      if (this.CanLog != null) {
        this.CanLog.start();
      }
    } catch (ThreadDeath exn) {
      throw exn;
    } catch (Throwable exn) {
      OptionPane
        .error(
          null,
          new String[] {
                    "Error while initializing CAN bus. CAN logging will be disabled.",
                    exn.toString() });
      this.CanLog = null;
    }

    Calendar now = Calendar.getInstance();
    now.setTime(new Date(System.currentTimeMillis()));

    IdMap uid_map = new IdMap(true);

    this.out.openElement("log");
    this.out.openElement("setup");
    this.out.printElement("time", WizardLog.getDate(now) + ", "
      + WizardLog.getTime(now, ':'));
    for (int i = 0; i < WizardLog.infos.length; i++) {
      this.out.printElement(WizardLog.infos[i], this.values
        .get(WizardLog.infos[i]));
    }

    this.out.closeElement("setup");
    d.write(this.out, uid_map);
    this.out.println();
    this.out.openElement("execution");

    this.base = System.currentTimeMillis();
    this.printEvent("start", null, null);

    return uid_map;
  }


  public synchronized void end() {

    try {
      if (this.CanLog != null) {
        this.CanLog.stop();
      }
    } catch (Exception exn) {
    }

    this.printEvent("end", null, null);
    this.out.closeElement("execution");
    this.out.closeElement("log");
    this.out.close();

    this.out = null;
  }


  public synchronized void printElement(String type, Object value) {

    if (this.out != null) {
      this.printElement(type, null, null, value);
    }
  }


  public synchronized void printElement(String tag, String arg, Object value) {

    if (this.out != null) {
      this
        .printElement(tag, new String[] { arg }, new Object[] { value }, null);
    }
  }


  public synchronized long printEvent(String tag, String arg, Object value) {

    return this.printEvent(tag, new String[] { arg }, new Object[] { value },
      null);
  }


  public synchronized void printElement(String tag, String[] argnames,
      Object[] argvalues,
            Object value) {

    if (this.out != null) {
      this.out.printElement(tag, argnames, argvalues, value);
    }
  }


  public synchronized long printEvent(String tag, String[] argnames,
      Object[] argvalues,
            Object value) {

    if (tag.equals("event") && (this.CanLog != null)) {
      this.CanLog.mark();
    }

    if (this.out != null) {
      long time = System.currentTimeMillis() - this.base;
      if (argnames != null) {
        String[] tmp = new String[argnames.length + 1];
        System.arraycopy(argnames, 0, tmp, 0, argnames.length);
        tmp[tmp.length - 1] = "time";
        argnames = tmp;
      }
      else {
        argnames = new String[] { "time" };
      }
      if (argvalues != null) {
        Object[] tmp = new Object[argvalues.length + 1];
        System.arraycopy(argvalues, 0, tmp, 0, argvalues.length);
        tmp[tmp.length - 1] = new Long(time);
        argvalues = tmp;
      }
      else {
        argvalues = new Object[] { new Long(time) };
      }

      this.out.printElement(tag, argnames, argvalues, value);

      return time;
    }
    else {
      return 0;
    }
  }


  public synchronized void openElement(String type) {

    this.openElement(type, null, null);
  }


  public synchronized void openElement(String type, String[] argnames,
      Object[] argvalues) {

    if (this.out != null) {
      long time = System.currentTimeMillis() - this.base;
      if (argnames != null) {
        String[] tmp = new String[argnames.length + 1];
        System.arraycopy(argnames, 0, tmp, 0, argnames.length);
        tmp[tmp.length - 1] = "time";
        argnames = tmp;
      }
      else {
        argnames = new String[] { "time" };
      }
      if (argvalues != null) {
        Object[] tmp = new Object[argvalues.length + 1];
        System.arraycopy(argvalues, 0, tmp, 0, argvalues.length);
        tmp[tmp.length - 1] = new Long(time);
        argvalues = tmp;
      }
      else {
        argvalues = new Object[] { new Long(time) };
      }

      this.out.openElement(type, argnames, argvalues);
    }
  }


  public synchronized void closeElement(String type) {

    if (this.out != null) {
      this.out.closeElement(type);
    }
  }


  private static final String getDate(Calendar now) {

    StringBuffer b = new StringBuffer(10);
    b.append(now.get(Calendar.YEAR));
    b.append('-');
    b.append(WizardLog.i2s(now.get(Calendar.MONTH) + 1, 2));
    b.append('-');
    b.append(WizardLog.i2s(now.get(Calendar.DAY_OF_MONTH), 2));
    return b.toString();
  }


  private static final String getTime(Calendar now, char delimiter) {

    StringBuffer b = new StringBuffer();
    b.append(now.get(Calendar.HOUR_OF_DAY));
    b.append(delimiter);
    b.append(WizardLog.i2s(now.get(Calendar.MINUTE), 2));
    b.append('h');
    return b.toString();
  }


  private static final char[] i2s(int i, int digits) {

    char[] cs = new char[digits];
    while (i > 0) {
      cs[--digits] = Character.forDigit((i % 10), 10);
      i = i / 10;
    }
    while (digits > 0) {
      cs[--digits] = '0';
    }
    return cs;
  }

  private static class SetupDialog
        extends JDialog
        implements Commander, Commands {

    private JTextField[] text;
    private JCheckBox canLog;

    // public PortAudioInput pa;
    // public Settings settings = null;
    public JavaSoundSetup jss;
    public String[] values = null;
    public boolean recordCAN = false;
    public BooleanProperty showSubdialogsDuringExecution =
      new DefaultBooleanProperty(
            "subdialog", Resources.getString("ExecuteInNewWindows"), null,
        false);


    public SetupDialog(Component parent, String[] infos, boolean CanLog,
        boolean reduced) {

      super(GUI.getFrameForComponent(parent), Resources.getString("LogSetup"),
        true);
      this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
      this.setResizable(false);
      /*
       * addWindowListener(new WindowAdapter() { public void
       * windowClosing(WindowEvent evt) { doCommand(cmdCancel); } });
       */
      this.jss = new JavaSoundSetup();

      Container c = this.getContentPane();
      c.setLayout(new GridBagLayout());
      GridBagConstraints gbc = new GridBagConstraints();

      gbc.anchor = GridBagConstraints.WEST;
      gbc.insets = new Insets(3, 3, 3, 3);
      gbc.gridy = 0;
      this.text = new JTextField[infos.length];
      for (int i = 0; i < infos.length; i++) {
        gbc.gridx = 0;
        c.add(new JLabel(infos[i] + ":"), gbc);
        gbc.gridx++;
        this.text[i] = new JTextField(25);
        c.add(this.text[i], gbc);
        gbc.gridy++;
      }

      gbc.gridx = 0;
      gbc.gridwidth = 2;

      if (CanLog) {
        this.canLog = new JCheckBox("Log CAN messages", true);
      }
      else {
        this.canLog = new JCheckBox(Resources.getString("CannotLogCAN"), false);
        this.canLog.setEnabled(false);
      }

      if (!reduced) {
        c.add(this.canLog, gbc);
      }

      gbc.gridy++;
      if (!reduced) {
        c.add(this.showSubdialogsDuringExecution.createEditor(true), gbc);
      }

      gbc.gridy++;
      if (!reduced) {
        c.add(this.jss, gbc);
      }

      Box buttons = Box.createHorizontalBox();

      buttons.add(Box.createHorizontalGlue());
      JButton bCancel =
        new CmdButton(this, Commands.cmdCancel, Resources.getString("NoLog"));
      buttons.add(bCancel);
      buttons.add(Box.createHorizontalStrut(8));
      JButton bStart =
        new CmdButton(this, Commands.cmdOK, Resources.getString("OK"));
      buttons.add(bStart);

      Dimension d1 = bCancel.getPreferredSize();
      Dimension d2 = bStart.getPreferredSize();

      bCancel.setPreferredSize(new Dimension(Math.max(d1.width, d2.width), Math
        .max(
                d1.height, d2.height)));
      bStart.setPreferredSize(bCancel.getPreferredSize());

      gbc.gridy++;
      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.insets = new Insets(6, 6, 6, 6);
      c.add(buttons, gbc);

      GUI.setDefaultButtons(this, bStart, bCancel);
      GUI.assignMnemonics(this.getContentPane());

      this.pack();
      this.setLocationRelativeTo(parent != null ? parent : this.getParent());
    }


    public boolean doCommand(int cmd) {

      switch (cmd) {
        case cmdCancel:
          this.jss = null;
          this.values = null;
          this.dispose();
          break;
        case cmdOK:
          this.values = new String[this.text.length];
          for (int i = 0; i < this.text.length; i++) {
            this.values[i] = this.text[i].getText();
          }
          this.recordCAN = this.canLog.isSelected();
          this.dispose();
          break;
        default:
          return false;
      }
      return true;
    }
  }
}