package com.clt.script;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.swing.JOptionPane;

import com.clt.script.exp.EvaluationException;
import com.clt.script.exp.Value;
import com.clt.script.exp.values.BoolValue;
import com.clt.script.exp.values.IntValue;
import com.clt.script.exp.values.ListValue;
import com.clt.script.exp.values.RealValue;
import com.clt.script.exp.values.StringValue;
import com.clt.script.exp.values.StructValue;
import com.clt.script.exp.values.Undefined;
import com.clt.util.StringTools;

/**
 * This class provides built-in functions for all users of the script package.
 * Functions must be public static and all argument and return types must be
 * derived from {@link com.clt.script.exp.Value}. A function can take a variable
 * list of arguments if its last argument is of type Value[]
 * .<p>
 *
 * Implementation note: Make function types as strict as possible, i.e. don't
 * make the return type Value if you know that it will always be a StructValue.
 * The specified types are used for type checking within the script framework,
 * so making them as strict as possible helps find possible errors at compile
 * time. To overcome the problem that Undefined and other Value types are
 * compatible within the script language but not in Java, you can return null if
 * you want a funtion to return undefined although it was specified to return
 * e.g. a StructValue.
 *
 * @author Daniel Bobbert
 * @version 1.0
 */
public class BuiltinFunctions {

    /**
     * Return the length of string <code>s</code>
     */
    public static IntValue length(StringValue s) {

        return new IntValue(s.getString().length());
    }

    /**
     * Return the substring of <code>s</code> starting at <code>offset</code>
     */
    public static StringValue substring(StringValue s, IntValue offset) {

        return BuiltinFunctions.substring(s, offset, new IntValue(s.getString().length() - offset.getInt()));
    }

    /**
     * Return the substring of <code>s</code> starting at <code>offset</code>
     * with length <code>length</code>
     */
    public static StringValue substring(StringValue s, IntValue offset, IntValue length) {

        long start = offset.getInt();
        long end = start + length.getInt();
        return new StringValue(s.getString().substring((int) start, (int) end));
    }

    /**
     * Return a copy of <code>s</code> with all characters converted to
     * lowercase
     */
    public static StringValue toLowerCase(StringValue s) {

        return new StringValue(s.getString().toLowerCase());
    }

    /**
     * Return a copy of <code>s</code> with all characters converted to
     * uppercase
     */
    public static StringValue toUpperCase(StringValue s) {

        return new StringValue(s.getString().toUpperCase());
    }

    /**
     * Return the nth character of <code>s</code> as a string
     */
    public static StringValue charAt(StringValue s, IntValue index) {

        return new StringValue(String.valueOf(s.getString().charAt(
                (int) index.getInt())));
    }

    /**
     * Test whether <code>s</code> starts with <code>prefix</code>
     */
    public static BoolValue startsWith(StringValue s, StringValue prefix) {

        return new BoolValue(s.getString().startsWith(prefix.getString()));
    }

    /**
     * Test whether <code>s</code> ends with <code>suffix</code>
     */
    public static BoolValue endsWith(StringValue s, StringValue suffix) {

        return new BoolValue(s.getString().endsWith(suffix.getString()));
    }

    /**
     * Test whether <code>s</code> contains <code>infix</code>
     */
    public static BoolValue contains(StringValue s, StringValue infix) {

        return new BoolValue(s.getString().indexOf(infix.getString()) >= 0);
    }

    /**
     * Return the position of <code>infix</code> within <code>s</code> If
     * <code>s</code> does not contain <code>infix</code>, return
     * <code>-1</code>.
     */
    public static IntValue indexOf(StringValue s, StringValue infix) {

        return new IntValue(s.getString().indexOf(infix.getString()));
    }

