package com.clt.diamant;

import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import com.clt.dialog.client.GUIClientWindow;
import com.clt.dialogos.plugin.PluginLoader;
import com.clt.diamant.gui.DocumentWindow;
import com.clt.diamant.gui.LogDocumentWindow;
import com.clt.diamant.gui.MultiDocumentWindow;
import com.clt.diamant.gui.SingleDocumentWindow;
import com.clt.event.ProgressListener;
import com.clt.gui.ApplicationMenuPanel;
import com.clt.gui.Commands;
import com.clt.gui.FileChooser;
import com.clt.gui.GUI;
import com.clt.gui.Images;
import com.clt.gui.OptionPane;
import com.clt.gui.WindowUtils;
import com.clt.gui.menus.AbstractMenuCommander;
import com.clt.gui.menus.CmdCheckBoxMenuItem;
import com.clt.gui.menus.CmdMenuBar;
import com.clt.gui.menus.MenuCommander;
import com.clt.gui.menus.MenuOwner;
import com.clt.io.FileFormatException;
import com.clt.mac.ApplicationUtils;
import com.clt.mac.RequiredEventHandler;
import com.clt.util.AbstractAction;
import com.clt.util.Misc;
import com.clt.util.Platform;
import com.clt.xml.XMLFormatException;

public class Main implements MenuCommander, MenuOwner, Commands {

    static final int cmdNewEx = Commands.cmdApplication + 1;
    static final int cmdHelpFunctions = Commands.cmdApplication + 2;
    static final int cmdTest = Commands.cmdApplication + 3;

    private static Window projectStartupWindow = null;
    private static JMenu projectStartupMRU = null;
    private static com.clt.dialog.client.GUIClientFactory clients = null;

    private DocumentWindow<?> currentDocument = null;
    private static Collection<DocumentWindow<?>> documents = new ArrayList<DocumentWindow<?>>();

    private final FileChooser fileChooser = new FileChooser(Preferences.getPrefs().getLastUsedFile());

    private RequiredEventHandler systemEventHandler = new RequiredEventHandler(true, true) {

        @Override
        public boolean handleQuit() {

            if (Main.this.menuItemState(Commands.cmdQuit) == true) {
                if (Main.this.doCommand(Commands.cmdQuit)) {
                    return Main.documents.size() == 0;
                }
            }
            return false;
        }

        @Override
        public boolean handleAbout() {

            if (Main.this.menuItemState(Commands.cmdAbout) == true) {
                return Main.this.doCommand(Commands.cmdAbout);
            }
            return false;
        }

        @Override
        public boolean handleReOpenApplication() {

            if (Main.documents.size() == 0) {
                Main.this.openApp(null);
            }
            return true;
        }

        @Override
        public boolean handleOpenFile(File f) {

            boolean result = false;
            if (Main.this.menuItemState(Commands.cmdOpen) == true) {
                result = Main.this.openDocument(f) != null;
            }
            Main.this.closeProjectFrameOnSuccess();
            return result;
        }

        @Override
        public boolean handlePrintFile(File f) {

            if (Main.this.menuItemState(Commands.cmdOpen) == true) {
                DocumentWindow<?> d = Main.this.openDocument(f);
                if ((d != null) && (Main.this.menuItemState(Commands.cmdPrint) == true)) {
                    return Main.this.doCommand(Commands.cmdPrint);
                }
            }

            return false;
        }

        @Override
        public boolean handlePreferences() {

            return Main.this.doCommand(Commands.cmdPreferences);
        }
    };

    public Main(File appDir) {

        ApplicationUtils.registerEventHandler(this.systemEventHandler);

    }

    private void closeProjectFrameOnSuccess() {

        if ((Main.documents.size() > 0) && (Main.projectStartupWindow != null)) {
            Main.projectStartupWindow.setVisible(false);
        }
    }

