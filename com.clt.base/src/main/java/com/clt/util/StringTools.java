package com.clt.util;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.text.Collator;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.util.*;

public class StringTools {
    
    /**
     * Removes all HTML elements from the string.
     * 
     * @param s
     * @return 
     */
    public static String stripHtml(String s) {
        String noHtml = s.replaceAll("<[^>]*>", " ");
        return noHtml.trim().replaceAll(" +", " ");
    }

    /**
     * Joins a list of strings into a larger string. This is similar to Perl's
     * <code>join()</code> function.
     *
     * @param strings a list of strings
     * @param separator a string that is inserted between any two members of the
     * list
     * @return the joined string
     */
    public static <E> String join(Collection<E> strings, String separator) {
        boolean first = true;
        StringBuffer sb = new StringBuffer();

        for (Object s : strings) {
            if (first) {
                first = false;
            } else {
                sb.append(separator);
            }

            sb.append(s.toString());
        }

        return sb.toString();

    }

    /**
     * Splits a string around the given character. The resulting sub strings
     * will not contain the split character. Two consecutive split charaters
     * will create an empty string in the resulting array.
     */
    public static final String[] split(String s, char splitChar) {

        return StringTools.split(s, new char[]{splitChar});
    }

    private static int indexOf(String s, char[] delimiters, int offset) {

        for (int i = offset; i < s.length(); i++) {
            char c = s.charAt(i);
            for (int j = 0; j < delimiters.length; j++) {
                if (c == delimiters[j]) {
                    return i;
                }
            }
        }

        return -1;
    }

    /**
     * Splits a string around matches of any of the given characters. The
     * resulting sub strings will not contain the split characters. Two
     * consecutive split charaters will create an empty string in the resulting
     * array.
     */
    public static final String[] split(String s, char[] c) {

        int i = 0;
        int numWords = 1;
        while ((i = StringTools.indexOf(s, c, i)) != -1) {
            i++;
            numWords++;
        }

        String[] words = new String[numWords];
        int index = 0;
        int start = 0, end;

        while ((end = StringTools.indexOf(s, c, start)) != -1) {
            words[index++] = s.substring(start, end);
            start = end + 1;
        }
        if (start <= s.length()) {
            words[index] = s.substring(start);
        }

        return words;
    }

    /**
     * Splits a string around matches of the given substring. The resulting sub
     * strings will not contain the split string. Two consecutive split strings
     * will create an empty string in the resulting array.
     */
    public static final String[] split(String s, String delimiter) {

        return StringTools.split(s, delimiter, false);
    }

    public static final String[] split(String s, String delimiter, boolean trim) {

        int i = 0;
        int numWords = 1;
        while ((i = s.indexOf(delimiter, i)) != -1) {
            i += delimiter.length();
            numWords++;
        }

        String[] words = new String[numWords];
        int index = 0;
        int start = 0, end;

        while ((end = s.indexOf(delimiter, start)) != -1) {
            words[index++] = s.substring(start, end);
            start = end + delimiter.length();
        }
        if (start <= s.length()) {
            words[index] = s.substring(start);
        }

        if (trim) {
            for (i = 0; i < words.length; i++) {
                words[i] = words[i].trim();
            }
        }

        return words;
    }

    /**
     * Normalize white space in a string. This method will trim leading and
     * trailing white space, convert tabs to spaces and reduce multiple
     * consecutive spaces to a single space.
     *
     * @param s The string to normalize.
     * @return The normalized string.
     */
    public static String normalize(String s) {

        if (s.length() == 0) {
            return s;
        }

        char[] buf = s.toCharArray();
        int i, j;
        int start = 0, end = buf.length - 1;

        while ((buf[end] == ' ') || (buf[end] == '\t')) {
            end--;
            if (end == -1) {
                return "";
            }
        }

        while ((buf[start] == ' ') || (buf[start] == '\t')) {
            start++;
        }

        for (i = j = start; i <= end; i++) {
            if ((buf[i] == ' ') || (buf[i] == '\t')) {
                // Das kann nicht beim ersten Zeichen passieren,
                // weil wir getrimmt haben. Das j-1 geht also bedenkenlos.
                if (buf[j - 1] != ' ') {
                    buf[j++] = ' '; // Leerzeichen nur kopieren, wenn davor
                    // keins war
                }
            } else {
                buf[j++] = buf[i]; // Normale Zeichen einfach kopieren
            }
        }
        return new String(buf, start, j - start);
    }

