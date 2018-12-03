package com.clt.diamant.gui;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;

import javax.swing.JMenu;
import javax.swing.WindowConstants;

import com.clt.diamant.Document;
import com.clt.diamant.FileSaveException;
import com.clt.diamant.Main;
import com.clt.diamant.Resources;
import com.clt.diamant.graph.search.SearchResultsDialog;
import com.clt.event.ProgressListener;
import com.clt.gui.FileChooser;
import com.clt.gui.GUI;
import com.clt.gui.ManagedFrame;
import com.clt.gui.OptionPane;
import com.clt.gui.ProgressDialog;
import com.clt.gui.WindowUtils;
import com.clt.gui.menus.CmdMenuBar;
import com.clt.gui.menus.MenuCommander;
import com.clt.gui.menus.MenuOwner;
import com.clt.util.DefaultLongAction;
import com.clt.util.UserCanceledException;

public class DocumentWindow<DocType extends Document> extends ManagedFrame implements MenuCommander, MenuOwner {

    /**
     *
     */
    private static final long serialVersionUID = 337224910603943455L;

    public static enum Saving {
        SAVE_CHANGES,
        ASK_SAVE_CHANGES,
        DONT_SAVE_CHANGES
    }

    private static Point gStackedPosition = new Point(0, 0);

    private JMenu windowMenu = null;
    private Collection<CmdMenuBar> mbars = null;

    private DocType document;
    private PropertyChangeListener documentPropertyListener;

