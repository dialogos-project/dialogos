package com.clt.diamant.graph.nodes;

import com.clt.diamant.*;
import com.clt.diamant.Grammar;
import com.clt.diamant.graph.Edge;
import com.clt.diamant.graph.Graph;
import com.clt.diamant.graph.Node;
import com.clt.diamant.graph.SpecialEdge;
import com.clt.diamant.graph.search.SearchResult;
import com.clt.diamant.graph.ui.EdgeConditionModel;
import com.clt.diamant.gui.NodePropertiesDialog;
import com.clt.diamant.gui.ScriptEditorDialog;
import com.clt.gui.*;
import com.clt.gui.border.GroupBorder;
import com.clt.gui.table.TableRowDragger;
import com.clt.gui.table.TextRenderer;
import com.clt.script.debug.Debugger;
import com.clt.script.debug.DefaultDebugger;
import com.clt.script.exp.Match;
import com.clt.script.exp.Pattern;
import com.clt.script.exp.Type;
import com.clt.script.exp.Value;
import com.clt.script.exp.expressions.Constant;
import com.clt.script.exp.patterns.VarPattern;
import com.clt.script.exp.values.IntValue;
import com.clt.script.exp.values.StringValue;
import com.clt.script.parser.ParseException;
import com.clt.speech.Language;
import com.clt.speech.recognition.*;
import com.clt.util.StringTools;
import com.clt.xml.XMLReader;
import com.clt.xml.XMLWriter;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * This class encapsulates common functionality for input nodes that are useful
 * for implementations of speech recognition (see e.g. SphinxNode) or text input (see TextInputNode).

 * Created by timo on 09.10.17.
 */
abstract public class AbstractInputNode extends Node {

    private static ImageIcon micOn = null;
    private static ImageIcon micOff = null;

    private static final float DEFAULT_THRESHOLD = 0; // TODO choose a more reasonable value
    private static final String TIMEOUT = "timeout";
    private static final String FORCE_TIMEOUT = "forceTimeout";
    private static final String GRAMMAR = "grammar";
    private static final String GRAMMAR_EXPRESSION = "grammarExpression";
    private static final String LANGUAGE = "language";
    /**
     * name of the property that stores whether recognition should be performed in the background
     * for recognition in the background, results are passed on via the InputCenter
     */
    private static final String BACKGROUND = "background";
    /** name of the property that stores the recognition threshold */
    private static final String THRESHOLD = "threshold";

    private static Object DIRECT_GRAMMAR = new Object() {
        @Override
        public String toString() {
            return Resources.getString("DirectGrammar");
        }
    };
    private static Object DYNAMIC_GRAMMAR = new Object() {
        @Override
        public String toString() {
            return Resources.getString("DynamicGrammar");
        }
    };

    private static final boolean supportDynamicGrammars = true;

    public AbstractInputNode() {
        /* important that some value is set (must not be one of Boolean values, not null later) */
        this.setProperty(BACKGROUND, Boolean.FALSE);
        if (micOn == null) {
            micOn = Images.load(this, "mic_on.png");
        }
        if (micOff == null) {
            micOff = Images.load(this, "mic_off.png");
        }
    }

    /**
     * Returns the default color of the node.
     *
     * @return the color red.
     */
    public static Color getDefaultColor() {
        return Color.RED.darker();
    }

    @Override
    public Color getPortColor(int portNumber) {
        if (this.getEdge(portNumber) instanceof TimeoutEdge) {
            // if (getProperty(TIMEOUT) != null && portNumber == numEdges()-1)
            return new Color(255, 153, 153);
        } else {
            return super.getPortColor(portNumber);
        }
    }

    @Override
    public void updateEdges() {
        List<Edge> patternEdges = new ArrayList<Edge>();
        TimeoutEdge timeoutEdge = null;
        for (int i = 0; i < this.numEdges(); i++) {
            Edge e = this.getEdge(i);
            if (e instanceof TimeoutEdge) {
                timeoutEdge = (TimeoutEdge) e;
            } else {
                patternEdges.add(e);
            }
        }
        if ((timeoutEdge == null) && (this.getProperty(TIMEOUT) != null)) {
            // Convert old style node: last edge is timeout edge.
            Edge e = this.getEdge(this.numEdges() - 1);
            timeoutEdge = new TimeoutEdge(e.getSource(), e.getTarget());
            patternEdges.remove(e);
        }
        this.reinstallEdges(patternEdges, timeoutEdge);
    }

    private void reinstallEdges(List<? extends Edge> patternEdges, TimeoutEdge timeoutEdge) {
        this.removeAllEdges();
        for (Edge e : patternEdges) {
            this.addEdge(e);
        }
        String timeout = (String) this.getProperty(TIMEOUT);
        if (timeout != null) {
            this.addEdge(timeoutEdge != null ? timeoutEdge : new TimeoutEdge(this));
        }
    }

