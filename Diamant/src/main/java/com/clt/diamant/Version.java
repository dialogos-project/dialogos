package com.clt.diamant;

import com.clt.gui.AboutDialog;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 *
 * @author Daniel Bobbert
 * @version 1.0
 */
public class Version {

    public static boolean HICOLOR = false;
    public static boolean ANIMATE = false;
    public static boolean DEBUG = false;

    public static final String PRODUCT_NAME = "DialogOS";

    public static final int MAJOR_VERSION = 2;
    public static final int MINOR_VERSION = 0;
    public static final int BUGFIX_VERSION = 2;
//    public static final String NONRELEASE = "beta1";
    public static final boolean IS_NONRELEASE = false;

    public static String getShortGitRevision() {
        InputStream is = Version.class.getResourceAsStream("/VERSION");
        Properties props = new Properties();

        try {
            props.load(is);
            return props.getProperty("shortGitRevision");
        } catch (IOException ex) {
            return null;
        }
    }

    public static String getBuildTimestamp() {
        InputStream is = Version.class.getResourceAsStream("/VERSION");
        Properties props = new Properties();

        try {
            props.load(is);
            return props.getProperty("buildTimestamp");
        } catch (IOException ex) {
            return null;
        }
    }

    public static String[] getVersionLines() {
        StringBuffer b = new StringBuffer();

        b.append("v");
        b.append(Version.MAJOR_VERSION);
        b.append('.');
        b.append(Version.MINOR_VERSION);
        if (Version.BUGFIX_VERSION != 0) {
            b.append('.');
            b.append(Version.BUGFIX_VERSION);
        }

        if (Version.IS_NONRELEASE) {
            b.append(" rev ");
            b.append(getShortGitRevision());
        }

        if( Version.IS_NONRELEASE ) {
            String line2 = getBuildTimestamp();
            return new String[] { b.toString(), line2 };
        } else {
            return new String[] { b.toString() };
        }
    }

    public static String getVersion() {
        String[] lines = getVersionLines();

        if( lines.length > 1 ) {
            String s = String.join("<br/>", lines);
            return "<html>" + s + "</html>";
        } else {
            return lines[0];
        }
    }

    public static String getVersionNoLinebreaks() {
        String[] lines = getVersionLines();
        return String.join(" ", lines);
    }

    private static final String[] designers = {
        "Daniel Bobbert",
        "Philipp Detemple",
        "Gerhard Fliedner",
        "C.J. Rupp"
    };

    private static final String[] implementers = {
        "Timo Baumann (CMU)",
        "Daniel Beck (CLT)",
        "Daniel Bobbert (CLT)",
        "Bri Burr (U Hamburg)",
        "Vincent Dahmen (U Hamburg)",
        "Max Friedrich (U Hamburg)",
        "Otis Juliusson (U Hamburg)",
        "Arne Köhn (U Hamburg)",
        "Till Kollenda (U Hamburg)",
        "Alexander Koller (Saarland U)",
        "Nicolas Schroh (U Hamburg)",
        "Phil Sehlmeyer (U Hamburg)",
        "André Simon (U Hamburg)"
    };

    public static String join(String label, String[] people) {
        StringBuffer buf = new StringBuffer();
        buf.append("<b>");
        buf.append(label);

        for (String person : people) {
            buf.append("\n");
            buf.append(person);
        }

        buf.append("\n\n");

        return buf.toString();
    }

    public static void showAboutDialog() {

        String credits
                = join("design", designers)
                + join("implementation", implementers);

// "<b>Das Entwickler-Team:\n\n" +
//                "<b>design\nDaniel Bobbert\nPhilipp Detemple\nGerhard Fliedner\nC.J. Rupp\n\n"
//                + "<b>implementation\nTimo Baumann\nDaniel Beck\nDaniel Bobbert\nArne Köhn\nAlexander Koller\n\n"
//                + "<b>testing\nAnastasia Ammann\nMatthias Bauer\nDaniel Beck\nDaniel Bobbert\nPhilipp Detemple\nGerd Fliedner\nClaudia Grote\nHajo Keffer\nIris Kersten\nRoland Roller\nMichael Roth\nSarah Schmitt\nDiana Steffen";
        new AboutDialog(
                Version.PRODUCT_NAME,
                Version.getVersion(),
                "\u00A9 Universität des Saarlandes\n"
                + "\nDialogOS includes the Sphinx speech\n"
                + "recognizer (\u00A9 Carnegie Mellon University)\n"
                + "and the MaryTTS speech synthesizer\n"
                + "(\u00A9 DFKI GmbH).\n"
                + "\nThis product includes software\n"
                + "developed by the Apache Software\n"
                + "Foundation (http://www.apache.org/)",
                credits).show(null);
    }
}
