package com.clt.diamant.graph;

import java.awt.Color;

import com.clt.diamant.Mapping;

public class Comment extends VisualGraphElement {

    public Comment() {

        this.setComment("");
        this.setColor(new Color(255, 255, 153));
        this.setLocation(0, 0);
        this.setSize(100, 60);
    }

    @Override
    public Comment clone(Mapping map) {

        Comment c = new Comment();
        c.setColor(this.getColor());
        c.setLocation(this.getX(), this.getY());
        c.setSize(this.getWidth(), this.getHeight());
        c.setComment(this.getComment());
        return c;
    }

    @Override
    public void update(Mapping map) {

    }

    public void dispose() {

    }
}
