package com.clt.diamant.gui;

import com.clt.diamant.*;
import com.clt.gui.ApplicationMenuPanel;
import com.clt.gui.Commands;
import com.clt.gui.Images;
import com.clt.gui.menus.CmdMenuBar;
import com.clt.mac.RequiredEventHandler;
import com.clt.util.AbstractAction;
import com.clt.util.Platform;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ProjectStartupFrame extends JFrame {

    private ChangeListener preferenceListener;
    AbstractAction newProject;
    AbstractAction openProject;
    AbstractAction quit;
    Main main;
    JMenu projectStartupMRU; // most recently used dialog models


    public ProjectStartupFrame(final Main main, final JMenu projectStartupMRU, final RequiredEventHandler systemEventHandler) {
        super(Version.PRODUCT_NAME);
        setIconImage(Images.load("logo/DialogOS.png").getImage());
        this.preferenceListener = evt -> {
            if (evt.getSource() == Preferences.getPrefs().locale) {
                this.initUI(systemEventHandler);
            }
        };
        this.projectStartupMRU = projectStartupMRU;
        this.main = main;
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evt) {
                main.doCommand(Commands.cmdQuit);
            }
        });

        newProject = new AbstractAction(Resources
                .getString("CreateNewDialog")
                + "...", Images.load("NewFileWizard.png")) {
            @Override
            public void run() {
                main.doCommand(Commands.cmdNew);
                closeProjectFrameOnSuccess();
            }
        };
        newProject.setAccelerator(KeyEvent.VK_N);

        openProject = new AbstractAction(Resources
                .getString("OpenDialog")
                + "...", Images.load("OpenFile.png")) {
            @Override
            public void run() {
                main.doCommand(Commands.cmdOpen);
                closeProjectFrameOnSuccess();
            }
        };
        openProject.setAccelerator(KeyEvent.VK_O);

        quit = new AbstractAction(Resources
                .getString("Quit")) {
            @Override
            public void run() {
                main.doCommand(Commands.cmdQuit);
            }
        };
        quit.setAccelerator(KeyEvent.VK_Q);

        initUI(systemEventHandler);
    }

    public void initUI(RequiredEventHandler systemEventHandler) {
        updateLanguage();
        initContent(systemEventHandler);
        initMenus();
        pack();
    }

    private void updateLanguage() {
        projectStartupMRU.setText(Resources.getString("MRU"));
        newProject.setName(Resources.getString("CreateNewDialog"));
        openProject.setName(Resources.getString("OpenDialog"));
        quit.setName(Resources.getString("Quit"));
    }

    private void initMenus() {
        // update menu bar each time
        CmdMenuBar mbar = new CmdMenuBar();
        JMenu menu = new JMenu(Resources.getString("File"));
        menu.add(new JMenuItem(newProject));
        menu.add(new JMenuItem(openProject));

        menu.add(projectStartupMRU);

        if (Platform.showQuitMenuItem()) {
            menu.addSeparator();
            menu.add(new JMenuItem(quit));
        }
        mbar.add(menu);

        MenuUtils.addHelpMenu(mbar);

        mbar.updateMenus();

        setJMenuBar(mbar);
    }

    private void initContent(RequiredEventHandler systemEventHandler) {
        setContentPane(new ApplicationMenuPanel(
                Version.PRODUCT_NAME, Images.load("AppIconLarge.png")
                .getImage(), new Action[]{ /* newEmptyProject, */
                newProject, openProject}, new Action[]{quit}));
        setResizable(false);
    }

    private void closeProjectFrameOnSuccess() {
        if (main.getDocuments().length > 0) {
            this.setVisible(false);
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        Preferences.getPrefs().addPropertyChangeListener(this.preferenceListener);
    }

    @Override
    public void removeNotify() {
        Preferences.getPrefs().removePropertyChangeListener(this.preferenceListener);
        super.removeNotify();
    }

}
