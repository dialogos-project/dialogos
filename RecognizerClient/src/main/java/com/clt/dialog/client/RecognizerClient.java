package com.clt.dialog.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.clt.audio.LevelMeter;
import com.clt.event.ProgressEvent;
import com.clt.event.ProgressListener;
import com.clt.gui.CmdButton;
import com.clt.gui.FileChooser;
import com.clt.gui.GUI;
import com.clt.gui.ListSelectionDialog;
import com.clt.gui.OptionPane;
import com.clt.gui.ProgressDialog;
import com.clt.gui.border.GroupBorder;
import com.clt.gui.menus.CmdCheckBoxMenuItem;
import com.clt.gui.menus.CmdMenu;
import com.clt.gui.menus.MenuCommander;
import com.clt.io.PreprocessingReader;
import com.clt.properties.BooleanProperty;
import com.clt.properties.DefaultBooleanProperty;
import com.clt.properties.DefaultFloatProperty;
import com.clt.properties.DefaultIntegerProperty;
import com.clt.properties.EnumProperty;
import com.clt.properties.FloatProperty;
import com.clt.properties.IntegerProperty;
import com.clt.properties.Property;
import com.clt.properties.PropertySet;
import com.clt.script.exp.Value;
import com.clt.script.exp.values.BoolValue;
import com.clt.script.exp.values.IntValue;
import com.clt.script.exp.values.ListValue;
import com.clt.script.exp.values.RealValue;
import com.clt.script.exp.values.StringValue;
import com.clt.script.exp.values.StructValue;
import com.clt.speech.G2P;
import com.clt.speech.SpeechException;
import com.clt.speech.recognition.AbstractRecognizer;
import com.clt.speech.recognition.Domain;
import com.clt.speech.recognition.RecognitionContext;
import com.clt.speech.recognition.RecognitionResult;
import com.clt.speech.recognition.RecognizerEvent;
import com.clt.speech.recognition.RecognizerException;
import com.clt.speech.recognition.RecognizerListener;
import com.clt.speech.recognition.Utterance;
import com.clt.speech.recognition.Word;
import com.clt.speech.recognition.test.OptimizeParameters;
import com.clt.speech.recognition.test.Sample;
import com.clt.srgf.Grammar;
import com.clt.srgf.ParseOptions;
import com.clt.srgf.Rule;
import com.clt.util.DefaultLongAction;
import com.clt.util.MetaCollection;
import com.clt.util.StringTools;
import com.clt.util.Timeout;
import com.clt.xml.XMLWriter;

public abstract class RecognizerClient<RecognizerType extends AbstractRecognizer> extends SpeechClient {

    protected static final int cmdPTT = 3100;
    protected static final int cmdStart = 3101;
    protected static final int cmdProcessFile = 3102;
    protected static final int cmdProcessFiles = 3103;
    protected static final int cmdTestVocabulary = 3104;
    protected static final int cmdNewGrammar = 3105;
    protected static final int cmdOpenGrammar = 3106;
    protected static final int cmdTreeView = 3107;
    protected static final int cmdShowParameters = 3108;
    protected static final int cmdRunOfflineTest = 3109;

    private static final Object recognitionLock = new Object();

    protected SimulationDialog simulation = null;
    private TreesView treeView;

    protected BooleanProperty simulate;
    protected IntegerProperty numAlternatives;
    protected BooleanProperty autoStart;
    protected BooleanProperty sendSemantics;
    protected BooleanProperty ignoreEmptyRecognition;
    protected BooleanProperty sendAlternatives;
    protected BooleanProperty sendAlternativesAsList;
    protected BooleanProperty logging;
    protected FloatProperty cutoff;
    protected FloatProperty distance;

    protected EnumProperty<Domain> domains;
    protected EnumProperty<RecognitionContext> contexts;

    private transient StructValue lastAddWords = null;
    private transient ListValue lastPhonetics = null;
    private Map<String, Map<String, String>> substitutions
            = new HashMap<String, Map<String, String>>();

    private RecognizerType recognizer = null;
    private RecognizerListener recognizerCallback;

    private TextPane output;
    protected LevelMeter levelMeter;
    protected BooleanProperty showLevels;
    private TextPane detail;
    private JPanel optionsPanel;
    private JFrame parameterWindow = null;

    protected JLabel indicator;
    private List<JLabel> externalIndicators = new LinkedList<JLabel>();
    private List<JLabel> externalResultFields = new LinkedList<JLabel>();
    private JLabel partialResult = null;
    private Icon speechIcon = null;
    private Icon waveIcon = null;
    private Icon textIcon = null;
    private JPanel mainPanel;

    private JTextArea temporaryGrammar = null;
    private FileChooser fileChooser = new FileChooser();

