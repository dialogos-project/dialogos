package com.clt.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author dabo
 *
 */
public class TemplateBundle {

    private Map<Locale, Map<String, Template>> bundle;

    public TemplateBundle() {

        this.bundle = new HashMap<Locale, Map<String, Template>>();
    }

    Template getTemplate(Locale locale, String name) {

        if (locale == null) {
            locale = Locale.getDefault();
        }
        Map<String, Template> templates = this.bundle.get(locale);
        return templates.get(name);
    }

    public String format(String template, Map<String, ?> data,
            Object... selectors) {

        return this.format(null, template, data, selectors);
    }

    public String format(Locale locale, String template, Map<String, ?> data,
            Object... selectors) {

        Template t = this.getTemplate(locale, template);
        if (t != null) {
            return t.format(locale, data, selectors);
        } else {
            throw new IllegalArgumentException("Unknown template " + template);
        }
    }

    public void read(Locale locale, Reader r)
            throws IOException {

        BufferedReader in = new BufferedReader(r);
        String line;
        int line_number = 0;
        while ((line = in.readLine()) != null) {
            line_number++;
            String name;
            String selectors[];
            String formatString;

            if (line.trim().length() == 0) {
                continue;
            }

            if (line.startsWith("[") || line.startsWith("#") || line.startsWith("//")) {
                continue;
            }

            int pos = 0;
            while ((pos < line.length()) && Character.isWhitespace(line.charAt(pos))) {
                pos++;
            }
            int start = pos;
            while ((pos < line.length())
                    && Character.isUnicodeIdentifierPart(line.charAt(pos))) {
                pos++;
            }

            if (pos == start) {
                throw new com.clt.io.FileFormatException("Empty template name in line "
                        + line_number);
            }
            name = line.substring(start, pos);
            while ((pos < line.length()) && Character.isWhitespace(line.charAt(pos))) {
                pos++;
            }
            if (pos >= line.length()) {
                throw new com.clt.io.FileFormatException(
                        "Missing content for template " + name
                        + " in line " + line_number);
            }
            if (line.charAt(pos) == '[') {
                start = ++pos;
                while ((pos < line.length()) && (line.charAt(pos) != ']')) {
                    pos++;
                }
                if (pos >= line.length()) {
                    throw new com.clt.io.FileFormatException();
                }
                String selector = line.substring(start, pos);
                selectors = StringTools.split(selector, ",", true);
                pos++;
                while ((pos < line.length())
                        && Character.isWhitespace(line.charAt(pos))) {
                    pos++;
                }
            } else {
                selectors = new String[0];
            }
            if ((pos >= line.length()) || (line.charAt(pos) != '=')) {
                throw new com.clt.io.FileFormatException(
                        "Missing = after template name in line "
                        + line_number);
            }
            pos++;

            formatString = line.substring(pos).trim();

            // System.out.println("New template: " + name);
            // System.out.println("Selectors: " + Arrays.toString(selectors));
            Map<String, Template> templates = this.bundle.get(locale);
            if (templates == null) {
                templates = new HashMap<String, Template>();
                this.bundle.put(locale, templates);
            }

            Template t = templates.get(name);
            if (t == null) {
                t = new Template(this);
                templates.put(name, t);
            }

            if (t.contains(selectors)) {
                throw new com.clt.io.FileFormatException(
                        "Duplicate definition for template "
                        + name + Arrays.toString(selectors));
            }
            t.add(formatString, selectors);
        }
    }

    public static void main(String... strings) {

        TemplateBundle bundle = new TemplateBundle();
        String templates = "x[a] = Bitte nennen Sie den Zielort in $city\n"
                + "x[b] = Bitte nennen Sie die Straße{ in $city} $$y[c]\n"
                + "y[c] = oder{ in $city}\n";

        try {
            bundle.read(Locale.getDefault(), new java.io.StringReader(templates));

            Map<String, Object> data = new HashMap<String, Object>();
            data.put("city", "M\u00FCnchen");
            System.out.println(bundle.format("x", data, "a"));
            System.out.println(bundle.format("x", data, "b"));
            data.remove("city");
            System.out.println(bundle.format("x", data, "b"));
            System.out.println(bundle.format("x", data, "a"));
        } catch (IOException exn) {
            // TODO Auto-generated catch block
        }
    }
}
