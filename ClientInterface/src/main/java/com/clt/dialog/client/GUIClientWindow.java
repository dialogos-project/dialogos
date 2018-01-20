package com.clt.dialog.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import com.clt.gui.AboutDialog;
import com.clt.gui.Commands;
import com.clt.gui.GUI;
import com.clt.gui.OptionPane;
import com.clt.gui.StaticText;
import com.clt.gui.menus.CmdCheckBoxMenuItem;
import com.clt.gui.menus.CmdMenu;
import com.clt.gui.menus.CmdMenuBar;
import com.clt.gui.menus.MenuCommander;
import com.clt.mac.ApplicationUtils;
import com.clt.mac.RequiredEventHandler;
import com.clt.script.exp.Value;
import com.clt.script.exp.values.BoolValue;
import com.clt.script.exp.values.StructValue;
import com.clt.util.Platform;

/**
 * @author dabo
 * 
 */
public class GUIClientWindow extends JFrame implements MenuCommander {

  private static final String DEFAULT_TITLE = "Dialog Client";

  private static final int cmdPlaySound = Commands.cmdApplication + 1;
  private static final int cmdUseTabbedPane = Commands.cmdApplication + 2;
  private static final int cmdPTT = Commands.cmdApplication + 3;
  private static final int cmdPanic = Commands.cmdApplication + 4;
  private static final int cmdBargeIn = Commands.cmdApplication + 5;

  private boolean useTabbedPane = true;
  private List<GUIClient> clients = new ArrayList<GUIClient>();
  private List<Component> clientUIs = new ArrayList<Component>();

  private Runnable quitHandler;
  private boolean disposeOnClose = true;

  private Point preferredLocation = null;


  public GUIClientWindow(String title) {

    super(title != null ? title : GUIClientWindow.DEFAULT_TITLE);

    this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    this.addWindowListener(new WindowAdapter() {

      @Override
      public void windowClosing(WindowEvent e) {

        GUIClientWindow.this.doCommand(Commands.cmdClose);
      }
    });
  }


  public void setDisposeOnClose(boolean disposeOnClose) {

    this.disposeOnClose = disposeOnClose;
  }


  public void setQuitHandler(Runnable quitHandler) {

    this.quitHandler = quitHandler;
  }


  protected void addClient(GUIClient client) {

    this.clients.add(client);
  }


  public Collection<GUIClient> getClients() {

    return Collections.unmodifiableCollection(this.clients);
  }


  public boolean menuItemState(int cmd) {

    switch (cmd) {
      case cmdUseTabbedPane:
        return this.useTabbedPane;

      default:
        return true;
    }
  }


  public String menuItemName(int cmd, String oldName) {

    return oldName;
  }


  public boolean dispose(boolean force) {

    boolean closed = true;
    for (Iterator<GUIClient> it = this.clients.iterator(); it.hasNext();) {
      try {
        if (!it.next().dispose()) {
          closed = false;
        }
      } catch (Exception exn) {
        OptionPane.error(this, exn);
        closed = false;
      }
    }
    if (!closed) {
      if (force) {
        closed = true;
      }
      else {
        closed =
          OptionPane
            .confirm(
              this,
                  "Some clients failed to clean up properly. Do you still want to close?",
                  "Really close?", OptionPane.OK_CANCEL_OPTION) == OptionPane.OK;
      }
    }
    if (closed) {
      this.dispose();
    }
    return closed;
  }


  public boolean doCommand(int cmd) {

    switch (cmd) {
      case cmdPlaySound:
        try {
          GUIClient.playSound();
        } catch (Exception exn) {
          OptionPane.error(this, exn);
        }
        break;

      case cmdPTT:
        this.send(new StructValue(new String[] { "PTT" },
          new Value[] { new BoolValue(true) }));
        break;

      case cmdBargeIn:
        this.send(new StructValue(new String[] { "bargein" },
                    new Value[] { new BoolValue(true) }));
        break;

      case cmdPanic:
        this.send(new StructValue(new String[] { "panic" },
          new Value[] { new BoolValue(true) }));
        break;

      case cmdUseTabbedPane:
        this.useTabbedPane = !this.useTabbedPane;
        this.layoutUI();
        this.repaint();
        break;

      case cmdAbout:
        StringBuilder b = new StringBuilder();
        for (Iterator<GUIClient> it = this.clients.iterator(); it.hasNext();) {
          b.append(it.next().getName());
          b.append('\n');
        }
        String credits =
          "<b>design & implementation\nDaniel Bobbert\nDaniel Beck\n\n"
                        + "<b>active modules\n" + b.toString();
        new AboutDialog("DialogClient", Device.VERSION, "\u00A9 CLT Sprachtechnologie GmbH", credits).show(this);
        break;

      case cmdClose:
        if (this.disposeOnClose) {
          this.dispose(false);
        }
        else {
          this.setVisible(false);
        }
        break;

      case cmdQuit:
        if (this.quitHandler != null) {
          this.quitHandler.run();
        }
        break;

      default:
        return false;
    }
    this.updateMenus();
    return true;
  }


