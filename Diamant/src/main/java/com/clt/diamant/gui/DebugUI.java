package com.clt.diamant.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Stack;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import com.clt.dialog.client.DeviceEvent;
import com.clt.dialog.client.DeviceListener;
import com.clt.diamant.Device;
import com.clt.diamant.DialogInput;
import com.clt.diamant.Document;
import com.clt.diamant.Executer;
import com.clt.diamant.ExecutionStoppedException;
import com.clt.diamant.InputCenter;
import com.clt.diamant.Resources;
import com.clt.diamant.SingleDocument;
import com.clt.diamant.Version;
import com.clt.diamant.WozInterface;
import com.clt.diamant.graph.Graph;
import com.clt.diamant.graph.GraphExecutionListener;
import com.clt.diamant.graph.GraphOwner;
import com.clt.diamant.graph.Node;
import com.clt.diamant.graph.nodes.OwnerNode;
import com.clt.diamant.graph.ui.GraphUI;
import com.clt.gui.CmdButton;
import com.clt.gui.GUI;
import com.clt.gui.Images;
import com.clt.gui.LoggingPane;
import com.clt.gui.menus.CmdMenu;
import com.clt.gui.menus.CmdMenuBar;
import com.clt.gui.menus.MenuCommander;
import com.clt.gui.menus.MenuOwner;
import com.clt.script.cmd.Command;
import com.clt.script.exp.Expression;
import com.clt.script.exp.Pattern;
import com.clt.script.exp.Value;
import com.clt.util.Timeout;

/**
 * @author dabo
 *
 */
class DebugUI extends JDialog implements WozInterface, MenuCommander, MenuOwner {

    private static final int cmdPause = 3800;
    private static final int cmdContinue = 3801;
    private static final int cmdStop = 3802;
    private static final int cmdStepOver = 3803;
    private static final int cmdStepInto = 3804;
    private static final int cmdStepOut = 3805;
    private static final int cmdTimeout = 3806;

    private enum Mode {
        RUN,
        STEPINTO,
        STEPOUT,
        STEPOVER,
        STOPPED
    }

    private enum TimeoutMode {
        FORCE,
        SIGNAL
    }

    private Collection<GraphExecutionListener> graphExecutionListeners;
    private long delay;
    private VarList variables = new VarList();

    private Executer executer;
    private JPanel graphView;
    private CommTableModel communication;
    private LoggingPane messages;

    private JToolBar toolbar;
    private JButton timeoutButton;

    private final Object runLock = new Object();
    private Mode mode;
    private TimeoutMode timeoutMode;

    private transient GraphUI currentGraph = null;
    private transient Node currentNode = null;
    private transient DialogInput<?> recognitionResult = null;
    private transient boolean timeoutAllowed = false;

    private Stack<GraphOwner> callHierarchy;
    private int stepOverStackDepth = 0;

