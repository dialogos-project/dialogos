package com.clt.diamant.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.MouseInputListener;

import com.clt.dialog.client.DeviceEvent;
import com.clt.dialog.client.DeviceListener;
import com.clt.dialog.client.ServerDevice;
import com.clt.diamant.Device;
import com.clt.diamant.DialogInput;
import com.clt.diamant.Document;
import com.clt.diamant.Executer;
import com.clt.diamant.ExecutionStoppedException;
import com.clt.diamant.ForcedTimeout;
import com.clt.diamant.IdMap;
import com.clt.diamant.InputCenter;
import com.clt.diamant.Resources;
import com.clt.diamant.SingleDocument;
import com.clt.diamant.Version;
import com.clt.diamant.WozInterface;
import com.clt.diamant.graph.Graph;
import com.clt.diamant.graph.GraphExecutionListener;
import com.clt.diamant.graph.Node;
import com.clt.diamant.graph.nodes.OwnerNode;
import com.clt.diamant.graph.nodes.ProcNode;
import com.clt.diamant.log.WizardLog;
import com.clt.gui.CmdButton;
import com.clt.gui.GUI;
import com.clt.gui.Images;
import com.clt.gui.MouseClickAdapter;
import com.clt.gui.OptionPane;
import com.clt.gui.WindowUtils;
import com.clt.gui.border.GroupBorder;
import com.clt.gui.menus.CmdMenu;
import com.clt.gui.menus.CmdMenuBar;
import com.clt.gui.menus.MenuCommander;
import com.clt.script.exp.Pattern;
import com.clt.script.exp.Value;
import com.clt.util.Timeout;
import com.clt.util.UserCanceledException;

class WizardUI extends JDialog implements WozInterface {

    public static final int 
            TIMEOUT_IGNORE = 0, 
            TIMEOUT_SIGNAL = 1,
            TIMEOUT_FORCE = 2;

    private static final boolean USE_KEYBOARD_SHORTCUTS = true;
    private static final boolean DISPLAY_KEYBOARD_SHORTCUTS = false;

    private final Object startLock = new Object();

    private transient Object result = null;
    private transient Pattern[] alternatives = new Pattern[0];

    private Box options;
    private JButton startButton;
    private WizardLog log;
    private IdMap uid_map;
    private Stack<Long> subgraph_times;
    private JPanel deviceView;
    private JPanel stateView;
    private Map<Device, JTextField> outputMap;
    private Map<Device, JTextField> inputMap;
    private Map<Device, JLabel> labelMap;
    private Collection<GraphExecutionListener> graphExecutionListeners;
    private long delay;

    private VarList variables = new VarList();

    private Device wizardDevice = new Device("Wizard", null);

    private static String ABORT = "stop";

    private Executer executer;
    private int timeoutMode;
    private boolean allowInteraction;
    private JLabel timeoutLabel = null;
    private JButton timeoutButton = null;
    private DeviceListener deviceListener;

