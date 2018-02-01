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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by timo on 09.10.17.
 */
public abstract class AbstractOutputNode extends Node {

    protected static final String PROMPT_TYPE = "prompt_type";
    protected static final String PROMPT = "prompt";
    protected static final String VOICE = "voice";
    protected static final String WAIT = "wait";

    public interface IPromptType {
        public IPromptType[] getValues();
        public String name();
        public IPromptType groovy();
        public IPromptType expression();
    }

    public AbstractOutputNode() {
        this.setProperty(PROMPT, "");
        this.setProperty(PROMPT_TYPE, getDefaultPromptType());
        this.setProperty(WAIT, Boolean.TRUE);
        this.setProperty(VOICE, new VoiceName("", null));
        this.addEdge(); // output nodes have one port for an outgoing edge
    }

    public static Color getDefaultColor() {
        return Color.GREEN.darker();
    }

    @Override
    protected JComponent createEditorComponent(
            final Map<String, Object> properties) {

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
        p.add(NodePropertiesDialog
                .createComboBox(properties, VOICE, voices), gbc);

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
            button.addItemListener(new ItemListener(){
                @Override
                public void itemStateChanged(ItemEvent e) {
                    if(button.isSelected()) {
                        if (button.getText().equals(getDefaultPromptType().groovy().toString())) {
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
                    new Thread(new Runnable() {
                        public void run() {
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
                        }
                    }).start();
                }
            }
        });
        border.add(tryPrompt, BorderLayout.CENTER);
        border.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        p.add(border, gbc);

        //Add tabs
        JTabbedPane jtp = new JTabbedPane();
        jtp.addTab("Speech Synthesis", p);
        jtp = addMoreTabsToEditorComponent(jtp);
        return jtp;
    }

    protected JTabbedPane addMoreTabsToEditorComponent(JTabbedPane jtp) {
        return jtp;
    }

    abstract protected IPromptType getDefaultPromptType();
    abstract public String getResourceString(String key);

    abstract protected List<VoiceName> getAvailableVoices();

    protected void speak(Map<String, Object> properties) throws SpeechException {
        speak(determineSpeechOutput(properties), properties);
    }
    protected abstract void speak(String prompt, Map<String, Object> properties) throws SpeechException;

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

    abstract protected void stopSynthesis();


    @Override
    public boolean editProperties(Component parent) {
        boolean result = super.editProperties(parent);
        this.stopSynthesis();
        return result;
    }


    protected String evaluateGroovyScript(String groovyScript) {
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
            for (GroovyVariable entry : groovyVars){
                if(!entry.getValue().equals(newVars.get(entry.getName()))){
                    entry.setValue(newVars.get(entry.getName()));
                }
            }
        } catch (EvaluationException e) {
            //TODO localize Exception
            throw new NodeExecutionException(this, "Can't change type of global variables in Groovy script", e);
        } catch (Exception e) {
            throw new NodeExecutionException(this, "Can't change type of global variables in Groovy script", e);
        }
        System.out.println("result"+result);
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
            e.printStackTrace();
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
    protected void writeVoiceXML(XMLWriter w, IdMap uid_map) {
        // TODO: implement? doesn't seem to be implemented anywhere in DialogOS...
    }

    @Override
    protected void readAttribute(XMLReader r, String name, String value,
                                 IdMap uid_map)
            throws SAXException {

        if (name.equals(PROMPT)) {
            this.setProperty(name, value);
        }
        else if (name.equals(VOICE)) {
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
        Graph.printAtt(out, WAIT, ((Boolean)this.getProperty(WAIT))
                .booleanValue());
    }


}
