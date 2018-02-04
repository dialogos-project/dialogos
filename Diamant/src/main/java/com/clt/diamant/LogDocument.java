package com.clt.diamant;

import java.io.File;

import org.xml.sax.Attributes;

import com.clt.diamant.log.LogPlayer;
import com.clt.xml.AbstractHandler;
import com.clt.xml.XMLReader;

public class LogDocument extends SingleDocument {

    private LogPlayer player;

    public LogDocument() {

        super();

        this.player = new LogPlayer(this);

        this.setReadOnly(true);
    }

    public LogPlayer getLogPlayer() {

        return this.player;
    }

    @Override
    public void load(final File f, final XMLReader r) {

        final IdMap uid_map = new IdMap();

        r.setHandler(new AbstractHandler("log") {

            @Override
            public void start(String name, Attributes atts) {

                if (name.equals("setup")) {
                    LogDocument.this.player.readSetup(r);
                } else if (name.equals("wizard")) {
                    LogDocument.this.load(f, r, uid_map);
                } else if (name.equals("execution")) {
                    LogDocument.this.player.readExecution(r, uid_map);
                }
            }
        });
    }

    @Override
    public boolean isDirty() {

        return false;
    }
}
