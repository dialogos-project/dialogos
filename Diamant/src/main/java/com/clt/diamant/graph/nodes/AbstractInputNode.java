package com.clt.diamant.graph.nodes;

import com.clt.audio.LevelMeter;
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
import com.clt.speech.SpeechException;
import com.clt.speech.recognition.*;
import com.clt.util.StringTools;
import com.clt.xml.XMLReader;
import com.clt.xml.XMLWriter;
import org.xml.sax.SAXException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * This class encapsulates common functionality for input nodes that are useful
 * for implementations of speech recognition (see e.g. SphinxNode) or text input
 * (see TextInputNode).
 *
 * Created by timo on 09.10.17.
 */
abstract public class AbstractInputNode extends Node {

    /**
     * used while the recognizer is loading
     */
    private static ImageIcon micInv = null;
    private static ImageIcon micOn = null;
    private static ImageIcon micOff = null;

    private static final float DEFAULT_THRESHOLD = 0;
    private static final String TIMEOUT = "timeout";
    private static final String FORCE_TIMEOUT = "forceTimeout";
    private static final String GRAMMAR = "grammar";
    private static final String GRAMMAR_EXPRESSION = "grammarExpression";
    private static final String LANGUAGE = "language";
    /**
     * name of the property that stores whether recognition should be performed
     * in the background for recognition in the background, results are passed
     * on via the InputCenter
     */
    private static final String BACKGROUND = "background";
    /**
     * name of the property that stores whether recognizers should aim for
     * robust recognition beyond the strict grammar itself
     */
    protected static final String ENABLE_GARBAGE = "enableGarbage";
    /**
     * name of the property that stores the recognition threshold
     */
    protected static final String THRESHOLD = "threshold";

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

    protected boolean supportDynamicGrammars = true;
    protected boolean supportDirectGrammar = true;

    private EdgeManager edgeManager = new EdgeManager(this, TIMEOUT);

