/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.clt.mac;

import java.awt.Desktop;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class that registers handlers for menu events such as "About" and
 * "Preferences" on Java 9. In Java 9, support for Mac-specific menu handling
 * architectures such as EAWT was removed. This class registers these using the
 * new java.awt.Desktop functionality. It makes heavy use of reflection so it
 * can still be compiled (but will not work) on older versions of Java.
 *
 * @author koller
 */
public class Java9MacHandler implements SystemEventAdapter {

    private static final ClassLoader CL = Java9MacHandler.class.getClassLoader();
    private RequiredEventHandler handler;

    public void register(RequiredEventHandler handler) {
        this.handler = handler;
        
        if (handler.insertAboutItem) {
            setDesktopHandler("setAboutHandler", "java.awt.desktop.AboutHandler");
        }

        if (handler.insertPreferencesItem) {
            setDesktopHandler("setPreferencesHandler", "java.awt.desktop.PreferencesHandler");
//            app.addPreferencesMenuItem();
//            app.setEnabledPreferencesMenu(true);
        }

//        app.addApplicationListener(new EAWTApplicationListener(handler));
    }

    /**
     * Registers an instance of ProxyListener (see below) with the AWT Desktop,
     * using the given method name for setting the handler (e.g.
     * "setAboutHandler") and interface name for the handler (e.g.
     * "java.awt.desktop.AboutHandler"). Returns true if this worked correctly,
     * and false if an exception occurred.
     *
     * @param handlerSettingMethodName
     * @param interfaceName
     * @return
     */
    private boolean setDesktopHandler(String handlerSettingMethodName, String interfaceName) {
        try {
            Desktop desktop = Desktop.getDesktop();
            Class aboutHandlerClass = CL.loadClass(interfaceName);
            System.err.println("ah class: " + aboutHandlerClass);
            Method m = Desktop.class.getMethod(handlerSettingMethodName, aboutHandlerClass);
            System.err.println("meth: " + m);
            Object aboutHandler = Proxy.newProxyInstance(CL, new Class[]{aboutHandlerClass}, new ProxyListener(handler));
            m.invoke(desktop, aboutHandler);
            return true;
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException | InvocationTargetException ex) {
            Logger.getLogger(Java9MacHandler.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    private static class ProxyListener implements InvocationHandler {
        private Class aboutEventClass;
        private Class preferencesEventClass;
        private RequiredEventHandler handler;

        public ProxyListener(RequiredEventHandler handler) {
            this.handler = handler;
            
            try {
                aboutEventClass = Java9MacHandler.class.getClassLoader().loadClass("java.awt.desktop.AboutEvent");
                preferencesEventClass = Java9MacHandler.class.getClassLoader().loadClass("java.awt.desktop.PreferencesEvent");
            } catch (ClassNotFoundException ex) {
                // should not happen if a ProxyListener is constructed at all
                aboutEventClass = null;
                preferencesEventClass = null;
            }
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("handleAbout") && args.length == 1 && aboutEventClass.isAssignableFrom(args[0].getClass())) {
                handleAbout();
            }

            if (method.getName().equals("handlePreferences") && args.length == 1 && preferencesEventClass.isAssignableFrom(args[0].getClass())) {
                handlePreferences();
            }

            return null;
        }

        public void handleAbout() {
            handler.handleAbout();
        }
        
        public void handlePreferences() {
            handler.handlePreferences();
        }

    }

}
