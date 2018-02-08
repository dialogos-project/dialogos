
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

    public static final int MAJOR_VERSION = 1;
    public static final int MINOR_VERSION = 9;
    public static final int BUGFIX_VERSION = 0;
//    public static final String NONRELEASE = "beta1";
    public static final boolean IS_NONRELEASE = true;
    
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

    public static String getVersion() {
        StringBuffer b = new StringBuffer();
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
        return b.toString();
    }

    public static void showAboutDialog() {

        String credits
                = // "<b>Das Entwickler-Team:\n\n" +
                "<b>design\nDaniel Bobbert\nPhilipp Detemple\nGerhard Fliedner\nC.J. Rupp\n\n"
                + "<b>implementation\nTimo Baumann\nDaniel Beck\nDaniel Bobbert\nArne Köhn\nAlexander Koller\n\n"
                + "<b>testing\nAnastasia Ammann\nMatthias Bauer\nDaniel Beck\nDaniel Bobbert\nPhilipp Detemple\nGerd Fliedner\nClaudia Grote\nHajo Keffer\nIris Kersten\nRoland Roller\nMichael Roth\nSarah Schmitt\nDiana Steffen";
        new AboutDialog(
                Version.PRODUCT_NAME,
                Version.getVersion(),
                "\u00A9 Universität des Saarlandes\n"
                + "\nThis product includes software\n"
                + "developed by the Apache Software\n"
                + "Foundation (http://www.apache.org/)",
                credits).show(null);
    }
}