    public AbstractInputNode() {
        /* important that some value is set (must not be one of Boolean values, not null later) */
        this.setProperty(BACKGROUND, Boolean.FALSE);
        if (micInv == null) {
            micInv = Images.load(this, "asr/mic_inv.png");
        }
        if (micOn == null) {
            micOn = Images.load(this, "asr/mic_on.png");
        }
        if (micOff == null) {
            micOff = Images.load(this, "asr/mic_off.png");
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

    /**
     * Manages the outgoing edges of a node based on the edges in its associated
     * EdgeConditionModel.<p>
     *
     * To use correctly, you will need to override the updateEdges and
     * editProperties methods as in the AbstractInputNode class.
     */
    public static class EdgeManager {
        private Node managedNode;
        private String timeoutPropertyName;

        public EdgeManager(Node managedNode, String timeoutPropertyName) {
            this.managedNode = managedNode;
            this.timeoutPropertyName = timeoutPropertyName;
        }

        public void updateEdges() {
            List<Edge> patternEdges = new ArrayList<Edge>();
            TimeoutEdge timeoutEdge = null;
            for (int i = 0; i < managedNode.numEdges(); i++) {
                Edge e = managedNode.getEdge(i);
                if (e instanceof TimeoutEdge) {
                    timeoutEdge = (TimeoutEdge) e;
                } else {
                    patternEdges.add(e);
                }
            }

            if ((timeoutEdge == null) && (managedNode.getProperty(timeoutPropertyName) != null)) {
                // Convert old style node: last edge is timeout edge.
                Edge e = managedNode.getEdge(managedNode.numEdges() - 1);
                timeoutEdge = new TimeoutEdge(e.getSource(), e.getTarget());
                patternEdges.remove(e);
            }

            this.reinstallEdges(patternEdges, timeoutEdge);
        }

        private void reinstallEdges(List<? extends Edge> patternEdges, TimeoutEdge timeoutEdge) {
            managedNode.removeAllEdges();
            for (Edge e : patternEdges) {
                managedNode.addEdge(e);
            }
            
            String timeout = (String) managedNode.getProperty(timeoutPropertyName);
            if (timeout != null) {
                managedNode.addEdge(timeoutEdge != null ? timeoutEdge : new TimeoutEdge(managedNode));
            }
        }

        public TimeoutEdge updateEdgeProperty() {
            // collect the current out-edges and save them
            // in the EDGE_PROPERTY property of this node
            TimeoutEdge timeoutEdge = null;
            List<Edge> explicitEdges = new ArrayList<Edge>();

            for (int i = 0; i < managedNode.numEdges(); i++) {
                Edge e = managedNode.getEdge(i);
                if (e instanceof TimeoutEdge) {
                    timeoutEdge = (TimeoutEdge) e;
                } else {
                    explicitEdges.add(e);
                }
            }

            managedNode.setProperty(EdgeConditionModel.EDGE_PROPERTY, explicitEdges);
            
            return timeoutEdge;
        }

        public void reinstallEdgesFromProperty(TimeoutEdge timeoutEdge) {
            List<Edge> es = (List<Edge>) managedNode.getProperty(EdgeConditionModel.EDGE_PROPERTY);
            this.reinstallEdges(es, timeoutEdge);
        }
    }

    @Override
    public void updateEdges() {
        edgeManager.updateEdges();
    }

    @Override
    public boolean editProperties(Component parent) {
        TimeoutEdge timeoutEdge = edgeManager.updateEdgeProperty();
        boolean approved = super.editProperties(parent);

        if (approved) {
            edgeManager.reinstallEdgesFromProperty(timeoutEdge);
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

    /**
     * The table for editing patterns for recognition results that is displayed
     * in the center of the "Recognition" tab of an ASR node.
     *
     */
    public static class PatternTable extends JPanel {
        private JTable table;
        private JButton newButton, deleteButton;

        public PatternTable(EdgeConditionModel edgeModel) {
            super(new BorderLayout());

            // set up table
            table = new JTable(edgeModel);
            final JScrollPane jsp = GUI.createScrollPane(table, new Dimension(300, 150));
            table.getTableHeader().setReorderingAllowed(false);

            TableRowDragger.addDragHandler(table);
            TableColumn column = table.getColumnModel().getColumn(0);
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
            add(center, BorderLayout.CENTER);

            // set up "New" and "Delete" buttons for editing the table
            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));

            deleteButton = new CmdButton(() -> {
                if (!table.isEditing() || table.getCellEditor().stopCellEditing()) {
                    edgeModel.deleteRows(table.getSelectedRows());
                }
            }, com.clt.diamant.Resources.getString("Delete"));
            
            buttons.add(deleteButton);
            deleteButton.setEnabled(table.getSelectedRow() >= 0);
            
            table.getSelectionModel().addListSelectionListener((e) -> {
                deleteButton.setEnabled(table.getSelectedRow() >= 0);
            });

            newButton = new CmdButton(() -> {
                if (!table.isEditing() || table.getCellEditor().stopCellEditing()) {
                    int row = edgeModel.addRow();
                    table.setRowSelectionInterval(row, row);
                }
            }, com.clt.diamant.Resources.getString("New"));
            
            buttons.add(newButton);

            add(buttons, BorderLayout.SOUTH);
            add(Box.createHorizontalStrut(8), BorderLayout.WEST);
            add(Box.createHorizontalStrut(8), BorderLayout.EAST);
        }

        public void setEnabled(boolean bg) {
            table.setEnabled(!bg);
            newButton.setEnabled(!bg);
            if (bg) {
                deleteButton.setEnabled(false);
            }
        }

        private void stopCellEditing() {
            if (table.getCellEditor() != null) {
                table.getCellEditor().stopCellEditing();
            }
        }

        private void setHeader(JComponent header) {
            add(header, BorderLayout.NORTH);
        }

    }

    @Override
    public JComponent createEditorComponent(final Map<String, Object> properties) {
        List<LanguageName> languages = new ArrayList<>(getAvailableLanguages());

        if (!languages.contains(properties.get(LANGUAGE))) {
            languages.add(1, (LanguageName) properties.get(LANGUAGE));
        }
        languages.add(0, new LanguageName(Resources.getString("DefaultLanguage"), null));

        JTabbedPane tabs = GUI.createTabbedPane();

        final EdgeConditionModel edgeModel = new EdgeConditionModel(this,
                properties, com.clt.diamant.Resources.getString("InputPatterns"));
        final PatternTable patternTable = new PatternTable(edgeModel);

        Vector<Object> grammars = new Vector<>();
        if (supportDirectGrammar) {
            grammars.add(DIRECT_GRAMMAR);
        }
        if (supportDynamicGrammars) {
            grammars.add(DYNAMIC_GRAMMAR);
        }

        List<Object> definedGrammars = getGrammars();
        if (!definedGrammars.isEmpty()) {
            grammars.add(new JSeparator());
            grammars.addAll(definedGrammars);
        }

        final JComboBox language = NodePropertiesDialog.createComboBox(properties, LANGUAGE, languages);

        final JComboBox grammar = new JComboBox(grammars);
        grammar.setRenderer(new com.clt.gui.ComponentRenderer());

        /**
         * button to open a new window to edit the selected grammar/grammar
         * expression
         */
        final JButton editGrammar = new CmdButton(com.clt.diamant.Resources.getString("Edit"), () -> {
                                              Object selection = grammar.getSelectedItem();
                                              if (selection instanceof Grammar) {
                                                  Grammar g = (Grammar) selection;
                                                  ScriptEditorDialog.editGrammar(tabs, g);
                                              } // this case happens if "generate from expression" was chosen.
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
                                                  OptionPane.message(tabs, jsp, Resources
                                                                     .getString("Expression"), OptionPane.PLAIN);
                                                  properties.put(GRAMMAR_EXPRESSION, a.getText());
                                              }
                                          });

        grammar.addActionListener(evt -> {
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
                        List<LanguageName> languages1 = getAvailableLanguages();
                        // first try exact locale matches
                        for (LanguageName ln : languages1) {
                            if (ln.getLanguage().equals(l)) {
                                grammarLanguage = ln;
                                break;
                            }
                        }
                        if (grammarLanguage == null) {
                            // then try language only matches
                            for (LanguageName ln : languages1) {
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
                edgeModel.setName(Resources.getString("InputPatterns"));
            } // The words/sentences to be recognized is given as a list
            // of words are sentences
            else if (grammar.getSelectedItem() == DIRECT_GRAMMAR) {
                properties.remove(GRAMMAR);
                properties.remove(GRAMMAR_EXPRESSION);
                editGrammar.setText(Resources.getString("Edit"));
                editGrammar.setEnabled(false);
                edgeModel.setName(Resources.getString("InputWords"));
            } // The grammar is given as an expression (to be evaluated).
            else if (grammar.getSelectedItem() == DYNAMIC_GRAMMAR) {
                properties.remove(GRAMMAR);
                properties.putIfAbsent(GRAMMAR_EXPRESSION, "");
                editGrammar.setText(Resources.getString("EditExpression"));
                editGrammar.setEnabled(true);
                edgeModel.setName(Resources.getString("InputPatterns"));
                // editGrammar.doClick();
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
        
        patternTable.setHeader(header);
        

        final JTextField tf = NodePropertiesDialog.createTextField(properties, TIMEOUT);
        final JCheckBox timeout = new JCheckBox(com.clt.diamant.Resources.getString("Timeout") + ':');
        final JCheckBox forceTimeout = NodePropertiesDialog.createCheckBox(
                properties, FORCE_TIMEOUT, com.clt.diamant.Resources.getString(FORCE_TIMEOUT));

        timeout.addItemListener(evt -> {
            tf.setEnabled(timeout.isSelected());
            forceTimeout.setEnabled(timeout.isSelected());
            if (timeout.isSelected()) {
                tf.selectAll();
            } else {
                properties.remove(TIMEOUT);
                forceTimeout.setSelected(false);
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
        final Runnable updater = () -> {
            boolean bg = (Boolean) properties.get(BACKGROUND);
            patternTable.setEnabled(bg);
        };
        
        background.addItemListener(evt -> {
            boolean bg = evt.getStateChange() == ItemEvent.SELECTED;
            if (bg) {
                // if in background mode, patterns are located at an input
                // node
                edgeModel.clear();
                edgeModel.addRow();
            }
            updater.run();
        });
        updater.run();
        options.add(background, gbc);

        gbc.gridy++;
        gbc.weighty = 0.0;
        gbc.weightx = 1.0;
        gbc.gridwidth = 2;
        JCheckBox robustness = NodePropertiesDialog.createCheckBox(properties,
                                                                   ENABLE_GARBAGE, Resources.getString(ENABLE_GARBAGE));
        options.add(robustness, gbc);

        gbc.gridy++;
        gbc.weighty = 0.0;
        gbc.weightx = 1.0;
        gbc.gridwidth = 2;

        JPanel threshold = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        threshold.add(new JLabel(Resources.getString(THRESHOLD)));
        threshold.add(NodePropertiesDialog.createLongField(properties, THRESHOLD, 0, 100));
        threshold.add(new JLabel("%"));
        options.add(threshold, gbc);

        gbc.gridy++;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        gbc.gridwidth = 2;
        gbc.insets = insets;
        options.add(Box.createVerticalGlue(), gbc);

        /**
         * try out recognition
         */
        final JButton test = new JButton(Resources.getString("TryRecognition"));
        test.addActionListener(evt -> new Thread(() -> {
            patternTable.stopCellEditing();
            RootPaneContainer rpc = GUI.getParent(test, RootPaneContainer.class);
            try {
                recognizeExec(rpc.getLayeredPane(),
                              new DefaultDebugger(), null, properties,
                              true);
            } catch (ExecutionStoppedException exn) {
                // aborted by user
                System.err.println("execution aborted by user");
            } catch (NodeExecutionException exn) {
                if (exn.getException() != null) {
                    OptionPane.error(test, exn.getException());
                } else {
                    OptionPane.error(test, exn);
                }
            } catch (Exception exn) {
                OptionPane.error(test, exn);
            }
        }).start());

        JPanel mainPage = new JPanel(new BorderLayout(6, 0));

        mainPage.add(patternTable, BorderLayout.CENTER);

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
        Node returnNode = recognizeExec(comm != null ? comm.getLayeredPane() : null, comm, input, this.properties, false);
        if (returnNode == null)
            throw new NodeExecutionException(this, "recognition returned null and we have no clue why", logger);
        return returnNode;
    }

    /**
     * determine timeout (if applicable), background, determine timeoutedge from
     * edges, assemble grammar
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
            try {
                MatchResult mep = graphicallyRecognize(layer, recGrammar, patterns, timeout, confidenceThreshold, interactiveTest);
                if (mep == null || mep.getEdge() == -1) {
                    throw new ExecutionStoppedException();
                } else {
                    setVariablesAccordingToMatch(mep.getMatch());
                    return edges.get(mep.getEdge()).getTarget();
                }
            } catch (TimeoutException te) {
                return edges.get(timeoutEdgeIndex).getTarget();
            }
            /*            if (mep == null) {
                assert timeoutEdgeIndex > -1 : "timeout but there's no timeout edge. graphicallyRecognize should only return null for timeouts";
                return edges.get(timeoutEdgeIndex).getTarget();*/
        }
    }

    public MatchResult graphicallyRecognize(JLayeredPane layer, com.clt.srgf.Grammar recGrammar, Pattern[] patterns, long timeout, float confidenceThreshold, boolean interactiveTest) throws TimeoutException {
        RecognitionExecutor recExecutor = createRecognitionExecutor(recGrammar);

        final JButton stop = new JButton(GUI.getString("Cancel"));
        stop.addActionListener(evt -> {
            recExecutor.stop();
            synchronized (stop) {
                stop.notifyAll();
            }
        });
        final JLabel micState = new JLabel("", micOff, SwingConstants.LEFT);
        final TextBox result = new TextBox(1);
        final LevelMeter levelMeter;
        try {
            levelMeter = new LevelMeter(getAudioFormat());
        } catch (UnsupportedAudioFileException e) {
            throw new RuntimeException("something's wrong with the recognizer implementation");
        }
        levelMeter.setEnabled(true);
        levelMeter.setBorder(BorderFactory.createLoweredBevelBorder());
        result.setPreferredSize(new Dimension(300, 75));
        JPanel p = new JPanel(new BorderLayout(12, 12));
        JPanel micPanel = new JPanel(new BorderLayout(12, 0));
        micPanel.add(micState, BorderLayout.NORTH);
        micPanel.add(levelMeter, BorderLayout.SOUTH);
        p.add(micPanel, BorderLayout.NORTH);
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
            popup.addMouseListener(new MouseAdapter() {
            });
            popup.setSize(layer.getWidth(), layer.getHeight());
            layer.add(popup, JLayeredPane.POPUP_LAYER);
            layer.revalidate();
        }

        MatchResult mr;
        try {
            try {
                RecognizerListener stateListener = new AudioAwareRecognizerListener() {
                    @Override
                    public void newAudio(byte[] audio) {
                        try {
                            levelMeter.getStream().write(audio);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void newAudio(double[] audio) {
                        levelMeter.processDoubleData(audio);
                    }

                    public void recognizerStateChanged(RecognizerEvent evt) {
                        // ensure we execute on Swing thread (has a tendency to be more important on linux)
                        SwingUtilities.invokeLater(() -> executeStateChange(evt));
                    }

                    private void executeStateChange(RecognizerEvent evt) {
                        switch (evt.getType()) {
                            case RecognizerEvent.RECOGNIZER_LOADING:
                            case RecognizerEvent.RECOGNIZER_DEACTIVATED:
                                micState.setIcon(micInv);
                                micState.setText(evt.toString());
                                break;
                            case RecognizerEvent.RECOGNIZER_ACTIVATED:
                            case RecognizerEvent.RECOGNIZER_READY:
                                micState.setIcon(micOff);
                                micState.setText(evt.toString());
                                break;
                            case RecognizerEvent.START_OF_SPEECH:
                            case RecognizerEvent.END_OF_SPEECH:
                                micState.setIcon(micOn);
                                result.setText("");
                                break;
                            case RecognizerEvent.INVALID_RESULT:
                            case RecognizerEvent.PARTIAL_RESULT:
                                micState.setIcon(micOff);
                                result.setText(evt.getResult().getAlternative(0).getWords());
                                break;
                            //default:
                            //System.err.println(evt.toString());
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
        } catch (InterruptedException exn) {
            recExecutor.stop();
            throw new ExecutionStoppedException();
        } catch (TimeoutException exn) {
            recExecutor.stop();
            throw exn;
//            return null; // null is the proper return value for timeouts
        } catch (ExecutionException exn) {
            throw new NodeExecutionException(this, 
                    Resources.getString("RecognizerError") + ".", 
                    exn.getCause());
        } catch (Throwable exn) {
            throw new NodeExecutionException(this, 
                    Resources.getString("RecognizerError") + ".", 
                    exn);
        }
        return mr;
    }

    abstract public AudioFormat getAudioFormat();

    abstract public RecognitionExecutor createRecognitionExecutor(com.clt.srgf.Grammar recGrammar);

    abstract public Device getDevice();

    @SuppressWarnings("unchecked")
    public List<Object> getGrammars() {
        return (List) this.getGraph().getOwner().getGrammars();
    }

    private boolean hasGrammar() {
        return properties.get(GRAMMAR) != null;
    }

    private boolean hasTimeout() {
        return properties.get(TIMEOUT) != null;
    }

    protected List<Edge> getEdges(long timeout, Map<String, Object> properties, boolean interactiveTest) {
        if (interactiveTest) {
            List<Edge> edges = new ArrayList<>((List<Edge>) properties.get(EdgeConditionModel.EDGE_PROPERTY));
            if (timeout > 0) {
                edges.add(new TimeoutEdge(this));
            }
            return edges;
        } else {
            return new ArrayList<>(this.edges());
        }
    }

    private int getTimeoutEdgeIndex(List<Edge> edges) {
        for (int i = 0; i < edges.size(); i++) {
            if (edges.get(i) instanceof TimeoutEdge) {
                return i;
            }
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
    public void recognizeInBackground(com.clt.srgf.Grammar recGrammar, InputCenter input,
            VarPattern backgroundPattern, float confidenceThreshold) {
        RecognitionExecutor recThread = createRecognitionExecutor(recGrammar);
        new Thread(() -> {
            try {
                com.clt.speech.recognition.MatchResult mr = recThread.start(recGrammar, new Pattern[]{backgroundPattern}, 0, null, confidenceThreshold);
                if (mr != null) {
                    Value result = mr.getMatch().get(backgroundPattern.getVariableName());
                    input.put(new DialogInput<Object>(getDevice(), result));
                }
            } catch (InterruptedException | ExecutionException | TimeoutException | SpeechException exn) {
                // TODO - better exception handling #100
                exn.printStackTrace();
            }
        }).start();
    }

    /**
     * useful for subclasses to construct a MatchResult (pair of match and
     * edge-ID) during recognition attemps
     *
     * @param utterance a string representation of the text to be checked
     * against the grammar
     * @param recGrammar the grammar to be tested against (needed for included
     * semantic tags)
     * @param patterns the patterns to be tested and matched against
     * @return a MatchResult which encodes the actual match (name-value pairs)
     * and the ID of the matching pattern, or null if no match
     */
    public static MatchResult findMatch(String utterance, com.clt.srgf.Grammar recGrammar, Pattern[] patterns) {
        Value r = recGrammar.match(utterance, null);
        for (int i = 0; i < patterns.length; i++) {
            Match match = patterns[i].match(r);
            if (match != null) {
                return new MatchResult(utterance, match, i);
            }
        }
        return null;
    }

    protected com.clt.srgf.Grammar compileGrammar(Map<String, Object> properties,
            List<Edge> edges)
            throws Exception {
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
                    edges = new ArrayList<>(this.numEdges());
                    for (int i = 0; i < this.numEdges(); i++) {
                        edges.add(this.getEdge(i));
                    }
                } else {
                    edges = new ArrayList<>((List<Edge>) properties.get(EdgeConditionModel.EDGE_PROPERTY));
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
                Long threshold = Long.valueOf(value);
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

    abstract public List<LanguageName> getAvailableLanguages();

    abstract public LanguageName getDefaultLanguage();

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
    public void writeVoiceXML(XMLWriter w, IdMap uid_map) {
        // TODO: implement? doesn't seem to be implemented anywhere in DialogOS...
        // see #97 (AK)
    }

    @Override
    public String getDescription(Edge selectedEdge) {
        StringBuilder buffer = new StringBuilder(super.getDescription(selectedEdge));

        buffer.append("<p><b>" + this.html(Resources.getString("Keywords")) + ":</b>");
        buffer.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">");

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

    @Override
    public void setGraph(Graph g) {
        super.setGraph(g);

        if (g != null) {
            this.setProperty(LANGUAGE, getDefaultLanguage());
        }
    }
}
