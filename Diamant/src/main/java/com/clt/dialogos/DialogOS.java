package com.clt.dialogos;

import com.clt.dialog.client.GUIClientFactory;
import com.clt.dialog.client.GUIClientStartupScreen;
import com.clt.dialog.client.GUIClientWindow;
import com.clt.dialog.client.StdIOConnectionChooser;
import com.clt.dialogos.plugin.PluginLoader;
import com.clt.diamant.*;
import com.clt.diamant.gui.DocumentWindow;
import com.clt.diamant.gui.SingleDocumentWindow;
import com.clt.diamant.log.LogPlayer;
import com.clt.event.ProgressListener;
import com.clt.gui.GUI;
import com.clt.gui.OptionPane;
import com.clt.util.Misc;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * @author dabo, 2mfriedr
 */
public class DialogOS {

    private static void usage() {
        System.err.println("Usage: " + Version.PRODUCT_NAME.toLowerCase() + " [-headless] [-clients] [-execute] [<model>]");
        System.exit(1);
    }

    public static File getDefaultModel(File appDir) {
        File file = new File(appDir, "Model.xml");
        
        if (file.isFile()) {
            return file;
        } else {
            file = new File(appDir, "Model");
            
            if (file.isDirectory()) {
                file = new File(file, "Model.xml");
                
                if (file.isFile()) {
                    return file;
                }
            }
        }

        return null;
    }
    
    // Enforce that the character encoding is UTF-8,
    // even on Windows, where the default is CP1252.
    // This is a trick from http://araklefeistel.blogspot.com/2015/10/set-fileencoding-in-jvm.html
    private static void enforceUtf8() {
        try {
            System.setProperty("file.encoding", "UTF-8");
            Field cs = Charset.class.getDeclaredField("defaultCharset");
            cs.setAccessible(true);
            cs.set(null, null);
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException ex) {
            System.err.println("An exception occurred while trying to set the character encoding to UTF-8:");
            System.err.println(ex);
            System.exit(1);
        } 
    }

