package com.clt.dialog.client;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarFile;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import com.clt.event.ProgressEvent;
import com.clt.event.ProgressListener;
import com.clt.gui.CmdButton;
import com.clt.gui.GUI;
import com.clt.gui.OptionPane;
import com.clt.gui.WindowUtils;
import com.clt.util.Counter;
import com.clt.util.IniFile;
import com.clt.util.Misc;

/**
 * @author Daniel Bobbert
 * @version 6.0
 */
public class GUIClientFactory {

    private IniFile ini = null;
    private Map<String, GUIClientWindow> windows;

    public GUIClientFactory() {

        this.windows = new HashMap<String, GUIClientWindow>();
    }

    public Collection<GUIClientWindow> getWindows() {

        return this.windows.values();
    }

    private IniFile loadIniFile(File appDir) throws IOException {

        IniFile ini = null;
        File f = new File(appDir, "Clients.ini");
        if (f.isFile()) {
            InputStream in = new FileInputStream(f);
            try {
                ini = IniFile.read(in);
            } finally {
                in.close();
            }
        }
        return ini;
    }

    public void quit(boolean force) {

        for (GUIClientWindow window : new ArrayList<GUIClientWindow>(this.windows
                .values())) {
            if (!window.dispose(force)) {
                return;
            }
        }

        // System will automatically be implicitely terminated by the last
        // WindowListener
        // If that doesn't suffice, signal explicititely that the user meant to quit
        // terminate(true);
    }

    protected synchronized void terminate(boolean explicit) {

        System.exit(0);
    }

    protected GUIClientWindow createWindow(String name) {

        return new GUIClientWindow(name);
    }

    protected void initUI(Collection<GUIClient> clients) {

        for (GUIClient client : clients) {
            Properties properties = null;
            if (this.ini != null) {
                properties = this.ini.getProperties(client.getName());
            }

            String windowName = null;
            if (properties != null) {
                windowName = properties.getProperty("Window");
            }

            GUIClientWindow window = this.windows.get(windowName);
            if (window == null) {
                window = this.createWindow(windowName);
                window.setQuitHandler(new Runnable() {

                    public void run() {

                        GUIClientFactory.this.quit(false);
                    }
                });
                window.addWindowListener(new WindowAdapter() {

                    @Override
                    public void windowClosed(WindowEvent evt) {

                        GUIClientFactory.this.windows.values().remove(evt.getWindow());
                        // terminate when the last window was closed
                        if (GUIClientFactory.this.windows.size() == 0) {
                            GUIClientFactory.this.terminate(false);
                        }
                    }
                });
                this.windows.put(windowName, window);
            }

            if (properties != null) {
                try {
                    window.setPreferredLocation(new Point(
                            Integer.parseInt(properties.getProperty("Window.x")),
                            Integer.parseInt(properties.getProperty("Window.y"))));
                } catch (Exception ignore) {
                }
                try {
                    window.setPreferredSize(new Dimension(
                            Integer
                                    .parseInt(properties.getProperty("Window.width")),
                            Integer.parseInt(properties
                                    .getProperty("Window.height"))));
                } catch (Exception ignore) {
                }
            }

            window.addClient(client);
        }

        for (GUIClientWindow window : this.windows.values()) {
            window.initUI();
        }
    }

    private void addClient(Collection<GUIClient> clients, GUIClient client) {

        Properties properties = null;
        if (this.ini != null) {
            properties = this.ini.getProperties(client.getName());
        }

        boolean enabled = true;
        if (properties != null) {
            if (properties.get("Enabled") != null) {
                enabled = new Boolean(properties.getProperty("Enabled")).booleanValue();
            }
            if (properties.get("Disabled") != null) {
                enabled
                        = !new Boolean(properties.getProperty("Disabled")).booleanValue();
            }
        }

        if (enabled) {
            clients.add(client);
        }
    }

    protected Collection<GUIClient> loadModules(Component parent, File appDir) {

        return this.loadModules(parent, appDir, false);
    }