    /**
     * Check whether a string is <code>null</code> or empty.
     *
     * @return true if the given string is <code>null</code> or has length
     * <code>0</code>
     */
    public static boolean isEmpty(String s) {

        return s != null ? s.length() == 0 : true;
    }

    /**
     * Convert a unicode charater to its UTF8 presentation. The resulting string
     * may have one to three 8bit characters as defined in the UTF8 standard.
     */
    public static final String toUTF(char c) {

        if (c >= 0x0800) {
            return new String(
                    new char[]{(char) ((c >>> 12) | 0xE0),
                        (char) (((c >>> 6) & 0x3F) | 0x80), (char) ((c & 0x3F) | 0x80)});
        } else if (c >= 0x0080) {
            return new String(new char[]{(char) ((c >>> 6) | 0xC0),
                (char) ((c & 0x3F) | 0x80)});
        } else {
            return new String(new char[]{c});
        }
    }

    /**
     * Convert a unicode string to its UTF8 presentation. The resulting string
     * will contain only 8bit characters as defined in the UTF8 standard.
     */
    public static final String toUTF(String s) {

        StringBuilder b = new StringBuilder((s.length() * 3) / 2);
        for (int i = 0; i < s.length(); i++) {
            b.append(StringTools.toUTF(s.charAt(i)));
        }
        return b.toString();
    }

    /**
     * Encode a string to HTML using named entities whereever possible. This
     * method method will also convert &lt;, &gt; and linebreaks to their
     * according entities.
     *
     * @see #toHTML(String, boolean)
     */
    public static String toHTML(String s) {

        return StringTools.toHTML(s, true);
    }

