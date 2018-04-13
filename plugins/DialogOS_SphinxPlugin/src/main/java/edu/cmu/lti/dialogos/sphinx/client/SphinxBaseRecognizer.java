package edu.cmu.lti.dialogos.sphinx.client;

import com.clt.properties.Property;
import com.clt.speech.Language;
import com.clt.speech.SpeechException;

import javax.sound.sampled.AudioFormat;

/** anything that is irrelevant for core Sphinx functioning
 * (but needs to be added on top of SingleDomainRecognizer)
 */
public abstract class SphinxBaseRecognizer extends SingleDomainRecognizer {

    public static AudioFormat audioFormat = new AudioFormat(16000f, 16, 1, true, false);
    public static AudioFormat getAudioFormat() { return audioFormat; }

    public Property<?>[] getProperties() {
        return new Property<?>[0];
    }

    /** only ever called from TranscriptionWindow (and nobody seems to use that */
    @Override public String[] transcribe(String word, Language language) throws SpeechException {
        return null;
    }


}
