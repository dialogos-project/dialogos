package com.clt.srgf;

import java.io.PrintWriter;
import java.util.Map;

import com.clt.script.debug.Debugger;
import com.clt.script.exp.EvaluationException;
import com.clt.script.exp.Expression;
import com.clt.script.exp.Type;
import com.clt.script.exp.TypeException;
import com.clt.script.exp.Value;
import com.clt.script.exp.types.TypeVariable;
import com.clt.script.exp.values.IntValue;
import com.clt.script.exp.values.StringValue;
import com.clt.script.exp.values.StructValue;

/**
 * A variable reference in a semantic {@link Tag}.
 *
 * @author Daniel Bobbert
 * @version 1.0
 */
public class SemanticID extends Expression {

    private String name;
    private Tag tag;

    public SemanticID(String name, Tag tag) {

        this.name = SemanticID.strip(name);
        this.tag = tag;

        if (this.name.startsWith("$.")) {
            this.name = this.name.substring(2);
        }
    }

    @Override
    public SemanticID copy(Map<?, ?> mapping) {

        if (mapping.containsKey(this.tag)) {
            return new SemanticID(this.name, (Tag) mapping.get(this.tag));
        } else {
            return new SemanticID(this.name, this.tag);
        }
    }

    @Override
    protected Value eval(Debugger dbg) {
        try {
            Value value = this.tag.getParserState().getValue();
            if (this.name.equals("$")) {
                return value;
            } else if (!this.name.startsWith("$") || this.name.startsWith("$.")) {
                // x, x.y, $.x, $.x.y
                String s = this.name;
                if (s.startsWith("$.")) {
                    s = s.substring(2);
                }

                if (s.equals("_text_")) {
                    return new StringValue(this.tag.getParserState().getText());
                } else if (s.equals("_start_")) {
                    return new IntValue(this.tag.getParserState().getStart());
                } else if (s.equals("_end_")) {
                    return new IntValue(this.tag.getParserState().getEnd());
                } else if (value instanceof StructValue) {
                    try {
                        return SemanticID.getValue((StructValue) value, s);
                    } catch (Exception exn) {
                        throw new EvaluationException("Current tag value does not have a feature named " + this.name);
                    }
                } else {
//                    System.err.println("before exc: " + this);
//                    System.err.println("-> " + value);
//                    System.err.println("-> s=" + s);
//                    System.err.println(value.getClass());
                    throw new EvaluationException("Current tag value is not a structure");
                }
            } else {
                // $x, $x.y
                int dot = this.name.indexOf('.');
                String rulename = dot < 0 ? this.name.substring(1) : this.name.substring(1, dot);
                String element = dot < 0 ? null : this.name.substring(dot + 1);

                // find last occurence of rule
                value = this.tag.getValue(rulename);
                if (value == null) {
                    throw new EvaluationException("Unknown variable '" + rulename + "'");
                } else if (element == null) {
                    return value;
                }
                if (element.equals("_text_")) {
                    return value.getAttribute("text");
                } else if (element.equals("_start_")) {
                    return value.getAttribute("start");
                } else if (element.equals("_end_")) {
                    return value.getAttribute("end");
                } else if (!(value instanceof StructValue)) {
                    throw new EvaluationException("Value of last instance of rule '"
                            + rulename
                            + "' is not a structure");
                } else {
                    try {
                        return ((StructValue) value).getValue(element);
                    } catch (Exception exn) {
                        throw new EvaluationException("Value of last instance of rule '"
                                + rulename
                                + "' does not contain a feature named "
                                + element);
                    }
                }
            }
        } catch (RuntimeException exn) {
            throw exn;
        } catch (Exception exn) {
            throw new EvaluationException("Unknown variable '" + this.name + "'");
        }
    }

    private static Value getValue(StructValue v, String label) {

        int pos = label.indexOf('.');
        if (pos < 0) {
            return v.getValue(label);
        } else {
            String l = label.substring(0, pos);
            label = label.substring(pos + 1);
            Value v2 = v.getValue(l);
            if (v2 instanceof StructValue) {
                return SemanticID.getValue((StructValue) v2, label);
            } else {
                throw new EvaluationException();
            }
        }
    }

    @Override
    public Type getType() {

        if (!this.name.equals("$") && (this.tag.getVisibleIDs() != null)) {
            String id = this.name;
            if (id.startsWith("$") && !id.startsWith("$.")) {
                int pos = id.indexOf('.');
                if (pos >= 0) {
                    id = id.substring(0, pos);
                }
                if (!this.tag.getVisibleIDs().contains(id)) {
                    throw new TypeException("Access to unknown rule " + id);
                }
            }
        }
        return new TypeVariable();
    }

    public static String strip(String s) {

        StringBuilder b = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (!Character.isWhitespace(c)) {
                b.append(c);
            }
        }
        return b.toString();
    }

    @Override
    public int getPriority() {

        return Integer.MAX_VALUE;
    }

    @Override
    public void write(PrintWriter w) {

        w.print(this.name);
    }
}
