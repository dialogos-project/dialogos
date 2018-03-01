package com.clt.speech.tts;

import com.clt.util.StringTools;
import com.clt.speech.Resources;

public class VoiceName implements Comparable<VoiceName> {

    private String name;
    private Voice voice;

    public VoiceName(String name, Voice voice) {

        if (name == null) {
            throw new IllegalArgumentException();
        }

        this.name = name;
        this.voice = voice;
    }

    public String getName() {

        return this.name;
    }

    public Voice getVoice() {

        return this.voice;
    }

    public String getNormalizedName() {

        String name = this.getName();
        if (name.endsWith("16")) {
            name = name.substring(0, name.length() - 2);
        }
        return name;
    }

    @Override
    public boolean equals(Object o) {

        if (o instanceof VoiceName) {
            VoiceName vn = (VoiceName) o;
            if (vn.name.equals(this.name)) {
                return this.voice == null ? vn.voice == null : this.voice
                        .equals(vn.voice);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {

        return this.name.hashCode();
    }

    @Override
    public String toString() {

        if (StringTools.isEmpty(this.name)) {
            return "<" + Resources.getString("DefaultVoice") + ">";
        } else {
            String name = this.getNormalizedName();
            if (this.voice == null) {
                return name + " " + Resources.getString("NotAvailable");
            } else {
                return name + " (" + this.voice.getLanguage().getName() + ")";
            }
        }
    }

    public int compareTo(VoiceName o) {

        int result = this.name.compareTo(o.name);
        if (result != 0) {
            return result;
        } else if (this.voice == null) {
            return o.voice == null ? 0 : 1;
        } else if (o.voice == null) {
            return this.voice == null ? 0 : -1;
        } else {
            return this.voice.getLanguage().toString().compareTo(
                    o.voice.getLanguage().toString());
        }
    }
}
