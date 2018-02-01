/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.clt.dialogos.sphinx;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.clt.diamant.graph.nodes.AbstractInputNode;
import com.clt.script.exp.Match;
import com.clt.script.exp.Pattern;
import com.clt.script.exp.Value;
import com.clt.speech.recognition.*;
import com.clt.speech.recognition.RecognitionExecutor;
import com.clt.srgf.Grammar;

/**
 *
 */
public class SphinxRecognitionExecutor implements RecognitionExecutor {

    private Sphinx recognizer;
    private Settings settings;
    private RecognitionResult recognitionResult = null;

    public SphinxRecognitionExecutor(Sphinx recognizer, Settings settings) {
        this.recognizer = recognizer;
        this.settings = settings;
    }

    public MatchResult start(final Grammar grammar, final Pattern[] patterns, long timeout, final RecognizerListener stateListener, final float recognitionThreshold)
            throws InterruptedException, ExecutionException, TimeoutException {
        Future<MatchResult> result = Executors.newSingleThreadExecutor().submit(() ->
            {
                recognizer.setContext(grammar);
                if (stateListener != null) {
                    recognizer.addRecognizerListener(stateListener);
                }
                recognitionResult = null;
                try {
                    do {
                        recognitionResult = recognizer.startLiveRecognition();
                        if (recognitionResult == null) {
                            return null;
                        }
                    } while ((recognitionResult.numAlternatives() == 0)
                            || (recognitionResult.getAlternative(0).getConfidence() < recognitionThreshold));
                } finally {
                    if (stateListener != null) {
                        recognizer.removeRecognizerListener(stateListener);
                    }
                }
                Utterance utterance = SphinxRecognitionExecutor.this.recognitionResult.getAlternative(0);
                MatchResult mr = AbstractInputNode.findMatch(utterance.getWords(), grammar, patterns);
                // TODO unlocalized string
                if (mr == null)
                    throw new RecognizerException("No match for recognition result '" + utterance.getWords() + "'");
//                throw new RecognizerException(Resources.format("NoMatchFor", r));
                return mr;
            });
        if (stateListener != null) {
            stateListener.recognizerStateChanged(
                    new RecognizerEvent(recognizer, RecognizerEvent.RECOGNIZER_ACTIVATED));
        }
        if (timeout <= 0) {
            return result.get();
        } else {
            return result.get(timeout, TimeUnit.MILLISECONDS);
        }
    }

    public void stop() {
        try {
            recognizer.stopRecognition();
        } catch (Exception exn) {
            exn.printStackTrace();
            System.out.println(System.currentTimeMillis() + ": exn");
        }
    }
}
