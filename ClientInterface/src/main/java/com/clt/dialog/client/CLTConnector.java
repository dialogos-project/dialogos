package com.clt.dialog.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.rmi.RemoteException;

import org.xml.sax.Attributes;

import com.clt.script.exp.Value;
import com.clt.xml.AbstractHandler;
import com.clt.xml.XMLReader;
import com.clt.xml.XMLWriter;

/**
 *
 *
 * @author Daniel Bobbert
 * @version 6.5
 */
abstract class CLTConnector extends AbstractConnector {

    private DeviceDelegate device;

    private Connection client;
    private XMLWriter xml_out;
    private DataOutputStream bin_out;
    private InputStream in;

    private final Object echoLock = new Object();
    private InputQueue<Object> inputQueue;
    private InputQueue<Object> rpcQueue = new InputQueue<Object>();

    protected CLTConnector() {

        this.device = new DeviceDelegate();
    }

    public InetAddress getTargetAddress() {

        if (this.client == null) {
            return null;
        } else {
            return this.client.getTargetAddress();
        }
    }

    public int getPort() {

        if (this.client == null) {
            return 0;
        } else {
            return this.client.getPort();
        }
    }

    public void addDeviceListener(DeviceListener l) {

        this.device.addDeviceListener(l);
    }

    public void removeDeviceListener(DeviceListener l) {

        this.device.removeDeviceListener(l);
    }

    public ConnectionState getState() {

        return this.device.getState();
    }

    public void setState(ConnectionState state) {

        this.device.setState(state);
    }

    public void error(Throwable exn) {

        exn.printStackTrace();
        this.close();
    }

    public synchronized void start()
            throws IOException {

        if (this.xml_out != null) {
            this.xml_out.printElement(Protocol.XML_START, null);
            this.xml_out.flush();
        } else if (this.bin_out != null) {
            this.bin_out.writeByte(Protocol.BIN_START);
            this.bin_out.flush();
        } else {
            throw new ConnectException("Socket not connected");
        }
    }

    public synchronized void reset()
            throws IOException {

        // release pending echos, because otherwise they will never return after a
        // reset
        synchronized (this.echoLock) {
            this.echoLock.notifyAll();
        }

        this.rpcQueue.interrupt();

        if (this.xml_out != null) {
            this.xml_out.printElement(Protocol.XML_RESET, null);
            this.xml_out.flush();
        } else if (this.bin_out != null) {
            this.bin_out.writeByte(Protocol.BIN_RESET);
            this.bin_out.flush();
        } else {
            throw new ConnectException("Socket not connected");
        }
    }

    public synchronized void send(Value value)
            throws IOException {

        if (this.xml_out != null) {
            this.device.send(this.xml_out, value);
        } else if (this.bin_out != null) {
            this.device.send(this.bin_out, value);
        } else {
            throw new ConnectException("Socket not connected");
        }
    }

    public synchronized Value rpc(String procedure, Value[] arguments)
            throws IOException {

        if (this.xml_out != null) {
            this.xml_out.openElement(Protocol.XML_RPC,
                    new String[]{Protocol.XML_RPC_SRC},
                    new Object[]{new Integer(CLTConnector.this.hashCode())});
            this.xml_out.printElement(Protocol.XML_RPC_PROC, procedure);
            if (arguments != null) {
                for (int i = 0; i < arguments.length; i++) {
                    this.device.send(this.xml_out, arguments[i]);
                }
            }
            this.xml_out.closeElement(Protocol.XML_RPC);
            this.xml_out.flush();
            try {
                Object result = this.rpcQueue.get();
                if (result instanceof Value) {
                    return (Value) result;
                } else {
                    throw new RemoteException(String.valueOf(result));
                }
            } catch (InterruptedException exn) {
                this.close();
                throw new InterruptedIOException(exn.getLocalizedMessage());
            }
        } else if (this.bin_out != null) {
            this.bin_out.writeByte(Protocol.BIN_RPC);
            this.bin_out.writeUTF(procedure);
            if (arguments == null) {
                this.bin_out.writeInt(0);
            } else {
                this.bin_out.writeInt(arguments.length);
                for (int i = 0; i < arguments.length; i++) {
                    this.device.send(this.bin_out, arguments[i]);
                }
            }
            this.bin_out.flush();
            try {
                Object result = this.rpcQueue.get();
                if (result instanceof Value) {
                    return (Value) result;
                } else {
                    throw new RuntimeException((String) result);
                }
            } catch (InterruptedException exn) {
                this.close();
                throw new InterruptedIOException(exn.getLocalizedMessage());
            }
        } else {
            throw new ConnectException("Socket not connected");
        }
    }

