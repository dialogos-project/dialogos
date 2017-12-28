package com.clt.diamant.graph.nodes;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.xml.sax.SAXException;

import com.clt.diamant.ExecutionLogger;
import com.clt.diamant.IdMap;
import com.clt.diamant.InputCenter;
import com.clt.diamant.Resources;
import com.clt.diamant.WozInterface;
import com.clt.diamant.graph.Edge;
import com.clt.diamant.graph.Graph;
import com.clt.diamant.graph.Node;
import com.clt.diamant.graph.search.SearchResult;
import com.clt.diamant.gui.NodePropertiesDialog;
import com.clt.script.exp.Expression;
import com.clt.script.exp.Type;
import com.clt.script.exp.expressions.Assignment;
import com.clt.script.exp.values.BoolValue;
import com.clt.util.StringTools;
import com.clt.xml.XMLReader;
import com.clt.xml.XMLWriter;

public class ConditionalNode
    extends Node {

  public static final String EXPRESSION = "expression";


  public ConditionalNode() {

    super();

    this.addEdge("true");
    this.addEdge("false");

    this.addPropertyChangeListener(new PropertyChangeListener() {

      public void propertyChange(PropertyChangeEvent evt) {

        if (evt.getPropertyName().equals(ConditionalNode.EXPRESSION)) {
          String cond = (String)evt.getNewValue();
          if (ConditionalNode.this.numEdges() > 0) {
            if ((cond != null) && (cond.trim().length() > 0)) {
              ConditionalNode.this.getEdge(0).setCondition(cond);
              ConditionalNode.this.getEdge(1).setCondition("else");
            }
            else {
              ConditionalNode.this.getEdge(0).setCondition("true");
              ConditionalNode.this.getEdge(1).setCondition("false");
            }
          }
        }
      }
    });
  }


  @Override
  public Edge addEdge(Edge edge) {

    String condition = (String)this.getProperty(ConditionalNode.EXPRESSION);
    if (condition != null) {
      if (this.numEdges() == 0) {
        edge.setCondition(condition);
      }
      else {
        edge.setCondition("else");
      }
    }
    return super.addEdge(edge);
  }


  @Override
  public Color getPortColor(int portNumber) {

    if (portNumber < this.numEdges() - 1) {
      return new Color(153, 255, 153);
    }
    else {
      // false = red
      return new Color(255, 153, 153);
    }
  }


  @Override
  public JComponent createEditorComponent(Map<String, Object> properties) {

    JPanel p = new JPanel(new GridBagLayout());

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = gbc.gridy = 0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.NORTH;
    gbc.insets = new Insets(6, 6, 6, 6);
    gbc.weightx = 0.0;
    gbc.weighty = 1.0;
    p.add(new JLabel(Resources.getString("Condition") + ':'), gbc);

    gbc.gridx++;
    gbc.weightx = 1.0;
    gbc.weighty = 0.0;
    p.add(NodePropertiesDialog.createTextField(properties,
      ConditionalNode.EXPRESSION), gbc);

    return p;
  }


  @Override
  public String getDescription(Edge selectedEdge) {

    StringBuffer buffer = new StringBuffer(super.getDescription(selectedEdge));

    buffer.append("<p><b>" + this.html(Resources.getString("Condition"))
      + " :</b> <tt>"
                + this.getProperty(ConditionalNode.EXPRESSION) + "</tt><p>");

    return buffer.toString();
  }


  @Override
  protected void writeAttributes(XMLWriter out, IdMap uid_map) {

    super.writeAttributes(out, uid_map);

    Graph.printAtt(out, ConditionalNode.EXPRESSION, (String)this
      .getProperty(ConditionalNode.EXPRESSION));
  }


  @Override
  protected void readAttribute(XMLReader r, String name, String value,
      IdMap uid_map)
        throws SAXException {

    if (name.equals(ConditionalNode.EXPRESSION)) {
      this.setProperty(name, value);
    }
    else {
      super.readAttribute(r, name, value, uid_map);
    }
  }


  @Override
  public Node execute(WozInterface comm, InputCenter input, ExecutionLogger logger) {
	logNode(logger);
    Expression e = null;
    BoolValue result;
    try {
      e =
        this.parseExpression((String)this
          .getProperty(ConditionalNode.EXPRESSION));
      result = (BoolValue)e.evaluate(comm);
    } catch (Exception exn) {
      throw new NodeExecutionException(this, Resources.getString("ConditionNotEvaluated"),
        exn, logger);
    }

    Node target;
    if (result.getBool()) {
      target = this.getEdge(0).getTarget();
    }
    else {
      target = this.getEdge(1).getTarget();
    }

    comm.transition(this, target, result.getBool() ? 0 : 1, result.toString());
    return target;
  }


  @Override
  public void validate(Collection<SearchResult> errors) {

    super.validate(errors);
    String exp = (String)this.getProperty(ConditionalNode.EXPRESSION);
    if (StringTools.isEmpty(exp)) {
      this.reportError(errors, false, Resources.getString("missingExpression"));
    }
    else {
      try {
        Expression e = this.parseExpression(exp);
        Type.unify(e.getType(), Type.Bool);

        if (e instanceof Assignment) {
          this.reportError(errors, false, Resources
            .getString("containsAssignmentAsCondition")
                          + ": " + exp);
        }
      } catch (Exception exn) {
        this.reportError(errors, false, Resources
          .getString("containsIllegalExpression")
          + ": "
                        + exn.getLocalizedMessage());
      }
    }
  }


  @Override
  protected void writeVoiceXML(XMLWriter w, IdMap uid_map) {

    w.openElement("if", new String[] { "cond" },
            new String[] { Node.vxmlExp((String)this
              .getProperty(ConditionalNode.EXPRESSION)) });

    this.writeVoiceXMLGoto(w, uid_map, 0);

    w.printElement("else", null);

    this.writeVoiceXMLGoto(w, uid_map, 1);

    w.closeElement("if");
  }
}