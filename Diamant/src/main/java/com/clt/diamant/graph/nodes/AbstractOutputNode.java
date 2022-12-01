package com.clt.diamant.graph.nodes;

import com.clt.diamant.*;
import com.clt.diamant.graph.Graph;
import com.clt.diamant.graph.Node;
import com.clt.diamant.graph.search.SearchResult;
import com.clt.diamant.gui.NodePropertiesDialog;
import com.clt.gui.GUI;
import com.clt.gui.OptionPane;
import com.clt.script.exp.EvaluationException;
import com.clt.script.exp.Expression;
import com.clt.script.exp.Value;
import com.clt.script.exp.values.PrimitiveValue;
import com.clt.script.exp.values.StringValue;
import com.clt.speech.SpeechException;
import com.clt.speech.tts.VoiceName;
import com.clt.util.StringTools;
import com.clt.xml.XMLReader;
import com.clt.xml.XMLWriter;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.xml.sax.SAXException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Base class for output nodes in the DialogOS graph.
 * 
 * Created by timo on 09.10.17.
 */
public abstract class AbstractOutputNode extends Node {

    protected static final String PROMPT_TYPE = "prompt_type";
    protected static final String PROMPT = "prompt";
    protected static final String VOICE = "voice";
    protected static final String WAIT = "wait";
    protected static final String AWAIT_SILENCE = "await_silence";

    private JComboBox cbVoices;


    /**
     * Enumeration of different types of prompts.
     * A prompt type specifies how the value that this node outputs
     * is determined. The different prompt types are represented
     * as radio buttons in the "Speech Synthesis" tab of the node
     * in the DialogOS GUI.<p>
     * 
     * The {@link AbstractOutputNode} knows how to
     * evaluate values that are specified as raw text, as DialogOS
     * expressions, or as Groovy expressions. 
     */
    public interface IPromptType {
        public IPromptType[] getValues();
        public String name();
        public IPromptType groovy();
        public IPromptType expression();
    }
    
    /**
     * A default collection of prompt types.
     * In most cases, you can simply return one of the
     * values of this enum (e.g. DefaultPromptType.text())
     * in the {@link #getDefaultPromptType() } method of your
     * custom output node.
     */
    public static enum DefaultPromptType implements IPromptType {
        text("Text"),
        expression("Expression"),
        groovy("GroovyScript");

        public IPromptType groovy() { return groovy; }
        public IPromptType expression() { return expression; }
        private String key;
        public IPromptType[] getValues() { return values(); };

        private DefaultPromptType(String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return Resources.getString(this.key);
        }
    }

    public AbstractOutputNode() {
        this.setProperty(PROMPT, "");
        this.setProperty(PROMPT_TYPE, getDefaultPromptType());
        this.setProperty(WAIT, Boolean.TRUE);
        this.setProperty(AWAIT_SILENCE, Boolean.FALSE);
        this.setProperty(VOICE, new VoiceName("", null));
        this.addEdge(); // output nodes have one port for an outgoing edge
    }

    public static Color getDefaultColor() {
        return Color.GREEN.darker();
    }

    @Override
    public JComponent createEditorComponent(final Map<String, Object> properties) {

        List<VoiceName> voices = getAvailableVoices();

        Component standardEditor = NodePropertiesDialog.createTextArea(properties, PROMPT);
        Component groovyEditor = NodePropertiesDialog.createGroovyScriptEditor(properties, PROMPT);

        //Create first tab (User text)
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gbc.gridy = 0;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 3, 3, 3);