    public DebugUI(Component parent) {

        super(GUI.getFrameForComponent(parent), "Debugger", false);

        this.timeoutMode = TimeoutMode.SIGNAL;

        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent evt) {

                if (DebugUI.this.mode == Mode.STOPPED) {
                    // es laeuft nichts.
                    // so lange koennen wir einfach aufhoeren
                    DebugUI.this.abort();
                }
                // ansonsten ignorieren wir den Klick
            }
        });

        this.callHierarchy = new Stack<GraphOwner>();
        this.graphExecutionListeners = new ArrayList<GraphExecutionListener>();
        this.delay = 0;
        this.executer = new Executer(this, false);

        this.graphView = new JPanel(new GridLayout(1, 1));
        this.graphView.setPreferredSize(new Dimension(600, 400));

        this.toolbar = new JToolBar();
        this.toolbar.setFloatable(false);

        this.addToolbarButton(DebugUI.cmdPause, Resources.getString("Pause"),
                Images.load("wizard/WizardPause.png"));
        this.addToolbarButton(DebugUI.cmdContinue, Resources.getString("Continue"),
                Images.load("wizard/WizardContinue.png"));
        this.addToolbarButton(DebugUI.cmdStepOver, Resources.getString("StepOver"),
                Images.load("wizard/WizardStepOver.png"));
        this.addToolbarButton(DebugUI.cmdStepInto, Resources.getString("StepInto"),
                Images.load("wizard/WizardStepInto.png"));
        this.addToolbarButton(DebugUI.cmdStepOut, Resources.getString("StepOut"),
                Images.load("wizard/WizardStepOut.png"));
        this.addToolbarButton(DebugUI.cmdStop, Resources.getString("Stop"), Images
                .load("wizard/WizardStop.png"));
        this.timeoutButton
                = this.addToolbarButton(DebugUI.cmdTimeout, Resources
                        .getString("TimeoutExceeded"),
                        Images.load("wizard/WizardTimeout.png"));

        JTabbedPane infos = new JTabbedPane();
        infos.setUI(new com.clt.gui.plaf.AdobeTabbedPaneUI());
        infos.addTab(Resources.getString("Variables"),
                GUI.createScrollPane(this.variables.createVariableDisplay()));
        infos.addTab(Resources.getString("Callstack"),
                GUI.createScrollPane(this.variables.createCallstackDisplay()));

        this.communication = new CommTableModel();
        infos.addTab(Resources.getString("Devices"),
                GUI.createScrollPane(new JTable(this.communication)));

        this.messages = new LoggingPane(0);
        this.messages.setEditable(false);

        infos.addTab(Resources.getString("Log"), new JScrollPane(this.messages));

        JPanel content = new JPanel(new BorderLayout());
        content.add(this.toolbar, BorderLayout.NORTH);

        JSplitPane jsp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        /*
     * content.add(graphView, BorderLayout.CENTER); content.add(infos,
     * BorderLayout.SOUTH);
         */
        jsp.setTopComponent(this.graphView);
        jsp.setBottomComponent(infos);
        infos.setPreferredSize(new Dimension(
                this.graphView.getPreferredSize().width, 150));
        content.add(jsp, BorderLayout.CENTER);
        this.setContentPane(content);
        GUI.assignMnemonics(this.getContentPane());

        this.initMenus();

        this.pack();
        this.setLocationRelativeTo(this.getParent());
    }

    private void initMenus() {

        CmdMenuBar mbar = new CmdMenuBar(this);
        CmdMenu m = mbar.addMenu(Resources.getString("Debug"));
        m.addItem(Resources.getString("Pause"), DebugUI.cmdPause);
        m.addItem(Resources.getString("Continue"), DebugUI.cmdContinue);
        m.addItem(Resources.getString("Stop"), DebugUI.cmdStop);
        m.addSeparator();
        m.addItem(Resources.getString("StepOver"), DebugUI.cmdStepOver,
                KeyEvent.VK_RIGHT);
        m.addItem(Resources.getString("StepInto"), DebugUI.cmdStepInto,
                KeyEvent.VK_DOWN);
        m.addItem(Resources.getString("StepOut"), DebugUI.cmdStepOut,
                KeyEvent.VK_UP);

        this.setJMenuBar(mbar);
    }

    private JButton addToolbarButton(int cmd, String name, Icon icon) {

        CmdButton button = new CmdButton(this, cmd, name);
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

        this.toolbar.add(button);
        return button;
    }

    public boolean initInterface() {

        if (!this.executer.initInterface()) {
            return false;
        }

        this.setVisible(true);

        return true;
    }

    public void disposeInterface(boolean error) {

        this.executer.disposeInterface(error);
        this.variables.clear();

        this.setVisible(false);
        this.dispose();
    }

    public void startDocument(Document d, String name, InputCenter input) {

        if (d instanceof SingleDocument) {
            SingleDocument sd = (SingleDocument) d;
            this.variables.push(sd, sd.getOwnedGraph().getAllVariables(Graph.LOCAL));
            this.setGraph(sd.getOwnedGraph());
        }

        if (d instanceof SingleDocument) {
            this.callHierarchy.push((SingleDocument) d);
        }

        input.addDeviceListener(new DeviceListener() {

            public void stateChanged(DeviceEvent evt) {

            }

            public void dataSent(DeviceEvent evt) {

            }

            public void dataReceived(DeviceEvent evt) {

                DebugUI.this.communication.add((Device) evt.getSource(), true, evt
                        .getData().toString());
            }

            public void dataLogged(DeviceEvent evt) {

            }
        });

        this.mode = Mode.STEPINTO;
        this.currentNode = null;
        this.executer.startDocument(d, name, input);
    }

    public void endDocument(Document d) {

        this.executer.endDocument(d);

        if (d instanceof SingleDocument) {
            this.variables.pop();
            this.callHierarchy.pop();
        }
    }

    public void output(Device d, Value value) {

        this.executer.output(d, value);

        if (value != null) {
            this.communication.add(d, false, value.toString());
        }
    }

    public void discardOldInput(Device d) {

        this.executer.discardOldInput(d);
    }

    public synchronized DialogInput<?> getInput(final Pattern[] alternatives,
            final Device d,
            final Collection<Device> allDevices,
            final Collection<Device> waitDevices,
            final long timeout, final boolean forceTimeout) {

        if (this.mode == Mode.STOPPED) {
            throw new ExecutionStoppedException();
        }

        this.recognitionResult = null;

        Thread executerThread = null;

        this.timeoutAllowed = (timeout != 0);

        final Color origColor = this.timeoutButton.getForeground();

        executerThread = new Thread(new Runnable() {

            public void run() {

                try {
                    long executerTimeout = timeout;
                    while (true) {
                        DialogInput<?> in
                                = DebugUI.this.executer.getInput(alternatives, d, allDevices,
                                        waitDevices, executerTimeout, forceTimeout);
                        Object result = in.getInput();
                        if (result instanceof Timeout) {
                            if ((DebugUI.this.timeoutMode == TimeoutMode.FORCE)
                                    || forceTimeout
                                    || (in.getDevice() != null)) {
                                if (DebugUI.this.postResult(in)) {
                                    return;
                                }
                            } else {
                                executerTimeout = 0;
                                DebugUI.this.timeoutButton
                                        .setForeground(Color.RED);
                                if (d != null) {
                                    d.signalTimeout();
                                } else {
                                    for (Device device : allDevices) {
                                        device.signalTimeout();
                                    }
                                }
                            }
                        } else {
                            if (DebugUI.this.postResult(in)) {
                                return;
                            }
                        }
                    }
                } catch (Exception ignore) {
                }
            }
        }, Version.PRODUCT_NAME + " DebugUI Executer Thread");
        executerThread.start();
        this.updateMenus();

        try {
            while ((this.recognitionResult == null) && (this.mode != Mode.STOPPED)) {
                this.wait();
            }
        } catch (InterruptedException exn) {
        }

        this.timeoutAllowed = false;
        this.timeoutButton.setForeground(origColor);
        this.updateMenus();

        if (this.mode == Mode.STOPPED) {
            throw new ExecutionStoppedException();
        }

        return this.recognitionResult;
    }

    private synchronized boolean postResult(DialogInput<?> result) {

        if (this.recognitionResult == null) {
            this.recognitionResult = result;
            this.notifyAll();
            return true;
        } else {
            return false;
        }
    }

    public void transition(Node source, Node destination, int index,
            String condition) {

        if (this.mode == Mode.STOPPED) {
            throw new ExecutionStoppedException();
        }

        this.executer.transition(source, destination, index, condition);
    }

    public void subgraph(OwnerNode owner, boolean enter) {

        if (this.mode == Mode.STOPPED) {
            throw new ExecutionStoppedException();
        }

        for (GraphExecutionListener l : this.graphExecutionListeners) {
            if (enter) {
                l.graphExecutionStarted(owner.getOwnedGraph());
            } else {
                l.graphExecutionStopped(owner.getOwnedGraph());
            }
        }

        this.executer.subgraph(owner, enter);

        if (enter) {
            this.callHierarchy.push(owner);
            this.variables.push(owner, owner.getOwnedGraph().getAllVariables(
                    Graph.LOCAL));
        } else {
            this.variables.pop();
            this.callHierarchy.pop();
        }
    }

    public void error(String type, String message) {

        this.executer.error(type, message);

        this.messages.println(type + ": " + message, Color.RED);
    }

    @Override
    public String getName() {

        return "Debugger";
    }

    public void setState(State state) {

        this.executer.setState(state);
    }

    public void abort() {

        this.executer.abort();

        if (this.mode == Mode.STOPPED) {
            synchronized (this.runLock) {
                this.runLock.notifyAll();
            }
        }

        synchronized (this) {
            this.mode = Mode.STOPPED;
            this.notifyAll();
        }
    }

    public boolean isDebugger() {

        return true;
    }

    public boolean showSubdialogsDuringExecution() {

        // we do our own display
        return false;
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

    private void breakpoint(Node breakpoint) {

        if (breakpoint != null) {
            this.setGraph(breakpoint.getGraph());
            this.currentGraph.showNode(breakpoint);
        }

        this.mode = Mode.STOPPED;
        this.stepOverStackDepth = 0;
        this.updateMenus();

        try {
            synchronized (this.runLock) {
                this.runLock.wait();
            }
        } catch (InterruptedException ignore) {
        }

        if (this.mode == Mode.STOPPED) {
            throw new ExecutionStoppedException();
        } else if (this.mode == Mode.STEPOVER) {
            this.stepOverStackDepth = this.callHierarchy.size();
        } else if (this.mode == Mode.STEPOUT) {
            this.stepOverStackDepth = this.callHierarchy.size() - 1;
        }
    }

    public void preExecute(Node node) {

        this.currentNode = node;
        switch (this.mode) {
            case STEPINTO:
                this.breakpoint(node);
                break;

            case STEPOVER:
            case STEPOUT:
                if (this.stepOverStackDepth >= this.callHierarchy.size()) {
                    this.breakpoint(node);
                }
                break;

            case RUN:
                if (node.isBreakpoint()) {
                    this.breakpoint(node);
                }
                break;

            case STOPPED:
                throw new ExecutionStoppedException();
        }

        this.executer.preExecute(node);
    }

    public void preExecute(Command cmd) {

        this.executer.preExecute(cmd);
    }

    public void preEvaluate(Expression exp) {

        this.executer.preEvaluate(exp);
    }

    public void log(String s) {

        this.messages.println(s);
    }

    public boolean menuItemState(int cmd) {

        switch (cmd) {
            case cmdPause:
                return (this.mode != Mode.STEPINTO) && (this.mode != Mode.STOPPED);
            case cmdContinue:
                return this.mode == Mode.STOPPED;
            case cmdStepOver:
                return (this.mode != Mode.RUN) && (this.callHierarchy.size() > 0);
            case cmdStepInto:
                return (this.mode != Mode.RUN)
                        && (this.currentNode instanceof GraphOwner);
            case cmdStepOut:
                return (this.mode != Mode.RUN) && (this.callHierarchy.size() > 1);
            case cmdStop:
                return true;
            case cmdTimeout:
                return this.timeoutAllowed;
            default:
                return true;
        }
    }

    public String menuItemName(int cmd, String oldName) {

        switch (cmd) {
            default:
                return oldName;
        }
    }

    public boolean doCommand(int cmd) {

        boolean cmdHandled = true;

        synchronized (this.runLock) {
            switch (cmd) {
                case cmdPause:
                    this.mode = Mode.STEPINTO;
                    break;
                case cmdContinue:
                    this.mode = Mode.RUN;
                    this.runLock.notifyAll();
                    break;
                case cmdStepInto:
                    this.mode = Mode.STEPINTO;
                    this.runLock.notifyAll();
                    break;
                case cmdStepOut:
                    this.mode = Mode.STEPOUT;
                    this.runLock.notifyAll();
                    break;
                case cmdStepOver:
                    this.mode = Mode.STEPOVER;
                    this.runLock.notifyAll();
                    break;
                case cmdStop:
                    this.mode = Mode.STOPPED;
                    this.abort();
                    break;
                case cmdTimeout:
                    this.postResult(new DialogInput<Object>(new Device("Debugger"),
                            new Timeout()));
                    break;
                default:
                    cmdHandled = false;
            }
        }

        this.updateMenus();

        return cmdHandled;
    }

    public void updateMenus() {

        JMenuBar mbar = this.getJMenuBar();
        if (mbar instanceof CmdMenuBar) {
            ((CmdMenuBar) mbar).updateMenus();
        }

        for (int i = 0; i < this.toolbar.getComponentCount(); i++) {
            Component c = this.toolbar.getComponent(i);
            if (c instanceof CmdButton) {
                CmdButton b = (CmdButton) c;
                b.setEnabled(this.menuItemState(b.getCommand()));
                b.setText(this.menuItemName(b.getCommand(), b.getText()));
            }
        }
    }

    private boolean setGraph(Graph g) {

        if ((this.currentGraph == null) || (g != this.currentGraph.getGraph())) {
            this.currentGraph = new GraphUI(g);
            this.graphView.removeAll();
            this.graphView.add(this.currentGraph.getScrollPane());
            this.graphView.invalidate();
            this.graphView.validate();
            this.graphView.repaint();
            return true;
        } else {
            return false;
        }
    }
}
