/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.lti.dialogos.sphinx.plugin;

import com.clt.diamant.Main;
import com.clt.diamant.graph.nodes.AbstractInputNode;
import com.clt.diamant.gui.DocumentWindow;
import com.clt.script.exp.Pattern;
import com.clt.speech.recognition.MatchResult;
import com.clt.speech.recognition.RecognitionExecutor;
import com.clt.speech.recognition.RecognizerException;
import com.clt.speech.recognition.RecognizerListener;
import com.clt.srgf.Grammar;

/**
 *
 * @author koller
 */
public class SilentRecognitionExecutor implements RecognitionExecutor {

    @Override
    public MatchResult start(Grammar grammar, Pattern[] patterns, long timeout, RecognizerListener stateListener, float recognitionThreshold) throws RecognizerException {
        DocumentWindow parent = null; // Main.getInstance().getCurrentWindow(); // this doesn't work; see #142.
        SilentInputWindow w = new SilentInputWindow(parent);

        if (parent == null) {
            // if parent unavailable (e.g. because recognizer called via
            // "Try" button of properties window), center on screen
            w.setLocationRelativeTo(null);
        }

        w.setVisible(true);

        if (w.getInput() == null) {
            return null;
        } else {
            MatchResult mr = AbstractInputNode.findMatch(w.getInput(), grammar, patterns);

            if (mr == null) {
                throw new RecognizerException("No match for recognition result '" + w.getInput() + "'"); // TODO localize
            }

            return mr;
        }
    }

    @Override
    public void stop() {
    }
}