    @Override
    public boolean editProperties(Component parent) {
        TimeoutEdge timeoutEdge = null;
        List<Edge> explicitEdges = new ArrayList<Edge>();
        for (int i = 0; i < this.numEdges(); i++) {
            Edge e = this.getEdge(i);
            if (e instanceof TimeoutEdge) {
                timeoutEdge = (TimeoutEdge) e;
            } else {
                explicitEdges.add(e);
            }
        }
        this.setProperty(EdgeConditionModel.EDGE_PROPERTY, explicitEdges);
        if (super.editProperties(parent)) {
            @SuppressWarnings("unchecked")
            List<Edge> es = (List<Edge>) this.getProperty(EdgeConditionModel.EDGE_PROPERTY);
            this.reinstallEdges(es, timeoutEdge);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void update(Mapping map) {
        super.update(map);
        if (this.getProperty(GRAMMAR) != null) {
            this.setProperty(GRAMMAR, map.getGrammar((Grammar) this
                    .getProperty(GRAMMAR)));
        }
    }

    @Override
    protected JComponent createEditorComponent(
            final Map<String, Object> properties) {

        List<LanguageName> languages = new ArrayList<>(getAvailableLanguages());

        if (!languages.contains(properties.get(LANGUAGE))) {
            languages.add(1, (LanguageName) properties.get(LANGUAGE));
        }
        languages.add(0, new LanguageName(Resources.getString("DefaultLanguage"), null));

        JTabbedPane tabs = GUI.createTabbedPane();

        final JPanel p = new JPanel(new BorderLayout());

        final EdgeConditionModel edgeModel = new EdgeConditionModel(this,
                properties, com.clt.diamant.Resources
                .getString("InputPatterns"));

        Vector<Object> grammars = new Vector<Object>();
        grammars.add(DIRECT_GRAMMAR);
        if (supportDynamicGrammars) {
            grammars.add(DYNAMIC_GRAMMAR);
        }

        List<Grammar> definedGrammars = this.getGraph().getOwner().getGrammars();
        if (!definedGrammars.isEmpty()) {
            grammars.add(new JSeparator());
            grammars.addAll(definedGrammars);
        }

        final JComboBox language = NodePropertiesDialog.createComboBox(
                properties, LANGUAGE, languages);

        final JComboBox grammar = new JComboBox(grammars);
        grammar.setRenderer(new com.clt.gui.ComponentRenderer());

        /** button to open a new window to edit the selected grammar/grammar expression */
        final JButton editGrammar = new CmdButton(com.clt.diamant.Resources
                .getString("Edit"), new Runnable() {
            public void run() {
                Object selection = grammar.getSelectedItem();
                if (selection instanceof Grammar) {
                    Grammar g = (Grammar) selection;
                    ScriptEditorDialog.editGrammar(p, g);
                }
                // this case happens if "generate from expression" was chosen.
                // should a script-editor be used there?
                else if (selection == DYNAMIC_GRAMMAR) {
                    String g = (String) properties.get(GRAMMAR_EXPRESSION);

                    JTextArea a = new JTextArea();
                    a.setWrapStyleWord(true);
                    a.setLineWrap(true);
                    a.setText(g);
                    a.setEditable(true);

                    JScrollPane jsp = new JScrollPane(a,
                            ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                    jsp.setPreferredSize(new Dimension(400, 200));
                    OptionPane.message(p, jsp, com.clt.diamant.Resources
                            .getString("Expression"), OptionPane.PLAIN);
                    properties.put(GRAMMAR_EXPRESSION, a.getText());
                }
            }
        });

        grammar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                language.setEnabled(true);
                // A normal grammar is used.
                if (grammar.getSelectedItem() instanceof Grammar) {
                    Grammar g = (Grammar) grammar.getSelectedItem();
                    properties.put(GRAMMAR, g);
                    try {
                        String lang = compileGrammar(properties, null)
                                .getLanguage();
                        if (lang != null) {
                            LanguageName grammarLanguage = null;
                            Language l = new Language(lang);
                            List<LanguageName> languages = getAvailableLanguages();
                            // first try exact locale matches
                            for (LanguageName ln : languages) {
                                if (ln.getLanguage().equals(l)) {
                                    grammarLanguage = ln;
                                    break;
                                }
                            }
                            if (grammarLanguage == null) {
                                // then try language only matches
                                for (LanguageName ln : languages) {
                                    if (ln
                                            .getLanguage()
                                            .getLocale()
                                            .getLanguage()
                                            .equals(l.getLocale().getLanguage())) {
                                        grammarLanguage = ln;
                                        break;
                                    }
                                }
                            }
                            if (grammarLanguage != null) {
                                language.setSelectedItem(grammarLanguage);
                            }
                            language.setEnabled(false);
                        }
                    } catch (Exception exn) {
                        // ignore: exception was thrown because grammar could not be compiled (and hence lang can't be set)
                    }
                    editGrammar.setText(Resources.getString("EditGrammar"));
                    editGrammar.setEnabled(true);
                    edgeModel.setName(com.clt.diamant.Resources.getString("InputPatterns"));
                }
                // The words/sentences to be recognized is given as a list
                // of words are sentences
                else if (grammar.getSelectedItem() == DIRECT_GRAMMAR) {
                    properties.remove(GRAMMAR);
                    properties.remove(GRAMMAR_EXPRESSION);
                    editGrammar.setText(com.clt.diamant.Resources.getString("Edit"));
                    editGrammar.setEnabled(false);
                    edgeModel.setName(Resources.getString("InputWords"));
                }
                // The grammar is given as an expression (to be evaluated).
                else if (grammar.getSelectedItem() == DYNAMIC_GRAMMAR) {
                    properties.remove(GRAMMAR);
                    properties.putIfAbsent(GRAMMAR_EXPRESSION, "");
                    editGrammar.setText(Resources.getString("EditExpression"));
                    editGrammar.setEnabled(true);
                    edgeModel.setName(com.clt.diamant.Resources
                            .getString("InputPatterns"));
                    // editGrammar.doClick();
                }
            }
        });

        Grammar g = (Grammar) properties.get(GRAMMAR);
        if (g != null) {
            grammar.setSelectedItem(g);
        } else {
            if (this.getProperty(GRAMMAR_EXPRESSION) != null) {
                grammar.setSelectedItem(DYNAMIC_GRAMMAR);
            } else {
                grammar.setSelectedItem(DIRECT_GRAMMAR);
            }
        }

        JPanel header = new JPanel(new GridBagLayout());
        header.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(3, 3, 3, 3);

        header.add(new JLabel(
                com.clt.diamant.Resources.getString("Grammar") + ':'), gbc);
        gbc.gridx++;
        gbc.weightx = 1.0;
        header.add(grammar, gbc);
        gbc.weightx = 0.0;
        if (supportDynamicGrammars) {
            gbc.gridx++;
            header.add(editGrammar, gbc);
        }
        gbc.gridx = 0;
        gbc.gridy++;

        header.add(new JLabel(Resources.getString("Language") + ':'), gbc);
        gbc.gridx++;
        gbc.weightx = 1.0;
        header.add(language, gbc);

        p.add(header, BorderLayout.NORTH);

        final JTable table = new JTable(edgeModel);
        final JScrollPane jsp = GUI.createScrollPane(table, new Dimension(300,
                150));
        // table.setRowSelectionAllowed(false);
        // table.setColumnSelectionAllowed(false);
        table.getTableHeader().setReorderingAllowed(false);

        TableRowDragger.addDragHandler(table);
        TableColumn column = table.getColumnModel().getColumn(0);
        // column.setCellEditor(new TableCellEditor()
        column.setCellRenderer(new TextRenderer() {

            @Override
            public Component getTableCellRendererComponent(JTable table,
                                                           Object value, boolean isSelected, boolean hasFocus,
                                                           int row, int column) {

                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                if (!table.isEnabled()) {
                    label.setForeground(jsp.getBackground());
                    label.setBackground(jsp.getBackground());
                } else if (!edgeModel.isCellEditable(row, column)) {
                    label.setForeground(Color.lightGray);
                }
                return label;
            }
        });

        JPanel center = new JPanel(new BorderLayout());
        center.add(jsp, BorderLayout.CENTER);
        p.add(center, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        final JButton deleteButton = new CmdButton(new Runnable() {
            public void run() {
                if (!table.isEditing() || table.getCellEditor().stopCellEditing()) {
                    edgeModel.deleteRows(table.getSelectedRows());
                }
            }
        }, com.clt.diamant.Resources.getString("Delete"));
        buttons.add(deleteButton);
        deleteButton.setEnabled(table.getSelectedRow() >= 0);
        table.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent e) {
                        deleteButton.setEnabled(table.getSelectedRow() >= 0);
                    }
                });
        final JButton newButton = new CmdButton(new Runnable() {
            public void run() {
                if (!table.isEditing() || table.getCellEditor().stopCellEditing()) {
                    int row = edgeModel.addRow();
                    table.setRowSelectionInterval(row, row);
                }
            }
        }, com.clt.diamant.Resources.getString("New"));
        buttons.add(newButton);

        p.add(buttons, BorderLayout.SOUTH);
        p.add(Box.createHorizontalStrut(8), BorderLayout.WEST);
        p.add(Box.createHorizontalStrut(8), BorderLayout.EAST);

        final JTextField tf = NodePropertiesDialog.createTextField(properties, TIMEOUT);
        final JCheckBox timeout = new JCheckBox(com.clt.diamant.Resources
                .getString("Timeout") + ':');
        final JCheckBox forceTimeout = NodePropertiesDialog.createCheckBox(
                properties, FORCE_TIMEOUT, com.clt.diamant.Resources.getString(FORCE_TIMEOUT));

        timeout.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent evt) {

                tf.setEnabled(timeout.isSelected());
                forceTimeout.setEnabled(timeout.isSelected());
                if (timeout.isSelected()) {
                    tf.selectAll();
                } else {
                    properties.remove(TIMEOUT);
                    forceTimeout.setSelected(false);
                }
            }
        });
        timeout.setSelected(properties.get(TIMEOUT) != null);
        forceTimeout.setEnabled(timeout.isSelected());

        JPanel options = new JPanel(new GridBagLayout());
        gbc = new GridBagConstraints();

        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.insets = new Insets(3, 12, 3, 0);
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridwidth = 1;

        gbc.gridx = 0;
        gbc.gridy = 0;

        gbc.gridwidth = 1;

        options.add(timeout, gbc);
        gbc.gridx++;
        options.add(tf, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        Insets insets = gbc.insets;
        gbc.insets = new Insets(0, insets.left + 12, insets.bottom,
                insets.right);
        options.add(forceTimeout, gbc);
        gbc.insets = insets;

        gbc.gridy++;
        gbc.weighty = 0.0;
        gbc.weightx = 1.0;
        gbc.gridwidth = 2;
        JCheckBox background = NodePropertiesDialog.createCheckBox(properties,
                BACKGROUND, Resources.getString(BACKGROUND));
        final Runnable updater = new Runnable() {

            public void run() {

                boolean bg = (Boolean) properties.get(BACKGROUND);
                table.setEnabled(!bg);
                newButton.setEnabled(!bg);
                if (bg) {
                    deleteButton.setEnabled(false);
                }
            }
        };
        background.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {

                boolean bg = e.getStateChange() == ItemEvent.SELECTED;
                if (bg) {
                    // if in background mode, patterns are located at an input
                    // node
                    edgeModel.clear();
                    edgeModel.addRow();
                }

                updater.run();
            }
        });
        updater.run();
        options.add(background, gbc);

        gbc.gridy++;
        gbc.weighty = 0.0;
        gbc.weightx = 1.0;
        gbc.gridwidth = 2;

        JPanel threshold = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        threshold.add(new JLabel(Resources.getString("threshold")));
        threshold.add(NodePropertiesDialog.createLongField(properties, THRESHOLD, 0, 100));
        threshold.add(new JLabel("%"));
        options.add(threshold, gbc);

        gbc.gridy++;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        gbc.gridwidth = 2;
        gbc.insets = insets;
        options.add(Box.createVerticalGlue(), gbc);

        /** try out recognition */
        final JButton test = new JButton(Resources.getString("TryRecognition"));
        test.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new Thread(new Runnable() {
                    public void run() {
                        if (table.getCellEditor() != null) {
                            table.getCellEditor().stopCellEditing();
                        }
                        RootPaneContainer rpc = GUI.getParent(test, RootPaneContainer.class);
                        try {
                            recognizeExec(rpc.getLayeredPane(),
                                    new DefaultDebugger(), null, properties,
                                    true);
                        } catch (ExecutionStoppedException exn) {
                            // aborted by user
                        } catch (NodeExecutionException exn) {
                            if (exn.getException() != null) {
                                OptionPane.error(test, exn.getException());
                            } else {
                                OptionPane.error(test, exn);
                            }
                        } catch (ThreadDeath d) {
                            throw d;
                        } catch (Throwable exn) {
                            exn.printStackTrace();
                            OptionPane.error(test, exn);
                        }
                    }
                }).start();
            }
        });

        JPanel mainPage = new JPanel(new BorderLayout(6, 0));

        mainPage.add(p, BorderLayout.CENTER);

        JPanel tryRec = new JPanel(new FlowLayout(FlowLayout.CENTER));
        tryRec.setBorder(BorderFactory.createEmptyBorder(0, 6, 6, 6));
        tryRec.add(test);
        mainPage.add(tryRec, BorderLayout.SOUTH);

        tabs.addTab(Resources.getString("RecognitionTab"), mainPage);
        tabs.addTab(com.clt.diamant.Resources.getString("Options"), options);

        return tabs;
    }

    /**
     * entrypoint into speech recognition when the system is running
     */
    @Override
    public Node execute(WozInterface comm, InputCenter input, ExecutionLogger logger) {
        return recognizeExec(comm != null ? comm.getLayeredPane() : null, comm, input, this.properties, false);
    }

    /**
     * determine timeout (if applicable), background,
     * determine timeoutedge from edges, assemble grammar
     */
    protected Node recognizeExec(JLayeredPane layer, Debugger dbg,
                             final InputCenter input, Map<String, Object> properties, boolean interactiveTest) {
        long timeout = getTimeout(dbg);

        // Boolean b = (Boolean) properties.get(FORCE_TIMEOUT);
        // boolean forceTimeout = b != null ? b.booleanValue() : false;
        boolean background = (Boolean) properties.get(BACKGROUND);

        List<Edge> edges = getEdges(timeout, properties, interactiveTest);

        int timeoutEdgeIndex = getTimeoutEdgeIndex(edges);
        Pattern[] patterns = createPatterns(edges, background);

        final com.clt.srgf.Grammar recGrammar;

        try {
            recGrammar = compileGrammar(properties, edges);
        } catch (Exception exn) {
            if (!hasGrammar()) {
                throw new NodeExecutionException(this, Resources.format("GrammarCompileError", "") + ".", exn);
            } else {
                throw new NodeExecutionException(this, Resources.getString("PatternCompileError"), exn);
            }
        }

        if (recGrammar == null) {
            throw new NodeExecutionException(this, "No grammar selected");
        } else if (recGrammar.getLanguage() == null) {
            // preference ordering for recognition language:
            // 1. grammar, 2. property of the node, 3. default language
            LanguageName l = (LanguageName) properties.get(LANGUAGE);
            recGrammar.setLanguage((!StringTools.isEmpty(l.getName()) ? l : getDefaultLanguage()).getName());
        }

        Long threshold = (Long) this.getProperty(THRESHOLD);
        final float confidenceThreshold = threshold != null ? ((float) threshold / 100.0f) : DEFAULT_THRESHOLD;

        if (background && (input != null)) {
            recognizeInBackground(recGrammar, input, (VarPattern) patterns[0], confidenceThreshold);
            return edges.get(0).getTarget();
        } else {
            MatchResult mep = graphicallyRecognize(layer, recGrammar, patterns, timeout, confidenceThreshold, interactiveTest);
            if (mep == null) {
                return edges.get(timeoutEdgeIndex).getTarget();
            } else if (mep.getEdge() == -1) {
                throw new ExecutionStoppedException();
            } else {
                setVariablesAccordingToMatch(mep.getMatch());
                return edges.get(mep.getEdge()).getTarget();
            }
        }
    }

    protected MatchResult graphicallyRecognize(JLayeredPane layer, com.clt.srgf.Grammar recGrammar, Pattern[] patterns, long timeout, float confidenceThreshold, boolean interactiveTest) {
        RecognitionExecutor recExecutor = createRecognitionExecutor(recGrammar);

        final JButton stop = new JButton(GUI.getString("Cancel"));
        stop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                recExecutor.stop();
                synchronized (stop) {
                    stop.notifyAll();
                }
            }
        });
        final JLabel micState = new JLabel("", micOff, SwingConstants.LEFT);
        final TextBox result = new TextBox(1);
        result.setPreferredSize(new Dimension(300, 75));
        JPanel p = new JPanel(new BorderLayout(12, 12));
        p.add(micState, BorderLayout.NORTH);
        p.add(result, BorderLayout.CENTER);
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottom.setOpaque(false);
        bottom.add(stop);
        p.add(bottom, BorderLayout.SOUTH);
        p.setOpaque(false);
        Border bevel = new Border() {
            private int size = 20;
            public Insets getBorderInsets(Component c) {
                return new Insets(this.size, this.size, this.size, this.size);
            }
            public boolean isBorderOpaque() {
                return false;
            }
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                g.setColor(new Color(0, 0, 0, 128));
                g.fillRoundRect(x, y, width, height, this.size * 2, this.size * 2);
            }
        };
        p.setBorder(bevel);
        p.setBorder(new GroupBorder(Resources.getString("Sphinx")));

        JComponent popup = null;
        if (layer != null) {
            Color c = p.getBackground();
            popup = new Passpartout(p, c, false);
            // consume mouse events
            popup.addMouseListener(new MouseAdapter() {});
            popup.setSize(layer.getWidth(), layer.getHeight());
            layer.add(popup, JLayeredPane.POPUP_LAYER);
            layer.revalidate();
        }

        MatchResult mr;
        try {
            try {
                RecognizerListener stateListener = new RecognizerListener() {
                    public void recognizerStateChanged(RecognizerEvent evt) {
                        // ensure we execute on Swing thread (has a tendency to be more important on linux)
                        SwingUtilities.invokeLater(() -> executeStateChange(evt));
                    }
                    private void executeStateChange(RecognizerEvent evt) {
                        switch (evt.getType()) {
                            case RecognizerEvent.RECOGNIZER_ACTIVATED:
                                micState.setIcon(micOff);
                                break;
                            case RecognizerEvent.START_OF_SPEECH:
                                micState.setIcon(micOn);
                                break;
                            case RecognizerEvent.RECOGNIZER_DEACTIVATED:
                                micState.setIcon(micOff);
                                break;
                        }
                        if (evt.getType() != RecognizerEvent.RECOGNIZER_WARNING) {
                            micState.setText(evt.toString());
                        }
                    }
                };

                // the assumption probably is that recThread.start() will loop until we have a recognition result
                mr = recExecutor.start(recGrammar, patterns, timeout, stateListener, confidenceThreshold);

                int resultEdge = mr != null ? mr.getEdge() : -1;

                if ((resultEdge >= 0) && interactiveTest) {
                    synchronized (stop) {
                        stop.setText(GUI.getString("OK"));
                        micState.setText(Resources.getString("RecognitionResult") + ":");
                        // Utterance bestAlternative = recThread.getLastResult()
                        // .getAlternative(0);
                        // result.setForeground(Color.black);
                        // result.setText(bestAlternative.getWords() );
                        // result.setForeground(Color.GRAY.brighter());
                        // result.append( " (" +
                        // bestAlternative.getConfidence()*100 + "%)");
                        result.setText(mr.getUtterance());
                        stop.wait();
                    }
                }
            } finally {
                if (layer != null) {
                    layer.remove(popup);
                    if (layer.getParent() != null) {
                        layer.getParent().repaint();
                    }
                }
            }
        } catch (ThreadDeath d) {
            throw d;
        } catch (InterruptedException exn) {
            recExecutor.stop();
            throw new ExecutionStoppedException();
        } catch (TimeoutException exn) {
            recExecutor.stop();
            return null; // null is the proper return value for timeouts
        } catch (ExecutionException exn) {
            throw new NodeExecutionException(this, Resources
                    .getString("RecognizerError")
                    + ".", exn.getCause());
        } catch (Throwable exn) {
            throw new NodeExecutionException(this, Resources
                    .getString("RecognizerError")
                    + ".", exn);
        }
        return mr;
    }

    protected abstract RecognitionExecutor createRecognitionExecutor(com.clt.srgf.Grammar recGrammar);

    protected abstract Device getDevice();

    private boolean hasGrammar() {
        return properties.get(GRAMMAR) != null;
    }

    private boolean hasTimeout() {
        return properties.get(TIMEOUT) != null;
    }

    private List<Edge> getEdges(long timeout, Map<String, Object> properties, boolean interactiveTest) {
        if (interactiveTest) {
            List<Edge> edges = new ArrayList<Edge>((List<Edge>) properties.get(EdgeConditionModel.EDGE_PROPERTY));
            if (timeout > 0) {
                edges.add(new TimeoutEdge(this));
            }
            return edges;
        } else
            return new ArrayList<Edge>(this.edges());
    }

    private int getTimeoutEdgeIndex(List<Edge> edges) {
        for (int i = 0; i < edges.size(); i++) {
            if (edges.get(i) instanceof TimeoutEdge)
                return i;
        }
        return -1;
    }

    private Pattern[] createPatterns(List<Edge> edges, boolean background) {
        final Pattern[] patterns = new Pattern[hasTimeout() ? edges.size() - 1 : edges.size()];
        final VarPattern backgroundPattern = new VarPattern("result");
        if (background) {
            for (int i = 0; i < patterns.length; i++) {
                patterns[i] = backgroundPattern;
            }
        } else {
            int n = 0;
            for (int i = 0; i < edges.size(); i++) {
                Edge e = edges.get(i);
                if (!(e instanceof TimeoutEdge)) {
                    try {
                        if ((hasGrammar()) || (getProperty(GRAMMAR_EXPRESSION) != null)) {
                            patterns[n] = parsePattern(e.getCondition());
                        } else {
                            patterns[n] = new Constant(i);
                        }
                    } catch (Exception exn) {
                        throw new NodeExecutionException(this,
                                com.clt.diamant.Resources.getString("IllegalPattern")
                                        + ": " + e.getCondition(), exn);
                    }
                    n++;
                }
            }
        }
        return patterns;
    }

    private long getTimeout(Debugger dbg) {
        long timeout = 0;
        String t = (String) properties.get(TIMEOUT);

        if (t != null) {
            try {
                timeout = ((IntValue) this.parseExpression(t).evaluate(dbg)).getInt();
            } catch (Exception exn) {
                throw new NodeExecutionException(this,
                        com.clt.diamant.Resources
                                .getString("IllegalTimeoutValue")
                                + " " + t, exn);
            }
            if (timeout < 0) {
                throw new NodeExecutionException(this,
                        com.clt.diamant.Resources
                                .getString("IllegalTimeoutValue")
                                + " " + t);
            }
        }
        return timeout;
    }

    /* * recognize in the background and return result via @param input */
    protected void recognizeInBackground(
        com.clt.srgf.Grammar recGrammar,
        InputCenter input,
        VarPattern backgroundPattern,
        float confidenceThreshold) {
        RecognitionExecutor recThread = createRecognitionExecutor(recGrammar);
        new Thread(() -> {
            try {
                com.clt.speech.recognition.MatchResult mr = recThread.start(recGrammar, new Pattern[] {backgroundPattern},0, null, confidenceThreshold);
                if (mr != null) {
                    Value result = mr.getMatch().get(backgroundPattern.getVariableName());
                    input.put(new DialogInput<Object>(getDevice(), result));
                }
            } catch (InterruptedException | ExecutionException | TimeoutException exn) {
                exn.printStackTrace();
            }
        }).start();
    }

    /**
     * useful for subclasses to construct a MatchResult (pair of match and edge-ID) during recognition attemps
     * @param utterance a string representation of the text to be checked against the grammar
     * @param recGrammar the grammar to be tested against (needed for included semantic tags)
     * @param patterns the patterns to be tested and matched against
     * @return a MatchResult which encodes the actual match (name-value pairs) and the ID of the matching pattern, or null if no match
     */
    public static MatchResult findMatch(String utterance, com.clt.srgf.Grammar recGrammar, Pattern[] patterns) {
        Value r = recGrammar.match(utterance, null);
        for (int i = 0; i < patterns.length; i++) {
            Match match = patterns[i].match(r);
            if (match != null)
                return new MatchResult(utterance, match, i);
        }
        return null;
    }

    /**
     * sets variables according to a match;
     * to be used by implementors of recognizeBlocking/recognizeInBackground
     * @param match
     */
    private void setVariablesAccordingToMatch(Match match){
        List<Slot> accessible_vars = this.getGraph().getAllVariables(Graph.LOCAL);
        for (Iterator<String> vars = match.variables(); vars.hasNext(); ) {
            String name = vars.next();
            Slot v = null;
            for (int j = accessible_vars.size() - 1; (j >= 0)
                    && (v == null); j--) {
                Slot s = accessible_vars.get(j);
                if (name.equals(s.getName())) {
                    v = s;
                }
            }
            if (v != null) {
                v.setValue(match.get(name));
            } else {
                throw new NodeExecutionException(this,
                        "Attempt to bind non existing variable " + name);
            }
        }
    }


    protected com.clt.srgf.Grammar compileGrammar(Map<String, Object> properties,
                                                  List<Edge> edges)
            throws ParseException, Exception {
        Grammar grammar = (Grammar) properties.get(GRAMMAR);
        com.clt.srgf.Grammar recGrammar;
        if (grammar != null) {
            recGrammar = com.clt.srgf.Grammar.create(grammar.getGrammar());
        } else if (this.getProperty(GRAMMAR_EXPRESSION) != null) {
            Value v = this.parseExpression(
                            (String) this.getProperty(GRAMMAR_EXPRESSION))
                    .evaluate();
            recGrammar = com.clt.srgf.Grammar.create(((StringValue) v).getString());
        } else {
            if (edges == null) {
                if (properties == this.properties) {
                    edges = new ArrayList<Edge>(this.numEdges());
                    for (int i = 0; i < this.numEdges(); i++) {
                        edges.add(this.getEdge(i));
                    }
                } else {
                    edges = new ArrayList<Edge>((List<Edge>) properties.get(EdgeConditionModel.EDGE_PROPERTY));
                }
            }

            if (edges.isEmpty()) {
                throw new ParseException("empty grammar"); // TODO: localize?
            }
            StringBuilder body = new StringBuilder();
            for (int i = 0; i < edges.size(); i++) {
                Edge e = edges.get(i);
                if (!(e instanceof SpecialEdge)) {
                    if (body.length() > 0) {
                        body.append("| (\n");
                    } else {
                        body.append("(\n");
                    }
                    String expansion = e.getCondition();
                    if (expansion.trim().length() == 0) {
                        body.append("$VOID");
                    } else {
                        body.append(e.getCondition());
                    }
                    body.append("\n) { " + i + " } ");
                }
            }
            String generatedGrammar = "root $main;\npublic $main = " + body + ";";
            recGrammar = com.clt.srgf.Grammar.create(generatedGrammar);
        }
        return recGrammar;
    }


    @Override
    public void validate(Collection<SearchResult> errors) {
        super.validate(errors);
        try {
            boolean patterns = (this.properties.get(GRAMMAR) != null)
                    || (this.properties.get(GRAMMAR_EXPRESSION) != null);
            if (!patterns) {
                compileGrammar(this.properties, null);
            }

            if (this.properties.get(GRAMMAR_EXPRESSION) != null) {
                Type.unify(Type.String, this.parseExpression(
                        (String) this.properties.get(GRAMMAR_EXPRESSION))
                        .getType());
            }

            for (int i = 0; i < this.numEdges(); i++) {
                Edge e = this.getEdge(i);
                if (!(e instanceof SpecialEdge)) {
                    if (e.getCondition().trim().length() == 0) {
                        this.reportError(errors, false, com.clt.diamant.Resources
                                .getString("containsEmptyInputPattern"));
                    } else if (patterns) {
                        try {
                            this.parsePattern(e.getCondition());
                        } catch (Exception exn) {
                            this.reportError(errors, false,
                                    com.clt.diamant.Resources.format(
                                            "containsIllegalInputPattern", exn
                                                    .getLocalizedMessage()));
                        }
                    }
                }
            }
        } catch (Exception exn) {
            this.reportError(errors, false, com.clt.diamant.Resources.format(
                    "containsIllegalInputPattern", exn.getLocalizedMessage()));
        }

        String timeout = (String) this.getProperty(TIMEOUT);
        if (timeout != null) {
            try {
                Type t = this.parseExpression(timeout).getType();
                Type.unify(t, Type.Int);
            } catch (Exception exn) {
                this.reportError(errors, false, com.clt.diamant.Resources.format(
                        "containsIllegalTimeoutExpression", exn
                                .getLocalizedMessage()));
            }
        }

    }


    @Override
    protected void readAttribute(XMLReader r, String name, String value,
                                 IdMap uid_map)
            throws SAXException {

        if (name.equals(TIMEOUT)) {
            this.setProperty(name, value);
        } else if (name.equals(FORCE_TIMEOUT)) {
            this.setProperty(name, value.equals("1") ? Boolean.TRUE : Boolean.FALSE);
        } else if (name.equals(THRESHOLD)) {
            try {
                Long threshold = new Long(value);
                if ((threshold < 0) || (threshold > 100)) {
                    throw new NumberFormatException("Value out of range");
                }
                this.setProperty(name, threshold);
            } catch (Exception exn) {
                throw new SAXException("Illegal value for confidence threshold");
            }
        } else if (name.equals(BACKGROUND)) {
            this.setProperty(name, value.equals("1") ? Boolean.TRUE : Boolean.FALSE);
        } else if (name.equals(GRAMMAR)) {
            try {
                Grammar g = uid_map.grammars.get(value);
                this.setProperty(GRAMMAR, g);
            } catch (Exception exn) {
                r.raiseAttributeValueException(name);
            }
        } else if (name.equals(GRAMMAR_EXPRESSION)) {
            this.setProperty(name, value);
        } else if (name.equals(LANGUAGE)) {
            this.setProperty(name, new LanguageName(value, null));
            List<LanguageName> languages = getAvailableLanguages();
            for (LanguageName language : languages) {
                if (language.getName().equals(value)) {
                    this.setProperty(name, language);
                    break;
                }
            }
        } else {
            super.readAttribute(r, name, value, uid_map);
        }
    }

    protected abstract List<LanguageName> getAvailableLanguages();
    protected abstract LanguageName getDefaultLanguage();

    @Override
    protected void writeAttributes(XMLWriter out, IdMap uid_map) {

        super.writeAttributes(out, uid_map);

        String timeout = (String) this.getProperty(TIMEOUT);
        if (timeout != null) {
            Graph.printAtt(out, TIMEOUT, timeout);
        }
        boolean forceTimeout = this.getBooleanProperty(FORCE_TIMEOUT);
        if (forceTimeout) {
            Graph.printAtt(out, FORCE_TIMEOUT, forceTimeout);
        }

        boolean background = this.getBooleanProperty(BACKGROUND);
        if (background) {
            Graph.printAtt(out, BACKGROUND, background);
        }

        Long threshold = (Long) this.getProperty(THRESHOLD);
        if (threshold != null) {
            Graph.printAtt(out, THRESHOLD, threshold.intValue());
        }

        LanguageName language = (LanguageName) this.getProperty(LANGUAGE);
        if ((language != null) && !StringTools.isEmpty(language.getName())) {
            Graph.printAtt(out, LANGUAGE, language.getName());
        }

        Grammar g = (Grammar) this.getProperty(GRAMMAR);
        if (g != null) {
            try {
                Graph.printAtt(out, GRAMMAR, uid_map.grammars.getKey(g));
            } catch (NoSuchElementException exn) {
                // grammar no longer exists. Ignore this info.
            }
        }
        if (this.getProperty(GRAMMAR_EXPRESSION) != null) {
            Graph.printAtt(out, GRAMMAR_EXPRESSION,
                    (String) this.getProperty(GRAMMAR_EXPRESSION));
        }
    }

    @Override
    protected void writeVoiceXML(XMLWriter w, IdMap uid_map) {
        // TODO: implement? doesn't seem to be implemented anywhere in DialogOS...
    }


    @Override
    public String getDescription(Edge selectedEdge) {

        StringBuilder buffer = new StringBuilder(super
                .getDescription(selectedEdge));

        buffer.append("<p><b>" + this.html(Resources.getString("Keywords"))
                + ":</b>");
        buffer
                .append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">");
        for (int i = 0; i < this.numEdges(); i++) {
            Edge e = this.getEdge(i);
            buffer.append("<tr>");
            buffer.append("<td width=\"20\"></td>");
            buffer.append("<td align=\"right\" valign=\"top\">");
            if (e == selectedEdge) {
                buffer.append("<font color=\"red\">");
            }
            buffer.append(String.valueOf(i) + '.');
            if (e == selectedEdge) {
                buffer.append("</font>");
            }
            buffer.append("</td><td width=\"10\"></td><td>");
            if (e == selectedEdge) {
                buffer.append("<font color=\"red\">");
            }
            buffer.append(this.html(e.getCondition()));
            if (e == selectedEdge) {
                buffer.append("</font>");
            }
            buffer.append("</td></tr>");
        }
        buffer.append("</table>");

        return buffer.toString();
    }

    @Override public void setGraph(Graph g) {
        super.setGraph(g);
        this.setProperty(LANGUAGE, getDefaultLanguage());
    }

}
