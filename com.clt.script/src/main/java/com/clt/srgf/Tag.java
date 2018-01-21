package com.clt.srgf;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.clt.script.exp.Value;

/**
 * A semantic tag in the right hand side of a {@link Rule}.
 *
 * @author Daniel Bobbert
 * @version 1.0
 */
public class Tag extends Expansion {

    private List<SemanticAssignment> assignments;

    private transient ParserState parserState = null;
    private transient Collection<String> visibleIDs = null;

    public Tag() {

        this.assignments = new ArrayList<SemanticAssignment>();
    }

    @Override
    public void check(Collection<String> visibleIDs,
            Collection<Exception> warnings) {

        this.visibleIDs = visibleIDs;

        for (SemanticAssignment assignment : this.assignments) {
            assignment.check(warnings);
        }

        this.visibleIDs = null;
    }

    @Override
    Iterator<StringBuilder> generate_(StringBuilder prefix,
            GenerationOptions options) {

        return Collections.singleton(prefix).iterator();
    }

    @Override
    Expansion optimize_(boolean removeTags) {

        if (removeTags) {
            Sequence s = new Sequence();
            s.setRepeat(0, 0);
            return s;
        } else {
            Tag t = new Tag();
            t.setRepeat(this.getRepeatMin(), this.getRepeatMax());
            for (SemanticAssignment assignment : this.assignments) {
                t.add(assignment.clone(t));
            }
            return t;
        }
    }

    public void add(SemanticAssignment a) {

        this.assignments.add(a);
        this.makeDirty();
    }

    ParserState getParserState() {

        return this.parserState;
    }

    Collection<String> getVisibleIDs() {

        return this.visibleIDs;
    }

    public Value getValue(String rulename) {

        return this.parserState.getBinding().get(rulename);
    }

    public Value evaluate(ParserState state) {

        this.parserState = state;

        Value currentValue = state.getValue();
        for (SemanticAssignment assignment : this.assignments) {
            currentValue = assignment.evaluate(currentValue);
            state.setValue(currentValue);
        }

        this.parserState = null;

        return currentValue;
    }

    @Override
    Item createInstance() {

        return new TagItem();
    }

    @Override
    Collection<TreeMatch> matchImpl(TreeMatch match, Grammar grammar,
            ParseOptions options) {

        match.setValue(this.evaluate(match));
        return Collections.singleton(match);
    }

    @Override
    boolean eq(Expansion e) {

        return e == this;
    }

    @Override
    boolean computePossibleTokens_(boolean recompute) {

        return this.addPossibleToken(Token.TAG);
    }

    @Override
    public void export(PrintWriter w, Grammar.Format format) {

        switch (format) {
            case SRGF:
                w.print("{ ");
                if (this.assignments.size() == 1) {
                    w.print(this.assignments.get(0).toString(true));
                } else {
                    for (Iterator<SemanticAssignment> it = this.assignments.iterator(); it
                            .hasNext();) {
                        w.print(it.next().toString());
                        if (it.hasNext()) {
                            w.print("; ");
                        }
                    }
                }
                w.print(" }");
                break;
            case GRXML:
                StringBuilder tags = new StringBuilder();
                for (Iterator<SemanticAssignment> it = this.assignments.iterator(); it
                        .hasNext();) {
                    tags.append(it.next().toString());
                    if (it.hasNext()) {
                        tags.append("; ");
                    }
                }
                ((com.clt.xml.XMLWriter) w).printElement("tag", tags.toString());
                break;
            case JSGF:
            case TEMIC:
                w.print("<NULL>");
                break;
            case LH:
            case VOCON:
            case VOCON_G:
                // !action bzw. !id
                break;
            case NGSL:
                break;
            default:
                throw new IllegalArgumentException("Unknown export format");
        }
    }

    @Override
    void write(PrintWriter w, Grammar.Format format) {

    }

    @Override
    WordGraph.Node[] createWordGraphImpl(WordGraph.Node predecessors[],
            boolean mergePrivateRules,
            Tokenizer tokenizer) {

        return predecessors;
    }

    @Override
    public String toString() {

        return "{...}";
    }

    private class TagItem
            extends Item {

        public TagItem() {

            super(Tag.this.getRepeatMin(), Tag.this.getRepeatMax());
        }

        private TagItem(TagItem item) {

            super(item);
        }

        @Override
        public Item copy() {

            return new TagItem(this);
        }

        @Override
        public List<Parse> shift_(Parse p) {

            if (p.getOptions().evaluateTags) {
                p.setValue(Tag.this.evaluate(p.getCurrentRule()));
            }
            this.setDone(true);
            return Item.itemList(p);
        }
    }
}
