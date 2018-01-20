package com.clt.dialog.client;

/**
 * Launcher for the DialogClient main application.
 *
 * @author dabo
 */
public class Main {

    public static void main(String args[]) {
        if (System.getProperty("apple.laf.useScreenMenuBar") == null) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        }
        if (System.getProperty("com.apple.macos.useScreenMenuBar") == null) {
            System.setProperty("com.apple.macos.useScreenMenuBar", "true");
        }
        if (System.getProperty("com.apple.mrj.application.apple.menu.about.name") == null) {
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "DialogClient");
        }

        GUIClientFactory.main(args);
    }
}
