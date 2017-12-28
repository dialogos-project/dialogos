package com.clt.speech.tts;

import com.clt.properties.Property;
import com.clt.speech.SpeechException;

public interface Synthesizer {

  /** Get all parameters of this synthesizer */
  public Property<?>[] getProperties();


  /** Speak the given text */
  public void speak(String text)
      throws SpeechException;


  /** Abort output and discard pending text */
  public void stop()
      throws SpeechException;


  /** Release all resources acquired by this instance */
  public void dispose()
      throws SpeechException;


  /** Get the current voice */
  public Voice getVoice();


  /** Set the current voice */
  public void setVoice(Voice voice);


  /** Find a voice by name */
  public Voice findVoice(String name);


  /** Return all voices supported by this synthesizer */
  public Voice[] getAvailableVoices()
      throws SpeechException;
}