    public synchronized void allowTimeout(boolean allowTimeout)
            throws IOException {

        if (this.xml_out != null) {
            this.xml_out.printElement(Protocol.XML_TIMEOUT, String
                    .valueOf(allowTimeout));
            this.xml_out.flush();
        } else if (this.bin_out != null) {
            this.bin_out.writeByte(Protocol.BIN_TIMEOUT);
            this.bin_out.writeBoolean(allowTimeout);
            this.bin_out.flush();
        } else {
            throw new ConnectException("Socket not connected");
        }
    }

    public synchronized void signalTimeout()
            throws IOException {

        if (this.xml_out != null) {
            this.xml_out.printElement(Protocol.XML_TIMEOUT_SIGNAL, null);
            this.xml_out.flush();
        } else if (this.bin_out != null) {
            this.bin_out.writeByte(Protocol.BIN_TIMEOUT_SIGNAL);
            this.bin_out.flush();
        } else {
            throw new ConnectException("Socket not connected");
        }
    }

    public void echo()
            throws IOException {

        if (this.xml_out != null) {
            try {
                synchronized (this.echoLock) {
                    synchronized (this) {
                        this.xml_out.printElement(Protocol.XML_PING, null);
                        this.xml_out.flush();
                    }
                    this.echoLock.wait();
                }
            } catch (InterruptedException exn) {
            }
        } else if (this.bin_out != null) {
            try {
                synchronized (this.echoLock) {
                    synchronized (this) {
                        this.bin_out.writeByte(Protocol.BIN_PING);
                        this.bin_out.writeUTF("");
                        this.bin_out.flush();
                    }
                    this.echoLock.wait();
                }
            } catch (InterruptedException exn) {
            }
        } else {
            throw new ConnectException("Socket not connected");
        }
    }

    public void pushInput(Value value) {

        this.inputQueue.put(value);
    }

    public Value receive()
            throws IOException, InterruptedException {

        if (this.inputQueue == null) {
            throw new ConnectException(
                    "Connector not initialized. You must call open() first.");
        }

        Object o = this.inputQueue.get();
        if (o instanceof IOException) {
            throw (IOException) o;
        } else if (o instanceof InterruptedIOException) {
            throw (InterruptedIOException) o;
        } else if (o instanceof Value) {
            return (Value) o;
        } else {
            throw new IOException("Unexpected type of input");
        }
    }

    private void fillInputQueue(InputStream in)
            throws IOException {

        if (!(in instanceof BufferedInputStream)) {
            in = new BufferedInputStream(in);
        }

        if (this.xml_out != null) {
            this.fillInputQueueXML(in);
        } else if (this.bin_out != null) {
            this.fillInputQueueBinary(in);
        } else {
            throw new IOException("Output stream is not connected");
        }
    }