    final protected Collection<GUIClient> loadModules(Component parent, File appDir, boolean alwaysSearchForExternalModules) {

        Collection<GUIClient> clients = new LinkedList<GUIClient>();
        // first load local (compiled in) modules
        try {
            InputStream is = null;
            try {
                is = this.getClass().getClassLoader().getResourceAsStream("module.txt");
            } catch (Exception exn) {
            }

            if (is != null) {
                final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
                clients.addAll(this
                        .loadModules(parent, classLoader, "main program", is));
            }
        } catch (ThreadDeath d) {
            throw d;
        } catch (Throwable exn) {
            OptionPane.error(parent, new String[]{
                "Could not load builtin modules.",
                exn.toString()});
        }

        if ((clients.size() == 0) || alwaysSearchForExternalModules) {
            // if there's no builtin module(s), look for external modules
            File modules = new File(appDir, "modules");
            if (modules.isDirectory()) {
                File[] files = modules.listFiles(new FileFilter() {

                    public boolean accept(File f) {

                        return f.isFile() && f.getName().endsWith(".jar");
                    }
                });

                Arrays.sort(files);

                for (int i = 0; i < files.length; i++) {
                    try {
                        InputStream is = null;
                        try {
                            JarFile jar = new JarFile(files[i]);
                            is = jar.getInputStream(jar.getJarEntry("module.txt"));
                        } catch (Exception exn) {
                        }

                        if (is != null) {
                            final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
                            clients.addAll(this.loadModules(parent, classLoader, files[i].getName(), is));
                        }
                    } catch (ThreadDeath d) {
                        throw d;
                    } catch (Throwable exn) {
                        OptionPane.error(parent, new String[]{
                            "Could not load modules from "
                            + files[i].getName() + ".",
                            exn.toString()});
                    }
                }
            }
        }
        return clients;
    }

    private Collection<GUIClient> loadModules(Component parent,
            ClassLoader classLoader,
            String source, InputStream is)
            throws IOException {

        Collection<GUIClient> clients = new LinkedList<GUIClient>();

        BufferedReader in = new BufferedReader(new InputStreamReader(is));

        String className;
        while ((className = in.readLine()) != null) {
            className = className.trim();
            if (className.length() > 0) {
                try {
                    Class<?> module = classLoader.loadClass(className);
                    if (!GUIClient.class.isAssignableFrom(module)) {
                        throw new ClassCastException("Modules must be subclasses of "
                                + GUIClient.class.getName());
                    }
                    GUIClient client = (GUIClient) module.newInstance();
                    client.setClassLoader(classLoader);
                    this.addClient(clients, client);
                } catch (ThreadDeath d) {
                    throw d;
                } catch (Throwable exn) {
                    exn.printStackTrace();
                    OptionPane.error(parent, new String[]{
                        "Could not load module " + className + " from "
                        + source + ".",
                        exn.toString()});
                }
            }
        }

        return clients;
    }

    private Map<GUIClient, Throwable> initClients(
            final GUIClientStartupScreen startupWindow,
            final Collection<GUIClient> clients, final String[] args) {

        final Counter currentClient = new Counter(0);

        Map<GUIClient, Throwable> errors
                = new IdentityHashMap<GUIClient, Throwable>();

        ProgressListener l = new ProgressListener() {

            public void progressChanged(final ProgressEvent evt) {

                if (startupWindow != null) {
                    GUI.invokeAndWait(new Runnable() {

                        public void run() {

                            startupWindow.setClientStatus(evt);
                        }
                    });
                }
            }
        };

        final ProgressEvent evt
                = new ProgressEvent(this, "Initializing clients", 0,
                        clients.size(), 0);
        for (Iterator<GUIClient> clientIterator = clients.iterator(); clientIterator
                .hasNext();) {
            final GUIClient client = clientIterator.next();
            try {
                if (startupWindow != null) {
                    startupWindow.setTitle("Initializing " + client.getName());
                    startupWindow.setStatus(startupWindow.getTitle() + "...");
                    GUI.invokeAndWait(new Runnable() {

                        public void run() {

                            startupWindow.increaseProgress(0);
                        }
                    });
                } else {
                    System.out.println("Initializing " + client.getName() + "...");
                }

                evt.setMessage("Initializing...");
                evt.setValues(0, 0, 0);
                l.progressChanged(evt);

                Properties properties = null;
                if (this.ini != null) {
                    properties = this.ini.getProperties(client.getName());
                }
                if (properties == null) {
                    properties = new Properties();
                }
                client.initClient(l, args, properties);
                currentClient.increase();
                if (startupWindow != null) {
                    GUI.invokeAndWait(new Runnable() {

                        public void run() {

                            startupWindow.increaseProgress(1);
                        }
                    });
                }
            } catch (ThreadDeath d) {
                throw d;
            } catch (final Throwable exn) {
                exn.printStackTrace();
                if (startupWindow != null) {
                    errors.put(client, exn);
                }
                // clientIterator.remove();
            }
        }

        Collection<GUIClientWindow> windows = this.windows.values();
        for (GUIClientWindow w : windows) {
            for (GUIClient client : w.getClients()) {
                try {
                    client.setupComplete(w, windows);
                } catch (Exception exn) {
                    client.error(exn);
                }
            }
        }

        return errors;
    }

