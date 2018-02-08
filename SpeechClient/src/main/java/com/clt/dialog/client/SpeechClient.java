package com.clt.dialog.client;

import com.clt.gui.menus.CmdMenu;
import com.clt.speech.G2P;

/**
 * @author dabo
 *
 */
public abstract class SpeechClient extends GUIClient {

    protected static final int cmdTranscribe = 3079;

    protected abstract G2P getG2P();

    @Override
    protected void initMenu(CmdMenu menu) {

        super.initMenu(menu);

        menu.addItem("Transcribe...", SpeechClient.cmdTranscribe);
    }

    @Override
    public boolean menuItemState(int cmd) {

        switch (cmd) {
            case cmdTranscribe:
                return this.getG2P() != null;
            default:
                return super.menuItemState(cmd);
        }
    }

    @Override
    public String menuItemName(int cmd, String oldName) {

        switch (cmd) {
            case cmdTranscribe:
                return oldName;
            default:
                return super.menuItemName(cmd, oldName);
        }
    }

    @Override
    public boolean doCommand(int cmd) {

        boolean cmdHandled = true;
        switch (cmd) {
            case cmdTranscribe:
                new SimpleTranscriptionWindow(this.getUI(), this.getG2P()).setVisible(true);
                break;
            default:
                return super.doCommand(cmd);
        }

        this.updateMenus();

        return cmdHandled;
    }
}
