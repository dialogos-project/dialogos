package com.clt.srgf;

import java.util.LinkedList;
import java.util.ListIterator;

class PenaltyList {

    private LinkedList<LinkedList<Parse>> lists;

    public PenaltyList() {

        this.lists = new LinkedList<LinkedList<Parse>>();
    }

    public void add(Parse p) {

        int inputSize = p.getInputSize();
        for (ListIterator<LinkedList<Parse>> it = this.lists.listIterator(); it
                .hasNext();) {
            LinkedList<Parse> l = it.next();
            int size = l.getFirst().getInputSize();
            if (inputSize > size) {
                l = new LinkedList<Parse>();
                l.add(p);
                if (it.hasPrevious()) {
                    it.previous();
                    it.add(l);
                } else {
                    this.lists.addFirst(l);
                }
                return;
            } else if (inputSize == size) {
                l.addFirst(p);
                return;
            }
        }

        LinkedList<Parse> l = new LinkedList<Parse>();
        l.add(p);
        this.lists.add(l);
    }

    public Parse removeFirst() {

        LinkedList<Parse> firstList = this.lists.getFirst();
        Parse result = firstList.removeFirst();
        if (firstList.isEmpty()) {
            this.lists.removeFirst();
        }
        return result;
    }

    public boolean isEmpty() {

        return this.lists.isEmpty();
    }
}
