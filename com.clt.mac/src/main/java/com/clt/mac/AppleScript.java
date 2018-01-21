package com.clt.mac;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

class AppleScript {

    private static boolean libraryLoaded = false;

    public static void initLibrary() {

        if (!AppleScript.libraryLoaded) {
            System.loadLibrary("MacUtilities");
            AppleScript.libraryLoaded = true;
        }
    }

    public AppleScript()
            throws InstantiationException {

        try {
            AppleScript.initLibrary();
        } catch (UnsatisfiedLinkError exn) {
            throw new InstantiationException(exn.getLocalizedMessage());
        }
    }

    public void installEventHandler(String eventClass, String eventID,
            AppleEventHandler handler)
            throws MacError {

        this.AEInstallEventHandler(eventClass, eventID, handler);
    }

    public void sendEvent(String targetSignature, String eventClass,
            String eventID)
            throws MacError {

        this.AESend(targetSignature, eventClass, eventID);
    }

    public Object doScript(String script)
            throws MacError {

        return this.DoScript(script);
    }

    private native void AESend(String targetSignature, String eventClass,
            String eventID)
            throws MacError;

    private native void AEInstallEventHandler(String eventClass, String eventID,
            Object handler)
            throws MacError;

    private native Object DoScript(String script)
            throws MacError;

    public static void prettyPrint(Object o) {

        AppleScript.prettyPrint(o, new PrintWriter(System.out));
    }

    public static void prettyPrint(Object o, PrintWriter out) {

        AppleScript.prettyPrint(o, out, 0);
        out.flush();
    }

    private static void prettyPrint(Object o, PrintWriter out, int indent) {

        if (o instanceof List) {
            List<?> v = (List) o;
            if (v.size() == 0) {
                out.println("[ ]");
            } else {
                out.println("[");
                indent++;
                for (Object element : v) {
                    for (int j = 0; j < indent; j++) {
                        out.print("\t");
                    }
                    AppleScript.prettyPrint(element, out, indent);
                }
                indent--;
                for (int j = 0; j < indent; j++) {
                    out.print("\t");
                }
                out.println("]");
            }
        } else if (o instanceof Map) {
            Map<?, ?> h = (Map) o;
            if (h.size() == 0) {
                out.println("{ }");
            } else {
                out.println("{");
                indent++;
                for (Iterator<?> keys = h.keySet().iterator(); keys.hasNext();) {
                    Object key = keys.next();
                    for (int j = 0; j < indent; j++) {
                        out.print("\t");
                    }
                    out.print(key);
                    out.print(" = ");
                    AppleScript.prettyPrint(h.get(key), out, indent);
                }
                indent--;
                for (int j = 0; j < indent; j++) {
                    out.print("\t");
                }
                out.println("}");
            }
        } else {
            out.println(o);
        }
    }
}
