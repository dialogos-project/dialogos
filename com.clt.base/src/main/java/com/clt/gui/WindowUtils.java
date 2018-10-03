package com.clt.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.RootPaneContainer;

import com.clt.util.Platform;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Toolkit;

public class WindowUtils {

    /**
     * How to position the dialog on the screen.
     */
    public static final int 
            CENTER_ON_SCREEN = 0, 
            CENTER_ON_PARENT = 1,
            ALERT_POSITION = 2;

    public static void setLocation(Window w, int mode) {

        Dimension screenSize = getScreenSize();
        
        switch (mode) {
            case CENTER_ON_PARENT:
                WindowUtils.setLocationRelativeTo(w, w.getParent());
                break;
            case CENTER_ON_SCREEN:
                w.setLocation((screenSize.width - w.getSize().width) / 2,
                        (screenSize.height - w.getSize().height) / 2);
                break;
            case ALERT_POSITION:
                // w.setLocation((screenSize.width - w.getSize().width)/2,
                // screenSize.height/4);
                WindowUtils.setLocationRelativeTo(w, w.getParent());
                break;
        }
    }
    
    /**
     * Returns the screen size. On Linux, this works around a bug where
     * sometimes the screen size would be reported as far too wide,
     * leading to a placement of the DialogOS windows in an invisible
     * location; it returns the size of the default monitor instead.
     * 
     * @return 
     */
    public static Dimension getScreenSize() {
        if( Platform.isLinux() ) {
            GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice dev = env.getDefaultScreenDevice();
            Rectangle bounds = dev.getDefaultConfiguration().getBounds();
            return new Dimension(bounds.width, bounds.height);
        } else {
            return Toolkit.getDefaultToolkit().getScreenSize();
        }
    }

    public static void setLocationRelativeTo(Window w, Component parent) {

        Point parentLocation;
        Dimension parentSize;
        if ((parent == null) || !parent.isShowing()) {
            parentLocation = new Point(0, 0);
            parentSize = getScreenSize();
        } else {
            parentLocation = parent.getLocationOnScreen();
            parentSize = parent.getSize();
        }

        Dimension size = w.getSize();
        Dimension screenSize = getScreenSize();

        int x = parentLocation.x + parentSize.width / 2 - size.width / 2;
        // int y = parentLocation.y + parentSize.height/2 - size.height/2;
        int y = parentLocation.y + parentSize.height / 2 - size.height / 2;
        // use golden mean
        double phi = (Math.sqrt(5) + 1.0) / 2;
        y = parentLocation.y + (int) (parentSize.height * (1.0 - 1.0 / phi)) - size.height / 2;

        if (x < 0) {
            x = 0;
        } else if (x + size.width > screenSize.width) {
            x = Math.max(0, screenSize.width - size.width);
        }

        if (y < 0) {
            y = 0;
        } else if (y + size.height > screenSize.height) {
            y = Math.max(0, screenSize.height - size.height);
        }

        w.setLocation(x, y);
    }

    /*
   * WICHTIG: Den ganzen modalen Schnickschnack nur unter Windows machen!!! Auf
   * dem Mac sind sonst die Menues weg, ausserdem funktioniert dort alles auch
   * ohne modalParent
     */
    private static Vector<ModalWindowListener> modalListeners
            = new Vector<ModalWindowListener>();

    private static class ModalWindowListener {

        public Window mw;

        public ModalWindowListener(Window w) {

            synchronized (WindowUtils.modalListeners) {
                w.addWindowListener(new WindowAdapter() {

                    @Override
                    public void windowClosed(WindowEvent e) {

                        WindowUtils.modalListeners.removeElement(ModalWindowListener.this);
                    }
                });
                this.mw = w;
                WindowUtils.modalListeners.addElement(this);
            }
        }

        public void toFront() {

            // Wenn wir schon Focus haben, dann nichts machen!
            // Das ist ganz wichtig, weil sonst manche Dialoge unter JRE 1.3
            // beim Schliessen
            // ein merkwuerdiges Flackern mit anderen Dialogen verursachen
            if (this.mw.getFocusOwner() != null) {
                return;
            }

            this.mw.toFront(); // evtl. unsichtbarer tempModalFrame
            // toFront() bringt alle seine Kinder nach vorne
            if (this.mw.isShowing()) {
                this.mw.requestFocus();
            }
        }

        @Override
        public String toString() {

            return "ModalWindowListener on " + this.mw;
        }

    }

    public static void installModalDialog(Window w) {

        if (Platform.isWindows()) {
            new ModalWindowListener(w);
        }
    }

    public static void deinstallModalDialog(Window w) {

        if (Platform.isWindows()) {
            synchronized (WindowUtils.modalListeners) {
                for (ModalWindowListener l : WindowUtils.modalListeners) {
                    if (l.mw == w) {
                        WindowUtils.modalListeners.removeElement(l);
                        return;
                    }
                }
            }
        }
    }

    private static boolean updatingModals = false;

    public static void activateModals() {

        // geblockt, um wildes Flackern durch Rekursion zu verhindern
        if (WindowUtils.updatingModals == false) {
            WindowUtils.updatingModals = true;
            synchronized (WindowUtils.modalListeners) {
                for (ModalWindowListener l : WindowUtils.modalListeners) {
                    l.toFront();
                }
            }
            WindowUtils.updatingModals = false;
        }
    }

    public static Frame createTempModalFrame() {

        Frame f = new Frame();
        WindowUtils.installModalDialog(f);
        return f;
    }

    public static void setWindowModified(Window window, boolean modified) {

        try {
            // JFrames have a pure Java way of setting the windowModified
            // bit.
            if (window instanceof RootPaneContainer) {
                ((RootPaneContainer) window).getRootPane().putClientProperty(
                        "windowModified",
                        new Boolean(modified));
            } else {
                // other windows need to use JDirect
                Class.forName("com.clt.jdirect.WindowUtils").getMethod(
                        "setWindowModified",
                        new Class[]{Window.class, Boolean.TYPE}).invoke(null,
                                new Object[]{window, new Boolean(modified)});
            }
        } catch (Exception ignore) {
            // ignore errors. This is only a visual clue anyways
        }
    }

}