    public DocumentWindow(DocType d) {

        super(d.getTitle());

        this.document = d;

        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent evt) {
                WindowUtils.activateModals();
            }
        });

        this.documentPropertyListener = evt -> {
            if (evt.getPropertyName().equals("title")) {
                DocumentWindow.this.setTitle(DocumentWindow.this.document.getTitle());
            }
        };

        this.windowMenu = new JMenu(Resources.getString("Window"));
        this.mbars = Collections.emptyList();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        this.document.addPropertyChangeListener(this.documentPropertyListener);
        this.setTitle(this.document.getTitle());
    }

    @Override
    public void removeNotify() {
        this.document.removePropertyChangeListener(this.documentPropertyListener);
        super.removeNotify();
    }

    public DocType getDocument() {
        return this.document;
    }

    protected void setupMenus(Collection<CmdMenuBar> mbars) {
        this.mbars = mbars;
    }

    protected Collection<CmdMenuBar> getMenus() {
        return this.mbars;
    }

    public void finishSetup() {
        this.updateMenus();
        this.pack();
        Dimension size = this.getSize();
        Dimension screenSize = WindowUtils.getScreenSize();
        int min = Math.min(screenSize.width - size.width, screenSize.height - size.height);
        DocumentWindow.gStackedPosition = new Point(DocumentWindow.gStackedPosition.x + 30, DocumentWindow.gStackedPosition.y + 30);

        if ((DocumentWindow.gStackedPosition.x >= min - 30) || (DocumentWindow.gStackedPosition.y >= min - 30)) {
            DocumentWindow.gStackedPosition = new Point(30, 30);
        }

        this.setLocation(DocumentWindow.gStackedPosition);
        this.setSize(new Dimension(Math.min(size.width, screenSize.width - this.getLocation().x - 40), Math.min(size.height, screenSize.height - this.getLocation().y - 40)));
        


// setLocation(30 + gUntitledDocuments*20 % min, 30 + gUntitledDocuments*20
        // % min);
        this.setDirty(false);
    }

    @Override
    public void setTitle(String title) {
        super.setTitle(title);
        // trigger reconstruction of Window menu for managed frames
        this.processWindowEvent(new WindowEvent(this, WindowEvent.WINDOW_ACTIVATED));
    }

    public boolean isDirty() {
        return this.document.isDirty();
    }

    public void setDirty(boolean dirty) {
        if (dirty != this.isDirty()) {
            this.document.setDirty(dirty);
            this.updateMenus();
            WindowUtils.setWindowModified(this, this.isDirty());
        }
    }

    public File getFile() {
        return this.document.getFile();
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

    public boolean readyToClose(Saving saving) {
        if (this.isDirty() && (saving != Saving.DONT_SAVE_CHANGES)) {
            int result;
            if (saving == Saving.SAVE_CHANGES) {
                result = OptionPane.SAVE;
            } else {
                result = OptionPane.confirm(this, new String[]{
                    Resources.format("SaveChangesToFile", this.getTitle()),
                    Resources.getString("SaveChangesInfo")},
                        Resources.getString("SaveChanges?"), OptionPane.SAVE_OPTION);
            }
            if (result == OptionPane.SAVE) {
                return this.save(this.document.getFile());
            } else if (result == OptionPane.DONTSAVE) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    public boolean saveAs() {
        File f = new FileChooser(this.document.getFile() != null ? this.document.getFile()
                .getParent() : null).standardPutFile(
                this, this.document.getFile() != null ? this.document.getFile()
                .getName() : (this.getTitle() + ".xml"));
        if (f != null) {
            return this.save(f);
        } else {
            return false;
        }
    }

    public boolean save(final File f) {
        if (this.getDocument().isReadOnly()) {
            return true;
        }

        if (f == null) {
            return this.saveAs();
        } else {
            try {
                final ProgressDialog d = new ProgressDialog(this);
                d
                        .run(new DefaultLongAction(Resources
                                .format("Saving", this.getTitle())) {

                            @Override
                            public void run(ProgressListener l)
                                    throws Exception {

                                try {
                                    GUI.setWaitCursor(DocumentWindow.this);
                                    DocumentWindow.this.document.save(d, f, l);
                                } finally {
                                    GUI.setDefaultCursor(DocumentWindow.this);
                                }
                            }
                        });
            } catch (InvocationTargetException texn) {
                GUI.setDefaultCursor(this);
                Throwable exn = texn.getTargetException();
                if (exn instanceof FileSaveException) {
                    FileSaveException fse = (FileSaveException) exn;
                    SearchResultsDialog.show(this, fse.getLocalizedMessage(), fse
                            .getErrors());
                } else if (!(exn instanceof UserCanceledException)) {
                    exn.printStackTrace();
                    String message = exn.toString();
                    if (exn instanceof NullPointerException) {
                        StackTraceElement trace[] = exn.getStackTrace();
                        if ((trace != null) && (trace.length > 0)) {
                            message += " at " + trace[0];
                        }
                    }
                    OptionPane.message(this, new String[]{
                        Resources.format("CouldNotSaveFile", f.getName()),
                        Resources.getString("FileWriteError") + ": "
                        + message},
                            Resources.getString("IOError"), OptionPane.ERROR);
                }
                return false;
            }
            return true;
        }
    }

    public boolean menuItemState(int cmd) {
        switch (cmd) {
            case cmdSave:
                return this.isDirty() || (this.getFile() == null);
            case cmdSaveAs:
                return true;
            default:
                return false;
        }
    }

    public String menuItemName(int cmd, String old_name) {
        return old_name;
    }

    /**
     * "Does" / Executes the command with the specific command number
     *
     * @param cmd command number
     * @return true if the command was handled
     */
    public boolean doCommand(int cmd) {
        boolean cmdHandled = true;
        switch (cmd) {
            case cmdClose:
                this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
                break;

            case cmdSave:
                this.save(this.getFile());
                break;

            case cmdSaveAs:
                this.saveAs();
                break;

            default:
                cmdHandled = false;
        }

        return cmdHandled;
    }

    public void updateMenus() {
        if (this.mbars != null) {
            for (CmdMenuBar mbar : this.mbars) {
                mbar.updateMenus();
                GUI.assignMnemonics(mbar);
            }
        }

    }

    @Override
    public void toFront() {
        super.toFront();
        this.requestFocus();
    }

    @Override
    public String toString() {
        return this.getTitle();
    }

}
