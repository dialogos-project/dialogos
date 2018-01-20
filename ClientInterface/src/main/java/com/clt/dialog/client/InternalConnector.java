package com.clt.dialog.client;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.util.Collection;
import java.util.LinkedList;

import com.clt.properties.DefaultStringProperty;
import com.clt.properties.Property;
import com.clt.properties.StringProperty;
import com.clt.script.exp.Value;

/**
 * @author dabo
 * 
 */
public class InternalConnector extends AbstractConnector {

  private GUIClient client = null;
  private GUIClient.InternalConnection internalConnection = null;
  private Collection<DeviceListener> deviceListeners = new LinkedList<DeviceListener>();
  private InputQueue<Object> inputQueue = null;
  private InputQueue<Runnable> outputQueue = null;
  private final Object echoLock = new Object();

  private StringProperty CLIENT_NAME = new DefaultStringProperty("Client", "Client Name", null);


  public InternalConnector() {

    this("");
  }


  public InternalConnector(String name) {

    this.CLIENT_NAME.setValue(name);
  }


  public InternalConnector copy() {

    return new InternalConnector(this.CLIENT_NAME.getValue());
  }


  public Object getHelp() {

    return new String[] {
                "This connector works with clients using the CLT Client Interface Protocol.",
                "The client must be running within the same virtual machine. The connector "
                        + "will pass values directly to the client without the need for any kind of "
                        + "network communcation." };
  }


  public String getName() {

    return "CLT Connector (Internal)";
  }


  public String getInfo() {

    StringBuilder b = new StringBuilder();
    b.append("Internal: ");

    b.append(this.CLIENT_NAME.getValue());

    return b.toString();

  }


  public Property<?>[] getProperties() {

    return new Property<?>[] { this.CLIENT_NAME };
  }


  public String open(long timeout, TargetSelector selector)
        throws IOException {

    Collection<GUIClient> clients = new LinkedList<GUIClient>();

    for (GUIClient c : GUIClient.getActiveClients()) {
      if (c.getName().equals(this.CLIENT_NAME.getValue())) {
        clients.add(c);
      }
    }

    if (selector == null) {
      if (!clients.isEmpty()) {
        this.client = clients.iterator().next();
      }
    }
    else if (clients.size() == 1) {
      this.client = clients.iterator().next();
    }
    else if (clients.size() > 1) {
      this.client =
        selector.choose(clients.toArray(new GUIClient[clients.size()]), null);
    }
    else {
      this.client = null;
    }

    if (this.client == null) {
      throw new ConnectException("Client " + this.CLIENT_NAME.getValue()
        + " not found");
    }

    this.internalConnection = new GUIClient.InternalConnection() {

      public void send(Object o) {

        InternalConnector.this.inputQueue.put(Value.of(o));
      }


      public void sendTimeout() {

        InternalConnector.this.inputQueue.put(new InterruptedIOException());
      }


      public void log(String message) {

        synchronized (InternalConnector.this.deviceListeners) {
          DeviceEvent evt =
            new DeviceEvent(this, InternalConnector.this.getState(), message);
          for (DeviceListener listener : InternalConnector.this.deviceListeners) {
            listener.dataLogged(evt);
          }
        }
      }


      public void terminate() {

        InternalConnector.this.closedByClient();
      }
    };

    this.inputQueue = new InputQueue<Object>();
    this.outputQueue = new InputQueue<Runnable>();
    this.client.addInternalConnection(this.internalConnection);

    DeviceEvent evt = new DeviceEvent(this, ConnectionState.CONNECTED);
    synchronized (this.deviceListeners) {
      for (DeviceListener listener : this.deviceListeners) {
        listener.stateChanged(evt);
      }
    }

    new Thread(new Runnable() {

      public void run() {

        GUIClient client = InternalConnector.this.client;
        while (client == InternalConnector.this.client) {
          try {
            InternalConnector.this.outputQueue.get().run();
          }
                    catch (InterruptedException exn) {
                      break;
                    }
                    catch (ThreadDeath d) {
                      throw d;
                    }
                    catch (Throwable t) {
                      client.error(t);
                    }
                  }
                }
    }, "Internal Connector Output Queue").start();

    return "INT";
  }


  private void closedByClient() {

    try {
      this.close();
    } catch (Exception ignore) {
    }
  }


