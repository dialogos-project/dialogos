package com.clt.dialog.client;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;

import com.clt.script.exp.EvaluationException;
import com.clt.script.exp.Value;

/**
 * The base class for clients. Clients are standalone programs which can attach
 * to DialogOS as "devices". Clients receive data from DialogOS
 * <a href="https://github.com/dialogos-project/dialogos/wiki/Nodes#output-node">Output
 * nodes</a>, and can send data to DialogOS, to be received by
 * <a href="https://github.com/dialogos-project/dialogos/wiki/Nodes#input-1">Input
 * nodes</a>.<p>
 *
 * Implementing a client requires three steps:
 * <ol>
 * <li>Create a new class that inherits from <code>Client</code>.
 * <li>Override a number of callbacks to implement client behavior.
 * <li>Call <code>open</code> on the client class to activate it.
 * </ol><p>
 *
 * Clients must implement the abstract callback methods {@link #getName},
 * {@link #stateChanged}, {@link #sessionStarted}, {@link #reset},
 * {@link #error}, and {@link #output}. They may additionally overwrite the
 * methods {@link #waitUntilDone} and {@link #rpc} (to provide remote procedures)
 * .<p>
 *
 * By implementing {@link #getName}, you provide a name for the client. This
 * name will be used by DialogOS to find the client, i.e. the name must be
 * entered in the <code>Service Name</code> field in the Rendezvous connector
 * under Dialog / Devices. Alternatively, DialogOS can use the "Fixed Server"
 * connector with the port number that the client passed to {@link #open(int) }
 * .<p>
 *
 * Clients can send data back to DialogOS. This data may include results of a
 * speech recognizer, sensor input etc. Simply call {@link #send(java.lang.Object)
 * } to send data to DialogOS. DialogOS will internally queue and filter
 * your data, so you can call <code>send</code> at any time.<p>
 *
 * "Output" messages that are sent to the client are internally queued, and
 * callbacks to {@link #output(com.clt.script.exp.Value) } are handled
 * synchronously. This means that "output" will only get the next output message
 * after the previous call to "output" returns. If you want to trigger a long
 * lasting action and process further output messages at the same time, you have
 * to start a new thread from your handler.
 * <strong>NOTE:</strong> This is only true for the <code>output</code> handler.
 * The <code>reset()</code>, <code>sessionStarted()</code>,
 * <code>stateChanged()</code> and <code>rpc()</code> callbacks are always
 * called immediately, even if your <code>output</code> handler is running. This
 * means on the other hand, you need to take care of synchronization between
 * <code>rpc()</code> and <code>output()</code> yourself, if they share any
 * data.<p>
 *
 * Example: A sample player that receives names of files to play via
 * <code>output</code> should start playing the file and only return when the
 * sample has finished playing or your <code>reset</code> callback is called (in
 * this case you should abort the currently playing sample).
 * <p>
 *
 * <code>waitUntilDone</code> is called when DialogOS wants to make sure that
 * the client has finished processing all pending output events. If you do not
 * start any new threads from your <code>output</code> callback you do not have
 * to implement <code>waitUntilDone</code>. But if you have started new threads,
 * you should wait and only return from <code>waitUntilDone</code> when all
 * these threads have finished execution.<p>
 *
 * For more information, see the
 * <a href="https://github.com/dialogos-project/dialogos/wiki/Clients">wiki page
 * on implementing clients</a>.
 *
 * @author Daniel Bobbert
 * @version 7.0
 */
public abstract class Client {

    private int priority = Thread.NORM_PRIORITY;
    private ClientDevice device;
    private Object lock = new Object();

    /**
     * Create a new client. The client does nothing until you call its
     * <code>open</code> method.
     */
    public Client() {

        this.device = null;
    }

    /**
     * Notification that the client has changed its state. For instance, this
     * method is called whenever DialogOS connects or disconnects from the
     * client.
     *
     * @param state the new client state
     *
     * @see ConnectionState
     */
    public abstract void stateChanged(ConnectionState state);

    /**
     * Notification that the dialog engine is up and running. The client should
     * setup its internal state for the beginning of a new dialog session.<p>
     *
     * This callback should not be used for initialization actions that takes
     * more than a second because DialogOS expects the client to be active
     * immediately.
     */
    public abstract void sessionStarted();

