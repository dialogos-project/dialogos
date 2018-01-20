package com.clt.dialog.client;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.clt.event.ProgressListener;
import com.clt.gui.Commands;
import com.clt.gui.FileChooser;
import com.clt.gui.OptionPane;
import com.clt.gui.ProgressDialog;
import com.clt.gui.StaticText;
import com.clt.gui.menus.CmdMenu;
import com.clt.gui.menus.MenuCommander;
import com.clt.script.exp.Expression;
import com.clt.script.exp.Value;
import com.clt.util.DefaultLongAction;

/**
 * @author Daniel Bobbert
 * @version 7.0
 */

public abstract class GUIClient extends Client implements MenuCommander {

  private static final Collection<GUIClient> activeClients = new HashSet<GUIClient>();
  private static final Collection<GUIClient> allClients = new HashSet<GUIClient>();

  public static final String PROPERTY_PORT = "PORT";
  public static final String PROPERTY_STATE = "STATE";

  private CmdMenu menu = null;
  private Component ui = new JPanel();
  private ConnectionState state = ConnectionState.DISCONNECTED;

  private int port = -1;
  private boolean isPublic = true;

  protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);

  private final Object testLock = new Object();
  private boolean testRunning = false;

  private FileChooser fileChooser = null;
  private Collection<InternalConnection> internalConnections = new LinkedList<InternalConnection>();

  private Collection<ErrorHandler> errorHandlers = new LinkedList<ErrorHandler>();

  private ClassLoader classLoader = null;


  protected FileChooser getFileChooser() {

    if (this.fileChooser == null) {
      this.fileChooser = new FileChooser();
    }
    return this.fileChooser;
  }


  void setClassLoader(ClassLoader classLoader) {

    this.classLoader = classLoader;
  }


  protected ClassLoader getClassLoader() {

    if (this.classLoader != null) {
      return this.classLoader;
    }
    else {
      return this.getClass().getClassLoader();
    }
  }


  final public Component setupUI(CmdMenu modulesMenu) {

    this.ui = this.createUI();

    this.menu = new CmdMenu(this.getName(), Commands.cmdMenu, this) {

      @Override
      public JMenuItem add(JMenuItem item) {

        if (this.getItemCount() == 1) {
          this.addSeparator();
        }
        return super.add(item);
      }
    };
    modulesMenu.add(this.menu);

    JMenuItem item = new JMenuItem("Set Port...");
    item.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent evt) {

        String s =
          OptionPane.edit(GUIClient.this.getUI(), "Set Port",
            "Please enter a port for "
                        + GUIClient.this.getName() + ":", String
              .valueOf(GUIClient.this.getClientPort()));
        if (s != null) {
          try {
            final int port = Integer.parseInt(s);
            if (port >= 0) {
              if (port != GUIClient.this.getClientPort()) {
                if (GUIClient.this.getState() != ConnectionState.DISCONNECTED) {
                  new ProgressDialog(GUIClient.this.getUI())
                    .run(new DefaultLongAction(
                                        "Switching port")
                                    {

                                      @Override
                                      public void run(ProgressListener l) {

                                        try {
                                          GUIClient.this.close();
                                          GUIClient.this.setClientPort(port);
                                          GUIClient.this.open();
                                        }
                                            catch (Exception exn) {
                                              OptionPane.error(GUIClient.this
                                                .getUI(), exn);
                                            }
                                          }
                                    });
                }
                else {
                  GUIClient.this.setClientPort(port);
                }
              }
            }
            else {
              throw new NumberFormatException();
            }
          }
                    catch (Exception exn) {
                      OptionPane.error(GUIClient.this.ui,
                        "The port number must be a positiv integer.");
                    }
                  }
                }
    });
    // menu.add(item);

    item = new JMenuItem("Test input...");
    item.addActionListener(new ActionListener() {

      String testMessage = "";


      public void actionPerformed(ActionEvent evt) {

        String s =
          OptionPane.edit(GUIClient.this.getUI(),
            "Please enter the input value:", "Test input",
                    this.testMessage);
        if (s != null) {
          this.testMessage = s;
          synchronized (GUIClient.this.testLock) {
            GUIClient.this.testRunning = true;
            try {
              Value v = Expression.parseExpression(s).evaluate();
              GUIClient.this.output(v);
            }
                        catch (Exception exn) {
                          OptionPane.error(GUIClient.this.getUI(), exn);
                        }
                        GUIClient.this.testRunning = false;
                      }
                    }
                  }
    });
    this.menu.add(item);

    this.initMenu(this.menu);

    return this.ui;
  }


  /** Notification from the factory that all clients have been initialized */
  public void setupComplete(GUIClientWindow myWindow,
            Collection<? extends GUIClientWindow> allWindows) {

  }


  final public Component getUI() {

    return this.ui;
  }


  public boolean dispose() {

    this.close();

    synchronized (this.internalConnections) {
      for (InternalConnection ic : new ArrayList<InternalConnection>(
        this.internalConnections)) {
        ic.terminate();
      }
    }

    GUIClient.allClients.remove(this);

    return true;
  }


  @Override
  public void stateChanged(ConnectionState state) {

    if (this.state != state) {
      // System.out.println("state = " + state + ", port = " + getPort());
      ConnectionState oldState = this.state;
      this.state = state;
      this.pcs.firePropertyChange(GUIClient.PROPERTY_STATE, oldState, state);

      if (state == ConnectionState.CONNECTING) {
        GUIClient.activeClients.add(this);
      }
      else if (state == ConnectionState.DISCONNECTED) {
        GUIClient.activeClients.remove(this);
      }
    }
  }


  protected void firePropertyChange(String propertyName, Object oldValue,
      Object newValue) {

    this.pcs.firePropertyChange(propertyName, oldValue, newValue);
  }


  final public void addPropertyChangeListener(PropertyChangeListener l) {

    this.pcs.addPropertyChangeListener(l);
  }


  final public void removePropertyChangeListener(PropertyChangeListener l) {

    this.pcs.removePropertyChangeListener(l);
  }


  public boolean menuItemState(int cmd) {

    return true;
  }


  public String menuItemName(int cmd, String oldName) {

    return oldName;
  }


  public boolean doCommand(int cmd) {

    return false;
  }


  public void updateMenus() {

    if (this.menu != null) {
      this.menu.updateMenus();
    }
  }


  final public int getClientPort() {

    return this.port == -1 ? this.getPreferredPort() : this.port;
  }


  final public void setClientPort(int port) {

    if (this.port != port) {
      int oldPort = this.port;
      this.port = port;
      this.pcs.firePropertyChange(GUIClient.PROPERTY_PORT, oldPort, port);
    }
  }


  @Override
  public void open()
      throws IOException {

    try {
      this.open(this.getClientPort());
    } catch (IOException exn) {
      super.open();
    }
  }


  @Override
  void sendImpl(Object o)
      throws IOException {

    synchronized (this.testLock) {
      if (this.testRunning) {
        if (o instanceof Value) {
          StringWriter sw = new StringWriter();
          PrintWriter w = new PrintWriter(sw);
          ((Value)o).prettyPrint(w);
          w.flush();
          o = sw.toString();
        }

        OptionPane.message(this.getUI(), new JScrollPane(new StaticText(o
          .toString())) {

          @Override
          public Dimension getPreferredSize() {

            return new Dimension(400, 300);
          }
        });
      }
      else {
        synchronized (this.internalConnections) {
          if (this.internalConnections.size() > 0) {
            for (InternalConnection c : this.internalConnections) {
              c.send(o);
            }
          }
          else {
            super.sendImpl(o);
          }
        }
      }
    }
  }


  @Override
  public void sendTimeout()
      throws IOException {

    synchronized (this.internalConnections) {
      if (this.internalConnections.size() > 0) {
        for (InternalConnection c : this.internalConnections) {
          c.sendTimeout();
        }
      }
      else {
        super.sendTimeout();
      }
    }
  }


  @Override
  public void log(String message)
      throws IOException {

    synchronized (this.internalConnections) {
      if (this.internalConnections.size() > 0) {
        for (InternalConnection c : this.internalConnections) {
          c.log(message);
        }
      }
      else {
        super.log(message);
      }
    }
  }


  public abstract int getPreferredPort();


  public abstract String getDescription();


  public abstract String getVersion();


  public abstract Map<String, String> getAcceptedInput();


  public final boolean initClient(ProgressListener l, String args[],
      Properties properties) {

    boolean initialized = this.initialize(l, args, properties);

    if (properties.getProperty("Port") != null) {
      try {
        this.setClientPort(Integer.parseInt(properties.getProperty("Port")));
      } catch (Exception exn) {
        this.error(new IllegalArgumentException(
                    "Error while reading property 'Port' from Clients.ini: "
                      + exn.toString()));
      }
    }

    if (properties.getProperty("Public") != null) {
      try {
        this.isPublic =
          new Boolean(properties.getProperty("Public")).booleanValue();
      } catch (Exception exn) {
        this.error(new IllegalArgumentException(
                    "Error while reading property 'Public' from Clients.ini: "
                      + exn.toString()));
      }
    }

    if (properties.getProperty("Private") != null) {
      try {
        this.isPublic =
          !new Boolean(properties.getProperty("Private")).booleanValue();
      } catch (Exception exn) {
        this.error(new IllegalArgumentException(
                    "Error while reading property 'Private' from Clients.ini: "
                      + exn.toString()));
      }
    }

    if (properties.getProperty("Priority") != null) {
      try {
        int priority =
          new Integer(properties.getProperty("Priority")).intValue();
        if (priority < Thread.MIN_PRIORITY) {
          priority = Thread.MIN_PRIORITY;
        }
        if (priority > Thread.MAX_PRIORITY) {
          priority = Thread.MAX_PRIORITY;
        }
        this.setPriority(priority);
      } catch (Exception exn) {
        this
          .error(new IllegalArgumentException(
                    "Error while reading property 'Priority' from Clients.ini: "
                      + exn.toString()));
      }
    }

    if (initialized) {
      GUIClient.allClients.add(this);
    }

    return initialized;
  }


  protected abstract boolean initialize(ProgressListener l, String args[],
      Properties properties);


  protected abstract Component createUI();


  protected void initMenu(CmdMenu menu) {

  }


  /**
   * Notification that an internal error has occurred while receiving data from
   * the client. The default implementation notifies all registered
   * ErrorHandlers.
   * 
   * @see #addErrorHandler
   */
  @Override
  public void error(Throwable t) {

    synchronized (this.errorHandlers) {
      for (ErrorHandler handler : this.errorHandlers) {
        handler.handleError(t);
      }
    }
  }


  public boolean systemTask() {

    return false;
  }


  @Override
  public boolean isPublic() {

    return this.isPublic;
  }


  protected static void playSound()
      throws Exception {

    GUIClient.playSound(new File("Sound.wav"));
  }

  private static Clip clip = null;


  protected static void playSound(File file)
      throws Exception {

    if (GUIClient.clip == null) {
      AudioInputStream stream;
      try {
        stream = AudioSystem.getAudioInputStream(file);
      } catch (Exception exn) {
        DataInputStream in = new DataInputStream(
                    ClassLoader.getSystemResourceAsStream(file.getName()));
        byte[] data = new byte[in.available()];
        in.readFully(data);
        in.close();
        stream =
          AudioSystem.getAudioInputStream(new ByteArrayInputStream(data));
      }
      AudioFormat format = stream.getFormat();
      DataLine.Info info = new DataLine.Info(Clip.class, format);

      GUIClient.clip = (Clip)AudioSystem.getLine(info);
      GUIClient.clip.open(stream);
    }

    GUIClient.clip.setFramePosition(0);
    GUIClient.clip.start();
  }


  @Override
  public ConnectionState getState() {

    if (this.internalConnections.size() > 0) {
      return ConnectionState.CONNECTED;
    }
    else {
      return super.getState();
    }
  }


  public void addErrorHandler(ErrorHandler handler) {

    synchronized (this.errorHandlers) {
      this.errorHandlers.add(handler);
    }
  }


  public void removeErrorHandler(ErrorHandler handler) {

    synchronized (this.errorHandlers) {
      this.errorHandlers.remove(handler);
    }
  }


  public void addInternalConnection(InternalConnection c)
      throws IOException {

    synchronized (this.internalConnections) {
      if (this.getState() != ConnectionState.CONNECTING) {
        throw new IOException("Client not ready to connect");
      }
      // stop responding to external connections
      this.close();
      this.internalConnections.add(c);
      this.stateChanged(this.getState());
    }
  }


  public void removeInternalConnection(InternalConnection c)
      throws IOException {

    synchronized (this.internalConnections) {
      this.internalConnections.remove(c);
      this.stateChanged(this.getState());
      if (this.internalConnections.isEmpty()) {
        // start responding to external connections
        this.open();
      }
    }
  }


  public static Collection<GUIClient> getActiveClients() {

    return Collections.unmodifiableCollection(GUIClient.activeClients);
  }

  public static interface InternalConnection {

    public void send(Object o);


    public void sendTimeout();


    public void log(String message);


    public void terminate();
  }
}