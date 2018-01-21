package com.clt.script.exp.patterns;

import java.util.Map;

import com.clt.script.exp.Match;
import com.clt.script.exp.Pattern;
import com.clt.script.exp.Type;
import com.clt.script.exp.TypeException;
import com.clt.script.exp.Value;
import com.clt.script.exp.types.ListType;
import com.clt.script.exp.types.TypeVariable;
import com.clt.script.exp.values.ListValue;

/**
 * @author Daniel Bobbert
 * @version 2.0
 */
public class ConsPattern implements Pattern {

    private Pattern hd;
    private Pattern tl;

    public ConsPattern(Pattern hd, Pattern tl) {

        this.hd = hd;
        this.tl = tl;
    }

    public Pattern.VarSet getFreeVars() {

        Pattern.VarSet s = this.hd.getFreeVars();
        s.addAll(this.tl.getFreeVars());
        return s;
    }

    public Match match(Value v) {

        if (!(v instanceof ListValue)) {
            return null;
        } else if (((ListValue) v).size() == 0) {
            return null;
        } else {
            ListValue l = (ListValue) v;
            Match match = new Match();
            Match m = this.hd.match(l.get(0));
            if (m == null) {
                return null;
            } else {
                match.merge(m);
            }
            m = this.tl.match(l.subList(1, l.size()));
            if (m == null) {
                return null;
            } else {
                match.merge(m);
            }
            return match;
        }
    }

    public Type getType(Map<String, Type> variableTypes) {

        Type hdType = this.hd.getType(variableTypes).resolve();
        Type tlType = this.tl.getType(variableTypes).resolve();
        if (tlType instanceof ListType) {
            try {
                return new ListType(Type.unify(hdType, ((ListType) tlType)
                        .getElementType()));
            } catch (Exception exn) {
                return new ListType();
            }
        } else if (tlType instanceof TypeVariable) {
            return new ListType(this.hd.getType(variableTypes));
        } else {
            throw new TypeException(
                    "Tail of cons pattern must be a variable or a list pattern");
        }
    }

    @Override
    public String toString() {

        return this.hd + "::" + this.tl;
    }
}
