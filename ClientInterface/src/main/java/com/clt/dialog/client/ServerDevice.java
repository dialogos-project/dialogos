/*
 * @(#)ServerDevice.java
 * Created on Fri Oct 17 2003
 *
 * Copyright (c) 2003 CLT Sprachtechnologie GmbH.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CLT Sprachtechnologie GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CLT Sprachtechnologie GmbH.
 */

package com.clt.dialog.client;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.lang.reflect.InvocationTargetException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.ListModel;

import com.clt.event.ProgressEvent;
import com.clt.event.ProgressListener;
import com.clt.script.exp.Value;
import com.clt.util.DefaultLongAction;

/**
 * @author Daniel Bobbert
 * @version 6.5
 */

public abstract class ServerDevice {

  private final Object connectLock = new Object();

  private Connector connector;
  private String name;
  private Icon icon;
  private Collection<DeviceListener> deviceListeners =
    new ArrayList<DeviceListener>();

  private AtomicBoolean closed = new AtomicBoolean(true);

  private final DeviceListener connectorListener = new DeviceListener() {

    public void stateChanged(DeviceEvent evt) {

      Device.debug("Device state changed: " + evt.getState());

      evt = new DeviceEvent(ServerDevice.this, evt.getState(), evt.getData());
      for (DeviceListener l : ServerDevice.this.deviceListeners) {
        l.stateChanged(evt);
      }
    }


    public void dataSent(DeviceEvent evt) {

      evt = new DeviceEvent(ServerDevice.this, evt.getState(), evt.getData());
      for (DeviceListener l : ServerDevice.this.deviceListeners) {
        l.dataSent(evt);
      }
    }


    public void dataReceived(DeviceEvent evt) {

      evt = new DeviceEvent(ServerDevice.this, evt.getState(), evt.getData());
      for (DeviceListener l : ServerDevice.this.deviceListeners) {
        l.dataReceived(evt);
      }
    }


    public void dataLogged(DeviceEvent evt) {

      evt = new DeviceEvent(ServerDevice.this, evt.getState(), evt.getData());
      for (DeviceListener l : ServerDevice.this.deviceListeners) {
        l.dataLogged(evt);
      }
    }
  };


  protected ServerDevice(String name, Connector connector) {

    this.name = name;
    this.icon = null;
    this.setConnector(connector);
  }


  public String getName() {

    return this.name;
  }


  public void setName(String name) {

    this.name = name;
  }


  public Icon getIconData() {

    return this.icon;
  }


  public void setIconData(Icon icon) {

    this.icon = icon;
  }


  public void setConnector(Connector connector) {

    if (this.connector != null) {
      this.connector.removeDeviceListener(this.connectorListener);
      try {
        this.connector.close();
      } catch (Exception ignore) {
      }
    }
    this.connector = connector;
    if (this.connector != null) {
      this.connector.addDeviceListener(this.connectorListener);
    }
  }


  public Connector getConnector() {

    return this.connector;
  }


  public void addDeviceListener(DeviceListener l) {

    this.deviceListeners.add(l);
  }


  public void removeDeviceListener(DeviceListener l) {

    this.deviceListeners.remove(l);
  }


  public InetAddress getTargetAddress() {

    if (this.connector == null) {
      return null;
    }
    else {
      return this.connector.getTargetAddress();
    }
  }


  public int getPort() {

    if (this.connector == null) {
      return 0;
    }
    else {
      return this.connector.getPort();
    }
  }


  public ConnectionState getState() {

    if (this.connector == null) {
      return ConnectionState.DISCONNECTED;
    }
    else {
      return this.connector.getState();
    }
  }


  public synchronized void start()
      throws IOException {

    Device.debug("Sending start signal");

    if (this.connector == null) {
      throw new IOException("No connector installed on device " + this);
    }
    this.connector.start();
  }


  public synchronized void reset()
      throws IOException {

    Device.debug("Sending reset signal");

    if (this.connector == null) {
      throw new IOException("No connector installed on device " + this);
    }
    this.connector.reset();
  }


