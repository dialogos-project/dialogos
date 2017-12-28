/*
 * @(#)TimeoutEdge.java
 * Created on 06.03.2007 by dabo
 *
 * Copyright (c) CLT Sprachtechnologie GmbH.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CLT Sprachtechnologie GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CLT Sprachtechnologie GmbH.
 */

package com.clt.diamant.graph.nodes;

import com.clt.diamant.IdMap;
import com.clt.diamant.Resources;
import com.clt.diamant.graph.Graph;
import com.clt.diamant.graph.Node;
import com.clt.diamant.graph.SpecialEdge;
import com.clt.xml.XMLWriter;

public class TimeoutEdge
    extends SpecialEdge {

  // do not modifiy!
  public static final String TYPE = "timeout";


  public TimeoutEdge(Node source) {

    super(source);
  }


  public TimeoutEdge(Node source, Node target) {

    super(source, target);
  }


  @Override
  public TimeoutEdge clone(Node newSource) {

    return new TimeoutEdge(newSource, this.getTarget());
  }


  @Override
  public String getType() {

    return TimeoutEdge.TYPE;
  }


  @Override
  public String getCondition() {

    return Resources.getString("TimeoutAfter") + " "
      + this.getSource().getProperty(InputNode.TIMEOUT)
                + "ms";
  }


  @Override
  public void setCondition(String s) {

  }


  @Override
  protected void writeAttributes(XMLWriter out, IdMap uid_map) {

    super.writeAttributes(out, uid_map);

    Graph.printAtt(out, InputNode.TIMEOUT, (String)this.getSource()
      .getProperty(InputNode.TIMEOUT));
  }
}