  public void updateMenus() {

    JMenuBar mbar = this.getJMenuBar();
    if (mbar instanceof CmdMenuBar) {
      ((CmdMenuBar)mbar).updateMenus();
    }

    for (GUIClient client : this.clients) {
      client.updateMenus();
    }
  }


  public void connect() {

    for (final GUIClient client : this.clients) {
      try {
        client.open();
      } catch (ThreadDeath d) {
        throw d;
      } catch (final Throwable exn) {
        exn.printStackTrace();
        client.error(exn);
        GUI.invokeAndWait(new Runnable() {

          public void run() {

            OptionPane.error(GUIClientWindow.this, new String[] {
                                "The network connection for client \""
                                  + client.getName()
                                        + "\" could not be started.",
              exn.toString() });
          }
        });
      }
    }

    this.updateMenus();
  }


  private boolean send(Value v) {

    boolean ok = false;
    for (Iterator<GUIClient> it = this.clients.iterator(); !ok && it.hasNext();) {
      Client c = it.next();
      try {
        c.send(v);
        ok = true;
      } catch (Exception ignore) {
      }
    }
    return ok;
  }


  private CmdMenuBar initMenuBar() {

    CmdMenuBar mbar = new CmdMenuBar(this);

    CmdMenu m = mbar.addMenu("File");
    m.addItem("Play Sound", GUIClientWindow.cmdPlaySound, KeyStroke
      .getKeyStroke(KeyEvent.VK_F1, 0));
    m.addItem("Panic", GUIClientWindow.cmdPanic, KeyStroke.getKeyStroke(
      KeyEvent.VK_F2, 0));
    m.addItem("Barge In", GUIClientWindow.cmdBargeIn, KeyStroke.getKeyStroke(
      KeyEvent.VK_F3, 0));
    m.addItem("PTT", GUIClientWindow.cmdPTT, KeyStroke.getKeyStroke(
      KeyEvent.VK_F12, 0));
    m.addItem("PTT BMW", GUIClientWindow.cmdPTT, KeyStroke.getKeyStroke(
      KeyEvent.VK_NUMPAD2, 0));
    if (this.clients.size() > 1) {
      m.addSeparator();
      m.add(new CmdCheckBoxMenuItem("Use tabbed interface",
        GUIClientWindow.cmdUseTabbedPane, null, this));
    }
    this.addMenuOptions(m);
    m.addSeparator();
    m.addItem("Close", Commands.cmdClose, KeyEvent.VK_W);
    if (Platform.showQuitMenuItem()) {
      m.addItem("Quit", Commands.cmdQuit, KeyEvent.VK_Q);
    }

    this.setJMenuBar(mbar);

    ApplicationUtils
      .registerEventHandler(new RequiredEventHandler(true, false) {

        @Override
        public boolean handleAbout() {

          return GUIClientWindow.this.doCommand(Commands.cmdAbout);
        }


        @Override
        public boolean handleQuit() {

          return GUIClientWindow.this.doCommand(Commands.cmdQuit);
        }
      });

    return mbar;
  }


  protected void addMenuOptions(CmdMenu fileMenu) {

  }


