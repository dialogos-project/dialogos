package com.clt.mac;

import java.io.File;

public class RequiredEventHandler {

    public static final boolean INSERT_ABOUT_ITEM = true,
            INSERT_PREFERENCES_ITEM = true,
            NO_ABOUT_ITEM = false,
            NO_PREFERENCES_ITEM = false;

    boolean insertAboutItem;
    boolean insertPreferencesItem;

    public RequiredEventHandler(boolean insertAboutItem, boolean insertPreferencesItem) {
        this.insertAboutItem = insertAboutItem;
        this.insertPreferencesItem = insertPreferencesItem;
    }

    public boolean handleAbout() {
        return false;
    }

    public boolean handlePreferences() {
        return false;
    }

    public boolean handleOpenApplication() {
        return false;
    }

    public boolean handleReOpenApplication() {
        return false;
    }

    public boolean handleOpenFile(File f) {
        return false;
    }

    public boolean handlePrintFile(File f) {
        return false;
    }

    public boolean handleQuit() {
        return false;
    }

}
