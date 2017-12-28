package com.clt.diamant;

import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.clt.dialog.client.DeviceEvent;
import com.clt.dialog.client.DeviceListener;
import com.clt.dialog.client.InputQueue;
import com.clt.util.Timeout;

public class InputCenter {

  private Map<Device, InputQueue<DialogInput<?>>> inputQueues;
  private InputQueue<DialogInput<Object>> inputQueue;
  private boolean disposed = false;
  private Collection<DeviceListener> deviceListeners =
    new ArrayList<DeviceListener>();


  public InputCenter(Collection<Device> devices) {

    this.inputQueues = new HashMap<Device, InputQueue<DialogInput<?>>>();
    this.inputQueue = new InputQueue<DialogInput<Object>>();

    this.init(devices);
  }


  private void init(Collection<Device> devices) {

    for (final Device d : devices) {
      final InputQueue<DialogInput<?>> q = new InputQueue<DialogInput<?>>();
      this.inputQueues.put(d, q);
      new Thread(new Runnable() {

        public void run() {

          DeviceListener dl = new DeviceListener() {

            public void stateChanged(DeviceEvent evt) {

              synchronized (InputCenter.this.deviceListeners) {
                for (DeviceListener l : InputCenter.this.deviceListeners) {
                  l.stateChanged(evt);
                }
              }
            }


            public void dataSent(DeviceEvent evt) {

              synchronized (InputCenter.this.deviceListeners) {
                for (DeviceListener l : InputCenter.this.deviceListeners) {
                  l.dataSent(evt);
                }
              }
            }


            public void dataReceived(DeviceEvent evt) {

              synchronized (InputCenter.this.deviceListeners) {
                for (DeviceListener l : InputCenter.this.deviceListeners) {
                  l.dataReceived(evt);
                }
              }
            }


            public void dataLogged(DeviceEvent evt) {

              synchronized (InputCenter.this.deviceListeners) {
                for (DeviceListener l : InputCenter.this.deviceListeners) {
                  l.dataLogged(evt);
                }
              }
            }
          };

          d.addDeviceListener(dl);

          while (InputCenter.this.inputQueues.containsKey(d)) {
            try {
              DialogInput<Object> di = new DialogInput<Object>(d, d.receive());
              InputCenter.this.put(di);
            }
                        catch (InterruptedIOException exn) {
                          InputCenter.this.put(new DialogInput<Object>(d,
                            new ForcedTimeout()));
                        }
                        catch (Exception exn) {
                        }
                      }
                      d.removeDeviceListener(dl);
                    }
      }, "InputCenter delegate for " + d.getName()).start();
    }
  }


  public void clear(final Device d) {

    synchronized (this.inputQueue) {
      if (d == null) {
        this.inputQueue.clear();
        for (InputQueue<?> q : this.inputQueues.values()) {
          q.clear();
        }
      }
      else {
        InputQueue<?> q = this.inputQueues.get(d);
        if (q != null) {
          q.clear();
          this.inputQueue.remove(new Object() {

            @Override
            public boolean equals(Object o) {

              if (o instanceof DialogInput) {
                if (((DialogInput)o).getDevice() == d) {
                  return true;
                }
              }
              return false;
            }
          });
        }
      }
    }
  }


  public void dispose() {

    if (!this.disposed) {
      this.disposed = true;
      synchronized (this.inputQueue) {
        this.inputQueue.dispose();
        for (InputQueue<?> q : this.inputQueues.values()) {
          q.dispose();
        }
        this.inputQueues.clear();
      }
    }
  }


  public void put(DialogInput<Object> in) {

    assert (in != null) : "null input Object";

    synchronized (this.inputQueue) {
      InputQueue<DialogInput<?>> q = this.inputQueues.get(in.getDevice());
      if (q != null) {
        q.put(in);
      }
      this.inputQueue.put(in);
    }
  }


  public DialogInput<?> get(Device d, long timeout)
      throws Timeout, InterruptedException {

    synchronized (this.inputQueue) {
      if (d != null) {
        InputQueue<DialogInput<?>> q = this.inputQueues.get(d);
        if (q == null) {
          throw new IllegalArgumentException("Unregistered device: " + d);
        }

        // give away lock if the queue is empty, so that new input
        // can be put
        long time = System.currentTimeMillis();
        while (q.isEmpty()) {
          this.inputQueue.wait(timeout);
          if (this.disposed) {
            throw new InterruptedException();
          }
          if (timeout > 0) {
            timeout -= (System.currentTimeMillis() - time);
            if (timeout <= 0) {
              throw new Timeout();
            }
          }
        }

        // once we get here, there is some input on the queue, so we
        // don't need to care about timeouts
        DialogInput<?> in = q.get();
        this.inputQueue.remove(in);
        return in;
      }
      else {
        DialogInput<?> in = this.inputQueue.get(timeout);
        if (this.disposed) {
          throw new InterruptedException();
        }
        if (in == null) {
          throw new Timeout();
        }

        d = in.getDevice();
        InputQueue<DialogInput<?>> q = this.inputQueues.get(d);
        if (q != null) {
          q.remove(in);
        }
        return in;
      }
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
}