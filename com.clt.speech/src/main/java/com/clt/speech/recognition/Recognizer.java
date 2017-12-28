/*
 * @(#)Recognizer.java
 * Created on Tue Nov 12 2002
 *
 * Copyright (c) 2002 CLT Sprachtechnologie GmbH.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CLT Sprachtechnologie GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CLT Sprachtechnologie GmbH.
 */

package com.clt.speech.recognition;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.UnsupportedAudioFileException;

import com.clt.properties.Property;
import com.clt.speech.SpeechException;

/**
 * The base interface for all recognizers.
 * 
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

public interface Recognizer {

  public void addRecognizerListener(RecognizerListener l);


  public void removeRecognizerListener(RecognizerListener l);


  /**
   * Start recognition. This method returns the recognition result or
   * <code>null</code> if recognition was stopped manually.
   */
  public RecognitionResult startLiveRecognition()
      throws SpeechException;


  /**
   * Start recognition on the file. This method returns the recognition result
   * or <code>null</code> if recognition was stopped manually.
   * 
   * @throws IOException
   * @throws UnsupportedAudioFileException
   */
  public RecognitionResult startOfflineRecognition(File soundFile)
        throws SpeechException, IOException, UnsupportedAudioFileException;


  /** Abort recognition. */
  public void stopRecognition()
      throws SpeechException;


  /** Return whether recognition is currently active */
  public boolean isActive()
      throws SpeechException;


  /** Return whether the recognizer supports offline recognition */
  public boolean supportsOfflineRecognition()
      throws SpeechException;


  /** Return whether the recognizer is configured for live recognition */
  public boolean isLive()
      throws SpeechException;


  /** Return an array of recognizer properties. */
  public Property<?>[] getProperties();


  /**
   * Return an informative string describing the available domains and (if
   * listContexts is true) their respective contexts.
   */
  public String getEngineInfo(boolean listContexts)
      throws SpeechException;


  public Domain[] getDomains()
      throws SpeechException;


  public Domain createDomain(String name)
      throws SpeechException;


  public void setDomain(Domain domain)
      throws SpeechException;


  public Domain getDomain()
      throws SpeechException;


  public void setContext(RecognitionContext context)
      throws SpeechException;


  public RecognitionContext getContext()
      throws SpeechException;
}
