package com.clt.diamant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

import javax.swing.JLayeredPane;
import javax.swing.RootPaneContainer;

import com.clt.dialog.client.DeviceEvent;
import com.clt.dialog.client.DeviceListener;
import com.clt.dialog.client.ServerDevice;
import com.clt.diamant.graph.GraphExecutionListener;
import com.clt.diamant.graph.Node;
import com.clt.diamant.graph.nodes.OwnerNode;
import com.clt.diamant.graph.nodes.ProcNode;
import com.clt.diamant.log.WizardLog;
import com.clt.script.exp.Pattern;
import com.clt.script.exp.Value;
import com.clt.util.Timeout;
import com.clt.util.UserCanceledException;

public class Executer implements WozInterface {

    private InputCenter input;
    private boolean abort;
    private Collection<GraphExecutionListener> graphExecutionListeners;
    private long delay;
    private WizardLog log;
    private DeviceListener deviceListener;
    private IdMap uid_map;
    private Stack<Long> subgraph_times;
    private RootPaneContainer parent;

    public Executer(RootPaneContainer parent, boolean logging) {

        this.parent = parent;

        if (logging) {
            try {
                this.log = new WizardLog(parent != null ? parent.getContentPane() : null, true);
            } catch (UserCanceledException exn) {
                this.log = null;
            }
        } else {
            this.log = null;
        }

        this.input = null;
        this.abort = false;
        this.delay = 0;
        this.graphExecutionListeners = new ArrayList<GraphExecutionListener>();

        this.deviceListener = new DeviceListener() {

            public void stateChanged(DeviceEvent evt) {

            }

            public void dataSent(DeviceEvent evt) {

            }

            public void dataReceived(DeviceEvent evt) {

                if (Executer.this.log != null) {
                    Executer.this.log.printEvent("input", new String[]{"name"},
                            new Object[]{((ServerDevice) evt.getSource())
                                        .getName()}, evt.getData());
                }
            }

            public void dataLogged(DeviceEvent evt) {

                if (Executer.this.log != null) {
                    Executer.this.log.printEvent("input", new String[]{"name", "log"},
                            new Object[]{
                                ((ServerDevice) evt.getSource()).getName(), "1"},
                            evt.getData());
                }
            }
        };
    }

    public void output(Device d, Value value) {

        // System.out.println("Device " + d + " says: " + s);
        if (this.log != null) {
            this.log.printEvent("prompt", new String[]{"name"}, new Object[]{d
                .getName()},
                    value.toString());
        }
    }

    public void setState(State state) {

        if (this.log != null) {
            this.log.setState(state);
        }
    }

    public void discardOldInput(Device d) {

        this.input.clear(d);
    }

    static class TimeMarker {

        long time;

        public TimeMarker(long time) {

            this.time = time;
        }

        public synchronized void setTime(long time) {

            this.time = time;
        }

        public synchronized long getTime() {

            return this.time;
        }
    }

    public DialogInput<?> getInput(final Pattern[] alternatives, Device d,
            Collection<Device> allDevices, Collection<Device> waitDevices,
            final long timeout,
            boolean forceInput) {

        if (this.abort) {
            throw new ExecutionStoppedException();
        }

        DialogInput<?> din = null;
        final TimeMarker endTime = new TimeMarker(Long.MIN_VALUE);

        final Set<Device> devs = new HashSet<Device>(waitDevices);
        if ((timeout > 0) && (devs.size() > 0)) {
            endTime.setTime(Long.MAX_VALUE);

            for (Iterator<Device> it = new HashSet<Device>(devs).iterator(); it
                    .hasNext();) {
                final Device dev = it.next();
                new Thread(new Runnable() {

                    public void run() {

                        try {
                            // System.out.println("Sending bargein echo to " + dev);
                            dev.echo();
                        } catch (Exception ignore) {
                        }
                        // System.out.println("Echo returned from " + dev);
                        synchronized (devs) {
                            devs.remove(dev);
                            if (devs.size() == 0) {
                                // System.out.println("All echos returned. Starting countdown");
                                devs.notifyAll();
                            }
                        }
                    }
                }, "BargeIn Echo").start();
            }

            new Thread(new Runnable() {

                public void run() {

                    synchronized (devs) {
                        if (devs.size() > 0) {
                            try {
                                devs.wait();
                            } catch (Exception ignore) {
                            }
                        }
                    }

                    endTime.setTime(System.currentTimeMillis() + timeout);
                }
            }, "BargeIn Wait").start();
        }

        try {
            long wait = timeout;
            while (din == null) {
                try {
                    din = this.input.get(d, wait);
                } catch (Timeout t) {
                    long diff = endTime.getTime() - System.currentTimeMillis();
                    if ((diff > 0) && (endTime.getTime() != Long.MIN_VALUE)) {
                        if (wait > diff) {
                            wait = diff;
                            endTime.setTime(Long.MIN_VALUE);
                        }
                    } else {
                        throw t;
                    }
                }
            }
        } catch (Timeout exn) {
            din = new DialogInput<Object>(d, exn);
        } catch (InterruptedException exn) {
            din = null;
        }

        // System.out.println("Received input: " + din);
        devs.clear();
        synchronized (devs) {
            devs.notifyAll();
        }

        if (this.abort || (din == null)) {
            throw new ExecutionStoppedException();
        } else {
            return din;
        }
    }

