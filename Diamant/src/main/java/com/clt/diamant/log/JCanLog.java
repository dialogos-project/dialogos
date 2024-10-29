package com.clt.diamant.log;

import java.io.File;

import com.clt.diamant.WozInterface;

public class JCanLog {

    private static boolean libraryLoaded = false;

    private File file = new File("CAN.log");
    private int delay = 1000;
    private boolean quit = false;
    private boolean mark = false;
    private WozInterface.State state = WozInterface.State.NORMAL;

    public JCanLog() throws InstantiationException {

        try {
            if (!JCanLog.libraryLoaded) {
                System.loadLibrary("JCanLog");
                JCanLog.libraryLoaded = true;
            }
        } catch (Throwable t) {
            throw new InstantiationException(t.getLocalizedMessage());
        }
    }

    public void setFile(File file) {

        this.file = file;
    }

    public void setDelay(int delay) {

        this.delay = delay;
    }

    public void setState(WozInterface.State state) {

        this.state = state;
    }

    public void mark() {

        this.mark = true;
    }

    public void start() {

        if (!JCanLog.libraryLoaded) {
            return;
        }

        this.init(this.file.getAbsolutePath());
        this.quit = false;

        new Thread(new Runnable() {

            public void run() {

                long time = System.currentTimeMillis();
                while (!JCanLog.this.quit) {
                    JCanLog.this.sample(JCanLog.this.mark ? -1 : JCanLog.this.state
                            .ordinal());
                    JCanLog.this.mark = false;
                    try {
                        Thread.sleep(JCanLog.this.delay
                                - (System.currentTimeMillis() - time));
                    } catch (InterruptedException exn) {
                    }
                    time = System.currentTimeMillis();
                }
                JCanLog.this.close();
            }
        }, "CAN sampler").start();
    }

    public void stop() {

        this.quit = true;
    }

    private native void init(String file);

    private native void close();

    private native void sample(int state);

}