    /**
     * Notification that the client should reset its internal state. The client
     * should stop any pending output. This message is sent when the "Send
     * reset" option is enabled for an OutputNode. The intent is to clear any
     * pending output before producing a new output.
     */
    public abstract void reset();

    /**
     * Notification that the client should output some data. This is an instance
     * of {@link Value}. If you start a new thread from <code>output</code> you
     * need to override <code>waitUntilDone</code>.
     *
     * @param value the data to output
     *
     * @see #waitUntilDone
     */
    public abstract void output(Value value);

    /**
     * Request that the client should executed the given procedure. Clients may
     * override this method to provide their own procedures. If you can't
     * resolve a procedure, because the name is unknown or the number or type of
     * arguments do not match, you should call
     * <code>super.rpc(procedure, arguments)</code>. If you throw an exception
     * while handling the call, you should provide a descriptive message of the
     * cause of the error with your exception.
     *
     * @param procedure the name of the procedure to call
     * @param arguments the (possibly empty) array of arguments
     *
     * @return The result of executing the given procedure with the given
     * arguments. You may return <code>null</code> or
     * {@link com.clt.script.exp.values.Undefined}, if your procedure has no
     * sensible return value.
     */
    public Value rpc(String procedure, Value[] arguments) throws Exception {
        throw new EvaluationException("Unknown procedure \"" + procedure + "\".");
    }

    /**
     * Notification whether the client is allowed to send timeout signals. You
     * should only call {@link #sendTimeout()} after you received this
     * notification with an argument value of <code>true</code> and only until
     * you receive this message with a value of <code>true</code>.
     */
    public void allowTimeout(boolean allowTimeout) {

    }

    /**
     * Notification that the dialog manager has a timeout. The dialog is in
     * debugging mode, so the timeout is not yet executed.
     */
    public void signalTimeout() {

    }

    /**
     * Send a timeout signal to the dialog manager. You should do so only, if
     * you received a message, that timeouts are allowed.
     *
     * @see #allowTimeout
     * @throws IOException
     */
    public void sendTimeout() throws IOException {

        synchronized (this.lock) {
            if (this.device == null) {
                throw new SocketException("Client is not connected. Call open() first.");
            }

            this.device.sendTimeout();
        }
    }

    /**
     * Returns a human-readable name for this client.
     */
    public abstract String getName();

    /**
     * Notification that DialogOS is waiting for the client to finish all
     * output. The default implementation assumes that you haven't started any
     * new threads from your <code>output</code> handler so it simply returns.
     * If you start your own threads from <code>output</code> you need to
     * override <code>waitUntilDone</code> and make sure that your threads have
     * finished before <code>waitUntilDone</code> returns.
     */
    public void waitUntilDone() {

    }

    /**
     * Notification that an internal error has occurred while receiving data
     * from the client.
     */
    public abstract void error(Throwable t);

    /**
     * Get the current connection state.
     *
     * @see ConnectionState
     */
    public ConnectionState getState() {

        if (this.device == null) {
            return ConnectionState.DISCONNECTED;
        } else {
            return this.device.getState();
        }
    }

    /**
     * Return the port that this client is currently listening on.
     *
     * @return The port or <code>0</code> if the client is disconnected
     */
    public int getPort() {

        if (this.device == null) {
            return 0;
        } else {
            return this.device.getPort();
        }
    }

    /**
     * Send data to DialogOS.<p>
     *
     * This should be an instance of {@link com.clt.script.exp.Value}.
     * <code>Client</code> tries to automatically map instances of other classes
     * to appropriate subclasses of {@link com.clt.script.exp.Value}:
     * <table summary="The class mapping">
     * <tr>
     * <td valign=top>java.lang.Object[]</td>
     * <td>{@link com.clt.script.exp.values.ListValue}</td>
     * </tr>
     * <tr>
     * <td valign=top>java.util.Collection</td>
     * <td>{@link com.clt.script.exp.values.ListValue}</td>
     * </tr>
     * <tr>
     * <td valign=top>java.util.Iterator</td>
     * <td>{@link com.clt.script.exp.values.ListValue}</td>
     * </tr>
     * <tr>
     * <td valign=top>java.util.Enumeration</td>
     * <td>{@link com.clt.script.exp.values.ListValue}</td>
     * </tr>
     * <tr>
     * <td valign=top>java.util.Map</td>
     * <td>{@link com.clt.script.exp.values.StructValue}<br>
     * The map's keys are converted to Strings using
     * <code>toString()</code></td>
     * </tr>
     * </table>
     * If no appropriate Value subclass exists, <code>data.toString()</code> is
     * used to convert the object to a
     * {@link com.clt.script.exp.values.StringValue}. These transformations are
     * applied recursively, so a <code>java.util.Map[]</code> will be converted
     * to a {@link com.clt.script.exp.values.ListValue} containing
     * {@link com.clt.script.exp.values.StructValue}s whose labels are the keys
     * of the respective Map (converted to String) and whose values are the
     * converted values of the Map.
     *
     * @param data the data to send
     * @exception IOException if writing to the socket failed
     */
    public final void send(Object data) throws IOException {
        this.sendImpl(data);
    }

