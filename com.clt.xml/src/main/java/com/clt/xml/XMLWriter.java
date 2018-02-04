package com.clt.xml;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class XMLWriter extends IndentedWriter {

    private static final String lineSeparator = "\r\n";

    public XMLWriter(File f)
            throws IOException {

        this(f, null);
    }

    public XMLWriter(File f, String encoding) throws IOException {

        this(new BufferedOutputStream(new FileOutputStream(f)), encoding);
    }

    public XMLWriter(OutputStream out)
            throws IOException {

        this(out, null);
    }

    public XMLWriter(OutputStream out, String encoding) throws IOException {

        this(out, encoding, false);
    }

    public XMLWriter(OutputStream out, String encoding, boolean append) throws IOException {

        super(new BufferedWriter(new OutputStreamWriter(out, XMLWriter.getJavaEncodingName(encoding))), false);

        this.setLineSeparator(XMLWriter.lineSeparator);

        if (append) {
            this.indent();
        } else {
            this.println("<?xml version=\"1.0\" encoding=\"" + XMLWriter.getXMLEncodingName(encoding) + "\"?>");
        }
    }

    public XMLWriter(Writer out) {

        this(out, false);
    }

    public XMLWriter(Writer out, boolean append) {

        super(new BufferedWriter(out), false);

        if (append) {
            this.indent();
        } else {
            if (out instanceof OutputStreamWriter) {
                String encoding = ((OutputStreamWriter) out).getEncoding();
                this.println("<?xml version=\"1.0\" encoding=\""
                        + XMLWriter.getXMLEncodingName(encoding) + "\"?>");
            } else {
                this.println("<?xml version=\"1.0\"?>");
            }
        }
    }

    private static String getXMLEncodingName(String encoding) {

        if ((encoding == null) || encoding.equalsIgnoreCase("UTF8")
                || encoding.equalsIgnoreCase("UTF-8")) {
            return "UTF-8";
        } else {
            return encoding;
        }
    }

    private static String getJavaEncodingName(String encoding) {

        if ((encoding == null) || encoding.equalsIgnoreCase("UTF8")
                || encoding.equalsIgnoreCase("UTF-8")) {
            return "UTF8";
        } else {
            return encoding;
        }
    }

    public void printElement(String name, Object value) {

        this.printElement(name, null, null, value);
    }

    public void printElement(String name, String[] argnames, Object[] argvalues) {

        this.printElement(name, argnames, argvalues, null);
    }

    public void printElement(String name, String[] argnames, Object[] argvalues,
            Object value) {

        this.print("<" + name);
        if (argnames != null) {
            if (argvalues == null ? true : argnames.length != argvalues.length) {
                throw new IllegalArgumentException(
                        "Argument names and values do not match");
            }
            for (int i = 0; i < argnames.length; i++) {
                if (argvalues[i] != null) {
                    this.print(" " + argnames[i] + "=\""
                            + XMLWriter.encode(argvalues[i].toString()) + "\"");
                }
            }
        }
        if (value != null) {
            this.print(">");
            this.print(XMLWriter.encode(value.toString()));
            this.print("</");
            this.print(name);
            this.println(">");
        } else {
            this.println("/>");
        }
    }

    public void openElement(String name) {

        this.openElement(name, null, null);
    }

    public void openElement(String name, String[] argnames, Object[] argvalues) {

        this.print("<" + name);
        if (argnames != null) {
            if (argvalues == null ? true : argnames.length != argvalues.length) {
                throw new IllegalArgumentException(
                        "Argument names and values do not match");
            }
            for (int i = 0; i < argnames.length; i++) {
                if (argvalues[i] != null) {
                    this.print(" " + argnames[i] + "=\""
                            + XMLWriter.encode(argvalues[i].toString()) + "\"");
                }
            }
        }
        this.println(">");
        this.indent();
    }

    public void closeElement(String name) {

        this.unindent();
        this.println("</" + name + ">");
    }

    public static String encode(String s) {

        StringBuilder b = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            switch (s.charAt(i)) {
                case '&':
                    b.append("&amp;");
                    break;
                case '<':
                    b.append("&lt;");
                    break;
                case '>':
                    b.append("&gt;");
                    break;
                case '\"':
                    b.append("&quot;");
                    break;
                case '\n':
                    b.append(XMLWriter.lineSeparator);
                    break;
                case '\r':
                    if ((i + 1 < s.length()) && (s.charAt(i + 1) == '\n')) {
                        b.append(XMLWriter.lineSeparator);
                        i++;
                    }
                    break;
                // case '\'':
                // b.append("&apos;");
                // break;

                default:
                    b.append(s.charAt(i));
                    break;
            }
        }
        return b.toString();
    }
}
