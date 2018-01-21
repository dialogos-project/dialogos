package com.clt.script.exp.patterns;

import java.util.Map;

import com.clt.script.exp.Match;
import com.clt.script.exp.Pattern;
import com.clt.script.exp.Type;
import com.clt.script.exp.Value;
import com.clt.script.exp.types.TypeVariable;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */
public class VarPattern implements Pattern {

    private String name;

    public VarPattern(String name) {

        this.name = name;
    }

    public String getVariableName() {

        return this.name;
    }

    public Match match(Value v) {

        Match m = new Match();
        if (!this.name.equals("_")) {
            m.put(this.name, v);
        }
        return m;
    }

    public Type getType(Map<String, Type> variableTypes) {

        if (variableTypes != null) {
            Type t = variableTypes.get(this.name);
            if (t != null) {
                return t;
            }
        }
        return new TypeVariable();
    }

    public Pattern.VarSet getFreeVars() {

        Pattern.VarSet s = new Pattern.VarSet();
        if (!this.name.equals("_")) {
            s.add(this.name);
        }
        return s;
    }

    @Override
    public String toString() {

        return this.name;
    }
}
