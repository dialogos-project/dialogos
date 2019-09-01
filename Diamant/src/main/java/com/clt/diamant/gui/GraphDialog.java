package com.clt.diamant.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

import com.clt.diamant.Main;
import com.clt.diamant.MenuUtils;
import com.clt.diamant.Preferences;
import com.clt.diamant.Resources;
import com.clt.diamant.graph.GraphOwner;
import com.clt.diamant.graph.Node;
import com.clt.diamant.graph.Procedure;
import com.clt.diamant.graph.VisualGraphElement;
import com.clt.diamant.graph.search.SearchResult;
import com.clt.diamant.graph.search.SearchResultsDialog;
import com.clt.diamant.graph.ui.GraphUI;
import com.clt.event.ProgressListener;
import com.clt.gui.Commands;
import com.clt.gui.FileChooser;
import com.clt.gui.ManagedFrame;
import com.clt.gui.OptionPane;
import com.clt.gui.ProgressDialog;
import com.clt.gui.WindowUtils;
import com.clt.gui.menus.CmdMenu;
import com.clt.gui.menus.CmdMenuBar;
import com.clt.gui.menus.MenuCommander;
import com.clt.gui.menus.MenuOwner;
import com.clt.util.DefaultLongAction;

/**
 *
 *
 * @author Daniel Bobbert
 * @version 1.0
 */
public class GraphDialog extends ManagedFrame implements GraphEditor, MenuCommander, MenuOwner {

    private static final int cmdRename = 590;
    private static final int cmdComment = 591;
    private static final int cmdValidate = 592;

    private Collection<CmdMenuBar> mbars;

    private GraphUI graph;
    private GraphOwner owner;
    private DefaultToolbox toolbox;
    private ManagedFrame mainWindow;
    private JMenu windowMenu = null;

