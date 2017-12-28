//
//  EAWTHandler.java
//  AppleScript
//
//  Created by Daniel Bobbert on Sat Mar 20 2004.
//  Copyright (c) 2004 CLT Sprachtechnologie GmbH. All rights reserved.
//

package com.clt.mac;

import java.io.File;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationEvent;
import com.apple.eawt.ApplicationListener;

class EAWTHandler implements SystemEventAdapter {

  public void register(RequiredEventHandler handler) {

    Application app = Application.getApplication();

    if (handler.insertAboutItem) {
      app.addAboutMenuItem();
    }
    if (handler.insertPreferencesItem) {
      app.addPreferencesMenuItem();
      app.setEnabledPreferencesMenu(true);
    }

    app.addApplicationListener(new EAWTApplicationListener(handler));
  }

  private static class EAWTApplicationListener implements ApplicationListener {

    RequiredEventHandler handler;


    public EAWTApplicationListener(RequiredEventHandler handler) {

      this.handler = handler;
    }


    public void handleAbout(ApplicationEvent event) {

      event.setHandled(this.handler.handleAbout());
    }


    public void handleOpenApplication(ApplicationEvent event) {

      event.setHandled(this.handler.handleOpenApplication());
    }


    public void handleReOpenApplication(ApplicationEvent event) {

      event.setHandled(this.handler.handleReOpenApplication());
    }


    public void handlePreferences(ApplicationEvent event) {

      event.setHandled(this.handler.handlePreferences());
    }


    public void handleQuit(ApplicationEvent event) {

      event.setHandled(this.handler.handleQuit());
    }


    public void handleOpenFile(ApplicationEvent event) {

      event.setHandled(this.handler.handleOpenFile(this.getFile(event)));
    }


    public void handlePrintFile(ApplicationEvent event) {

      event.setHandled(this.handler.handlePrintFile(this.getFile(event)));
    }


    private File getFile(ApplicationEvent event) {

      return new File(event.getFilename());
    }
  }
}