    /**
     * Return a copy of <code>s</code> with all occurences of <code>a</code>
     * replaced by <code>b</code>
     */
    public static StringValue replace(StringValue s, StringValue a, StringValue b) {

        String s_ = s.getString();
        String a_ = a.getString();
        String b_ = b.getString();

        StringBuilder buf = new StringBuilder(s_.length());

        int i = 0;
        while (i < s_.length()) {
            if (s_.startsWith(a_, i)) {
                buf.append(b_);
                i += a_.length();
            } else {
                buf.append(s_.charAt(i));
                i++;
            }
        }

        return new StringValue(buf.toString());
    }

    /**
     * Test whether <code>s</code> can be converted to an <code>int</code>.
     */
    public static BoolValue isInt(StringValue s) {

        try {
            Long.parseLong(s.getString());
            return BoolValue.TRUE;
        } catch (NumberFormatException exn) {
            return BoolValue.FALSE;
        }
    }

    /**
     * Test whether <code>s</code> can be converted to a <code>real</code>.
     */
    public static BoolValue isReal(StringValue s) {

        try {
            Double.parseDouble(s.getString());
            return BoolValue.TRUE;
        } catch (NumberFormatException exn) {
            return BoolValue.FALSE;
        }
    }

    /**
     * Convert <code>s</code> to an <code>int</code>. Returns
     * <code>undefined</code> if conversion fails.
     */
    public static IntValue parseInt(StringValue s) {

        try {
            return new IntValue(Long.parseLong(s.getString()));
        } catch (NumberFormatException exn) {
            throw new EvaluationException(s + " cannot be converted to an int");
        }
    }

    /**
     * Convert <code>s</code> to a <code>real</code>. Returns
     * <code>undefined</code> if conversion fails.
     */
    public static RealValue parseReal(StringValue s) {

        try {
            return new RealValue(Double.parseDouble(s.getString()));
        } catch (NumberFormatException exn) {
            throw new EvaluationException(s + " cannot be converted to a real");
        }
    }

    /**
     * Convert to string
     */
    public static StringValue str(Value v) {

        if (v instanceof StringValue) {
            return (StringValue) v;
        } else if (v instanceof IntValue) {
            return new StringValue(String.valueOf(((IntValue) v).getInt()));
        } else if (v instanceof RealValue) {
            return new StringValue(String.valueOf(((RealValue) v).getReal()));
        } else if (v instanceof BoolValue) {
            return new StringValue(String.valueOf(((BoolValue) v).getBool()));
        } else {
            return new StringValue(v.toString());
        }
    }

    /**
     * Return the absolute value of <code>i</code>
     */
    public static IntValue abs(IntValue i) {

        return new IntValue(Math.abs(i.getInt()));
    }

    /**
     * Returns Euler's number <i>e</i> raised to the power of <code>r</code>.
     */
    public static RealValue exp(RealValue r) {

        return new RealValue(Math.exp(r.getReal()));
    }

    /**
     * Returns the natural logarithm of <code>r</code>.
     */
    public static RealValue log(RealValue r) {

        return new RealValue(Math.log(r.getReal()));
    }

    /**
     * Return a random <code>int</code> between <code>min</code> and
     * <code>max</code> including both.
     */
    public static IntValue random(IntValue min, IntValue max) {

        return new IntValue(min.getInt()
                + Math.round(Math.random() * (max.getInt() - min.getInt())));
    }

    /**
     * Round <code>r</code> to the nearest <code>int</code>
     */
    public static IntValue round(RealValue r) {

        return new IntValue(Math.round(r.getReal()));
    }

    /**
     * Return a new <code>list</code> consisting of <code>v</code> prepended to
     * <code>l</code>
     */
    public static ListValue cons(Value v, ListValue l) {

        Value[] l2 = new Value[l.size() + 1];
        l2[0] = v;
        for (int i = 0; i < l.size(); i++) {
            l2[i + 1] = l.get(i);
        }
        return new ListValue(l2);
    }

    /**
     * Return a new <code>list</code> consisting of the concatenation of
     * <code>l1</code> and <code>l2</code>
     */
    public static ListValue append(ListValue l1, ListValue l2) {

        Value[] l = new Value[l1.size() + l2.size()];
        for (int i = 0; i < l1.size(); i++) {
            l[i] = l1.get(i);
        }
        for (int i = 0; i < l2.size(); i++) {
            l[i + l1.size()] = l2.get(i);
        }
        return new ListValue(l);
    }

