package com.clt.mac;

import java.io.File;

import com.apple.mrj.MRJAboutHandler;
import com.apple.mrj.MRJApplicationUtils;
import com.apple.mrj.MRJOpenDocumentHandler;
import com.apple.mrj.MRJPrintDocumentHandler;
import com.apple.mrj.MRJQuitHandler;

class MRJHandler implements SystemEventAdapter {

    private static boolean osx;

    private static boolean quitFlag = false;
    private static Object quitLock = new Object();

    static {
        String os = System.getProperty("os.name");
        MRJHandler.osx = os.indexOf("X") >= 0;
    }

    @SuppressWarnings("deprecation")
    public void register(final RequiredEventHandler handler) {

        MRJApplicationUtils.registerQuitHandler(new MRJQuitHandler() {

            public void handleQuit() {

                if (MRJHandler.osx) {
                    synchronized (MRJHandler.quitLock) {
                        if (MRJHandler.quitFlag) {
                            return;
                        } else {
                            MRJHandler.quitFlag = true;
                        }
                    }
                    new Thread("com.clt.mac.MRJHandler Quit Handler") {

                        @Override
                        public void run() {

                            handler.handleQuit();
                            synchronized (MRJHandler.quitLock) {
                                MRJHandler.quitFlag = false;
                            }
                        }
                    }.start();
                } else {
                    handler.handleQuit();
                }
            }
        });

        if (handler.insertAboutItem) {
            MRJApplicationUtils.registerAboutHandler(new MRJAboutHandler() {

                public void handleAbout() {
                    handler.handleAbout();
                }
            });
        }

        MRJApplicationUtils
                .registerOpenDocumentHandler(new MRJOpenDocumentHandler() {

                    public void handleOpenFile(File file) {

                        handler.handleOpenFile(file);
                    }
                });

        MRJApplicationUtils
                .registerPrintDocumentHandler(new MRJPrintDocumentHandler() {

                    public void handlePrintFile(File file) {

                        handler.handlePrintFile(file);
                    }
                });
    }

}
