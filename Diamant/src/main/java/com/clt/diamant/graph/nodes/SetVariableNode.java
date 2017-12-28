package com.clt.diamant.graph.nodes;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.clt.diamant.ExecutionLogger;
import com.clt.diamant.IdMap;
import com.clt.diamant.InputCenter;
import com.clt.diamant.Mapping;
import com.clt.diamant.Resources;
import com.clt.diamant.Slot;
import com.clt.diamant.WozInterface;
import com.clt.diamant.graph.Edge;
import com.clt.diamant.graph.Graph;
import com.clt.diamant.graph.Node;
import com.clt.diamant.graph.search.SearchResult;
import com.clt.diamant.gui.NodePropertiesDialog;
import com.clt.gui.Buttons;
import com.clt.gui.Images;
import com.clt.script.exp.Expression;
import com.clt.script.exp.Type;
import com.clt.script.exp.Value;
import com.clt.util.StringTools;
import com.clt.xml.AbstractHandler;
import com.clt.xml.XMLReader;
import com.clt.xml.XMLWriter;

public class SetVariableNode
    extends Node {

  // property names. Don't change, these are written to XML.
  public static final String VAR_ID = "var_id";
  public static final String VAR_UID = "var_uid";
  public static final String VAR_NAME = "var_name";
  public static final String VAR_VALUE = "var_value";
  public static final String ASSIGNMENTS = "assignments";
  public static final String SIMULTANEOUS = "simultaneous";


  public SetVariableNode() {

    super();

    this.addEdge();
  }


  @Override
  protected Object deep_copy(Object o) {

    if (o instanceof VarAssignment) {
      VarAssignment va = ((VarAssignment)o);
      return new VarAssignment(va.getVariable(), va.getValue());
    }
    else {
      return super.deep_copy(o);
    }
  }


  @SuppressWarnings("unchecked")
  protected List<VarAssignment> getAssignments(Map<String, Object> properties,
      boolean create) {

    List<VarAssignment> assignments =
      (List<VarAssignment>)properties.get(SetVariableNode.ASSIGNMENTS);
    if ((assignments == null) && create) {
      assignments = new Vector<VarAssignment>();
      properties.put(SetVariableNode.ASSIGNMENTS, assignments);
    }
    return assignments;
  }


  @Override
  public JComponent createEditorComponent(Map<String, Object> properties) {

    JPanel p = new JPanel();
    p.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
    this.setupEditor(p, properties);
    return p;
  }


  private void setupEditor(final JPanel p, final Map<String, Object> properties) {

    p.removeAll();
    p.setLayout(new GridBagLayout());

    List<VarAssignment> assignments = this.getAssignments(properties, true);
    if (assignments.isEmpty()) {
      assignments.add(new VarAssignment());
    }

    final List<VarAssignment> ass = assignments;

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = gbc.gridy = 0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.CENTER;
    gbc.weighty = 0.0;

    final List<Slot> vars = this.getGraph().getAllVariables(Graph.LOCAL);

    for (int i = 0; i < assignments.size(); i++) {
      final int index = i;
      final VarAssignment va = assignments.get(index);

      gbc.gridx = 0;
      gbc.weightx = 0.0;
      gbc.insets = new Insets(3, 6, 3, 6);
      final JComboBox varname = new JComboBox(new Vector<Slot>(vars));
      varname.setPreferredSize(new Dimension(120,
        varname.getPreferredSize().height));
      Slot s = va.getVariable();
      if ((s != null) && vars.contains(s)) {
        varname.setSelectedItem(s);
      }
      else {
        varname.setSelectedItem(null);
      }
      varname.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {

          Object o = varname.getSelectedItem(); // may be null if "values" is
                                                // empty
          if (o != null) {
            va.setVariable((Slot)o);
          }
        }
      });

      p.add(varname, gbc);

      gbc.gridx++;
      p.add(new JLabel("="), gbc);

      gbc.gridx++;
      gbc.weightx = 1.0;
      final JTextField f = new JTextField(20);
      String value = va.getValue();
      if (value != null) {
        f.setText(value);
      }
      f.addActionListener(NodePropertiesDialog.okAction);
      f.addCaretListener(new CaretListener() {

        public void caretUpdate(CaretEvent e) {

          va.setValue(f.getText());
        }
      });
      f.addKeyListener(NodePropertiesDialog.cancelAction);
      p.add(f, gbc);

      JButton add =
        Buttons.createImageButton(Images.load("Plus.png"), new int[] {
                    Buttons.NORMAL, Buttons.DISABLED, Buttons.PRESSED });
      JButton delete =
        Buttons.createImageButton(Images.load("Minus.png"), new int[] {
                    Buttons.NORMAL, Buttons.DISABLED, Buttons.PRESSED });

      add.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent evt) {

          ass.add(index + 1, new VarAssignment());
          SetVariableNode.this.setupEditor(p, properties);
        }
      });

      if (ass.size() == 1) {
        delete.setEnabled(false);
      }
      else {
        delete.addActionListener(new ActionListener() {

          public void actionPerformed(ActionEvent evt) {

            ass.remove(va);
            SetVariableNode.this.setupEditor(p, properties);
          }
        });
      }

      gbc.gridx++;
      gbc.weightx = 0.0;
      gbc.insets = new Insets(3, 6, 3, 6);
      p.add(delete, gbc);
      gbc.gridx++;
      gbc.insets = new Insets(3, 0, 3, 6);
      p.add(add, gbc);

      gbc.gridy++;
    }

    gbc.gridx = 0;
    gbc.gridwidth = 5;
    gbc.weighty = 1.0;
    p.add(new JPanel(), gbc);

    gbc.gridy++;
    gbc.weighty = 0.0;
    p.add(NodePropertiesDialog.createCheckBox(properties,
      SetVariableNode.SIMULTANEOUS,
            Resources.getString("SimultaneousAssignments")), gbc);

    p.revalidate();
    p.repaint();
  }


  @Override
  public String getDescription(Edge selectedEdge) {

    StringBuilder buffer =
      new StringBuilder(super.getDescription(selectedEdge));

    List<VarAssignment> assigments =
      this.getAssignments(this.properties, false);
    if (assigments == null) {
      buffer.append("<p>"
        + this.html(Resources.getString("NoVariableAssigned")) + "</p>");
    }
    else {
      for (VarAssignment va : assigments) {
        if ((va.getVariable() != null) || !StringTools.isEmpty(va.getValue())) {
          buffer.append("<p><tt>");
          if (va.getVariable() != null) {
            buffer.append(this.html(va.getVariable().getName()));
          }
          else {
            buffer.append("???");
          }
          buffer.append(" := ");
          if (va.getValue() != null) {
            buffer.append(this.html(va.getValue()));
          }
          buffer.append("</tt></p>");
        }
      }
    }

    return buffer.toString();
  }


  @Override
  protected void writeAttributes(XMLWriter out, IdMap uid_map) {

    super.writeAttributes(out, uid_map);

    List<VarAssignment> assignments =
      this.getAssignments(this.properties, false);
    if (assignments != null) {
      boolean inited = false;
      int i = 0;
      for (Iterator<VarAssignment> it = assignments.iterator(); it.hasNext(); i++) {
        VarAssignment va = it.next();
        if ((va.getVariable() != null) || !StringTools.isEmpty(va.getValue())) {
          if (!inited) {
            Graph.printAtt(out, "list", SetVariableNode.ASSIGNMENTS, null);
            inited = true;
          }

          Graph.printAtt(out, "list", String.valueOf(i + 1), null);
          Slot v = va.getVariable();
          if (v != null) {
            try {
              String uid = uid_map.variables.getKey(v);
              Graph.printAtt(out, SetVariableNode.VAR_UID, uid);
              // Graph.printAtt(out, VAR_NAME, v.getName()); // this is
              // redundant
            } catch (Exception exn) {
            } // variable deleted
          }
          Graph.printAtt(out, SetVariableNode.VAR_VALUE, va.getValue());
          out.closeElement("att");
        }
      }
      if (inited) {
        out.closeElement("att");
      }
    }

    Graph.printAtt(out, "boolean", SetVariableNode.SIMULTANEOUS, this
      .getBooleanProperty(SetVariableNode.SIMULTANEOUS) ? "1" : "0");
  }


  @Override
  protected void readAttribute(final XMLReader r, String name, String value,
      final IdMap uid_map)
        throws SAXException {

    if (name.equals(SetVariableNode.VAR_VALUE)
      || name.equals(SetVariableNode.VAR_ID)
      || name.equals(SetVariableNode.VAR_UID)) {
      // old file format: only 1 assignment
      List<VarAssignment> assignments =
        this.getAssignments(this.properties, true);
      if (assignments.isEmpty()) {
        assignments.add(new VarAssignment());
      }
      this.readVariable(r, name, value, uid_map, assignments.get(0));
    }
    else if (name.equals(SetVariableNode.SIMULTANEOUS)) {
      this.setProperty(name, value.equals("1") ? Boolean.TRUE : Boolean.FALSE);
    }
    else if (name.equals(SetVariableNode.ASSIGNMENTS)) {
      r.setHandler(new AbstractHandler("att") {

        VarAssignment currentAssignment = null;


        @Override
        protected void start(String name, Attributes atts) {

          if (name.equals("att")) {
            this.currentAssignment = new VarAssignment();
            List<VarAssignment> assignments =
              SetVariableNode.this.getAssignments(
                SetVariableNode.this.properties, true);
            assignments.add(this.currentAssignment);
            r.setHandler(new AbstractHandler("att") {

              @Override
              protected void start(String name, Attributes atts)
                  throws SAXException {

                if (name.equals("att")) {
                  r.setHandler(new AbstractHandler("att")); // zu fruehes
                                                            // Aussteigen aus
                                                            // der Liste
                                                            // verhindern
                  String attname = atts.getValue("name");
                  String attval = atts.getValue("value");
                  SetVariableNode.this.readVariable(r, attname, attval,
                    uid_map, currentAssignment);
                }
              }
            });
          }
        }
      });
    }
    else {
      super.readAttribute(r, name, value, uid_map);
    }
  }


  protected void readVariable(XMLReader r, String name, String value,
      IdMap uid_map,
            VarAssignment assignment)
      throws SAXException {

    if (name.equals(SetVariableNode.VAR_VALUE) && (value != null)) {
      assignment.setValue(value);
    }
    else if (name.equals(SetVariableNode.VAR_NAME) && (value != null)) {
      Slot v = assignment.getVariable();
      if (v == null) {
        // v not yet set by uid: old file format, only local variables by name
        List<Slot> variables = this.getGraph().getVariables();
        for (Slot slot : variables) {
          if (slot.getName().equals(value)) {
            assignment.setVariable(slot);
            return;
          }
        }
        r.raiseException(Resources.format("UnknownVariable", value));
      }
      else {
        // new file format, variable already set by id
        if (!v.getName().equals(value)) {
          r.raiseException(Resources.format("NameIdMatch", value));
        }
      }
    }
    else if (name.equals(SetVariableNode.VAR_ID) && (value != null)) {
      Graph g = this.getGraph();
      int pos;
      for (pos = 0; pos < value.length() ? value.charAt(pos) == '^' : false; pos++) {
        g = g.getSuperGraph();
      }

      try {
        // old file format: variables by index
        int index = Integer.parseInt(value.substring(pos));
        assignment.setVariable(g.getVariables().get(index));
      } catch (Exception exn) {
        r.raiseAttributeValueException(SetVariableNode.VAR_ID);
      }
    }
    else if (name.equals(SetVariableNode.VAR_UID) && (value != null)) {
      try {
        assignment.setVariable(uid_map.variables.get(value));
      } catch (Exception exn) {
        r.raiseException(Resources.format("UnknownVariable", "ID " + value));
      }
    }
  }


  @Override
  public void update(Mapping map) {

    super.update(map);

    List<VarAssignment> assignments =
      this.getAssignments(this.properties, false);
    if (assignments != null) {
      for (VarAssignment va : assignments) {
        Slot v = va.getVariable();
        if (v != null) {
          Slot new_v = map.getVariable(v);
          if (new_v != v) {
            va.setVariable(new_v);
          }
        }
      }
    }
  }


  @Override
  public Node execute(WozInterface comm, InputCenter input, ExecutionLogger logger) {
	logNode(logger);
    List<VarAssignment> assignments =
      this.getAssignments(this.properties, false);
    if (assignments != null) {
      List<Value> values = new Vector<Value>(assignments.size());
      for (VarAssignment va : assignments) {
        if ((va.getVariable() != null) || !StringTools.isEmpty(va.getValue())) {
          Slot v = va.getVariable();
          if (v == null) {
            throw new NodeExecutionException(this,
                          Resources.getString("NoVariableAssigned"), logger);
          }

          Expression e = null;
          try {
            e = this.parseExpression(va.getValue());
          } catch (Exception exn) {
            throw new NodeExecutionException(this,Resources.getString("IllegalExpression") +
                              "'" + va.getValue() + "'", exn, logger);
          }
          Value val = e.evaluate(comm).copy();
          values.add(val);
          if (!this.getBooleanProperty(SetVariableNode.SIMULTANEOUS)) {
            v.setValue(val);
          }
        }
      }

      if (this.getBooleanProperty(SetVariableNode.SIMULTANEOUS)) {
        Iterator<Value> val = values.iterator();
        for (VarAssignment va : assignments) {
          if ((va.getVariable() != null) || !StringTools.isEmpty(va.getValue())) {
            va.getVariable().setValue(val.next());
          }
        }
      }
    }

    Node target = this.getEdge(0).getTarget();
    comm.transition(this, target, 0, null);
    return target;
  }


  @Override
  public void validate(Collection<SearchResult> errors) {

    super.validate(errors);

    List<VarAssignment> assignments =
      this.getAssignments(this.properties, false);
    if (assignments != null) {
      for (VarAssignment va : assignments) {
        if ((va.getVariable() != null) || !StringTools.isEmpty(va.getValue())) {
          Slot v = va.getVariable();
          if (v != null) {
            if (!this.getGraph().getAllVariables(Graph.LOCAL).contains(v)) {
              this.reportError(errors, false, Resources.format(
                              "referencesInaccessibleVariable", v.getName()));
            }
          }
          else {
            this.reportError(errors, false, Resources
              .getString("hasNoVariableAssigned"));
          }

          String value = va.getValue();
          if (value == null) {
            this.reportError(errors, false, Resources
              .getString("missingExpression"));
          }
          else {
            try {
              Type t = this.parseExpression(value).getType();
              if (v != null) {
                Type.unify(v.getType(), t);
              }
            } catch (Exception exn) {
              this.reportError(errors, false,
                                Resources
                                  .getString("containsIllegalExpression")
                                  + ": "
                                        + exn.getLocalizedMessage());
            }
          }
        }
      }
    }
  }


  @Override
  public Map<Slot, Node> getFreeVariables(Map<Slot, Node> h) {

    List<VarAssignment> assignments =
      this.getAssignments(this.properties, false);
    if (assignments != null) {
      for (VarAssignment va : assignments) {
        Slot v = va.getVariable();
        if (v != null) {
          h.put(v, this);
        }
      }
    }
    return h;
  }


  @Override
  protected void writeVoiceXML(XMLWriter w, IdMap uid_map) {

    List<VarAssignment> assignments =
      this.getAssignments(this.properties, false);
    if (assignments != null) {
      for (VarAssignment va : assignments) {
        w.printElement("assign", new String[] { "name", "expr" }, new Object[] {
                        va.getVariable(), Node.vxmlExp(va.getValue()) });
      }
    }
  }

  public static class VarAssignment {

    Slot variable;
    String value;


    public VarAssignment() {

      this(null, null);
    }


    public VarAssignment(Slot variable, String value) {

      this.variable = variable;
      this.value = value;
    }


    public String getValue() {

      return this.value;
    }


    public void setValue(String value) {

      this.value = value;
    }


    public Slot getVariable() {

      return this.variable;
    }


    public void setVariable(Slot variable) {

      this.variable = variable;
    }


    @Override
    public String toString() {

      return this.getVariable() + " = " + this.getValue();
    }
  }
}