    GraphDialog(final GraphOwner owner, GraphUI graph, boolean modal) {

        this.owner = owner;
        this.graph = graph;

        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosed(WindowEvent evt) {

                GraphEditorFactory.unregister(owner);
            }

            @Override
            public void windowActivated(WindowEvent evt) {

                GraphDialog.this.updateMenus();
            }
        });

        this.setupTitle();

        JPanel content = new JPanel(new BorderLayout());

        this.toolbox = new DefaultToolbox(null);
        if (Preferences.getPrefs().showToolbox.getValue()) {
            content.add(this.toolbox, BorderLayout.NORTH);
        }

        content.add(graph.getScrollPane(), BorderLayout.CENTER);
        /*
     * if (Preferences.getShowSubgraphPath()) { StringBuffer path =
     * owner.getOwnedGraph().graphPath(); content.add(new
     * WindowHeader(path.toString(), WindowHeader.LEFT), BorderLayout.NORTH); }
         */
        this.setContentPane(content);

        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        GraphOwner mainOwner = owner;
        while (mainOwner.getSuperGraph() != null) {
            mainOwner = mainOwner.getSuperGraph().getOwner();
        }

        this.mainWindow = (ManagedFrame) GraphEditorFactory.get(mainOwner);
        if (this.mainWindow == null) {
            throw new IllegalStateException("Could not find the main window");
        }

        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {

                GraphDialog.this.doCommand(Commands.cmdClose);
            }
        });

        this.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {

                if (e.getKeyCode() == KeyEvent.VK_ALT) {
                    GraphDialog.this.getGraphUI().adjustCursor(DefaultToolbox.HAND);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

                if (e.getKeyCode() == KeyEvent.VK_ALT) {
                    GraphDialog.this.getGraphUI().adjustCursor(
                            GraphDialog.this.toolbox.getTool());
                }
            }
        });

        if (modal || true) {
            this.mbars = this.initMenus(this.getRootPane(), this);
        } else {
            this.mbars = new ArrayList<CmdMenuBar>();
        }
        this.updateMenus();

        this.pack();

        Dimension screen = WindowUtils.getScreenSize();
        screen.width = Math.min(screen.width - 80, this.getWidth());
        screen.height = Math.min(screen.height - 80, this.getHeight());
        this.setSize(screen);

        WindowUtils.setLocationRelativeTo(this, null);
    }

    public void showEditor() {

        this.setVisible(true);
        this.toFront();
    }

    public void closeEditor() {

        this.doCommand(Commands.cmdClose);
    }

    private void setupTitle() {

        if (Preferences.getPrefs().showSubgraphPath.getValue()) {
            this.setTitle(this.owner.getOwnedGraph().graphPath(true).toString());
        } else {
            this.setTitle(this.owner.getGraphName());
        }
    }

    @Override
    public void addNotify() {

        super.addNotify();
        this.toolbox.addPropertyChangeListener(this.graph);
        this.toolbox.notifyState();
    }

    @Override
    public void removeNotify() {

        this.toolbox.removePropertyChangeListener(this.graph);
        super.removeNotify();
    }

    public void updateMenus() {

        for (CmdMenuBar mbar : this.mbars) {
            mbar.updateMenus();
        }
    }

    @Override
    public ManagedFrame getMainWindow() {

        return this.mainWindow;
    }

    @Override
    public JMenu getWindowMenu() {

        return this.windowMenu;
    }

    @Override
    protected void rebuildWindowMenu() {

        super.rebuildWindowMenu();

        JMenu menu = this.getWindowMenu();
        if (menu != null) {
            Main.appendClientMenu(menu);
        }
    }

    public GraphUI getGraphUI() {

        return this.graph;
    }

    public GraphOwner getGraphOwner() {

        return this.owner;
    }

    public boolean doCommand(int cmd) {

        boolean cmdHandled = true;

        try {
            switch (cmd) {
                case cmdClose:
                    this.setVisible(false);
                    this.dispose();
                    break;

                case cmdRename:
                    this.graph.getGraph().rename(this);
                    this.setupTitle();
                    break;

                case cmdComment:
                    if (this.owner instanceof Node) {
                        Node n = (Node) this.owner;
                        Map<String, Object> t = new Hashtable<String, Object>();
                        if (n.getProperty(VisualGraphElement.COMMENT) != null) {
                            t.put(VisualGraphElement.COMMENT, n
                                    .getProperty(VisualGraphElement.COMMENT));
                        }
                        if (OptionPane.confirm(this, NodePropertiesDialog
                                .createTextArea(t, VisualGraphElement.COMMENT), Resources
                                .getString("Comment"), OptionPane.OK_CANCEL_OPTION) == OptionPane.OK) {
                            if (t.containsKey(VisualGraphElement.COMMENT)) {
                                n.setProperty(VisualGraphElement.COMMENT, t
                                        .get(VisualGraphElement.COMMENT));
                            }
                        }
                    }
                    break;

                case cmdImport:
                    this.graph.getGraph().importGraph(this.graph);
                    break;

                case cmdExport:
                    try {
                        File f = new FileChooser().standardPutFile(this, this.owner
                                .getGraphName());
                        if (f != null) {
                            this.owner.export(this.owner.getOwnedGraph(), f);
                        }
                    } catch (Exception exn) {
                        exn.printStackTrace();
                        OptionPane.error(this, new String[]{
                            Resources.format("CouldNotExportGraph", this.owner
                            .getGraphName()), exn.toString()});
                    }
                    break;

                case cmdValidate:
                    try {
                        final Collection<SearchResult> errors
                                = new LinkedList<SearchResult>();

                        new ProgressDialog(this, 1000).run(new DefaultLongAction(
                                Resources.format("ValidatingX", this.owner
                                        .getGraphName())) {

                            @Override
                            public void run(ProgressListener l) {

                                GraphDialog.this.owner.getOwnedGraph().validate(errors, l);
                            }
                        });

                        if (errors.size() > 0) {
                            // Document.showErrors(this,
                            // Resources.getString("DocumentProblems"), errors,
                            // warnings);
                            SearchResultsDialog.show(null, Resources
                                    .getString("DocumentProblems"), errors);
                        } else {
                            OptionPane.message(this, Resources
                                    .getString("DocumentValid"));
                        }
                    } catch (InvocationTargetException exn) {
                        OptionPane.error(this, exn.getTargetException());
                    }
                    break;

                case cmdPrint:
                    this.graph.print();
                    break;

                default:
                    cmdHandled = this.graph.doCommand(cmd);
                    break;
            }
        } catch (ThreadDeath d) {
            throw d;
        } catch (Throwable t) {
            System.gc();
            System.runFinalization();
            t.printStackTrace();
            OptionPane.error(this, new String[]{
                Resources.getString("CouldNotComplete"), t.toString()});
        }

        this.updateMenus();
        return cmdHandled;
    }

    public String menuItemName(int cmd, String oldName) {

        switch (cmd) {
            case cmdMenu:
            case cmdRename:
            case cmdImport:
            case cmdExport:
            case cmdClose:
            case cmdPrint:
            case cmdComment:
            case cmdValidate:
                return oldName;
            default:
                return this.graph.menuItemName(cmd, oldName);
        }
    }

    public boolean menuItemState(int cmd) {

        switch (cmd) {
            case cmdMenu:
            case cmdExport:
            case cmdClose:
            case cmdPrint:
            case cmdValidate:
                return true;

            case cmdRename:
            case cmdImport:
            case cmdComment:
                return !this.getGraphUI().isReadOnly();

            default:
                return this.graph.menuItemState(cmd);
        }
    }

    /**
     * Adds a JMenubar to the rootpane of DialogOS.
     *
     * @param rootpane Reference on the rootpane.
     */
    private Collection<CmdMenuBar> initMenus(JRootPane rootpane,
            MenuCommander commander) {

        Collection<CmdMenuBar> mbars = new ArrayList<CmdMenuBar>();

        CmdMenuBar mbar = new CmdMenuBar(commander);
        CmdMenu m;

        m
                = mbar.addMenu(this.owner.getOwnedGraph() instanceof Procedure ? Resources
                        .getString("Procedure") : Resources.getString("Subdialog"));
        m.addItem(Resources.getString("Close"), Commands.cmdClose,
                KeyEvent.VK_W);
        m.addItem(Resources.getString("Rename") + "...", GraphDialog.cmdRename);
        m.addItem(Resources.getString("Comment") + "...", GraphDialog.cmdComment);
        // m.addItem(Resources.getString("Import") + "...", Commands.cmdImport);
        // m.addItem(Resources.getString("Export") + "...", Commands.cmdExport);
        m.addSeparator();
        if (this.owner.getOwnedGraph() instanceof Procedure) {
            m.addItem(Resources.getString("Parameters") + "...",
                    GraphUI.cmdEditParameters);
            m.addItem(Resources.getString("ReturnVariables") + "...",
                    GraphUI.cmdEditReturnVariables);
            m.addSeparator();
        }
        m.addItem(Resources.getString("Print") + "...", Commands.cmdPrint,
                KeyEvent.VK_P);

        m = mbar.addMenu(Resources.getString("EditMenu"));
        m.addItem(Resources.getString("Undo"), Commands.cmdUndo, KeyEvent.VK_Z);
        m.addItem(Resources.getString("Redo"), Commands.cmdRedo, KeyStroke
                .getKeyStroke(KeyEvent.VK_Z, (m.getToolkit()
                        .getMenuShortcutKeyMask() | Event.SHIFT_MASK)));
        m.addSeparator();
        m.addItem(Resources.getString("Cut"), Commands.cmdCut, KeyEvent.VK_X);
        m.addItem(Resources.getString("Copy"), Commands.cmdCopy, KeyEvent.VK_C);
        m.addItem(Resources.getString("Paste"), Commands.cmdPaste,
                KeyEvent.VK_V);
        m.addItem(Resources.getString("Delete"), Commands.cmdDelete);
        m.addSeparator();
        m.addItem(Resources.getString("SelectAll"), Commands.cmdSelectAll,
                KeyEvent.VK_A);

        m = mbar.addMenu(Resources.getString("GraphMenu"));

        m.addItem(Resources.getString("Variables") + "...",
                GraphUI.cmdEditVariables);
        m.addItem(Resources.getString("Functions") + "...",
                GraphUI.cmdEditFunctions);
        m.addItem(Resources.getString("GroovyFunctions") + "...",
                GraphUI.cmdEditGroovyFunctions);
        m.addItem("Groovy " + Resources.getString("Variables") + "...",
                GraphUI.cmdEditGroovyVariables);
        m.addItem(Resources.getString("Grammars") + "...",
                GraphUI.cmdEditGrammars);
        if (this.owner.getOwnedGraph().supportsHandlers()) {
            m.addItem(Resources.getString("InputHandlers") + "...",
                    GraphUI.cmdEditHandlers);
        }
        m.addItem(Resources.getString("CanvasSize") + "...",
                GraphUI.cmdCanvasSize);
        m.addSeparator();
        m.addItem(Resources.getString("Validate"), GraphDialog.cmdValidate,
                KeyEvent.VK_K);

        m = mbar.addMenu(Resources.getString("NodeMenu"));
        CmdMenu colorMenu = m.addSubMenu(Resources.getString("Color"),
                GraphUI.cmdColorMenu);
        /*
     * colorMenu.addItem(Resources.getString("Red"), GraphUI.cmdColorRed);
     * colorMenu.addItem(Resources.getString("Green"), GraphUI.cmdColorGreen);
     * colorMenu.addItem(Resources.getString("Blue"), GraphUI.cmdColorBlue);
     * colorMenu.addItem(Resources.getString("Yellow"),
     * GraphUI.cmdColorYellow); colorMenu.addItem(Resources.getString("Gray"),
     * GraphUI.cmdColorGray);
         */
        ButtonGroup group = new ButtonGroup();
        for (String name : GraphUI.gNodeColors.keySet()) {
            JMenuItem colorMenuItem = this.graph.createColorItem(name, null);
            group.add(colorMenuItem);
            colorMenu.add(colorMenuItem);
        }
        colorMenu.addSeparator();
        colorMenu.addItem(Resources.getString("Other") + "...",
                GraphUI.cmdColorOther);
        m.addSeparator();
        m.addItem(Resources.getString("Align") + "...", GraphUI.cmdAlign,
                KeyEvent.VK_L);
        m.addSeparator();
        m.addItem(Resources.getString("Group"), GraphUI.cmdGroup,
                KeyEvent.VK_G);
        m.addItem(Resources.getString("Ungroup"), GraphUI.cmdUngroup,
                KeyStroke.getKeyStroke(KeyEvent.VK_G, (m.getToolkit()
                        .getMenuShortcutKeyMask() | Event.SHIFT_MASK)));
        m.addItem(Resources.getString("Collate"), GraphUI.cmdCollateNodes);
        m.addSeparator();
        m.addItem(Resources.getString("Find") + "...", GraphUI.cmdFind,
                KeyEvent.VK_F);
        // m.addItem(Resources.getString("Show Graph") + "...", GraphUI.showGraph,
        // KeyEvent.VK_2);
        m.addItem(Resources.getString("SetBreakpoint"),
                GraphUI.cmdBreakpoint);

        this.windowMenu = new JMenu(Resources.getString("Window"));
        mbar.add(this.windowMenu);

        MenuUtils.addHelpMenu(mbar, graph.getGraph().getEnvironment(false));

        rootpane.setJMenuBar(mbar);
        mbars.add(mbar);

        CmdMenuBar hiddenMenuBar = new CmdMenuBar(commander);
        CmdMenu hiddenMenu = hiddenMenuBar.addMenu("");
        hiddenMenu.addItem(Resources.getString("Delete"),
                Commands.cmdDelete, KeyStroke.getKeyStroke(
                        KeyEvent.VK_BACK_SPACE, 0));
        hiddenMenu.addItem(Resources.getString("Delete"),
                Commands.cmdDelete, KeyStroke.getKeyStroke(
                        KeyEvent.VK_DELETE, 0));

        hiddenMenu.addItem(Resources.getString("Up"), GraphUI.cmdUp, KeyStroke
                .getKeyStroke(KeyEvent.VK_UP, 0));
        hiddenMenu.addItem(Resources.getString("Down"), GraphUI.cmdDown,
                KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0));
        hiddenMenu.addItem(Resources.getString("Left"), GraphUI.cmdLeft,
                KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0));
        hiddenMenu.addItem(Resources.getString("Right"), GraphUI.cmdRight,
                KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0));

        rootpane.getLayeredPane().add(hiddenMenuBar);
        mbars.add(hiddenMenuBar);

        return mbars;
    }
}