    /**
     * Return a new <code>list</code> consisting of the concatenation of all
     * lists in <code>lists</code>
     */
    public static ListValue concat(ListValue lists) {

        int size = 0;
        for (int i = 0; i < lists.size(); i++) {
            Value v = lists.get(i);
            if (!(v instanceof ListValue)) {
                throw new EvaluationException(
                        "Argument to concat() is not a list of lists");
            } else {
                size += ((ListValue) v).size();
            }
        }
        Value[] l = new Value[size];
        int offset = 0;
        for (int i = 0; i < lists.size(); i++) {
            ListValue v = (ListValue) lists.get(i);
            for (int j = 0; j < v.size(); j++) {
                l[j + offset] = v.get(j);
            }
            offset += v.size();
        }
        return new ListValue(l);
    }

    /**
     * Return a new <code>list</code> consisting of all elements of
     * <code>list</code> minus the elements of <code>remove</code>
     */
    public static ListValue remove(ListValue list, ListValue remove) {

        List<Value> v = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            if (!BuiltinFunctions.member(remove, list.get(i)).getBool()) {
                v.add(list.get(i));
            }
        }
        return new ListValue(v.toArray(new Value[v.size()]));
    }

    /**
     * Return a new <code>list</code> consisting of all elements of
     * <code>list</code> minus the nth element
     */
    public static ListValue remove(ListValue list, IntValue n) {

        List<Value> v = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            if (i != n.getInt()) {
                v.add(list.get(i));
            }
        }
        return new ListValue(v.toArray(new Value[v.size()]));
    }

    /**
     * Returns the length of list <code>l</code>
     */
    public static IntValue length(ListValue l) {

        return new IntValue(l.size());
    }

    /**
     * Returns whether the list <code>l</code> is empty
     */
    public static BoolValue empty(ListValue l) {

        return new BoolValue(l.size() == 0);
    }

    /**
     * Returns the first (head) element of <code>l</code>
     */
    public static Value head(ListValue l) {

        if (l.size() == 0) {
            throw new EvaluationException("head() called with empty list");
        }
        return l.get(0);
    }

    /**
     * Returns the remaining (tail) elements of <code>l</code>
     */
    public static ListValue tail(ListValue l) {

        if (l.size() == 0) {
            throw new EvaluationException("tail() called with empty list");
        }
        Value[] ll = new Value[l.size() - 1];
        for (int i = 1; i < l.size(); i++) {
            ll[i - 1] = l.get(i);
        }
        return new ListValue(ll);
    }

    /**
     * Returns the nth element of <code>l</code>
     */
    public static Value get(ListValue l, IntValue index) {

        try {
            return l.get((int) index.getInt());
        } catch (IndexOutOfBoundsException exn) {
            throw new EvaluationException("get(" + index.getInt()
                    + ") called with list of length "
                    + l.size());
        }
    }

    /**
     * Equivalent to <code>contains(list, v)</code>
     */
    public static BoolValue member(ListValue list, Value v) {

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals(v)) {
                return BoolValue.TRUE;
            }
        }
        return BoolValue.FALSE;
    }

    /**
     * Returns whether <code>l</code> contains <code>v</code>
     */
    public static BoolValue contains(ListValue list, Value v) {

        return BuiltinFunctions.member(list, v);
    }

    /**
     * Returns the posiition of <code>v</code> in <code>list</code> or
     * <code>-1</code> if <code>list</code> does not contain <code>v</code>
     */
    public static IntValue indexOf(ListValue list, Value v) {

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals(v)) {
                return new IntValue(i);
            }
        }
        return new IntValue(-1);
    }

    /**
     * Return the sublist of <code>list</code> starting at <code>offset</code>
     */
    public static ListValue sublist(ListValue list, IntValue offset) {

        return list.subList((int) offset.getInt(), list.size());
    }

    /**
     * Return the sublist of <code>list</code> starting at <code>offset</code>
     * with length <code>length</code>
     */
    public static ListValue sublist(ListValue list, IntValue offset,
            IntValue length) {

        return list.subList((int) offset.getInt(), (int) (offset.getInt() + length
                .getInt()));
    }

    /**
     * Return a reversed copy of <code>list</code>
     */
    public static ListValue reverse(ListValue list) {

        Value[] elems = new Value[list.size()];
        for (int i = 0; i < elems.length; i++) {
            elems[elems.length - i - 1] = list.get(i);
        }
        return new ListValue(elems);
    }

    /**
     * Sort a list according to the natural order of its element. All elements
     * must have the same type.
     */
    public static ListValue sort(ListValue list) {

        Value[] elements = new Value[list.size()];
        for (int i = 0; i < list.size(); i++) {
            elements[i] = list.get(i);
        }

        Arrays.sort(elements, (v1, v2) -> {

            if (v1.getClass() != v2.getClass()) {
                throw new EvaluationException("Can't compare values of type "
                        + v1.getType()
                        + " and " + v2.getType());
            } else if (!(v1 instanceof Comparable)) {
                throw new EvaluationException("Can't compare values of type "
                        + v1.getType());
            } else {
                return ((Comparable<Value>) v1).compareTo(v2);
            }
        });
        return new ListValue(elements);
    }

    public static ListValue enumerate(IntValue startValue, IntValue endValue,
            IntValue intervalValue) {

        long start = startValue.getInt();
        long end = endValue.getInt();
        long interval = Math.abs(intervalValue.getInt());

        Value[] values = new Value[(int) (Math.abs(end - start) / interval) + 1];

        if (start <= end) {
            for (int i = 0; start <= end; start += interval) {
                values[i++] = new IntValue(start);
            }
        } else {
            for (int i = 0; start >= end; start -= interval) {
                values[i++] = new IntValue(start);
            }
        }
        return new ListValue(values);
    }

    /**
     * Returns whether <code>str</code> contains a label named <code>key</code>
     */
    public static BoolValue contains(StructValue str, StringValue key) {

        if (str.containsLabel(key.getString())) {
            return BoolValue.TRUE;
        } else {
            return BoolValue.FALSE;
        }
    }

    /**
     * Returns the element of <code>str</code> assigned to label
     * <code>key</code>
     */
    public static Value get(StructValue str, StringValue key) {

        if (str.containsLabel(key.getString())) {
            return str.getValue(key.getString());
        } else {
            return new Undefined();
        }
    }

    /**
     * The counterpart to get(struct, string)
     */
    public static StructValue put(StructValue str, StringValue key, Value value) {

        return BuiltinFunctions.merge(str, new StructValue(new String[]{key
            .getString()}, new Value[]{value}));
    }

    /**
     * Returns a list of all keys in <code>str</code>
     */
    public static ListValue keys(StructValue str) {

        Value[] keys = new Value[str.size()];
        int i = 0;
        for (Iterator<String> it = str.labels(); it.hasNext(); i++) {
            keys[i] = new StringValue(it.next());
        }
        return new ListValue(keys);
    }

    /**
     * Returns a list of all values in <code>str</code>
     */
    public static ListValue values(StructValue str) {

        Value[] values = new Value[str.size()];
        int i = 0;
        for (Iterator<String> it = str.labels(); it.hasNext(); i++) {
            values[i] = str.getValue(it.next());
        }
        return new ListValue(values);
    }

    /**
     * Return the merger of <code>str1</code> and <code>str2</code>
     * <p>
     * The source structures will not be modified. Instead a new StructValue
     * will be created and returned. If <code>str1</code> and <code>str2</code>
     * both contain the same label, the result depends on the type of its
     * subvalues. If the label is mapped to a substructure in both
     * <code>str1</code> and <code>str2</code> then these substructures are
     * merged recursively. Otherwise the value from <code>str2</code> is taken.
     * </p>
     * Examples:<br>
     * <code>merge({ x=3, y=4 }, { x=4, z=5 })</code> =
     * <code>{ x=4, y=4, z=5 }</code><br>
     * <code>merge({ x={x=3}, y={a=4} }, { x={y=4}, y=5 })</code> =
     * <code>{ x={x=3, y=4}, y=5 }</code>
     */
    public static StructValue merge(StructValue str1, StructValue str2) {

        return StructValue.merge(str1, str2);
    }

    /**
     * Like merge(), but only overwrite values in <code>str1</code> if the value
     * in <code>str2</code> is not undefined.
     */
    public static StructValue mergeDefined(StructValue str1, StructValue str2) {

        return StructValue.mergeDefined(str1, str2);
    }

    /**
     * Return the current time as a structure <code>{ h:int, m:int }</code>
     */
    public static StructValue currentTime() {

        Calendar c = Calendar.getInstance();

        return new StructValue(new String[]{"h", "m"}, new Value[]{
            new IntValue(c.get(Calendar.HOUR_OF_DAY)),
            new IntValue(c.get(Calendar.MINUTE))});
    }

    /**
     * Return the difference, measured in milliseconds, between the current time
     * and midnight, January 1, 1970 UTC.
     */
    public static IntValue currentTimeMillis() {

        return new IntValue(System.currentTimeMillis());
    }

    /**
     * Return the current time as a structure
     * <code>{ day:int, month:int, year:int }</code>
     */
    public static StructValue currentDate() {

        Calendar c = Calendar.getInstance();

        return new StructValue(new String[]{"day", "month", "year"},
                new Value[]{
                    new IntValue(c.get(Calendar.DAY_OF_MONTH)),
                    new IntValue(c.get(Calendar.MONTH) + 1),
                    new IntValue(c.get(Calendar.YEAR))});
    }

    /**
     * Calculate the sum of <code>date</code> and <code>addition</code>
     * <p>
     * <code>date</code> must be a full date structure
     * <code>{ day:int, month:int, year:int }</code>. <code>addition</code> may
     * contain only some of the date fields. Years are added first, then month
     * and then days.
     * </p>
     * Examples:<br>
     * <code>{ day=10, month=6, year=2003 } + { day=25 } = { day = 5, month=7, year = 2003 }</code>
     * <br>
     * <code>{ day=10, month=6, year=2003 } + { day=25, month=1 } = { day = 5, month=8, year = 2003 }</code>
     * <br>
     */
    public static StructValue addDate(StructValue date, StructValue addition) {

        Calendar c = Calendar.getInstance();

        c
                .set(Calendar.DAY_OF_MONTH, (int) ((IntValue) date.getValue("day"))
                        .getInt());
        c.set(Calendar.MONTH, (int) ((IntValue) date.getValue("month")).getInt() - 1);
        c.set(Calendar.YEAR, (int) ((IntValue) date.getValue("year")).getInt());

        if (addition.containsLabel("year")) {
            c.add(Calendar.YEAR, (int) ((IntValue) addition.getValue("year")).getInt());
        }
        if (addition.containsLabel("month")) {
            c.add(Calendar.MONTH, (int) ((IntValue) addition.getValue("month"))
                    .getInt());
        }
        if (addition.containsLabel("day")) {
            c.add(Calendar.DAY_OF_MONTH, (int) ((IntValue) addition.getValue("day"))
                    .getInt());
        }

        return new StructValue(new String[]{"day", "month", "year"},
                new Value[]{
                    new IntValue(c.get(Calendar.DAY_OF_MONTH)),
                    new IntValue(c.get(Calendar.MONTH) + 1),
                    new IntValue(c.get(Calendar.YEAR))});

    }

    /**
     * Return the current weekday and week of the year as a structure
     * <code>{ day:int, week:int }</code> Weekdays start at 0: 0=Sunday,
     * 1=Monday, ...
     */
    public static StructValue currentWeekDay() {

        Calendar c = Calendar.getInstance();

        return new StructValue(new String[]{"day", "week"}, new Value[]{
            new IntValue(c.get(Calendar.DAY_OF_WEEK) - 1),
            new IntValue(c.get(Calendar.WEEK_OF_YEAR))});
    }

    /**
     * Print a string representation of <code>v</code> to stdout.
     */
    public static void print(Value v) {

        if (v instanceof StringValue) {
            System.out.println(((StringValue) v).getString());
        } else {
            System.out.println(v.toString());
        }
    }

    public static void printf(StringValue format, Value[] args) {

        int argp = 0;
        StringBuilder b = new StringBuilder();

        String f = format.getString();
        int n = 0;
        while (n < f.length()) {
            char c = f.charAt(n++);
            if (c == '%') {
                if (n >= f.length()) {
                    throw new EvaluationException("Illegal format string.");
                }
                if (f.charAt(n) == '%') {
                    b.append('%');
                    n++;
                } else {
                    if (argp >= args.length) {
                        throw new EvaluationException("Too few arguments for format string");
                    }
                    if (args[argp] instanceof StringValue) {
                        b.append(((StringValue) args[argp]).getString());
                    } else {
                        b.append(args[argp].toString());
                    }
                    argp++;
                    n++;
                }
            } else {
                b.append(c);
            }
        }

        if (argp != args.length) {
            throw new EvaluationException("Too many arguments for format string");
        }
        System.out.println(b.toString());
    }

    public static void showDialog(Value[] message) {

        String[] msg = new String[message.length];
        for (int i = 0; i < msg.length; i++) {
            if (message[i] instanceof StringValue) {
                msg[i] = ((StringValue) message[i]).getString();
            } else {
                msg[i] = message[i].toString();
            }
        }

        JOptionPane.showMessageDialog(null, msg);
    }

    public static void error(StringValue message) {

        throw new EvaluationException(message.getString());
    }

    public static StructValue loadProperties(StringValue directory,
            StringValue filename) {

        File f = new File(directory.getString(), filename.getString());
        if (!f.isFile()) {
            return null;
        } else {
            try {
                InputStream in = new BufferedInputStream(new FileInputStream(f));
                Properties p = new Properties();
                p.load(in);
                in.close();

                String[] labels = new String[p.size()];
                Value[] values = new Value[labels.length];
                int n = 0;
                for (Enumeration<?> it = p.propertyNames(); it.hasMoreElements(); n++) {
                    labels[n] = String.valueOf(it.nextElement());
                    values[n] = new StringValue(p.getProperty(labels[n]));
                }
                return new StructValue(labels, values);
            } catch (Exception exn) {
                return null;
            }
        }
    }

    // TODO : Added by Daniel Beck.
    public static StringValue loadTemplate(StructValue templates,
            StringValue templateName,
            StructValue optional_param) {

        StringValue stringValue
                = (StringValue) templates.getAttribute(templateName.getString());
        String string = stringValue.getString();

        Collection<String> attributes = optional_param.getAttributes();
        for (String attr : attributes) {
            String subst
                    = ((StringValue) optional_param.getAttribute(attr)).getString();

            string = string.replace(attr, subst);
        }
        return new StringValue(string);

    }

    public static ListValue stripSpecialCharacters(ListValue list) {

        Value[] stripped = new Value[list.size()];
        int n = 0;
        for (Value element : list) {
            String s = ((StringValue) element).getString();
            StringBuilder b = new StringBuilder(s.length());
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (Character.isLetterOrDigit(c)) {
                    b.append(c);
                }
            }
            stripped[n++] = new StringValue(b.toString());
        }
        return new ListValue(stripped);
    }

    public static ListValue split(StringValue string, StringValue delimiter) {

        String[] s = StringTools.split(string.getString(), delimiter.getString());
        StringValue[] elements = new StringValue[s.length];
        for (int i = 0; i < s.length; i++) {
            elements[i] = new StringValue(s[i]);
        }
        return new ListValue(elements);
    }

    public static StringValue trim(StringValue v) {

        return new StringValue(v.getString().trim());
    }
}
