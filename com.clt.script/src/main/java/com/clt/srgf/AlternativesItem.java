package com.clt.srgf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author dabo
 *
 */
public class AlternativesItem extends Item {

    private Alternatives alternatives;

    public AlternativesItem(Alternatives alternatives, int repeatMin,
            int repeatMax) {

        super(repeatMin, repeatMax);

        this.alternatives = alternatives;
    }

    private AlternativesItem(AlternativesItem item) {

        super(item);

        this.alternatives = item.alternatives;
    }

    @Override
    public Item copy() {

        return new AlternativesItem(this);
    }

    @Override
    public List<Parse> shift_(Parse p) {

        if (this.alternatives.size() == 0) {
            return Collections.emptyList();
        } else if (this.alternatives.size() == 1) {
            Expansion e = this.alternatives.get(0);
            this.setDone(true);
            p
                    .setProbability(p.getProbability()
                            * this.alternatives.getProbability(0));
            p.push(e.createInstance());
            return Item.itemList(p);
        } else {
            List<Parse> l = new ArrayList<Parse>(this.alternatives.size());
            this.setDone(true);
            l.add(p);
            for (int i = 1; i < this.alternatives.size(); i++) {
                Expansion e = this.alternatives.get(i);
                Parse branch = p.branch();
                branch.setProbability(branch.getProbability()
                        * this.alternatives.getProbability(i));
                branch.push(e.createInstance());
                l.add(branch);
            }
            p.push(this.alternatives.get(0).createInstance());
            p
                    .setProbability(p.getProbability()
                            * this.alternatives.getProbability(0));
            return l;
        }
    }

}