    public Window getProjectStartupWindow() {

        if (Main.projectStartupWindow == null) {
            JFrame projectStartupFrame = new JFrame(Version.PRODUCT_NAME);

            @SuppressWarnings("unused")
            AbstractAction newEmptyProject = new AbstractAction(Resources
                    .getString("EmptyDialog"), Images.load("NewFile.png")) {

                @Override
                public void run() {

                    Main.this.createNewDocument(false);
                    Main.this.closeProjectFrameOnSuccess();
                }
            };
            AbstractAction newProject = new AbstractAction(Resources
                    .getString("CreateNewDialog")
                    + "...", Images.load("NewFileWizard.png")) {

                @Override
                public void run() {

                    Main.this.createNewDocument(false);
                    Main.this.closeProjectFrameOnSuccess();
                }
            };
            newProject.setAccelerator(KeyEvent.VK_N);
            AbstractAction openProject = new AbstractAction(Resources
                    .getString("OpenDialog")
                    + "...", Images.load("OpenFile.png")) {

                @Override
                public void run() {

                    Main.this.doCommand(Commands.cmdOpen);
                    Main.this.closeProjectFrameOnSuccess();
                }
            };
            openProject.setAccelerator(KeyEvent.VK_O);
            final AbstractAction quit = new AbstractAction(Resources
                    .getString("Quit")) {

                @Override
                public void run() {

                    Main.this.doCommand(Commands.cmdQuit);
                }
            };
            quit.setAccelerator(KeyEvent.VK_Q);

            projectStartupFrame.setContentPane(new ApplicationMenuPanel(
                    Version.PRODUCT_NAME, Images.load("AppIconLarge.png")
                            .getImage(), new Action[]{ /* newEmptyProject, */
                        newProject, openProject}, new Action[]{quit}));

            projectStartupFrame
                    .setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            projectStartupFrame.addWindowListener(new WindowAdapter() {

                @Override
                public void windowClosing(WindowEvent evt) {

                    Main.this.doCommand(Commands.cmdQuit);
                }
            });
            projectStartupFrame.setResizable(false);
            projectStartupFrame.pack();

            // update menu bar each time
            CmdMenuBar mbar = new CmdMenuBar();
            JMenu menu = new JMenu(Resources.getString("File"));
            // menu.add(new JMenuItem(newEmptyProject));
            menu.add(new JMenuItem(newProject));
            menu.add(new JMenuItem(openProject));

            Main.projectStartupMRU = new JMenu(Resources.getString("MRU"));
            menu.add(Main.projectStartupMRU);
            MenuUtils.addMRU(Main.projectStartupMRU, this.systemEventHandler);

            if (Platform.showQuitMenuItem()) {
                menu.addSeparator();
                menu.add(new JMenuItem(quit));
            }
            mbar.add(menu);

            MenuUtils.addHelpMenu(mbar);

            mbar.updateMenus();

            projectStartupFrame.setJMenuBar(mbar);
            Main.projectStartupWindow = projectStartupFrame;
        }

        Main.projectStartupMRU.removeAll();
        MenuUtils.addMRU(Main.projectStartupMRU, this.systemEventHandler);

        return Main.projectStartupWindow;
    }

    public void openApp(final File model) {

        SwingUtilities.invokeLater(new Runnable() {

            private void openInitialWindow() {
                WindowUtils.setLocation(Main.this.getProjectStartupWindow(), WindowUtils.CENTER_ON_SCREEN);
                Main.this.getProjectStartupWindow().setVisible(true);
            }

            public void run() {

                synchronized (Main.this) {
                    if (Main.this.currentDocument == null) {
                        if ((model != null) && model.isFile()) {
                            if (Main.this.openDocument(model) == null) {
                                this.openInitialWindow();
                            }
                        } else {
                            this.openInitialWindow();
                        }
                    }
                }
            }
        });
    }