  public synchronized void send(Value value)
      throws IOException {

    Device.debug("Sending " + value);

    if (this.connector == null) {
      throw new IOException("No connector installed on device " + this);
    }
    this.connector.send(value);
  }


  public synchronized Value rpc(String procedure, Value[] arguments)
      throws IOException {

    Device.debug("Sending rpc: " + procedure);
    Value result = this.connector.rpc(procedure, arguments);
    Device.debug("Received rpc result: " + result);
    return result;
  }


  public synchronized void allowTimeout(boolean allowTimeout)
      throws IOException {

    Device.debug("Sending permission to generate timeout: " + allowTimeout);
    this.connector.allowTimeout(allowTimeout);
  }


  public synchronized void signalTimeout() {

    Device.debug("Sending signal that a timeout was detected");
    try {
      this.connector.signalTimeout();
    } catch (Exception ignore) {
    }
  }


  public Value receive()
      throws IOException, InterruptedException {

    synchronized (this.connectLock) {
      while (this.getState() != ConnectionState.CONNECTED) {
        if (this.closed.get()) {
          throw new InterruptedException();
        }
        else {
          this.connectLock.wait();
        }
      }
    }

    try {
      Value result = this.connector.receive();
      Device.debug("Got input from client: " + result);
      return result;
    } catch (InterruptedIOException exn) {
      Device.debug("Received timeout from client.");
      throw exn;
    }
  }


  public void echo()
      throws IOException {

    Device.debug("Sending echo request");

    if (this.connector == null) {
      throw new IOException("No connector installed on device " + this);
    }
    this.connector.echo();
    Device.debug("Got echo");
  }


  public void pushInput(Value value)
      throws IOException {

    if (this.connector == null) {
      throw new IOException("No connector installed on device " + this);
    }
    this.connector.pushInput(value);
  }


  public String open(int timeout)
      throws IOException {

    return this.open(timeout, null);
  }


  /*
   * public String open(final int timeout, final TargetSelector selector) throws
   * IOException { return connector.open(timeout, selector); }
   */

  public final String open(final int timeout, final TargetSelector selector)
      throws IOException {

    if (this.connector == null) {
      throw new IOException("No connector installed on device " + this);
    }

    final StringBuilder protocol_name = new StringBuilder();

    if (!this.closed.compareAndSet(true, false)) {
      throw new IOException("Device is already open");
    }

    Thread t = new Thread(new Runnable() {

      public void run() {

        while (!ServerDevice.this.closed.get()) {
          String protocol = null;
          try {
            try {
              // System.out.println("Connecting device " + ServerDevice.this);
              protocol = ServerDevice.this.connector.open(timeout, selector);
              if ((protocol != null) && (protocol_name.length() == 0)) {
                protocol_name.append(protocol);
              }
            }
                        catch (ConnectException ignore) {
                        }
                        catch (Exception exn) {
                          exn.printStackTrace();
                        }
                        finally {
                          synchronized (ServerDevice.this.connectLock) {
                            ServerDevice.this.connectLock.notifyAll();
                          }
                        }
                      }
                    catch (Exception exn) {
                      // connection closed. We will retry in a second.
                      if (Device.DEBUG_INTERFACE) {
                        exn.printStackTrace();
                      }
                    }

                    try {
                      // wait while we are connected
                      synchronized (ServerDevice.this.connectLock) {
                        while (ServerDevice.this.getState() == ConnectionState.CONNECTED) {
                          ServerDevice.this.connectLock.wait();
                        }
                      }

                      // if disconnected (which may be immediately,
                      // if connection failed), take a break
                      // before attempting reconnection
                      Thread.sleep(1000);
                    }
                    catch (Exception ignore) {
                    }
                  }
                }
    }, this.connector.getDescription());
    t.setPriority(Thread.MIN_PRIORITY);

    try {
      synchronized (this.connectLock) {
        t.start();
        this.connectLock.wait();
      }
    } catch (InterruptedException iexn) {
      this.close();
      throw new InterruptedIOException(iexn.getLocalizedMessage());
    }

    return protocol_name.toString();
  }


  public void close()
      throws IOException {

    this.closed.set(true);

    if (this.connector != null) {
      this.connector.close();
    }

    synchronized (this.connectLock) {
      this.connectLock.notifyAll();
    }
  }