  public void close()
      throws IOException {

    if (this.client != null) {
      this.client.removeInternalConnection(this.internalConnection);
      this.internalConnection = null;
      this.client = null;
      this.outputQueue.dispose();
      this.outputQueue = null;
      this.inputQueue.dispose();
      this.inputQueue = null;
    }

    // Release echo lock, in case it was left after a crash
    synchronized (this.echoLock) {
      this.echoLock.notifyAll();
    }

    DeviceEvent evt = new DeviceEvent(this, ConnectionState.DISCONNECTED);
    synchronized (this.deviceListeners) {
      for (DeviceListener listener : this.deviceListeners) {
        listener.stateChanged(evt);
      }
    }
  }


  public InetAddress getTargetAddress() {

    return null;
  }


  public int getPort() {

    return 0;
  }


  public String getTarget() {

    if (this.client != null) {
      return "Internal";
    }
    else {
      return null;
    }
  }


  public ConnectionState getState() {

    if (this.client != null) {
      return ConnectionState.CONNECTED;
    }
    else {
      return ConnectionState.DISCONNECTED;
    }
  }


  public void addDeviceListener(DeviceListener l) {

    synchronized (this.deviceListeners) {
      this.deviceListeners.add(l);
    }
  }


  public void removeDeviceListener(DeviceListener l) {

    synchronized (this.deviceListeners) {
      this.deviceListeners.remove(l);
    }
  }


  public void start()
      throws IOException {

    if (this.client == null) {
      throw new IOException("Not connected");
    }
    else {
      this.client.sessionStarted();
    }
  }


  public void reset()
      throws IOException {

    // release pending echos, because otherwise they will never return after a
    // reset
    synchronized (this.echoLock) {
      this.echoLock.notifyAll();
    }

    if (this.client == null) {
      throw new IOException("Not connected");
    }
    else {
      this.clearActions();
      this.client.reset();
    }
  }


  public void allowTimeout(final boolean allow)
      throws IOException {

    if (this.client == null) {
      throw new IOException("Not connected");
    }
    else {
      this.postAction(new Runnable() {

        public void run() {

          InternalConnector.this.client.allowTimeout(allow);
        }
      });
    }
  }


  public void signalTimeout()
      throws IOException {

    if (this.client == null) {
      throw new IOException("Not connected");
    }
    else {
      this.postAction(new Runnable() {

        public void run() {

          InternalConnector.this.client.signalTimeout();
        }
      });
    }
  }


  public void send(final Value value)
      throws IOException {

    if (this.client == null) {
      throw new IOException("Not connected");
    }
    else {
      this.postAction(new Runnable() {

        public void run() {

          InternalConnector.this.client.output(value);
        }
      });
    }
  }


  public Value rpc(String procedure, Value[] arguments) {

    try {
      return this.client.rpc(procedure, arguments);
    } catch (Exception exn) {
      throw new RuntimeException(exn.getLocalizedMessage());
    }
  }


  public void pushInput(Value value) {

    this.inputQueue.put(value);
  }


  public void echo()
      throws IOException {

    if (this.client == null) {
      throw new IOException("Not connected");
    }
    else {
      synchronized (this.echoLock) {
        this.postAction(new Runnable() {

          public void run() {

            try {
              InternalConnector.this.client.waitUntilDone();
            }
                        finally {
                          synchronized (InternalConnector.this.echoLock) {
                            InternalConnector.this.echoLock.notifyAll();
                          }
                        }
                      }


          @Override
          public String toString() {

            return "waitUntilDone()";
          }
        });
        try {
          this.echoLock.wait();
        } catch (InterruptedException ignore) {
        }
      }
    }
  }


  public Value receive()
      throws IOException, InterruptedException {

    if (this.inputQueue == null) {
      throw new ConnectException(
        "Connector not initialized. You must call open() first.");
    }
    else {
      Object o = this.inputQueue.get();
      if (o instanceof IOException) {
        throw (IOException)o;
      }
      else if (o instanceof InterruptedIOException) {
        throw (InterruptedIOException)o;
      }
      else if (o instanceof Value) {
        return (Value)o;
      }
      else {
        throw new IOException("Unexpected type of input");
      }
    }
  }


  private void postAction(Runnable r) {

    this.outputQueue.put(r);
  }


  private void clearActions() {

    this.outputQueue.clear();
  }
}
