package com.clt.srgf;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.clt.script.exp.Expression;
import com.clt.script.exp.Value;
import com.clt.script.exp.values.StructValue;

/**
 * An assignment in a semantic {@link Tag}.
 *
 * @author Daniel Bobbert
 * @version 1.0
 */
public class SemanticAssignment {

    private String name;
    private Expression exp;
    private Tag tag;

    public SemanticAssignment(String name, Expression value, Tag currentTag) {

        this.name = SemanticID.strip(name);
        this.exp = value;
        this.tag = currentTag;
    }

    public Value evaluate(Value currentValue) {

        Value value = this.exp.evaluate();

        if (this.name.equals("$")) {
            return value;
        } else {
            String s = this.name;
            if (s.startsWith("$.")) {
                s = s.substring(2);
            }

            return this.merge(currentValue, s, value);

            /*
       * if (!(currentValue instanceof StructValue)) currentValue = new
       * StructValue(new String[] { s }, new Value[] { value }); else
       * currentValue = StructValue.merge((StructValue) currentValue, new
       * StructValue(new String[] {s}, new Value[] {value})); return
       * currentValue;
             */
        }
    }

    private StructValue merge(Value v, String name, Value value) {

        StructValue newValue;
        int pos = name.indexOf('.');
        if (pos < 0) {
            newValue = new StructValue(new String[]{name},
                    new Value[]{this.copySlotConfidence(value)});
        } else {
            String label = name.substring(0, pos);
            name = name.substring(pos + 1);
            newValue
                    = new StructValue(new String[]{label},
                    new Value[]{this.copySlotConfidence(this.merge(null, name,
                                value))});
        }

        if (v instanceof StructValue) {
            return StructValue.merge((StructValue) v, newValue);
        } else {
            return newValue;
        }
    }

    private Value copySlotConfidence(Value v) {

        if (this.tag.getParserState().getCurrentNode() != null) {
            Rulename.copyAttributes("SlotConfidence", this.tag.getParserState()
                    .getCurrentNode(), v);
        }
        return v;
    }

    SemanticAssignment clone(Tag context) {

        Map<Tag, Tag> mapping = new HashMap<Tag, Tag>();
        mapping.put(this.tag, context);
        return new SemanticAssignment(this.name, this.exp.copy(mapping), context);
    }

    public void check(Collection<Exception> warnings) {

        try {
            this.exp.getType();
        } catch (Exception exn) {
            warnings.add(exn);
        }
        if (!this.name.equals("$")) {
            this.tag.getVisibleIDs().add(this.name);
        }
    }

    @Override
    public String toString() {

        return this.toString(false);
    }

    public String toString(boolean single) {

        if (this.name.equals("$") && single) {
            return this.exp.toString();
        } else {
            return this.name + " = " + this.exp.toString();
        }
    }
}
