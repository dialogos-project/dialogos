package com.clt.dialog.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.jmdns.ServiceInfo;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.clt.script.exp.Value;
import com.clt.script.exp.values.Undefined;
import com.clt.xml.AbstractHandler;
import com.clt.xml.DefaultErrorHandler;
import com.clt.xml.XMLReader;
import com.clt.xml.XMLWriter;

/**
 * @author Daniel Bobbert
 * @version 6.5
 */
class ClientDevice
        extends Device {

    static final int DYNAMIC_PORT_SELECTION = 0;

    private static final boolean handleTimeoutSignalImmediately = false;

    private ServerSocket socket;
    private InputQueue<Runnable> inputQueue;

    private boolean dying = false;
    private Thread qThread, rThread;

    protected Client client;

    private InputStream in;
    private XMLWriter xml_out;
    private DataOutputStream bin_out;
    private int priority = Thread.NORM_PRIORITY;
    private ServiceInfo service = null;

    public ClientDevice(final Client client) {

        this.client = client;

        if (client == null) {
            throw new IllegalArgumentException("Client may not be null");
        }

        this.socket = null;

        this.addDeviceListener(new DeviceListener() {

            public void stateChanged(DeviceEvent evt) {

                Device.debug("Device state changed: " + evt.getState());

                client.stateChanged(evt.getState());
            }

            public void dataSent(DeviceEvent evt) {

            }

            public void dataReceived(DeviceEvent evt) {

            }

            public void dataLogged(DeviceEvent evt) {

            }
        });
    }

    public void setPriority(int priority) {

        this.priority = priority;
        if (this.qThread != null) {
            this.qThread.setPriority(priority);
        }
        if (this.rThread != null) {
            this.rThread.setPriority(priority);
        }
    }

    private void closeStream() {

        if (this.xml_out != null) {
            try {
                this.xml_out.closeElement("messages");
            } catch (Exception ignore) {
            }

            try {
                this.xml_out.close();
            } catch (Exception ignore) {
            }

            this.xml_out = null;
        }

        if (this.bin_out != null) {
            try {
                this.bin_out.close();
            } catch (Exception ignore) {
            }

            this.bin_out = null;
        }

        if (this.in != null) {
            try {
                this.in.close();
            } catch (Exception ignore) {
            }
            this.in = null;
        }
    }

    public void close() {

        this.dying = true;

        this.closeStream();

        try {
            if (this.service != null) {
                Rendezvous.removeService(this.service);
            }
        } catch (Exception ignore) {
        }
        this.service = null;

        if (this.socket != null) {
            try {
                this.socket.close();
            } catch (Exception ignore) {
            }
            this.socket = null;
        }

        if (this.inputQueue != null) {
            this.inputQueue.dispose();
            this.inputQueue = null;
        }

        try {
            this.qThread.interrupt();
            this.qThread.join();
        } catch (Exception exn) {
            // System.err.println(exn);
        }

        try {
            this.rThread.interrupt();
            this.rThread.join();
        } catch (Exception exn) {
            // System.err.println(exn);
        }
    }

    public void activate(int port, final Runnable init, boolean advertise, boolean localhostOnly) throws IOException {
        this.dying = false;

        // System.out.println("open on port " + port);
        if (port == ClientDevice.DYNAMIC_PORT_SELECTION) {
            this.socket = new ServerSocket(0);
        } else {
            this.socket = new ServerSocket(port);
        }

        this.socket.setSoTimeout(0);

        port = this.getPort();
        if (advertise) {
            this.service = Rendezvous.createService(this.client.getName(), port, Device.VERSION, localhostOnly);
            Rendezvous.addService(this.service);
        }

        this.inputQueue = new InputQueue<Runnable>();

        this.qThread = new Thread(new Runnable() {

            public void run() {

                try {
                    if (init != null) {
                        init.run();
                    }
                    while (!ClientDevice.this.dying) {
                        try {
                            Runnable r = ClientDevice.this.inputQueue.get();
                            Device.debug("Executing action " + r);
                            r.run();
                        } catch (InterruptedException exn) {
                            throw exn;
                        } catch (ThreadDeath d) {
                            throw d;
                        } catch (Throwable exn) {
                            exn.printStackTrace();
                            ClientDevice.this.client.error(exn);
                        }
                    }
                } catch (InterruptedException exn) {
                }
            }
        }, this.client.getName() + " input queue");
        this.qThread.setPriority(this.priority);
        this.qThread.start();

        this.rThread = new Thread(new Runnable() {

            public void run() {

                try {
                    ClientDevice.this.run();
                } catch (Exception ignore) {
                }
            }
        }, this.client.getName() + " network protocol");
        this.rThread.setPriority(this.priority);
        this.rThread.start();
    }

    private Connection accept()
            throws IOException {

        final Socket clientSocket = this.socket.accept();
        if (clientSocket == null) {
            throw new InterruptedIOException();
        }

        InputStream in = clientSocket.getInputStream();
        final int type = new DataInputStream(in).readInt();
        if (type == Protocol.RAW_XML) {
            in = new SequenceInputStream(new ByteArrayInputStream(new byte[]{
                (byte) ((Protocol.RAW_XML >> 24) & 0xff),
                (byte) ((Protocol.RAW_XML >> 16) & 0xff),
                (byte) ((Protocol.RAW_XML >> 8) & 0xff),
                (byte) ((Protocol.RAW_XML >> 0) & 0xff)}), in);
        }

        final OutputStream output
                = new BufferedOutputStream(clientSocket.getOutputStream());
        final InputStream input = new BufferedInputStream(in);

        return new Connection() {

            public InetAddress getTargetAddress() {

                return clientSocket.getInetAddress();
            }

            public int getPort() {

                return clientSocket.getPort();
            }

            public InputStream getInputStream() {

                return input;
            }

            public OutputStream getOutputStream() {

                return output;
            }

            public void close()
                    throws IOException {

                clientSocket.close();
            }

            public int getProtocol() {

                return type;
            }
        };
    }

    private void run()
            throws IOException {

        while (!this.dying) {
            this.setState(ConnectionState.CONNECTING);

            Connection connection = this.accept();

            this.setState(ConnectionState.CONNECTED);

            try {
                this.execute(connection);
            } catch (ThreadDeath d) {
                throw d;
            } catch (Throwable t) {
            }

            // System.out.println("closed");
            this.setState(ConnectionState.DISCONNECTED);

            this.closeStream();
        }
    }

    public int getPort() {

        if (this.socket == null) {
            return 0;
        } else {
            return this.socket.getLocalPort();
        }
    }

    private void postAction(Runnable r) {

        this.inputQueue.put(r);
    }

    private void clearActions() {

        this.inputQueue.clear();
    }

    private void execute(Connection connection)
            throws IOException {

        this.client.reset();

        switch (connection.getProtocol()) {
            case Protocol.XML:
            case Protocol.RAW_XML:
                this.executeXML(connection.getInputStream(), connection
                        .getOutputStream());
                break;
            case Protocol.BINARY:
                this.executeBinary(connection.getInputStream(), connection
                        .getOutputStream());
                break;
            default:
                int type = connection.getProtocol();
                throw new IOException("Unexpected protocol type: "
                        + (char) ((type >> 24) & 0xff)
                        + (char) ((type >> 16) & 0xff)
                        + (char) ((type >> 8) & 0xff)
                        + (char) ((type >> 0) & 0xff));
        }
    }

    private void executeXML(final InputStream in, final OutputStream out) throws IOException {
        final XMLReader r = new XMLReader(false);
        final XMLWriter w = new XMLWriter(out);
        w.setIndent(false);
        w.openElement(Protocol.XML_MAIN);
        w.flush();
        this.in = in;
        this.xml_out = w;

        r.parse(in, new AbstractHandler(Protocol.XML_MAIN) {
            // Attributes atts;
            @Override
            protected void start(String name, Attributes atts) {
                Device.debug("<" + name + " " + Device.toString(atts) + ">");

                // this.atts = copyAttributes(atts);
                if (name.equals(Protocol.XML_NAME)) {
                    ClientDevice.this.echo(w, Protocol.XML_NAME, ClientDevice.this.client.getName());
                } else if (name.equals(Protocol.XML_START)) {
                    ClientDevice.this.client_start();
                } else if (name.equals(Protocol.XML_RESET)) {
                    ClientDevice.this.client_reset();
                } else if (name.equals(Protocol.XML_VALUE)) {
                    ClientDevice.this.receive(r, atts.getValue("type"), null, new ValueReceiver() {
                        public void valueReceived(String name, Value v) {
                            ClientDevice.this.client_dataReceived(v);
                        }
                    });
                } else if (name.equals(Protocol.XML_RPC)) {
                    r.setHandler(new AbstractHandler(name) {
                        String procedure = null;
                        List<Value> arguments = new ArrayList<Value>();

                        @Override
                        protected void start(String name, Attributes atts) {
                            if (name.equals(Protocol.XML_VALUE)) {
                                ClientDevice.this.receive(r, atts.getValue("type"), null, new ValueReceiver() {
                                    public void valueReceived(String name, final Value v) {
                                        arguments.add(v);
                                    }
                                });
                            }
                        }

                        @Override
                        protected void end(String name) throws SAXException {
                            if (name.equals(Protocol.XML_RPC_PROC)) {
                                this.procedure = this.getValue();
                            } else if (name.equals(Protocol.XML_RPC)) {
                                if (this.procedure != null) {
                                    throw new SAXException("missing procedure name in rpc");
                                }
                                Value result = null;
                                try {
                                    result = ClientDevice.this.client.rpc(this.procedure,
                                            this.arguments.toArray(new Value[this.arguments.size()]));

                                    try {
                                        w.openElement(Protocol.XML_RPC_RES);
                                        ClientDevice.this.send(w, result);
                                        w.closeElement(Protocol.XML_RPC_RES);
                                    } catch (Exception ioexn) {
                                        ClientDevice.this.error(ioexn);
                                    }
                                } catch (ThreadDeath d) {
                                    throw d;
                                } catch (Throwable exn) {
                                    try {
                                        w.printElement(Protocol.XML_RPC_ERR, exn.getLocalizedMessage());
                                        w.flush();
                                    } catch (Exception ioexn) {
                                        ClientDevice.this.error(ioexn);
                                    }
                                }
                            }
                        }
                    });
                } else {
                    System.err.println("WARNING: Unexpected XML protocol element \"" + name + "\"");

                    // r.skipElement(name);
                    // AK 01/18: Don't skip unexpected elements. DialogOS and the client
                    // are sending at least "version", "messages", and "ping" messages
                    // to each other. Maybe this should be cleaned up sometime (see issue #23).
                }
            }

            @Override
            protected void end(String name) {

                Device.debug("  " + this.getValue());
                Device.debug("</" + name + ">");

                if (name.equals(Protocol.XML_VERSION)) {
                    ClientDevice.this.echo(w, Protocol.XML_VERSION, Device.VERSION);
                } else if (name.equals(Protocol.XML_PAUSE)) {
                    ClientDevice.this.client_pause(this.getValue());
                } else if (name.equals(Protocol.XML_TIMEOUT)) {
                    ClientDevice.this.client_allowTimeout(this.getValue()
                            .equalsIgnoreCase("true"));
                } else if (name.equals(Protocol.XML_TIMEOUT_SIGNAL)) {
                    ClientDevice.this.client_signalTimeout();
                } else if (name.equals(Protocol.XML_PING)) {
                    Device.debug("Got echo request");
                    final String id = this.getValue();
                    ClientDevice.this.postAction(new Runnable() {

                        public void run() {

                            try {
                                Device.debug("Waiting for client...");
                                ClientDevice.this.client.waitUntilDone();
                            } finally {
                                Device.debug("Sending echo response");
                                if (id == null ? true : id.trim().length() == 0) {
                                    ClientDevice.this.echo(w, Protocol.XML_PING,
                                            null);
                                } else {
                                    ClientDevice.this
                                            .echo(w, Protocol.XML_PING, id);
                                }
                            }
                        }

                        @Override
                        public String toString() {

                            return "waitUntilDone()";
                        }
                    });
                }
            }
        }, new DefaultErrorHandler());
    }

    private void executeBinary(final InputStream in, final OutputStream out)
            throws IOException {

        final DataInputStream r = new DataInputStream(in);
        final DataOutputStream w = new DataOutputStream(out);

        this.in = r;
        this.bin_out = w;

        while (true) {
            int msg = r.readByte();
            switch (msg) {
                case Protocol.BIN_NAME:
                    this.echo(w, Protocol.BIN_NAME, this.client.getName());
                    break;

                case Protocol.BIN_START:
                    this.client_start();
                    break;

                case Protocol.BIN_RESET:
                    this.client_reset();
                    break;

                case Protocol.BIN_VALUE:
                    this.client_dataReceived(this.receive(r));
                    break;

                case Protocol.BIN_RPC:
                    String rpc_proc = r.readUTF();
                    int numArgs = r.readInt();
                    Value[] rpc_args = new Value[numArgs];
                    for (int i = 0; i < rpc_args.length; i++) {
                        byte argMsg = r.readByte();
                        if (argMsg != Protocol.BIN_VALUE) {
                            throw new IOException("Expected RPC argument but got: " + argMsg);
                        }
                        rpc_args[i] = this.receive(r);
                    }
                    try {
                        Value result = this.client.rpc(rpc_proc, rpc_args);
                        if (result == null) {
                            result = new Undefined();
                        }

                        try {
                            w.writeByte(Protocol.BIN_RPC_RES);
                            this.send(w, result);
                        } catch (Exception ioexn) {
                            this.error(ioexn);
                        }
                    } catch (ThreadDeath d) {
                        throw d;
                    } catch (Throwable exn) {
                        try {
                            w.writeByte(Protocol.BIN_RPC_ERR);
                            w.writeUTF(exn.getLocalizedMessage());
                            w.flush();
                        } catch (Exception ioexn) {
                            this.error(ioexn);
                        }
                    }
                    break;

                case Protocol.BIN_VERSION:
                    @SuppressWarnings("unused") String serverVersion = r.readUTF();
                    this.echo(w, Protocol.BIN_VERSION, Device.VERSION);
                    break;

                case Protocol.BIN_PAUSE:
                    this.client_pause(r.readUTF());
                    break;

                case Protocol.BIN_TIMEOUT:
                    this.client_allowTimeout(r.readBoolean());
                    break;

                case Protocol.BIN_TIMEOUT_SIGNAL:
                    this.client_signalTimeout();
                    break;

                case Protocol.BIN_PING:
                    Device.debug("Got echo request");
                    final String id = r.readUTF();
                    this.postAction(new Runnable() {

                        public void run() {

                            try {
                                Device.debug("Waiting for client...");
                                ClientDevice.this.client.waitUntilDone();
                            } finally {
                                Device.debug("Sending echo response");
                                if (id == null ? true : id.trim().length() == 0) {
                                    ClientDevice.this.echo(w, Protocol.BIN_PING,
                                            null);
                                } else {
                                    ClientDevice.this
                                            .echo(w, Protocol.BIN_PING, id);
                                }
                            }
                        }

                        @Override
                        public String toString() {

                            return "waitUntilDone()";
                        }
                    });
                    break;

                default:
                    throw new Protocol.Exception(
                            "Unexpected binary protocol message from server: "
                            + msg);
            }
        }
    }

    public void send(Object value)
            throws IOException {

        if (this.xml_out != null) {
            this.send(this.xml_out, value);
        }

        if (this.bin_out != null) {
            this.send(this.bin_out, value);
        }
    }

    public void sendTimeout()
            throws IOException {

        if (this.xml_out != null) {
            synchronized (this.xml_out) {
                this.xml_out.printElement(Protocol.XML_TIMEOUT, null);
                this.xml_out.flush();
            }
        }

        if (this.bin_out != null) {
            synchronized (this.bin_out) {
                this.bin_out.writeByte(Protocol.BIN_TIMEOUT);
                this.bin_out.flush();
            }
        }
    }

    public void log(String message)
            throws IOException {

        if (this.xml_out != null) {
            synchronized (this.xml_out) {
                this.xml_out.printElement(Protocol.XML_LOG, message);
                this.xml_out.flush();
            }
        }

        if (this.bin_out != null) {
            synchronized (this.bin_out) {
                this.bin_out.writeByte(Protocol.BIN_LOG);
                this.bin_out.writeUTF(message);
                this.bin_out.flush();
            }
        }
    }

    private void echo(XMLWriter w, String tag, String value) {

        try {
            w.printElement(tag, value);
            w.flush();
        } catch (Exception exn) {
            this.error(exn);
        }
    }

    private void echo(DataOutputStream w, byte tag, String value) {

        try {
            w.writeByte(tag);
            if (value == null) {
                w.writeUTF("");
            } else {
                w.writeUTF(value);
            }
            w.flush();
        } catch (Exception exn) {
            this.error(exn);
        }
    }

    private void client_start() {

        Device.debug("start");
        this.clearActions();
        this.client.sessionStarted();
    }

    private void client_reset() {

        Device.debug("reset");
        this.clearActions();
        this.client.reset();
    }

    private void client_dataReceived(final Value v) {

        Device.debug("Received value " + v);
        this.postAction(new Runnable() {

            public void run() {

                ClientDevice.this.client.output(v);
            }

            @Override
            public String toString() {

                return "output(" + v + ")";
            }
        });
    }

    private void client_pause(final String pause) {

        this.postAction(new Runnable() {

            public void run() {

                try {
                    Thread.sleep(Integer.parseInt(pause));
                } catch (Exception exn) {
                    ClientDevice.this.error(exn);
                }
            }

            @Override
            public String toString() {

                return "pause(" + pause + ")";
            }
        });
    }

    private void client_allowTimeout(final boolean allowTimeout) {

        Device.debug("allowTimeout(" + allowTimeout + ")");
        if (ClientDevice.handleTimeoutSignalImmediately) {
            this.client.allowTimeout(allowTimeout);
        } else {
            this.postAction(new Runnable() {

                public void run() {

                    ClientDevice.this.client.allowTimeout(allowTimeout);
                }

                @Override
                public String toString() {

                    return "allowTimeout(" + allowTimeout + ")";
                }
            });
        }

    }

    private void client_signalTimeout() {

        Device.debug("signalTimeout()");
        if (ClientDevice.handleTimeoutSignalImmediately) {
            this.client.signalTimeout();
        } else {
            this.postAction(new Runnable() {

                public void run() {

                    ClientDevice.this.client.signalTimeout();
                }

                @Override
                public String toString() {

                    return "signalTimeout()";
                }
            });
        }
    }

    @Override
    protected void error(Throwable exn) {

        this.client.error(exn);
    }

    /*
   * private static class MessageFormatException extends SAXException { public
   * MessageFormatException(String s) { super(s); } }
     */
}
