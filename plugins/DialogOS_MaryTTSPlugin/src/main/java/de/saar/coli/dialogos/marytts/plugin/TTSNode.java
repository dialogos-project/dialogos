package de.saar.coli.dialogos.marytts.plugin;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.*;

import com.clt.diamant.*;
import com.clt.speech.tts.VoiceName;
import de.saar.coli.dialogos.marytts.MaryTTS;
import org.xml.sax.SAXException;

import com.clt.diamant.GroovyVariable;
import com.clt.diamant.graph.Graph;
import com.clt.diamant.graph.Node;
import com.clt.diamant.graph.nodes.NodeExecutionException;
import com.clt.diamant.graph.search.SearchResult;
import com.clt.diamant.gui.NodePropertiesDialog;
import com.clt.gui.GUI;
import com.clt.gui.OptionPane;
import com.clt.script.exp.EvaluationException;
import com.clt.script.exp.Expression;
import com.clt.script.exp.Value;
import com.clt.script.exp.values.StringValue;
import com.clt.speech.SpeechException;
import com.clt.speech.tts.Synthesizer;
import com.clt.speech.tts.Voice;
import com.clt.util.StringTools;
import com.clt.xml.XMLReader;
import com.clt.xml.XMLWriter;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

/**
 * This is the implementation of the text-to-speech node for the
 * Mary-synthesizer. It includes the following configurations: + prompt-type:
 * Either text, expression or maryxml. (Enum PromptType, GUI: Radio buttons) +
 * prompt: User text to be spoken; evaluated according to prompt-type. (String,
 * GUI: Text field) + voice (Voicename, GUI: ComboBox) + wait: Wait for that to
 * be spoken to evaluate further TTSNodes (Boolean, GUI: checkbox) + xml-area:
 * XML that is generated from what's in the prompt and Settings configs.
 * (String, GUI: Text field) The configurations are stored in a property map
 * (See: DefaultPropertyContainer, which is extended by TTSNode). The GUIs are
 * created with the help of the NodePropertiesDialog. Note: When the prompt is
 * of type MaryXML the configurations in Settings are ignored.
 *
 * @author Nicolas and Phil (taking over the TTSNode class of the old Realspeak
 * Plugin by Daniel Bobbert)
 * @author Till and Bri (Groovy functionality) TODO : This recasting to MaryTTS
 * every time we dont know how to call it doesnt make sense! Change that!
 */
public class TTSNode extends Node {

    private static final String PROMPT_TYPE = "prompt_type";
    private static final String PROMPT = "prompt";
    private static final String VOICE = "voice";
    private static final String WAIT = "wait";

    //private static final String EXPRESSION = "expression";//TODO Expr checkbox (?)
    private static final String XMLAREA = "xmlarea";

    public static enum PromptType {
        text("Text"),
        maryxml("MaryXML"),
        expression("Expression"),//TODO Expr as promptType or as checkbox?
        groovy("GroovyScript");

        private String key;

        private PromptType(String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return Resources.getString(this.key);
        }
    }

    public TTSNode() {

        this.setProperty(TTSNode.PROMPT_TYPE, PromptType.text);
        this.setProperty(TTSNode.PROMPT, "");
        this.setProperty(TTSNode.WAIT, Boolean.TRUE);

        this.setProperty(TTSNode.VOICE, new VoiceName("", null));
        this.setProperty(TTSNode.XMLAREA, "");
        //this.setProperty(TTSNode.EXPRESSION, Boolean.FALSE);//TODO Expr checkbox (?)
        this.addEdge();
    }

    public static Color getDefaultColor() {

        return Color.GREEN.darker();
    }

    @Override
    protected JComponent createEditorComponent(
            final Map<String, Object> properties) {

        List<VoiceName> voices = Plugin.getAvailableVoices();
        voices.add(0, new VoiceName("", null));

        if (!voices.contains(properties.get(TTSNode.VOICE))) {
            voices.add(1, (VoiceName) properties.get(TTSNode.VOICE));
        }

        Component standardEditor = NodePropertiesDialog.createTextArea(properties, TTSNode.PROMPT);
        Component groovyEditor = NodePropertiesDialog.createGroovyScriptEditor(properties, TTSNode.PROMPT);

        //Create first tab (User text)
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gbc.gridy = 0;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 3, 3, 3);

