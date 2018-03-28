package com.clt.script.exp;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

import com.clt.script.DefaultEnvironment;
import com.clt.script.Environment;
import com.clt.script.debug.Debugger;
import com.clt.script.debug.DefaultDebugger;
import com.clt.script.parser.Parser;

/**
 * Base class for all arithmetic and boolean expressions.
 *
 * Each Expression has a {@link Type} and can be evaluated to a {@link Value}.
 * Subclasses must implement {@link #eval(Debugger)} and {@link #getType()}.
 *
 * @author Daniel Bobbert
 * @version 2.0
 */
public abstract class Expression {

    protected abstract Value eval(Debugger dbg);

    public abstract Type getType();

    public final Value evaluate() {
        return this.evaluate(new DefaultDebugger());
    }

    public final Value evaluate(Debugger dbg) {
        dbg.preEvaluate(this);
        return this.eval(dbg);
    }

    public int getPriority() {
        return 0;
    }

    public abstract void write(PrintWriter w);

    public abstract Expression copy(Map<?, ?> mapping);

    public final void write(PrintWriter w, boolean parens) {
        if (parens) {
            w.write('(');
        }
        this.write(w);
        if (parens) {
            w.write(')');
        }
    }

    @Override
    public final String toString() {
        StringWriter w = new StringWriter();
        PrintWriter pw = new PrintWriter(w);
        this.write(pw);
        pw.flush();
        return w.toString();
    }

    public static String[] getBuiltInFunctions(boolean html) {
        List<String> functions = new ArrayList<>();
        for (MethodDescriptor md : new DefaultEnvironment().getMethods()) {
            functions.add(md.getDescription(html));
        }
        return functions.toArray(new String[] {});
    }

    public static Expression parseExpression(String exp)
            throws Exception {
        return parseExpression(exp, new DefaultEnvironment());
    }

    public static Expression parseExpression(String exp, Environment env)
            throws Exception {
        return Parser.parseExpression(exp, env);
    }

    public static Pattern parsePattern(String exp)
            throws Exception {
        return Parser.parsePattern(exp);
    }

    public static String replace(String s, String src, String dst) {
        if (!dst.equals(src)) {
            int pos = 0;
            while ((pos = s.indexOf(src, pos)) >= 0) {
                s = s.substring(0, pos)
                        + dst
                        + s.substring(pos + src.length());
                pos += dst.length();
            }
        }
        return s;
    }
}
