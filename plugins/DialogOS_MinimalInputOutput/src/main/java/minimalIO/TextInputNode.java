package minimalIO;

import com.clt.diamant.Device;
import com.clt.diamant.InputCenter;
import com.clt.diamant.graph.nodes.AbstractInputNode;
import com.clt.diamant.graph.nodes.NodeExecutionException;
import com.clt.script.exp.Match;
import com.clt.script.exp.Pattern;
import com.clt.script.exp.Value;
import com.clt.script.exp.patterns.VarPattern;
import com.clt.speech.htk.MlfNode;
import com.clt.speech.recognition.*;
import com.clt.srgf.Grammar;

import javax.sound.sampled.AudioFormat;
import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Created by timo on 09.10.17.
 */
public class TextInputNode extends AbstractInputNode {

    private static Scanner sysin;
    static {
        sysin = new Scanner(System.in);
    }

    @Override
    protected RecognitionExecutor createRecognitionExecutor(Grammar recGrammar) {
        return null;
    }

    @Override
    protected Device getDevice() {
        return null;
    }

    @Override protected MatchResult graphicallyRecognize(JLayeredPane layer, com.clt.srgf.Grammar recGrammar, Pattern[] patterns, long timeout, float confidenceThreshold, boolean interactiveTest) {
        MatchResult match = null;
        int trials = -1;
        do {
            String recognitionResult = attemptRecognition(recGrammar, patterns, timeout);
            match = findMatch(recognitionResult, recGrammar, patterns);
            trials++;
        } while (match == null && trials < timeout);
        if (trials > timeout)
            System.out.println("reached timeout: " + timeout);
        if (interactiveTest)
            System.out.println("confirming result: " + match.getUtterance());
        return match;
    }

    @Override
    protected AudioFormat getAudioFormat() {
        return null;
    }

    private String attemptRecognition(Grammar recGrammar, Pattern[] patterns, long timeout) {
        System.out.println("recGrammar: " + recGrammar.toString());
        System.out.println("given above recognition grammar, what's your result?");
        System.out.print  ("> ");
        return sysin.nextLine();
    }

    @Override
    protected void recognizeInBackground(Grammar recGrammar, InputCenter input, VarPattern backgroundPattern, float confidenceThreshold) {
        throw new NodeExecutionException(this, "TextInputNode does not support background recognition");
    }

    private LanguageName defaultLanguage = new LanguageName("", null);
    @Override protected List<LanguageName> getAvailableLanguages() {
        return new ArrayList<LanguageName>(Arrays.asList(defaultLanguage));
    }
    @Override protected LanguageName getDefaultLanguage() {
        return defaultLanguage;
    }

}
