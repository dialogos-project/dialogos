
package de.saar.coli.dialogos.marytts;

import com.clt.speech.Language;

/**
 * @author dabo
 * 
 */
public class MaryTTSVoice implements com.clt.speech.tts.Voice {

  String name;
  Language language;

  public MaryTTSVoice(String name, Language language) {
    this.name = name;
    this.language = language;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public Language getLanguage() {
    return this.language;
  }
}
