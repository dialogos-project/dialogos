package com.clt.lego;

import java.io.IOException;

import com.clt.io.InterfaceType;

/**
 * @author dabo
 *
 */
public interface Brick {

    /**
     * Return the interface type that this brick is connected to
     */
    public InterfaceType getInterfaceType();

    /**
     * Return the port name, that this brick is connected to
     */
    public String getPort();

    /**
     * Return the URI that uniquely identifies this brick
     */
    public String getResourceString() throws IOException;

    /**
     * Return device info about this brick
     */
    public BrickInfo getDeviceInfo() throws IOException;

    /**
     * Keep this brick alive and return the currently configured sleep time in
     * ms. A return value of <code>0</code> means "never". This method will
     * return <code>-1</code> if the brick is executing a program.
   *
     */
    public long keepAlive() throws IOException;

    /**
     * Close the connecion to this brick. Any attempt to call other methods on
     * this brick after the connection has been closed will result in an
     * exception.
   *
     */
    public void close() throws IOException;

    /**
     * Return the current battery level of this brick
     */
    public int getBatteryLevel() throws IOException;

    /**
     * Play a tone with he given frequency in Hz for the given duration in ms.
     * The method will wait and return when the tone has finished playing
     */
    public void playTone(int frequency, int duration) throws IOException;

}
