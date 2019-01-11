package com.clt.diamant.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.RootPaneContainer;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import com.clt.diamant.Resources;
import com.clt.diamant.graph.Node;
import com.clt.diamant.graph.VisualGraphElement;
import com.clt.diamant.graph.ui.EdgeConditionModel;
import com.clt.gui.CmdButton;
import com.clt.gui.GUI;
import com.clt.gui.WindowUtils;
import com.clt.gui.table.TableRowDragger;
import com.clt.gui.table.TextRenderer;
import com.clt.script.ScriptEditor;
import com.clt.script.UndoRedoTextComponent;
import com.clt.util.StringTools;
import java.awt.event.ItemEvent;
import javax.swing.SwingUtilities;

/**
 * NodePropertiesDialog contains several static methods which given a property
 * map and a string (a key) returns a GUI element (ComboBox, TextField, etc).
 * Each GUI element will change the respective entry in the property
 * datastructure whenever a change (ActionListener, CaretListener, etc) occurs.
 */
public class NodePropertiesDialog extends JDialog {

    public static final String LAST_TAB = "__last_tab__",
            LAST_POSITION = "__last_position__",
            LAST_SIZE = "__last_size__";

    private boolean approved = false;
    private JButton cancelButton;
    private Node node;

    /**
     * A boolean that is set to true if something in the dialog has been changed
     * by the user.
     */
    private static boolean _propertiesChanged;

