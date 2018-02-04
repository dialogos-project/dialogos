package com.clt.diamant.log;

import com.clt.audio.PortAudioInput;
import com.clt.audio.Settings;

public class AudioLog {

    public PortAudioInput pa;
    public Settings settings = null;

    public AudioLog() {

        this.pa = new PortAudioInput();

    }

}
