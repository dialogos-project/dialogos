/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.saar.coli.dialogos.pocketsphinx.plugin;

import com.clt.diamant.Device;
import com.clt.diamant.DialogInput;
import com.clt.diamant.ExecutionLogger;
import com.clt.diamant.ExecutionStoppedException;
import com.clt.diamant.Grammar;
import com.clt.diamant.IdMap;
import com.clt.diamant.InputCenter;
import com.clt.diamant.Mapping;
import com.clt.diamant.Resources;
import com.clt.diamant.Slot;
import com.clt.diamant.WozInterface;
import com.clt.diamant.graph.Edge;
import com.clt.diamant.graph.Graph;
import com.clt.diamant.graph.Node;
import com.clt.diamant.graph.SpecialEdge;
import com.clt.diamant.graph.nodes.NodeExecutionException;
import com.clt.diamant.graph.nodes.TimeoutEdge;
import com.clt.diamant.graph.search.SearchResult;
import com.clt.diamant.graph.ui.EdgeConditionModel;
import com.clt.diamant.gui.NodePropertiesDialog;
import com.clt.diamant.gui.NodeToolbox;
import com.clt.diamant.gui.ScriptEditorDialog;
import com.clt.gui.CmdButton;
import com.clt.gui.GUI;
import com.clt.gui.Images;
import com.clt.gui.OptionPane;
import com.clt.gui.Passpartout;
import com.clt.gui.TextBox;
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
import com.clt.util.StringTools;
import com.clt.xml.XMLReader;
import com.clt.xml.XMLWriter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.RootPaneContainer;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import org.xml.sax.SAXException;
import com.clt.speech.Language;
import com.clt.speech.SpeechException;
import com.clt.speech.recognition.RecognizerListener;
import com.clt.speech.recognition.RecognizerEvent;
import de.saar.coli.dialogos.pocketsphinx.PocketSphinx;

import java.awt.event.ActionEvent;
import javax.swing.border.Border;

/**
 * @author koller
 */
public class PocketSphinxNode extends Node {

    private static final String TIMEOUT = "timeout";
    private static final String FORCE_TIMEOUT = "forceTimeout";
    private static final String GRAMMAR = "grammar";
    private static final String GRAMMAR_EXPRESSION = "grammarExpression";
    private static final String LANGUAGE = "language";
    private static final String BACKGROUND = "background";
    private static final String THRESHOLD = "threshold";

    private static ImageIcon micOn = null;
    private static ImageIcon micOff = null;

    private static final float DEFAULT_THRESHOLD = 0; // TODO choose a more reasonable value

    private static Device voconDevice = new Device(Resources.getString("PocketSphinx"));

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


    public PocketSphinxNode() {

        if (micOn == null) {
            micOn = Images.load(this, "mic_on.png");
        }
        if (micOff == null) {
            micOff = Images.load(this, "mic_off.png");
        }

        this.setProperty(PocketSphinxNode.LANGUAGE, new LanguageName("", null));
        this.setProperty(PocketSphinxNode.BACKGROUND, Boolean.FALSE);

//        this.setProperty(SphinxNode.THRESHOLD, new Long(40)); // TODO put this back in

        this.removeAllEdges();
    }


    /**
     * Returns the default color of the node.
     *
     * @return the color red.
     */
    public static Color getDefaultColor() {

        return Color.RED.darker();
    }


    private PocketSphinx getRecognizer() {
        PocketSphinx ret = Plugin.getRecognizer();
        ret.setDummyMode(getSettings().isDummyMode());
        ret.setModel(getSettings().getModel());
        return ret;
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

        if ((timeoutEdge == null) && (this.getProperty(PocketSphinxNode.TIMEOUT) != null)) {
            // Convert old style node: last edge is timeout edge.
            Edge e = this.getEdge(this.numEdges() - 1);
            timeoutEdge = new TimeoutEdge(e.getSource(), e.getTarget());
            patternEdges.remove(e);
        }

        this.reinstallEdges(patternEdges, timeoutEdge);
    }