    /**
     * Opens a NodePropertiesDialog.
     *
     * @param node the node whose properties should be edited
     * @param parent the parent component of the dialog
     * @param properties the properties of the node
     * @param editor the editor component of the node (if the editor is a
     * JTabbedpane, all tabs get added to the dialog)
     */
    public NodePropertiesDialog(Node node, final Component parent, final Map<String, Object> properties, Container editor) {
        super(GUI.getFrameForComponent(parent), true);

        this.node = node;

        _propertiesChanged = false;

        this.setTitle(Resources.format("PropertiesForNode", properties.get("title")));
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        // setResizable(false);

        this.init(this.createEditor(node, properties, editor));

        GUI.assignMnemonics(this.getContentPane());
        this.pack();

        Dimension size = (Dimension) properties.get(NodePropertiesDialog.LAST_SIZE);
        if (size != null) {
            Dimension min = this.getMinimumSize();
            this.setSize(Math.max(size.width, min.width), Math.max(size.height, min.height));
        } else {
            this.setSize(Math.max(this.getSize().width, 400), Math.max(this.getSize().height, 200));
        }

        Point pos = (Point) properties.get(NodePropertiesDialog.LAST_POSITION);
        if (pos != null) {
            this.setLocation(pos.x, pos.y);
        } else {
            WindowUtils.setLocationRelativeTo(this, parent);
        }

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentMoved(ComponentEvent evt) {
                properties.put(NodePropertiesDialog.LAST_POSITION, NodePropertiesDialog.this.getLocation());
            }

            @Override
            public void componentResized(ComponentEvent evt) {
                properties.put(NodePropertiesDialog.LAST_SIZE, NodePropertiesDialog.this.getSize());
            }
        });
    }

    public static final ActionListener okAction = evt -> {
        if (evt.getSource() instanceof Component) {
            Component c = (Component) evt.getSource();
            while ((c != null) && !(c instanceof RootPaneContainer)) {
                c = c.getParent();
            }
            if (c != null) {
                JButton b = ((RootPaneContainer) c).getRootPane().getDefaultButton();
                if (b != null) {
                    b.doClick();
                }
            }
        }
    };

    public static final KeyListener cancelAction = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            if ((e.getSource() instanceof Component) && (e.getKeyCode() == KeyEvent.VK_ESCAPE)) {
                Component c = (Component) e.getSource();
                Window w = GUI.getWindowForComponent(c);
                if (w instanceof NodePropertiesDialog) {
                    JButton b = ((NodePropertiesDialog) w).getCancelButton();
                    if (b != null) {
                        b.doClick();
                    }
                }
            }
        }
    };

    public static JTextArea createTextAreaWithoutJScrollPane(Map<String, Object> properties, String key) {
        final JTextArea f = new JTextArea();
        f.setLineWrap(true);
        f.setWrapStyleWord(true);
        f.setTabSize(4);

        Object o = properties.get(key);
        if (o != null) {
            f.setText((String) o);
            f.setCaretPosition(0);
        }
        f.addCaretListener(new CaretListener() {

            public void caretUpdate(CaretEvent e) {
                _propertiesChanged = true;

                properties.put(key, f.getText());
            }
        });
        f.addKeyListener(NodePropertiesDialog.cancelAction);
        return f;
    }

    public static JScrollPane createTextArea(final Map<String, Object> properties, final String key) {
        final JTextArea f = createTextAreaWithoutJScrollPane(properties, key);

        JScrollPane jsp = new JScrollPane(f, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jsp.setMinimumSize(new Dimension(300, 150));
        jsp.setPreferredSize(jsp.getMinimumSize());
        return jsp;
    }

    /**
     * Gives back a Component with an editor for the Script language.
     *
     * @param properties Property Map
     * @param key the key for the property in the Property Map
     * @return the editor component
     */
    public static JComponent createScriptEditor(final Map<String, Object> properties, final String key) {
        final JEditorPane f = new ScriptEditor(ScriptEditor.Type.SCRIPT);
        final UndoRedoTextComponent scriptEditor = new UndoRedoTextComponent(f);

        Object o = properties.get(key);
        if (o != null) {
            f.setText((String) o);
        } else {
            f.setText("");
        }

        f.addCaretListener(evt -> {
            _propertiesChanged = true;
            properties.put(key, f.getText());
        });
        f.addKeyListener(NodePropertiesDialog.cancelAction);

        return scriptEditor;
    }

    /**
     * Gives back a Component with an editor for Groovy script.
     *
     * @param properties Property Map
     * @param key the key for the property in the Property Map
     * @return the editor component
     */
    public static JComponent createGroovyScriptEditor(final Map<String, Object> properties, final String key) {
        RSyntaxTextArea textArea = new RSyntaxTextArea(20, 60);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_GROOVY);
        textArea.setCodeFoldingEnabled(true);

        final UndoRedoTextComponent scriptEditor = new UndoRedoTextComponent(textArea);

        Object o = properties.get(key);
        if (o != null) {
            textArea.setText((String) o);
        }

        textArea.addCaretListener(new CaretListener() {

            public void caretUpdate(CaretEvent e) {
                _propertiesChanged = true;
                properties.put(key, textArea.getText());
            }
        });
        textArea.addKeyListener(NodePropertiesDialog.cancelAction);
        return scriptEditor;
    }

    /**
     * Gives back a Component with an editor for the Outgoing Edges of a node.
     *
     * @param properties Property Map
     * @param key the key for the property in the Property Map
     * @param node the node whose outgoing edges are edited
     * @return the editor component
     */
    public static Component createOutgoingEdgesEditor(final Map<String, Object> properties, final String key, Node node) {
        JPanel edgesP = new JPanel(new BorderLayout());

        // creating a table for the outgoing edges
        final EdgeConditionModel model = new EdgeConditionModel(node, properties, Resources.getString("OutgoingEdges")) {

            /**
             * Returns whether a cell may be edited. Returns true if row is
             * greater than 0.
             *
             * @param row Row of the cell
             * @param column Column of the cell
             * @return True if row is greater than 0
             */
            @Override
            public boolean isCellEditable(int row, int column) {
                return row > 0;
            }

            /**
             * Returns whether a cell may be moved. Returns true if row is
             * greater than 0.
             *
             * @return True if row is greater than 0.
             */
            @Override
            public boolean isRowMovable(int row) {
                return isCellEditable(row, 0);
            }

            /**
             * Moves a row. A row can't be moved to the first row
             *
             * @return True if the row was moved.
             */
            @Override
            public boolean moveRow(int column, int from, int to) {
                if (to < 1) {
                    return false;
                }
                return super.moveRow(column, from, to);
            }
        };
        final JTable table = new JTable(model);
        table.getTableHeader().setReorderingAllowed(false);
        table.getColumnModel().getColumn(0).setCellRenderer(new TextRenderer() {

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {

                JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                        column);
                if (!model.isCellEditable(row, column)) {
                    c.setForeground(Color.lightGray);
                }
                return c;
            }
        });
        TableRowDragger.addDragHandler(table);

        model.addTableModelListener(evt -> {
            _propertiesChanged = true;
            node.setProperty("temp edges", model.getEdges());
        });

        // adding the table to a ScrollPane
        JScrollPane jsp = GUI.createScrollPane(table, new Dimension(300, 150));
        edgesP.add(jsp, BorderLayout.CENTER);

        // creating a panel for the "delete" and "new" buttons
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        final JButton deleteButton = new CmdButton(()
                -> model.deleteRows(table.getSelectedRows()), Resources.getString("Delete")
        );
        buttons.add(deleteButton);
        deleteButton.setEnabled(table.getSelectedRow() >= 0);
        table.getSelectionModel().addListSelectionListener(evt -> {
            // if the first row (default) is selected, the delete button is
            // disabled
            deleteButton.setEnabled(table.getSelectedRow() >= 1);
        });

        final JButton newButton = new CmdButton(() -> {
            int row = model.addRow();
            table.setRowSelectionInterval(row, row);
        }, Resources.getString("New"));
        buttons.add(newButton);

        edgesP.add(buttons, BorderLayout.SOUTH);
        edgesP.add(Box.createHorizontalStrut(8), BorderLayout.WEST);
        edgesP.add(Box.createHorizontalStrut(8), BorderLayout.EAST);

        return edgesP;
    }

    public static JTextField createTextField(final Map<String, Object> properties, final String key) {
        final JTextField f = new JTextField(20);
        f.setMinimumSize(f.getPreferredSize());
        Object o = properties.get(key);
        if (o != null) {
            f.setText((String) o);
        }
        f.addActionListener(NodePropertiesDialog.okAction);
        f.addCaretListener(evt -> {
            _propertiesChanged = true;
            properties.put(key, f.getText());
        });
        f.addKeyListener(NodePropertiesDialog.cancelAction);
        return f;
    }

    public static JTextField createLongField(final Map<String, Object> properties, final String key, final long min,
            final long max) {
        final JTextField f = new JTextField(20);
        f.setMinimumSize(f.getPreferredSize());
        Object o = properties.get(key);
        if (o != null) {
            f.setText(o.toString());
        }
        f.addActionListener(NodePropertiesDialog.okAction);
        f.addCaretListener(new CaretListener() {

            Color defaultBackground = null;

            public void caretUpdate(CaretEvent e) {
                _propertiesChanged = true;
                try {
                    if (StringTools.isEmpty(f.getText())) {
                        properties.remove(key);
                    } else {
                        long value = Long.parseLong(f.getText());
                        if ((value < min) || (value > max)) {
                            throw new NumberFormatException("Value out of range");
                        }
                        properties.put(key, value);
                    }
                    if (this.defaultBackground != null) {
                        f.setBackground(this.defaultBackground);
                    }
                } catch (NumberFormatException exn) {
                    if (this.defaultBackground == null) {
                        this.defaultBackground = f.getBackground();
                    }
                    f.setBackground(new Color(255, 128, 128));
                }
            }
        });
        f.addKeyListener(NodePropertiesDialog.cancelAction);
        return f;
    }

    public static JComboBox createComboBox(final Map<String, Object> properties, final String key, final Object[] values) {
        return NodePropertiesDialog.createComboBox(properties, key, Arrays.asList(values));
    }

    public static JComboBox createComboBox(final Map<String, Object> properties, final String key, final Collection<?> values) {
        final JComboBox cb = new JComboBox(new Vector<Object>(values));
        int minWidth = 120;
        int width = Math.min(240, Math.max(minWidth, cb.getPreferredSize().width));
        cb.setPreferredSize(new Dimension(width, cb.getPreferredSize().height));
        cb.setMinimumSize(new Dimension(minWidth, cb.getPreferredSize().height));
        Object k = properties.get(key);

        SwingUtilities.invokeLater(() -> {
            if ((k != null) && values.contains(k)) {
                cb.setSelectedItem(k);
            } else {
                cb.setSelectedItem(null);
            }
        });

        cb.addActionListener(evt -> {
            Object o = cb.getSelectedItem(); // may be null if "values"
            // is empty
            if (o != null) {
                properties.put(key, o);
            }
        });
        return cb;
    }

    /**
     * Creates a combobox that manages an int property from its values.
     *
     * @param properties
     * @param key
     * @param values
     * @return
     */
    public static JComboBox createIntComboBox(final Map<String, Object> properties, final String key, final Collection<?> values) {
        final JComboBox cb = new JComboBox(new Vector<Object>(values));
        int minWidth = 120;
        int width = Math.min(240, Math.max(minWidth, cb.getPreferredSize().width));
        cb.setPreferredSize(new Dimension(width, cb.getPreferredSize().height));
        cb.setMinimumSize(new Dimension(minWidth, cb.getPreferredSize().height));

        Object k = properties.get(key);
        SwingUtilities.invokeLater(() -> {
            if ((k != null) && k instanceof Integer) {
                cb.setSelectedIndex((Integer) k);
            } else {
                cb.setSelectedIndex(0);
            }
        });
        
        cb.addActionListener(evt -> {
            int index = cb.getSelectedIndex();
            properties.put(key, index);
        });

        return cb;
    }

    public static JCheckBox createCheckBox(final Map<String, Object> properties, final String key, final String title) {
        final JCheckBox cb = new JCheckBox(title != null ? title : key);
        Object o = properties.get(key);
        cb.setSelected(o == null ? false : ((Boolean) o).booleanValue());
        cb.addItemListener(evt -> {
            _propertiesChanged = true;
            properties.put(key, cb.isSelected() ? Boolean.TRUE : Boolean.FALSE);
        });
        return cb;
    }

    public static JRadioButton[] createRadioButtons(Map<String, Object> properties, String key, Object[] values) {
        ButtonGroup group = new ButtonGroup();
        JRadioButton[] buttons = new JRadioButton[values.length];
        for (int i = 0; i < values.length; i++) {
            buttons[i] = NodePropertiesDialog.createRadioButton(properties, key, values[i]);
            group.add(buttons[i]);
        }
        return buttons;
    }

    public static JRadioButton createRadioButton(final Map<String, Object> properties, final String key,
            final Object value) {
        final JRadioButton b = new JRadioButton(value.toString());
        b.setSelected(value.equals(properties.get(key)));
        b.addItemListener(evt -> {
            _propertiesChanged = true;
            if (b.isSelected()) {
                properties.put(key, value);
            }
        });
        return b;
    }

    private Container createEditor(Node node, final Map<String, Object> properties, Container editor) {
        JPanel general = new JPanel(new GridBagLayout());
        general.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        // general.setMinimumSize(new Dimension(300, 200));
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(6, 12, 0, 0);
        gbc.insets = new Insets(3, 6, 3, 6);
        general.add(new JLabel(Resources.getString("Title") + ':'), gbc);
        gbc.gridx++;
        gbc.gridwidth = 2;
        general.add(NodePropertiesDialog.createTextField(properties, "title"), gbc);

        gbc.gridx++;
        gbc.weightx = 1.0;
        gbc.gridwidth = 1;
        general.add(Box.createHorizontalGlue(), gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        general.add(new JLabel(Resources.getString("Color") + ':'), gbc);
        gbc.gridx++;
        final JComponent swatch = new JComponent() {

            @Override
            public Dimension getMinimumSize() {
                return this.getPreferredSize();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(30, 16);
            }

            @Override
            public boolean isOpaque() {
                return true;
            }

            @Override
            public void paintComponent(Graphics g) {
                g.setColor((Color) properties.get("color"));
                g.fillRect(0, 0, this.getWidth(), this.getHeight());
                g.setColor(Color.black);
                g.drawRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);
            }
        };
        swatch.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Color c = JColorChooser.showDialog(swatch, Resources.getString("ChooseColor"),
                        (Color) properties.get("color"));
                if (c != null) {
                    properties.put("color", c);
                    swatch.repaint();
                }
            }
        });
        general.add(swatch, gbc);
        gbc.gridx++;
        final JButton change = new JButton(Resources.getString("Change") + "...");
        change.addActionListener(evt -> {
            Color c = JColorChooser.showDialog(change, Resources.getString("ChooseColor"),
                    (Color) properties.get("color"));
            if (c != null) {
                properties.put("color", c);
            }
            swatch.repaint();
        });
        general.add(change, gbc);

        gbc.gridx++;
        gbc.weightx = 1.0;
        gbc.gridwidth = 1;
        general.add(Box.createHorizontalGlue(), gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        general.add(new JLabel(Resources.getString("Comment") + ':'), gbc);

        gbc.gridx++;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.BOTH;
        general.add(NodePropertiesDialog.createTextArea(properties, VisualGraphElement.COMMENT), gbc);
        gbc.gridwidth = 1;

        final JTabbedPane tabs = GUI.createTabbedPane();

        if (editor instanceof JTabbedPane) {
            JTabbedPane tp = (JTabbedPane) editor;
            for (int i = tp.getTabCount() - 1; i >= 0; i--) {
                String title = tp.getTitleAt(i);
                Component c = tp.getComponentAt(i);
                tp.removeTabAt(i);
                tabs.insertTab(title, null, c, null, 0);
            }
        } else if (editor != null) {
            tabs.addTab(Node.getLocalizedNodeTypeName(node), editor);
        }

        tabs.addTab(Resources.getString("General"), general);

        tabs.addChangeListener(evt -> {
            properties.put(NodePropertiesDialog.LAST_TAB, tabs.getTitleAt(tabs.getSelectedIndex()));
        });

        String tab = (String) properties.get(NodePropertiesDialog.LAST_TAB);
        if (tab != null) {
            for (int i = 0; i < tabs.getTabCount(); i++) {
                if (tab.equals(tabs.getTitleAt(i))) {
                    tabs.setSelectedIndex(i);
                    break;
                }
            }
        } else {
            tabs.setSelectedIndex(0);
        }
        return tabs;
    }

    private void init(final Container node_editor) {
        Container c = this.getContentPane();
        c.setLayout(new BorderLayout());

        c.add(node_editor, BorderLayout.CENTER);

        JPanel ButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        this.cancelButton = new JButton(Resources.getString("Cancel"));
        this.cancelButton.addActionListener(evt -> {
            node_editor.setVisible(false);
            NodePropertiesDialog.this.approved = false;
            NodePropertiesDialog.this.dispose();
        });

        ButtonPanel.add(this.cancelButton);
        final JButton okButton = new JButton(Resources.getString("OK"));
        okButton.addActionListener(evt -> {
            if (NodePropertiesDialog.this.finishEditing(node_editor)) {
                if (node.acceptableToSave()) {
                    node_editor.setVisible(false);
                    NodePropertiesDialog.this.approved = true;
                    NodePropertiesDialog.this.dispose();
                } else {
                    JOptionPane.showMessageDialog(GUI.getFrameForComponent(getParent()),
                            Resources.getString("CannotSaveEdges"), Resources.getString("Error"),
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        this.addWindowListener(new WindowListener() {
            public void windowActivated(WindowEvent e) {
            }

            public void windowClosed(WindowEvent e) {
            }

            public void windowClosing(WindowEvent e) {
                if (_propertiesChanged) {
                    // TODO add SaveChangesNode to dialog
                    // Show option dialog that asks if the changes should be
                    // saved
                    Object[] options
                            = {Resources.getString("Save"), Resources.getString("DontSave"), Resources.getString("Cancel")};
                    int dialogReturn = JOptionPane.showOptionDialog(GUI.getFrameForComponent(getParent()),
                            Resources.getString("SaveChangesNodeInfo"), Resources.getString("SaveChanges?"),
                            JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                    switch (dialogReturn) {
                        case 0:
                            okButton.doClick();
                            break;
                        case 1:
                            cancelButton.doClick();
                            break;
                        case 2:
                        default:
                            break;
                    }
                } else {
                    cancelButton.doClick();
                    // node_editor.setVisible(false);
                    // NodePropertiesDialog.this.approved = false;
                    // NodePropertiesDialog.this.dispose();
                }
            }

            public void windowDeactivated(WindowEvent e) {
            }

            public void windowDeiconified(WindowEvent e) {
            }

            public void windowIconified(WindowEvent e) {
            }

            public void windowOpened(WindowEvent e) {
            }
        });

        GUI.setDefaultButtons(this, okButton, this.cancelButton);
        okButton.setPreferredSize(this.cancelButton.getPreferredSize());
        ButtonPanel.add(okButton);

        c.add(ButtonPanel, BorderLayout.SOUTH);
    }

    private boolean finishEditing(Component c) {
        if (c instanceof JTable) {
            JTable table = (JTable) c;
            return table.isEditing() ? table.getCellEditor().stopCellEditing() : true;
        } else if (c instanceof Container) {
            for (int i = ((Container) c).getComponentCount() - 1; i >= 0; i--) {
                if (!this.finishEditing(((Container) c).getComponent(i))) {
                    return false;
                }
            }
            return true;
        } else {
            return true;
        }
    }

    /**
     * @return true if the changes in the dialog where approved
     */
    public boolean approved() {
        return this.approved;
    }

    public JButton getCancelButton() {
        return this.cancelButton;
    }

}
