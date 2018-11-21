package com.clt.diamant.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.clt.dialog.client.ConnectDialog;
import com.clt.dialogos.plugin.Plugin;
import com.clt.dialogos.plugin.PluginLoader;
import com.clt.diamant.Device;
import com.clt.diamant.Executer;
import com.clt.diamant.ExecutionResult;
import com.clt.diamant.IdMap;
import com.clt.diamant.MenuUtils;
import com.clt.diamant.Preferences;
import com.clt.diamant.Resources;
import com.clt.diamant.SingleDocument;
import com.clt.diamant.Version;
import com.clt.diamant.WozInterface;
import com.clt.diamant.graph.Graph;
import com.clt.diamant.graph.GraphExecutionListener;
import com.clt.diamant.graph.GraphOwner;
import com.clt.diamant.graph.Node;
import com.clt.diamant.graph.search.NodeSearchFilter;
import com.clt.diamant.graph.search.SearchResult;
import com.clt.diamant.graph.search.SearchResultsDialog;
import com.clt.diamant.graph.ui.GraphUI;
import com.clt.diamant.graph.ui.ProcTree;
import com.clt.event.ProgressListener;
import com.clt.gui.Commands;
import com.clt.gui.FileChooser;
import com.clt.gui.GUI;
import com.clt.gui.Images;
import com.clt.gui.ListEditor;
import com.clt.gui.OptionPane;
import com.clt.gui.ProgressDialog;
import com.clt.gui.WindowUtils;
import com.clt.gui.menus.CmdMenu;
import com.clt.gui.menus.CmdMenuBar;
import com.clt.gui.menus.CmdMenuItem;
import com.clt.gui.menus.MenuCommander;
import com.clt.gui.plaf.IconTabbedPaneUI;
import com.clt.mac.RequiredEventHandler;
import com.clt.util.DefaultLongAction;
import com.clt.util.UserCanceledException;
import com.clt.xml.XMLWriter;
import java.util.function.ToIntFunction;
import javax.swing.UIManager;