    private void reinstallEdges(List<? extends Edge> patternEdges,
                                TimeoutEdge timeoutEdge) {

        this.removeAllEdges();

        for (Edge e : patternEdges) {
            this.addEdge(e);
        }

        String timeout = (String) this.getProperty(PocketSphinxNode.TIMEOUT);
        if (timeout != null) {
            if (timeoutEdge != null) {
                this.addEdge(timeoutEdge);
            } else {
                this.addEdge(new TimeoutEdge(this));
            }
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
            List<Edge> es =
                    (List<Edge>) this.getProperty(EdgeConditionModel.EDGE_PROPERTY);

            this.reinstallEdges(es, timeoutEdge);

            return true;
        } else {
            return false;
        }
    }


    @Override
    protected JComponent createEditorComponent(final Map<String, Object> properties) {
        List<LanguageName> languages = Plugin.getAvailableLanguages();
        languages.add(0, new LanguageName("", null));

        if (!languages.contains(properties.get(PocketSphinxNode.LANGUAGE))) {
            languages.add(1, (LanguageName) properties.get(PocketSphinxNode.LANGUAGE));
        }

        JTabbedPane tabs = GUI.createTabbedPane();

        final JPanel p = new JPanel(new BorderLayout());

        final JComboBox language = NodePropertiesDialog.createComboBox(properties, PocketSphinxNode.LANGUAGE, languages);

        final EdgeConditionModel edgeModel = new EdgeConditionModel(this,
                properties, com.clt.diamant.Resources
                .getString("InputPatterns"));

        Vector<Object> grammars = new Vector<Object>();
        grammars.add(PocketSphinxNode.DIRECT_GRAMMAR);
        if (PocketSphinxNode.supportDynamicGrammars) {
            grammars.add(PocketSphinxNode.DYNAMIC_GRAMMAR);
        }

        List<Grammar> definedGrammars = this.getGraph().getOwner().getGrammars();
        if (!definedGrammars.isEmpty()) {
            grammars.add(new JSeparator());
            grammars.addAll(definedGrammars);
        }

        final JComboBox grammar = new JComboBox(grammars);
        grammar.setRenderer(new com.clt.gui.ComponentRenderer());

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
                else if (selection == PocketSphinxNode.DYNAMIC_GRAMMAR) {
                    String g = (String) properties.get(PocketSphinxNode.GRAMMAR_EXPRESSION);

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
                    properties.put(PocketSphinxNode.GRAMMAR_EXPRESSION, a.getText());
                }
            }
        });

        grammar.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                language.setEnabled(true);

                // A normal grammar is used.
                if (grammar.getSelectedItem() instanceof Grammar) {
                    Grammar g = (Grammar) grammar.getSelectedItem();
                    properties.put(PocketSphinxNode.GRAMMAR, g);
                    try {
                        String lang = PocketSphinxNode.this.compileGrammar(properties, null)
                                .getLanguage();
                        if (lang != null) {
                            LanguageName grammarLanguage = null;
                            Language l = new Language(lang);
                            List<LanguageName> languages = Plugin
                                    .getAvailableLanguages();
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
                            } else {
                                // language.setSelectedItem(new
                                // LanguageName(lang, null));
                            }

                            language.setEnabled(false);
                        }
                    } catch (Exception exn) {
                        // ignore
                    }
                    // properties.remove(GRAMMAR_EXPRESSION);
                    editGrammar.setText(Resources.getString("EditGrammar"));
                    editGrammar.setEnabled(true);
                    edgeModel.setName(com.clt.diamant.Resources
                            .getString("InputPatterns"));
                }
                // The words/sentences to be recognized is given as a list
                // of words are sentences
                else if (grammar.getSelectedItem() == PocketSphinxNode.DIRECT_GRAMMAR) {
                    properties.remove(PocketSphinxNode.GRAMMAR);
                    properties.remove(PocketSphinxNode.GRAMMAR_EXPRESSION);
                    editGrammar.setText(com.clt.diamant.Resources
                            .getString("Edit"));
                    editGrammar.setEnabled(false);
                    edgeModel.setName(Resources.getString("InputWords"));
                }
                // The grammar is given as an expression (to be evaluated).
                else if (grammar.getSelectedItem() == PocketSphinxNode.DYNAMIC_GRAMMAR) {
                    properties.remove(PocketSphinxNode.GRAMMAR);
                    if (properties.get(PocketSphinxNode.GRAMMAR_EXPRESSION) == null) {
                        properties.put(PocketSphinxNode.GRAMMAR_EXPRESSION, "");
                    }
                    editGrammar.setText(Resources.getString("EditExpression"));
                    editGrammar.setEnabled(true);
                    edgeModel.setName(com.clt.diamant.Resources.getString("InputPatterns"));
                    // editGrammar.doClick();
                }
            }
        });

        Grammar g = (Grammar) properties.get(PocketSphinxNode.GRAMMAR);
        if (g != null) {
            grammar.setSelectedItem(g);
        } else {
            if (this.getProperty(PocketSphinxNode.GRAMMAR_EXPRESSION) != null) {
                grammar.setSelectedItem(PocketSphinxNode.DYNAMIC_GRAMMAR);
            } else {
                grammar.setSelectedItem(PocketSphinxNode.DIRECT_GRAMMAR);
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
        if (PocketSphinxNode.supportDynamicGrammars) {
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

                if (table.isEditing() ? table.getCellEditor().stopCellEditing()
                        : true) {
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

                if (table.isEditing() ? table.getCellEditor().stopCellEditing()
                        : true) {
                    int row = edgeModel.addRow();
                    table.setRowSelectionInterval(row, row);
                }
            }
        }, com.clt.diamant.Resources.getString("New"));
        buttons.add(newButton);

        p.add(buttons, BorderLayout.SOUTH);
        p.add(Box.createHorizontalStrut(8), BorderLayout.WEST);
        p.add(Box.createHorizontalStrut(8), BorderLayout.EAST);

        final JTextField tf = NodePropertiesDialog.createTextField(properties,
                PocketSphinxNode.TIMEOUT);
        final JCheckBox timeout = new JCheckBox(com.clt.diamant.Resources
                .getString("Timeout") + ':');
        final JCheckBox forceTimeout = NodePropertiesDialog.createCheckBox(properties, PocketSphinxNode.FORCE_TIMEOUT, com.clt.diamant.Resources
                        .getString(PocketSphinxNode.FORCE_TIMEOUT));

        timeout.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent evt) {

                tf.setEnabled(timeout.isSelected());
                forceTimeout.setEnabled(timeout.isSelected());
                if (timeout.isSelected()) {
                    tf.selectAll();
                } else {
                    properties.remove(PocketSphinxNode.TIMEOUT);
                    forceTimeout.setSelected(false);
                }
            }
        });
        timeout.setSelected(properties.get(PocketSphinxNode.TIMEOUT) != null);
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
                PocketSphinxNode.BACKGROUND, Resources.getString(PocketSphinxNode.BACKGROUND));
        final Runnable updater = new Runnable() {

            public void run() {

                boolean bg = ((Boolean) properties.get(PocketSphinxNode.BACKGROUND))
                        .booleanValue();
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
        threshold.add(NodePropertiesDialog.createLongField(properties, PocketSphinxNode.THRESHOLD, 0, 100));
        threshold.add(new JLabel("%"));
        options.add(threshold, gbc);

        gbc.gridy++;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        gbc.gridwidth = 2;
        gbc.insets = insets;
        options.add(Box.createVerticalGlue(), gbc);

        final JButton test = new JButton(Resources.getString("TryRecognition"));
        test.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                new Thread(new Runnable() {

                    public void run() {

                        try {
                            if (table.getCellEditor() != null) {
                                table.getCellEditor().stopCellEditing();
                            }
                            RootPaneContainer rpc = GUI.getParent(test,
                                    RootPaneContainer.class);
                            PocketSphinxNode.this.recognize(rpc.getLayeredPane(),
                                    new DefaultDebugger(), null, properties,
                                    true);
                        } catch (ExecutionStoppedException exn) {
                            // aborte by user
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

        tabs.addTab(Resources.getString("PocketSphinx"), mainPage);
        tabs.addTab(com.clt.diamant.Resources.getString("Options"), options);

        return tabs;
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


    @Override
    public void update(Mapping map) {

        super.update(map);

        if (this.getProperty(PocketSphinxNode.GRAMMAR) != null) {
            this.setProperty(PocketSphinxNode.GRAMMAR, map.getGrammar((Grammar) this
                    .getProperty(PocketSphinxNode.GRAMMAR)));
        }
    }


    private com.clt.srgf.Grammar compileGrammar(Map<String, Object> properties, List<Edge> edges) throws Exception {
        Grammar grammar = (Grammar) properties.get(PocketSphinxNode.GRAMMAR);

        com.clt.srgf.Grammar recGrammar;

        if (grammar != null) {
            recGrammar = com.clt.srgf.Grammar.create(grammar.getGrammar());
        } else if (this.getProperty(PocketSphinxNode.GRAMMAR_EXPRESSION) != null) {
            Value v = this.parseExpression((String) this.getProperty(PocketSphinxNode.GRAMMAR_EXPRESSION)).evaluate();
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

            String generatedGrammar = "root $main;\npublic $main = " + body
                    + ";";
            recGrammar = com.clt.srgf.Grammar.create(generatedGrammar);
        }

        return recGrammar;
    }


    @Override
    public Node execute(WozInterface comm, InputCenter input, ExecutionLogger logger) {
        return this.recognize(comm.getLayeredPane(), comm, input, this.properties, false);
    }
    
    private Settings getSettings() {
        return (Settings) this.getGraph().getOwner().getPluginSettings(Plugin.class);
    }


    private Node recognize(JLayeredPane layer, Debugger dbg,
                           final InputCenter input, Map<String, Object> properties,
                           boolean interactive) {

        long timeout = 0;
        String t = (String) properties.get(PocketSphinxNode.TIMEOUT);

        if (t != null) {
            try {
                timeout = ((IntValue) this.parseExpression(t).evaluate(dbg))
                        .getInt();
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

        // Boolean b = (Boolean) properties.get(FORCE_TIMEOUT);
        // boolean forceTimeout = b != null ? b.booleanValue() : false;
        boolean background = ((Boolean) properties.get(PocketSphinxNode.BACKGROUND))
                .booleanValue();

        Grammar grammar = (Grammar) properties.get(PocketSphinxNode.GRAMMAR);

        List<Edge> edges;
        if (properties == this.properties) {
            edges = new ArrayList<Edge>(this.numEdges());
            for (int i = 0; i < this.numEdges(); i++) {
                edges.add(this.getEdge(i));
            }
        } else {
            edges = new ArrayList<Edge>((List<Edge>) properties
                    .get(EdgeConditionModel.EDGE_PROPERTY));
            if (timeout > 0) {
                edges.add(new TimeoutEdge(this));
            }
        }

        int timeoutEdge = -1;
        final Pattern[] patterns = new Pattern[timeout > 0 ? edges.size() - 1 : edges.size()];
        final VarPattern backgroundPattern = new VarPattern("result");
        if (background) {
            for (int i = 0; i < patterns.length; i++) {
                patterns[i] = backgroundPattern;
            }
        } else {
            int[] patternTargets = new int[patterns.length];
            int n = 0;
            for (int i = 0; i < edges.size(); i++) {
                Edge e = edges.get(i);
                if (e instanceof TimeoutEdge) {
                    timeoutEdge = i;
                } else {
                    try {
                        if ((grammar != null)
                                || (this.getProperty(PocketSphinxNode.GRAMMAR_EXPRESSION) != null)) {
                            patterns[n] = this.parsePattern(e.getCondition());
                        } else {
                            patterns[n] = new Constant(i);
                        }
                    } catch (Exception exn) {
                        throw new NodeExecutionException(this,
                                com.clt.diamant.Resources
                                        .getString("IllegalPattern")
                                        + ": " + e.getCondition(), exn);
                    }
                    patternTargets[n] = i;
                    n++;
                }
            }
        }
        final com.clt.srgf.Grammar recGrammar;

        try {
            recGrammar = this.compileGrammar(properties, edges);
        } catch (Exception exn) {
            if (grammar == null) {
                throw new NodeExecutionException(this, Resources.format(
                        "GrammarCompileError", "")
                        + ".", exn);
            } else {
                throw new NodeExecutionException(this, Resources
                        .getString("PatternCompileError"), exn);
            }
        }

        if (recGrammar == null) {
            throw new NodeExecutionException(this, "No grammar selected");
        } else {
            if (recGrammar.getLanguage() == null) {
                LanguageName l = (LanguageName) properties.get(PocketSphinxNode.LANGUAGE);
                if (!StringTools.isEmpty(l.getName())) {
                    recGrammar.setLanguage(l.getName());
                } else {
                    recGrammar.setLanguage(getSettings().getDefaultLanguage().getName());
                }
            }
        }

        try {
            this.getRecognizer().stopRecognition();
            this.getRecognizer().setContext(recGrammar);
        } catch (SpeechException exn) {
            throw new NodeExecutionException(this, Resources
                    .getString("RecognizerError")
                    + ".", exn);
        }

        final RecognitionThread recThread = new RecognitionThread(this.getRecognizer());

        Long threshold = (Long) this.getProperty(PocketSphinxNode.THRESHOLD);
        final float confidenceThreshold = threshold != null ? ((float) threshold / 100.0f) : DEFAULT_THRESHOLD;

        if (background && !interactive && (input != null)) {
            new Thread(new Runnable() {

                public void run() {

                    try {
                        MatchResult mr = recThread.start(recGrammar, patterns,0, null, confidenceThreshold);
                        if (mr != null) {
                            Value result = mr.getMatch().get(backgroundPattern.getVariableName());
                            input.put(new DialogInput<Object>(PocketSphinxNode.voconDevice, result));
                        }
                    } catch (InterruptedException exn) {
                        exn.printStackTrace();
                    } catch (ExecutionException exn) {
                        exn.printStackTrace();
                    } catch (TimeoutException exn) {
                        exn.printStackTrace();
                    }
                }
            }).start();

            return edges.get(0).getTarget();
        } else {
            int resultEdge = this.recognizeAsync(recThread, layer, recGrammar,
                    patterns, timeout, timeoutEdge, interactive,
                    confidenceThreshold);

            if (resultEdge == -1) {
                throw new ExecutionStoppedException();
            } else {
                return edges.get(resultEdge).getTarget();
            }
        }
    }


    private int recognizeAsync(final RecognitionThread recThread,
                               JLayeredPane layer, com.clt.srgf.Grammar recGrammar,
                               final Pattern[] patterns, final long timeout, int timeoutEdge,
                               final boolean interactive, float confidenceThreshold) {

        final JButton stop = new JButton(GUI.getString("Cancel"));
        stop.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                recThread.stop();
                synchronized (stop) {
                    stop.notifyAll();
                }
            }
        });
        final JLabel micState =
                new JLabel("", PocketSphinxNode.micOff, SwingConstants.LEFT);
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


            public void paintBorder(Component c, Graphics g, int x, int y,
                                    int width, int height) {

                g.setColor(new Color(0, 0, 0, 128));
                g.fillRoundRect(x, y, width, height, this.size * 2, this.size * 2);
            }

        };
        p.setBorder(bevel);
        p.setBorder(new GroupBorder(Resources.getString("PocketSphinx")));

        JComponent popup = null;

        if (layer != null) {
            Color c = p.getBackground();
            // c = new Color(0, 0, 0, 0);
            popup = new Passpartout(p, c, false);
            // swallow mouse events
            popup.addMouseListener(new MouseAdapter() {
            });

            popup.setSize(layer.getWidth(), layer.getHeight());
            layer.add(popup, JLayeredPane.POPUP_LAYER);
            layer.revalidate();
        }

        int resultEdge;

        try {
            try {
                RecognizerListener stateListener = new RecognizerListener() {

                    public void recognizerStateChanged(RecognizerEvent evt) {

                        switch (evt.getType()) {
                            case RecognizerEvent.RECOGNIZER_ACTIVATED:
                            case RecognizerEvent.START_OF_SPEECH:
                                micState.setIcon(PocketSphinxNode.micOn);
                                break;
                            case RecognizerEvent.RECOGNIZER_DEACTIVATED:
                                micState.setIcon(PocketSphinxNode.micOff);
                                break;
                        }
                        if (evt.getType() != RecognizerEvent.RECOGNIZER_WARNING) {
                            micState.setText(evt.toString());
                        }
                    }
                };

                MatchResult mr = recThread.start(recGrammar, patterns, timeout, stateListener, confidenceThreshold);

                if ((mr != null) && !interactive) {
                    Match match = mr.getMatch();
                    List<Slot> accessible_vars = this.getGraph().getAllVariables(
                            Graph.LOCAL);
                    for (Iterator<String> vars = match.variables(); vars
                            .hasNext(); ) {
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
                                    "Attempt to bind non existing variable "
                                            + name);
                        }
                    }
                }

                resultEdge = mr != null ? mr.getEdge() : -1;
                micState.setIcon(PocketSphinxNode.micOff);
                if ((resultEdge >= 0) && interactive) {
                    synchronized (stop) {
                        stop.setText(GUI.getString("OK"));
                        micState.setText(Resources
                                .getString("RecognitionResult")
                                + ":");
                        // Utterance bestAlternative = recThread.getLastResult()
                        // .getAlternative(0);
                        // result.setForeground(Color.black);
                        // result.setText(bestAlternative.getWords() );
                        // result.setForeground(Color.GRAY.brighter());
                        // result.append( " (" +
                        // bestAlternative.getConfidence()*100 + "%)");

                        result.setText(recThread.getLastResult()
                                .getAlternative(0).getWords());

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
            recThread.stop();
            throw new ExecutionStoppedException();
        } catch (TimeoutException exn) {
            recThread.stop();
            resultEdge = timeoutEdge;
        } catch (ExecutionException exn) {
            throw new NodeExecutionException(this, Resources
                    .getString("RecognizerError")
                    + ".", exn.getCause());
        } catch (Throwable exn) {
            throw new NodeExecutionException(this, Resources
                    .getString("RecognizerError")
                    + ".", exn);
        }

        return resultEdge;
    }


    @Override
    public void validate(Collection<SearchResult> errors) {

        super.validate(errors);

        try {
            boolean patterns = (this.properties.get(PocketSphinxNode.GRAMMAR) != null)
                    || (this.properties.get(PocketSphinxNode.GRAMMAR_EXPRESSION) != null);
            if (!patterns) {
                this.compileGrammar(this.properties, null);
            }

            if (this.properties.get(PocketSphinxNode.GRAMMAR_EXPRESSION) != null) {
                Type.unify(Type.String, this.parseExpression((String) this.properties.get(PocketSphinxNode.GRAMMAR_EXPRESSION))
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

        String timeout = (String) this.getProperty(PocketSphinxNode.TIMEOUT);
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

        if (name.equals(PocketSphinxNode.TIMEOUT)) {
            this.setProperty(name, value);
        } else if (name.equals(PocketSphinxNode.FORCE_TIMEOUT)) {
            this.setProperty(name, value.equals("1") ? Boolean.TRUE : Boolean.FALSE);
        } else if (name.equals(PocketSphinxNode.THRESHOLD)) {
            try {
                Long threshold = new Long(value);
                if ((threshold < 0) || (threshold > 100)) {
                    throw new NumberFormatException("Value out of range");
                }
                this.setProperty(name, threshold);
            } catch (Exception exn) {
                throw new SAXException("Illegal value for confidence threshold");
            }
        } else if (name.equals(PocketSphinxNode.BACKGROUND)) {
            this.setProperty(name, value.equals("1") ? Boolean.TRUE : Boolean.FALSE);
        } else if (name.equals(PocketSphinxNode.GRAMMAR)) {
            try {
                Grammar g = uid_map.grammars.get(value);
                this.setProperty(PocketSphinxNode.GRAMMAR, g);
            } catch (Exception exn) {
                r.raiseAttributeValueException(name);
            }
        } else if (name.equals(PocketSphinxNode.GRAMMAR_EXPRESSION)) {
            this.setProperty(name, value);
        } else if (name.equals(PocketSphinxNode.LANGUAGE)) {
            this.setProperty(name, new LanguageName(value, null));
            List<LanguageName> languages = Plugin.getAvailableLanguages();
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


    @Override
    protected void writeAttributes(XMLWriter out, IdMap uid_map) {

        super.writeAttributes(out, uid_map);

        String timeout = (String) this.getProperty(PocketSphinxNode.TIMEOUT);
        if (timeout != null) {
            Graph.printAtt(out, PocketSphinxNode.TIMEOUT, timeout);
        }
        boolean forceTimeout = this.getBooleanProperty(PocketSphinxNode.FORCE_TIMEOUT);
        if (forceTimeout) {
            Graph.printAtt(out, PocketSphinxNode.FORCE_TIMEOUT, forceTimeout);
        }

        boolean background = this.getBooleanProperty(PocketSphinxNode.BACKGROUND);
        if (background) {
            Graph.printAtt(out, PocketSphinxNode.BACKGROUND, background);
        }

        Long threshold = (Long) this.getProperty(PocketSphinxNode.THRESHOLD);
        if (threshold != null) {
            Graph.printAtt(out, PocketSphinxNode.THRESHOLD, threshold.intValue());
        }

        LanguageName language = (LanguageName) this.getProperty(PocketSphinxNode.LANGUAGE);
        if ((language != null) && !StringTools.isEmpty(language.getName())) {
            Graph.printAtt(out, PocketSphinxNode.LANGUAGE, language.getName());
        }

        Grammar g = (Grammar) this.getProperty(PocketSphinxNode.GRAMMAR);
        if (g != null) {
            try {
                Graph.printAtt(out, PocketSphinxNode.GRAMMAR, uid_map.grammars.getKey(g));
            } catch (NoSuchElementException exn) {
                // grammar no longer exists. Ignore this info.
            }
        }
        if (this.getProperty(PocketSphinxNode.GRAMMAR_EXPRESSION) != null) {
            Graph.printAtt(out, PocketSphinxNode.GRAMMAR_EXPRESSION,
                    (String) this.getProperty(PocketSphinxNode.GRAMMAR_EXPRESSION));
        }
    }


    @Override
    protected void writeVoiceXML(XMLWriter w, IdMap uid_map) {

        // TODO Auto-generated method stub
    }


    /**
     * The name under which this node type will be displayed
     * in the {@link NodeToolbox}.
     *
     * @param c
     * @return
     */
    public static String getNodeTypeName(Class<?> c) {
        return "PocketSphinx";
    }
}
