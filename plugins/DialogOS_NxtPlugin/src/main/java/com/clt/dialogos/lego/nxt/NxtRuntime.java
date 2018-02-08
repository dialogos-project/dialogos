package com.clt.dialogos.lego.nxt;

import java.io.IOException;
import java.util.Map;

import com.clt.dialogos.plugin.PluginRuntime;
import com.clt.lego.nxt.Nxt;
import com.clt.lego.nxt.Sensor;

/**
 * @author dabo
 *
 */
public class NxtRuntime implements PluginRuntime {

    private Nxt brick;
    private Map<Sensor.Port, SensorType> sensorTypes;

    public NxtRuntime(Nxt brick, Map<Sensor.Port, SensorType> sensorTypes) {

        this.brick = brick;
        this.sensorTypes = sensorTypes;

        if (brick != null) {
            Thread t = new Thread(new Runnable() {

                public void run() {

                    try {
                        long delay = NxtRuntime.this.keepAlive();
                        while (delay != 0) {
                            // ping the brick 10s before it wants to sleep
                            if (delay < 0) {
                                Thread.sleep(10000);
                            } else {
                                Thread.sleep(delay - 10000);
                            }
                            delay = NxtRuntime.this.keepAlive();
                        }
                    } catch (Exception exn) {
                        // ignore
                    }
                }
            });
            t.setDaemon(true);
            t.start();
        }
    }

    private synchronized long keepAlive()
            throws IOException {

        if (this.brick == null) {
            return 0;
        } else {
            return this.brick.keepAlive();
        }
    }

    public synchronized void dispose() {

        if (this.brick != null) {
            try {
                this.brick.close();
            } catch (Exception exn) {
                // ignore
            }
            this.brick = null;
        }
    }

    public Nxt getBrick() {

        return this.brick;
    }

    public SensorType getSensorType(Sensor.Port port) {

        return this.sensorTypes.get(port);
    }
}
