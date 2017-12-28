package com.clt.diamant.graph.nodes;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Collection;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.xml.sax.SAXException;

import com.clt.diamant.ExecutionLogger;
import com.clt.diamant.IdMap;
import com.clt.diamant.InputCenter;
import com.clt.diamant.Resources;
import com.clt.diamant.WozInterface;
import com.clt.diamant.graph.Graph;
import com.clt.diamant.graph.Node;
import com.clt.diamant.graph.search.SearchResult;
import com.clt.diamant.gui.NodePropertiesDialog;
import com.clt.script.exp.EvaluationException;
import com.clt.script.exp.Type;
import com.clt.script.exp.Value;
import com.clt.script.exp.values.IntValue;
import com.clt.util.StringTools;
import com.clt.xml.XMLReader;
import com.clt.xml.XMLWriter;

public class SleepNode
    extends Node {

  public static final String SLEEPTIME = "time";


  public SleepNode() {

    super();

    this.addEdge();

    // setTitle(Resources.getString("Wait"));
    this.setProperty(SleepNode.SLEEPTIME, "0");
  }


  public static Color getDefaultColor() {

    return new Color(153, 255, 255);
  }


  @Override
  protected JComponent createEditorComponent(Map<String, Object> properties) {

    JPanel p = new JPanel(new GridBagLayout());
    p.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = gbc.gridy = 0;
    gbc.weightx = 0.0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(3, 3, 3, 3);

    p.add(new JLabel(Resources.getString("TimeInMs") + ':'), gbc);
    gbc.gridx++;
    gbc.weightx = 1.0;
    p.add(
      NodePropertiesDialog.createTextField(properties, SleepNode.SLEEPTIME),
      gbc);

    gbc.gridy++;
    gbc.weighty = 1.0;
    gbc.gridx = 0;
    gbc.gridwidth = 2;
    p.add(new JPanel(), gbc);

    return p;
  }


  @Override
  public Node execute(WozInterface comm, InputCenter input, ExecutionLogger logger) {
	logNode(logger);
    Node target = this.getEdge(0).getTarget();
    try {
      Value v =
        this.parseExpression((String)this.getProperty(SleepNode.SLEEPTIME))
          .evaluate(comm);
      if (!(v instanceof IntValue)) {
        throw new EvaluationException( Resources.getString("SleepTimeNotAnInt"));
      }
      long time = ((IntValue)v).getInt();
      if (time >= 0) {
        Thread.sleep(time);
      }
    } catch (InterruptedException ignore) {
    } catch (Exception exn) {
      throw new NodeExecutionException(this, Resources.getString("CouldNotEvaluateSleepTime"),
        exn, logger);
    }
    comm.transition(this, target, 0, null);
    return target;
  }


  @Override
  public void validate(Collection<SearchResult> errors) {

    super.validate(errors);

    String exp = (String)this.getProperty(SleepNode.SLEEPTIME);
    if (StringTools.isEmpty(exp)) {
      this.reportError(errors, false, Resources.getString("missingExpression"));
    }

    try {
      Type.unify(this.parseExpression(exp).getType(), Type.Int);
    } catch (Exception exn) {
      this.reportError(errors, false, Resources
        .getString("containsIllegalExpression")
        + ": "
                    + exn.getLocalizedMessage());
    }
  }


  @Override
  protected void writeAttributes(XMLWriter out, IdMap uid_map) {

    super.writeAttributes(out, uid_map);

    String sleepTime = (String)this.getProperty(SleepNode.SLEEPTIME);
    if (!StringTools.isEmpty(sleepTime)) {
      Graph.printAtt(out, SleepNode.SLEEPTIME, sleepTime);
    }
  }


  @Override
  protected void readAttribute(XMLReader r, String name, String value,
      IdMap uid_map)
        throws SAXException {

    if (name.equals(SleepNode.SLEEPTIME)) {
      this.setProperty(name, value);
    }
    else {
      super.readAttribute(r, name, value, uid_map);
    }
  }


  @Override
  protected void writeVoiceXML(XMLWriter w, IdMap uid_map) {

    // vxml
  }

}