package com.clt.script.exp.patterns;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;

import com.clt.script.exp.Match;
import com.clt.script.exp.Pattern;
import com.clt.script.exp.Type;
import com.clt.script.exp.Value;
import com.clt.script.exp.values.StringValue;
import com.clt.script.exp.values.Undefined;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */
public class RegExp implements Pattern {

    private Pattern[] vars;
    private java.util.regex.Pattern regexp;
    private String pattern;

    public RegExp(String re, Pattern[] vars)  throws PatternSyntaxException {

        this.pattern = re;
        this.regexp = java.util.regex.Pattern.compile(re);
        if (vars == null) {
            this.vars = null;
        } else {
            this.vars = new Pattern[vars.length];
            for (int i = 0; i < vars.length; i++) {
                this.vars[i] = vars[i];
            }
        }
    }

    /**
     * Return a list of free variable names in this pattern.
     */
    public Pattern.VarSet getFreeVars() {

        Pattern.VarSet s = new Pattern.VarSet();
        if (this.vars != null) {
            for (int i = 0; i < this.vars.length; i++) {
                s.addAll(this.vars[i].getFreeVars());
            }
        }
        return s;
    }

    public Match match(Value v) {

        if (v instanceof StringValue) {
            // synchronize on regexp to make sure that no other thread
            // intercepts match() and getParen()
            synchronized (this.regexp) {
                Matcher matcher = this.regexp.matcher(((StringValue) v).getString());
                if (matcher.matches()) {
                    Match m = new Match();
                    if (this.vars != null) {
                        // parens count from 1 to getParenCount(). Parenthesis 0
                        // is the whole expression
                        for (int i = 0; (i < matcher.groupCount())
                                && (i < this.vars.length); i++) {
                            Match m2;
                            String paren = matcher.group(i + 1);
                            if (paren == null) {
                                m2 = this.vars[i].match(new Undefined());
                            } else {
                                m2 = this.vars[i].match(new StringValue(paren));
                            }
                            if (m2 == null) {
                                return null;
                            } else {
                                m.merge(m2);
                            }
                        }
                    }
                    return m;
                } else {
                    return null;
                }
            }
        } else {
            return null;
        }
    }

    public Type getType(Map<String, Type> variableTypes) {

        return Type.String;
    }

    @Override
    public String toString() {

        StringBuilder b = new StringBuilder();
        b.append("/");

        b.append(this.pattern);

        b.append("/");

        if (this.vars != null) {
            b.append(" = (");
            for (int i = 0; i < this.vars.length; i++) {
                if (i != 0) {
                    b.append(", ");
                }
                b.append(this.vars[i]);
            }
            b.append(")");
        }

        return b.toString();
    }
}