    private void fillInputQueueXML(InputStream in)
            throws IOException {

        final XMLReader r = new XMLReader(false);
        r.parse(in, new AbstractHandler(Protocol.XML_MAIN) {

            @Override
            protected void start(String name, Attributes atts) {

                if (name.equals(Protocol.XML_PING) || name.equals("pong")) {
                    synchronized (CLTConnector.this.echoLock) {
                        CLTConnector.this.echoLock.notifyAll();
                    }
                } else if (name.equals(Protocol.XML_VALUE)) {
                    CLTConnector.this.device.receive(r, atts.getValue("type"), null,
                            new DeviceDelegate.ValueReceiver() {

                        public void valueReceived(String name, final Value v) {

                            CLTConnector.this.pushInput(v);
                        }
                    });
                } else if (name.equals(Protocol.XML_TIMEOUT)) {
                    CLTConnector.this.inputQueue.put(new InterruptedIOException());
                } else if (name.equals(Protocol.XML_RPC_ERR)) {
                    r.setHandler(new AbstractHandler(name) {

                        @Override
                        protected void end(String name) {

                            if (name.equals("rpc_error")) {
                                CLTConnector.this.rpcQueue.put(this.getValue());
                            }
                        }
                    });
                } else if (name.equals(Protocol.XML_RPC_RES)) {
                    r.setHandler(new AbstractHandler(name) {

                        @Override
                        protected void start(String name, Attributes atts) {

                            if (name.equals(Protocol.XML_VALUE)) {
                                CLTConnector.this.device.receive(r, atts.getValue("type"),
                                        null,
                                        new DeviceDelegate.ValueReceiver() {

                                    public void valueReceived(String name,
                                            final Value v) {

                                        CLTConnector.this.rpcQueue.put(v);
                                    }
                                });
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

                if (name.equals(Protocol.XML_LOG)) {
                    CLTConnector.this.device.fireDataLogged(this.getValue());
                } else if (name.equals(Protocol.XML_VERSION)) {
                    String clientInterfaceVersion = this.getValue();
                    if (!clientInterfaceVersion.equals(Device.VERSION)) {
                        System.err.println("WARNING: Client Interface Mismatch");
                    }
                }
            }
        });
    }

    private void fillInputQueueBinary(InputStream in)
            throws IOException {

        DataInputStream r = new DataInputStream(in);

        while (true) {
            byte msg = r.readByte();

            Device.debug("Received binary message " + msg);
            switch (msg) {
                case Protocol.BIN_PING:
                    @SuppressWarnings("unused") String pingID = r.readUTF();
                    synchronized (this.echoLock) {
                        this.echoLock.notifyAll();
                    }
                    break;

                case Protocol.BIN_VALUE:
                    this.pushInput(this.device.receive(r));
                    break;

                case Protocol.BIN_TIMEOUT:
                    this.inputQueue.put(new InterruptedIOException());
                    break;

                case Protocol.BIN_RPC_ERR:
                    this.rpcQueue.put(r.readUTF());
                    break;

                case Protocol.BIN_RPC_RES:
                    byte b = r.readByte();
                    if (b != Protocol.BIN_VALUE) {
                        throw new Protocol.Exception(
                                "RPC result anounced, expected value but got " + b);
                    }
                    Value result = this.device.receive(r);
                    if (result == null) {
                        result = Value.Void;
                    }
                    this.rpcQueue.put(result);
                    break;

                case Protocol.BIN_LOG:
                    this.device.fireDataLogged(r.readUTF());
                    break;

                case Protocol.BIN_VERSION:
                    String clientInterfaceVersion = r.readUTF();
                    if (!clientInterfaceVersion.equals(Device.VERSION)) {
                        System.err.println("WARNING: Client Interface Mismatch");
                    }
                    break;

                default:
                    throw new IOException(
                            "Unexpected binary protocol message from client: " + msg);
            }
        }
    }

    protected abstract Connection createConnection(TargetSelector selector)
            throws IOException;

    private String connect(TargetSelector selector)
            throws IOException {

        this.setState(ConnectionState.CONNECTING);

        this.client = this.createConnection(selector);
        if (this.client != null) {
            int type = this.client.getProtocol();
            String protocol = null;
            if ((type == Protocol.XML) || (type == Protocol.RAW_XML)) {
                this.connectXML(type == Protocol.XML);
                protocol = "XML";
            } else if (type == Protocol.BINARY) {
                this.connectBinary();
                protocol = "BIN";
            } else {
                throw new IllegalArgumentException("Unknown protocol type.");
            }

            this.setState(ConnectionState.CONNECTED);
            return protocol;
        } else {
            return null;
        }
    }

    public String getTarget() {

        if (this.client != null) {
            return this.client.toString();
        } else {
            return null;
        }
    }

    private void connectXML(boolean writeType)
            throws IOException {

        if (writeType) {
            new DataOutputStream(this.client.getOutputStream())
                    .writeInt(Protocol.XML);
        }

        this.xml_out
                = new XMLWriter(new BufferedOutputStream(this.client.getOutputStream()));
        this.xml_out.openElement(Protocol.XML_MAIN);
        this.xml_out.printElement(Protocol.XML_VERSION, Device.VERSION);
        this.xml_out.flush();
    }

    private void connectBinary()
            throws IOException {

        new DataOutputStream(this.client.getOutputStream())
                .writeInt(Protocol.BINARY);

        this.bin_out
                = new DataOutputStream(new BufferedOutputStream(this.client
                        .getOutputStream()));
        this.bin_out.writeByte(Protocol.BIN_VERSION);
        this.bin_out.writeUTF(Device.VERSION);
        this.bin_out.flush();
    }

    public final String open(long timeout, final TargetSelector selector)
            throws IOException {

        String protocol = this.connect(selector);
        // System.out.println(protocol);
        if (this.client != null) {
            this.inputQueue = new InputQueue<Object>();
            new Thread(new Runnable() {

                public void run() {

                    try {
                        CLTConnector.this.fillInputQueue(CLTConnector.this.client
                                .getInputStream());
                    } catch (Protocol.Exception exn) {
                        exn.printStackTrace();
                    } catch (Exception exn) {
                    }

                    // Release echo lock, in case it was left after a crash
                    CLTConnector.this.rpcQueue.interrupt();
                    synchronized (CLTConnector.this.echoLock) {
                        CLTConnector.this.echoLock.notifyAll();
                    }
                }
            }, "InputQueue " + CLTConnector.this.getInfo()).start();
        }
        return protocol;
    }

    public void close() {

        this.rpcQueue.interrupt();

        if (this.xml_out != null) {
            try {
                this.xml_out.closeElement(Protocol.XML_MAIN);
                this.xml_out.println();
                this.xml_out.flush();
            } catch (Exception ignore) {
            }
        }

        try {
            this.client.close();
        } catch (Exception ignore) {
        }
        try {
            this.xml_out.close();
        } catch (Exception ignore) {
        }
        try {
            this.bin_out.close();
        } catch (Exception ignore) {
        }
        try {
            this.in.close();
        } catch (Exception ignore) {
        }

        if (this.inputQueue != null) {
            this.inputQueue.dispose();
            this.inputQueue = null;
        }

        synchronized (this.echoLock) {
            this.echoLock.notifyAll();
        }

        this.client = null;
        this.xml_out = null;
        this.bin_out = null;
        this.in = null;

        this.setState(ConnectionState.DISCONNECTED);
    }

    private class DeviceDelegate
            extends Device {

        @Override
        protected void error(Throwable exn) {

            CLTConnector.this.error(exn);
        }
    }

    protected static class SocketConnection
            implements Connection {

        private Socket socket;
        private int protocol;

        public SocketConnection(Socket socket, int protocol) {

            this.socket = socket;
            this.protocol = protocol;
        }

        public void close()
                throws IOException {

            this.socket.close();
        }

        public InputStream getInputStream()
                throws IOException {

            return this.socket.getInputStream();
        }

        public OutputStream getOutputStream()
                throws IOException {

            return this.socket.getOutputStream();
        }

        public InetAddress getTargetAddress() {

            return this.socket.getInetAddress();
        }

        public int getPort() {

            return this.socket.getPort();
        }

        public int getProtocol() {

            return this.protocol;
        }

        @Override
        public String toString() {

            return this.getTargetAddress().getHostName() + ":" + this.getPort();
        }
    }
}
