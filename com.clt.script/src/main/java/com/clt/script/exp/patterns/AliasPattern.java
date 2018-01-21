package com.clt.script.exp.patterns;

import java.util.Map;

import com.clt.script.exp.Match;
import com.clt.script.exp.Pattern;
import com.clt.script.exp.Type;
import com.clt.script.exp.Value;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */
public class AliasPattern implements Pattern {

    private String name;
    private Pattern pattern;

    public AliasPattern(Pattern pattern, String name) {

        this.name = name;
        this.pattern = pattern;
    }

    public Match match(Value v) {

        Match m = this.pattern.match(v);
        if (m == null) {
            return null;
        } else {
            m.put(this.name, v);
            return m;
        }
    }

    public Type getType(Map<String, Type> variableTypes) {

        return this.pattern.getType(variableTypes);
    }

    public Pattern.VarSet getFreeVars() {

        Pattern.VarSet freeVars = this.pattern.getFreeVars();
        freeVars.add(this.name);
        return freeVars;
    }

    @Override
    public String toString() {

        return (this.name + " as " + this.pattern);
    }
}
