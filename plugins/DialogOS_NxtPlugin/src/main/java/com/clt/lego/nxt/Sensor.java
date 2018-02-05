package com.clt.lego.nxt;

import java.io.IOException;

/**
 * @author dabo
 *
 */
public class Sensor {

    public enum Port {
        S1(0, "1"),
        S2(1, "2"),
        S3(2, "3"),
        S4(3, "4");

        private int id;
        private String name;

        private Port(int id, String name) {

            this.id = id;
            this.name = name;
        }

        public int getID() {

            return this.id;
        }

        @Override
        public String toString() {

            return Resources.getString("Sensor") + " " + this.name;
        }
    }

    public enum Mode {
        /**
         * Value in 0..1023.
         */
        RAW(0x00),
        
        /**
         * Either 0 or 1.
         */
        BOOLEAN(0x20),
        
        /**
         * Number of boolean transitions.
         */
        EDGE(0x40),
        
        /**
         * Number of boolean transitions divided by two.
         */
        PULSE(0x60),
        
        /**
         * Raw value scaled to 0..100.
         */
        PERCENTAGE(0x80),
        
        /**
         * 1/10ths of a degree, -19,8..69,5.
         */
        CELSIUS(0xA0),
        
        /**
         * 1/10ths of a degree, -3,6..157,1.
         */
        FAHRENHEIT(0xC0),
        
        /**
         * 1/16ths of a rotation, represented as a signed short.
         */
        ANGLE(0xE0);

        public static final int MODE_MASK = 0xE0;

        private int value;

        private Mode(int value) {

            this.value = value;
        }

        public int getValue() {

            return this.value;
        }

        @Override
        public String toString() {

            return Resources.getString("SENSORMODE_" + this.name());
        }
    }

    public enum Type {
        NONE(0x00),
        SWITCH(0x01),
        RCX_TEMPERATURE(0x02),
        RCX_LIGHT(0x03),
        RCX_ROTATION(0x04),
        LIGHT_ON(0x05),
        LIGHT_OFF(0x06),
        SOUND_DB(0x07),
        SOUND_DBA(0x08),
        CUSTOM(0x09),
        I2C(0x0A),
        I2C_9V(0x0B);

        private int value;

        private Type(int value) {

            this.value = value;
        }

        public int getValue() {

            return this.value;
        }

        @Override
        public String toString() {

            return Resources.getString("SENSORTYPE_" + this.name());
        }
    }

    private Nxt brick;
    private Port port;
    private Type type;
    private Mode mode;

    public Sensor(Nxt brick, Port port) {

        if (brick == null) {
            throw new IllegalArgumentException();
        }
        if (port == null) {
            throw new IllegalArgumentException();
        }
        this.brick = brick;
        this.port = port;
        this.type = null;
        this.mode = null;
    }

    public Port getPort() {

        return this.port;
    }

    public void setType(Type type, Mode mode)
            throws IOException {

        this.brick.setSensorType(this.port.id, type, mode, 0);
        this.type = type;
        this.mode = mode;
    }

    public int getValue()
            throws IOException {

        Type type = this.getType();
        if ((type == Type.I2C) || (type == Type.I2C_9V)) {
            return this.getI2CValue();
        } else {
            return this.brick.getSensorValue(this.port.id);
        }
    }

    public int getRawValue()
            throws IOException {

        Type type = this.getType();
        if ((type == Type.I2C) || (type == Type.I2C_9V)) {
            return this.getI2CValue();
        } else {
            return this.brick.getSensorRawValue(this.port.id);
        }
    }

    private int getI2CValue()
            throws IOException {

        // initialize for one shot reading
        this.brick.lsRead(this.port.id, new byte[]{0x02, 0x41, 0x01}, 0);

        // this sleeping time is needed for the ultra-sonic sensor.
        try {
            Thread.sleep(40);
        } catch (InterruptedException exn) {
            exn.printStackTrace();
        }
        byte[] answer
                = this.brick.lsRead(this.port.id, new byte[]{0x02, 0x42}, 1);
        return answer[Math.min(4, answer.length - 1)] & 0xFF;
    }

    public Type getType()
            throws IOException {

        if (this.type != null) {
            return this.type;
        } else {
            return this.brick.getSensorType(this.port.id);
        }
    }

    public Mode getMode()
            throws IOException {

        if (this.mode != null) {
            return this.mode;
        } else {
            return this.brick.getSensorMode(this.port.id);
        }
    }
}