    protected RecognizerClient() {

        this.logging
                = new DefaultBooleanProperty(
                        "logging",
                        "Logging",
                        "Select this option to send logging information to the dialog manager",
                        false);
        this.simulate
                = new DefaultBooleanProperty(
                        "simulate",
                        "Simulate",
                        "Select this option to simulate recognition using manual input instead of starting the recognizer",
                        false);
        this.numAlternatives
                = new DefaultIntegerProperty("numAlternatives", "Alternatives",
                        "The maximum number of alternatives.", 1);
        this.numAlternatives.setEditType(Property.EDIT_TYPE_NUMBERFIELD);
        this.autoStart
                = new DefaultBooleanProperty(
                        "autoStart",
                        "Auto-Start",
                        "Select this option to automatically reactivate the recognizer after returning a recognition result",
                        false);
        this.sendSemantics
                = new DefaultBooleanProperty(
                        "sendSemantics",
                        "Send Semantics",
                        "Select this option to interpret the recognition result using a grammar",
                        false);
        this.ignoreEmptyRecognition
                = new DefaultBooleanProperty(
                        "ignoreEmptyRecognition",
                        "Ignore Empty Results",
                        "Select this option to silently restart the recognizer on empty recognition results instead of sending the empty result to the dialog.",
                        false);
        this.sendAlternatives
                = new DefaultBooleanProperty(
                        "sendAlternatives",
                        "Send Alternatives",
                        "Select this option to send all alternatives returned by he recognizer",
                        false);
        this.sendAlternativesAsList
                = new DefaultBooleanProperty(
                        "sendAlternativesAsList",
                        "Send Alternatives as a List",
                        "Select this option to send alternatives as a list instead of a structure",
                        false);
        this.cutoff = new DefaultFloatProperty("cutoff", "Threshold",
                "Results with a confidence below this value will be ignored", 0.0f);
        this.distance
                = new DefaultFloatProperty(
                        "distance",
                        "Distance",
                        "Results with a confidence less than the best alternative's confidence minus this value will be ignored",
                        0.3f);

        this.showLevels
                = new DefaultBooleanProperty("levels", "Show Audio Level", null, true);
        this.showLevels.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {

                if (RecognizerClient.this.levelMeter != null) {
                    RecognizerClient.this.levelMeter
                            .setEnabled(RecognizerClient.this.showLevels.getValue());
                }
            }
        });

        this.domains = new EnumProperty<Domain>("domain") {

            protected void setValueImpl(Domain value) {

                try {
                    RecognizerClient.this.recognizer.setDomain(value);
                } catch (Exception exn) {
                    RecognizerClient.this.error(exn);
                }
            }

            public Domain getValue() {

                try {
                    return RecognizerClient.this.recognizer.getDomain();
                } catch (Exception exn) {
                    return null;
                }
            }

            public Domain[] getPossibleValues() {

                try {
                    return RecognizerClient.this.recognizer.getDomains();
                } catch (Exception e) {
                    return new Domain[0];
                }
            }

            public String getName() {

                return "Domain";
            }

            public String getDescription() {

                return null;
            }
        };

        this.contexts = new EnumProperty<RecognitionContext>("context") {

            protected void setValueImpl(RecognitionContext value) {

                try {
                    RecognizerClient.this.activateContext(value, null, null);
                } catch (Exception exn) {
                    RecognizerClient.this.error(exn);
                }
            }

            public RecognitionContext getValue() {

                try {
                    return RecognizerClient.this.recognizer.getContext();
                } catch (Exception exn) {
                    return null;
                }
            }

            public RecognitionContext[] getPossibleValues() {

                try {
                    Domain d = RecognizerClient.this.domains.getValue();
                    if (d != null) {
                        return d.getContexts();
                    } else {
                        return null;
                    }
                } catch (Exception exn) {
                    return null;
                }
            }

            public String getName() {

                return "Context";
            }

            public String getDescription() {

                return null;
            }
        };

        this.domains.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {

                RecognizerClient.this.updateMenus();
            }
        });
        this.contexts.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {

                RecognizerClient.this.updateMenus();
            }
        });

        this.speechIcon
                = new ImageIcon(this.getClass().getClassLoader().getResource(
                        "images/SpeechActive.png"));
        this.waveIcon
                = new ImageIcon(this.getClass().getClassLoader().getResource(
                        "images/FileActive.png"));
        this.textIcon
                = new ImageIcon(this.getClass().getClassLoader().getResource(
                        "images/TextActive.png"));

        this.indicator = new JLabel(null, null, JLabel.CENTER);
        this.indicator.setOpaque(true);
        this.indicator.setPreferredSize(new Dimension(40, 20));
        this.indicator.setMinimumSize(this.indicator.getPreferredSize());
        this.indicator.setBorder(BorderFactory.createLineBorder(Color.black, 1));
        this.indicator.setBackground(Color.red);

        this.indicator.addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent evt) {

                if (evt.getClickCount() == 2) {
                    RecognizerClient.this.doCommand(RecognizerClient.cmdStart);
                }
            }
        });

        this.levelMeter = new LevelMeter();
        this.levelMeter.setEnabled(this.showLevels.getValue());

        this.recognizerCallback = new RecognizerListener() {

            public void recognizerStateChanged(RecognizerEvent evt) {

                RecognizerClient.this.updateMenus();
                RecognizerClient.this.setIndicatorColor(evt.getType());
                try {
                    switch (evt.getType()) {
                        case RecognizerEvent.RECOGNIZER_READY:
                        case RecognizerEvent.RECOGNIZER_ACTIVATED:
                        case RecognizerEvent.RECOGNIZER_DEACTIVATED:
                        case RecognizerEvent.START_OF_SPEECH:
                        case RecognizerEvent.END_OF_SPEECH:
                            if (RecognizerClient.this.logging.getValue()) {
                                try {
                                    RecognizerClient.this.log(evt.toString());
                                } catch (Exception exn) {
                                    // ignore
                                }
                            }
                            break;
                        case RecognizerEvent.RECOGNIZER_WARNING:
                            RecognizerClient.this.detail.println("WARNING: "
                                    + evt.getErrorMessage());
                            if (RecognizerClient.this.logging.getValue()) {
                                try {
                                    RecognizerClient.this.log(evt.toString());
                                } catch (Exception exn) {
                                    // ignore
                                }
                            }
                            break;
                        case RecognizerEvent.PARTIAL_RESULT:
                            if (evt.getResult() != null) {
                                RecognizerClient.this.partialResult.setText(evt
                                        .getResult().getAlternative(0).getWords());
                            }
                            break;
                        default:
                            RecognizerClient.this
                                    .println("Unexpected recognizer state change: "
                                            + evt.getType());
                            break;
                    }
                } catch (ThreadDeath d) {
                    throw d;
                } catch (Throwable exn) {
                    RecognizerClient.this.error(exn);
                }
                RecognizerClient.this.updateMenus();
            }
        };
    }

    protected void println(String message) {

        this.output.println(message);
    }

    private void initRecognizer(Properties properties, ProgressListener progress)
            throws SpeechException {

        this.lastAddWords = null;
        this.lastPhonetics = null;

        this.recognizer = this.createRecognizer(properties, progress);
        this.recognizer.addRecognizerListener(this.recognizerCallback);
    }

    @Override
    public boolean dispose() {

        if (this.recognizer != null) {
            try {
                this.recognizer.dispose();
                this.recognizer = null;
            } catch (Exception exn) {
                return false;
            }
        }
        return super.dispose();
    }

    protected RecognizerType getRecognizer() {

        return this.recognizer;
    }

    protected G2P getG2P() {

        return this.getRecognizer();
    }

    protected abstract RecognizerType createRecognizer(Properties properties,
            ProgressListener progress)
            throws SpeechException;

    public abstract String getRecognizerName();

    protected void handleRecognitionResult(RecognitionResult result,
            Map<String, Map<String, String>> substitutions) {

        synchronized (RecognizerClient.recognitionLock) {
            try {
                if (this.treeView != null) {
                    this.treeView.show(result);
                }

                Value message = this.buildResultMessage(result, substitutions);

                if (message != null) {
                    this.println(message.toString());
                    try {
                        this.send(message);
                    } catch (Exception ignore) {
                    }
                }
            } catch (Exception exn) {
                this.error(exn);
            }
        }
    }

    private Value buildResultMessage(RecognitionResult result,
            Map<String, Map<String, String>> substitutions)
            throws SpeechException {

        Value message;

        if (this.sendAlternatives.getValue()) {
            int numAlternatives = Math.min(result.numAlternatives(),
                    this.numAlternatives.getValue());
            if (numAlternatives > 0) {
                int n = 0;
                float topConf = result.getAlternative(0).getConfidence();
                while ((n < result.numAlternatives())
                        && (n < numAlternatives)
                        && (result.getAlternative(n).getConfidence() >= this.cutoff
                        .getValue())
                        && (result.getAlternative(n).getConfidence() >= topConf
                        - this.distance.getValue())) {
                    n++;
                }

                numAlternatives = n;
            }

            String names[] = new String[numAlternatives];
            Value values[] = new Value[numAlternatives];

            if (this.logging.getValue()) {
                try {
                    this.log("Recognition result: " + result.getAlternative(0));
                } catch (IOException exn) {
                    // ignore
                }
            }

            for (int i = 0; i < numAlternatives; i++) {
                names[i] = "r" + (i + 1);
                values[i] = this.interpret(result.getAlternative(i), substitutions);
            }
            if (this.sendAlternativesAsList.getValue()) {
                message = new ListValue(values);
            } else {
                message = new StructValue(names, values);
            }
        } else {
            message = this.interpret(result.getAlternative(0), substitutions);
        }

        return message;
    }

    protected Map<String, Map<String, String>> getSubstitutions() {

        return this.substitutions;
    }

    protected void setSubstitutions(Map<String, Map<String, String>> substitutions) {

        this.substitutions = substitutions;
    }

    protected void startRecording() {

    }

    protected void saveRecording() {

    }

    private void processAudioFile(File file, final XMLWriter output)
            throws Exception {

        RecognizerListener callback = new RecognizerListener() {

            public void recognizerStateChanged(RecognizerEvent evt) {

                switch (evt.getType()) {
                    case RecognizerEvent.RECOGNIZER_WARNING:
                        output.printElement("warning", evt.getErrorMessage());
                        break;
                }
            }
        };

        if (this.recognizer.isActive()) {
            this.recognizer.stopRecognition();
        }
        this.recognizer.addRecognizerListener(callback);
        try {
            RecognitionResult r = this.recognizer.startOfflineRecognition(file);
            if (r != null) {
                output.openElement("result", new String[]{"alternatives"},
                        new String[]{String.valueOf(r.numAlternatives())});
                for (int i = 0; i < r.numAlternatives(); i++) {
                    Utterance utt = r.getAlternative(i);
                    output.openElement("alternative", new String[]{"confidence"},
                            new String[]{String.valueOf(utt.getConfidence())});
                    output.printElement("value", this.interpret(utt, this
                            .getSubstitutions()));
                    for (int j = 0; j < utt.length(); j++) {
                        com.clt.speech.recognition.Word word = utt.getWord(j);
                        if ((word.getStart() != 0) && (word.getEnd() != 0)) {
                            output.printElement("word",
                                    new String[]{"start", "end", "confidence"},
                                    new String[]{
                                        String.valueOf(word.getStart()),
                                        String.valueOf(word.getEnd()),
                                        String.valueOf(word.getConfidence())},
                                    word.getWord());
                        } else {
                            output.printElement("word", new String[]{"confidence"},
                                    new String[]{String.valueOf(word
                                                .getConfidence())},
                                    word.getWord());
                        }
                        output.closeElement("alternative");
                    }
                }
                output.closeElement("result");
            }
        } finally {
            this.recognizer.removeRecognizerListener(callback);
            this.setIndicatorColor(RecognizerEvent.RECOGNIZER_READY);
        }
    }

    protected boolean sendEmptyResult() {

        if (!this.ignoreEmptyRecognition.getValue()) {
            try {
                Value message;
                if (this.sendAlternatives.getValue()) {
                    if (this.sendAlternativesAsList.getValue()) {
                        message = new ListValue(new Value[0]);
                    } else {
                        message = new StructValue();
                    }
                } else {
                    if (this.sendSemantics.getValue()) {
                        message = new StructValue();
                    } else {
                        message = new StringValue("");
                    }
                }

                this.println(message.toString());
                this.send(message);
                return true;
            } catch (SocketException exn) {
            } catch (Exception exn) {
                this.error(exn);
            }
        }
        return false;
    }

    protected void showResults(RecognitionResult result) {

        this.detail.clear();
        this.detail.println(result.toString());
        for (JLabel externalIndicator : this.externalIndicators) {
            externalIndicator.setText(this.getRecognizerName());
        }
        for (JLabel externalResultField : this.externalResultFields) {
            if ((result.numAlternatives() > 0)
                    && (result.getAlternative(0).getConfidence() >= this.cutoff
                    .getValue())) {
                externalResultField.setText(result.getAlternative(0).getWords());
            } else {
                externalResultField.setText("<>");
            }
        }
    }

    public void addIndicator(JLabel indicator, JLabel resultField) {

        this.externalIndicators.add(indicator);
        this.externalResultFields.add(resultField);
    }

    private boolean isActive() {

        if (this.simulate.getValue()) {
            return (this.simulation != null) && this.simulation.isShowing();
        } else if (this.recognizer != null) {
            return this.recognizer.isActive();
        } else {
            return false;
        }
    }

    private void setIndicatorColor(int state) {

        Color c = null;

        switch (state) {
            case RecognizerEvent.RECOGNIZER_ACTIVATED:
            case RecognizerEvent.START_OF_SPEECH:
                c = Color.green;
                break;
            case RecognizerEvent.RECOGNIZER_DEACTIVATED:
            case RecognizerEvent.END_OF_SPEECH:
                c = Color.orange;
                break;
            case RecognizerEvent.RECOGNIZER_READY:
                c = Color.red;
                break;
        }

        if (this.indicator != null) {
            if (c != null) {
                this.indicator.setBackground(c);
            }
            switch (state) {
                case RecognizerEvent.START_OF_SPEECH:
                    try {
                        if (this.simulate.getValue()) {
                            this.indicator.setIcon(this.textIcon);
                        } else if (this.recognizer.isLive()) {
                            this.indicator.setIcon(this.speechIcon);
                        } else {
                            this.indicator.setIcon(this.waveIcon);
                        }
                    } catch (Exception ignore) {
                        this.indicator.setIcon(this.speechIcon);
                    }
                    break;
                case RecognizerEvent.RECOGNIZER_READY:
                case RecognizerEvent.RECOGNIZER_ACTIVATED:
                case RecognizerEvent.RECOGNIZER_DEACTIVATED:
                case RecognizerEvent.END_OF_SPEECH:
                    this.indicator.setIcon(null);
                    break;
            }

            this.indicator.repaint();
        }

        for (JLabel externalIndicator : this.externalIndicators) {
            if (c != null) {
                externalIndicator.setBackground(c);
            }
            if ((state == RecognizerEvent.RECOGNIZER_ACTIVATED)
                    || (state == RecognizerEvent.START_OF_SPEECH)
                    || (state == RecognizerEvent.PARTIAL_RESULT)) {
                externalIndicator.setText(this.getRecognizerName());
            }
            externalIndicator.repaint();
        }
    }

    private Map<String, Map<String, String>> toLowercase(
            Map<String, Map<String, String>> substitutions) {

        Map<String, Map<String, String>> result
                = new HashMap<String, Map<String, String>>();

        for (String cls : substitutions.keySet()) {
            Map<String, String> map = substitutions.get(cls);
            Map<String, String> lowMap = new HashMap<String, String>();
            result.put(cls, lowMap);
            for (String word : map.keySet()) {
                lowMap.put(word.toLowerCase(), map.get(word));
            }
        }

        return result;
    }

    private RecognitionResult showSimulation(Object lock, Grammar g)
            throws Timeout {

        this.setIndicatorColor(RecognizerEvent.START_OF_SPEECH);

        if (g != null) {
            ParseOptions options = new ParseOptions();
            options.dynamicVocabulary = this.substitutions;
            options.fillerRules = new String[]{"FillerSeq", "OOVSeq"};

            return this.simulation.show(lock, g.getUsedWords(options),
                    this.numAlternatives.getValue());
        } else if (this.substitutions != null) {
            List<Collection<String>> cs
                    = new ArrayList<Collection<String>>(this.substitutions.size());
            for (Map<String, String> words : this.substitutions.values()) {
                cs.add(words.keySet());
            }
            return this.simulation.show(lock, new MetaCollection<String>(cs),
                    this.numAlternatives.getValue());
        } else {
            Collection<String> addWords = Collections.emptySet();
            return this.simulation.show(lock, addWords, this.numAlternatives
                    .getValue());
        }
        // System.out.println(simulation.getFocusOwner());
    }

    public void allowTimeout(boolean allowTimeout) {

        this.simulation.allowTimeout(allowTimeout);
    }

    public void signalTimeout() {

        this.simulation.signalTimeout();
    }

    public void stateChanged(ConnectionState state) {

        super.stateChanged(state);

        if (state == ConnectionState.DISCONNECTED) {
            this.stopRecognition();
        }
    }

    private final void startRecognition(final File file) {

        synchronized (RecognizerClient.recognitionLock) {
            try {
                final RecognitionContext context = this.getRecognizer().getContext();
                if (context == null) {
                    throw new RecognizerException("You must activate a context first.");
                }

                final Object startupLock = new Object();

                synchronized (startupLock) {
                    // if there already is a recognition thread running...
                    if (this.isActive()) {
                        if ((file == null) || this.simulate.getValue()) {
                            // for live recognition or simulation, simply ignore this second
                            // request and let the running thread do its work
                            return;
                        } else {
                            // for offline recognition, stop the working thread to start a new
                            // one
                            this.stopRecognition();
                        }
                    }

                    Thread recognitionThread = new Thread(new Runnable() {

                        public void run() {

                            boolean forceRestart = false;
                            RecognitionResult result = null;
                            Map<String, Map<String, String>> substitutions
                                    = RecognizerClient.this.getSubstitutions();

                            try {
                                if (RecognizerClient.this.simulate.getValue()) {
                                    result
                                            = RecognizerClient.this.showSimulation(startupLock, context
                                                    .getGrammar());
                                    if (result != null) {
                                        substitutions
                                                = RecognizerClient.this.toLowercase(substitutions);
                                    }
                                } else {
                                    if (file != null) {
                                        result
                                                = RecognizerClient.this.getRecognizer()
                                                        .startOfflineRecognition(
                                                                startupLock, file);
                                    } else {
                                        RecognizerClient.this.startRecording();
                                        try {
                                            result
                                                    = RecognizerClient.this.getRecognizer()
                                                            .startLiveRecognition(
                                                                    startupLock);
                                        } finally {
                                            RecognizerClient.this.saveRecording();
                                        }
                                    }
                                }

                                if (result != null) {
                                    RecognizerClient.this.showResults(result);
                                    if ((result.numAlternatives() > 0)
                                            && (result.getAlternative(0)
                                                    .getConfidence() >= RecognizerClient.this.cutoff
                                                    .getValue())) {
                                        RecognizerClient.this
                                                .handleRecognitionResult(result,
                                                        substitutions);
                                    } else {
                                        forceRestart
                                                = !RecognizerClient.this
                                                        .sendEmptyResult();
                                    }
                                }
                            } catch (Timeout t) {
                                try {
                                    RecognizerClient.this.sendTimeout();
                                } catch (Exception ignore) {
                                }
                            } catch (Exception exn) {
                                forceRestart
                                        = !RecognizerClient.this.sendEmptyResult();
                                RecognizerClient.this.println("ERROR: "
                                        + exn.toString());
                            }

                            RecognizerClient.this
                                    .setIndicatorColor(RecognizerEvent.RECOGNIZER_READY);
                            RecognizerClient.this.updateMenus();

                            if ((file == null)
                                    && (forceRestart || RecognizerClient.this.autoStart
                                            .getValue())) {
                                RecognizerClient.this.startRecognition(null);
                            }
                        }
                    }, this.getName() + " Recognition");
                    recognitionThread.start();

                    try {
                        startupLock.wait();
                    } catch (InterruptedException exn) {
                    }
                }
            } catch (Exception exn) {
                this.error(exn);
            }

            this.updateMenus();
        }
    }

    private void stopRecognition() {

        synchronized (RecognizerClient.recognitionLock) {
            try {
                if (this.simulate.getValue()) {
                    this.simulation.setVisible(false);

                    this.recognizerCallback.recognizerStateChanged(new RecognizerEvent(
                            this.recognizer,
                            RecognizerEvent.RECOGNIZER_READY));
                } else {
                    this.getRecognizer().stopRecognition();
                }
            } catch (Exception exn) {
                this.error(exn);
            }
            this.updateMenus();
        }
    }

    public void sessionStarted() {

        this.reset();
        if ((this.recognizer != null) && this.autoStart.getValue()) {
            this.startRecognition(null);
        }
    }

    public void reset() {

        // output.clear();
        // detail.clear();
    }

    public void error(Throwable exn) {

        for (JLabel externalResultField : this.externalResultFields) {
            externalResultField.setText(exn.toString());
        }

        this.println(exn.toString());
        exn.printStackTrace(System.err);

        super.error(exn);
    }

    public Map<String, String> getAcceptedInput() {

        Map<String, String> m = new LinkedHashMap<String, String>();

        m
                .put(
                        "\"start\"",
                        "Activate the recognizer using the current context. As soon as an utterance is recognized, the result is sent to the dialogue manager.");
        m
                .put(
                        "\"stop\"",
                        "Stop the recognizer. No intermediate recognition result will be sent to the dialogue manager.");

        m
                .put(
                        "{ recognize = <string> }",
                        "Start offline processing of the given file using the current context. As soon as an utterance is recognized, the result is sent to the dialogue manager.");

        m
                .put(
                        "{ batch = { in = <string>, out = <string> } }",
                        "Start offline processing a batch of files using the current context. "
                        + "<in> is the name of a file that holds a list of input audio files, one file name per line. "
                        + "<out> is the name of the file, that recognition results will be written to.");

        m
                .put(
                        "{ autostart = <bool> }",
                        "Set whether the reconizer should automatically reactivate after an utterance was recognized.");
        m
                .put(
                        "{ semantics = <bool> }",
                        "If set to true the recognizer will return the actions contained in the grammar instead of the actual utterance recognized.");

        m
                .put(
                        "{ threshold = <float> }",
                        "Set the recognition threshold (0.0 - 1.0). Recognition results with a score below this threshold will be discarded.");
        m
                .put(
                        "{ distance = <float> }",
                        "Set the recognition threshold distance (0.0 - 1.0). Recognition alternatives with a score that is less than the score of the best alternative minus the given distance will be discarded.");
        m.put("{ numAlternatives = <int> }",
                "Set the maximum number of recognition alternatives.");
        m
                .put(
                        "{ sendAlternatives = <bool> }",
                        "Switch whether multiple recognition results should be sent to the dialogue manager. If set to true alternatives are sent as a struct { r1 = <string>, r2 = <string> ... } even if there is only one alternative.");
        m
                .put(
                        "{ sendAlternativesAsList = <bool> }",
                        "Switch whether multiple recognition results are sent as a list instead of a struct. Only make sense if sendAlternatives is set to true.");
        m
                .put(
                        "{ sendEmpty = <bool> }",
                        "Set whether the recognizer should send an empty string to the dialogue manager in case of a failed recognition. "
                        + "If set to false, these empty recognitions will simply be discarded and the recognizer will be silently restarted. "
                        + "If set to true, the recognizer will be stopped and an empty string (or an empty structure, if semantics is turned on) will be sent to the dialogue manager.");

        m.put("{ domain = <string> }", "Switch the reocgnizer to the given domain");

        StringBuilder paramNames = new StringBuilder();
        try {
            Property<?>[] params = this.getRecognizer().getParameters();
            for (int i = 0; i < params.length; i++) {
                if (i > 0) {
                    paramNames.append(", ");
                }
                paramNames.append("\"" + params[i].getName() + "\"");
            }

            m
                    .put(
                            "{ setParam = { name = <string>, value = ... }}",
                            "Set the value of the given recognizer parameter. Supported parameters are "
                            + paramNames.toString() + ".");
        } catch (Exception ignore) {

        }
        return m;
    }

    public void output(Value v) {

        try {
            if (v instanceof StringValue) {
                String s = ((StringValue) v).getString();
                if (s.equals("start")) {
                    this.startRecognition(null);
                } else if (s.equals("stop")) {
                    this.stopRecognition();
                } else if (s.equalsIgnoreCase("autostart")) {
                    this.autoStart.setValue(true);
                } else if (s.equalsIgnoreCase("noautostart")) {
                    this.autoStart.setValue(false);
                } else if (s.equalsIgnoreCase("ignoreempty")) {
                    this.ignoreEmptyRecognition.setValue(true);
                } else if (s.equalsIgnoreCase("sendempty")) {
                    this.ignoreEmptyRecognition.setValue(false);
                } else if (s.equalsIgnoreCase("semantics")) {
                    this.sendSemantics.setValue(true);
                } else if (s.equalsIgnoreCase("nosemantics")) {
                    this.sendSemantics.setValue(false);
                }
            } else if (v instanceof StructValue) {
                StructValue data = (StructValue) v;

                if (data.containsLabel("autostart")) {
                    this.autoStart.setValue(((BoolValue) data.getValue("autostart"))
                            .getBool());
                }

                if (data.containsLabel("semantics")) {
                    this.sendSemantics.setValue(((BoolValue) data.getValue("semantics"))
                            .getBool());
                }

                if (data.containsLabel("sendAlternatives")) {
                    this.sendAlternatives.setValue(((BoolValue) data
                            .getValue("sendAlternatives")).getBool());
                }

                if (data.containsLabel("sendAlternativesAsList")) {
                    this.sendAlternativesAsList.setValue(((BoolValue) data
                            .getValue("sendAlternativesAsList")).getBool());
                }

                if (data.containsLabel("sendEmpty")) {
                    this.ignoreEmptyRecognition.setValue(!((BoolValue) data
                            .getValue("sendEmpty")).getBool());
                }

                if (data.containsLabel("numAlternatives")) {
                    int n = (int) ((IntValue) data.getValue("numAlternatives")).getInt();
                    if (n >= 1) {
                        this.numAlternatives.setValue(n);
                        // c_numAlternatives.setText(numAlternatives.getValueAsString());
                    } else {
                        this.println("Number of alternatives must be 1 or greater.");
                    }
                }

                if (data.containsLabel("cutoff")) {
                    float f = (float) ((RealValue) data.getValue("cutoff")).getReal();
                    if ((f >= 0.0) && (f <= 1.0)) {
                        this.cutoff.setValue(f);
                    } else {
                        this.println("Illegal threshold value: " + f);
                    }
                }

                if (data.containsLabel("threshold")) {
                    float f = (float) ((RealValue) data.getValue("threshold")).getReal();
                    if ((f >= 0.0) && (f <= 1.0)) {
                        this.cutoff.setValue(f);
                    } else {
                        this.println("Illegal threshold value: " + f);
                    }
                }

                if (data.containsLabel("distance")) {
                    float f = (float) ((RealValue) data.getValue("distance")).getReal();
                    if ((f >= 0.0) && (f <= 1.0)) {
                        this.distance.setValue(f);
                    } else {
                        this.println("Illegal distance value: " + f);
                    }
                }

                if (data.containsLabel("setParam")) {
                    StructValue param = (StructValue) data.getValue("setParam");
                    String name = ((StringValue) param.getValue("name")).getString();
                    Property<?> parameter = this.getRecognizer().getParameter(name);
                    if (parameter == null) {
                        throw new RecognizerException("Unknown parameter \"" + name + "\"");
                    }

                    Value value = param.getValue("value");
                    parameter.setValueFromString(value instanceof StringValue
                            ? ((StringValue) value).getString()
                            : value.toString());
                }

                if (data.containsLabel("domain")) {
                    String domainName
                            = ((StringValue) data.getValue("domain")).getString();
                    this.setDomain(domainName);
                }

                if (data.containsLabel("recognize")) {
                    String fileName
                            = ((StringValue) data.getValue("recognize")).getString();
                    this.startRecognition(new File(fileName));
                }

                if (data.containsLabel("batch")) {
                    StructValue batch = (StructValue) data.getValue("batch");
                    String in = ((StringValue) batch.getValue("in")).getString();
                    String out = ((StringValue) batch.getValue("out")).getString();

                    File inFile = new File(in);
                    XMLWriter outStream = new XMLWriter(new FileOutputStream(out));
                    try {
                        this.batchProcess(inFile, outStream, null);
                    } finally {
                        outStream.close();
                    }
                }
            }
        } catch (Throwable exn) {
            this.error(exn);
        }

        this.updateMenus();
    }

    private void setDomain(String domainName)
            throws SpeechException {

        Domain domain = this.getRecognizer().findDomain(domainName, false);
        if (domain != null) {
            this.getRecognizer().setDomain(domain);
        } else {
            this.println("Unknown domain \"" + domainName + "\"");
        }
    }

    public JComponent createUI() {

        JPanel c = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.weightx = 0.5;
        gbc.gridwidth = 2;

        this.output = new TextPane(this);
        c.add(this.output, gbc);
        gbc.gridy++;
        this.detail = new TextPane(null);
        c.add(this.detail, gbc);
        gbc.gridy++;

        this.optionsPanel = new JPanel(new GridBagLayout());
        this.optionsPanel.setBorder(new GroupBorder("Recognizer settings"));

        gbc.gridx = 0;
        gbc.weighty = 0;

        c.add(this.optionsPanel, gbc);

        gbc.gridy++;

        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(2, 3, 2, 3);
        c.add(this.indicator, gbc);
        gbc.gridx++;
        gbc.weightx = 1.0;

        this.partialResult = new JLabel();
        this.partialResult.setBorder(BorderFactory.createEtchedBorder());

        c.add(this.partialResult, gbc);

        this.mainPanel = new JPanel(new GridLayout());
        this.mainPanel.add(c);

        /*
     * if (treeView == null) toggleTreeView();
         */
        return this.mainPanel;
    }

    private Property<?>[] getParameters() {

        try {
            Property<?>[] params = this.recognizer.getProperties();
            Property<?>[] defaultParams
                    = new Property[]{this.domains, this.contexts, this.cutoff,
                        this.distance,
                        this.numAlternatives};
            // Property[] defaultParams = new Property[] { cutoff, distance,
            // numAlternatives };
            Property<?>[] parameters
                    = new Property[params.length + defaultParams.length];
            System.arraycopy(defaultParams, 0, parameters, 0, defaultParams.length);
            System.arraycopy(params, 0, parameters, defaultParams.length,
                    params.length);

            return parameters;
        } catch (Exception exn) {
            this.error(exn);
            return new Property[0];
        }
    }

    protected Value interpret(Utterance utterance,
            Map<String, Map<String, String>> substitutions)
            throws SpeechException {

        Value result;

        if (this.sendSemantics.getValue()) {
            Word[] input = new Word[utterance.length()];
            for (int i = 0; i < input.length; i++) {
                input[i] = utterance.getWord(i);
            }

            RecognitionContext context = this.getRecognizer().getContext();
            Grammar g = context.getGrammar();
            if (g != null) {
                ParseOptions options = new ParseOptions();
                options.buildParseTree = false;
                options.allowSparseParses = false;
                options.evaluateTags = true;

                options.dynamicVocabulary = substitutions;
                options.allowSparseParses = false;
                options.maxParses = ParseOptions.BEST_PARSE;

                result = g.match(input, g.getRoot(), options);
                if (result == null) {
                    options.allowSparseParses = true;
                    result = g.match(input, g.getRoot(), options);
                }
                if (result == null) {
                    this.println("Grammar doesn't match utterance \""
                            + Arrays.toString(input) + "\".");
                    // result = new StructValue(new String[0], new Value[0]);
                    result = new StringValue("");
                }
            } else {
                this.println("Grammar " + context.getName() + " not found.");
                result = new StringValue(utterance.getWords());
            }
        } else {
            result = new StringValue(utterance.getWords());
        }
        result.setAttribute("confidence", utterance.getConfidence());

        return result;
    }

    private void toggleTreeView() {

        Frame f = GUI.getFrameForComponent(this.getUI());
        if (this.treeView != null) {
            JSplitPane pane = (JSplitPane) this.mainPanel.getComponent(0);
            this.mainPanel.remove(pane);
            this.mainPanel.add(pane.getLeftComponent());
            if (f != null) {
                f.setSize(f.getWidth() - this.treeView.getWidth(), f.getHeight());
            }
            this.treeView = null;
        } else {
            this.treeView = new TreesView();
            Component ui = this.mainPanel.getComponent(0);
            this.mainPanel.remove(ui);
            JSplitPane pane = new JSplitPane();
            pane.setLeftComponent(ui);
            // mainPanel.add(treeView);
            pane.setRightComponent(this.treeView);
            this.mainPanel.add(pane);
            if (f != null) {
                f.setSize(f.getWidth() + this.getUI().getWidth(), f.getHeight());
            }
        }
        f.validate();
        f.repaint();
    }

    private void testVocabulary() {

        int fieldSize = 15;
        int numFields = 5;

        JPanel p = new JPanel(new GridLayout(0, 2, 6, 3));

        Collection<Rule> classes = new ArrayList<Rule>();
        try {
            Grammar g = this.recognizer.getContext().getGrammar();
            classes.addAll(g.getClasses());
            for (Grammar externalGrammar : g.getExternalGrammars()) {
                classes.addAll(externalGrammar.getClasses());
            }
            numFields = classes.size();

            if (numFields == 0) {
                OptionPane
                        .message(
                                this.getUI(),
                                "The currently active context does not contain any word classes.");
                return;
            }
        } catch (Exception exn) {
            classes = Collections.emptySet();
            numFields = 5;
        }

        p.add(new JLabel("Word class"));
        p.add(new JLabel("Words (comma separated)"));

        JTextField name[] = new JTextField[numFields];
        JTextField words[] = new JTextField[name.length];

        Iterator<Rule> classNames = classes.iterator();
        for (int i = 0; i < name.length; i++) {
            name[i] = new JTextField(fieldSize);
            p.add(name[i]);
            if (classNames.hasNext()) {
                name[i].setText(classNames.next().getName());
                name[i].setEditable(false);
                name[i].setEnabled(false);
            }
            words[i] = new JTextField(fieldSize);
            p.add(words[i]);
        }

        if (OptionPane.confirm(this.getUI(), p, "Test Vocabulary",
                OptionPane.OK_CANCEL_OPTION) == OptionPane.OK) {
            int count = 0;
            for (int i = 0; i < name.length; i++) {
                if (!StringTools.isEmpty(name[i].getText())) {
                    count++;
                }
            }

            StructValue addWords = null;
            if (count > 0) {
                String labels[] = new String[count];
                Value values[] = new Value[count];

                count = 0;
                for (int i = 0; i < name.length; i++) {
                    if (!StringTools.isEmpty(name[i].getText())) {
                        labels[count] = name[i].getText();
                        Collection<Value> ws = new ArrayList<Value>();
                        String w[]
                                = StringTools.split(words[i].getText(), new char[]{','});
                        for (int j = 0; j < w.length; j++) {
                            w[j] = StringTools.normalize(w[j]);
                            if (w[j].length() > 0) {
                                ws.add(new StringValue(w[j]));
                            }
                        }
                        values[count] = new ListValue(ws.toArray(new Value[ws.size()]));
                        count++;
                    }
                }
                addWords = new StructValue(labels, values);
            }

            try {
                this.activateContext(this.recognizer.getContext(), addWords, null);
            } catch (Exception exn) {
                exn.printStackTrace();
                OptionPane.error(this.getUI(), exn);
            }
        }
    }

    protected void batchProcess(final File batch, final XMLWriter output,
            ProgressListener l)
            throws IOException {

        List<String> files = new LinkedList<String>();
        BufferedReader r = new BufferedReader(new FileReader(batch));
        try {
            String line;
            while ((line = r.readLine()) != null) {
                files.add(line);
            }
        } finally {
            r.close();
        }

        ProgressEvent evt;

        output.openElement("batch_recognition", new String[]{"file"},
                new String[]{batch.getAbsolutePath()});

        int n = 0;
        for (String filename : files) {
            evt
                    = new ProgressEvent(this, "Processing " + filename + "...", 0, files
                            .size(), n++);
            if (l != null) {
                l.progressChanged(evt);
            }
            // first try as absolute path
            File waveFile = new File(filename);
            if (!waveFile.exists()) {
                // if the file doesn't exist, try relative to the batch file
                waveFile = new File(batch.getParentFile(), filename);
            }

            output.openElement("file", new String[]{"path"},
                    new String[]{waveFile.getAbsolutePath()});
            try {
                if (waveFile.exists()) {
                    this.processAudioFile(waveFile, output);
                } else {
                    throw new FileNotFoundException(filename);
                }
            } catch (Exception exn) {
                output.printElement("error", exn.toString());
            }
            output.closeElement("file");
        }
        output.closeElement("batch_recognition");
        output.flush();
        output.close();

    }

    protected final void activateGrammar(Reader r, StructValue addWords,
            ListValue phonetics)
            throws Exception {

        Grammar g = Grammar.create(r);
        RecognitionContext ctx
                = this.recognizer.createTemporaryContext(g, this.recognizer.getDomain());

        this.activateContext(ctx, addWords, phonetics);
        this.updateMenus();
    }

    protected final void activateContext(String name, StructValue addWords,
            ListValue phonetics)
            throws SpeechException {

        if (this.recognizer != null) {
            if (this.recognizer.getDomain() == null) {
                throw new RecognizerException("You must choose a domain first");
            }

            RecognitionContext ctx = this.recognizer.getDomain().findContext(name);
            if (ctx == null) {
                throw new RecognizerException("Unknown context \"" + name
                        + "\" in domain "
                        + this.recognizer.getDomain().getName());
            }
            this.activateContext(ctx, addWords, phonetics);
        } else {
            throw new RecognizerException("Recognizer not initialized.");
        }
    }

    protected final void activateContext(RecognitionContext ctx,
            StructValue addWords,
            ListValue phonetics)
            throws SpeechException {

        if ((addWords != null) && (addWords.size() == 0)) {
            addWords = null;
        }
        if ((phonetics != null) && (phonetics.size() == 0)) {
            phonetics = null;
        }

        if (ctx == null) {
            this.recognizer.setContext((RecognitionContext) null);
            this.lastAddWords = addWords;
            this.lastPhonetics = phonetics;
            this.substitutions.clear();
        } else {
            RecognitionContext activeContext = this.recognizer.getContext();
            if ((activeContext == null)
                    || !activeContext.equals(ctx)
                    || ((addWords != null) && ((this.lastAddWords == null) || !addWords
                            .equals(this.lastAddWords)))
                    || ((addWords == null) && (this.lastAddWords != null))
                    || ((phonetics != null) && ((this.lastPhonetics == null) || !phonetics
                            .equals(this.lastPhonetics)))
                    || ((phonetics == null) && (this.lastPhonetics != null))) {
                this.recognizer.setContext((RecognitionContext) null);
                this.lastAddWords = null;
                this.lastPhonetics = null;
                this.substitutions.clear();

                this.activateContextImpl(ctx, addWords, phonetics);
                this.lastAddWords = addWords;
                this.lastPhonetics = phonetics;

                this.updateMenus();
            }
        }
    }

    protected abstract void activateContextImpl(RecognitionContext context, StructValue addWords, ListValue phonetics)
            throws SpeechException;

    public final boolean initialize(ProgressListener progress, String[] args,
            Properties properties) {

        try {
            this.initRecognizer(properties, progress);
        } catch (Exception exn) {
            this.error(exn);
            return false;
        }

        this.simulation = new SimulationDialog(this.getUI());

        String property = properties.getProperty("Simulate");
        if (property != null) {
            this.simulate.setValue(property.equalsIgnoreCase("true"));
        }

        property = properties.getProperty("Semantics");
        if (property != null) {
            this.sendSemantics.setValue(property.equalsIgnoreCase("true"));
        }

        property = properties.getProperty("Logging");
        if (property != null) {
            this.logging.setValue(property.equalsIgnoreCase("true"));
        }

        property = properties.getProperty("Threshold");
        if (property != null) {
            this.cutoff.setValue(Float.parseFloat(property));
        }

        property = properties.getProperty("Distance");
        if (property != null) {
            this.distance.setValue(Float.parseFloat(property));
        }

        property = properties.getProperty("Alternatives");
        if (property != null) {
            this.numAlternatives.setValue(Integer.parseInt(property));
        }

        property = properties.getProperty("ShowLevels");
        if (property != null) {
            this.showLevels.setValue(property.equalsIgnoreCase("true"));
        }

        property = properties.getProperty("ShowTrees");
        if (property != null) {
            if (property.equalsIgnoreCase("true") == (this.treeView == null)) {
                this.toggleTreeView();
            }
        }

        if (this.initializeImpl(progress, args, properties)) {
            try {
                if (progress != null) {
                    progress.progressChanged(new ProgressEvent(this, "Loading contexts",
                            0, 0, 0));
                }
                this.getRecognizer().loadInternalGrammars("ISO-8859-1");
            } catch (Exception exn) {
                this.error(exn);
            }

            GridBagConstraints gbc = new GridBagConstraints();
            this.levelMeter.setBorder(BorderFactory.createLoweredBevelBorder());
            gbc.gridx = gbc.gridy = 0;
            gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.EAST;
            gbc.weightx = 0.0;
            // optionsPanel.add(new JLabel(showLevels.getName(), JLabel.RIGHT), gbc);
            this.optionsPanel.add(this.showLevels.createEditor(true), gbc);
            gbc.gridx++;
            gbc.weightx = 1.0;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(3, 3, 3, 3);
            // gbc.fill = GridBagConstraints.HORIZONTAL;
            this.optionsPanel.add(this.levelMeter, gbc);

            gbc.gridy++;
            gbc.gridx = 0;
            Property<?>[] parameters = this.getParameters();
            new PropertySet<Property<?>>(parameters).fillPropertyPanel(
                    this.optionsPanel, gbc, false);

            for (int i = 0; i < parameters.length; i++) {
                parameters[i].addChangeListener(new ChangeListener() {

                    public void stateChanged(ChangeEvent e) {

                        RecognizerClient.this.updateMenus();
                    }
                });
            }

            try {
                if (this.recognizer.getContext() == null) {
                    Domain domain = null;

                    boolean showContextChooser
                            = properties.getProperty("ContextChooser", "false").equalsIgnoreCase(
                                    "true");

                    property = properties.getProperty("Domain");
                    if (property != null) {
                        domain = this.recognizer.findDomain(property, false);
                    }

                    if (domain == null) {
                        Domain[] domains = this.recognizer.getDomains();
                        if (domains.length == 1) {
                            domain = domains[0];
                        } else if (domains.length > 0) {
                            if (showContextChooser) {
                                domain
                                        = new ListSelectionDialog<Domain>(this.getUI(), "Domain",
                                                "Please choose a domain", domains)
                                                .getSelectedItem();
                            } else {
                                domain = domains[0];
                            }
                        }
                    }
                    this.domains.setValue(domain);

                    if (domain != null) {
                        RecognitionContext context = null;
                        property = properties.getProperty("Context");
                        if (property != null) {
                            context = domain.findContext(property);
                        }

                        if (context == null) {
                            RecognitionContext[] contexts = domain.getContexts();
                            if (contexts.length == 1) {
                                context = contexts[0];
                            } else if (contexts.length > 0) {
                                if (showContextChooser) {
                                    context
                                            = new ListSelectionDialog<RecognitionContext>(this.getUI(),
                                                    "Context", "Please choose a context",
                                                    contexts).getSelectedItem();
                                } else {
                                    context = contexts[0];
                                }
                            }
                        }
                        this.contexts.setValue(context);
                    } else {
                        this.contexts.setValue(null);
                    }
                }
            } catch (Exception ignore) {
            }

            this.updateMenus();
            return true;
        } else {
            return false;
        }
    }

    protected abstract boolean initializeImpl(ProgressListener progress,
            String[] args,
            Properties properties);

    private boolean updatingMenus = false;

    public void updateMenus() {

        super.updateMenus();

        if (!this.updatingMenus) {
            this.updatingMenus = true;
            this.domains.update();
            this.contexts.update();
            this.updatingMenus = false;
        }
    }

    private void addCheckboxItem(CmdMenu m, final BooleanProperty p) {

        CmdCheckBoxMenuItem item = new CmdCheckBoxMenuItem(p.getName(), 1, null,
                new MenuCommander() {

            public boolean menuItemState(int cmd) {

                return p.getValue();
            }

            public String menuItemName(int cmd, String oldName) {

                return p.getName();
            }

            public boolean doCommand(int cmd) {

                p.setValue(!p.getValue());
                RecognizerClient.this.updateMenus();
                return true;
            }

        });
        item.setToolTipText(p.getDescription());
        m.add(item);
    }

    protected void initMenu(CmdMenu m) {

        // m.addItem("PTT", cmdPTT, KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0));
        m.addItem("Start recognizer", RecognizerClient.cmdStart, KeyEvent.VK_S);
        m.addItem("Process sound file...", RecognizerClient.cmdProcessFile,
                KeyStroke.getKeyStroke(KeyEvent.VK_S,
                        m.getToolkit().getMenuShortcutKeyMask() + KeyEvent.SHIFT_MASK));
        m.addItem("Batch process files...", RecognizerClient.cmdProcessFiles);
        m.addItem("Run offline test...", RecognizerClient.cmdRunOfflineTest);

        m.addSeparator();
        m.addItem("Try grammar...", RecognizerClient.cmdNewGrammar, KeyEvent.VK_N);
        m.addItem("Load external grammar...", RecognizerClient.cmdOpenGrammar,
                KeyEvent.VK_O);
        m.addItem("Load dynamic vocabulary...", RecognizerClient.cmdTestVocabulary,
                KeyEvent.VK_D);

        super.initMenu(m);

        // m.addSeparator();
        m.addItem("Show additional parameters", RecognizerClient.cmdShowParameters);
        m.add(new CmdCheckBoxMenuItem("Show result trees",
                RecognizerClient.cmdTreeView, null, this));

        // CmdMenu options = m;
        CmdMenu options = m.addSubMenu("Options");
        this.addCheckboxItem(options, this.logging);
        this.addCheckboxItem(options, this.simulate);
        this.addCheckboxItem(options, this.autoStart);
        this.addCheckboxItem(options, this.sendSemantics);
        this.addCheckboxItem(options, this.sendAlternatives);
        this.addCheckboxItem(options, this.sendAlternativesAsList);
        this.addCheckboxItem(options, this.ignoreEmptyRecognition);
    }

    public boolean menuItemState(int cmd) {

        switch (cmd) {
            case cmdStart:
                if (this.simulate.getValue()) {
                    return true;
                } else if (this.recognizer != null) {
                    try {
                        return this.recognizer.getContext() != null;
                    } catch (Exception exn) {
                        return false;
                    }
                } else {
                    return false;
                }

            case cmdProcessFile:
            case cmdProcessFiles:
                try {
                    return this.recognizer.supportsOfflineRecognition()
                            && !this.recognizer.isActive()
                            && (this.recognizer.getContext() != null);
                } catch (Exception exn) {
                    return false;
                }

            case cmdRunOfflineTest:
                try {
                    return this.recognizer.supportsOfflineRecognition()
                            && !this.recognizer.isActive();
                } catch (Exception exn) {
                    return false;
                }

            case cmdNewGrammar:
            case cmdOpenGrammar:
                return this.recognizer != null;

            case cmdTestVocabulary:
                try {
                    return this.recognizer.getContext() != null;
                } catch (Exception exn) {
                    return false;
                }

            case cmdShowParameters:
                return this.getRecognizer() != null;

            case cmdTreeView:
                return this.treeView != null;

            case cmdPTT:
                return true;

            default:
                return super.menuItemState(cmd);
        }
    }

    public String menuItemName(int cmd, String oldName) {

        switch (cmd) {
            case cmdStart:
                if (this.recognizer != null) {
                    try {
                        return this.recognizer.isActive() ? "Stop recognizer"
                                : "Start recognizer";
                    } catch (Exception exn) {
                        return oldName;
                    }
                } else if (this.simulate.getValue()) {
                    return ((this.simulation != null) && this.simulation.isShowing())
                            ? "Stop recognizer"
                            : "Start recognizer";
                } else {
                    return "Recognizer not available";
                }

            case cmdProcessFile:
            case cmdProcessFiles:
            case cmdRunOfflineTest:
            case cmdNewGrammar:
            case cmdOpenGrammar:
            case cmdTestVocabulary:
            case cmdTreeView:
            case cmdShowParameters:
                return oldName;

            default:
                return super.menuItemName(cmd, oldName);
        }
    }

    public boolean doCommand(int cmd) {

        boolean cmdHandled = true;

        switch (cmd) {
            case cmdPTT:
                try {
                    this.send(new StructValue(new String[]{"PTT"},
                            new Value[]{new BoolValue(true)}));
                } catch (Exception exn) {
                    this.error(exn);
                }
                break;

            case cmdStart:
                try {
                    if (this.isActive()) {
                        this.stopRecognition();
                    } else {
                        new Thread(new Runnable() {

                            public void run() {

                                RecognizerClient.this.startRecognition(null);
                            }
                        }).start();
                    }
                } catch (Throwable exn) {
                    this.error(exn);
                }
                break;

            case cmdProcessFile:
                try {
                    if (this.isActive()) {
                        this.stopRecognition();
                    }
                    final File f
                            = this.getFileChooser().standardGetFile(this.getUI(),
                                    "Choose a sound file");
                    if (f != null) {
                        new Thread(new Runnable() {

                            public void run() {

                                RecognizerClient.this.startRecognition(f);
                            }
                        }).start();
                    }
                } catch (Exception exn) {
                    this.error(exn);
                }
                break;

            case cmdProcessFiles:
                try {
                    final File f
                            = this.getFileChooser().standardGetFile(this.getUI(),
                                    "Choose a batch file");
                    if (f != null) {
                        StringWriter text = new StringWriter();
                        final XMLWriter results = new XMLWriter(text);
                        ProgressDialog d = new ProgressDialog(this.getUI());
                        d.run(new DefaultLongAction("Batch recognition") {

                            public void run(ProgressListener l)
                                    throws IOException {

                                RecognizerClient.this.batchProcess(f, results, l);
                            }
                        });
                        JTextArea a = new JTextArea();
                        a.setWrapStyleWord(true);
                        a.setLineWrap(true);
                        a.setText(text.toString());
                        JScrollPane jsp = new JScrollPane(a);
                        jsp.setPreferredSize(new Dimension(400, 250));
                        String options[] = {"Close", "Save"};

                        if (OptionPane.showOptionDialog(this.getUI(), jsp,
                                "Batch log file",
                                OptionPane.OK_CANCEL_OPTION,
                                OptionPane.INFORMATION, null, options,
                                options[0]) == 1) {
                            File save
                                    = this.getFileChooser().standardPutFile(this.getUI(),
                                            "Results.xml");
                            if (save != null) {
                                FileWriter w = new FileWriter(save);
                                w.write(results.toString());
                                w.close();
                            }
                        }
                    }
                } catch (InvocationTargetException exn) {
                    this.error(exn.getTargetException());
                } catch (Exception exn) {
                    this.error(exn);
                }
                break;

            case cmdRunOfflineTest:
                try {
                    File testFile = this.fileChooser.standardGetFile(this.getUI());
                    if (testFile != null) {
                        final OptimizeParameters test = OptimizeParameters.load(testFile);

                        StringWriter text = new StringWriter();
                        final XMLWriter results = new XMLWriter(text);
                        ProgressDialog d = new ProgressDialog(this.getUI());
                        d.run(new DefaultLongAction("Offline test") {

                            public void run(ProgressListener l)
                                    throws SpeechException {

                                Collection<Sample> samples
                                        = test.run(RecognizerClient.this.getRecognizer(), l);
                                results.openElement("test");

                                for (Sample sample : samples) {
                                    sample.print(results);
                                }

                                results.closeElement("test");
                                results.close();
                            }
                        });
                        JTextArea a = new JTextArea();
                        a.setWrapStyleWord(true);
                        a.setLineWrap(true);
                        a.setText(text.toString());
                        JScrollPane jsp = new JScrollPane(a);
                        jsp.setPreferredSize(new Dimension(400, 250));
                        String options[] = {"Close", "Save"};

                        if (OptionPane.showOptionDialog(this.getUI(), jsp,
                                "Batch log file",
                                OptionPane.OK_CANCEL_OPTION,
                                OptionPane.INFORMATION, null, options,
                                options[0]) == 1) {
                            File save
                                    = this.getFileChooser().standardPutFile(this.getUI(),
                                            "Results.xml");
                            if (save != null) {
                                FileWriter w = new FileWriter(save);
                                w.write(results.toString());
                                w.close();
                            }
                        }
                    }
                } catch (InvocationTargetException exn) {
                    this.error(exn.getTargetException());
                } catch (Exception exn) {
                    this.error(exn);
                }
                break;

            case cmdNewGrammar:
                if (this.temporaryGrammar == null) {
                    this.temporaryGrammar = new JTextArea();
                    this.temporaryGrammar.setLineWrap(true);
                    this.temporaryGrammar.setWrapStyleWord(true);

                    this.temporaryGrammar.setText("root $main;\n\npublic $main = ;");
                }
                int confirm
                        = OptionPane.confirm(this.getUI(), new JScrollPane(
                                this.temporaryGrammar) {

                            public Dimension getPreferredSize() {

                                return new Dimension(500, 300);
                            }
                        }, "Test grammar", OptionPane.OK_CANCEL_OPTION);
                if (confirm == OptionPane.OK) {
                    try {
                        this.activateGrammar(new StringReader(this.temporaryGrammar
                                .getText()), null, null);
                    } catch (Exception exn) {
                        OptionPane.error(this.getUI(), exn);
                    }
                }

                break;

            case cmdOpenGrammar:
                try {
                    File f = this.fileChooser.standardGetFile(this.getUI());
                    if (f != null) {
                        Reader r = new PreprocessingReader(f);
                        try {
                            this.activateGrammar(r, null, null);
                        } finally {
                            r.close();
                        }
                    }
                } catch (ThreadDeath d) {
                    throw d;
                } catch (Throwable exn) {
                    OptionPane.error(this.getUI(), exn);
                }
                break;

            case cmdTestVocabulary:
                this.testVocabulary();
                break;

            case cmdTreeView:
                this.toggleTreeView();
                break;

            case cmdShowParameters:
                try {
                    if (this.parameterWindow == null) {
                        final Property<?>[] parameters
                                = this.getRecognizer().getParameters();
                        this.parameterWindow = new JFrame("Recognizer parameters");
                        JPanel p = new JPanel(new BorderLayout());
                        p.add(Property.createPropertyPanel(parameters, false),
                                BorderLayout.CENTER);
                        JButton b = new CmdButton(new Runnable() {

                            public void run() {

                                try {
                                    RecognizerClient.this.getRecognizer().optimizeParameters();
                                } catch (Exception exn) {
                                    OptionPane.error(
                                            RecognizerClient.this.parameterWindow, exn);
                                }
                            }
                        }, "Reset to optimal values");
                        p.add(b, BorderLayout.SOUTH);
                        this.parameterWindow.setContentPane(p);
                        this.parameterWindow
                                .setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                        this.parameterWindow.pack();
                    }
                    this.parameterWindow.setVisible(!this.parameterWindow.isVisible());
                } catch (Exception exn) {
                    OptionPane.error(this.getUI(), exn);
                }
                return true;

            default:
                cmdHandled = super.doCommand(cmd);
                break;
        }

        this.updateMenus();
        return cmdHandled;
    }

    protected Property<?>[] getProperties() {

        return new Property[]{this.numAlternatives, this.cutoff, this.distance};
    }
}