  protected void initUI() {

    if (this.clients.size() <= 2) {
      this.useTabbedPane = false;
    }

    CmdMenuBar mbar = this.initMenuBar();

    if (this.clients.size() == 0) {
      JLabel l = new JLabel("No client modules found", SwingConstants.CENTER);
      l.setPreferredSize(new Dimension(300, 200));
      this.getContentPane().add(l);

      this.setTitle(GUIClientWindow.DEFAULT_TITLE);
    }
    else {
      CmdMenu moduleMenu = mbar.addMenu("Modules");

      StringBuilder title = new StringBuilder();
      for (Iterator<GUIClient> it = this.clients.iterator(); it.hasNext();) {
        GUIClient client = it.next();

        try {
          this.clientUIs.add(client.setupUI(moduleMenu));
        } catch (ThreadDeath d) {
          throw d;
        } catch (Throwable exn) {
          exn.printStackTrace();
          this.clientUIs.add(new StaticText("Failed to create UI for "
            + client.getName()
                            + ":\n" + exn));
        }
        title.append(client.getName());
        if (it.hasNext()) {
          title.append('/');
        }
      }
      this.setTitle(title.toString());

      if (moduleMenu.getItemCount() == 1) {
        JMenu m = (JMenu)moduleMenu.getItem(0);
        moduleMenu.remove(0);
        mbar.remove(moduleMenu);
        mbar.add(m);
      }

      this.layoutUI();
    }

    CmdMenu help = mbar.addMenu("Help");
    if (!Platform.isMac()) {
      help.addItem("About...", Commands.cmdAbout);
      help.addSeparator();
    }
    for (final GUIClient client : this.clients) {
      JMenuItem item = new JMenuItem(client.getName());
      item.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent evt) {

          try {
            SimpleAttributeSet bold = new SimpleAttributeSet();
            StyleConstants.setBold(bold, true);

            String description = client.getDescription();
            Map<String, String> inputs = client.getAcceptedInput();

            JTextPane p = new JTextPane();
            p.setEditable(false);
            p.setEnabled(false);
            p.setDisabledTextColor(Color.black);
            Document d = p.getDocument();
            d.insertString(0, description, null);
            d
              .insertString(d.getLength(),
                            "\n\nThe client accepts the following input:", null);

            for (String pattern : inputs.keySet()) {
              String desc = inputs.get(pattern);

              d.insertString(d.getLength(), "\n\n" + pattern + "\n", bold);
              d.insertString(d.getLength(), desc, null);
            }
            p.setCaretPosition(0);
            JScrollPane jsp = GUI.createScrollPane(p,
                            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            jsp.setPreferredSize(new Dimension(400, 250));
            jsp.setBorder(BorderFactory.createEmptyBorder());

            OptionPane.message(GUIClientWindow.this, new Object[] { jsp },
                            client.getName() + " v" + client.getVersion(),
              OptionPane.INFORMATION);
          }
                    catch (ThreadDeath d) {
                      throw d;
                    }
                    catch (Throwable exn) {
                      OptionPane.error(GUIClientWindow.this, exn);
                    }

                  }
      });
      help.add(item);
    }

    /*
     * help.addSeparator(); JMenuItem item = new
     * JMenuItem("Show active threads"); item.addActionListener(new
     * ActionListener() { public void actionPerformed(ActionEvent e) {
     * Misc.dumpThreads(); } }); help.add(item);
     */
  }


  protected void layoutUI() {

    JPanel p = new JPanel();

    if (this.useTabbedPane) {
      p.setLayout(new BorderLayout());
      JTabbedPane tp = GUI.createTabbedPane();
      tp.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
      for (int i = 0; i < this.clients.size(); i++) {
        tp.addTab(this.clients.get(i).getName(), this.clientUIs.get(i));
      }
      p.add(tp, BorderLayout.CENTER);
    }
    else {
      p.setLayout(new GridBagLayout());
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.gridx = gbc.gridy = 0;
      gbc.weighty = 1.0;
      gbc.fill = GridBagConstraints.BOTH;

      for (int i = 0; i < this.clientUIs.size(); i++) {
        if (i > 0) {
          gbc.weightx = 0.0;
          p.add(new JSeparator(SwingConstants.VERTICAL), gbc);
          gbc.gridx++;
        }
        gbc.weightx = 1.0;
        p.add(this.clientUIs.get(i), gbc);
        gbc.gridx++;
      }
    }

    this.setContentPane(p);
    this.setSize(this.getPreferredSize());
    this.validate();
  }


  @Override
  public Dimension getPreferredSize() {

    Dimension d = super.getPreferredSize();

    Dimension size = this.getToolkit().getScreenSize();
    size.width = Math.min(size.width, d.width);
    size.height = Math.min(size.height - 30, d.height);

    return size;
  }


  public Point getPreferredLocation() {

    return this.preferredLocation;
  }


  public void setPreferredLocation(Point preferredLocation) {

    this.preferredLocation = preferredLocation;
  }

}
