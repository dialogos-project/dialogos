package com.clt.diamant.graph.search;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.clt.diamant.Document;
import com.clt.diamant.Main;
import com.clt.diamant.Resources;
import com.clt.diamant.SingleDocument;
import com.clt.diamant.graph.nodes.CallNode;
import com.clt.diamant.graph.nodes.ConditionalNode;
import com.clt.diamant.graph.nodes.ContinueNode;
import com.clt.diamant.graph.nodes.EndNode;
import com.clt.diamant.graph.nodes.GotoNode;
import com.clt.diamant.graph.nodes.GraphNode;
import com.clt.diamant.graph.nodes.GroovyNode;
import com.clt.diamant.graph.nodes.InputNode;
import com.clt.diamant.graph.nodes.LabelNode;
import com.clt.diamant.graph.nodes.LoopNode;
import com.clt.diamant.graph.nodes.OutputNode;
import com.clt.diamant.graph.nodes.ProcNode;
import com.clt.diamant.graph.nodes.ReturnNode;
import com.clt.diamant.graph.nodes.ScriptNode;
import com.clt.diamant.graph.nodes.SetVariableNode;
import com.clt.diamant.graph.nodes.SleepNode;
import com.clt.diamant.graph.nodes.StartNode;
import com.clt.diamant.graph.nodes.TestVariableNode;
import com.clt.event.ProgressListener;
import com.clt.gui.Buttons;
import com.clt.gui.CmdButton;
import com.clt.gui.ComponentRenderer;
import com.clt.gui.GUI;
import com.clt.gui.Images;
import com.clt.gui.OptionPane;
import com.clt.gui.ProgressDialog;
import com.clt.gui.WindowUtils;
import com.clt.gui.menus.CmdMenuItem;
import com.clt.properties.DefaultEnumProperty;
import com.clt.properties.DefaultStringProperty;
import com.clt.properties.Property;
import com.clt.util.DefaultLongAction;
import com.clt.util.StringTools;

/**
 *
 *
 * @author Daniel Bobbert
 * @version 1.0
 */
public class NodeSearchDialog extends JFrame {

    private static final Object ALL_DOCUMENTS = new Object() {

        @Override
        public String toString() {

            return Resources.getString("AllDocuments");
        }
    };

    private static NodeSearchDialog dialog = null;

    private FilterType[] filterTypes;

    private JPanel content;
    private List<Property<?>> filters;
    private JComboBox docfilter;

    private JButton findButton;

