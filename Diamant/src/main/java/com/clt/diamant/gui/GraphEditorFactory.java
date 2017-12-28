package com.clt.diamant.gui;

import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;

import com.clt.diamant.graph.GraphOwner;
import com.clt.diamant.graph.ui.GraphUI;

public class GraphEditorFactory {

  private static Map<GraphOwner, GraphEditor> graphEditors =
    new WeakHashMap<GraphOwner, GraphEditor>();


  public static GraphEditor get(GraphOwner owner) {

    return GraphEditorFactory.graphEditors.get(owner);
  }


  public static GraphEditor show(GraphOwner owner) {

    GraphEditor d = GraphEditorFactory.get(owner);
    if (d == null) {
      d = new GraphDialog(owner, new GraphUI(owner.getOwnedGraph()), false);
      GraphEditorFactory.register(owner, d);
    }
    d.showEditor();
    return d;
  }


  public static void ownerDeleting(GraphOwner owner) {

    for (GraphEditor d : new ArrayList<GraphEditor>(
      GraphEditorFactory.graphEditors.values())) {
      if (d.getGraphOwner().getSuperGraph() != null) {
        if (d.getGraphOwner().getSuperGraph().getOwner() == owner) {
          GraphEditorFactory.ownerDeleting(d.getGraphOwner());
        }
      }
    }
    GraphEditor e = GraphEditorFactory.get(owner);
    if (e != null) {
      e.closeEditor();
    }
  }


  public static void register(GraphOwner owner, GraphEditor editor) {

    GraphEditorFactory.graphEditors.put(owner, editor);
  }


  public static void unregister(GraphOwner owner) {

    GraphEditorFactory.graphEditors.remove(owner);
  }


  public static boolean isShowing(GraphOwner owner) {

    GraphEditor editor = GraphEditorFactory.graphEditors.get(owner);
    return (editor != null) && editor.isShowing();
  }


  public static int size() {

    return GraphEditorFactory.graphEditors.size();
  }
}