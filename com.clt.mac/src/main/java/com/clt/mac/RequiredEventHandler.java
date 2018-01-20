//
//  RequiredEventHandler.java
//  AppleScript
//
//  Created by Daniel Bobbert on Sat Mar 20 2004.
//  Copyright (c) 2004 CLT Sprachtechnologie GmbH. All rights reserved.
//
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
        System.err.println("*** REH ABOUT ***"); /// AKAKAK
        new RuntimeException().printStackTrace();
        
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