    public final void startup(File appDir, final String[] args, GUIClientStartupScreen startupScreen, boolean gui)
            throws IOException {

        this.ini = this.loadIniFile(appDir);

        Collection<GUIClient> clients
                = this.loadModules(null, appDir);

        if (clients.isEmpty()) {
            if ((startupScreen == null) && gui) {
                final GUIClientStartupScreen startupWindow
                        = new GUIClientStartupScreen(
                                "Dialog Client", String.valueOf(Device.VERSION));
                startupWindow.setTitle("Startup failed");
                final Object lock = new Object();
                final JComponent p = new JPanel(new BorderLayout(6, 6));

                p.add(
                        new JLabel("No active clients were found.", SwingConstants.CENTER),
                        BorderLayout.NORTH);
                JPanel button = new JPanel(new FlowLayout(FlowLayout.CENTER));
                button.setOpaque(false);
                button.add(new CmdButton(new Runnable() {

                    public void run() {

                        startupWindow.dispose();
                        synchronized (lock) {
                            lock.notifyAll();
                        }
                    }
                }, "OK"));
                p.add(button, BorderLayout.SOUTH);

                GUI.invokeAndWait(new Runnable() {

                    public void run() {

                        startupWindow.show(p);
                    }
                });
                synchronized (lock) {
                    try {
                        lock.wait();
                    } catch (InterruptedException exn) {
                    }
                }
                return;
            }
        } else {
            this.initUI(clients);

            final GUIClientStartupScreen startupWindow;
            if (gui) {
                if (startupScreen == null) {
                    startupWindow = new GUIClientStartupScreen("Dialog Client",
                            String.valueOf(Device.VERSION));
                } else {
                    startupWindow = startupScreen;
                }

                startupWindow.addNumClients(clients.size());

                if (!startupWindow.isShowing()) {
                    GUI.invokeAndWait(new Runnable() {

                        public void run() {

                            startupWindow.show(null);
                        }
                    });
                }
            } else {
                startupWindow = null;
            }

            Map<GUIClient, Throwable> errors = null;
            try {
                errors = this.initClients(startupWindow, clients, args);
            } finally {
                if ((startupScreen == null) && (startupWindow != null)) {
                    startupWindow.setVisible(false);
                }
            }

            if ((errors != null) && false) {
                for (GUIClient client : errors.keySet()) {
                    OptionPane.error(startupWindow, new String[]{
                        client.getName() + " could not be initialized.",
                        errors.get(client).toString()});
                }

            }
        }
    }

    public void runEventLoop(boolean showAllWindows) {

        int base = 30;
        int n = base;
        int screenWidth = WindowUtils.getScreenSize().width;
        for (GUIClientWindow window : this.windows.values()) {
            window.pack();
            if (window.getPreferredLocation() != null) {
                window.setLocation(window.getPreferredLocation());
            } else {
                if ((n > base) && (n + window.getWidth() > screenWidth)) {
                    base += 30;
                    n = base;
                }
                window.setLocation(n, base);

                n += window.getWidth() + 30;
            }
            if (showAllWindows) {
                window.setVisible(true);
            }

        }

        for (GUIClientWindow window : new ArrayList<GUIClientWindow>(this.windows
                .values())) {
            if (window.isVisible()) {
                window.connect();
                window.getContentPane().requestFocusInWindow();
            }
        }

        while (!this.windows.isEmpty()) {
            boolean somethingDone = false;
            long time = System.currentTimeMillis();

            for (GUIClientWindow window : this.windows.values()) {
                for (GUIClient client : window.getClients()) {
                    if (client.systemTask()) {
                        somethingDone = true;
                    }
                }
            }

            time = System.currentTimeMillis() - time;
            if (!somethingDone) {
                try {
                    Thread.sleep(1000 - time);
                } catch (Exception ignore) {
                }
            }
        }
    }

    public static void main(String args[]) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignore) {
        }

        File appDir = Misc.getApplicationDirectory();

        try {
            GUIClientFactory factory = new GUIClientFactory();
            factory.startup(appDir, args, null, true);
            factory.runEventLoop(true);
            System.exit(0);
        } catch (ThreadDeath d) {
            throw d;
        } catch (Throwable t) {
            t.printStackTrace();
            OptionPane.error(null, t);
            System.exit(1);
        }
    }

}
