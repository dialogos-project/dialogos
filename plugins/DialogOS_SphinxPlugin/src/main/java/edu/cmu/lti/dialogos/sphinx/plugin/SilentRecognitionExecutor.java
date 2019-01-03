/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.lti.dialogos.sphinx.plugin;

import com.clt.diamant.Main;
import com.clt.diamant.graph.nodes.AbstractInputNode;
import com.clt.diamant.gui.DocumentWindow;
import com.clt.diamant.gui.SilentInputWindow;
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
        String input = SilentInputWindow.getString(parent);

        if (input == null) {
            return null;
        } else {
            MatchResult mr = AbstractInputNode.findMatch(input, grammar, patterns);

            if (mr == null) {
                throw new RecognizerException("No match for recognition result '" + input + "'"); // TODO localize
            }

            return mr;
        }
    }

    @Override
    public void stop() {
    }
}
