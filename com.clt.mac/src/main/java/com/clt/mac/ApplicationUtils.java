//
//  ApplicationUtils.java
//  AppleScript
//
//  Created by Daniel Bobbert on Sat Mar 20 2004.
//  Copyright (c) 2004 CLT Sprachtechnologie GmbH. All rights reserved.
//
package com.clt.mac;

public class ApplicationUtils {

    public static boolean DEBUG = false;

    public static boolean registerEventHandler(RequiredEventHandler handler) {

        if (ApplicationUtils.DEBUG) {
            System.out.println("Registering CoreSuite AppleEvent handlers.");
            System.out.flush();
        }

        // first try the "new" EAWT (as of 2004 - AK)
        try {
            if (ApplicationUtils.DEBUG) {
                System.out.println("Trying EAWT");
            }
            Class<?> cls = Class.forName("com.clt.mac.EAWTHandler");
            SystemEventAdapter sea = (SystemEventAdapter) cls.newInstance();
            sea.register(handler);
            if (ApplicationUtils.DEBUG) {
                System.out.println("Succeeded.");
            }
            return true;
        } catch (Throwable exn) {
            if (ApplicationUtils.DEBUG) {
                System.out.println("Failed:");
                exn.printStackTrace();
            }
        }

        // now try the Java 9 integration
        try {
            if (ApplicationUtils.DEBUG) {
                System.out.println("Trying Java 9");
            }
            Class<?> cls = Class.forName("com.clt.mac.Java9MacHandler");
            SystemEventAdapter sea = (SystemEventAdapter) cls.newInstance();
            sea.register(handler);
            if (ApplicationUtils.DEBUG) {
                System.out.println("Succeeded.");
            }
            return true;
        } catch (Throwable exn) {
            if (ApplicationUtils.DEBUG) {
                System.out.println("Failed:");
                exn.printStackTrace();
            }
        }

        // if we get here, EAWT failed, so try MRJ
        try {
            if (ApplicationUtils.DEBUG) {
                System.out.println("Trying MJRUtilities");
            }
            Class<?> cls = Class.forName("com.clt.mac.MRJHandler");
            SystemEventAdapter sea = (SystemEventAdapter) cls.newInstance();
            sea.register(handler);
            if (ApplicationUtils.DEBUG) {
                System.out.println("Succeeded.");
            }

            try {
                if (ApplicationUtils.DEBUG) {
                    System.out.println("Trying extended MJRUtilities");
                }
                Class<?> mrjext = Class.forName("com.clt.mac.MRJExtendedHandler");
                sea = (SystemEventAdapter) mrjext.newInstance();
                sea.register(handler);
                if (ApplicationUtils.DEBUG) {
                    System.out.println("Succeeded.");
                }
            } catch (Throwable exn) {
                if (ApplicationUtils.DEBUG) {
                    System.out.println("Failed:");
                    exn.printStackTrace();
                }
            }

            return true;
        } catch (Throwable exn) {
            if (ApplicationUtils.DEBUG) {
                System.out.println("Failed:");
                exn.printStackTrace();
            }
        }

        if (ApplicationUtils.DEBUG) {
            System.out.flush();
        }

        // still no luck, so return false
        return false;
    }
}
