package com.clt.diamant.graph.nodes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.clt.diamant.Device;
import com.clt.diamant.DialogInput;
import com.clt.diamant.ExecutionLogger;
import com.clt.diamant.Grammar;
import com.clt.diamant.IdMap;
import com.clt.diamant.InputCenter;
import com.clt.diamant.Mapping;
import com.clt.diamant.Resources;
import com.clt.diamant.Slot;
import com.clt.diamant.WozInterface;
import com.clt.diamant.graph.Edge;
import com.clt.diamant.graph.Graph;
import com.clt.diamant.graph.HandlerEdge;
import com.clt.diamant.graph.InputHandler;
import com.clt.diamant.graph.Node;
import com.clt.diamant.graph.SpecialEdge;
import com.clt.diamant.graph.search.SearchResult;
import com.clt.diamant.graph.ui.EdgeConditionModel;
import com.clt.diamant.gui.NodePropertiesDialog;
import com.clt.gui.CmdButton;
import com.clt.gui.ComponentRenderer;
import com.clt.gui.GUI;
import com.clt.gui.InputDialog;
import com.clt.gui.OptionPane;
import com.clt.gui.border.GroupBorder;
import com.clt.gui.table.TableRowDragger;
import com.clt.gui.table.TextRenderer;
import com.clt.script.exp.Match;
import com.clt.script.exp.Pattern;
import com.clt.script.exp.Type;
import com.clt.script.exp.Value;
import com.clt.script.exp.values.IntValue;
import com.clt.util.Timeout;
import com.clt.xml.AbstractHandler;
import com.clt.xml.XMLReader;
import com.clt.xml.XMLWriter;

