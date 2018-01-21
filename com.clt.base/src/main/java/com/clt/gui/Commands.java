package com.clt.gui;

public interface Commands {
    public final int noCmd = -1;
    public final int cmdMenu = -2;

    public final int cmdOK = 0;
    public final int cmdCancel = 1;

    public final int cmdAbout = 100;
    public final int cmdNew = 101;
    public final int cmdOpen = 102;
    public final int cmdImport = 103;
    public final int cmdExport = 104;
    public final int cmdClose = 105;
    public final int cmdSave = 106;
    public final int cmdSaveAs = 107;
    public final int cmdRevert = 108;
    public final int cmdPageSetup = 109;
    public final int cmdPrint = 110;
    public final int cmdQuit = 111;

    public final int cmdUndo = 201;
    public final int cmdRedo = 202;
    public final int cmdCut = 203;
    public final int cmdCopy = 204;
    public final int cmdPaste = 205;
    public final int cmdDelete = 206;
    public final int cmdSelectAll = 207;
    public final int cmdPreferences = 208;

    public final int cmdHelp = 301;

    public final int cmdApplication = 1000;
    public final int cmdDocument = 2000;
}
