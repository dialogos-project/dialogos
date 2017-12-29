/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.saar.coli.dialogos.pocketsphinx.plugin;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.clt.script.exp.Match;
import com.clt.script.exp.Pattern;
import com.clt.script.exp.Value;
import com.clt.speech.SpeechException;
import com.clt.speech.recognition.RecognitionResult;
import com.clt.speech.recognition.RecognizerException;
import com.clt.speech.recognition.RecognizerListener;
import com.clt.speech.recognition.Utterance;
import com.clt.srgf.Grammar;
import de.saar.coli.dialogos.pocketsphinx.PocketSphinx;

/**
 *
 * TODO - reuse one class for all recognizer plugins? AK
 *
 * @author dabo
 */
public class RecognitionThread {

    private PocketSphinx recognizer;
    private RecognitionResult recognitionResult = null;

    public RecognitionThread(PocketSphinx recognizer) {
        this.recognizer = recognizer;
    }

    MatchResult start(final Grammar grammar, final Pattern[] patterns, long timeout, final RecognizerListener stateListener, final float recognitionThreshold)
            throws InterruptedException, ExecutionException, TimeoutException {

        Future<MatchResult> result = Executors.newSingleThreadExecutor().submit(new Callable<MatchResult>() {
            public MatchResult call() throws SpeechException {
                if (stateListener != null) {
                    RecognitionThread.this.recognizer.addRecognizerListener(stateListener);
                }

                RecognitionThread.this.recognitionResult = null;
                try {
                    do {
                        RecognitionThread.this.recognitionResult = RecognitionThread.this.recognizer.startLiveRecognition();
                        if (RecognitionThread.this.recognitionResult == null) {
                            return null;
                        }
                    } while ((RecognitionThread.this.recognitionResult.numAlternatives() == 0)
                            || (RecognitionThread.this.recognitionResult.getAlternative(0).getConfidence() < recognitionThreshold));
                } finally {
                    if (stateListener != null) {
                        RecognitionThread.this.recognizer
                                .removeRecognizerListener(stateListener);
                    }
                }
                
                // TODO - If the grammar is not a real grammar, but the "direct" or "dynamic"
                // grammar, allow partial matches (i.e., keywords appear somewhere in recognized string)

                for (int alt = 0; alt < recognitionResult.numAlternatives(); alt++) { // look for patterns in all recognizer hypotheses
                    Utterance utterance = RecognitionThread.this.recognitionResult.getAlternative(alt);
//                    System.err.println("match with grammar: " + grammar);
                    Value r = grammar.match(utterance.getWords(), null);
                    System.err.printf("[%2d] Hyp: %s\n", alt, utterance.getWords());

                    for (int i = 0; i < patterns.length; i++) {
                        Match match = patterns[i].match(r);
                        if (match != null) {
                            return new MatchResult(match, i);
                        }
                    }
                }

//                throw new RecognizerException("No match for recognition result '" + utterance.getWords() + "'");
                
                // no match found
//                Value r = grammar.match(recognitionResult.getAlternative(0).getWords(), null);
                throw new RecognizerException(Resources.format("NoMatchFor", recognitionResult.getAlternative(0).getWords().toString()));
            }
        });

        if (timeout <= 0) {
            return result.get();
        } else {
            return result.get(timeout, TimeUnit.MILLISECONDS);
        }
    }

    RecognitionResult getLastResult() {

        return this.recognitionResult;
    }

    public void stop() {

        try {
            this.recognizer.stopRecognition();
        } catch (Exception exn) {
            System.out.println(System.currentTimeMillis() + ": exn");
        }
    }
}