        p.add(new JLabel(Resources.getString("Voice") + ':'), gbc);
        gbc.gridx++;
        p.add(NodePropertiesDialog
                .createComboBox(properties, TTSNode.VOICE, voices), gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weightx = 0.0;

        p.add(new JLabel(Resources.getString("PromptType") + ':'), gbc);
        gbc.gridx++;
        gbc.weightx = 1.0;
        JPanel types = new JPanel(new FlowLayout(FlowLayout.LEFT));
        for (JRadioButton button : NodePropertiesDialog.createRadioButtons(
                properties, TTSNode.PROMPT_TYPE,
                PromptType.values())) {
            button.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (button.isSelected()) {
                        if (button.getText().equals(PromptType.groovy.toString())) {
                            groovyEditor.setVisible(true);
                            standardEditor.setVisible(false);
                        } else {
                            standardEditor.setVisible(true);
                            groovyEditor.setVisible(false);
                        }
                    }
                }
            });
            types.add(button);
        }
        p.add(types, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        p.add(new JLabel(Resources.getString("Prompt") + ':'), gbc);
        gbc.gridx++;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;

        p.add(standardEditor, gbc);
        p.add(groovyEditor, gbc);

        if (properties.get(TTSNode.PROMPT_TYPE).equals(PromptType.groovy)) {
            standardEditor.setVisible(false);
        } else {
            groovyEditor.setVisible(false);
        }

        //TODO Expr checkbox (?)
//    gbc.gridy++;
//    gbc.gridx = 0;
//    gbc.gridwidth = 2;
//    gbc.weighty = 0.0;
//    gbc.fill = GridBagConstraints.NONE;
//    gbc.anchor = GridBagConstraints.WEST;
//    p.add(NodePropertiesDialog.createCheckBox(properties, TTSNode.EXPRESSION,
//            Resources.getString("Expression")), gbc);
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;

        p.add(NodePropertiesDialog.createCheckBox(properties, TTSNode.WAIT,
                Resources.getString("WaitUntilDone")), gbc);

        gbc.gridy++;
        gbc.anchor = GridBagConstraints.CENTER;

        JPanel border = new JPanel(new BorderLayout());
        final JButton tryPrompt = new JButton(Resources.getString("Try"));
        tryPrompt.addActionListener(new ActionListener() {

            boolean speaking = false;

            private void reset() {

                TTSNode.this.stopSynthesis();
                this.speaking = false;
                tryPrompt.setText(Resources.getString("Try"));
            }

            public void actionPerformed(ActionEvent e) {

                if (this.speaking) {
                    this.reset();
                } else {
                    new Thread(new Runnable() {

                        public void run() {

                            try {
                                speaking = true;
                                tryPrompt.setText(GUI.getString("Cancel"));
                                TTSNode.this.speak(properties);
                                //This Actionlistener is not necessary: when an action is performed
                                //again it will check the this.speaking variable and reset (if it's speaking)
                                //This works because audioplayer which is to be stopped is a thread.
//                tryPrompt.addActionListener(
//                        ae -> {stopSynthesis();}
//                );
                            } catch (Exception exn) {
                                String msg = exn.getLocalizedMessage();
                                if ((msg == null) || (msg.length() == 0)) {
                                    msg = exn.getClass().getName();
                                }
                                OptionPane.error(tryPrompt, msg);
                            }
                            reset();
                        }
                    }).start();
                }
            }
        });

        border.add(tryPrompt, BorderLayout.CENTER);
        border.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        p.add(border, gbc);

        //Create second tab for maryXML
        JPanel p2 = new JPanel(new GridBagLayout());
        p2.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.gridx = gbc2.gridy = 0;
        gbc2.weightx = 0.0;
        gbc2.weighty = 0.0;
        gbc2.fill = GridBagConstraints.HORIZONTAL;
        gbc2.insets = new Insets(3, 3, 3, 3);
        p2.add(new JLabel(Resources.getString("Prompt") + ':'), gbc2);

        gbc2.gridx++;
        gbc2.weightx = 1.0;
        gbc2.weighty = 1.0;
        gbc2.fill = GridBagConstraints.BOTH;

        // We will create our own JScrollPane instead of taking one from NodePropertiesDialog
        // because we need a reference on the textArea contained in JScrollPane. (Text area in XML tab).
        JTextArea jta = NodePropertiesDialog.createTextAreaWithoutJScrollPane(properties, TTSNode.XMLAREA);

        JScrollPane jspane = new JScrollPane(jta, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jspane.setMinimumSize(new Dimension(300, 150));
        jspane.setPreferredSize(jspane.getMinimumSize());

        p2.add(jspane, gbc2);

        gbc2.gridx++;
        gbc2.weightx = 1.0;
        gbc2.weighty = 1.0;
        gbc2.fill = GridBagConstraints.BOTH;
        final JButton refreshB = new JButton(Resources.getString("Prompt to XML"));
        refreshB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String text = (String) properties.get(TTSNode.XMLAREA);
                String strXML = ((MaryTTS) Plugin.getSynthesizer()).createMaryXMLStr(text);
                properties.replace(TTSNode.XMLAREA, strXML); //TODO Necessary?
                jta.setText(strXML);
            }
        });
        //Add buttons
        JPanel border2 = new JPanel(new BorderLayout());
        border2.add(refreshB, BorderLayout.CENTER);
        border2.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        p2.add(border2, gbc);

        //Add tabs
        JTabbedPane jtp = new JTabbedPane();
        jtp.addTab("Speech Synthesis", p);
        jtp.addTab("MaryXML", p2);

        return jtp;
    }

    @Override
    public boolean editProperties(Component parent) {

        boolean result = super.editProperties(parent);

        this.stopSynthesis();

        return result;
    }

    /*
  * stopSynthesis: Stops the Synthesis midway.
     */
    void stopSynthesis() {

        try {
            Plugin.getSynthesizer().stop();
        } catch (Exception ignore) {
        }
    }

    /*
  * speak: Given a properties map (configurations of the node) speaks what's
  * in the prompt (possibly according to the settings).
  * */
    private void speak(Map<String, Object> properties)
            throws SpeechException {

        //Note: Regardless of prompt-type selected (whether text or maryxml) we need to get
        // the prompt (second tab). XMLAREA is in 3rd tab and used only for displaying
        // xml of the prompt in the second tab.
        String prompt = (String) properties.get(TTSNode.PROMPT);
        if (StringTools.isEmpty(prompt)) {
            throw new IllegalArgumentException(Resources.getString("MissingPrompt"));
        }
        //Expression ?
        if (PromptType.expression.equals(properties.get(TTSNode.PROMPT_TYPE))) {

            if (PromptType.maryxml.equals(properties.get(TTSNode.PROMPT))) {
                //TODO: How do we recognize variables and the like?
                //Document xml = XMLHelper.str2Document(prompt);
            }
            Expression exp;
            try {
                exp = this.parseExpression(prompt);
            } catch (Exception exn) {
                throw new EvaluationException(Resources.format("ExpressionParseError",
                        prompt)
                        + ": " + exn.getLocalizedMessage());
            }
            Value result = exp.evaluate();
            if (result instanceof StringValue) {
                prompt = ((StringValue) result).getString();
            } else {
                throw new IllegalArgumentException(Resources
                        .getString("PromptMustBeString"));
            }
        } else if (PromptType.groovy.equals(properties.get(TTSNode.PROMPT_TYPE))) {
            prompt = evaluateGroovyScript(prompt);
            System.out.println(prompt);
        }

        Settings settings
                = (Settings) this.getGraph().getOwner().getPluginSettings(Plugin.class);

        // Till and Bri's (from the merge)
        //    VoiceName voicename = (VoiceName)properties.get(TTSNode.VOICE);
        //    if ((voicename == null) || StringTools.isEmpty(voicename.getName())) {
        //      voicename = settings.getDefaultVoice();
        //    }
        //    TTSNode.speak(settings, voicename, prompt);
        Synthesizer synthesizer = Plugin.getSynthesizer();
        VoiceName voicename = (VoiceName) properties.get(TTSNode.VOICE);
        setVoice(settings, synthesizer, voicename);
        ((MaryTTS) synthesizer).setProsody2MaryXML(settings.getStrVolume(), settings.getPitch(), settings.getSpeed());
        //Should the audio be played uninterrumped?
        boolean wait = ((Boolean) properties.get(TTSNode.WAIT)).booleanValue();
        PromptType pType = (PromptType) properties.get(TTSNode.PROMPT_TYPE);
        //Prompt is maryxml or text?
        if (PromptType.maryxml.equals(pType)) {
            //Note: Regardless of the configurations in Settings it will
            //speak according to the configs in the prompt (xml)
            ((MaryTTS) synthesizer).speakStrMaryXML(prompt, wait);
        } else { //text
            ((MaryTTS) synthesizer).speak(prompt, wait);
        }
    }

    private String evaluateGroovyScript(String groovyScript) {
        // Create new Binding and GroovyShell
        Binding sharedData = new Binding();
        GroovyShell gShell = new GroovyShell(sharedData);

        // get list of all variables(slots) from graph
        List<Slot> allVars = this.getGraph().getAllVariables(Graph.LOCAL);
        List<GroovyVariable> groovyVars = this.getGraph().getAllGroovyVariables();

        // put pre existing global variables in the binding
        for (Slot var : allVars) {
            String varName = var.getName();
            Object varValue = var.getValue().getReadableValue();
            sharedData.setProperty(varName, varValue);
        }
        for (GroovyVariable var : groovyVars) {
            String varName = var.getName();
            Object varValue = var.getValue();
            sharedData.setProperty(varName, varValue);
        }

        String functions = this.getGraph().getGroovyScript();
        functions = functions + "\n";
        // the return value of the script
        Object result;
        try {
            // Appends global groovy functions to the top of the script,
            // so they can be used by the script in this node
            groovyScript = functions + groovyScript;
            // execute the script
            result = gShell.evaluate(groovyScript);
        } catch (Exception exn) {
            throw new NodeExecutionException(this, "Illegal Groovy script", exn);
        }

        // get variables from Binding
        @SuppressWarnings("unchecked")
        Map<String, Object> newVars = (Map<String, Object>) sharedData.getVariables();
        // update global variables
        try {
            for (Slot slot : allVars) {
                if (!slot.getValue().equals(Value.of(newVars.get(slot.getName())))) {
                    slot.setValue(Value.of(newVars.get(slot.getName())));
                }
            }
            for (GroovyVariable entry : groovyVars) {
                if (!entry.getValue().equals(newVars.get(entry.getName()))) {
                    entry.setValue(newVars.get(entry.getName()));
                }
            }
        } catch (EvaluationException e) {
            //TODO localize Exception
            throw new NodeExecutionException(this, "Can't change type of global variables in Groovy script", e);
        } catch (Exception e) {
            throw new NodeExecutionException(this, "Can't change type of global variables in Groovy script", e);
        }
        System.out.println("result" + result);
        String prompt;
        try {
            prompt = (String) result;
        } catch (Exception e) {
            throw new IllegalArgumentException(Resources.getString("PromptMustBeString"));
        }
        return prompt;
    }

    /*
  * speak: Speaks whats in prompt according to the given settings and voicename.
  * */
    static void speak(Settings settings, VoiceName voicename, String prompt)
            throws SpeechException {
        Synthesizer synthesizer = Plugin.getSynthesizer();
        setVoice(settings, synthesizer, voicename);
        ((MaryTTS) synthesizer).setProsody2MaryXML(settings.getStrVolume(), settings.getPitch(), settings.getSpeed());
        synthesizer.speak(prompt);
    }

    /*
  * setVoice: Sets the given voicename to the given synthesizer.
  * */
    private static void setVoice(Settings settings, Synthesizer synthesizer, VoiceName voicename)
            throws SpeechException {

        if ((voicename == null) || StringTools.isEmpty(voicename.getName())) {
            voicename = settings.getDefaultVoice();
        }
        //Set up voice:
        Voice voice = synthesizer.findVoice(voicename.getName());
        if (voice == null) {
            throw new SpeechException(Resources.format("VoiceNotFound",
                    voicename.getNormalizedName()));
        }
        synthesizer.setVoice(voice);
    }

    public Node execute(WozInterface comm, InputCenter input, ExecutionLogger logger) {
        logNode(logger);
        try {
            TTSNode.this.speak(TTSNode.this.properties);
        } catch (SpeechException e) {
            e.printStackTrace();
        }
        return this.getEdge(0).getTarget();
    }

    @Override
    protected void writeVoiceXML(XMLWriter w, IdMap uid_map) {

    }

    @Override
    public void validate(Collection<SearchResult> errors) {

        super.validate(errors);

        String prompt = (String) this.getProperty(TTSNode.PROMPT);
        PromptType promptType = (PromptType) this.getProperty(TTSNode.PROMPT_TYPE);

        if (promptType == null) {
            this.reportError(errors, false, Resources.getString("MissingPromptType"));
        }
        if (StringTools.isEmpty(prompt)) {
            this.reportError(errors, false, Resources.getString("MissingPrompt"));
        }
    }

    @Override
    protected void readAttribute(XMLReader r, String name, String value,
            IdMap uid_map)
            throws SAXException {

        if (name.equals(TTSNode.PROMPT)) {
            this.setProperty(name, value);
        } else if (name.equals(TTSNode.VOICE)) {
            this.setProperty(name, new VoiceName(value, null));
            List<VoiceName> voices = Plugin.getAvailableVoices();
            for (VoiceName voice : voices) {
                if (voice.getName().equals(value)) {
                    this.setProperty(name, voice);
                    break;
                }
            }
        } else if (name.equals(TTSNode.PROMPT_TYPE)) {
            for (PromptType type : PromptType.values()) {
                if (type.name().equals(value)) {
                    this.setProperty(TTSNode.PROMPT_TYPE, type);
                    break;
                }
            }
        } else if (name.equals(TTSNode.WAIT)) {
            this.setProperty(TTSNode.WAIT, value.equals("1") ? Boolean.TRUE
                    : Boolean.FALSE);
        } else {
            super.readAttribute(r, name, value, uid_map);
        }
    }

    @Override
    protected void writeAttributes(XMLWriter out, IdMap uid_map) {

        super.writeAttributes(out, uid_map);

        VoiceName voice = (VoiceName) this.getProperty(TTSNode.VOICE);
        String prompt = (String) this.getProperty(TTSNode.PROMPT);
        PromptType promptType = (PromptType) this.getProperty(TTSNode.PROMPT_TYPE);

        if ((voice != null) && !StringTools.isEmpty(voice.getName())) {
            Graph.printAtt(out, TTSNode.VOICE, voice.getName());
        }
        if (!StringTools.isEmpty(prompt)) {
            Graph.printTextAtt(out, TTSNode.PROMPT, prompt);
        }
        if (promptType != null) {
            Graph.printAtt(out, TTSNode.PROMPT_TYPE, promptType.name());
        }
        Graph.printAtt(out, TTSNode.WAIT, ((Boolean) this.getProperty(TTSNode.WAIT))
                .booleanValue());
    }

    public static String getNodeTypeName(Class<?> c) {
        return "Mary TTS";

        /*
    String name = c.getName();
    // cut off package name
    name = name.substring(name.lastIndexOf('.') + 1);
    if (name.endsWith("Node")) {
      name = name.substring(0, name.length() - 4);
    }
    return Resources.getString(name);
         */
    }
}