    /**
     * Adds a documentWindow to the list of currently displayed document, and
     * register listeners to it.
     *
     * @param d DocumentWindow to be added to the list of listeners
     * @return The document window passed as argument
     */
    private DocumentWindow<?> addDocument(DocumentWindow<?> d) {

        d.addWindowListener(new WindowAdapter() {

            @Override
            public void windowActivated(final WindowEvent e) {

                if (e.getWindow() instanceof DocumentWindow) {
                    Main.this.currentDocument = (DocumentWindow) e.getWindow();
                    Main.this.updateMenus();
                }
            }

            @Override
            public void windowDeactivated(WindowEvent evt) {

                if (Main.this.currentDocument == evt.getWindow()) {
                    Main.this.currentDocument = null;
                }
            }

            @Override
            public void windowClosing(WindowEvent evt) {

                if (evt.getWindow() instanceof DocumentWindow) {
                    Main.this.closeDocument((DocumentWindow) evt.getWindow(),
                            DocumentWindow.Saving.ASK_SAVE_CHANGES, false);
                }
            }
        });

        Main.documents.add(d);

        return d;
    }

    private DocumentWindow<?> openDocument() {

        File f = this.fileChooser.standardGetFile(getProjectStartupWindow());
        if (f != null) {
            Preferences.getPrefs().setLastUsedFile(f);
        }
        return this.openDocument(f);
    }

    private DocumentWindow<?> openDocument(File f) {

        return this.openDocument(null, f, null);
    }

    private DocumentWindow<?> openDocument(final DocumentWindow<?> d,
            final File f) {

        return this.openDocument(d, f, null);
    }

    /**
     * Opens a dialog-file. If that file is already opened, its window is raised
     * on the top.
     *
     * @param d reference on the document window.
     * @param f File to be opened.
     * @return A new document window displaying the opened file.
     */
    public DocumentWindow<?> openDocument(final DocumentWindow<?> d,
            final File f, ProgressListener progress) {

        DocumentWindow<?> theDoc = null;

        if (f != null) {
            if (d == null) {
                for (DocumentWindow<?> doc : Main.documents) {
                    if (Misc.equals(doc.getDocument().getFile(), f)) {
                        doc.toFront();
                        return doc;
                    }
                }
            }

            try {
                if (d != null) {
                    GUI.setWaitCursor(d);
                }

                Document oldDoc = d == null ? null : d.getDocument();
                Document newDoc = new DocumentLoader(oldDoc).load(f, progress);
                // oldDoc=null;
                if (oldDoc != newDoc) {
                    theDoc = this.createWindow(newDoc);
                    this.addDocument(theDoc);
                } else {
                    theDoc = d;
                }

                theDoc.setDirty(false);
                theDoc.setVisible(true);

                if (d != null) {
                    GUI.setDefaultCursor(d);
                }
            } catch (ThreadDeath death) {
                throw death;
            } catch (Throwable exn) {
                if (d != null) {
                    GUI.setDefaultCursor(d);
                }

                if ((theDoc != null) && (d == null)) {
                    theDoc.dispose();
                    theDoc = null;
                }

                exn.printStackTrace();
                if ((exn instanceof FileFormatException)
                        || (exn instanceof XMLFormatException)) {
                    OptionPane.message(null, new String[]{
                        Resources.getString("CouldNotLoadXML"),
                        exn.getLocalizedMessage()}, Resources
                            .getString("IOError"), OptionPane.ERROR);
                } else {
                    OptionPane.message(null,
                            new String[]{Resources.getString("CouldNotLoad"),
                                exn.toString()}, Resources
                            .getString("IOError"), OptionPane.ERROR);
                }
            }
        }

        return theDoc;
    }