    public WizardUI(Frame parent, boolean allowInteraction) {

        super(parent, Version.PRODUCT_NAME + " Wizard", false);
        // super(Version.PRODUCT_NAME + " Wizard");
        // setAlwaysOnTop(true);

        this.allowInteraction = allowInteraction;
        if (allowInteraction) {
            this.timeoutMode = WizardUI.TIMEOUT_SIGNAL;
        } else {
            this.timeoutMode = WizardUI.TIMEOUT_FORCE;
        }

        this.graphExecutionListeners = new ArrayList<GraphExecutionListener>();
        this.delay = 0;
        this.executer = new Executer(this, false);

        this.deviceListener = new DeviceListener() {

            public void stateChanged(DeviceEvent evt) {

                JComponent label = WizardUI.this.labelMap.get(evt.getSource());
                if (label != null) {
                    switch (evt.getState()) {
                        case CONNECTED:
                            label.setForeground(Color.green);
                            break;
                        case CONNECTING:
                            label.setForeground(Color.orange);
                            break;
                        default:
                            label.setForeground(Color.red);
                            break;
                    }
                }
            }

            public void dataSent(DeviceEvent evt) {

            }

            public void dataReceived(DeviceEvent evt) {

                JTextField t = WizardUI.this.inputMap.get(evt.getSource());
                if (t != null) {
                    t.setText(evt.getData().toString());
                }
                if (WizardUI.this.log != null) {
                    WizardUI.this.log.printEvent("input", new String[]{"name"},
                            new Object[]{((ServerDevice) evt.getSource())
                                        .getName()}, evt.getData());
                }
            }

            public void dataLogged(DeviceEvent evt) {

                if (WizardUI.this.log != null) {
                    WizardUI.this.log.printEvent("input", new String[]{"name", "log"},
                            new Object[]{
                                ((ServerDevice) evt.getSource()).getName(), "1"},
                            evt.getData());
                }
            }
        };

        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent evt) {

                if (WizardUI.this.startButton.isEnabled()) {
                    // der Start-Button ist aktiviert, d.h. es laeuft noch nichts
                    // so lange koennen wir einfach aufhoeren
                    synchronized (WizardUI.this.startLock) {
                        WizardUI.this.startLock.notifyAll();
                    }
                }
                // ansonsten ignorieren wir den Klick
            }
        });

        JPanel content = GUI.initContentPane(this);
        content.setLayout(new GridBagLayout());

        JToolBar buttons = new JToolBar();
        buttons.setFloatable(false);

        this.startButton
                = this.createButton("Start", Images.load("wizard/WizardStart.png"), false);
        buttons.add(this.startButton);
        JButton stopButton
                = this.createButton("Stop", Images.load("wizard/WizardStop.png"), true);
        stopButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                WizardUI.this.abort();
            }
        });
        buttons.add(stopButton);
        this.timeoutButton
                = this.createButton("TimeoutExceeded", Images
                        .load("wizard/WizardTimeout.png"),
                        false);
        this.timeoutButton.setEnabled(false);
        this.timeoutButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                WizardUI.this.postResult(new Timeout());
            }
        });
        buttons.add(this.timeoutButton);
        buttons.add(Box.createHorizontalStrut(20));
        // buttons.addSeparator();
        // buttons.add(createButton("Barge in", GUI.loadImage("wizard/BargeIn.gif"),
        // true));
        buttons.add(this.createButton("Warning", Images
                .load("wizard/WizardWarning.png"), true));

        this.options = Box.createVerticalBox();

        JScrollPane options = new JScrollPane(this.options);
        options.getVerticalScrollBar().setUnitIncrement(20);
        options.setPreferredSize(new Dimension(300, 400));
        options.setBorder(new GroupBorder("Transitions"));

        this.inputMap = new HashMap<Device, JTextField>();
        this.outputMap = new HashMap<Device, JTextField>();
        this.labelMap = new HashMap<Device, JLabel>();
        this.deviceView = new JPanel(new GridLayout());
        this.deviceView.setBorder(new GroupBorder(Resources.getString("Devices")));
        this.deviceView.setPreferredSize(new Dimension(300, 200));

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.gridwidth = 2;

        content.add(buttons, gbc);

        gbc.gridy++;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = 1;
        gbc.gridheight = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        content.add(options, gbc);

        gbc.gridx++;
        gbc.gridheight = 1;
        content.add(this.deviceView, gbc);

        gbc.gridy++;
        this.stateView = new JPanel(new GridLayout());
        this.stateView.setBorder(new GroupBorder(Resources.getString("State")));
        this.stateView.setPreferredSize(new Dimension(300, 200));
        content.add(this.stateView, gbc);

        final int baseCmd = 1000;
        if (WizardUI.USE_KEYBOARD_SHORTCUTS) {
            final CmdMenuBar mbar = new CmdMenuBar(new MenuCommander() {

                public boolean menuItemState(int cmd) {

                    return true;
                }

                public String menuItemName(int cmd, String oldName) {

                    return oldName;
                }

                public boolean doCommand(int cmd) {

                    if (cmd == baseCmd) {
                        if (WizardUI.this.timeoutLabel != null) {
                            WizardUI.this.postResult(new Timeout());
                        }
                    } else if ((cmd >= (baseCmd + 1)) && (cmd <= (baseCmd + 9))) {
                        if (WizardUI.this.alternatives.length >= (cmd - baseCmd)) {
                            WizardUI.this.postResult(WizardUI.this.alternatives[cmd
                                    - (baseCmd + 1)]);
                        }

                        return true;
                    }
                    return false;
                }
            });

            CmdMenu m = mbar.addMenu("");
            m.addItem("Timeout", baseCmd, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,
                    0));
            m.addItem("1", baseCmd + 1, KeyStroke.getKeyStroke(KeyEvent.VK_1, 0));
            m.addItem("1", baseCmd + 1, KeyStroke
                    .getKeyStroke(KeyEvent.VK_NUMPAD1, 0));
            m.addItem("2", baseCmd + 2, KeyStroke.getKeyStroke(KeyEvent.VK_2, 0));
            m.addItem("2", baseCmd + 2, KeyStroke
                    .getKeyStroke(KeyEvent.VK_NUMPAD2, 0));
            m.addItem("3", baseCmd + 3, KeyStroke.getKeyStroke(KeyEvent.VK_3, 0));
            m.addItem("3", baseCmd + 3, KeyStroke
                    .getKeyStroke(KeyEvent.VK_NUMPAD3, 0));
            m.addItem("4", baseCmd + 4, KeyStroke.getKeyStroke(KeyEvent.VK_4, 0));
            m.addItem("4", baseCmd + 4, KeyStroke
                    .getKeyStroke(KeyEvent.VK_NUMPAD4, 0));
            m.addItem("5", baseCmd + 5, KeyStroke.getKeyStroke(KeyEvent.VK_5, 0));
            m.addItem("5", baseCmd + 5, KeyStroke
                    .getKeyStroke(KeyEvent.VK_NUMPAD5, 0));
            m.addItem("6", baseCmd + 6, KeyStroke.getKeyStroke(KeyEvent.VK_6, 0));
            m.addItem("6", baseCmd + 6, KeyStroke
                    .getKeyStroke(KeyEvent.VK_NUMPAD6, 0));
            m.addItem("7", baseCmd + 7, KeyStroke.getKeyStroke(KeyEvent.VK_7, 0));
            m.addItem("7", baseCmd + 7, KeyStroke
                    .getKeyStroke(KeyEvent.VK_NUMPAD7, 0));
            m.addItem("8", baseCmd + 8, KeyStroke.getKeyStroke(KeyEvent.VK_8, 0));
            m.addItem("8", baseCmd + 8, KeyStroke
                    .getKeyStroke(KeyEvent.VK_NUMPAD8, 0));
            m.addItem("9", baseCmd + 9, KeyStroke.getKeyStroke(KeyEvent.VK_9, 0));
            m.addItem("9", baseCmd + 9, KeyStroke
                    .getKeyStroke(KeyEvent.VK_NUMPAD9, 0));
            mbar.updateMenus();

            this.getLayeredPane().add(mbar);
        }

        GUI.assignMnemonics(this.getContentPane());
        this.pack();

        this.setLocation(WindowUtils.getScreenSize().width - this.getSize().width - 20, 20);
        // WindowUtils.setLocationRelativeTo(this, parent);
    }

    private JButton createButton(final String label, final Icon icon,
            final boolean includeInLog) {

        JButton button = new CmdButton(Resources.getString(label), null);
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setVerticalAlignment(SwingConstants.BOTTOM);
        button.setVerticalTextPosition(SwingConstants.BOTTOM);

        if (icon != null) {
            button.setIcon(icon);
            button.setDisabledIcon(Images.blend(icon, Color.WHITE, 0.6f));
            button.setPressedIcon(Images.adjustBrightness(icon, 0.7f));
        }
        button.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);

        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                if ((WizardUI.this.log != null) && includeInLog) {
                    WizardUI.this.log.printEvent("event", "type", label);
                }
            }
        });
        return button;
    }

    public void setState(State state) {

        if (this.executer != null) {
            this.executer.setState(state);
        }

        if (this.log != null) {
            this.log.setState(state);
        }

        if ((state == State.INSTRUCT) && this.allowInteraction) {
            OptionPane
                    .message(this, new String[]{Resources.getString("InstructUser"),
                Resources.format("ClickToStart", Resources.getString("OK"))});
        }
    }

    public boolean initInterface() {

        if (!this.executer.initInterface()) {
            return false;
        }

        try {
            this.log = new WizardLog(this.getParent());
        } catch (UserCanceledException exn) {
            this.log = null;
        }

        if (this.allowInteraction) {
            this.setVisible(true);

            ActionListener l = new ActionListener() {

                public void actionPerformed(ActionEvent e) {

                    synchronized (WizardUI.this.startLock) {
                        WizardUI.this.startButton.setEnabled(false);
                        WizardUI.this.startLock.notifyAll();
                    }
                }
            };

            this.startButton.addActionListener(l);

            try {
                synchronized (this.startLock) {
                    this.startLock.wait();
                }
            } catch (Exception exn) {
            }

            this.startButton.removeActionListener(l);

            if (this.startButton.isEnabled()) {
                // startButton immer noch aktiv, Wizard hat also abgebrochen
                throw new ExecutionStoppedException();
            }
        } else {
            this.startButton.setEnabled(false);
        }

        if (this.log != null) {
            try {
                this.log.startRecording();
            } catch (Exception exn) {
                exn.printStackTrace();
                OptionPane
                        .error(
                                this,
                                new String[]{
                                    "An error occurred while configuring the audio system. No audio data will be recorded.",
                                    exn.toString()});
            }
        }

        return true;
    }

    public boolean isDebugger() {

        return true;
    }

    public boolean showSubdialogsDuringExecution() {

        return this.log != null ? this.log.showSubdialogsDuringExecution() : false;
    }

    public void startDocument(Document d, String name, final InputCenter input) {

        this.executer.startDocument(d, name, input);

        input.clear(null);

        if (d instanceof SingleDocument) {
            SingleDocument sd = (SingleDocument) d;
            this.initDeviceView(sd.getDevices());
            this.variables.push(sd, sd.getOwnedGraph().getAllVariables(Graph.LOCAL));
        }
        this.stateView.removeAll();
        this.stateView.add(this.createVariableDisplay());
        this.stateView.validate();

        input.addDeviceListener(this.deviceListener);

        if (this.log != null) {
            try {
                this.log.setFile(name);
            } catch (IOException exn) {
                throw new RuntimeException(exn.getLocalizedMessage());
            }
            this.uid_map = this.log.start(d);
            this.subgraph_times = new Stack<Long>();
        }

    }

    private void initDeviceView(Collection<Device> devices) {

        JPanel p = new JPanel(new GridBagLayout());
        this.inputMap.clear();
        this.outputMap.clear();
        this.labelMap.clear();
        this.deviceView.removeAll();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.WEST;

        int n = 0;
        for (Device d : devices) {
            if (n++ > 0) {
                gbc.gridwidth = 2;
                gbc.insets = new Insets(0, 0, 0, 0);
                p.add(new JSeparator(SwingConstants.HORIZONTAL), gbc);
                gbc.gridy++;
            }
            gbc.gridwidth = 1;
            gbc.gridheight = 2;
            gbc.weightx = 0.0;
            gbc.insets = new Insets(2, 4, 2, 4);
            JLabel name
                    = new JLabel(
                            d.getName().length() > 10
                            ? (d.getName().substring(0, 8) + "...") : d.getName());
            p.add(name, gbc);
            this.labelMap.put(d, name);
            gbc.gridx++;
            gbc.gridheight = 1;
            gbc.weightx = 1.0;
            JTextField in = new JTextField();
            WizardUI.setupTextField(in);
            this.inputMap.put(d, in);
            JTextField out = new JTextField();
            WizardUI.setupTextField(out);
            this.outputMap.put(d, out);

            p.add(out, gbc);
            gbc.gridy++;
            p.add(in, gbc);
            gbc.gridy++;
            gbc.gridx = 0;
        }

        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridwidth = 2;
        p.add(Box.createVerticalGlue(), gbc);

        this.deviceView.add(GUI.createScrollPane(p,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER));

        this.deviceView.validate();
        this.deviceView.repaint();
    }

    private static void setupTextField(JTextField tf) {

        tf.setFont(new Font("SansSerif", Font.PLAIN, 10));
        tf.setEditable(false);
    }

    public void endDocument(Document d) {

        this.executer.endDocument(d);

        if (d instanceof SingleDocument) {
            this.variables.pop();
        }

        if (this.log != null) {
            if (this.allowInteraction) {
                OptionPane.message(this, new String[]{
                    Resources.getString("ExecutionStopped"),
                    Resources.getString("LoggingUntilClose")});
            }

            this.log.end();
            this.uid_map = null;
            this.subgraph_times = null;
        }
    }

    public void disposeInterface(boolean error) {

        this.executer.disposeInterface(error);

        if (this.log != null) {
            this.log.stopRecording();
            this.log = null;
        }

        this.setVisible(false);
        this.dispose();

        this.variables.clear();
    }

    public void output(Device d, Value value) {

        this.executer.output(d, value);

        if (value != null) {
            JTextField t = this.outputMap.get(d);
            if (t != null) {
                t.setText(value.toString());
            }
            if (this.log != null) {
                this.log.printEvent("prompt", new String[]{"name"}, new Object[]{d
                    .getName()},
                        value.toString());
            }
        }
    }

    public void abort() {

        this.executer.abort();

        if (this.startButton.isEnabled()) {// wir haben noch gar nicht angefangen
            synchronized (this.startLock) {
                this.startLock.notifyAll();
            }
        } else {
            synchronized (this) {
                this.result = WizardUI.ABORT;
                this.notifyAll();
            }
        }
    }

    public void discardOldInput(Device d) {

        this.executer.discardOldInput(d);
    }

    public synchronized DialogInput<?> getInput(final Pattern[] alt,
            final Device d,
            final Collection<Device> allDevices,
            final Collection<Device> waitDevices,
            final long timeout, final boolean forceTimeout) {

        if (this.result == WizardUI.ABORT) {
            this.result = null;
            throw new ExecutionStoppedException();
        }

        this.options.removeAll();
        this.alternatives = alt;

        for (int i = 0; i < this.alternatives.length; i++) {
            String prefix
                    = WizardUI.DISPLAY_KEYBOARD_SHORTCUTS ? (i + 1 <= 9 ? (i + 1) + ": "
                                    : "   ") : "";
            JLabel l = new JLabel(prefix + this.alternatives[i]);
            l.setForeground(Color.blue);
            l.setFont(new Font("Helvetica", Font.PLAIN, 16));
            MouseInputListener ml = new ResultHandler(this.alternatives[i]);
            l.addMouseListener(ml);
            l.addMouseMotionListener(ml);
            l.setBorder(BorderFactory.createCompoundBorder(BorderFactory
                    .createEmptyBorder(5, 20,
                            5, 20), BorderFactory.createEtchedBorder()));
            // l.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
            this.options.add(l);
        }

        final Color origColor = this.timeoutButton.getForeground();

        if (timeout > 0) {
            String prefix = WizardUI.DISPLAY_KEYBOARD_SHORTCUTS ? ("   ") : "";
            JLabel l = new JLabel(prefix + "<timeout after " + (timeout / 1000) + "."
                    + ((timeout / 100) % 10) + "s>");
            l.setForeground(Color.blue);
            l.setFont(new Font("Helvetica", Font.PLAIN, 16));
            MouseInputListener ml = new ResultHandler(new Timeout());
            l.addMouseListener(ml);
            l.addMouseMotionListener(ml);
            l.setBorder(BorderFactory.createCompoundBorder(BorderFactory
                    .createEmptyBorder(5, 20,
                            5, 20), BorderFactory.createEtchedBorder()));
            // l.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
            this.options.add(l);
            this.timeoutLabel = l;
            this.timeoutButton.setEnabled(true);
        } else {
            this.timeoutLabel = null;
            this.timeoutButton.setEnabled(false);
        }

        this.options.invalidate();
        this.getContentPane().validate();

        // toFront();
        this.options.requestFocus();
        this.repaint();

        this.result = null;

        final JLabel _timeoutLabel = this.timeoutLabel;
        Thread executerThread = new Thread(new Runnable() {

            public void run() {

                try {
                    long executerTimeout = timeout;
                    while (true) {
                        // System.out.println("Starting executer with timeout " +
                        // executerTimeout);
                        DialogInput<?> in
                                = WizardUI.this.executer.getInput(alt, d, allDevices, waitDevices,
                                        executerTimeout, forceTimeout);
                        Object result = in.getInput();
                        // System.out.println("Received from executer: " + result);
                        if (result instanceof Timeout) {
                            if ((WizardUI.this.timeoutMode == WizardUI.TIMEOUT_FORCE)
                                    || forceTimeout || (timeout == 1)
                                    || (executerTimeout == 0)
                                    || (result instanceof ForcedTimeout)) {
                                if (WizardUI.this.postResult(result)) {
                                    return;
                                }
                            } else {
                                if (WizardUI.this.timeoutMode == WizardUI.TIMEOUT_SIGNAL) {
                                    if (_timeoutLabel != null) {
                                        _timeoutLabel.setForeground(Color.red);
                                    }
                                    WizardUI.this.timeoutButton.setEnabled(true);
                                    WizardUI.this.timeoutButton
                                            .setForeground(Color.red);
                                }
                                executerTimeout = 0;
                                if (d != null) {
                                    d.signalTimeout();
                                } else {
                                    for (Device device : allDevices) {
                                        device.signalTimeout();
                                    }
                                }
                            }
                        } else {
                            if (WizardUI.this.postResult(result)) {
                                return;
                            }
                        }
                    }
                } catch (ExecutionStoppedException ignore) {
                } catch (Exception exn) {
                    exn.printStackTrace();
                }
            }
        }, "Woz Executer Thread");
        executerThread.start();

        try {
            while (this.result == null) {
                this.wait();
            }
        } catch (InterruptedException exn) {
        }

        this.options.removeAll();
        this.options.invalidate();
        this.options.validate();
        this.options.repaint();

        this.timeoutButton.setEnabled(false);
        this.timeoutButton.setForeground(origColor);

        if (executerThread != null) {
            executerThread.interrupt();
        }

        if ((this.result == WizardUI.ABORT) || (this.result == null)) {
            this.result = null;
            throw new ExecutionStoppedException();
        } else {
            return new DialogInput<Object>(this.wizardDevice, this.result);
        }
    }

    public void preExecute(Node node) {

        this.executer.preExecute(node);

        synchronized (this) {
            if (this.result == WizardUI.ABORT) {
                this.result = null;
                throw new ExecutionStoppedException();
            }
        }
    }

    public void transition(Node source, Node destination, int edge,
            String condition) {

        this.executer.transition(source, destination, edge, condition);

        synchronized (this) {
            if (this.result == WizardUI.ABORT) {
                this.result = null;
                throw new ExecutionStoppedException();
            }
        }

        if (source == null) {
            throw new IllegalArgumentException("null source");
        }
        if (this.log != null) {
            if (destination != null) {
                synchronized (this.log) {
                    this.log.openElement("transition");
                    this.log.printElement("src", new String[]{"uid", "class"},
                            new Object[]{
                                this.uid_map.nodes.put(source),
                                source.getClass().getName()},
                            source.getTitle());
                    this.log.printElement("dst", new String[]{"uid", "class"},
                            new Object[]{
                                this.uid_map.nodes.put(destination),
                                destination.getClass().getName()},
                            destination.getTitle());
                    if (condition != null) {
                        this.log.printElement("condition", condition);
                    }
                    this.log.closeElement("transition");
                }
            } else {
                this.error("EdgeNotConnectedException", "node_uid "
                        + this.uid_map.nodes.put(source)
                        + ", edge " + edge);
            }
        }
    }

    public void subgraph(OwnerNode owner, boolean enter) {

        for (GraphExecutionListener l : this.graphExecutionListeners) {
            if (enter) {
                l.graphExecutionStarted(owner.getOwnedGraph());
            } else {
                l.graphExecutionStopped(owner.getOwnedGraph());
            }
        }

        this.executer.subgraph(owner, enter);

        if (enter) {
            this.variables.push(owner, owner.getOwnedGraph().getAllVariables(
                    Graph.LOCAL));
        } else {
            this.variables.pop();
        }

        if (this.log != null) {
            String ownerID = this.uid_map.nodes.put(owner);
            String ownerName = owner.getGraphName();

            String tag = (owner instanceof ProcNode) ? "call" : "subgraph";
            if (enter) {
                long start_time
                        = this.log.printEvent(tag, new String[]{"title", "uid"},
                                new Object[]{ownerName, ownerID}, null);
                this.subgraph_times.push(Long.valueOf(start_time));
            } else {
                long end_time
                        = this.log.printEvent((owner instanceof ProcNode) ? "return" : "exit",
                                new String[]{"title", "uid"}, new Object[]{ownerName,
                            ownerID}, null);
                long time = end_time - this.subgraph_times.pop().longValue();
                this.log.printElement("duration", new String[]{"title", "uid"},
                        new Object[]{
                            owner.getGraphName(), ownerID}, Long.valueOf(time));
            }
        }
    }

    public void error(String type, String message) {

        this.executer.error(type, message);

        if (this.log != null) {
            synchronized (this.log) {
                this.log.openElement("error");
                this.log.printElement("type", type);
                if (message != null) {
                    this.log.printElement("message", message);
                }
                this.log.closeElement("error");
            }
        }
    }

    public void preExecute(com.clt.script.cmd.Command cmd) {

        this.executer.preExecute(cmd);
    }

    public void preEvaluate(com.clt.script.exp.Expression exp) {

        this.executer.preEvaluate(exp);
    }

    public void log(String s) {

        System.out.println(s);
    }

    private synchronized boolean postResult(Object result) {

        if (this.result == null) {
            this.result = result;
            this.notifyAll();
            return true;
        } else {
            return false;
        }
    }

    private class ResultHandler
            extends MouseClickAdapter {

        Object result;

        public ResultHandler(Object result) {

            super(Color.red, Color.blue);
            this.result = result;
        }

        @Override
        public void mouseClicked(MouseEvent e) {

            WizardUI.this.postResult(this.result);
        }
    }

    @Override
    public String getName() {

        return "Wizard interface";
    }

    private Component createVariableDisplay() {

        JTable t = this.variables.createVariableDisplay();
        return GUI.createScrollPane(t);
    }

    public void addGraphExecutionListener(GraphExecutionListener l) {

        this.graphExecutionListeners.add(l);
    }

    public void removeGraphExecutionListener(GraphExecutionListener l) {

        this.graphExecutionListeners.remove(l);
    }

    public void setDelay(long delay) {

        this.delay = delay;
    }

    public long getDelay() {

        return this.delay;
    }
}