    public static void main(String[] args) {
        boolean execute = false;
        boolean headless = false;
        boolean loadClients = false;
        File model = null;
        
        enforceUtf8();
        
        // AKAKAK
        System.err.println("args: " + Arrays.toString(args));

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-execute")) {
                execute = true;
            } else if (args[i].equals("-headless")) {
                headless = true;
            } else if (args[i].equals("-clients")) {
                loadClients = true;
//            } else if (args[i].equals("-model")) {
//                if (i >= args.length - 1) {
//                    DialogOS.usage();
//                }
//                model = new File(args[++i]);
            } else if (args[i].equals("-summarize")) {
                if (i >= args.length - 2) {
                    DialogOS.usage();
                }
                model = new File(args[++i]);
                try {
                    LogDocument d = (LogDocument) Document.load(model);
                    LogPlayer log = d.getLogPlayer();
                    log.summarize(new File(new File(args[++i]), model.getParentFile().getName() + ".xml"));
                } catch (Exception exn) {
                    exn.printStackTrace();
                }
                System.exit(0);
            } else if( model == null && ! args[i].startsWith("-")) {
                model = new File(args[i]);
            } else {
                DialogOS.usage();
            }
        }

        AppleLookAndFeel.setIfAppropriate();

        File appDir = Misc.getApplicationDirectory();

        final File initialModel;
        if (model != null) {
            initialModel = model;
        } else {
            initialModel = DialogOS.getDefaultModel(appDir);
        }
        
        final GUIClientStartupScreen startupScreen;

        if (headless) {
            startupScreen = null;
        } else {
            startupScreen = new GUIClientStartupScreen(Version.PRODUCT_NAME, Version.getVersion());

            if (!startupScreen.isShowing()) {
                GUI.invokeAndWait(() -> startupScreen.show(null));
            }
        }

        // initialize preferences
        Preferences.getPrefs();

        // Add the number of plugins to the progress bar size
        if (startupScreen != null) {
            startupScreen.addNumClients(PluginLoader.getNumberOfPlugins());
        }

        // Add 1 to the progress bar size if there is a model to load
        if ((initialModel != null) && (startupScreen != null)) {
            startupScreen.addNumClients(1);
        }

        PluginLoader.loadPlugins(appDir, e -> {
            GUI.invokeAndWait(() -> {
                String pluginName = e.getMessage();

                if (startupScreen != null) {
                    startupScreen.setStatus(Resources.format("LoadingPluginX", pluginName));
                    startupScreen.setClientStatus(e);
                    startupScreen.increaseProgress(1);
                } else {
                    System.out.println(Resources.format("LoadingPluginX", pluginName));

                }
            });
        });

        // Start built-in clients.
        try {
            final Main app = new Main(appDir);
            try {
                if (loadClients) {
                    DialogOS.loadBuiltinClients(app, appDir, startupScreen);
                }

                if (headless) {
                    if (initialModel == null) {
                        System.err.println("You must specify a dialog model.");
                        System.exit(1);
                    } else if (!initialModel.isFile()) {
                        System.err.println("The specified dialog model does not exist.");
                        System.exit(1);
                    }

                    if (!execute) {
                        System.err.println("You must specify the -execute option with -headless.");
                        System.exit(1);
                    }

                    boolean error = true;
                    try {
                        Document doc = Document.load(initialModel);
                        if (doc instanceof SingleDocument) {
                            final SingleDocument d = (SingleDocument) doc;

                            if (d.connectDevices(new StdIOConnectionChooser(), Preferences.getPrefs().getConnectionTimeout())) {
                                error = false;

                                new Thread(() -> {
                                    try {
                                        final WozInterface executer = new Executer(null, false);

                                        d.run(null, executer);

                                        System.out.println(Resources.getString("ExecutionComplete"));
                                        System.exit(0);
                                    } catch (Exception exn) {
                                        System.err.println(exn);
                                        exn.printStackTrace();
                                        System.exit(1);
                                    }
                                }).start();
                            }
                        } else {
                            System.err.println("File " + initialModel + " is not a dialog model.");
                        }
                    } catch (Exception exn) {
                        System.err.println(exn);
                    }
                    if (error) {
                        System.exit(1);
                    }
                } else {
                    if (initialModel != null) {
                        if (!startupScreen.isShowing()) {
                            GUI.invokeAndWait(() -> startupScreen.show(null));
                        }
                        startupScreen.setStatus(Resources.getString("InitializingDialogEngine"));
                        ProgressListener progress = event -> {
                            startupScreen.setClientStatus(event.getMessage());
                            startupScreen.setClientProgress(event.getPercentComplete());
                        };

                        if (initialModel.isFile()) {
                            final DocumentWindow<?> window = app.openDocument(null, initialModel, progress);

                            if (execute) {
                                if (window instanceof SingleDocumentWindow) {
                                    SwingUtilities.invokeLater(() -> window.doCommand(SingleDocumentWindow.cmdRun));
                                }
                            }
                        } else {
                            OptionPane.error(startupScreen, Resources.format("CouldNotFindFile", initialModel));
                        }
                    }
                }
            } finally {
                if (startupScreen != null) {
                    startupScreen.setVisible(false);
                }
            }
            if (!headless) {
                app.openApp(null);
            }

            app.startTasking();
        } catch (Exception exn) {
            System.err.println(exn);
        }
    }

    private static GUIClientFactory loadBuiltinClients(final Main app, File appDir,
            GUIClientStartupScreen startupScreen) throws IOException {
        String[] clientArgs = new String[0];
        GUIClientFactory clients = new GUIClientFactory() {

            @Override
            protected void terminate(boolean explicit) {
                if (explicit) {
                    Main.clientInitiatedExit(app);
                }
            }

            @Override
            protected GUIClientWindow createWindow(String name) {
                GUIClientWindow window = new GUIClientWindow(name);
                window.setDisposeOnClose(false);
                return window;
            }

        };
        clients.startup(appDir, clientArgs, startupScreen, startupScreen != null);
        return clients;
    }
}

/**
 * This class applies the Apple look and feel if appropriate.
 */
class AppleLookAndFeel {

    static void setIfAppropriate() {
        try {
            setPropertyIfNull("com.apple.macos.useScreenMenuBar", "true");
            setPropertyIfNull("apple.laf.useScreenMenuBar", "true");
            setPropertyIfNull("com.apple.mrj.application.apple.menu.about.name", Version.PRODUCT_NAME);

            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignore) {
        }
    }

    private static void setPropertyIfNull(String key, String value) {
        if (System.getProperty(key) == null) {
            System.setProperty(key, value);
        }
    }
}
