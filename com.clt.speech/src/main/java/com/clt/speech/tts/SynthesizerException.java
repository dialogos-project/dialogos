package com.clt.speech.tts;

import com.clt.speech.SpeechException;

public class SynthesizerException extends SpeechException {

  public SynthesizerException(String message) {

    super(message);
  }


  public SynthesizerException(String message, Throwable cause) {

    super(message, cause);
  }
}