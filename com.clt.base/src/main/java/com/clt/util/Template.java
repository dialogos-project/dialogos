package com.clt.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;

import com.clt.io.FileFormatException;

/**
 * @author dabo
 *
 */
public class Template {

    private TemplateBundle bundle;
    private Map<Selector, FormatString> formatStrings;

    public Template(TemplateBundle bundle) {

        this.bundle = bundle;
        this.formatStrings = new HashMap<Selector, FormatString>();
    }

    public boolean contains(String[] selectors) {

        return this.formatStrings.containsKey(new Selector(selectors));
    }

    public void add(String formatString, String[] selectors)
            throws FileFormatException {

        Selector selector = new Selector(selectors);
        if (this.formatStrings.containsKey(selector)) {
            throw new IllegalArgumentException("Duplicate template definition");
        }
        this.formatStrings.put(selector, new FormatString(formatString));
    }

    public String format(Locale locale, Map<String, ?> data, Object... selectors) {

        Selector selector = new Selector(selectors);
        FormatString s = this.formatStrings.get(selector);
        if (s == null) {
            throw new IllegalArgumentException("Unknown selector: " + selector);
        }
        return s.format(locale, data);
    }

    private interface Generator {

        public String format(Locale locale, Map<String, ?> data);
    }

    private class FormatString
            extends Group {

        FormatString(String s)
                throws FileFormatException {

            this.parse(s);
        }

        private void parse(String s)
                throws FileFormatException {

            Stack<Group> groups = new Stack<Group>();
            groups.push(this);

            int pos = 0;
            StringBuilder buffer = new StringBuilder();
            while (pos < s.length()) {
                char c = s.charAt(pos);
                if ("{}$".indexOf(c) >= 0) {
                    if (buffer.length() > 0) {
                        groups.peek().add(new Constant(buffer.toString()));
                        buffer = new StringBuilder();
                    }
                }
                switch (c) {
                    case '\\':
                        pos++;
                        if (pos >= s.length()) {
                            throw new FileFormatException("Unterminated escape sequence");
                        }
                        switch (s.charAt(pos)) {
                            case 'n':
                                buffer.append('\n');
                                pos++;
                                break;
                            case 'r':
                                buffer.append('\b');
                                pos++;
                                break;
                            case 't':
                                buffer.append('\t');
                                pos++;
                                break;
                            case 'b':
                                buffer.append('\b');
                                pos++;
                                break;
                            case 'u':
                                pos++;
                                if (pos >= s.length() - 4) {
                                    throw new FileFormatException(
                                            "Unterminated unicode escape sequence");
                                }
                                for (int i = 0; i < 4; i++) {
                                    if ((c < '0') && (c > '9') && (c < 'a') && (c > 'f')
                                            && (c < 'A')
                                            && (c > 'F')) {
                                        throw new FileFormatException(
                                                "Illegal unicode escape sequence");
                                    }
                                }
                                buffer.append(Integer.parseInt(s.substring(pos, pos + 4), 16));
                                pos += 4;
                                break;
                            case '\'':
                                buffer.append('\'');
                                pos++;
                                break;
                            case '"':
                                buffer.append('"');
                                pos++;
                                break;
                            case '\\':
                                buffer.append('\\');
                                pos++;
                                break;
                            default:
                                if ((s.charAt(pos) >= '0') && (s.charAt(pos) <= '9')) {
                                    if (pos >= s.length() - 3) {
                                        throw new FileFormatException(
                                                "Unterminated octal escape sequence");
                                    }
                                    for (int i = 0; i < 4; i++) {
                                        if ((c < '0') && (c > '9')) {
                                            throw new FileFormatException(
                                                    "Illegal octal escape sequence");
                                        }
                                    }
                                    buffer.append(Integer.parseInt(s.substring(pos, pos + 3), 8));
                                    pos += 3;
                                } else {
                                    throw new FileFormatException("Illegal escape sequence");
                                }
                        }
                        break;
                    case '{':
                        groups.push(new Group());
                        pos++;
                        break;
                    case '}':
                        Group g = groups.pop();
                        if (groups.size() == 0) {
                            throw new FileFormatException("Template format error");
                        }
                        groups.peek().add(g);
                        pos++;
                        break;
                    case '$':
                        boolean subTemplate = false;
                        pos++;
                        if ((pos < s.length()) && (s.charAt(pos) == '$')) {
                            pos++;
                            subTemplate = true;
                        }
                        int start = pos;
                        while ((pos < s.length())
                                && Character.isUnicodeIdentifierPart(s.charAt(pos))) {
                            pos++;
                        }
                        if (pos <= start) {
                            throw new com.clt.io.FileFormatException("Empty variable name");
                        }

                        String name = s.substring(start, pos);
                        String selectors[] = new String[0];
                        if ((pos < s.length()) && (s.charAt(pos) == '[')) {
                            if (subTemplate) {
                                start = ++pos;
                                while ((pos < s.length()) && (s.charAt(pos) != ']')) {
                                    pos++;
                                }
                                if (pos >= s.length()) {
                                    throw new com.clt.io.FileFormatException(
                                            "Unclosed selector list in invocation of sub-template "
                                            + name);
                                }
                                String selector = s.substring(start, pos);
                                selectors = StringTools.split(selector, ",", true);
                                pos++;
                            } else {
                                throw new com.clt.io.FileFormatException(
                                        "You can add selectros only to a sub-template invocation");
                            }
                        }
                        // System.out.println(subTemplate + " " + name);
                        if (subTemplate) {
                            groups.peek().add(new SubTemplate(name, selectors));
                        } else {
                            groups.peek().add(new Variable(name));
                        }
                        break;
                    default:
                        buffer.append(s.charAt(pos));
                        pos++;
                        break;
                }
            }
            if (groups.size() != 1) {
                throw new IllegalArgumentException("Unclosed group");
            }
            if (buffer.length() > 0) {
                groups.peek().add(new Constant(buffer.toString()));
            }
        }

        @Override
        public String format(Locale locale, Map<String, ?> data) {

            StringBuilder b = new StringBuilder();
            for (Generator g : this.generators) {
                String s = g.format(locale, data);
                if (s == null) {
                    throw new IllegalArgumentException(
                            "Missing value for a template variable in: "
                            + g);
                }
                b.append(s);
            }
            return b.toString();
        }
    }