        p.add(new JLabel(getResourceString("Voice") + ':'), gbc);
        gbc.gridx++;
        cbVoices = NodePropertiesDialog.createComboBox(properties, VOICE, voices);
        p.add(cbVoices, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weightx = 0.0;

        p.add(new JLabel(getResourceString("PromptType") + ':'), gbc);
        gbc.gridx++;
        gbc.weightx = 1.0;
        JPanel types = new JPanel(new FlowLayout(FlowLayout.LEFT));
        for (JRadioButton button : NodePropertiesDialog.createRadioButtons(
                properties, PROMPT_TYPE,
                getDefaultPromptType().getValues())) {
            button.addItemListener(e -> {
                if(button.isSelected()) {
                    if (button.getText().equals(getDefaultPromptType().groovy().toString())) {
                        groovyEditor.setVisible(true);
                        standardEditor.setVisible(false);
                    } else {
                        standardEditor.setVisible(true);
                        groovyEditor.setVisible(false);
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

        p.add(new JLabel(getResourceString("Prompt") + ':'), gbc);
        gbc.gridx++;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;

        p.add(standardEditor, gbc);
        p.add(groovyEditor, gbc);

        if (properties.get(PROMPT_TYPE).equals(getDefaultPromptType().groovy())) {
            standardEditor.setVisible(false);
        } else {
            groovyEditor.setVisible(false);
        }

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;

        p.add(NodePropertiesDialog.createCheckBox(properties, WAIT,
                getResourceString("WaitUntilDone")), gbc);

        gbc.gridy++;
        p.add(NodePropertiesDialog.createCheckBox(properties, AWAIT_SILENCE,
                getResourceString("LetPreviousOutputFinish")), gbc);

        gbc.gridy++;
        gbc.anchor = GridBagConstraints.CENTER;

        JPanel border = new JPanel(new BorderLayout());
        final JButton tryPrompt = new JButton(getResourceString("Try"));
        tryPrompt.addActionListener(new ActionListener() {
            boolean speaking = false;

            private void reset() {
                stopSynthesis();
                this.speaking = false;
                tryPrompt.setText(getResourceString("Try"));
            }

            public void actionPerformed(ActionEvent e) {
                if (this.speaking) {
                    this.reset();
                }
                else {
                    new Thread(() -> {
                        try {
                            speaking = true;
                            tryPrompt.setText(GUI.getString("Cancel"));
                            speak(properties);
                        } catch (Exception exn) {
                            String msg = exn.getLocalizedMessage();
                            if ((msg == null) || (msg.length() == 0)) {
                                msg = exn.getClass().getName();
                            }
                            OptionPane.error(tryPrompt, msg);
                        }
                        reset();
                    }).start();
                }
            }
        });
        border.add(tryPrompt, BorderLayout.CENTER);
        border.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        p.add(border, gbc);

        //Add tabs
        JTabbedPane jtp = new JTabbedPane();
        jtp.addTab(Resources.getString("SpeechSynthesis"), p);
        jtp = addMoreTabsToEditorComponent(jtp);
        return jtp;
    }

    /**
     * Returns the voice that is currently selected in the combobox of the
     * editor component. If the default voice is selected, the method
     * returns null, so the caller can look up the default voice elsewhere.
     * 
     * @return
     */
    protected VoiceName getSelectedVoice() {
        if( cbVoices.getSelectedIndex() == 0 ) {
            return null;
        } else {
            return (VoiceName) cbVoices.getSelectedItem();
        }
    }



    protected JTabbedPane addMoreTabsToEditorComponent(JTabbedPane jtp) {
        return jtp;
    }

    /**
     * Returns the {@link IPromptType} that is selected by
     * default in a new node of this class. If your output node
     * class defines a different set of prompt types than the
     * {@link DefaultPromptType}, overwrite this method to return 
     * an instance of that class here.
     * 
     * @return 
     */
    protected IPromptType getDefaultPromptType() {
        return DefaultPromptType.text;
    }
    
    /**
     * Returns a localized version of the given string.
     * 
     * @param key
     * @return 
     */
    abstract public String getResourceString(String key);

    /**
     * Returns the list of available voices for this node.
     * 
     * @return 
     */
    abstract public List<VoiceName> getAvailableVoices();

    protected void speak(Map<String, Object> properties) throws SpeechException {
        speak(determineSpeechOutput(properties), properties);
    }
    
    abstract public void speak(String prompt, Map<String, Object> properties) throws SpeechException;

    /**
     * evaluates DialogOS script and groovyscript expressions.
     * (does not care about TTS markup which must be handled downstream in implementing TTSNodes)
     */
    protected String determineSpeechOutput(Map<String, Object> properties) throws SpeechException {
        String prompt = (String)properties.get(PROMPT);
        if (StringTools.isEmpty(prompt)) {
            throw new IllegalArgumentException(Resources.getString("MissingPrompt"));
        }
        if (properties.get(PROMPT_TYPE).equals(getDefaultPromptType().expression())) {
            Expression exp;
            try {
                exp = this.parseExpression(prompt);
            } catch (Exception exn) {
                throw new EvaluationException(
                        Resources.format("ExpressionParseError", prompt) + ": " + exn.getLocalizedMessage());
            }
            Value result = exp.evaluate();
            if (result instanceof StringValue) {
                prompt = ((StringValue)result).getString();
            } else if (result instanceof PrimitiveValue) { // numbers can be just toStringed
                prompt = result.toString();
            } else { // we could potentially also toString Lists and Structs, but a warning might be just as good.
                throw new IllegalArgumentException(Resources
                        .getString("PromptMustBeString") + result.getClass());
            }
        }
        else if (properties.get(PROMPT_TYPE).equals(getDefaultPromptType().groovy())) {
            prompt = evaluateGroovyScript(prompt);
            //System.out.println(prompt);
        } else {
            // no need to do anything as prompt was already given verbatim
        }

        return prompt;
    }

    abstract public void stopSynthesis();


    @Override
    public boolean editProperties(Component parent) {
        boolean result = super.editProperties(parent);
        this.stopSynthesis();
        return result;
    }


    protected String evaluateGroovyScript(String groovyScript) {
        // Create new Binding and GroovyShell
        Binding sharedData = new Binding();
        GroovyShell gShell = new GroovyShell(this.getClass().getClassLoader(), sharedData);

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
            for (GroovyVariable entry : groovyVars){
                if(!entry.getValue().equals(newVars.get(entry.getName()))){
                    entry.setValue(newVars.get(entry.getName()));
                }
            }
        } catch (Exception e) {
            //TODO localize Exception
            throw new NodeExecutionException(this, "Can't change type of global variables in Groovy script", e);
        }

        String prompt;
        try {
            prompt = (String)result;
        }
        catch(Exception e)
        {
            throw new IllegalArgumentException("prompt must be string"); // TODO: Resources.getString("PromptMustBeString"));
        }
        return prompt;
    }

    public Node execute(WozInterface comm, InputCenter input, ExecutionLogger logger) {
        logNode(logger);
        try {
            speak(this.properties);
        } catch (SpeechException e) {
            throw new NodeExecutionException(this, e.getMessage(), e);
        }
        return this.getEdge(0).getTarget();
    }

    @Override
    public void validate(Collection<SearchResult> errors) {
        super.validate(errors);

        String prompt = (String)this.getProperty(PROMPT);
        IPromptType promptType = (IPromptType)this.getProperty(PROMPT_TYPE);

        if (promptType == null) {
            this.reportError(errors, false, Resources.getString("MissingPromptType"));
        }
        if (StringTools.isEmpty(prompt)) {
            this.reportError(errors, false, Resources.getString("MissingPrompt"));
        }
    }


    @Override
    public void writeVoiceXML(XMLWriter w, IdMap uid_map) {
        // nothing needed
        // see #97 (AK)
    }

    @Override
    protected void readAttribute(XMLReader r, String name, String value, IdMap uid_map) throws SAXException {
        if (name.equals(PROMPT)) {
            this.setProperty(name, value);
        } else if (name.equals(VOICE)) {
            this.setProperty(name, new VoiceName(value, null));
            List<VoiceName> voices = getAvailableVoices();
            for (VoiceName voice : voices) {
                if (voice.getName().equals(value)) {
                    this.setProperty(name, voice);
                    break;
                }
            }
        }
        else if (name.equals(PROMPT_TYPE)) {
            for (IPromptType type : getDefaultPromptType().getValues()) {
                if (type.name().equals(value)) {
                    this.setProperty(PROMPT_TYPE, type);
                    break;
                }
            }
        }
        else if (name.equals(WAIT)) {
            this.setProperty(WAIT, Boolean.valueOf(value.equals("1")));
        }
        else if (name.equals(AWAIT_SILENCE)) {
            this.setProperty(AWAIT_SILENCE, Boolean.valueOf(value.equals("1")));
        }
        else {
            super.readAttribute(r, name, value, uid_map);
        }
    }


    @Override
    protected void writeAttributes(XMLWriter out, IdMap uid_map) {
        super.writeAttributes(out, uid_map);

        VoiceName voice = (VoiceName)this.getProperty(VOICE);
        String prompt = (String)this.getProperty(PROMPT);
        IPromptType promptType = (IPromptType)this.getProperty(PROMPT_TYPE);

        if ((voice != null) && !StringTools.isEmpty(voice.getName())) {
            Graph.printAtt(out, VOICE, voice.getName());
        }
        if (!StringTools.isEmpty(prompt)) {
            Graph.printTextAtt(out, PROMPT, prompt);
        }
        if (promptType != null) {
            Graph.printAtt(out, PROMPT_TYPE, promptType.name());
        }
        Graph.printAtt(out, WAIT, ((Boolean)this.getProperty(WAIT)).booleanValue());
        Graph.printAtt(out, AWAIT_SILENCE, ((Boolean)this.getProperty(AWAIT_SILENCE)).booleanValue());
    }


}