    private NodeSearchDialog() {

        super(Resources.getString("NodeSearch"));
        this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

        // content = new JPanel();
        NodeTypeSearchFilter.NodeClass nodeTypes[] = {
            new NodeTypeSearchFilter.NodeClass(StartNode.class),
            new NodeTypeSearchFilter.NodeClass(OutputNode.class),
            new NodeTypeSearchFilter.NodeClass(InputNode.class),
            new NodeTypeSearchFilter.NodeClass(ConditionalNode.class),
            new NodeTypeSearchFilter.NodeClass(SetVariableNode.class),
            new NodeTypeSearchFilter.NodeClass(TestVariableNode.class),
            new NodeTypeSearchFilter.NodeClass(ScriptNode.class),
            new NodeTypeSearchFilter.NodeClass(SleepNode.class),
            new NodeTypeSearchFilter.NodeClass(EndNode.class),
            new NodeTypeSearchFilter.NodeClass(ReturnNode.class),
            new NodeTypeSearchFilter.NodeClass(ContinueNode.class),
            new NodeTypeSearchFilter.NodeClass(LoopNode.class),
            new NodeTypeSearchFilter.NodeClass(LabelNode.class),
            new NodeTypeSearchFilter.NodeClass(GotoNode.class),
            new NodeTypeSearchFilter.NodeClass(CallNode.class),
            new NodeTypeSearchFilter.NodeClass(ProcNode.class),
            new NodeTypeSearchFilter.NodeClass(GraphNode.class),
            new NodeTypeSearchFilter.NodeClass(GroovyNode.class)};

        this.filterTypes = new FilterType[]{
            new FilterType(NodeTypeSearchFilter.getRelations(),
            new DefaultEnumProperty<NodeTypeSearchFilter.NodeClass>(
            NodeTypeSearchFilter.NAME, Resources
            .getString(NodeTypeSearchFilter.NAME), null, nodeTypes)) {

                @Override
                public NodeSearchFilter createFilterImpl(Object value, Object relation) {

                    if (value == null) {
                        throw new IllegalArgumentException();
                    }
                    return new NodeTypeSearchFilter(
                            ((NodeTypeSearchFilter.NodeClass) value)
                                    .getNodeType());
                }
            },
            new FilterType(NodeNameSearchFilter.getRelations(),
            new DefaultStringProperty(
            NodeNameSearchFilter.NAME, Resources
            .getString(NodeNameSearchFilter.NAME),
            null, "")) {

                @Override
                public NodeSearchFilter createFilterImpl(Object value, Object relation) {

                    if (StringTools.isEmpty((String) value)) {
                        throw new IllegalArgumentException();
                    }
                    return new NodeNameSearchFilter((String) value, relation);
                }
            },
            new FilterType(UsesVariableSearchFilter.getRelations(),
            new DefaultStringProperty(
            UsesVariableSearchFilter.NAME, Resources
            .getString(UsesVariableSearchFilter.NAME), null, "")) {

                @Override
                public NodeSearchFilter createFilterImpl(Object value, Object relation) {

                    if (StringTools.isEmpty((String) value)) {
                        throw new IllegalArgumentException();
                    }
                    return new UsesVariableSearchFilter((String) value, relation);
                }
            }};

        this.content = new JPanel(new GridBagLayout());

        this.filters = new Vector<Property<?>>();

        JPanel p = new JPanel(new BorderLayout());

        JPanel p_docfilter = new JPanel(new BorderLayout(12, 12));
        p_docfilter.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        p_docfilter.add(new JLabel(Resources.getString("SearchIn") + ":"),
                BorderLayout.WEST);
        this.docfilter = new JComboBox();
        this.docfilter.setRenderer(new ComponentRenderer(this.docfilter
                .getRenderer()) {

            @Override
            public Component getListCellRendererComponent(JList list, Object value,
                    int index,
                    boolean isSelected, boolean cellHasFocus) {

                Component c
                        = super.getListCellRendererComponent(list, value, index, isSelected,
                                cellHasFocus);
                if ((value instanceof Document) && (c instanceof JLabel)) {
                    ((JLabel) c).setText(((Document) value).getTitle());
                }
                return c;
            }
        });
        this.docfilter.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent evt) {

            }
        });
        p_docfilter.add(this.docfilter, BorderLayout.CENTER);
        p.add(p_docfilter, BorderLayout.NORTH);

        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowActivated(WindowEvent evt) {

                Vector<Object> docs = new Vector<Object>();
                docs.add(NodeSearchDialog.ALL_DOCUMENTS);
                docs.add(new JSeparator());
                docs.addAll(Arrays.asList(Main.getDocuments()));
                NodeSearchDialog.this.docfilter
                        .setModel(new DefaultComboBoxModel(docs) {

                            @Override
                            public void setSelectedItem(Object o) {

                                if (!(o instanceof JSeparator)) {
                                    super.setSelectedItem(o);
                                }
                            }
                        });
            }
        });

        p.add(this.content, BorderLayout.CENTER);

        this.findButton
                = new CmdButton(Resources.getString("Find"), new Runnable() {

                    public void run() {

                        NodeSearchDialog.this.find();
                    }
                });

        JButton cancelButton
                = new CmdButton(GUI.getString("Cancel"), new Runnable() {

                    public void run() {

                        NodeSearchDialog.this.setVisible(false);
                    }
                });

        JPanel bp = new JPanel(new BorderLayout());
        bp.add(new JSeparator(), BorderLayout.NORTH);

        JPanel buttons
                = GUI.createButtonPanel(new JButton[]{this.findButton, cancelButton});
        buttons.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        bp.add(buttons, BorderLayout.CENTER);
        p.add(bp, BorderLayout.SOUTH);

        this.setContentPane(p);

        this.addFilterAfter(this.filters.size() - 1);

        GUI.setDefaultButtons(this, this.findButton, cancelButton);

        JMenuBar mbar = new JMenuBar();
        JMenu menu = new JMenu("File");
        menu.add(new CmdMenuItem("Close", KeyEvent.VK_W, new Runnable() {

            public void run() {

                // setVisible(false);
                NodeSearchDialog.this.dispatchEvent(new WindowEvent(
                        NodeSearchDialog.this, WindowEvent.WINDOW_CLOSING));
            }
        }));
        mbar.add(menu);
        this.getLayeredPane().add(mbar);

        this.pack();
        Dimension size = this.getSize();
        size.width = Math.max(size.width, 400);
        size.height = Math.max(size.height, 250);
        this.setSize(size);

        WindowUtils.setLocationRelativeTo(this, this.getParent());
    }

    private void find() {

        if (this.docfilter.getSelectedItem() == null) {
            return;
        }

        final NodeSearchFilter filter = this.getFilter();
        if (filter != null) {
            final List<SearchResult> matches = new LinkedList<SearchResult>();
            try {
                new ProgressDialog(this).run(new DefaultLongAction(Resources
                        .getString("Find")) {

                    @Override
                    public void run(ProgressListener l) {

                        Document[] sources;
                        if (NodeSearchDialog.this.docfilter.getSelectedItem() == NodeSearchDialog.ALL_DOCUMENTS) {
                            sources = Main.getDocuments();
                        } else {
                            sources
                                    = new Document[]{(Document) NodeSearchDialog.this.docfilter
                                                .getSelectedItem()};
                        }

                        for (int i = 0; i < sources.length; i++) {
                            if (sources[i] instanceof SingleDocument) {
                                matches.addAll(((SingleDocument) sources[i]).find(filter));
                            }
                        }
                    }
                });
                if (matches.size() == 0) {
                    OptionPane.message(this, "No matching nodes found.");
                } else {
                    SearchResultsDialog.show(null, Resources.getString("SearchResults"),
                            matches);
                }
            } catch (InvocationTargetException exn) {
                exn.printStackTrace();
                OptionPane.error(this, exn.getTargetException());
            }
        }
    }

    private void addFilterAfter(int index) {

        Property<?> filterSetup
                = new DefaultEnumProperty<FilterType>("Filter", "Filter", null,
                        this.filterTypes, this.filterTypes[0]);
        this.filters.add(index + 1, filterSetup);
        filterSetup.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent evt) {

                NodeSearchDialog.this.initContent();
            }
        });
        this.initContent();
    }

    private void initContent() {

        this.content.removeAll();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int n = 0;
        for (Iterator<Property<?>> it = this.filters.iterator(); it.hasNext(); n++) {
            final int index = n;
            Property<?> filterSetup = it.next();

            gbc.weightx = 0.0;
            gbc.insets = new Insets(3, 6, 3, 6);
            this.content.add(filterSetup.createEditor(false), gbc);
            FilterType type = (FilterType) filterSetup.getValueAsObject();
            gbc.gridx++;
            this.content.add(type.getRelation().createEditor(false), gbc);
            gbc.gridx++;
            gbc.weightx = 1.0;
            JComponent editor = type.createEditor();
            /*
       * editor.addKeyListener(new KeyAdapter() { public void
       * keyPressed(KeyEvent evt) { if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
       * findButton.doClick(); findButton.repaint(); } } });
             */
            this.content.add(editor, gbc);

            JButton add
                    = Buttons.createImageButton(Images.load("Plus.png"), new int[]{
                Buttons.NORMAL, Buttons.DISABLED, Buttons.PRESSED});
            JButton delete
                    = Buttons.createImageButton(Images.load("Minus.png"), new int[]{
                Buttons.NORMAL, Buttons.DISABLED, Buttons.PRESSED});

            add.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent evt) {

                    NodeSearchDialog.this.addFilterAfter(index);
                }
            });
            delete.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent evt) {

                    NodeSearchDialog.this.filters.remove(index);
                    NodeSearchDialog.this.initContent();
                }
            });
            if (this.filters.size() == 1) {
                delete.setEnabled(false);
            }

            gbc.weightx = 0.0;
            gbc.gridx++;
            gbc.insets = new Insets(3, 6, 3, 6);
            this.content.add(delete, gbc);
            gbc.gridx++;
            gbc.insets = new Insets(3, 0, 3, 6);
            this.content.add(add, gbc);

            gbc.gridy++;
            gbc.gridx = 0;
        }

        gbc.weighty = 1.0;
        this.content.add(new JPanel(), gbc);

        Dimension size = this.getPreferredSize();
        this.setSize(this.getWidth(), Math.max(this.getHeight(), size.height));
        this.getContentPane().validate();
        this.updateButtons();
        this.repaint();
    }

    private void updateButtons() {

        this.findButton.setEnabled(this.getFilter() != null);
    }

    private NodeSearchFilter getFilter() {

        List<NodeSearchFilter> filters = new Vector<NodeSearchFilter>();
        for (int i = 0; i < this.filters.size(); i++) {
            try {
                filters.add(((FilterType) this.filters.get(i).getValueAsObject())
                        .createFilter());
            } catch (Exception ignore) {
            }
        }
        if (filters.size() == 0) {
            return null;
        } else if (filters.size() == 1) {
            return filters.get(0);
        } else {
            return new And(filters.toArray(new NodeSearchFilter[filters.size()]));
        }
    }

    private abstract class FilterType {

        Property<?> relation;
        Property<?> value;

        public FilterType(Object[] relations, Property<?> value) {

            if ((relations == null) || (relations.length == 0)) {
                relations = new String[]{""};
            }

            if (relations.length == 1) {
                this.relation = new DefaultStringProperty("relation", "Relation", null,
                        relations[0].toString()) {

                    @Override
                    protected JComponent createEditorComponent(int editType, boolean label) {

                        return new JLabel(this.getValueAsString());
                    }
                };
            } else {
                this.relation
                        = new DefaultEnumProperty<Object>("relation", "Relation", null,
                                relations, relations[0]);
            }
            this.value = value;
            value.addChangeListener(new ChangeListener() {

                public void stateChanged(ChangeEvent evt) {

                    NodeSearchDialog.this.updateButtons();
                }
            });
        }

        public JComponent createEditor() {

            return this.value.createEditor(false);
        }

        public NodeSearchFilter createFilter() {

            return this.createFilterImpl(this.value.getValueAsObject(), this.relation
                    .getValueAsObject());
        }

        protected abstract NodeSearchFilter createFilterImpl(Object value,
                Object relation);

        public Property<?> getRelation() {

            return this.relation;
        }

        @Override
        public String toString() {

            return this.value.getName();
        }
    }

    public static void showDialog() {

        if (NodeSearchDialog.dialog == null) {
            NodeSearchDialog.dialog = new NodeSearchDialog();
        }
        NodeSearchDialog.dialog.setVisible(true);
        NodeSearchDialog.dialog.toFront();
    }
}
