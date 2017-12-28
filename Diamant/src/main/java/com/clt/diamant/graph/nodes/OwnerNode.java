package com.clt.diamant.graph.nodes;

import java.awt.Color;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.clt.dialogos.plugin.Plugin;
import com.clt.dialogos.plugin.PluginSettings;
import com.clt.diamant.Device;
import com.clt.diamant.Grammar;
import com.clt.diamant.IdMap;
import com.clt.diamant.Mapping;
import com.clt.diamant.Slot;
import com.clt.diamant.graph.EdgeEvent;
import com.clt.diamant.graph.EdgeListener;
import com.clt.diamant.graph.Graph;
import com.clt.diamant.graph.GraphListener;
import com.clt.diamant.graph.GraphOwner;
import com.clt.diamant.graph.Node;
import com.clt.diamant.graph.VisualGraphElement;
import com.clt.diamant.graph.search.NodeSearchFilter;
import com.clt.diamant.graph.search.SearchResult;
import com.clt.diamant.gui.GraphEditorFactory;
import com.clt.script.Environment;
import com.clt.xml.AbstractHandler;
import com.clt.xml.XMLReader;
import com.clt.xml.XMLWriter;

public abstract class OwnerNode
    extends Node
    implements GraphOwner {

  private Graph ownedGraph;
  private List<EndNode> endNodes;

  private Collection<EdgeListener> edgeListeners;

  private GraphListener graphListener = new GraphListener() {

    public void elementAdded(Graph g, VisualGraphElement n) {

      if (!OwnerNode.this.isReading() && (n instanceof EndNode)) {
        OwnerNode.this.determineEndNodes();
      }
    }


    public void elementRemoved(Graph g, VisualGraphElement n) {

      if (!OwnerNode.this.isReading() && (n instanceof EndNode)) {
        OwnerNode.this.determineEndNodes();
      }
    }


    public void sizeChanged(Graph g, int width, int height) {

    }


    public void graphRenamed(Graph g, String name) {

    }
  };


  public OwnerNode(Graph ownedGraph) {

    super();

    this.endNodes = new ArrayList<EndNode>();
    this.edgeListeners = new ArrayList<EdgeListener>();

    this.setOwnedGraph(ownedGraph);
  }


  @Override
  public Node clone(Class<? extends Node> targetClass, Mapping map) {

    OwnerNode n = (OwnerNode)super.clone(targetClass, map);

    Graph g = this.getOwnedGraph().clone(map);

    // build endNodes list without triggering edge listeners (as opposed to
    // determineEndNodes())
    n.endNodes = new ArrayList<EndNode>();
    for (int i = 0; i < this.endNodes.size(); i++) {
      EndNode end = (EndNode)map.getNode(this.getEndNode(i));
      n.endNodes.add(end);
    }

    n.setOwnedGraph(g);
    return n;
  }


  @Override
  public void update(Mapping map) {

    super.update(map);
    this.ownedGraph.update(map);

    for (int i = 0; i < this.endNodes.size(); i++) {
      this.endNodes.set(i, (EndNode)map.getNode(this.endNodes.get(i)));
    }
  }


  @Override
  public void updateEdges() {

    super.updateEdges();
    this.ownedGraph.updateEdges();
  }


  @Override
  public Collection<? extends SearchResult> find(NodeSearchFilter filter) {

    Collection<SearchResult> matches = new ArrayList<SearchResult>();
    matches.addAll(super.find(filter));
    matches.addAll(this.ownedGraph.find(filter));
    return matches;
  }


  private void setOwnedGraph(Graph g) {

    if (this.ownedGraph != null) {
      this.ownedGraph.removeGraphListener(this.graphListener);
      this.ownedGraph.setOwner(null);
    }
    this.ownedGraph = g;
    if (this.ownedGraph != null) {
      this.ownedGraph.setOwner(this);
      this.ownedGraph.addGraphListener(this.graphListener);
    }
    this.determineEndNodes();
  }


  public void addEdgeListener(EdgeListener l) {

    this.edgeListeners.add(l);
  }


  public void removeEdgeListener(EdgeListener l) {

    this.edgeListeners.remove(l);
  }


  private void fireEdgeEvent(int index, int id) {

    EdgeEvent e = new EdgeEvent(this, index, id);
    for (EdgeListener l : this.edgeListeners) {
      switch (id) {
        case EdgeEvent.EDGE_ADDED:
          l.edgeAdded(e);
          break;
        case EdgeEvent.EDGE_REMOVED:
          l.edgeRemoved(e);
          break;
        case EdgeEvent.EDGE_UPDATED:
          l.edgeUpdated(e);
          break;
      }
    }
  }

  private final PropertyChangeListener endNodePropertyListener =
    new PropertyChangeListener() {

      public void propertyChange(PropertyChangeEvent evt) {

        int index = OwnerNode.this.getEndNodeIndex((Node)evt.getSource());
        if (index >= 0) {
          OwnerNode.this.fireEdgeEvent(index, EdgeEvent.EDGE_UPDATED);
        }
      }
    };


  protected void determineEndNodes() {

    if (this.ownedGraph == null) {
      this.removeAllEdges();
      for (Iterator<EndNode> it = this.endNodes.iterator(); it.hasNext();) {
        it.next().removePropertyChangeListener(this.endNodePropertyListener);
      }
      this.endNodes.clear();
    }
    else {
      // we must be careful to keep existing edges. Otherwise the user
      // would have to reassign their targets every time.

      // Step 1: Collect End nodes
      List<EndNode> ends = new ArrayList<EndNode>();
      for (Node n : this.ownedGraph.getNodes()) {
        if (n instanceof EndNode) {
          ends.add((EndNode)n);
        }
      }

      // Step 2: remove no longer existing edges and
      // update existing edge names and colors
      for (int i = this.endNodes.size() - 1; i >= 0; i--) {
        EndNode n = this.endNodes.get(i);
        if (!ends.contains(n)) {
          this.endNodes.remove(i);
          n.removePropertyChangeListener(this.endNodePropertyListener);
          this.fireEdgeEvent(i, EdgeEvent.EDGE_REMOVED);
        }
        else {
          this.fireEdgeEvent(i, EdgeEvent.EDGE_UPDATED);
        }
      }

      // Step 3: add new edges
      for (int i = 0; i < ends.size(); i++) {
        if (!this.endNodes.contains(ends.get(i))) {
          EndNode n = ends.get(i);
          this.endNodes.add(n);
          n.addPropertyChangeListener(this.endNodePropertyListener);
          this.fireEdgeEvent(this.endNodes.size() - 1, EdgeEvent.EDGE_ADDED);
        }
      }
    }
  }


  public List<? extends EndNode> getEndNodes() {

    return Collections.unmodifiableList(this.endNodes);
  }


  public EndNode getEndNode(int index) {

    return this.endNodes.get(index);
  }


  public int getEndNodeIndex(Node n) {

    return this.endNodes.indexOf(n);
  }


  public Environment getEnvironment(boolean local) {

    return this.getGraph().getEnvironment(local);
  }


  public Collection<Device> getDevices() {

    if (this.getSuperGraph() != null) {
      return this.getSuperGraph().getOwner().getDevices();
    }
    else {
      throw new IllegalStateException();
    }
  }


  @Override
  public PluginSettings getPluginSettings(Class<? extends Plugin> pluginClass) {

    if (this.getSuperGraph() != null) {
      return this.getSuperGraph().getOwner().getPluginSettings(pluginClass);
    }
    else {
      throw new IllegalStateException();
    }
  }


  public List<Grammar> getGrammars() {

    List<Grammar> grammars = new ArrayList<Grammar>();
    if (this.getSuperGraph() != null) {
      grammars.addAll(this.getSuperGraph().getOwner().getGrammars());
    }
    grammars.addAll(this.getOwnedGraph().getGrammars());
    return grammars;
  }


  public void setDirty(boolean dirty) {

    if (this.getSuperGraph() != null) {
      if (this.getSuperGraph().getOwner() != null) {
        this.getSuperGraph().getOwner().setDirty(dirty);
      }
    }
  }


  // setGraph() sets a pointer to the graph that this node belongs to.
  // If it is null, the node is about to be deleted.
  // For setting the graph owned by this node, see setOwnedGraph()
  @Override
  public void setGraph(Graph g) {

    if (g == null) {
      GraphEditorFactory.ownerDeleting(this);
    }

    super.setGraph(g);
  }


  public String getGraphName() {

    return this.getTitle();
  }


  public void setGraphName(String name) {

    this.setTitle(name);
  }


  @Override
  public void setTitle(String title) {

    super.setTitle(title);
    if (this.getOwnedGraph() != null) {
      this.getOwnedGraph().nameChanged();
    }
  }


  public Graph getSuperGraph() {

    return this.getGraph();
  }


  public Graph getOwnedGraph() {

    return this.ownedGraph;
  }


  public void export(Graph g, File f)
      throws IOException {

    if (this.getSuperGraph() != null) {
      this.getSuperGraph().getOwner().export(g, f);
    }
    else {
      throw new IllegalStateException();
    }
  }


  @Override
  public Map<ProcNode, Node> getFreeProcedures(Map<ProcNode, Node> h) {

    h = super.getFreeProcedures(h);
    return this.getOwnedGraph().getFreeProcedures(h);
  }


  @Override
  public Map<Slot, Node> getFreeVariables(Map<Slot, Node> h) {

    h = super.getFreeVariables(h);
    return this.getOwnedGraph().getFreeVariables(h);
  }


  @Override
  public Map<Device, Node> getFreeDevices(Map<Device, Node> h) {

    h = super.getFreeDevices(h);
    return this.getOwnedGraph().getFreeDevices(h);
  }


  @Override
  public Map<Grammar, Node> getFreeGrammars(Map<Grammar, Node> h) {

    h = super.getFreeGrammars(h);
    return this.getOwnedGraph().getFreeGrammars(h);
  }


  @Override
  public boolean editProperties(Component parent) {

    GraphEditorFactory.show(this);
    return true;
  }


  @Override
  protected JComponent createEditorComponent(Map<String, Object> properties) {

    return null;
  }


  @Override
  protected void writeAttributes(XMLWriter out, IdMap uid_map) {

    super.writeAttributes(out, uid_map);

    Graph.printAtt(out, "object", "graph", null);
    this.ownedGraph.save(out, uid_map);
    out.closeElement("att");

    Graph.printAtt(out, "list", "endpoints", null);
    for (int i = 0; i < this.endNodes.size(); i++) {
      Graph.printAtt(out, String.valueOf(i), uid_map.nodes.getKey(this.endNodes
        .get(i)));
    }

    out.closeElement("att");
  }


  @Override
  protected void readAttribute(final XMLReader r, String name, String value,
      final IdMap uid_map)
        throws SAXException {

    if (name.equals("graph")) {
      this.ownedGraph.read(r, new Runnable() {

        // Hier kommt ein Hack:
        // fuer alte Dateien wird endNodes neu generiert. Da das im
        // Zusammenhang mit geloeschten Knoten und Undo zu Fehlern fuehrt,
        // ueberschreiben neue Dateien diese Werte durch die "endpoints"
        // Liste (s.u.)
        public void run() {

          for (Node n : OwnerNode.this.ownedGraph.getNodes()) {
            if (n instanceof EndNode) {
              OwnerNode.this.endNodes.add((EndNode)n);
              n
                .addPropertyChangeListener(OwnerNode.this.endNodePropertyListener);
            }
          }
        }
      }, uid_map);
    }
    else if (name.equals("endpoints")) {
      // Zuordnung [EndNodes des Graphen] -> [Edges des GraphNode]
      // Die Indices sind verschieden, wenn ein EndNode geloescht und per
      // Undo wieder eingefuegt wurde.
      r.setHandler(new AbstractHandler("att") {

        @Override
        public void start(String name, Attributes atts)
            throws SAXException {

          if (name.equals("att")) {
            // zu fruehes Aussteigen aus der Liste verhindern
            r.setHandler(new AbstractHandler("att"));

            try {
              int index = Integer.parseInt(atts.getValue("name"));
              // ensure size
              while (OwnerNode.this.endNodes.size() <= index) {
                OwnerNode.this.endNodes.add(null);
              }
              String node_id = atts.getValue("value");
              Node endPoint;
              try {
                // code for backwards compatibility
                int node = Integer.parseInt(atts.getValue("value"));
                Collection<Node> nodes = OwnerNode.this.ownedGraph.getNodes();
                if (!(nodes instanceof List)) {
                  nodes = new ArrayList<Node>(nodes);
                }
                endPoint = ((List<Node>)nodes).get(node);
              }
                            catch (NumberFormatException exn) {
                              endPoint = uid_map.nodes.get(node_id);
                            }
                            if (!(endPoint instanceof EndNode)) {
                              throw new SAXException("Node " + endPoint.getId()
                                + " (endpoint "
                                      + index + " of graph node '"
                                      + OwnerNode.this.nodePath(false)
                                + "') is not an end node.");
                            }
                            else {
                              if (OwnerNode.this.endNodes.get(index) != null) {
                                OwnerNode.this.endNodes.get(index)
                                  .removePropertyChangeListener(
                                      OwnerNode.this.endNodePropertyListener);
                              }
                              endPoint
                                .addPropertyChangeListener(OwnerNode.this.endNodePropertyListener);
                              OwnerNode.this.endNodes.set(index,
                                (EndNode)endPoint);
                            }
                          }
                        catch (NumberFormatException exn) {
                          r.raiseAttributeValueException("name");
                        }
                      }
                    }


        @Override
        protected void end(String name) {

        }
      });
    }
    else {
      super.readAttribute(r, name, value, uid_map);
    }
  }


  @Override
  public void validate(Collection<SearchResult> errors) {

    super.validate(errors);
    this.ownedGraph.validate(errors, null);
  }


  @Override
  public Color getPortColor(int portNumber) {

    if ((portNumber >= 0) && (portNumber < this.endNodes.size())) {
      return (Color)this.endNodes.get(portNumber).getProperty("color");
    }
    else {
      return super.getPortColor(portNumber);
    }
  }


  public String getPortName(int portNumber) {

    if ((portNumber >= 0) && (portNumber < this.endNodes.size())) {
      return this.endNodes.get(portNumber).getTitle();
    }
    else {
      return "";
    }
  }

}