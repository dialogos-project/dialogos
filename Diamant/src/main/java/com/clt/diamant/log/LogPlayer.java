package com.clt.diamant.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.clt.diamant.IdMap;
import com.clt.diamant.Resources;
import com.clt.diamant.SingleDocument;
import com.clt.diamant.graph.Node;
import com.clt.diamant.graph.nodes.OwnerNode;
import com.clt.diamant.graph.nodes.ProcNode;
import com.clt.diamant.log.ExecutionTree.LeafNode;
import com.clt.diamant.log.ExecutionTree.ParentNode;
import com.clt.xml.AbstractHandler;
import com.clt.xml.XMLReader;
import com.clt.xml.XMLWriter;

public class LogPlayer {

    private String vp;
    private String kommentar;
    private String time;

    private List<LogEvent<?>> events;

    private int duration = 0;
    private ParentNode executionRoot;

    public LogPlayer(final SingleDocument doc) {

        this.events = new ArrayList<LogEvent<?>>() {

            @Override
            public boolean add(LogEvent<?> o) {

                LogPlayer.this.duration
                        = Math.max(LogPlayer.this.duration, o.getTime());
                return super.add(o);
            }
        };
    }

    public void readSetup(final XMLReader r) {

        r.setHandler(new AbstractHandler("setup") {

            public void end(String name) {

                if (name.equals("VP")) {
                    LogPlayer.this.vp = this.getValue();
                    if (LogPlayer.this.vp == null) {
                        LogPlayer.this.vp
                                = '<' + Resources.getString("noNameSpecified") + '>';
                    }
                } else if (name.equals("Kommentar")) {
                    LogPlayer.this.kommentar = this.getValue();
                } else if (name.equals("time")) {
                    LogPlayer.this.time = this.getValue();
                }
            }
        });
    }

