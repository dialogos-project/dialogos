package com.clt.speech.tts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.clt.speech.G2P;
import com.clt.speech.Language;
import com.clt.speech.SpeechException;

/**
 * @author dabo
 *
 */
public abstract class AbstractSynthesizer implements Synthesizer, G2P {

    public void speak(File file) throws IOException, SpeechException {

        if (!file.isFile()) {
            throw new FileNotFoundException(file.toString());
        } else {
            BufferedReader r = new BufferedReader(new FileReader(file));
            String line;

            while ((line = r.readLine()) != null) {
                this.speak(line);
            }

            r.close();
        }
    }

    public abstract void synthesizeToFile(String text, File f) throws IOException, SpeechException;

    public Map<String, String[]> transcribe(Collection<String> words, Language language) throws SpeechException {

        Map<String, String[]> transcriptions = new HashMap<String, String[]>();
        for (String word : words) {
            transcriptions.put(word, this.transcribe(word, language));
        }
        return transcriptions;
    }

    public Voice findVoice(Language language) throws SpeechException {

        return this.findVoice(language.getLocale());
    }

    public Voice findVoice(Locale locale)
            throws SpeechException {

        Voice[] voices = this.getAvailableVoices();
        for (int i = 0; i < voices.length; i++) {
            Voice info = voices[i];
            try {
                if ((info.getLanguage() != null)
                        && info.getLanguage().getLocale().getLanguage().equals(
                                locale.getLanguage())) {
                    return voices[i];
                }
            } catch (Exception ignore) {
            }
        }
        // nothing found.
        return null;
    }
}
