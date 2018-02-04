package com.clt.speech.htk;

public class MlfNonterminalNode extends MlfNode {

    public MlfNonterminalNode(MlfNonterminalNode parent, String label) {

        super(parent, label);
    }

    public MlfNonterminalNode(MlfNonterminalNode parent, String label, double confidence) {

        super(parent, label, confidence);
    }

    @Override
    public boolean getAllowsChildren() {

        return true;
    }

    @Override
    public long getStart() {

        if (this.numChildren() == 0) {
            return 0;
        } else {
            return this.getChild(0).getStart();
        }
    }

    @Override
    public long getEnd() {

        if (this.numChildren() == 0) {
            return 0;
        } else {
            return this.getChild(this.numChildren() - 1).getEnd();
        }
    }
}