    public void readExecution(final XMLReader r, final IdMap uid_map) {

        this.executionRoot = new ParentNode(null, null, 0);

        r.setHandler(new AbstractHandler("execution") {

            private Attributes lastAtts = null;
            private ParentNode currentNode = LogPlayer.this.executionRoot;

            int getTime(Attributes atts)
                    throws SAXException {

                try {
                    return Integer.parseInt(atts.getValue("time"));
                } catch (NumberFormatException exn) {
                    r.raiseAttributeValueException("time");
                    return 0; // just to make the compiler happy
                }
            }

            Node getNode(Attributes atts, IdMap uid_map)
                    throws SAXException {

                try {
                    return uid_map.nodes.get(atts.getValue("uid"));
                } catch (NumberFormatException exn) {
                    r.raiseAttributeValueException("uid");
                    return null; // just to make the compiler happy
                }
            }

            public void start(String name, Attributes atts)
                    throws SAXException {

                if (name.equals("start")) {
                    // ignore
                } else if (name.equals("end")) {
                    LogPlayer.this.duration = this.getTime(atts);
                } else if (name.equals("state")) {
                    // ignore
                } else if (name.equals("transition")) {
                    final int time = this.getTime(atts);
                    final LogEvent<Object> evt
                            = new LogEvent<Object>(name, time, new Object[3]);
                    LogPlayer.this.events.add(evt);
                    r.setHandler(new AbstractHandler(name) {

                        public void start(String name, Attributes atts)
                                throws SAXException {

                            if (name.equals("src")) {
                                Node src = getNode(atts, uid_map);
                                evt.setArgument(0, src);
                                new LeafNode(currentNode, src, time, false, evt);
                            } else if (name.equals("dst")) {
                                Node dst = getNode(atts, uid_map);
                                evt.setArgument(1, dst);
                                new LeafNode(currentNode, dst, time, true, evt);
                            }
                        }

                        public void end(String name) {

                            if (name.equals("condition")) {
                                evt.setArgument(2, this.getValue());
                            }
                        }
                    });
                } else if (name.equals("event")) {
                    final LogEvent<String> evt
                            = new LogEvent<String>(name, this.getTime(atts),
                                    new String[]{atts.getValue("type")});
                    LogPlayer.this.events.add(evt);
                } else if (name.equals("error")) {
                    final LogEvent<String> evt
                            = new LogEvent<String>(name, this.getTime(atts),
                                    new String[3]);
                    LogPlayer.this.events.add(evt);
                    r.setHandler(new AbstractHandler("error") {

                        public void end(String name) {

                            if (name.equals("type")) {
                                evt.setArgument(0, this.getValue());
                            } else if (name.equals("message")) {
                                evt.setArgument(1, this.getValue());
                            }
                        }
                    });
                } else if (name.equals("call")) {
                    try {
                        ProcNode n = (ProcNode) this.getNode(atts, uid_map);
                        LogEvent<Object> evt
                                = new LogEvent<Object>("transition", this.getTime(atts),
                                        new Object[]{n, null, "call to"});
                        ParentNode tn
                                = new ParentNode(this.currentNode, n, this.getTime(atts), evt);
                        this.currentNode = tn;
                        LogPlayer.this.events.add(evt);
                    } catch (ClassCastException exn) {
                        r.raiseException(Resources
                                .getString("callTargetNotProcedure"));
                    }
                } else if (name.equals("return")) {
                    try {
                        ProcNode n = (ProcNode) this.getNode(atts, uid_map);
                        this.currentNode
                                = (ParentNode) this.currentNode.getParent();

                        LogPlayer.this.events.add(new LogEvent<Object>(
                                "transition", this.getTime(atts), new Object[]{
                            n, null, "return from"}));
                    } catch (ClassCastException exn) {
                        r.raiseException(Resources
                                .getString("callTargetNotProcedure"));
                    }
                } else if (name.equals("subgraph")) {
                    try {
                        OwnerNode n = (OwnerNode) this.getNode(atts, uid_map);
                        LogEvent<?> evt
                                = new LogEvent<Object>("transition", this.getTime(atts),
                                        new Object[]{n, null, "enter"});
                        ParentNode tn
                                = new ParentNode(this.currentNode, n, this.getTime(atts),
                                        evt);
                        this.currentNode = tn;

                        LogPlayer.this.events.add(evt);
                    } catch (ClassCastException exn) {
                        System.out.println("subgraph: " + exn.toString());
                        r.raiseException(Resources
                                .getString("subgraphTargetNotSubgraph"));
                    }
                } else if (name.equals("exit")) {
                    try {
                        OwnerNode n = (OwnerNode) this.getNode(atts, uid_map);
                        this.currentNode
                                = (ParentNode) this.currentNode.getParent();

                        LogPlayer.this.events.add(new LogEvent<Object>(
                                "transition", this.getTime(atts), new Object[]{
                            n, null, "exit"}));
                    } catch (ClassCastException exn) {
                        System.out.println("exit: " + exn.toString());
                        r.raiseException(Resources
                                .getString("subgraphTargetNotSubgraph"));
                    }
                } else if (name.equals("duration")) {
                } else if (name.equals("input")) {
                    // must make persistent copy!
                    this.lastAtts = new AttributesImpl(atts);
                } else if (name.equals("prompt")) {
                    // must make persistent copy!
                    this.lastAtts = new AttributesImpl(atts);
                } else {
                    r.raiseUnexpectedElementException(name);
                }
            }

            public void end(String name)
                    throws SAXException {

                if (name.equals("input")) {
                    boolean log = false;
                    if (this.lastAtts.getValue("log") != null) {
                        log = this.lastAtts.getValue("log").equals("1");
                    }
                    LogPlayer.this.events.add(new InputLogEvent(this
                            .getTime(this.lastAtts), this.lastAtts.getValue("name"),
                            this.getValue(), log));
                } else if (name.equals("prompt")) {
                    final LogEvent<Object> evt
                            = new LogEvent<Object>(name, this.getTime(this.lastAtts),
                                    new Object[]{this.lastAtts.getValue("name"),
                                        this.getValue()});
                    LogPlayer.this.events.add(evt);
                } else if (name.equals("duration")) {
                    try {
                        long d = Long.parseLong(this.getValue());
                        ((ParentNode) this.currentNode.getChildAt(this.currentNode
                                .getChildCount() - 1)).setDuration(d);
                    } catch (NumberFormatException exn) {
                        r.raiseAttributeValueException("duration");
                    }
                }
            }
        });
    }

    public int getDuration() {

        return this.duration;
    }