    /**
     * Creates a new window according to the type of the document passed as
     * parameter. If the document has the type MultiDocument, a
     * MultidocumentWindow is returned, if it has the type LogDocument, a
     * LogDocumentWindow is returned and if it has the type SingleDocument, a
     * SingleDocumentWindow is returned. If none of them applies, an
     * IllegarlArgumentException is raised.
     *
     * @param d Document for which a window should be created.
     * @exception IllegalArgumentException if the type of the document was
     * neither MultiDocument, nor LogDocument, nor SingleDocument.
     * @return a DocumentWindow, according to the type of the window.
     */
    private DocumentWindow<?> createWindow(Document d) {

        if (d instanceof MultiDocument) {
            return new MultiDocumentWindow((MultiDocument) d, this,
                    this.systemEventHandler);
        } else if (d instanceof LogDocument) {
            return new LogDocumentWindow((LogDocument) d, this,
                    this.systemEventHandler);
        } else if (d instanceof SingleDocument) {
            return new SingleDocumentWindow<SingleDocument>((SingleDocument) d,
                    this, this.systemEventHandler, false);
        } else {
            throw new IllegalArgumentException("Unknown document type: "
                    + d.getDocumentType());
        }
    }

    private boolean closeDocument(DocumentWindow<?> d,
            DocumentWindow.Saving state, boolean quitting) {

        if (d.readyToClose(state)) {
            Main.documents.remove(d);
            d.dispose();
            if ((Main.documents.size() == 0) && !quitting) {
                MenuUtils.hideHelp();

                Window w = this.getProjectStartupWindow();
                WindowUtils.setLocation(w, WindowUtils.CENTER_ON_SCREEN);
                w.setVisible(true);
            }
            return true;
        } else {
            return false;
        }
    }

    private void revertDocument(DocumentWindow<?> d) {

        File f = d.getDocument().getFile();
        if (f != null) {
            int result = OptionPane.showOptionDialog(d, new String[]{
                Resources.format("RevertChangesToFile", f.getName()),
                Resources.getString("RevertChangesInfo")}, Resources
                    .getString("RevertChanges?"), OptionPane.OK_CANCEL_OPTION,
                    OptionPane.WARNING, null, new String[]{
                        Resources.getString("RevertButton"),
                        Resources.getString("Cancel")}, Resources
                    .getString("Cancel"));
            if (result == OptionPane.OK) {
                GUI.setWaitCursor(d);
                try {
                    this.openDocument(d, f);
                    GUI.setDefaultCursor(d);
                } catch (Exception exn) {
                    exn.printStackTrace();
                    GUI.setDefaultCursor(d);
                    OptionPane.message(null, Resources
                            .getString("CouldNotReopen"), Resources
                            .getString("IOError"), OptionPane.ERROR);
                    this.closeDocument(d, DocumentWindow.Saving.DONT_SAVE_CHANGES,
                            false);
                }
            }
        }
    }

    public void updateMenus() {

        if (this.currentDocument != null) {
            this.currentDocument.updateMenus();
        }
    }

    public boolean menuItemState(int cmd) {

        switch (cmd) {
            case cmdMenu:
            case cmdAbout:
            case cmdNew:
            case cmdOpen:
            case cmdQuit:
            case cmdPreferences:
                return true;

            case cmdNewEx:
                return true;

            case cmdHelpFunctions:
            case cmdTest:
                return true;

            case cmdClose:
                return this.currentDocument != null;

            case cmdRevert:
                return this.currentDocument != null
                        ? (this.currentDocument.getFile() != null)
                        && this.currentDocument.isDirty()
                        && !this.currentDocument.getDocument().isReadOnly() : false;

            default:
                if (this.currentDocument != null) {
                    return this.currentDocument.menuItemState(cmd);
                } else {
                    return false;
                }
        }
    }

    public String menuItemName(int cmd, String oldName) {

        switch (cmd) {
            case cmdNew:
            case cmdClose:
            case cmdOpen:
            case cmdQuit:
                return oldName;

            default:
                if (this.currentDocument != null) {
                    return this.currentDocument.menuItemName(cmd, oldName);
                } else {
                    return oldName;
                }
        }
    }

    boolean done = false;

    private boolean createNewDocument(boolean wizard) {

        Document doc;
        if (wizard) {
            doc = DocumentAssistant.createDocument();
        } else {
            doc = new SingleDocument();
        }

        if (doc != null) {
            DocumentWindow<?> window = this.createWindow(doc);
            this.addDocument(window);
            window.setVisible(true);
            return true;
        } else {
            return false;
        }
    }