    /**
     * Encode a string to HTML using named entities whereever possible.
     * <p>
     * If <code>encodeTagsAndLinebreaks</code> is true, this method method will
     * also convert &lt;, &gt; and linebreaks to their according entities. If
     * your source string already contains HTML tags and you just want to make
     * sure that umlaut charaters are encoded correctly, set
     * <code>encodeTagsAndLinebreaks</code> to false.
     * </p>
     *
     * @see #toHTML(String, boolean)
     */
    public static String toHTML(String s, boolean encodeTagsAndLinebreaks) {

        StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            String encoded = StringTools.entities.get(Character.valueOf(c));
            if ((c == '\n') || (c == '<') || (c == '>')) {
                if (encodeTagsAndLinebreaks) {
                    b.append(encoded);
                } else {
                    b.append(c);
                }
            } else if (encoded != null) {
                b.append(encoded);
            } else if ((c >= 32) && (c < 128)) {
                b.append(c);
            } else if (c == '\u2022') {
                b.append("&middot;");
            } else if (c == '\u202F') {
                b.append("&nbsp;");
            } else {
                b.append("&#" + ((int) c) + ';');
            }
        }
        return b.toString();
    }

    /**
     * Decode all known HTML entities in <code>s</code> and return the decoded
     * string. This method will not interpret or alter HTML tags. It will only
     * decode the default character entities such as &amp;uuml;.
     */
    public static String parseHTML(String s) {

        StringBuilder b = new StringBuilder(s.length());
        int pos = 0;
        while (pos < s.length()) {
            if (s.charAt(pos) == '&') {
                boolean found = false;
                for (Iterator<Character> it = StringTools.entities.keySet().iterator(); !found
                        && it.hasNext();) {
                    Character c = it.next();
                    String entity = StringTools.entities.get(c);
                    if (s.startsWith(entity, pos)) {
                        b.append(c);
                        pos += entity.length();
                        found = true;
                    }
                }
                if (!found) {
                    if ((pos + 1 < s.length()) && (s.charAt(pos + 1) == '#')) {
                        int end = pos + 2;
                        while ((end < s.length()) && (s.charAt(end) != ';')) {
                            end++;
                        }
                        if ((end >= s.length()) || (end == pos + 2)) {
                            throw new IllegalArgumentException("Unknown escape sequence: "
                                    + s.substring(pos));
                        }
                        int n = 0;
                        try {
                            if ((s.charAt(pos + 2) == 'x') || (s.charAt(pos + 2) == 'X')) {
                                n = Integer.parseInt(s.substring(pos + 2, end), 16);
                            } else {
                                n = Integer.parseInt(s.substring(pos + 2, end), 10);
                            }
                        } catch (NumberFormatException exn) {
                            throw new IllegalArgumentException("Unknown escape sequence: "
                                    + s.substring(pos));
                        }
                        if ((n < 0) || (n > 0xFFFF)) {
                            throw new IllegalArgumentException("Unknown escape sequence: "
                                    + s.substring(pos));
                        }
                        b.append((char) n);
                        pos = end + 1;
                    } else {
                        throw new IllegalArgumentException("Unknown escape sequence: "
                                + s.substring(pos));
                    }
                }
            } else {
                b.append(s.charAt(pos++));
            }
        }
        return b.toString();
    }

    /**
     * Return a new string resulting from replacing all unprintable characters
     * in <code>s</code> by apropriate escape sequences.
     */
    public static String toSourceString(String s) {

        if (s == null) {
            return null;
        }

        StringBuilder b = new StringBuilder(s.length());
        b.append('\"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\n':
                    b.append("\\n");
                    break;
                case '\r':
                    b.append("\\r");
                    break;
                case '\t':
                    b.append("\\t");
                    break;
                case '\f':
                    b.append("\\f");
                    break;
                case '\b':
                    b.append("\\b");
                    break;
                case '\"':
                    b.append("\\\"");
                    break;
                case '\\':
                    b.append("\\\\");
                    break;
                default:
                    if (c < 0x20) {
                        b.append("\\x" + Integer.toHexString((c & 0xf0) >> 8)
                                + Integer.toHexString(c & 0x0f));
                    } else {
                        b.append(c);
                    }
                    break;
            }
        }
        b.append('\"');
        return b.toString();
    }

    /**
     * Create a hexadecimal string representation of a color.
     * <p>
     * <code>toHexString(Color.red)</code> = <code>"#ff0000"</code>
     * </p>
     * Colors encoded this way can be decoded using
     * {@link java.awt.Color#decode(java.lang.String)}
     */
    public static String toHexString(Color c) {

        return "#" + Integer.toHexString(c.getRed() / 16)
                + Integer.toHexString(c.getRed() % 16)
                + Integer.toHexString(c.getGreen() / 16)
                + Integer.toHexString(c.getGreen() % 16)
                + Integer.toHexString(c.getBlue() / 16)
                + Integer.toHexString(c.getBlue() % 16);
    }

    public static String toHexString(long number, int digits) {

        // convert to unsigned
        if (number < 0) {
            long n = 1;
            for (int i = 0; i < digits; i++) {
                n <<= 4;
            }
            number += n;
        }

        String s = Long.toHexString(number);
        StringBuilder b = new StringBuilder(digits + 2);
        for (int i = s.length(); i < digits; i++) {
            b.append('0');
        }
        b.append(s);
        return b.toString();
    }

    public static String toHexString(byte[] bytes, String delimiter) {

        StringBuilder b
                = new StringBuilder(bytes.length * (2 + delimiter.length()));

        for (int i = 0; i < bytes.length; i++) {
            if (i > 0) {
                b.append(delimiter);
            }
            b.append(StringTools.toHexString(bytes[i], 2));
        }

        return b.toString();
    }

    /**
     * Construct a map by splitting <code>s</code> and mapping each substring to
     * the given value.<br>
     * Example:<br>
     * <code>string2map("x, y, z", Boolean.TRUE)</code><br>
     * will result in a <code>Map&lt;String, Boolean&gt;</code> with mapping<br>
     * <code>"x" =&gt; Boolean.TRUE</code><br>
     * <code>"y" =&gt; Boolean.TRUE</code><br>
     * <code>"z" =&gt; Boolean.TRUE</code><br>
     */
    public static final <T> Map<String, T> string2map(String s, T value) {

        Map<String, T> h = new HashMap<String, T>();
        int start = 0, end = 0;
        while (start < s.length()) {
            end = s.indexOf(',', start);
            if (end == -1) {
                end = s.length();
            }
            String key = s.substring(start, end).trim();
            if (key.length() > 0) {
                h.put(key, value);
            }
            start = end + 1;
        }
        return h;
    }

    /**
     * Return the number of occurences of character <code>c</code> in
     * <code>s</code>. If <code>s</code> is <code>null</code>, this method will
     * return <code>0</code>.
     */
    public static final int numberOfOccurences(String s, char c) {

        if (s == null) {
            return 0;
        }
        int n = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == c) {
                n++;
            }
        }
        return n;
    }

    /**
     * Check whether the given array contains the string <code>s</code>. *
     * <code>s</code> may not be <code>null</code>.
     */
    public static final boolean contains(String[] array, String s) {

        return StringTools.indexOf(array, s) != -1;
    }

    /**
     * Return the position of <code>s</code> in the given array. <code>s</code>
     * may not be <code>null</code>.
     *
     * @return The index <code>n</code> if <code>array[n].equals(s)</code> of
     * <code>-1</code> if no string in the array equals <code>s</code>
     */
    public static final int indexOf(String[] array, String s) {

        for (int i = array.length - 1; i >= 0; i--) {
            if (s.equals(array[i])) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Check whether <code>s</code> contains at least one of the strings in
     * <code>substrings</code>.
     */
    public static final boolean containsOneOf(String s, String[] substrings) {

        for (int i = substrings.length - 1; i >= 0; i--) {
            if (s.indexOf(substrings[i]) >= 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Compare two string arrays for equality.
     *
     * @deprecated Use
     * {@link java.util.Arrays#equals(java.lang.Object[], java.lang.Object[])}
     * instead
     */
    public static final boolean equals(String[] x, String[] y) {

        if (x == y) {
            return true;
        } else if ((x == null) || (y == null)) {
            return false;
        } else if (x.length != y.length) {
            return false;
        }
        for (int i = 0; i < x.length; i++) {
            if (!x[i].equals(y[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Return a new string resulting from deleting all occurences of
     * <code>c</code> from <code>s</code>.
     */
    public static final String delete(String s, char c) {

        return StringTools.delete(s, c, 0);
    }

    /**
     * Return a new string resulting from deleting all occurences of
     * <code>c</code> starting at position <code>pos</code> from <code>s</code>.
     */
    public static final String delete(String s, char c, int pos) {

        if (s.indexOf(c, pos) < 0) {
            return s;
        }

        StringBuilder b = new StringBuilder(s.length());
        b.append(s.substring(0, pos));

        for (int i = pos; i < s.length(); i++) {
            if (s.charAt(i) != c) {
                b.append(s.charAt(i));
            }
        }
        return b.toString();
    }

    /**
     * Returns a new string resulting from replacing all occurrences of
     * <code>oldChar</code> in <code>s</code> with <code>newChar</code>.
     */
    public static final String replace(String s, char oldChar, char newChar) {

        return StringTools.replace(s, oldChar, newChar, 0);
    }

    /**
     * Returns a new string resulting from replacing all occurrences of
     * <code>oldChar</code> in <code>s</code> starting at position
     * <code>pos</code> with <code>newChar</code>.
     */
    public static final String replace(String s, char oldChar, char newChar,
            int pos) {

        if ((s.indexOf(oldChar, pos) < 0) || (oldChar == newChar)) {
            return s;
        }
        StringBuilder b;
        for (b = new StringBuilder(s); pos < s.length(); pos++) {
            if (s.charAt(pos) == oldChar) {
                b.setCharAt(pos, newChar);
            }
        }
        return b.toString();
    }

    /**
     * Returns a new string resulting from replacing all occurrences of
     * <code>oldString</code> in <code>s</code> with <code>newString</code>.
     */
    public static final String replace(String s, String oldString,
            String newString) {

        return StringTools.replace(s, oldString, newString, 0);
    }

    /**
     * Returns a new string resulting from replacing all occurrences of
     * <code>oldString</code> in <code>s</code> starting at position
     * <code>pos</code> with <code>newString</code>.
     */
    public static final String replace(String s, String oldString,
            String newString, int pos) {

        if (!newString.equals(oldString)) {
            StringBuilder b = new StringBuilder(s.length());
            int begin = 0;
            while ((pos = s.indexOf(oldString, pos)) >= 0) {
                b.append(s.substring(begin, pos));
                b.append(newString);
                pos += oldString.length();
                begin = pos;
            }
            if (begin < s.length()) {
                b.append(s.substring(begin));
            }
            return b.toString();
        } else {
            return s;
        }
    }

    /**
     * Construct a message from the given format string and the arguments.
     *
     * @return The formatted message
     * @see java.text.MessageFormat
     */
    public static final String format(String formatString, Object... args) {

        return MessageFormat.format(formatString, args);
    }

    /**
     * Construct a message from the given format string and the argument.
     *
     * @return The formatted message
     * @see java.text.MessageFormat
     */
    public static final String format(String formatString, boolean arg) {
        return MessageFormat.format(formatString, arg ? Misc.getString("true") : Misc.getString("false"));
    }

    /**
     * Construct a message from the given format string and the argument.
     *
     * @return The formatted message
     * @see java.text.MessageFormat
     */
    public static final String format(String formatString, char arg) {

        return MessageFormat.format(formatString, Character.valueOf(arg));
    }

    /**
     * Construct a message from the given format string and the argument.
     *
     * @return The formatted message
     * @see java.text.MessageFormat
     */
    public static final String format(String formatString, long arg) {

        return MessageFormat.format(formatString, Long.valueOf(arg));
    }

    /**
     * Construct a message from the given format string and the argument.
     *
     * @return The formatted message
     * @see java.text.MessageFormat
     */
    public static final String format(String formatString, double arg) {

        return MessageFormat.format(formatString, Double.valueOf(arg));
    }

    private static final Map<Character, String> entities;

    private static Collator Deutsch = null;

    static {
        String rules = null;
        try {
            rules
                    = "= '\u200b' = \u200c = \u200d = \u200e = \u200f"
                    + "= '\u0000' = '\u0001' = '\u0002' = '\u0003' = '\u0004' = '\u0005' = '\u0006' = '\u0007' = '\u0008' = '\u000e' = '\u000f'"
                    + "= '\u0011' = '\u0012' = '\u0014' = '\u0015' = '\u0016' = '\u0017' = '\u0018' = '\u0019' = '\u001a' = '\u001b' = '\u001c' = '\u001d' = '\u001e' = '\u001f'"
                    + "= \u0080 = \u0081 = \u0082 = \u0083 = \u0084 = \u0085 = \u0086 = \u0087 = \u0088 = \u0089 = \u008a = \u008b = \u008c = \u008d = \u008e = \u008f"
                    + "= \u0090 = \u0091 = \u0092 = \u0093 = \u0094 = \u0095 = \u0096 = \u0097 = \u0098 = \u0099 = \u009a = \u009b = \u009c = \u009d = \u009e = \u009f"
                    + "; '\u00a0' ; '\u3000' ; \ufeff ; '\u0009' ; '\u000c' ; \u000b"
                    + "; \u0301 ; \u0300 ; \u0306 ; \u0302 ; \u030c ; \u030a ; \u030d ; \u0308 ; \u030b ; \u0303 ; \u0307 ; \u0304 ; \u0337 ; \u0327 ; \u0328 ; \u0323"
                    + "; \u0332 ; \u0305 ; \u0309 ; \u030e ; \u030f ; \u0306\u0307"
                    + "; \u0311 ; \u0312 ; \u0313 ; \u0314 ; \u0315 ; \u0316 ; \u0317 ; \u0318 ; \u0319 ; \u031a ; \u031b ; \u031c ; \u031d ; \u031e ; \u031f ; \u0320"
                    + "; \u0321 ; \u0322 ; \u0324 ; \u0325 ; \u0326 ; \u0329 ; \u032a ; \u032b ; \u032c ; \u032d ; \u032e ; \u032f ; \u0330 ; \u0331 ; \u0333 ; \u0334"
                    + "; \u0335 ; \u0336 ; \u0338 ; \u0339 ; \u033a ; \u033b ; \u033c ; \u033d ; \u033e ; \u033f ; \u0340 ; \u0341 ; \u0342 ; \u0343 ; \u0308\u030d"
                    + "; \u0345 ; \u0360 ; \u0361 ; \u0483 ; \u0484 ; \u0485 ; \u0486 ; \u20d0 ; \u20d1 ; \u20d2 ; \u20d3 ; \u20d4 ; \u20d5 ; \u20d6 ; \u20d7 ; \u20d8"
                    + "; \u20d9 ; \u20da ; \u20db ; \u20dc ; \u20dd ; \u20de ; \u20df ; \u20e0 ; \u20e1 ; \u00ad ; \u2010 ; \u2011 ; \u2012 ; \u2013 ; \u2014 ; \u2015 ; \u2212"
                    + "< '\n' = '\r'"
                    + "< ' ' ; \u00A0 ; '\u2000' ; '\u2001' ; '\u2002' ; '\u2003' ; '\u2004' ; '\u2005' ; '\u2006' ; '\u2007' ; '\u2008' ; '\u2009' ; '\u200a'"
                    + "< '-' < '_' < '\u00af'"
                    + "< ',' < ';' < ':' < '!' < \u00a1 < '?' < '\u00bf' < '/' < '.' < '\u00b4' < '`' < '^' < '\u00a8' < '~' < '\u00b7' < '\u00b8'"
                    + "< ''' < \u2018, \u201a, \u201b < \u2019 < \u2039 < \u203a"
                    + "< '\"' < \u201c, \u201e, \u201f < \u201d < \u2033, \u02ba < \u00ab < \u00bb"
                    + "< '(' < ')' < '[' < ']' < '{' < '}'"
                    + "< \u00a7 < \u00b6 < \u00a9 < \u00ae < '@' < \u00a4 < \u0e3f < \u00a2 < \u20a1 < \u20a2"
                    + "< '$' < \u20ab < \u20ac < \u20a3 < \u20a4 < \u20a5 < \u20a6 < \u20a7 < \u00a3 < \u20a8 < \u20aa < \u20a9 < \u00a5"
                    + "< '*' < '\\' < '&' < '#' < '%' < '+' < '\u00b1' < '\u00f7' < '\u00d7' < '<' < '=' < '>' < \u00ac < '|' < \u00a6 < \u00b0 < \u00b5"
                    + "< 0 < 1 < 2 < 3 < 4 < 5 < 6 < 7 < 8 < 9"
                    + "< \u00bc < \u00bd < \u00be"
                    + "< a , A , \u00e6/E , \u00c6/E, \u00e4, \u00c4"
                    + "< b , B < c , C < d , D < \u00f0 , \u00d0 < e , E < f , F < g , G"
                    + "< h , H < i , I < j , J < k , K < l , L < m , M < n , N < o , O , \u0153/E , \u0152/E, \u00f6, \u00d6"
                    + "< p , P < q , Q < r , R < s , S , \u00df/S < t , T , \u00fe/H , \u00de/H < u , U, \u00fc, \u00dc"
                    + "< v , V < w , W < x , X < y , Y < z , Z";

            StringTools.Deutsch = new RuleBasedCollator(rules);
        } catch (ParseException e) {
            System.err.println("Parse Error at pos " + e.getErrorOffset() + "\n"
                    + e.getLocalizedMessage());
            System.err.println("rules at pos = "
                    + rules.substring(e.getErrorOffset() - 5, e
                            .getErrorOffset() + 5));

            StringTools.Deutsch = Collator.getInstance(Locale.GERMAN);
        }

        entities = new HashMap<Character, String>();
        StringTools.entities.put(Character.valueOf('\n'), "<br>");
        StringTools.entities.put(Character.valueOf('&'), "&amp;");
        StringTools.entities.put(Character.valueOf('<'), "&lt;");
        StringTools.entities.put(Character.valueOf('>'), "&gt;");
        StringTools.entities.put(Character.valueOf('\u0083'), "&fnof;");
        StringTools.entities.put(Character.valueOf('\u0086'), "&Yacute;");
        StringTools.entities.put(Character.valueOf('\u0087'), "&yacute;");
        StringTools.entities.put(Character.valueOf('\u008C'), "&OElig;");
        StringTools.entities.put(Character.valueOf('\u0095'), "&bull;");
        StringTools.entities.put(Character.valueOf('\u009C'), "&oelig;");
        StringTools.entities.put(Character.valueOf('\u009F'), "&Yuml;");

        StringTools.entities.put(Character.valueOf('\u00A0'), "&nbsp;");
        StringTools.entities.put(Character.valueOf('\u00A1'), "&iexcl;");
        StringTools.entities.put(Character.valueOf('\u00A2'), "&cent;");
        StringTools.entities.put(Character.valueOf('\u00A3'), "&pound;");
        StringTools.entities.put(Character.valueOf('\u00A4'), "&curren;");
        StringTools.entities.put(Character.valueOf('\u00A5'), "&yen;");
        StringTools.entities.put(Character.valueOf('\u00A6'), "&brvbar;");
        StringTools.entities.put(Character.valueOf('\u00A7'), "&sect;");
        StringTools.entities.put(Character.valueOf('\u00A8'), "&uml;");
        StringTools.entities.put(Character.valueOf('\u00A9'), "&copy;");
        StringTools.entities.put(Character.valueOf('\u00AA'), "&ordf;");
        StringTools.entities.put(Character.valueOf('\u00AB'), "&laquo;");
        StringTools.entities.put(Character.valueOf('\u00AC'), "&not;");
        StringTools.entities.put(Character.valueOf('\u00AD'), "&shy;");
        StringTools.entities.put(Character.valueOf('\u00AE'), "&reg;");
        StringTools.entities.put(Character.valueOf('\u00AF'), "&macr;");
        StringTools.entities.put(Character.valueOf('\u00B0'), "&deg;");
        StringTools.entities.put(Character.valueOf('\u00B1'), "&plusmn;");
        StringTools.entities.put(Character.valueOf('\u00B2'), "&sup2;");
        StringTools.entities.put(Character.valueOf('\u00B3'), "&sup3;");
        StringTools.entities.put(Character.valueOf('\u00B4'), "&acute;");
        StringTools.entities.put(Character.valueOf('\u00B5'), "&micro;");
        StringTools.entities.put(Character.valueOf('\u00B6'), "&para;");
        StringTools.entities.put(Character.valueOf('\u00B7'), "&middot;");
        StringTools.entities.put(Character.valueOf('\u00B8'), "&cedil;");
        StringTools.entities.put(Character.valueOf('\u00B9'), "&sup1;");
        StringTools.entities.put(Character.valueOf('\u00BA'), "&ordm;");
        StringTools.entities.put(Character.valueOf('\u00BB'), "&raquo;");
        StringTools.entities.put(Character.valueOf('\u00BC'), "&frac14;");
        StringTools.entities.put(Character.valueOf('\u00BD'), "&frac12;");
        StringTools.entities.put(Character.valueOf('\u00BE'), "&frac34;");
        StringTools.entities.put(Character.valueOf('\u00BF'), "&iquest;");
        StringTools.entities.put(Character.valueOf('\u00C0'), "&Agrave;");
        StringTools.entities.put(Character.valueOf('\u00C1'), "&Aacute;");
        StringTools.entities.put(Character.valueOf('\u00C2'), "&Acirc;");
        StringTools.entities.put(Character.valueOf('\u00C3'), "&Atilde;");
        StringTools.entities.put(Character.valueOf('\u00C4'), "&Auml;");
        StringTools.entities.put(Character.valueOf('\u00C5'), "&Aring;");
        StringTools.entities.put(Character.valueOf('\u00C6'), "&AElig;");
        StringTools.entities.put(Character.valueOf('\u00C7'), "&Ccedil;");
        StringTools.entities.put(Character.valueOf('\u00C8'), "&Egrave;");
        StringTools.entities.put(Character.valueOf('\u00C9'), "&Eacute;");
        StringTools.entities.put(Character.valueOf('\u00CA'), "&Ecirc;");
        StringTools.entities.put(Character.valueOf('\u00CB'), "&Euml;");
        StringTools.entities.put(Character.valueOf('\u00CC'), "&Igrave;");
        StringTools.entities.put(Character.valueOf('\u00CD'), "&Iacute;");
        StringTools.entities.put(Character.valueOf('\u00CE'), "&Icirc;");
        StringTools.entities.put(Character.valueOf('\u00CF'), "&Iuml;");
        StringTools.entities.put(Character.valueOf('\u00D0'), "&ETH;");
        StringTools.entities.put(Character.valueOf('\u00D1'), "&Ntilde;");
        StringTools.entities.put(Character.valueOf('\u00D2'), "&Ograve;");
        StringTools.entities.put(Character.valueOf('\u00D3'), "&Oacute;");
        StringTools.entities.put(Character.valueOf('\u00D4'), "&Ocirc;");
        StringTools.entities.put(Character.valueOf('\u00D5'), "&Otilde;");
        StringTools.entities.put(Character.valueOf('\u00D6'), "&Ouml;");
        StringTools.entities.put(Character.valueOf('\u00D7'), "&times;");
        StringTools.entities.put(Character.valueOf('\u00D8'), "&Oslash;");
        StringTools.entities.put(Character.valueOf('\u00D9'), "&Ugrave;");
        StringTools.entities.put(Character.valueOf('\u00DA'), "&Uacute;");
        StringTools.entities.put(Character.valueOf('\u00DB'), "&Ucirc;");
        StringTools.entities.put(Character.valueOf('\u00DC'), "&Uuml;");
        StringTools.entities.put(Character.valueOf('\u00DD'), "&Yacute;");
        StringTools.entities.put(Character.valueOf('\u00DE'), "&THORN;");
        StringTools.entities.put(Character.valueOf('\u00DF'), "&szlig;");
        StringTools.entities.put(Character.valueOf('\u00E0'), "&agrave;");
        StringTools.entities.put(Character.valueOf('\u00E1'), "&aacute;");
        StringTools.entities.put(Character.valueOf('\u00E2'), "&acirc;");
        StringTools.entities.put(Character.valueOf('\u00E3'), "&atilde;");
        StringTools.entities.put(Character.valueOf('\u00E4'), "&auml;");
        StringTools.entities.put(Character.valueOf('\u00E5'), "&aring;");
        StringTools.entities.put(Character.valueOf('\u00E6'), "&aelig;");
        StringTools.entities.put(Character.valueOf('\u00E7'), "&ccedil;");
        StringTools.entities.put(Character.valueOf('\u00E8'), "&egrave;");
        StringTools.entities.put(Character.valueOf('\u00E9'), "&eacute;");
        StringTools.entities.put(Character.valueOf('\u00EA'), "&ecirc;");
        StringTools.entities.put(Character.valueOf('\u00EB'), "&euml;");
        StringTools.entities.put(Character.valueOf('\u00EC'), "&igrave;");
        StringTools.entities.put(Character.valueOf('\u00ED'), "&iacute;");
        StringTools.entities.put(Character.valueOf('\u00EE'), "&icirc;");
        StringTools.entities.put(Character.valueOf('\u00EF'), "&iuml;");
        StringTools.entities.put(Character.valueOf('\u00F0'), "&eth;");
        StringTools.entities.put(Character.valueOf('\u00F1'), "&ntilde;");
        StringTools.entities.put(Character.valueOf('\u00F2'), "&ograve;");
        StringTools.entities.put(Character.valueOf('\u00F3'), "&oacute;");
        StringTools.entities.put(Character.valueOf('\u00F4'), "&ocirc;");
        StringTools.entities.put(Character.valueOf('\u00F5'), "&otilde;");
        StringTools.entities.put(Character.valueOf('\u00F6'), "&ouml;");
        StringTools.entities.put(Character.valueOf('\u00F7'), "&divide;");
        StringTools.entities.put(Character.valueOf('\u00F8'), "&oslash;");
        StringTools.entities.put(Character.valueOf('\u00F9'), "&ugrave;");
        StringTools.entities.put(Character.valueOf('\u00FA'), "&uacute;");
        StringTools.entities.put(Character.valueOf('\u00FB'), "&ucirc;");
        StringTools.entities.put(Character.valueOf('\u00FC'), "&uuml;");
        StringTools.entities.put(Character.valueOf('\u00FD'), "&yacute;");
        StringTools.entities.put(Character.valueOf('\u00FE'), "&thorn;");
        StringTools.entities.put(Character.valueOf('\u00FF'), "&yuml;");
    }

    /**
     * Compare two strings according to German collation rules.
     */
    public static int compare(String s1, String s2) {

        return StringTools.Deutsch.compare(s1, s2);
    }

    /**
     * Parse a double precision floating point number from a string.
     *
     * @throws NumberFormatException
     * @deprecated This method was provided for Java 1.0. Since Java 1.2 you
     * should use {@link java.lang.Double#parseDouble} instead.
     */
    public static double parseDouble(String s)
            throws NumberFormatException {

        int p = s.indexOf('.');
        if (p == -1) {
            return Long.parseLong(s);
        } else if (s.indexOf('.', p + 1) >= 0) {
            throw new NumberFormatException();
        }

        double d = Long.parseLong(s.substring(0, p));
        double factor = 1.0d;

        for (int i = p + 1; i < s.length(); i++) {
            int digit = s.charAt(i) - '0';
            if ((digit < 0) || (digit > 9)) {
                throw new NumberFormatException();
            }

            factor = factor / 10.0d;
            d = d + (factor * digit);
        }

        return d;
    }

    public static Locale parseLocale(String name) {

        if (name.length() == 2) {
            return new Locale(name);
        } else if ((name.length() == 5) && (name.charAt(2) == '_')) {
            // special case for en_UK which should really be en_GB
            if (name.equalsIgnoreCase("en_UK")) {
                return Locale.UK;
            } else {
                return new Locale(name.substring(0, 2), name.substring(3, 5));
            }
        } else if ((name.length() == 8) && (name.charAt(2) == '_')
                && (name.charAt(5) == '_')) {
            return new Locale(name.substring(0, 2), name.substring(3, 5), name
                    .substring(6, 8));
        }

        Set<Locale> localizations = new LinkedHashSet<Locale>();
        localizations.add(Locale.getDefault());
        localizations.add(Locale.GERMAN);
        localizations.add(Locale.ENGLISH);
        Locale[] locales = Locale.getAvailableLocales();
        for (Locale l : locales) {
            if (name.equalsIgnoreCase(l.getLanguage())) {
                return l;
            }
            for (Locale localization : localizations) {
                if (name.equalsIgnoreCase(l.getDisplayLanguage(localization))) {
                    return l;
                }
            }
            if (name.equalsIgnoreCase(l.getDisplayLanguage(l))) {
                return l;
            }
            for (Locale localization : localizations) {
                if (name.equalsIgnoreCase(l.getDisplayName(localization))) {
                    return l;
                }
            }
            if (name.equalsIgnoreCase(l.getDisplayName(l))) {
                return l;
            }
        }

        throw new IllegalArgumentException(
                "Could not determine locale for identifier \"" + name
                + "\"");
    }
    
    /**
     * Copies a string to the system clipboard.
     * 
     * @param writeMe 
     */
    public static void copyToClipboard(String writeMe) {
        Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable tText = new StringSelection(writeMe);
        clip.setContents(tText, null);
    }
}
