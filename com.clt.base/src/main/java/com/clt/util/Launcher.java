package com.clt.util;

import java.util.Locale;

/**
 * Launcher is a helper class for safely starting up your application. It takes
 * the name of the "real" main class as a command line argument and will try to
 * execute main(String[]) in this class. If this fails (e.g. because of missing
 * classes) Launcher will produce an error message. Use this class when you are
 * unsure if all the needed classes (e.g. Swing) will be available on a users
 * system. Usage: <code>java -classpath &lt;ClassPath&gt; Launcher -mainclass
 * &lt;MainClass&gt; </code>
 *
 * @author Daniel Bobbert
 * @version 1.0
 */
public class Launcher {

    public static void main(String[] args) {

        String mainClass = null;

        String[] clientArgs = new String[0];

        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                if ("-mainclass".equals(args[i])) {
                    if (i > args.length - 2) {
                        Launcher.error(new IllegalArgumentException(
                                "Missing argument for option -mainclass"));
                    }
                    mainClass = args[++i];
                } else if ("-gui".equals(args[i])) {
                    // try to initialize the awt thread as early as possible
                    // Load the class via reflection so that we can catch the case,
                    // where the class doesn't exist
                    try {
                        Object frame = Class.forName("java.awt.Frame").newInstance();
                        Class.forName("java.awt.Frame").getMethod("pack", new Class[0])
                                .invoke(
                                        frame, new Object[0]);
                    } catch (Exception exn) {
                        Launcher.error(exn);
                    }
                } else if ("-locale".equals(args[i])) {
                    if (i > args.length - 2) {
                        Launcher.error(new IllegalArgumentException(
                                "Missing argument for option -locale"));
                    }
                    String locale = args[++i];
                    int index = locale.indexOf('_');
                    if (index < 0) {
                        Locale.setDefault(new Locale(locale, ""));
                    } else {
                        Locale.setDefault(new Locale(locale.substring(0, index),
                                locale.substring(index + 1)));
                    }
                } else if ("--".equals(args[i])) {
                    clientArgs = new String[args.length - i - 1];
                    System.arraycopy(args, i + 1, clientArgs, 0, clientArgs.length);
                    break;
                }
            }
        }

        if (mainClass == null) {
            mainClass = System.getProperty("com.clt.mainclass", null);
        }

        if (mainClass != null) {
            try {
                Class<?> c = Class.forName(mainClass);
                c.getDeclaredMethod("main", new Class[]{String[].class}).invoke(
                        null,
                        new Object[]{clientArgs});
            } catch (ClassNotFoundException exn) {
                Launcher.error(exn);
            } catch (NoSuchMethodException exn) {
                Launcher.error(exn);
            } catch (Exception exn) {
                Launcher.error(exn);
            }
        } else {
            Launcher.error(new InstantiationException("No main class specified"));
        }
    }

    private static void error(Throwable exn) {

        if (exn instanceof java.lang.reflect.InvocationTargetException) {
            exn
                    = ((java.lang.reflect.InvocationTargetException) exn).getTargetException();
        }
        String message
                = "An exception occurred while launching the application:\n" + exn;
        System.err.println(message);
        try {
            try {
                Object systemLAF
                        = Class.forName("javax.swing.UIManager").getDeclaredMethod(
                                "getSystemLookAndFeelClassName", new Class[0]).invoke(null,
                                        new Object[0]);
                Class.forName("javax.swing.UIManager").getDeclaredMethod(
                        "setLookAndFeel",
                        new Class[]{String.class}).invoke(null,
                                new Object[]{systemLAF});
            } catch (Throwable t) {
                // setting the LAF failed. That's no problem.
            }

            Class<?> c = Class.forName("com.clt.gui.OptionPane");
            c
                    .getDeclaredMethod(
                            "error",
                            new Class[]{Class.forName("java.awt.Component"), Object.class})
                    .invoke(null,
                            new Object[]{null, message});
        } catch (Throwable swingError) {
            try {
                Class<?> c = Class.forName("com.clt.gui.AWTOptionPane");
                c
                        .getDeclaredMethod(
                                "error",
                                new Class[]{Class.forName("java.awt.Frame"), String.class})
                        .invoke(null,
                                new Object[]{null, message});
            } catch (java.lang.reflect.InvocationTargetException awtError) {
                awtError.getTargetException().printStackTrace(System.err);
            } catch (Throwable awtError) {
                awtError.printStackTrace(System.err);
            }
        }

        System.exit(1);
    }
}