    public boolean doCommand(int cmd) {

        if (this.menuItemState(cmd) == false) {
            return true; // we handled the command by ignoring it because it
            // was not available
        }

        // assume the event will be handled.
        boolean result = true;

        try {
            switch (cmd) {
                case cmdAbout:
                    Version.showAboutDialog();
                    break;

                case cmdNew:
                    this.createNewDocument(false);
                    break;

                case cmdNewEx:
                    this.addDocument(this.createWindow(new MultiDocument())).setVisible(
                            true);
                    break;

                case cmdOpen:
                    this.openDocument();
                    break;

                case cmdClose:
                    this.closeDocument(this.currentDocument,
                            DocumentWindow.Saving.ASK_SAVE_CHANGES, false);
                    break;

                case cmdRevert:
                    this.revertDocument(this.currentDocument);
                    break;

                case cmdPreferences:
                    Preferences.edit(null);
                    for (DocumentWindow<?> d : Main.documents) {
                        d.repaint();
                    }
                    break;

                case cmdQuit:
                    if ((this.currentDocument == null) && (Main.documents.size() > 0)) {
                        DocumentWindow<?> d = Main.documents.iterator().next();
                        d.toFront();
                        this.currentDocument = d;
                    }
                    while (Main.documents.size() > 0 ? this.closeDocument(
                            this.currentDocument,
                            DocumentWindow.Saving.ASK_SAVE_CHANGES, true) : false) {
                        if (Main.documents.size() > 0) {
                            DocumentWindow<?> d = Main.documents.iterator().next();
                            d.toFront();
                            this.currentDocument = d;
                        }
                    }
                    if (Main.documents.size() == 0) {
                        Main.exit(0);
                    }
                    break;

                default:
                    if (this.currentDocument != null) {
                        result = this.currentDocument.doCommand(cmd);
                    } else {
                        result = false; // could not handle command
                    }
                    break;
            }
        } catch (ThreadDeath d) {
            throw d;
        } catch (Throwable t) {
            System.gc();
            System.runFinalization();
            System.gc();
            System.runFinalization();
            t.printStackTrace();
            OptionPane.error(this.currentDocument, new String[]{
                Resources.getString("CouldNotComplete"), t.toString()});
        }

        this.updateMenus();

        return result;
    }

    public static void exit(int state) {

        if (Main.projectStartupWindow != null) {
            Main.projectStartupWindow.dispose();
        }

        Preferences.save(null);

        if (Main.clients != null) {
            Main.clients.quit(true);
        }

        PluginLoader.terminatePlugins();

        System.exit(state);
    }

    public static void appendClientMenu(JMenu menu) {

        if (Main.clients != null) {
            Collection<GUIClientWindow> windows = Main.clients.getWindows();
            if (!windows.isEmpty()) {
                menu.addSeparator();
                for (final GUIClientWindow window : windows) {
                    menu.add(new CmdCheckBoxMenuItem(window.getTitle(), 0,
                            Commands.noCmd, new AbstractMenuCommander() {

                        @Override
                        public boolean menuItemState(int cmd) {

                            return window.isVisible();
                        }

                        @Override
                        public boolean doCommand(int cmd) {

                            window.setVisible(!window.isVisible());
                            return true;
                        }
                    }));
                }
            }
        }
    }

    public static Document[] getDocuments() {

        Document[] ds = new Document[Main.documents.size()];
        int n = 0;
        for (DocumentWindow<?> window : Main.documents) {
            ds[n++] = window.getDocument();
        }
        return ds;
    }

    public static void clientInitiatedExit(Main app) {

        Main.clients = null;
        app.doCommand(Commands.cmdQuit);
    }

    public void startTasking() {

        if (Main.clients != null) {
            Main.clients.runEventLoop(true);
        }
    }

}