public class InputNode
    extends OutputNode {

  private static final boolean differentiateContinueNodes = true;

  // don't change! These names are written to XML.
  public static final String BARGE_IN_WAIT = "bargeinWait";
  public static final String BARGE_IN_RESET = "bargeinReset";
  public static final String BARGE_IN_SEND = "bargeinSend";
  public static final String TIMEOUT = "timeout";
  public static final String CATCH_ALL = "catchAll";
  public static final String FORCE_TIMEOUT = "forceTimeout";
  public static final String DEVICE = "device";
  public static final String DISCARD_OLD_INPUT = "discard";
  public static final String IGNORE_HANDLERS = "ignoreInputHandlers";
  
  public EdgeConditionModel edgeModel;


  public InputNode() {

    // super(new Color(255, 153, 153));
    super();

    this.removeAllEdges();

    this.setProperty(InputNode.BARGE_IN_WAIT, new Hashtable<Device, Boolean>());
    this
      .setProperty(InputNode.BARGE_IN_RESET, new Hashtable<Device, Boolean>());
    this.setProperty(InputNode.BARGE_IN_SEND, new Hashtable<Device, String>());
    this.setProperty(InputNode.IGNORE_HANDLERS, Boolean.FALSE);
  }


  public static Color getDefaultColor() {

    return new Color(255, 90, 90);
  }


  @SuppressWarnings("unchecked")
  protected Map<Device, Boolean> getBargeinWaitMap(
      Map<String, Object> properties, boolean create) {

    Map<Device, Boolean> h = (Map)properties.get(InputNode.BARGE_IN_WAIT);
    if ((h == null) && create) {
      h = new Hashtable<Device, Boolean>();
      properties.put(InputNode.BARGE_IN_WAIT, h);
    }
    return h;
  }


  @SuppressWarnings("unchecked")
  protected Map<Device, Boolean> getBargeinResetMap(
      Map<String, Object> properties, boolean create) {

    Map<Device, Boolean> h = (Map)properties.get(InputNode.BARGE_IN_RESET);
    if ((h == null) && create) {
      h = new Hashtable<Device, Boolean>();
      properties.put(InputNode.BARGE_IN_RESET, h);
    }
    return h;
  }


  @SuppressWarnings("unchecked")
  protected Map<Device, String> getBargeinSendMap(
      Map<String, Object> properties, boolean create) {

    Map<Device, String> h = (Map)properties.get(InputNode.BARGE_IN_SEND);
    if ((h == null) && create) {
      h = new Hashtable<Device, String>();
      properties.put(InputNode.BARGE_IN_SEND, h);
    }
    return h;
  }


  public void update(Mapping map) {

    super.update(map);

    Device dev = (Device)this.getProperty(InputNode.DEVICE);
    if (dev != null) {
      Device targetDev = map.getDevice(dev);
      if (targetDev != null) {
        this.setProperty(InputNode.DEVICE, targetDev);
      }
    }

    Map<Device, Boolean> waitMap =
      this.getBargeinWaitMap(this.properties, false);
    if (waitMap != null) {
      this.setProperty(InputNode.BARGE_IN_WAIT, this.updateDeviceMap(waitMap,
        map));
    }

    Map<Device, Boolean> resetMap =
      this.getBargeinResetMap(this.properties, false);
    if (resetMap != null) {
      this.setProperty(InputNode.BARGE_IN_RESET, this.updateDeviceMap(resetMap,
        map));
    }

    Map<Device, String> sendMap =
      this.getBargeinSendMap(this.properties, false);
    if (sendMap != null) {
      this.setProperty(InputNode.BARGE_IN_SEND, this.updateDeviceMap(sendMap,
        map));
    }

    if (this.getGraph() != null) {
      // make sure all handler edges are up to date
      for (int i = 0; i < this.numEdges(); i++) {
        Edge e = this.getEdge(i);
        if (e instanceof HandlerEdge) {
          HandlerEdge he = (HandlerEdge)e;
          he.setHandler(map.getHandler(he.getHandler()));
          he.setEndNode((EndNode)map.getNode(he.getEndNode()));
        }
      }
    }
  }


  @Override
  public void updateEdges() {

    this.updateEdges(false);
  }


  public void updateEdges(boolean convertCatchAll) {

    Map<Node, Node> handlerTargets = new HashMap<Node, Node>();
    List<Edge> patternEdges = new ArrayList<Edge>();
    TimeoutEdge timeoutEdge = null;
    CatchAllEdge catchAll = null;
    for (int i = 0; i < this.numEdges(); i++) {
      Edge e = this.getEdge(i);
      if (e instanceof HandlerEdge) {
        HandlerEdge he = (HandlerEdge)e;
        if (InputNode.differentiateContinueNodes) {
          handlerTargets.put(he.getEndNode(), he.getTarget());
        }
        else {
          handlerTargets.put(he.getHandler(), he.getTarget());
        }
      }
      else if (e instanceof TimeoutEdge) {
        timeoutEdge = (TimeoutEdge)e;
      }
      else if (e instanceof CatchAllEdge) {
        catchAll = (CatchAllEdge)e;
      }
      else {
        patternEdges.add(e);
      }
    }

    if ((timeoutEdge == null) && (this.getProperty(InputNode.TIMEOUT) != null)) {
      // Convert old style node: last edge is timeout edge.
      Edge e = this.getEdge(this.numEdges() - 1);
      timeoutEdge = new TimeoutEdge(e.getSource(), e.getTarget());
      patternEdges.remove(e);
    }

    if (!this.hasCatchAll() && (catchAll == null) && !patternEdges.isEmpty()
      && convertCatchAll) {
      Edge lastEdge = patternEdges.get(patternEdges.size() - 1);
      if ("_".equals(lastEdge.getCondition())) {
        Node target = lastEdge.getTarget();
        catchAll = new CatchAllEdge(this, target);
        this.setProperty(InputNode.CATCH_ALL, Boolean.TRUE);
        patternEdges.remove(lastEdge);
      }
    }

    this.reinstallEdges(handlerTargets, patternEdges, timeoutEdge, catchAll);
  }


  private void reinstallEdges(Map<Node, Node> handlerTargets,
      List<? extends Edge> patternEdges,
            TimeoutEdge timeoutEdge, CatchAllEdge catchAll) {

    this.removeAllEdges();
    if (!this.getBooleanProperty(InputNode.IGNORE_HANDLERS)) {
      for (InputHandler ih : this.getGraph().getPrefixHandlers()) {
        if (ih.hasContinuation()) {
          if (InputNode.differentiateContinueNodes) {
            for (ContinueNode end : ih.getContinuations()) {
              this.addEdge(new HandlerEdge(this, handlerTargets.get(end), ih,
                end));
            }
          }
          else {
            this.addEdge(new HandlerEdge(this, handlerTargets.get(ih), ih));
          }
        }
      }
    }
    for (Edge e : patternEdges) {
      this.addEdge(e);
    }

    if (!this.getBooleanProperty(InputNode.IGNORE_HANDLERS)) {
      for (InputHandler ih : this.getGraph().getPostfixHandlers()) {
        if (ih.hasContinuation()) {
          if (InputNode.differentiateContinueNodes) {
            for (ContinueNode end : ih.getContinuations()) {
              this.addEdge(new HandlerEdge(this, handlerTargets.get(end), ih,
                end));
            }
          }
          else {
            this.addEdge(new HandlerEdge(this, handlerTargets.get(ih), ih));
          }
        }
      }
    }

    if (this.hasCatchAll()) {
      if (catchAll != null) {
        this.addEdge(catchAll);
      }
      else {
        this.addEdge(new CatchAllEdge(this));
      }
    }

    String timeout = (String)this.getProperty(InputNode.TIMEOUT);
    if (timeout != null) {
      if (timeoutEdge != null) {
        this.addEdge(timeoutEdge);
      }
      else {
        this.addEdge(new TimeoutEdge(this));
      }
    }
  }


  private boolean hasCatchAll() {

    return this.getBooleanProperty(InputNode.CATCH_ALL);
  }


  public Color getPortColor(int portNumber) {

    if (this.getEdge(portNumber) instanceof TimeoutEdge) {
      // if (getProperty(TIMEOUT) != null && portNumber == numEdges()-1)
      return new Color(255, 153, 153);
    }
    else if (this.getEdge(portNumber) instanceof CatchAllEdge) {
      return super.getPortColor(portNumber).darker();
    }
    else if (this.getEdge(portNumber) instanceof HandlerEdge) {
      return new Color(255, 153, 255);
    }
    else {
      return super.getPortColor(portNumber);
    }
  }


  /*
   * public Edge addEdge(Edge e) { if (e.getProperty(TIMEOUT) != null) return
   * super.addEdge(new TimeoutEdge(e.getSource(), e.getTarget())); else if
   * (e.getProperty("handler") != null) { if (e.getProperty("node") != null)
   * return super.addEdge(new HandlerEdge(e.getSource(), e.getTarget(),
   * (InputHandler) e.getProperty("handler"), (EndNode) e.getProperty("node")));
   * else return super.addEdge(new HandlerEdge(e.getSource(), e.getTarget(),
   * (InputHandler) e.getProperty("handler"))); } else return super.addEdge(e);
   * }
   */

  public JComponent createEditorComponent(final Map<String, Object> properties) {

    JComponent c = super.createEditorComponent(properties);
    JTabbedPane tabs;
    int optionTab = -1;
    Component optionPanel = null;

    if (c instanceof JTabbedPane) {
      tabs = (JTabbedPane)c;
      for (int i = 0; i < tabs.getTabCount(); i++) {
        if (tabs.getTitleAt(i).equals(Resources.getString("Options"))) {
          optionTab = i;
          optionPanel = tabs.getComponentAt(i);
        }
      }
    }
    else {
      tabs = GUI.createTabbedPane();
      tabs.addTab(Resources.getString("Prompt"), c);
    }

    JPanel p = new JPanel(new BorderLayout());
    p.add(Box.createVerticalStrut(8), BorderLayout.NORTH);

    List<InputHandler> prefix = this.getGraph().getPrefixHandlers();
    List<InputHandler> postfix = this.getGraph().getPostfixHandlers();

    final EdgeConditionModel model = new EdgeConditionModel(this, properties,
            Resources.getString("InputPatterns"), prefix, postfix,
            !this.getBooleanProperty(InputNode.IGNORE_HANDLERS));
    this.edgeModel = model;
    final JTable table = new JTable(model);
    // table.setRowSelectionAllowed(false);
    // table.setColumnSelectionAllowed(false);
    table.getTableHeader().setReorderingAllowed(false);
    TableRowDragger.addDragHandler(table);
    TableColumn column = table.getColumnModel().getColumn(0);
    column.setCellRenderer(new TextRenderer() {

      public Component getTableCellRendererComponent(JTable table,
          Object value,
                    boolean isSelected, boolean hasFocus, int row, int column)
            {

              JLabel label =
                (JLabel)super.getTableCellRendererComponent(table, value,
                    isSelected, hasFocus, row, column);
              if (!model.isCellEditable(row, column)) {
                label.setForeground(Color.lightGray);
              }
              return label;
            }
    });

    JScrollPane jsp = GUI.createScrollPane(table, new Dimension(300, 150));
    JPanel center = new JPanel(new BorderLayout());
    center.add(jsp, BorderLayout.CENTER);
    center.add(NodePropertiesDialog.createCheckBox(properties,
      InputNode.CATCH_ALL,
            Resources.getString("IncludeCatchAll")), BorderLayout.SOUTH);
    p.add(center, BorderLayout.CENTER);

    JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));

    final JButton deleteButton = new CmdButton(new Runnable() {

      public void run() {

        if (table.isEditing() ? table.getCellEditor().stopCellEditing() : true) {
          model.deleteRows(table.getSelectedRows());
        }
      }
    }, Resources.getString("Delete"));
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

        if (table.isEditing() ? table.getCellEditor().stopCellEditing() : true) {
          int row = model.addRow();
          table.setRowSelectionInterval(row, row);
        }
      }
    }, Resources.getString("New"));
    buttons.add(newButton);

    p.add(buttons, BorderLayout.SOUTH);
    p.add(Box.createHorizontalStrut(8), BorderLayout.WEST);
    p.add(Box.createHorizontalStrut(8), BorderLayout.EAST);

    /*
     * final JComboBox rule = new JComboBox(); rule.addActionListener(new
     * ActionListener() { public void actionPerformed(ActionEvent e) { if
     * (rule.getSelectedItem() == null) properties.remove("rule"); else
     * properties.put("rule", rule.getSelectedItem()); } });
     * 
     * 
     * Vector grammars = new Vector();
     * grammars.addElement(Resources.getString("DontChange")); for (Enumeration
     * gs = getGraph().getOwner().getGrammars().elements();
     * gs.hasMoreElements(); ) grammars.addElement(gs.nextElement());
     * 
     * if (grammars.size() > 1) grammars.insertElementAt("", 1);
     * 
     * final JComboBox grammar = new JComboBox(grammars);
     * grammar.addActionListener(new ActionListener() { public void
     * actionPerformed(ActionEvent e) { if (grammar.getSelectedItem() instanceof
     * Grammar) { String r = (String) properties.get("rule");
     * 
     * rule.setEnabled(true); rule.setModel(new
     * DefaultComboBoxModel(Misc.hashKeys(((Grammar)
     * grammar.getSelectedItem()).getRules()))); properties.put("grammar",
     * grammar.getSelectedItem());
     * 
     * rule.setSelectedIndex(-1); for (int i=0; i<rule.getItemCount(); i++) { if
     * (rule.getItemAt(i).equals(r)) { rule.setSelectedIndex(i); break; } } }
     * else if (grammar.getSelectedIndex() == 0) { rule.setModel(new
     * DefaultComboBoxModel(new Object[] { Resources.getString("DontChange")
     * })); rule.setEnabled(false); properties.remove("grammar");
     * properties.remove("rule"); } else if (grammar.getSelectedIndex() == 1) {
     * grammar.setSelectedIndex(0); } } });
     * 
     * 
     * Grammar g = (Grammar) properties.get("grammar"); int gr_index =
     * grammars.indexOf(g); if (gr_index >= 0)
     * grammar.setSelectedIndex(gr_index); else grammar.setSelectedIndex(0);
     */

    Vector<Object> devs = new Vector<Object>();
    devs.add(Resources.getString("All"));
    for (Device dev : this.getGraph().getOwner().getDevices()) {
      devs.add(dev);
    }

    if (devs.size() > 1) {
      devs.add(1, new JSeparator());
    }

    final JComboBox device = new JComboBox(devs);
    device.setRenderer(new ComponentRenderer(device.getRenderer()));
    device.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {

        if (device.getSelectedItem() instanceof Device) {
          properties.put(InputNode.DEVICE, device.getSelectedItem());
        }
        else if (device.getSelectedIndex() == 0) {
          properties.remove(InputNode.DEVICE);
        }
        else if (device.getSelectedIndex() == 1) {
          device.setSelectedIndex(0);
        }
      }
    });

    Device d = (Device)properties.get(InputNode.DEVICE);
    int d_index = (d == null ? -1 : devs.indexOf(d));
    if (d_index >= 0) {
      device.setSelectedIndex(d_index);
    }
    else {
      device.setSelectedIndex(0);
    }

    JPanel options = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();

    gbc.weightx = 0.0;
    gbc.weighty = 0.0;
    gbc.insets = new Insets(3, 12, 3, 0);
    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.gridwidth = 1;

    gbc.gridy = 0;
    gbc.gridx = 0;
    options.add(new JLabel(Resources.getString("Devices") + ':'), gbc);
    gbc.gridx++;
    options.add(device, gbc);

    /*
     * gbc.gridy++; gbc.gridx = 0; options.add(new
     * JLabel(Resources.getString("Grammar") + ':'), gbc); gbc.gridx++;
     * options.add(grammar, gbc);
     * 
     * gbc.gridy++; gbc.gridx = 0; options.add(new
     * JLabel(Resources.getString("Rule") + ':'), gbc); gbc.gridx++;
     * options.add(rule, gbc);
     */

    gbc.gridy++;
    gbc.gridx = 0;
    gbc.gridwidth = 2;
    options.add(NodePropertiesDialog.createCheckBox(properties,
      InputNode.DISCARD_OLD_INPUT,
            Resources.getString("DiscardOldInput")), gbc);

    gbc.gridy++;
    final JCheckBox cb =
      NodePropertiesDialog.createCheckBox(properties,
        InputNode.IGNORE_HANDLERS,
            Resources.getString("IgnoreInputHandlers"));
    cb.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent evt) {

        model.showPreAndPostfix(!cb.isSelected());
      }
    });
    options.add(cb, gbc);
    gbc.gridwidth = 1;

    gbc.gridy++;
    gbc.gridx = 0;
    final JTextField tf =
      NodePropertiesDialog.createTextField(properties, InputNode.TIMEOUT);
    final JCheckBox timeout =
      new JCheckBox(Resources.getString("Timeout") + ':');
    final JCheckBox forceTimeout =
      NodePropertiesDialog.createCheckBox(properties,
            InputNode.FORCE_TIMEOUT, Resources
              .getString(InputNode.FORCE_TIMEOUT));

    timeout.addItemListener(new ItemListener() {

      public void itemStateChanged(ItemEvent evt) {

        tf.setEnabled(timeout.isSelected());
        forceTimeout.setEnabled(timeout.isSelected());
        if (timeout.isSelected()) {
          tf.selectAll();
        }
        else {
          properties.remove(InputNode.TIMEOUT);
          forceTimeout.setSelected(false);
        }
      }
    });
    timeout.setSelected(properties.get(InputNode.TIMEOUT) != null);
    forceTimeout.setEnabled(timeout.isSelected());

    Insets insets = gbc.insets;
    gbc.insets = new Insets(insets.top, insets.left, 0, insets.right);

    options.add(timeout, gbc);
    gbc.gridx++;
    options.add(tf, gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    gbc.gridwidth = 2;
    gbc.insets = new Insets(0, insets.left + 12, insets.bottom, insets.right);
    options.add(forceTimeout, gbc);

    gbc.gridy++;
    gbc.insets = insets;
    options.add(new JLabel("Barge-In"), gbc);
    gbc.gridy++;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;

    JTable bargein_table =
      new JTable(new BargeInTableModel(this.getGraph().getOwner().getDevices(),
            properties));
    bargein_table.getTableHeader().setReorderingAllowed(false);
    bargein_table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

    TableColumn name = bargein_table.getColumnModel().getColumn(0);
    // name.setResizable(false);
    name.setPreferredWidth(150);
    name.setMinWidth(name.getPreferredWidth());
    name.setMaxWidth(name.getPreferredWidth());
    name.setCellRenderer(new TextRenderer());

    TableColumn wait = bargein_table.getColumnModel().getColumn(1);
    wait.setResizable(false);
    wait.setPreferredWidth(70);
    wait.setMinWidth(wait.getPreferredWidth());
    wait.setMaxWidth(wait.getPreferredWidth());

    TableColumn reset = bargein_table.getColumnModel().getColumn(2);
    reset.setResizable(false);
    reset.setPreferredWidth(70);
    reset.setMinWidth(reset.getPreferredWidth());
    reset.setMaxWidth(reset.getPreferredWidth());

    TableColumn output = bargein_table.getColumnModel().getColumn(3);
    // output.setResizable(false);
    // output.setMinWidth(120);
    output.setCellRenderer(new TextRenderer());

    JScrollPane bargein_jsp = GUI.createScrollPane(bargein_table, 150);
    // jsp.setBorder(BorderFactory.createEmptyBorder());
    GUI.setupScrollBar(bargein_jsp.getVerticalScrollBar());
    GUI.setupScrollBar(bargein_jsp.getHorizontalScrollBar());
    options.add(bargein_jsp, gbc);

    if (optionPanel == null) {
      if (optionTab == -1) {
        tabs.addTab(Resources.getString("Options"), options);
        optionTab = tabs.getTabCount() - 1;
      }
      else {
        tabs.setComponentAt(optionTab, options);
      }
    }
    else {
      tabs.removeTabAt(optionTab);

      JPanel combinedOptions = new JPanel(new GridBagLayout());

      ((JPanel)optionPanel).setBorder(new GroupBorder(Resources
        .getString("Output")));
      options.setBorder(new GroupBorder(Resources.getString("Input")));

      gbc.gridx = gbc.gridy = 0;
      gbc.weightx = 1.0;
      gbc.weighty = 0.0;
      gbc.insets = new Insets(3, 6, 3, 6);

      gbc.fill = GridBagConstraints.BOTH;
      gbc.anchor = GridBagConstraints.NORTHWEST;
      combinedOptions.add(optionPanel, gbc);
      gbc.gridy++;
      gbc.weighty = 1.0;
      combinedOptions.add(options, gbc);

      tabs.addTab(Resources.getString("Options"), combinedOptions);
    }

    tabs.insertTab(Resources.getString("Input"), null, p, null, optionTab);

    return tabs;
  }


  public boolean editProperties(Component parent) {

    TimeoutEdge timeoutEdge = null;
    CatchAllEdge catchAll = null;

    List<Edge> explicitEdges = new ArrayList<Edge>();
    Map<Node, Node> handlerTargets = new HashMap<Node, Node>();
    for (int i = 0; i < this.numEdges(); i++) {
      Edge e = this.getEdge(i);
      if (e instanceof TimeoutEdge) {
        timeoutEdge = (TimeoutEdge)e;
      }
      else if (e instanceof CatchAllEdge) {
        catchAll = (CatchAllEdge)e;
      }
      else if (e instanceof HandlerEdge) {
        if (InputNode.differentiateContinueNodes) {
          handlerTargets.put(((HandlerEdge)e).getEndNode(), e.getTarget());
        }
        else {
          handlerTargets.put(((HandlerEdge)e).getHandler(), e.getTarget());
        }
      }
      else {
        explicitEdges.add(e);
      }
    }

    this.setProperty(EdgeConditionModel.EDGE_PROPERTY, explicitEdges);

    if (super.editProperties(parent)) {
      @SuppressWarnings("unchecked")
      List<Edge> es =
        (List<Edge>)this.getProperty(EdgeConditionModel.EDGE_PROPERTY);

      this.reinstallEdges(handlerTargets, es, timeoutEdge, catchAll);

      return true;
    }
    else {
      return false;
    }
  }


  public String getDescription(Edge selectedEdge) {

    StringBuilder buffer =
      new StringBuilder(super.getDescription(selectedEdge));

    buffer.append("<p><b>" + this.html(Resources.getString("Keywords"))
      + ":</b>");
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
  public Node execute(WozInterface comm, InputCenter inputCenter, ExecutionLogger logger) {
	logNode(logger);
    /*
     * Vector devices = new Vector() { // overwrite "equals" for input queue
     * filtering public boolean equals(Object o) { // return true, if the
     * DialogInput object o comes from one of the devices in this vector
     * DialogInput di = (DialogInput) o; return contains(di.getDevice()); } };
     * Device dev = (Device) getProperty(DEVICE); if (dev != null)
     * devices.addElement(dev); else { for (Enumeration ds =
     * getGraph().getOwner().getDevices().elements(); ds.hasMoreElements(); ) {
     * Device d = (Device) ds.nextElement(); devices.addElement(d); } }
     * 
     * if (getBooleanProperty(DISCARD_OLD_INPUT)) comm.discardOldInput(dev);
     */

    Device inputDevice = (Device)this.getProperty(InputNode.DEVICE);
    Collection<Device> devices;
    if (inputDevice != null) {
      devices = Collections.singleton(inputDevice);
    }
    else {
      devices = this.getGraph().getOwner().getDevices();
    }

    if (this.getBooleanProperty(InputNode.DISCARD_OLD_INPUT)) {
      for (Device d : devices) {
        comm.discardOldInput(d);
      }
    }

    this.prompt(comm, logger);

    String t = (String)this.getProperty(InputNode.TIMEOUT);

    for (Device d : devices) {
      try {
        d.allowTimeout(t != null);
      } catch (Exception ignore) {
      }
    }

    long timeout = 0;

    if (t != null) {
      try {
        timeout = ((IntValue)this.parseExpression(t).evaluate(comm)).getInt();
      } catch (Exception exn) {
        throw new NodeExecutionException(this, Resources
          .getString("IllegalTimeoutValue")
                        + " " + t, exn, logger);
      }
      if (timeout < 0) {
        throw new NodeExecutionException(this, Resources
          .getString("IllegalTimeoutValue")
                      + " " + t, logger);
      }
    }

    boolean forceTimeout = this.getBooleanProperty(InputNode.FORCE_TIMEOUT);

    List<String> patterns = new ArrayList<String>();
    List<Integer> patternTargets = new ArrayList<Integer>();

    Map<Node, Integer> handlerTargets = new HashMap<Node, Integer>();

    List<InputHandler> prefix;
    List<InputHandler> postfix;

    if (this.getBooleanProperty(InputNode.IGNORE_HANDLERS)) {
      prefix = new ArrayList<InputHandler>();
      postfix = new ArrayList<InputHandler>();
    }
    else {
      prefix = this.getGraph().getPrefixHandlers();
      postfix = this.getGraph().getPostfixHandlers();
    }

    int timeoutEdge = -1;
    int catchAllEdge = -1;

    for (int i = 0; i < this.numEdges(); i++) {
      Edge e = this.getEdge(i);
      if (e instanceof TimeoutEdge) {
        timeoutEdge = i;
      }
      else if (e instanceof CatchAllEdge) {
        catchAllEdge = i;
      }
      else if (e instanceof HandlerEdge) {
        if (InputNode.differentiateContinueNodes) {
          handlerTargets.put(((HandlerEdge)e).getEndNode(), new Integer(i));
        }
        else {
          handlerTargets.put(((HandlerEdge)e).getHandler(), new Integer(i));
        }
      }
      else {
        patterns.add(e.getCondition());
        patternTargets.add(new Integer(i));
      }
    }

    Pattern[] alternatives =
      new Pattern[prefix.size() + patterns.size() + postfix.size()];
    Object[] actions = new Object[alternatives.length];

    int n = 0;
    for (int i = 0; i < prefix.size(); i++) {
      InputHandler h = prefix.get(i);
      if (h.getPattern().trim().length() == 0) {
        throw new NodeExecutionException(this, Resources
          .getString("EmptyPattern"), logger);
      }
      try {
        alternatives[n] = this.parsePattern(h.getPattern());
        actions[n] = h;
        n++;
      } catch (Exception exn) {
        throw new NodeExecutionException(this, Resources
          .getString("IllegalPattern")
          + ": "
                        + h.getPattern(), exn, logger);
      }
    }
    for (int i = 0; i < patterns.size(); i++) {
      if (patterns.get(i).trim().length() == 0) {
        throw new NodeExecutionException(this, Resources
          .getString("EmptyPattern"), logger);
      }

      try {
        alternatives[n] = this.parsePattern(patterns.get(i));
        actions[n] = patternTargets.get(i);
        n++;
      } catch (Exception exn) {
        throw new NodeExecutionException(this, Resources
          .getString("IllegalPattern")
          + ": "
                        + patterns.get(i), exn, logger);
      }
    }
    for (int i = 0; i < postfix.size(); i++) {
      InputHandler h = postfix.get(i);
      if (h.getPattern().trim().length() == 0) {
        throw new NodeExecutionException(this, Resources
          .getString("EmptyPattern"), logger);
      }
      try {
        alternatives[n] = this.parsePattern(h.getPattern());
        actions[n] = h;
        n++;
      } catch (Exception exn) {
        throw new NodeExecutionException(this, Resources
          .getString("IllegalPattern")
          + ": "
                        + h.getPattern(), exn, logger);
      }
    }

    while (true) {
      DialogInput<?> input =
        comm.getInput(alternatives, inputDevice,
                this.getGraph().getOwner().getDevices(), this
                  .getBargeInWaitDevices(), timeout, forceTimeout);

      Object action = null;
      Match match = null;

      if (input.getInput() instanceof Timeout) {
        action = input.getInput();
      }
      else {
        for (int i = 0; (i < alternatives.length) && (action == null); i++) {
          Pattern p = alternatives[i];
          if (p != null) {
            // first test for pointer equality, in order to handle "fake"
            // interfaces
            // that return one of the alternatives (such as the Wizard of Oz)
            if (input.getInput() == p) {
              if (!(comm instanceof Component)) {
                throw new IllegalStateException(
                                  "WozInterfaces that return a pattern from getInput() must be a subclass of java.awt.Component");
              }
              Component parent = (Component)comm;
              String[] vars = p.getFreeVars().stringArray();
              if (vars.length > 0) {
                match = this.enterVariableBindings(parent, vars);
                if (match == null) {
                  action = null;
                  break;
                }
              }

              action = actions[i];
            }
            else if (input.getInput() instanceof Value) {
              match = p.match((Value)input.getInput());
              if (match != null) {
                action = actions[i];
              }
            }
          }
        }
      }

      if (action != null) {
        // Bind variables
        if (match != null) {
          List<Slot> accessible_vars =
            this.getGraph().getAllVariables(Graph.LOCAL);
          for (Iterator<String> vars = match.variables(); vars.hasNext();) {
            String name = vars.next();
            Slot v = null;
            for (int j = accessible_vars.size() - 1; (j >= 0) && (v == null); j--) {
              Slot s = accessible_vars.get(j);
              if (name.equals(s.getName())) {
                v = s;
              }
            }
            if (v != null) {
              v.setValue(match.get(name));
            }
            else {
              throw new NodeExecutionException(this,
                              "Attempt to bind non existing variable " + name, logger);
            }
          }
        }

        this.sendBargeInCompletion(comm, logger);

        // System.out.println("Continue");
        int targetEdge = -1;
        if (action instanceof Timeout) {
          targetEdge = timeoutEdge;
        }
        else if (action instanceof InputHandler) {
          InputHandler h = (InputHandler)action;
          Node result = h.execute(comm, inputCenter, logger);
          if (result instanceof LoopNode) {
            return this.execute(comm, inputCenter, logger);
          }
          else if (result instanceof ContinueNode) {
            if (InputNode.differentiateContinueNodes) {
              targetEdge = handlerTargets.get(result).intValue();
            }
            else {
              targetEdge = handlerTargets.get(h).intValue();
            }
          }
          else {
            return result;
          }
        }
        else if (action instanceof Integer) {
          targetEdge = ((Integer)action).intValue();
        }
        else {
          throw new IllegalStateException("Unexpected action");
        }

        Edge e = this.getEdge(targetEdge);
        comm.transition(this, e.getTarget(), targetEdge, e.getCondition());
        return e.getTarget();
      }
      else if (catchAllEdge >= 0) {
        Edge e = this.getEdge(catchAllEdge);
        comm.transition(this, e.getTarget(), catchAllEdge, e.getCondition());
        return e.getTarget();
      }
      else {
        comm.error("NoMatch", "Input couldn't be matched: " + input.getInput());
        // throw new NodeExecutionException(this,
        // StringTools.format(Resources.getString("NoMatchAfter"), "endless"));
      }
    }
  }


  private List<Device> getBargeInWaitDevices() {

    Map<Device, Boolean> bargeinWait =
      this.getBargeinWaitMap(this.properties, false);
    List<Device> waitDevices = new Vector<Device>();
    for (Device d : bargeinWait.keySet()) {
      Boolean wait = bargeinWait.get(d);
      if (wait != null ? wait.booleanValue() : false) {
        waitDevices.add(d);
      }
    }

    return waitDevices;
  }


  private Match enterVariableBindings(Component parent, String[] vars) {

    Match match = null;

    while (match == null) {
      match = new Match();
      InputDialog in = new InputDialog(parent, "Variable binding",
                "Enter values for bound pattern variables:", vars);
      if (in.getResult() == InputDialog.RESULT_CANCEL) {
        return null;
      }
      for (int j = 0; (j < vars.length) && (match != null); j++) {
        try {
          match.put(vars[j], this.parseExpression(in.getInput(j)).evaluate());
        } catch (Exception exn) {
          OptionPane.error(parent, exn);
          match = null;
        }
      }
    }

    return match;
  }


  private void sendBargeInCompletion(WozInterface comm, ExecutionLogger logger) {

    // now send bargein resets and signals
    Map<Device, Boolean> bargeinReset =
      this.getBargeinResetMap(this.properties, false);
    if (bargeinReset != null) {
      for (Device d : bargeinReset.keySet()) {
        if (bargeinReset.get(d).booleanValue()) {
          // System.out.println("Sending reset to " + d);
          try {
            d.reset();
          } catch (Exception ignore) {
          }
        }
      }
    }

    Map<Device, String> bargeinSend =
      this.getBargeinSendMap(this.properties, false);
    if (bargeinSend != null) {
      for (final Device d : bargeinSend.keySet()) {
        String exp = bargeinSend.get(d);
        if (exp.trim().length() > 0) {
          try {
            final Value v = this.parseExpression(exp).evaluate(comm);
            new Thread(new Runnable() {

              public void run() {

                try {
                  // System.out.println("Sending bargein signal to " + d);
                  d.send(v);
                  // System.out.println("Done: " + d);
                }
                                catch (Exception ignore) {
                                }
                              }
            }, "BargeIn send to " + d).start();
          } catch (Exception exn) {
            throw new NodeExecutionException(
              this,
                            "Error while evaluating bargein message for device "
                              + d, exn, logger);
          }
        }
      }
    }
  }


  @Override
  protected void writeAttributes(XMLWriter out, IdMap uid_map) {

    super.writeAttributes(out, uid_map);

    String timeout = (String)this.getProperty(InputNode.TIMEOUT);
    if (timeout != null) {
      Graph.printAtt(out, InputNode.TIMEOUT, timeout);
    }
    boolean forceTimeout = this.getBooleanProperty(InputNode.FORCE_TIMEOUT);
    if (forceTimeout) {
      Graph.printAtt(out, InputNode.FORCE_TIMEOUT, forceTimeout);
    }

    boolean catchAll = this.getBooleanProperty(InputNode.CATCH_ALL);
    if (catchAll) {
      Graph.printAtt(out, InputNode.CATCH_ALL, catchAll);
    }

    Device dev = (Device)this.getProperty(InputNode.DEVICE);
    if (dev != null) {
      try {
        String id = uid_map.devices.getKey(dev);
        Graph.printAtt(out, InputNode.DEVICE, id);
      } catch (NoSuchElementException exn) {
        // device no longer exists. Simply ignore this info.
      }
    }

    Grammar g = (Grammar)this.getProperty("grammar");
    if (g != null) {
      try {
        Graph.printAtt(out, "grammar", uid_map.grammars.getKey(g));
        String rule = (String)this.getProperty("rule");
        if (rule != null) {
          Graph.printAtt(out, "rule", rule);
        }
      } catch (NoSuchElementException exn) {
        // grammar no longer exists. Ignore this info.
      }
    }

    Graph.printAtt(out, "boolean", InputNode.DISCARD_OLD_INPUT,
            this.getBooleanProperty(InputNode.DISCARD_OLD_INPUT) ? "1" : "0");
    Graph.printAtt(out, "boolean", InputNode.IGNORE_HANDLERS, this
      .getBooleanProperty(InputNode.IGNORE_HANDLERS) ? "1"
                : "0");

    Map<Device, Boolean> bargeinWait =
      this.getBargeinWaitMap(this.properties, false);
    Map<Device, Boolean> bargeinReset =
      this.getBargeinResetMap(this.properties, false);
    Map<Device, String> bargeinSend =
      this.getBargeinSendMap(this.properties, false);

    Collection<Device> devices = this.getGraph().getOwner().getDevices();
    for (Device d : devices) {
      Boolean wait = bargeinWait != null ? bargeinWait.get(d) : null;
      Boolean reset = bargeinReset != null ? bargeinReset.get(d) : null;
      String send = bargeinSend != null ? bargeinSend.get(d) : null;
      if ((reset != null) || (wait != null) || (send != null)) {
        Graph.printAtt(out, "list", "bargein", null);
        Graph.printAtt(out, "device", d.getId());
        if (wait != null ? wait.booleanValue() : false) {
          Graph.printAtt(out, "wait", wait.booleanValue());
        }
        if (reset != null ? reset.booleanValue() : false) {
          Graph.printAtt(out, "reset", reset.booleanValue());
        }
        if (send != null) {
          Graph.printAtt(out, "send", send);
        }
        out.closeElement("att");
      }
    }
  }


  @Override
  public void readAttribute(final XMLReader r, String name, String value,
      final IdMap uid_map)
        throws SAXException {

    if (name.equals(InputNode.TIMEOUT)) {
      this.setProperty(InputNode.TIMEOUT, value);
    }
    else if (name.equals(InputNode.FORCE_TIMEOUT)) {
      this.setProperty(InputNode.FORCE_TIMEOUT, value.equals("1")
        ? Boolean.TRUE : Boolean.FALSE);
    }
    else if (name.equals(InputNode.CATCH_ALL)) {
      this.setProperty(InputNode.CATCH_ALL, value.equals("1") ? Boolean.TRUE
        : Boolean.FALSE);
    }
    else if (name.equals(InputNode.DEVICE)) {
      try {
        Device d = uid_map.devices.get(value);
        this.setProperty(InputNode.DEVICE, d);
      } catch (Exception exn) {
        r.raiseAttributeValueException(InputNode.DEVICE);
      }
    }
    else if (name.equals("grammar")) {
      try {
        Grammar g = uid_map.grammars.get(value);
        this.setProperty("grammar", g);
      } catch (Exception exn) {
        r.raiseAttributeValueException("grammar");
      }
    }
    else if (name.equals("rule")) {
      this.setProperty("rule", value);
    }
    else if (name.equals(InputNode.DISCARD_OLD_INPUT)) {
      this.setProperty(name, value.equals("1") ? Boolean.TRUE : Boolean.FALSE);
    }
    else if (name.equals(InputNode.IGNORE_HANDLERS)) {
      this.setProperty(name, value.equals("1") ? Boolean.TRUE : Boolean.FALSE);
    }
    else if (name.equals("bargein")) {
      r.setHandler(new AbstractHandler("att") {

        Device d = null;
        boolean wait = false;
        boolean reset = false;
        String send = null;


        public void start(String name, Attributes atts)
            throws SAXException {

          if (name.equals("att")) {
            String att = atts.getValue("name");
            if (att.equals("device")) {
              try {
                this.d = uid_map.devices.get(atts.getValue("value"));
              }
                            catch (NoSuchElementException exn) {
                              r.raiseAttributeValueException("device");
                            }
                          }
                          else if (att.equals("wait")) {
                            this.wait = atts.getValue("value").equals("1");
                          }
                          else if (att.equals("reset")) {
                            this.reset = atts.getValue("value").equals("1");
                          }
                          else if (att.equals("send")) {
                            this.send = atts.getValue("value");
                          }
                          r.setHandler(new AbstractHandler("att"));
                        }
                      }


        public void end(String name) {

          if (name.equals("att")) {
            if ((this.d != null) && this.wait) {
              InputNode.this.getBargeinWaitMap(InputNode.this.properties, true)
                .put(this.d, Boolean.TRUE);
            }
            if ((this.d != null) && this.reset) {
              InputNode.this
                .getBargeinResetMap(InputNode.this.properties, true).put(
                  this.d, Boolean.TRUE);
            }
            if ((this.d != null) && (this.send != null)) {
              InputNode.this.getBargeinSendMap(InputNode.this.properties, true)
                .put(this.d, this.send);
            }
          }
        }
      });
    }
    else {
      super.readAttribute(r, name, value, uid_map);
    }
  }


  public void validate(Collection<SearchResult> errors) {

    super.validate(errors);
    // there is nothing else to do

    Device dev = (Device)this.getProperty(InputNode.DEVICE);
    if (dev != null) {
      if (!this.getGraph().getOwner().getDevices().contains(dev)) {
        this.reportError(errors, false, Resources.format(
          "referencesInaccessibleDevice",
                  dev.getName()));
      }
    }

    String timeout = (String)this.getProperty(InputNode.TIMEOUT);
    if (timeout != null) {
      try {
        Type t = this.parseExpression(timeout).getType();
        Type.unify(t, Type.Int);
      } catch (Exception exn) {
        this.reportError(errors, false, Resources.format(
          "containsIllegalTimeoutExpression",
                    exn.getLocalizedMessage()));
      }
    }

    Map<Device, String> bargeinSend =
      this.getBargeinSendMap(this.properties, false); // (Hashtable)
                                                      // getProperty(BARGE_IN_SEND);
    if (bargeinSend != null) {
      for (final Device d : bargeinSend.keySet()) {
        String exp = bargeinSend.get(d);
        if (exp.trim().length() > 0) {
          try {
            @SuppressWarnings("unused")
            Type t = this.parseExpression(exp).getType();
          } catch (Exception exn) {
            this.reportError(errors, false, Resources
              .getString("containsIllegalExpression")
                                + ": " + exn.getLocalizedMessage());
          }
        }
      }
    }

    List<Slot> accessible_vars = this.getGraph().getAllVariables(Graph.LOCAL);
    for (int i = 0; i < this.numEdges(); i++) {
      Edge e = this.getEdge(i);
      if (!(e instanceof SpecialEdge)) {
        if (e.getCondition().trim().length() == 0) {
          this.reportError(errors, false, Resources
            .getString("containsEmptyInputPattern"));
        }
        else {
          try {
            Pattern p = this.parsePattern(e.getCondition());
            Map<String, Type> variablesTypes = new HashMap<String, Type>();
            for (String varName : p.getFreeVars()) {
              Slot v = null;
              for (int j = accessible_vars.size() - 1; (j >= 0) && (v == null); j--) {
                Slot s = accessible_vars.get(j);
                if (varName.equals(s.getName())) {
                  v = s;
                  variablesTypes.put(varName, v.getType());
                }
              }
              if (v == null) {
                this.reportError(errors, false, Resources.format(
                                  "containsIllegalInputPattern",
                                  "Attempt to bind non existing variable "
                                    + varName));
              }
            }
            p.getType(variablesTypes);
          } catch (Exception exn) {
            this.reportError(errors, false, Resources.format(
              "containsIllegalInputPattern",
                            exn.getLocalizedMessage()));
          }
        }
      }
    }
  }


  @Override
  protected void writeVoiceXML(XMLWriter w, IdMap uid_map) {

    // vxml
    w.openElement("field", new String[] { "name" }, new String[] { "input" });

    super.writeVoiceXML(w, uid_map);

    w.closeElement("field");
  }

  static class BargeInTableModel
        extends AbstractTableModel {

    private List<Device> devices;
    private Map<Device, Boolean> bargeinWait;
    private Map<Device, Boolean> bargeinReset;
    private Map<Device, String> bargeinSend;


    @SuppressWarnings("unchecked")
    public BargeInTableModel(Collection<Device> devices,
        final Map<String, Object> properties) {

      this.devices = new ArrayList<Device>(devices);

      this.bargeinWait =
        (Map<Device, Boolean>)properties.get(InputNode.BARGE_IN_WAIT);
      if (this.bargeinWait == null) {
        this.bargeinWait = new Hashtable<Device, Boolean>();
        properties.put(InputNode.BARGE_IN_WAIT, this.bargeinWait);
      }
      this.bargeinReset =
        (Map<Device, Boolean>)properties.get(InputNode.BARGE_IN_RESET);
      if (this.bargeinReset == null) {
        this.bargeinReset = new Hashtable<Device, Boolean>();
        properties.put(InputNode.BARGE_IN_RESET, this.bargeinReset);
      }
      this.bargeinSend =
        (Map<Device, String>)properties.get(InputNode.BARGE_IN_SEND);
      if (this.bargeinSend == null) {
        this.bargeinSend = new Hashtable<Device, String>();
        properties.put(InputNode.BARGE_IN_SEND, this.bargeinSend);
      }
    }


    public int getRowCount() {

      if (this.devices != null) {
        return this.devices.size();
      }
      return 0;
    }


    public int getColumnCount() {

      return 4;
    }


    public String getColumnName(int columnIndex) {

      switch (columnIndex) {
        case 0:
          return Resources.getString("Device");
        case 1:
          return Resources.getString("Wait");
        case 2:
          return Resources.getString("Reset");
        case 3:
          return Resources.getString("Send after input");
      }
      return null;
    }


    public Class<?> getColumnClass(int columnIndex) {

      switch (columnIndex) {
        case 0:
          return Device.class;
        case 1:
          return Boolean.class;
        case 2:
          return Boolean.class;
        case 3:
          return String.class;
      }
      return Object.class;
    }


    public boolean isCellEditable(int rowIndex, int columnIndex) {

      return columnIndex != 0;
    }


    public Object getValueAt(int rowIndex, int columnIndex) {

      if ((this.devices == null) || (rowIndex < 0)
        || (rowIndex >= this.devices.size())) {
        return null;
      }

      Device d = this.devices.get(rowIndex);

      switch (columnIndex) {
        case 0:
          return d;
        case 1:
          return this.bargeinWait.get(d);
        case 2:
          return this.bargeinReset.get(d);
        case 3:
          return this.bargeinSend.get(d);
      }
      return null;
    }


    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

      Device d = this.devices.get(rowIndex);

      switch (columnIndex) {
        case 0:
          break;
        case 1:
          this.bargeinWait.put(d, (Boolean)aValue);
          break;
        case 2:
          this.bargeinReset.put(d, (Boolean)aValue);
          break;
        case 3:
          this.bargeinSend.put(d, (String)aValue);
          break;
      }
    }
  }
  
  @Override
  public boolean acceptableToSave(){
  	return !existDuplicateEdgesOrEmptyLabels(this.edgeModel.getEdges());
  }
  
  /**
   * 
   * @param edgeList
   * @return true if edge labels are empty or duplicate
   */
  public boolean existDuplicateEdgesOrEmptyLabels(List<Edge> edgeList){
  	ArrayList<String> labels = new ArrayList<>(edgeList.size());
  	for(Edge edge : edgeList){
  		String label = edge.getCondition();
  		if(label.equals("")){
  			return true;
  		}
  		else if(labels.contains(edge.getCondition())){
  			return true;
  		}
  		else{
  			labels.add(edge.getCondition());
  		}
  	}
  	return false;
  }
}