    private class Constant
            implements Generator {

        private String content;

        Constant(String content) {

            this.content = content;
        }

        public String format(Locale locale, Map<String, ?> data) {

            return this.content;
        }

        @Override
        public String toString() {

            return this.content;
        }
    }

    private class Variable
            implements Generator {

        private String name;

        Variable(String name) {

            this.name = name;
        }

        public String format(Locale locale, Map<String, ?> data) {

            Object value = data.get(this.name);
            if (value == null) {
                return null;
            } else {
                return value.toString();
            }
        }

        @Override
        public String toString() {

            return "$" + this.name;
        }
    }

    private class SubTemplate
            implements Generator {

        private String name;
        private String[] selectors;

        SubTemplate(String name, String[] selectors) {

            this.name = name;
            this.selectors = selectors;
        }

        public String format(Locale locale, Map<String, ?> data) {

            Template t = Template.this.bundle.getTemplate(locale, this.name);
            if (t == null) {
                return null;
            } else {
                String sub = t.format(locale, data, (Object[]) this.selectors);
                if (sub.trim().length() == 0) {
                    return null;
                } else {
                    return sub;
                }
            }
        }

        @Override
        public String toString() {

            return "$$" + this.name + Arrays.toString(this.selectors);
        }
    }

    private class Group
            implements Generator {

        protected List<Generator> generators;

        public Group() {

            this.generators = new ArrayList<Generator>();
        }

        public void add(Generator generator) {

            this.generators.add(generator);
        }

        public String format(Locale locale, Map<String, ?> data) {

            StringBuilder b = new StringBuilder();
            for (Generator g : this.generators) {
                String s = g.format(locale, data);
                if (s == null) {
                    return "";
                }
                b.append(s);
            }
            return b.toString();
        }

        @Override
        public String toString() {

            StringBuilder b = new StringBuilder();
            b.append("{");
            for (Generator g : this.generators) {
                b.append(g);
            }
            b.append("}");
            return b.toString();
        }
    }

    private static class Selector {

        String s[];

        public Selector(Object[] o) {

            this.s = new String[o.length];
            for (int i = 0; i < o.length; i++) {
                this.s[i] = o[i].toString();
            }
        }

        @Override
        public int hashCode() {

            int hash = this.s.length;
            for (int i = 0; i < this.s.length; i++) {
                hash = hash * 31 + this.s[i].hashCode();
            }
            return hash;
        }

        @Override
        public boolean equals(Object o) {

            if (o instanceof Selector) {
                Selector s = (Selector) o;
                if (s.s.length != this.s.length) {
                    return false;
                }
                for (int i = 0; i < s.s.length; i++) {
                    if (!s.s[i].equals(this.s[i])) {
                        return false;
                    }
                }
                return true;
            } else {
                return false;
            }
        }

        @Override
        public String toString() {

            return Arrays.toString(this.s);
        }
    }
}
