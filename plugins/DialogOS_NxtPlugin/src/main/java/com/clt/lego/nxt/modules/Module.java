package com.clt.lego.nxt.modules;

import java.io.IOException;

import com.clt.lego.BrickUtils;
import com.clt.lego.nxt.Nxt;

/**
 * @author Daniel Bobbert
 *
 */
public class Module {

    public enum Type {
        Command(0x01),
        Output(0x02),
        Input(0x03),
        Button(0x04),
        Comm(0x05),
        IOCtrl(0x06),
        Led(0x07),
        Sound(0x08),
        Loader(0x09),
        Display(0x0A),
        LowSpeed(0x0B),
        UI(0x0C);

        private int id;

        private Type(int id) {

            this.id = id;
        }
    }

    private Nxt brick;
    private String name;
    private int id;

    protected Module(Nxt brick, String name) throws IOException {

        this.brick = brick;
        this.name = name;
        this.id = brick.getModuleID(name);

        System.out.println("Module " + name + ", version "
                + com.clt.util.StringTools.toHexString(this.getVersion(), 4)
                + " loaded.");
    }

    public Type getType() {

        int type = (this.id >>> 24) & 0xFF;
        for (Type t : Type.values()) {
            if (t.id == type) {
                return t;
            }
        }

        return null;
    }

    public int getVersion() {

        return this.id & 0xFFFF;
    }

    protected byte[] read(int offset, int length)
            throws IOException {

        return this.brick.readIOMap(this.name, offset, length);
    }

    protected long readNum(int offset, int length, boolean bigEndian)
            throws IOException {

        return BrickUtils.readNum(this.read(offset, length), 0, length, bigEndian);
    }

    protected void write(int offset, byte[] data)
            throws IOException {

        this.brick.writeIOMap(this.name, offset, data);
    }
}