    public String getKommentar() {

        return this.kommentar;
    }

    public String getTime() {

        return this.time;
    }

    public String getVp() {

        return this.vp;
    }

    public ParentNode getExecutionRoot() {

        return this.executionRoot;
    }

    public List<LogEvent<?>> getEvents() {

        return Collections.unmodifiableList(this.events);
    }

    public void summarize(File file)
            throws IOException {

        PrintWriter textOut
                = new PrintWriter(new BufferedWriter(new FileWriter(
                        new File(file.getParentFile(), file.getName().substring(0,
                                file.getName().length() - 4)
                                + ".txt"))));
        XMLWriter out = new XMLWriter(file);
        try {
            out.openElement("log");
            out.openElement("header");
            out.printElement("subject", this.getVp());
            out.printElement("comment", this.getKommentar());
            out.printElement("date", this.getTime());
            out.printElement("duration", this.getDuration());
            out.closeElement("header");

            out.openElement("execution");

            textOut.println("Subject: " + this.getVp());
            textOut.println("Comment: " + this.getKommentar());
            textOut.println("Date: " + this.getTime());
            textOut.println("Duration: " + this.getDuration() + "ms");
            textOut.println();

            final String PTT_ID = "4ef67be9-7012-11db-aa58-0728f732c727";
            boolean firstRun = true;
            for (LogEvent<?> evt : this.getEvents()) {
                if (evt instanceof InputLogEvent) {
                    InputLogEvent ievt = (InputLogEvent) evt;
                    String value = ievt.getArgument(0);
                    if (value.equals("{ PTT = true }")) {
                        out.printElement("ptt", new String[]{"time"},
                                new Object[]{evt.getTime()}, null);
                        textOut.println("+" + evt.getTime() + "ms: PTT pressed");
                    } else if (evt.getType().equals("Erkenner")) {
                        if (!value.startsWith("Recognizer warning")) {
                            if (ievt.logOnly()) {
                                out.printElement("asr", new String[]{"time"},
                                        new Object[]{evt.getTime()}, value);
                                textOut.println("+" + evt.getTime() + "ms: "
                                        + value);
                            } else {
                                out.printElement("input", new String[]{"time"},
                                        new Object[]{evt.getTime()}, value);
                                textOut.println("+" + evt.getTime()
                                        + "ms: Recognizer interpretation: "
                                        + value);
                            }
                        }
                    }
                } else if (evt.getType().equals("prompt")
                        && evt.getArgument(0).equals("Synthese")) {
                    String value = (String) evt.getArgument(1);
                    if (!value.startsWith("{")) {
                        if (value.trim().length() > 0) {
                            out.printElement("tts", new String[]{"time"},
                                    new Object[]{evt.getTime()}, value);
                            textOut.println("+" + evt.getTime() + "ms: Prompt: " + value);
                        }
                    }
                } else if (evt.getType().equals("transition")) {
                    if (PTT_ID.equals(((Node) evt.getArgument(0)).getId())) {
                        out.printElement("task", new String[]{"time"},
                                new Object[]{evt.getTime()}, "Start");
                        textOut.println("+" + evt.getTime() + "ms: Task started.");
                    } else if ((evt.getArgument(1) != null)
                            && PTT_ID
                                    .equals(((Node) evt.getArgument(1)).getId())) {
                        if (firstRun) {
                            firstRun = false;
                        } else {
                            out.printElement("task", new String[]{"time"},
                                    new Object[]{evt.getTime()}, "End");
                            textOut.println("+" + evt.getTime() + "ms: Task finished.");
                        }
                    }
                } else if (evt.getType().equals("error")) {
                    if ((evt.getArgument(0) != null)
                            && !evt.getArgument(0).equals("NoMatch")) {
                        if (!evt.getArgument(1).equals("Execution stopped by user")) {
                            out.printElement("error", new String[]{"time"},
                                    new Object[]{evt.getTime()}, evt
                                    .getArgument(1));
                            textOut.println("+" + evt.getTime() + "ms: ERROR: "
                                    + evt.getArgument(1));
                        }
                    }
                }
            }

            out.closeElement("execution");

            out.closeElement("log");
        } finally {
            out.close();
            textOut.close();
        }
    }
}