  public static ListModel getAllClients()
      throws IOException {

    return Rendezvous.getAllClients();
  }


  public static boolean connect(final ServerDevice[] devices,
      final int timeout,
            final String title, final ConnectionChooser chooser) {

    chooser.init(devices);

    final Map<ServerDevice, Object[]> ambiguities =
      new HashMap<ServerDevice, Object[]>();
    final Map<ServerDevice, Object> defaults =
      new HashMap<ServerDevice, Object>();
    final Collection<Thread> deviceThreads =
      new ArrayList<Thread>(devices.length);
    for (int i = 0; i < devices.length; i++) {
      final ServerDevice d = devices[i];
      final DeviceListener dl = new DeviceListener() {

        public void stateChanged(DeviceEvent evt) {

          chooser.stateChanged(d);
        }


        public void dataSent(DeviceEvent evt) {

        }


        public void dataReceived(DeviceEvent evt) {

        }


        public void dataLogged(DeviceEvent evt) {

        }
      };
      Thread t = new Thread(new Runnable() {

        public void run() {

          d.addDeviceListener(dl);

          chooser.stateChanged(d);
          // TargetSelector selector = chooser.createTargetSelector(d,
          // ambiguities);
          TargetSelector selector = new TargetSelector() {

            Object preferredClient = null;


            @SuppressWarnings("unchecked")
            public <T> T choose(T[] options, T defaultOption)
                        {

                          if ((this.preferredClient == null)
                                    || !Arrays.asList(options).contains(
                                      this.preferredClient))
                            {
                              ambiguities.put(d, options);
                              defaults.put(d, defaultOption);
                              try {
                                chooser.ambiguityDetected(d, ambiguities);
                                synchronized (ambiguities) {
                                  ambiguities.wait();
                                }
                                this.preferredClient = ambiguities.get(d)[0];
                              }
                                catch (InterruptedException exn) {
                                }
                              }

                              return (T)this.preferredClient;
                            }
          };

          try {
            String protocol = d.open(timeout, selector);
            chooser.protocolChanged(d, protocol);
            // System.out.println(d + " is using protocol " + protocol);
          }
                    catch (Exception exn) {
                      // System.err.println(exn);
                    }
                    d.removeDeviceListener(dl);
                  }
      }, "Connecting Dialogue Clients");
      deviceThreads.add(t);
      t.start();
    }

    try {
      chooser.start(new DefaultLongAction(title) {

        boolean done = false;


        @Override
        public void run(final ProgressListener l) {

          Thread t = new Thread(new Runnable() {

            private final int interval = 1000;
            int i;


            public void run() {

              ProgressEvent e =
                new ProgressEvent(this, getDescription() + "...", 0,
                                timeout, 0);
              l.progressChanged(e);
              this.i = 0;
              try {
                do {
                  Thread.sleep(this.interval);
                  this.i += this.interval;
                  if (!done) {
                    e.setCurrent(this.i);
                    l.progressChanged(e);
                  }
                } while (!done);

                Thread.sleep(250);
              }
                            catch (InterruptedException exn) {
                            }
                          }
          }, "Waiting for Client Connection");
          t.start();

          for (Thread thread : deviceThreads) {
            try {
              thread.join(timeout);
            }
                        catch (InterruptedException exn) {
                        }
                      }

                      this.done = true;
                      try {
                        t.join();
                      }
                    catch (InterruptedException exn) {
                    }
                  }
      });
    } catch (InvocationTargetException bodyDoesntThrow) {
    }

    boolean result;
    if (ambiguities.size() > 0) {
      result = chooser.resolve(ambiguities, defaults);
    }
    else {
      result = true;
    }

    synchronized (ambiguities) {
      ambiguities.notifyAll();
    }

    return result;
  }

  public static class Icon {

    private int[] data;
    private int width;


    public Icon(int[] data, int width) {

      this.data = data;
      this.width = width;
    }


    public int[] getData() {

      return this.data;
    }


    public int getHeight() {

      return this.data.length / this.width;
    }


    public int getWidth() {

      return this.width;
    }

  }
}