    void sendImpl(Object data) throws IOException {
        synchronized (this.lock) {
            if (this.device == null) {
                throw new SocketException("Client is not connected. Call open() first.");
            }

            this.device.send(data);
        }
    }

    /**
     * Send a log message to DialogOS.
     */
    public void log(String message) throws IOException {
        synchronized (this.lock) {
            if (this.device == null) {
                throw new SocketException("Client is not connected. Call open() first.");
            }

            this.device.log(message);
        }
    }

    /**
     * Start the client using a randomly chosen free port. The client will
     * advertise its service to any DialogOS on the local network using the name
     * that is returned by {@link #getName}. When a connection is lost or
     * terminated the client will immediately try to reopen it. The client will
     * select a free port automatically.
     *
     * @exception IOException If starting up the network interface failed.
     *
     * @see #close
     */
    public void open() throws IOException {
        this.open(ClientDevice.DYNAMIC_PORT_SELECTION);
    }

    /**
     * Start the client on the given port. The client will advertise its service
     * to any DialogOS on the local network using the name that is returned by
     * {@link #getName}. When a connection is lost or terminated the client will
     * immediately try to reopen it. The client will use the given port.
     *
     * @exception IOException If starting up the network interface failed.
     *
     * @see #close
     */
    public void open(int port) throws IOException {

        synchronized (this.lock) {
            if (this.device != null) {
                throw new ConnectException("Client is already connected.");
            }

            try {
                this.device = new ClientDevice(this);
                this.device.setPriority(this.priority);
                this.device.activate(port, null, this.useRendezvous(), !this.isPublic());
            } catch (IOException exn) {
                this.close();
                throw exn;
            }
        }
    }

    /**
     * Start the client. This method is only provided for compatibility
     * purposes. Please use {@link #open()} or {@link #open(int)} instead.
     *
     * @deprecated The server argument is no longer used. Use {@link #open()} or
     * {@link #open(int)} instead
     * @see #close
     * @see #open()
     * @see #open(int)
     */
    @Deprecated
    public void open(String server, int port) throws ConnectException {

        try {
            this.open(port);
        } catch (IOException exn) {
            if (exn instanceof ConnectException) {
                throw (ConnectException) exn;
            } else {
                throw new ConnectException(exn.toString());
            }
        }
    }

    /**
     * Stop the client. This will abort any active connection and close the
     * client port. No further connection attempts will be made. You can reopen
     * the client at any time using <code>open</code>.
     *
     * @see #open()
     * @see #open(int)
     */
    public void close() {

        synchronized (this.lock) {
            if (this.device != null) {
                this.device.close();
            }
            this.device = null;
        }
    }

    /**
     * Set the priority of the thread executing this client.
     */
    public void setPriority(int priority) {

        this.priority = priority;
        synchronized (this.lock) {
            if (this.device != null) {
                this.device.setPriority(priority);
            }
        }
    }

    /**
     * Return whether this client should be <em>publicly</em> advertised using
     * Rendezvous. The default returns true. Override to make your client
     * private (i.e. only visible by DialogOS instances running on the same machine). The
     * client will still be accessible to the "Fixed Server" connector.
     *
     * @see #useRendezvous()
     */
    public boolean isPublic() {
        return true;
    }

    /**
     * Return whether this client should be advertised using Rendezvous <em>at
     * all</em>. The default returns true. Override to make your client
     * accessible only for servers that connect directly to the client's port.
     *
     * @see #isPublic()
     */
    public boolean useRendezvous() {

        return true;
    }
}