public class SingleDocumentWindow<DocType extends SingleDocument>
        extends DocumentWindow<DocType>
        implements GraphEditor {

    public static final int cmdEditDevices = Commands.cmdDocument + 2;
    public static final int cmdResetDevices = Commands.cmdDocument + 3;
    public static final int cmdValidate = Commands.cmdDocument + 4;
    public static final int cmdZoomIn = Commands.cmdDocument + 6;
    public static final int cmdZoomOut = Commands.cmdDocument + 7;
    public static final int cmdSetup = Commands.cmdDocument + 10;
    public static final int cmdRun = Commands.cmdDocument + 11;
    public static final int cmdRunConfiguration = Commands.cmdDocument + 17;
    public static final int cmdRunWithLog = Commands.cmdDocument + 12;
    public static final int cmdDebug = Commands.cmdDocument + 13;
    public static final int cmdWoz = Commands.cmdDocument + 14;
    public static final int cmdDelay = Commands.cmdDocument + 15;
    public static final int cmdExportVXML = Commands.cmdDocument + 16;

    private DefaultToolbox toolbox;
    private NodeToolbox nodebox;
    private ProcTree procTree;
    private GraphUI mainView;
    private JCheckBoxMenuItem subwin;
    private CmdMenu colorMenu;

    private JPanel contentPanel;
    private Header header;
    private JComponent toolbars;
    private Collection<ComponentListener> viewListeners = new ArrayList<ComponentListener>();
    private boolean openSubWindows = false;
    private long delay = 0;
    private PropertyChangeListener documentPropertyListener;
    private ChangeListener preferenceListener;
    private MenuCommander superCommander;

    private transient WozInterface runtime;
    private transient Thread executionThread;

    static {
        // remove border around splitpane in MacOS
        UIManager.getDefaults().put("SplitPane.border", BorderFactory.createEmptyBorder());
    }

    public SingleDocumentWindow(DocType d, MenuCommander superCommander, final RequiredEventHandler systemEventHandler, boolean singleWindow) {
        super(d);

        this.superCommander = superCommander;
        this.contentPanel = new JPanel(new GridLayout(1, 1));
        this.header = new Header();
        this.header.addLinkListener(new Header.LinkListener() {

            public void linkClicked(Object link) {

                if (link instanceof Graph) {
                    SingleDocumentWindow.this.setMainView((Graph) link);
                }
            }
        });

        this.toolbars = new JPanel(new GridLayout(0, 1));

        this.toolbox = new DefaultToolbox(this);
        this.nodebox = new NodeToolbox(Preferences.getPrefs().groupNodeToolbox);
        this.procTree = new ProcTree(this, singleWindow);

        this.initUI(systemEventHandler);

        this.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {

                if ((e.getKeyCode() == KeyEvent.VK_ALT)
                        && (SingleDocumentWindow.this.mainView != null)) {
                    SingleDocumentWindow.this.mainView.adjustCursor(DefaultToolbox.HAND);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

                if ((e.getKeyCode() == KeyEvent.VK_ALT)
                        && (SingleDocumentWindow.this.mainView != null)
                        && (SingleDocumentWindow.this.toolbox != null)) {
                    SingleDocumentWindow.this.mainView
                            .adjustCursor(SingleDocumentWindow.this.toolbox.getTool());
                }
            }
        });

        this.documentPropertyListener = new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {

                if (evt.getPropertyName().equals("graph")) {
                    SingleDocumentWindow.this.updateGraph(true);
                }
            }
        };

        this.preferenceListener = new ChangeListener() {

            public void stateChanged(ChangeEvent e) {

                if ((e.getSource() == Preferences.getPrefs().showGrid)
                        || (e.getSource() == Preferences.getPrefs().gridColor)
                        || (e.getSource() == Preferences.getPrefs().gridSize)) {
                    SingleDocumentWindow.this.mainView.repaint();
                } else if ((e.getSource() == Preferences.getPrefs().showToolbox)
                        || (e.getSource() == Preferences.getPrefs().showProcedureTree)
                        || (e.getSource() == Preferences.getPrefs().showNodePanel)
                        || (e.getSource() == Preferences.getPrefs().lastUsedFile)
                        ) {
                    SingleDocumentWindow.this.initUI(systemEventHandler);
                }
            }
        };

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent evt) {
                SingleDocumentWindow.this.requestFocus();
                GraphEditorFactory.register(SingleDocumentWindow.this.getGraphOwner(), SingleDocumentWindow.this);
            }

            @Override
            public void windowClosed(WindowEvent evt) {
                GraphEditorFactory.unregister(SingleDocumentWindow.this.getGraphOwner());
                GraphEditorFactory.ownerDeleting(SingleDocumentWindow.this.getGraphOwner());
            }
        });

        this.finishSetup();
    }

    private void initUI(RequiredEventHandler systemEventHandler) {

        for (CmdMenuBar mbar : this.getMenus()) {
            mbar.getParent().remove(mbar);
        }

        this.initContentPane();
        this.setupMenus(this.initMenus(systemEventHandler));
        if (this.toolbox != null) {
            this.toolbox.update();
        }

        this.validate();
        this.updateMenus();
    }

    private void initContentPane() {
        Container content = this.getContentPane();
        content.removeAll();
        content.setLayout(new BorderLayout());

        // add toolbar at the top (select/scroll/delete - run/debug/wizard - ...)
        this.toolbars.removeAll();
        content.add(this.toolbars, BorderLayout.NORTH);

        if (Preferences.getPrefs().showToolbox.getValue()) {
            this.toolbars.add(this.toolbox);
        } else {
            this.toolbox.setTool(DefaultToolbox.ANCHOR);
        }

        if (Preferences.getPrefs().showProcedureTree.getValue()) {
            JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
            split.setOneTouchExpandable(true);

            JScrollPane jsp = GUI.createScrollPane(this.procTree, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            jsp.setBorder(null);
            jsp.setMinimumSize(new Dimension(100, 100));

            // add toolbar at the left for subgraphs
            JPanel left = new JPanel(new BorderLayout());
            left.add(jsp, BorderLayout.CENTER);
            left.add(new Header(Resources.getString("Subgraphs")), BorderLayout.NORTH);
            split.setLeftComponent(left);

            // add graph panel in the center
            JPanel right = new JPanel(new BorderLayout());
            right.add(this.header, BorderLayout.NORTH);
            right.add(this.contentPanel, BorderLayout.CENTER);
            split.setRightComponent(right);
            content.add(split, BorderLayout.CENTER);
        } else {
            // add graph panel (without header) in the center
            content.add(this.contentPanel, BorderLayout.CENTER);
            this.setMainView(this.getGraphOwner().getOwnedGraph());
        }

        // add toolbar at the right for node types
        if (Preferences.getPrefs().showNodePanel.getValue()) {
            JScrollPane nodeScroller = new JScrollPane(this.nodebox, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            nodeScroller.setBorder(null);
            nodeScroller.setViewportBorder(null);
            content.add(nodeScroller, BorderLayout.EAST);
        }

    }

    @Override
    public void validate() {
        super.validate();

        if (mainView != null) {
            // Depending on the configuration of the toolbars in the DialogOS window,
            // the original size of the graph in the GraphUI can be too small or too large.
            // Also, when the user drags the DialogOS window bigger, the boundaries of the
            // GraphUI become visible as a black border.

            // The code below automatically resizes the graph to the correct size.
            int scrollbarWidth = mainView.getScrollPane().getHorizontalScrollBar().getHeight();
            int requiredGraphWidth = contentPanel.getSize().width - scrollbarWidth;
            int requiredGraphHeight = contentPanel.getSize().height - scrollbarWidth;
            Graph g = mainView.getGraph();

            if (requiredGraphWidth > g.getWidth()) {
                g.setSize(requiredGraphWidth, g.getHeight());
            }

            if (requiredGraphHeight > g.getHeight()) {
                g.setSize(g.getWidth(), requiredGraphHeight);
            }
        }
    }

    private int totalSize(List<JComponent> components, ToIntFunction<JComponent> attribute) {
        int ret = 0;
        for (JComponent c : components) {
            ret += attribute.applyAsInt(c);
        }
        return ret;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        Preferences.getPrefs().addPropertyChangeListener(this.preferenceListener);
        this.getDocument().addPropertyChangeListener(this.documentPropertyListener);
        this.updateGraph(true);
    }

    @Override
    public void removeNotify() {
        this.getDocument().removePropertyChangeListener(this.documentPropertyListener);
        Preferences.getPrefs().removePropertyChangeListener(this.preferenceListener);
        this.updateGraph(false);
        super.removeNotify();
    }

    private void updateGraph(boolean show) {

        if (show) {
            Graph graph = this.getDocument().getOwnedGraph();
            if (this.procTree != null) {
                this.procTree.setMainGraph(graph);
            }

            this.setMainView(graph);
        } else {
            if (this.procTree != null) {
                this.procTree.setMainGraph(null);
            }

            // setMainView(null);
        }
    }

    public void showEditor() {

        Graph graph = this.getGraphOwner().getOwnedGraph();
        if (graph != null) {
            GraphUI ui = this.getMainView();
            if ((ui == null) || (ui.getGraph() != graph)) {
                ui = this.setMainView(graph);
            }

            ui.repaint();
        }
    }

    public void closeEditor() {

        if (this.isShowing()) {
            this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        }
    }

    private Collection<CmdMenuBar> initMenus(
            RequiredEventHandler systemEventHandler) {

        Collection<CmdMenuBar> mbars = new ArrayList<CmdMenuBar>();

        MenuCommander commander = this.superCommander;
        if (commander == null) {
            commander = this;
        }

        CmdMenuBar mbar = new CmdMenuBar(commander) {

            private final boolean allowBlending = false;

            private boolean blendWithToolbar() {

                return this.allowBlending
                        && Preferences.getPrefs().showToolbox.getValue();
            }

            @Override
            protected void paintComponent(Graphics g) {

                if (this.blendWithToolbar()) {
                    int width = this.getWidth();
                    int height = this.getHeight();

                    Color end = DefaultToolbox.startColor;
                    Color start = new Color(end.getRed() + 5, end.getGreen() + 5,
                                            end.getBlue() + 5, end.getAlpha());

                    Graphics2D gfx = (Graphics2D) g;
                    Paint oldPaint = gfx.getPaint();
                    gfx.setPaint(new GradientPaint(0, 0, start, 0, height - 1, end));
                    gfx.fillRect(0, 0, width, height - 1);
                    gfx.setPaint(oldPaint);
                } else {
                    super.paintComponent(g);
                }
            }

            @Override
            public boolean isBorderPainted() {

                if (this.blendWithToolbar()) {
                    return false;
                } else {
                    return super.isBorderPainted();
                }
            }

            @Override
            public JMenu add(final JMenu m) {

                if (this.allowBlending) {
                    m.addMouseListener(new MouseAdapter() {

                        @Override
                        public void mouseEntered(MouseEvent e) {

                            m.setOpaque(true);
                        }

                        @Override
                        public void mouseExited(MouseEvent e) {

                            m.setOpaque(false);
                        }
                    });
                    m.setBackground(new Color(0, 0, 0, 0));
                }
                return super.add(m);
            }
        };
        CmdMenu m;

        MenuUtils.addFileMenu(mbar, systemEventHandler);
        MenuUtils.addEditMenu(mbar);

        m = mbar.addMenu(Resources.getString("DialogMenu"));
        m.addItem(Resources.getString("Run"), SingleDocumentWindow.cmdRun,
                  KeyEvent.VK_R);
        m.addItem(Resources.getString("RunConfiguration"), SingleDocumentWindow.cmdRunConfiguration);
        // m.addItem(Resources.getString("RunWithLog"), cmdRunWithLog);
        m.addItem(Resources.getString("Debug"), SingleDocumentWindow.cmdDebug,
                  KeyStroke.getKeyStroke(KeyEvent.VK_R,
                                         (m.getToolkit().getMenuShortcutKeyMask() | Event.ALT_MASK)));
        m.addItem(Resources.getString("Woz"), SingleDocumentWindow.cmdWoz,
                  KeyStroke.getKeyStroke(KeyEvent.VK_R, (m
                                         .getToolkit().getMenuShortcutKeyMask() | Event.SHIFT_MASK)));
        // doesn't work if devices aren't connected
        // m.addItem(Resources.getString("ResetDevices"), cmdResetDevices);

        // m.addSeparator();
        // m.addItem(Resources.getString("SetDelay") + "...", cmdDelay);
        this.subwin
                = new JCheckBoxMenuItem(Resources.getString("ExecuteInNewWindows"), this
                                        .openSubWindows());
        this.subwin.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                SingleDocumentWindow.this.openSubWindows
                        = SingleDocumentWindow.this.subwin.isSelected();
            }
        });
        // m.add(subwin);

        m.addSeparator();

        // m.addItem(Resources.getString("DialogSetup") + "...", cmdSetup);
        m.addItem(Resources.getString("Devices") + "...",
                  SingleDocumentWindow.cmdEditDevices, KeyEvent.VK_D);

        for (final Plugin plugin : PluginLoader.getPlugins()) {
            m.add(new CmdMenuItem(plugin.getName(), 1, null, new MenuCommander() {

                              public String menuItemName(int cmd, String oldName) {

                                  return plugin.getName() + "...";
                              }

                              public boolean menuItemState(int cmd) {

                                  if (SingleDocumentWindow.this.runtime != null) {
                                      return false;
                                  } else {
                                      return true;
                                  }
                              }

                              public boolean doCommand(int cmd) {

                                  SingleDocumentWindow.this.showSetupDialog(plugin.getName());
                                  return true;
                              }

                          }));
        }

        m = mbar.addMenu(Resources.getString("GraphMenu"));
        m.addItem(Resources.getString("Variables") + "...", GraphUI.cmdEditVariables);
        m.addItem(Resources.getString("Functions") + "...", GraphUI.cmdEditFunctions);
        m.addItem(Resources.getString("GroovyFunctions") + "...", GraphUI.cmdEditGroovyFunctions);
        m.addItem("Groovy " + Resources.getString("Variables") + "...", GraphUI.cmdEditGroovyVariables);
        m.addItem(Resources.getString("Grammars") + "...", GraphUI.cmdEditGrammars);
        m.addItem(Resources.getString("InputHandlers") + "...", GraphUI.cmdEditHandlers);
        m.addItem(Resources.getString("CanvasSize") + "...", GraphUI.cmdCanvasSize);
        m.addSeparator();
        m.addItem(Resources.getString("Validate"), SingleDocumentWindow.cmdValidate, KeyEvent.VK_K);
        /*
     * m.addItem(Resources.getString("CheckSamples") + "...", cmdCheckSamples);
     * //m.addItem("Export as VoiceXML", cmdExportVXML); m.addSeparator();
     * m.addItem(Resources.getString("ZoomIn"), cmdZoomIn);
     * m.addItem(Resources.getString("ZoomOut"), cmdZoomOut);
         */

        m = mbar.addMenu(Resources.getString("NodeMenu"));
        this.colorMenu = m.addSubMenu(Resources.getString("Color"), GraphUI.cmdColorMenu);
        this.setupColorMenu(this.mainView);
        m.addSeparator();
        m.addItem(Resources.getString("Align") + "...", GraphUI.cmdAlign, KeyEvent.VK_L);
        m.addSeparator();
        m.addItem(Resources.getString("Group"), GraphUI.cmdGroup, KeyEvent.VK_G);
        m.addItem(Resources.getString("Ungroup"), GraphUI.cmdUngroup, KeyStroke
                  .getKeyStroke(
                          KeyEvent.VK_G,
                          (m.getToolkit().getMenuShortcutKeyMask() | Event.SHIFT_MASK)));
        m.addItem(Resources.getString("Collate"), GraphUI.cmdCollateNodes);
        m.addSeparator();
        m.addItem(Resources.getString("Find") + "...", GraphUI.cmdFind,
                  KeyEvent.VK_F);
        // m.addItem(Resources.getString("Show Graph") + "...", GraphUI.showGraph,
        // KeyEvent.VK_1);
        m.addItem(Resources.getString("SetBreakpoint"), GraphUI.cmdBreakpoint);

        if (this.getWindowMenu() != null) {
            this.getWindowMenu().setText(Resources.getString("Window"));
            mbar.add(this.getWindowMenu());
        }

        m = MenuUtils.addHelpMenu(mbar);
        if (Version.DEBUG) {
            JMenuItem item = new JMenuItem("Convert CatchAll Edges");
            item.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {

                    SingleDocumentWindow.this.getDocument().find(new NodeSearchFilter() {

                        @Override
                        public Collection<? extends SearchResult> match(Node n) {

                            if (n instanceof com.clt.diamant.graph.nodes.InputNode) {
                                ((com.clt.diamant.graph.nodes.InputNode) n).updateEdges(true);
                            }

                            return Collections.emptyList();
                        }
                    });
                }
            });
            m.add(item);
        }

        this.setJMenuBar(mbar);
        mbars.add(mbar);

        CmdMenuBar hiddenMenuBar = new CmdMenuBar(commander);
        CmdMenu hiddenMenu = hiddenMenuBar.addMenu("");
        GraphUI.initMenu(hiddenMenu);

        this.getLayeredPane().add(hiddenMenuBar);
        mbars.add(hiddenMenuBar);

        return mbars;
    }

    public boolean openSubWindows() {

        return this.openSubWindows;
    }

    private void setupColorMenu(GraphUI view) {

        if (this.colorMenu != null) {
            this.colorMenu.removeAll();
            ButtonGroup group = new ButtonGroup();
            for (String name : GraphUI.gNodeColors.keySet()) {
                if (view != null) {
                    JMenuItem colorMenuItem = view.createColorItem(name, null);
                    group.add(colorMenuItem);
                    this.colorMenu.add(colorMenuItem);
                } else {
                    JMenuItem item = new JMenuItem(Resources.getString(name));
                    item.setEnabled(false);
                    this.colorMenu.add(item);
                    group.add(item);
                }
            }
            JMenuItem item
                    = this.colorMenu.addItem(Resources.getString("Other") + "...",
                                             GraphUI.cmdColorOther);
            item.setEnabled(view != null);
        }
    }

    @Override
    public boolean menuItemState(int cmd) {

        if (this.runtime != null) {
            if (cmd == SingleDocumentWindow.cmdRun) {
                return true;
            } else {
                return false;
            }
        }

        switch (cmd) {
            case cmdSetup:
            case cmdExportVXML:
            case cmdEditDevices:
            case cmdResetDevices:
            case cmdRun:
            case cmdRunConfiguration:
            case cmdRunWithLog:
            case cmdDebug:
            case cmdWoz:
            case cmdDelay:
            case cmdValidate:
                return true;

            // case cmdCheckSamples:
            // return devices.size() > 0;
            case cmdPrint:
                return this.mainView != null;

            default:
                return super.menuItemState(cmd)
                        || ((this.mainView != null) && this.mainView.menuItemState(cmd));
        }
    }

    @Override
    public String menuItemName(int cmd, String oldName) {

        switch (cmd) {
            case cmdRun:
                return this.runtime != null ? Resources.getString("Stop") : Resources
                        .getString("Run");
            default:
                String s = super.menuItemName(cmd, oldName);
                if ((s != oldName) || (this.mainView == null)) {
                    return s;
                } else {
                    return this.mainView.menuItemName(cmd, oldName);
                }
        }
    }

    @Override
    public boolean doCommand(int cmd) {

        boolean cmdHandled = true;

        final SingleDocument doc = this.getDocument();

        try {
            switch (cmd) {
                case cmdExportVXML:
                    File vxml = new FileChooser().standardPutFile(this, "Dialog.vxml");
                    if (vxml != null) {
                        XMLWriter w = new XMLWriter(vxml);
                        IdMap uid_map = new IdMap(true);
                        try {
                            doc.exportVoiceXML(w, uid_map);
                        } finally {
                            w.close();
                        }
                    }
                    break;

                case cmdSetup:
                    this.showSetupDialog(null);
                    break;

                case cmdResetDevices:
                    for (Device d : doc.getDevices()) {
                        try {
                            d.reset();
                        } catch (Exception ignore) {
                        }
                    }
                    OptionPane.message(this, "Device reset successfully.");
                    break;

                case cmdEditDevices:
                    this.showSetupDialog("Devices");
                    break;

                case cmdPrint:
                    this.mainView.print();
                    break;

                case cmdRunConfiguration:
                    RunConfigurationDialog rcd = new RunConfigurationDialog(this,
                                                                            Resources.getString("RunConfiguration"), Preferences.getPrefs());
                    break;

                case cmdRun:
                case cmdRunWithLog:
                case cmdDebug:
                case cmdWoz:
                    if (this.runtime != null) {
                        synchronized (this.runtime) {
                            this.runtime.abort();
                            this.getDocument().closeDevices();
                            this.executionThread.interrupt();
                        }
                    } else {
                        try {
                            if (doc.connectDevices(new ConnectDialog(this), Preferences.getPrefs().getConnectionTimeout())) {
                                if (cmd == SingleDocumentWindow.cmdRun) {
                                    this.runtime = new Executer(this, false);
                                } else if (cmd == SingleDocumentWindow.cmdRunWithLog) {
                                    this.runtime = new Executer(this, true);
                                } else if (cmd == SingleDocumentWindow.cmdDebug) {
                                    // runtime = new WizardUI(this, false);
                                    this.runtime = new DebugUI(this);
                                } else {
                                    this.runtime = new WizardUI(this, true);
                                }

                                if (this.runtime.showSubdialogsDuringExecution()) {
                                    this.runtime.addGraphExecutionListener(new GraphExecutionListener() {
                                        Stack<GraphEditor> editors = new Stack<GraphEditor>();

                                        public void graphExecutionStarted(Graph g) {
                                            if (SingleDocumentWindow.this.openSubWindows()) {
                                                GraphEditor d = GraphEditorFactory.show(g.getOwner());
                                                if (d instanceof Window) {
                                                    ((Window) d).addWindowListener(new WindowAdapter() {

                                                        @Override
                                                        public void windowOpened(WindowEvent e) {

                                                            if (SingleDocumentWindow.this.runtime instanceof Window) {
                                                                ((Window) SingleDocumentWindow.this.runtime).toFront();
                                                                ((Window) SingleDocumentWindow.this.runtime).requestFocus();
                                                            }
                                                        }

                                                        @Override
                                                        public void windowClosed(WindowEvent e) {

                                                            if (SingleDocumentWindow.this.runtime instanceof Window) {
                                                                ((Window) SingleDocumentWindow.this.runtime).toFront();
                                                                ((Window) SingleDocumentWindow.this.runtime).requestFocus();
                                                            }
                                                        }
                                                    });
                                                }
                                                
                                                this.editors.push(d);
                                            } else {
                                                final Graph origGraph = SingleDocumentWindow.this.getMainView().getGraph();
                                                SingleDocumentWindow.this.setMainView(g);
                                                this.editors.push(new GraphEditor() {

                                                    public void showEditor() {

                                                        SingleDocumentWindow.this.showEditor();
                                                    }

                                                    public void closeEditor() {

                                                        SingleDocumentWindow.this.setMainView(origGraph);
                                                    }

                                                    public boolean isShowing() {

                                                        return SingleDocumentWindow.this.getMainView() == this.getGraphUI();
                                                    }

                                                    public GraphOwner getGraphOwner() {

                                                        return SingleDocumentWindow.this.getGraphOwner();
                                                    }

                                                    public GraphUI getGraphUI() {

                                                        return SingleDocumentWindow.this.getGraphUI();
                                                    }
                                                });
                                            }
                                        }

                                        public void graphExecutionStopped(Graph g) {

                                            if (this.editors.size() > 0) {
                                                this.editors.pop().closeEditor();
                                            }
                                        }
                                    });
                                }

                                this.executionThread = new Thread(new Runnable() {

                                    public void run() {

                                        try {
                                            ExecutionResult r = doc.run(SingleDocumentWindow.this, SingleDocumentWindow.this.runtime);

                                            if (r.getNode() == null) {
                                                if (r.getType() == ExecutionResult.INFORMATION) {
                                                    OptionPane.timedMessage(SingleDocumentWindow.this, r.getMessage(), Resources.getString("Message"), OptionPane.INFORMATION, 5000);
                                                } else {
                                                    OptionPane.message(SingleDocumentWindow.this, r.getMessage(), Resources.getString("Error"), OptionPane.ERROR);
                                                }
                                            } else {
                                                int result = OptionPane.showOptionDialog(SingleDocumentWindow.this, 
                                                        r.getMessage(),
                                                        r.getType() == ExecutionResult.INFORMATION ? Resources.getString("Message") : Resources.getString("Error"),
                                                        OptionPane.DEFAULT_OPTION, 
                                                        r.getType() == ExecutionResult.INFORMATION ? OptionPane.INFORMATION : OptionPane.ERROR,
                                                        null, 
                                                        new String[] { GUI.getString("OK"), Resources.getString("ShowNode") }, 
                                                        GUI.getString("OK"));
                                                
//                                                System.err.println(result); // debugging for https://github.com/dialogos-project/dialogos/issues/117
                                                
                                                if (result == 1) {
//                                                    System.err.println(r.getNode());
//                                                    System.err.println(r.getNode().getGraph());
//                                                    System.err.println(r.getNode().getGraph().getOwner());
//                                                    System.exit(0);
                                                    
                                                    GraphOwner owner = r.getNode().getGraph().getOwner();
                                                    
                                                    if ((owner == doc) && (SingleDocumentWindow.this.mainView != null)) {
                                                        SingleDocumentWindow.this.mainView.getSelectionModel().clear();
                                                    }
                                                    
                                                    GraphEditor editor = GraphEditorFactory.show(owner);
                                                    editor.getGraphUI().getSelectionModel().add(r.getNode());
                                                }
                                            }
                                        } catch (InvocationTargetException exn) {
                                            if (exn.getTargetException() instanceof UserCanceledException) {
                                                OptionPane.message(SingleDocumentWindow.this, Resources.getString("ExecutionStopped"));
                                            } else {
                                                OptionPane.error(SingleDocumentWindow.this, exn.getTargetException());
                                            }
                                        } catch (Exception exn) {
                                            OptionPane.error(SingleDocumentWindow.this, exn);
                                        }
                                        
                                        synchronized (SingleDocumentWindow.this.runtime) {
                                            SingleDocumentWindow.this.runtime = null;
                                            SingleDocumentWindow.this.executionThread = null;
                                        }
                                        SingleDocumentWindow.this.updateMenus();
                                    }
                                }, "SingleDocument Execution");
                                this.executionThread.start();
                            }
                        } catch (Exception exn) {
                            this.runtime = null;
                            this.executionThread = null;
                            doc.closeDevices();
                            OptionPane.error(this, exn);
                        }
                    }
                    break;

                case cmdDelay:
                    String s = OptionPane.edit(null, Resources.getString("PleaseEnterDelay"),
                                              Resources.getString("SetDelay"), String.valueOf(this.delay));
                    if (s != null) {
                        try {
                            this.delay = Integer.parseInt(s);
                            if (this.runtime != null) {
                                this.runtime.setDelay(this.delay);
                            }
                        } catch (NumberFormatException exn) {
                            this.getToolkit().beep();
                        }
                    }
                    break;

                case cmdValidate:
                    try {
                        final Collection<SearchResult> errors = new LinkedList<SearchResult>();

                        new ProgressDialog(this, 1000).run(new DefaultLongAction(Resources.format("ValidatingX", doc.getGraphName())) {

                            @Override
                            public void run(ProgressListener l) {

                                doc.validate(errors, l);
                            }
                        });
                        
                        if (errors.size() > 0) {
                            SearchResultsDialog.show(this, Resources
                                                     .getString("DocumentProblems"),
                                                     errors);
                        } else {
                            OptionPane.message(this, Resources.getString("DocumentValid"));
                        }
                    } catch (InvocationTargetException exn) {
                        OptionPane.error(this, exn.getTargetException());
                    }
                    
                    break;

                default:
                    cmdHandled = super.doCommand(cmd);
                    if (!cmdHandled && (this.mainView != null)) {
                        cmdHandled = this.mainView.doCommand(cmd);
                    }
                    break;
            }
        } catch (ThreadDeath d) {
            throw d;
        } catch (Exception t) {
            // This is the global exception handler for all the commands that
            // can be handled in this method.
            
            OptionPane.error(SingleDocumentWindow.this, t);

            System.gc();
            System.gc();
        }
        this.updateMenus();
        return cmdHandled;
    }

    @Override
    public void updateMenus() {

        super.updateMenus();

        if (this.toolbox != null) {
            this.toolbox.update();
        }

        if (this.subwin != null) {
            this.subwin.setSelected(this.openSubWindows());
        }
    }

    private void showSetupDialog(String selectedTab) {

        final SingleDocument doc = this.getDocument();

        JDialog setupDialog = new JDialog(this, Resources.getString("DialogSetup"), true);
        setupDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        JTabbedPane jtp = GUI.createTabbedPane();
        IconTabbedPaneUI ui = new IconTabbedPaneUI(SwingConstants.TOP);
        ui.setBackground(new Color(248, 248, 255));
        ui.setSelectionbackground(new Color(164, 192, 248));
        jtp.setUI(ui);
        jtp.addTab(Resources.getString("Devices"), Images.load("Devices.png"),
                   new ListEditor(
                           new ListEditor.Model() {

                       private List<Device> devices = new ArrayList<Device>(doc.getDevices());

                       public int getSize() {

                           return this.devices.size();
                       }

                       public Object getElementAt(int index) {

                           return this.devices.get(index);
                       }

                       @Override
                       public void editItemAt(Component parent, int index) {
                           DeviceEditor.editDevice(this.devices.get(index), parent);
                       }

                       @Override
                       public int addElement(Component parent) {
                           Device d = new Device();
                           if (DeviceEditor.editDevice(d, parent)) {
                               doc.getDevices().add(d);
                               this.devices.add(d);
                               return this.devices.indexOf(d);
                           } else {
                               return -1;
                           }
                       }

                       @Override
                       public boolean removeElement(Component parent, int index) {

                           Device d = this.devices.remove(index);
                           doc.getDevices().remove(d);
                           return true;
                       }
                   }, true));

        for (Plugin plugin : PluginLoader.getPlugins()) {
            JPanel p = new JPanel(new BorderLayout(6, 6));
            // p.setBorder(new EmptyBorder(8,8,8,8));
            p.add(new JLabel(plugin.getName() + ", " + Resources.getString("Version")
                    + " "
                    + plugin.getVersion()), BorderLayout.NORTH);
            p.add(this.getDocument().getPluginSettings(plugin.getClass())
                    .createEditor(),
                  BorderLayout.CENTER);
            jtp.addTab(plugin.getName(), plugin.getIcon(), p);
        }

        jtp.setTabPlacement(SwingConstants.LEFT);

        /*
     * jtp.setIconAt(0, GUI.loadImage("NewFile.png")); jtp.setIconAt(1,
     * GUI.loadImage("NewFileWizard.png")); for (int i = 0; i < 8; i++) {
     * jtp.addTab(Resources.getString("Tab " + (i + 2)), new JPanel()); } for
     * (int i = 0; i < 8; i++) { jtp.setIconAt(i + 2,
     * GUI.loadImage("NewFile.png")); }
         */
        // jtp.setTabPlacement(JTabbedPane.TOP);
        for (int i = 0; i < jtp.getTabCount(); i++) {
            ((JComponent) jtp.getComponentAt(i)).setBorder(BorderFactory
                    .createEmptyBorder(8, 8, 8,
                                       8));
            if (selectedTab != null) {
                if (Resources.getString(selectedTab).equals(jtp.getTitleAt(i))) {
                    jtp.setSelectedIndex(i);
                }
            }
        }

        setupDialog.setContentPane(jtp);
        setupDialog.pack();
        WindowUtils.setLocationRelativeTo(setupDialog, this);
        setupDialog.setVisible(true);
        this.setDirty(true);
    }

    @Override
    public boolean readyToClose(Saving saving) {

        // stop execution first
        if (this.runtime != null) {
            this.runtime.abort();
            this.getDocument().closeDevices();
        }

        return super.readyToClose(saving);
    }

    public GraphUI setMainView(Graph graph) {

        if (((graph == null) || (this.getMainView() == null)) ? true : graph != this.getMainView().getGraph()) {
            if (this.getMainView() != null) {
                this.contentPanel.removeAll();
                if (this.toolbox != null) {
                    this.toolbox.removePropertyChangeListener(this.getMainView());
                }
            }

            if (graph == null) {
                // mainView = null;
                if (this.header != null) {
                    this.header.clear();
                }
            } else {
                this.mainView = new GraphUI(graph);

                JScrollPane jsp = this.mainView.getScrollPane();
                // jsp.setBorder(null);
                this.contentPanel.setLayout(new BorderLayout());
                this.contentPanel.add(jsp, BorderLayout.CENTER);
                // contentPanel.add(mainView.getPropertyEditor(), BorderLayout.SOUTH);
                if (this.toolbox != null) {
                    this.toolbox.addPropertyChangeListener(this.mainView);
                    this.toolbox.notifyState();
                }
                if (this.header != null) {
                    LinkedList<Object> graphs = new LinkedList<Object>();
                    Graph g = graph;
                    while (g != null) {
                        graphs.addFirst(g);
                        g = g.getSuperGraph();
                    }
                    this.header.setText(graphs.toArray(), true);
                }

                for (ComponentListener l : this.viewListeners) {
                    l.componentShown(new ComponentEvent(jsp, ComponentEvent.COMPONENT_SHOWN));
                }
            }
            this.setupColorMenu(this.mainView);

            if (this.isShowing()) {
                this.invalidate();
                this.validate();
                this.repaint();
            }
        }

        return this.mainView;
    }

    public GraphUI getGraphUI() {

        return this.getMainView();
    }

    public GraphOwner getGraphOwner() {

        return this.getDocument();
    }

    public GraphUI getMainView() {

        return this.mainView;
    }

    public void addViewListener(ComponentListener l) {

        this.viewListeners.add(l);
    }

    public void removeViewListener(ComponentListener l) {

        this.viewListeners.remove(l);
    }

}