    public void preExecute(com.clt.script.cmd.Command cmd) {

    }

    public void preEvaluate(com.clt.script.exp.Expression cmd) {

    }

    public void log(String s) {

        System.out.println(s);
    }

    public void preExecute(Node node) {

        if (this.abort) {
            throw new ExecutionStoppedException();
        }
    }

    public void transition(Node source, Node destination, int edge,
            String condition) {

        if (this.abort) {
            throw new ExecutionStoppedException();
        }

        if (this.log != null) {
            if (destination != null) {
                synchronized (this.log) {
                    this.log.openElement("transition");
                    this.log.printElement("src", new String[]{"uid", "class"},
                            new Object[]{
                                this.uid_map.nodes.put(source),
                                source.getClass().getName()},
                            source.getTitle());
                    this.log.printElement("dst", new String[]{"uid", "class"},
                            new Object[]{
                                this.uid_map.nodes.put(destination),
                                destination.getClass().getName()},
                            destination.getTitle());
                    if (condition != null) {
                        this.log.printElement("condition", condition);
                    }
                    this.log.closeElement("transition");
                }
            } else {
                this.error("EdgeNotConnectedException", "node_uid "
                        + this.uid_map.nodes.put(source)
                        + ", edge " + edge);
            }
        }
    }

    public void subgraph(OwnerNode owner, boolean enter) {

        for (GraphExecutionListener l : this.graphExecutionListeners) {
            if (enter) {
                l.graphExecutionStarted(owner.getOwnedGraph());
            } else {
                l.graphExecutionStopped(owner.getOwnedGraph());
            }
        }

        if (this.log != null) {
            String ownerID = this.uid_map.nodes.put(owner);
            String ownerName = owner.getGraphName();

            String tag = (owner instanceof ProcNode) ? "call" : "subgraph";
            if (enter) {
                long start_time
                        = this.log.printEvent(tag, new String[]{"title", "uid"},
                                new Object[]{ownerName, ownerID}, null);
                this.subgraph_times.push(new Long(start_time));
            } else {
                long end_time
                        = this.log.printEvent((owner instanceof ProcNode) ? "return" : "exit",
                                new String[]{"title", "uid"}, new Object[]{ownerName,
                            ownerID}, null);
                long time = end_time - this.subgraph_times.pop().longValue();
                this.log.printElement("duration", new String[]{"title", "uid"},
                        new Object[]{
                            owner.getGraphName(), ownerID}, new Long(time));
            }
        }
    }

    public void error(String type, String message) {

        if (this.log != null) {
            synchronized (this.log) {
                this.log.openElement("error");
                this.log.printElement("type", type);
                if (message != null) {
                    this.log.printElement("message", message);
                }
                this.log.closeElement("error");
            }
        }
    }

    public void abort() {

        this.abort = true;
        if (this.input != null) {
            this.input.dispose();
        }
    }

    public boolean initInterface() {

        return true;
    }

    public boolean isDebugger() {

        return false;
    }

    public boolean showSubdialogsDuringExecution() {

        return false;
    }

    public void startDocument(Document d, String name, InputCenter input) {

        this.input = input;
        if (this.log != null) {
            input.addDeviceListener(this.deviceListener);
        }

        if (this.log != null) {
            try {
                this.log.setFile(name);
            } catch (IOException exn) {
                throw new RuntimeException(exn.getLocalizedMessage());
            }
            this.uid_map = this.log.start(d);
            this.subgraph_times = new Stack<Long>();
        }
    }

    public void endDocument(Document d) {

        if (this.log != null) {
            this.log.end();
            this.uid_map = null;
            this.subgraph_times = null;
            this.input.removeDeviceListener(this.deviceListener);
        }
    }

    public void disposeInterface(boolean error) {

        this.log = null;
    }

    public String getName() {

        return "Runtime environment";
    }

    public void addGraphExecutionListener(GraphExecutionListener l) {

        this.graphExecutionListeners.add(l);
    }

    public void removeGraphExecutionListener(GraphExecutionListener l) {

        this.graphExecutionListeners.remove(l);
    }

    public void setDelay(long delay) {

        this.delay = delay;
    }

    public long getDelay() {

        return this.delay;
    }

    public JLayeredPane getLayeredPane() {

        if (this.parent != null) {
            return this.parent.getLayeredPane();
        } else {
            return null;
        }
    }
}
