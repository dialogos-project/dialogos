package com.clt.speech.recognition;

import com.clt.speech.SpeechException;

/**
 * Speech recognizers should throw this Exception or a subclass of it in case of
 * an internal error.
 *
 * @author Daniel Bobbert
 * @version 1.0
 */
public class RecognizerException extends SpeechException {

    public RecognizerException(String message) {

        super(message);
    }

    public RecognizerException(String message, Throwable cause) {

        super(message, cause);
    }

}
