package com.clt.audio;

import com.clt.resources.DynamicResourceBundle;

class Audio {
    private static DynamicResourceBundle resources = new DynamicResourceBundle("com.clt.audio.Resources");

    static String getString(String key) {
        return Audio.resources.getString(key);
    }

}
