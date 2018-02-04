package com.clt.speech;

public class SpeechException extends Exception {

    public SpeechException(String message) {
        super(message);
    }

    public SpeechException(String message, Throwable cause) {
        super(message, cause);
    }

    public SpeechException(Throwable cause) {
        super(cause);
    }
}
