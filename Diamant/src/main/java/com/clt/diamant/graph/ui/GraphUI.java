package com.clt.diamant.graph.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;
import javax.swing.text.JTextComponent;
import javax.swing.undo.UndoableEdit;

import com.clt.diamant.Grammar;
import com.clt.diamant.Mapping;
import com.clt.diamant.Preferences;
import com.clt.diamant.Resources;
import com.clt.diamant.Version;
import com.clt.diamant.graph.ColorizableElement;
import com.clt.diamant.graph.Comment;
import com.clt.diamant.graph.Edge;
import com.clt.diamant.graph.Functions;
import com.clt.diamant.graph.Graph;
import com.clt.diamant.graph.GraphElement;
import com.clt.diamant.graph.GraphListener;
import com.clt.diamant.graph.GraphSelectionModel;
import com.clt.diamant.graph.Group;
import com.clt.diamant.graph.GroupElement;
import com.clt.diamant.graph.MoveableElement;
import com.clt.diamant.graph.Node;
import com.clt.diamant.graph.Procedure;
import com.clt.diamant.graph.VisualGraphElement;
import com.clt.diamant.graph.nodes.CallNode;
import com.clt.diamant.graph.nodes.EndNode;
import com.clt.diamant.graph.nodes.GotoNode;
import com.clt.diamant.graph.nodes.GraphNode;
import com.clt.diamant.graph.nodes.InputNode;
import com.clt.diamant.graph.nodes.LabelNode;
import com.clt.diamant.graph.nodes.OwnerNode;
import com.clt.diamant.graph.nodes.ProcNode;
import com.clt.diamant.graph.nodes.StartNode;
import com.clt.diamant.graph.search.NodeSearchDialog;
import com.clt.diamant.gui.AlignmentDialog;
import com.clt.diamant.gui.CanvasSizeDialog;
import com.clt.diamant.gui.DefaultToolbox;
import com.clt.diamant.gui.FunctionsEditor;
import com.clt.diamant.gui.GrammarsEditor;
import com.clt.diamant.gui.GraphEditor;
import com.clt.diamant.gui.GraphEditorFactory;
import com.clt.diamant.gui.GroovyScriptEditorDialog;
import com.clt.diamant.gui.GroovyVariableDialog;
import com.clt.diamant.gui.InputHandlerDialog;
import com.clt.diamant.gui.SingleDocumentWindow;
import com.clt.diamant.gui.Toolbox;
import com.clt.diamant.gui.VariableDialog;
import com.clt.diamant.undo.CanvasEdit;
import com.clt.diamant.undo.EdgeEdit;
import com.clt.diamant.undo.GroupEdit;
import com.clt.diamant.undo.MoveEdit;
import com.clt.diamant.undo.NodeEdit;
import com.clt.gui.Commands;
import com.clt.gui.GUI;
import com.clt.gui.OptionPane;
import com.clt.gui.ParentMouseInputListener;
import com.clt.gui.Passpartout;
import com.clt.gui.menus.CmdMenu;
import com.clt.gui.menus.CmdPopupMenu;
import com.clt.gui.menus.MenuCommander;
import com.clt.gui.menus.MenuOwner;
import com.clt.undo.AbstractEdit;
import com.clt.undo.Undo;
import com.clt.util.MetaCollection;
import com.clt.util.StringTools;
import javax.swing.UIManager;

/**
 * GraphUI is a JPanel which displays the nodes and the edges of a DialogOS
 * automaton. It supports undo and redo functionality.
 */
public class GraphUI extends JPanel implements MenuCommander, Commands, Printable, PropertyChangeListener, ClipboardOwner, GraphListener {

    private static final Clipboard clipboard = new Clipboard("Graph");

    // private static final Clipboard clipboard =
    // Toolkit.getDefaultToolkit().getSystemClipboard();
    public static Clipboard getClipboard() {
        return GraphUI.clipboard;
    }

    private Collection<NodeUI<?>> nodes;
    private Graph model;
    private MouseInputListener viewScroller;
    private JScrollPane graphScrollPane;
    private GraphSelectionModel selectionModel;

    private Map<VisualGraphElement, UIElement> ui;

    transient private Point dragStart, dragEnd;
    transient private Edge draggedEdge;

    private Undo undo = new Undo() {
        @Override
        public synchronized boolean addEdit(UndoableEdit edit) {

            boolean success = super.addEdit(edit);
            GraphUI.this.getGraph().setDirty(true);
            return success;
        }
    };

    private static final boolean marchingAnts = false;
    private static final boolean paintEdgesOnTop = false;

    private final int marchingAntsLength = 4;
    private int marchingAntsOffset = 0;
    private Ants ants = null;

    private int currentTool = DefaultToolbox.ANCHOR;

    public static final int cmdEditVariables = 501;
    public static final int cmdEditFunctions = 502;
    public static final int cmdEditGroovyFunctions = 503;
    public static final int cmdEditGroovyVariables = 504;
    public static final int cmdEditParameters = 551;
    public static final int cmdEditReturnVariables = 552;
    public static final int cmdEditGrammars = 553;
    public static final int cmdEditHandlers = 554;

    public static final int cmdAlign = 600;
    public static final int cmdGroup = 601;
    public static final int cmdUngroup = 602;
    public static final int cmdCanvasSize = 603;
    public static final int cmdFind = 605;
    public static final int cmdBreakpoint = 606;

    public static final int cmdColorMenu = 610;
    public static final int cmdColorOther = 611;
    public static final int cmdCollateNodes = 612;

    public static final int cmdUp = 620;
    public static final int cmdDown = 621;
    public static final int cmdLeft = 622;
    public static final int cmdRight = 623;

    private static BasicStroke lineStroke;
    private static Object lineAntiAliasingSettings;

    public static Map<String, Color> gNodeColors;

    static {
        if (com.clt.util.Platform.isMac() || (com.clt.util.Platform.isWindows() && Version.HICOLOR)) {
            GraphUI.lineStroke = new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            GraphUI.lineAntiAliasingSettings = RenderingHints.VALUE_ANTIALIAS_ON;
        } else {
            GraphUI.lineStroke = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            GraphUI.lineAntiAliasingSettings = RenderingHints.VALUE_ANTIALIAS_OFF;
        }

        GraphUI.gNodeColors = new LinkedHashMap<String, Color>();

        GraphUI.gNodeColors.put("Red", new Color(255, 130, 130));
        GraphUI.gNodeColors.put("DarkRed", new Color(255, 90, 90));
        GraphUI.gNodeColors.put("DeepRed", new Color(204, 0, 0));
        GraphUI.gNodeColors.put("Green", new Color(153, 255, 153));
        GraphUI.gNodeColors.put("DarkGreen", new Color(0, 204, 90));
        GraphUI.gNodeColors.put("DeepGreen", new Color(0, 204, 153));
        GraphUI.gNodeColors.put("Blue", new Color(153, 153, 255));
        GraphUI.gNodeColors.put("DarkBlue", new Color(0, 153, 204));
        GraphUI.gNodeColors.put("DeepBlue", new Color(0, 45, 204));
        GraphUI.gNodeColors.put("Purple", new Color(255, 45, 255));
        GraphUI.gNodeColors.put("DarkPurple", new Color(153, 45, 204));
        GraphUI.gNodeColors.put("DeepPurple", new Color(90, 0, 153));
        GraphUI.gNodeColors.put("Yellow", new Color(255, 255, 153));
        GraphUI.gNodeColors.put("DarkYellow", new Color(255, 200, 0));
        GraphUI.gNodeColors.put("DeepYellow", new Color(204, 153, 0));
        GraphUI.gNodeColors.put("Gray", new Color(192, 192, 192));
        GraphUI.gNodeColors.put("DarkGray", new Color(128, 128, 128));
        GraphUI.gNodeColors.put("DeepGray", new Color(76, 76, 128));
    }

    // AKAKAK (debugging of #51)
    public void printScrollbarSizes() {
        System.err.println("vert scrollbar prefsize " + graphScrollPane.getVerticalScrollBar().getPreferredSize());
        System.err.println("vert scrollbar size " + graphScrollPane.getVerticalScrollBar().getSize());
        System.err.println("horiz scrollbar prefsize " + graphScrollPane.getHorizontalScrollBar().getPreferredSize());
        System.err.println("horiz scrollbar size " + graphScrollPane.getHorizontalScrollBar().getSize());
        System.err.println("uiman scrollbar " + ((Integer) UIManager.get("ScrollBar.width")));
    }

    public GraphUI(Graph g) {
        this.setBackground(Color.white);
        // RepaintManager.currentManager(this).setDoubleBufferingEnabled(true);
        this.setDoubleBuffered(true);

        this.model = g;

        this.nodes = new LinkedList<NodeUI<?>>();
        this.selectionModel = new GraphSelectionModel();

        this.dragStart = null;
        this.dragEnd = null;
        this.draggedEdge = null;

        this.setupMouseHandling();

        super.setSize(this.model.getWidth(), this.model.getHeight());

        this.graphScrollPane = GUI.createScrollPane(new Passpartout(this),
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        this.graphScrollPane.setBorder(BorderFactory.createEmptyBorder());

        this.graphScrollPane.getHorizontalScrollBar().setUnitIncrement(10);
        this.graphScrollPane.getVerticalScrollBar().setUnitIncrement(10);
        
//        printScrollbarSizes();

        GUI.removeKeyBinding(this.graphScrollPane, KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0));
        GUI.removeKeyBinding(this.graphScrollPane, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0));
        GUI.removeKeyBinding(this.graphScrollPane, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0));
        GUI.removeKeyBinding(this.graphScrollPane, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0));

        GUI.setKeyBinding(this.graphScrollPane, KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                GraphUI.this.doCommand(GraphUI.cmdUp);
            }
        }, new Class[]{JTextComponent.class}, false);

        GUI.setKeyBinding(this.graphScrollPane, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                GraphUI.this.doCommand(GraphUI.cmdDown);
            }
        }, new Class[]{JTextComponent.class}, false);

        GUI.setKeyBinding(this.graphScrollPane, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                GraphUI.this.doCommand(GraphUI.cmdLeft);
            }
        }, new Class[]{JTextComponent.class}, false);

        GUI.setKeyBinding(this.graphScrollPane, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                GraphUI.this.doCommand(GraphUI.cmdRight);
            }
        }, new Class[]{JTextComponent.class}, false);

        this.initDragAndDrop();
    }

    private void initDragAndDrop() {
        DropTargetListener dropTargetListener = new DropTargetListener() {

            // Die Maus betritt die Komponente mit einem Objekt
            public void dragEnter(DropTargetDragEvent e) {
            }

            // Die Komponente wird verlassen
            public void dragExit(DropTargetEvent e) {
            }

            // Die Maus bewegt sich über die Komponente
            public void dragOver(DropTargetDragEvent e) {
            }

            public void drop(DropTargetDropEvent e) {
                try {
                    Transferable tr = e.getTransferable();
                    DataFlavor[] flavors = tr.getTransferDataFlavors();
                    for (int i = 0; i < flavors.length; i++) {
                        if (flavors[i] == NodeTransferable.flavor) {
                            e.acceptDrop(e.getDropAction());

                            Node node = (Node) tr.getTransferData(flavors[i]);
                            if (!(node instanceof EndNode)
                                    || GraphUI.this.getGraph().supportsEndNode(node.getClass())) {
                                node.setLocation(e.getLocation().x, e.getLocation().y);
                                GraphUI.this.getGraph().add(node);
                            } else {
                                OptionPane.message(GraphUI.this, Resources.format(
                                        "NodeTypeNotSupportedInGraph", Node
                                                .getLocalizedNodeTypeName(node)));
                            }
                            e.dropComplete(true);
                            return;
                        }
                    }
                } catch (Exception exn) {
                    OptionPane.error(GraphUI.this, exn);
                }
                // Ein Problem ist aufgetreten
                e.rejectDrop();
            }

            // Jemand hat die Art des Drops (Move, Copy, Link)
            // geändert
            public void dropActionChanged(DropTargetDragEvent e) {
            }
        };
        new DropTarget(this, dropTargetListener);
    }

    public JComponent getPropertyEditor() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setUI(new com.clt.gui.plaf.AdobeTabbedPaneUI());

        // ***
        Graph graph = this.getGraph();

        tabs.addTab(Resources.getString("Variables"), new VariableDialog(
                graph.getVariables(), graph.getGroovyVariables(),
                null));
        tabs.addTab(Resources.getString("Functions"),
                new FunctionsEditor(graph.getFunctions()));
        tabs.addTab("Groovy " + Resources.getString("Variables"),
                new GroovyVariableDialog(graph.getGroovyVariables(), null, this.getGraph().getVariables()));
        tabs.addTab(Resources.getString("Groovy-Functions"),
                new GroovyScriptEditorDialog(this, Resources.getString("EditScript"),
                        graph.getGroovyScript()));
        tabs.addTab(Resources.getString("Grammars"), new GrammarsEditor(graph.getGrammars()));
        tabs.addTab(Resources.getString("InputHandlers"), new InputHandlerDialog(graph, null));

        return tabs;
    }

    public GraphSelectionModel getSelectionModel() {
        return this.selectionModel;
    }

    public void selectAndShowNode(Node n) {
        this.getSelectionModel().clear();
        this.selectElement(n, true);
        this.showNode(n);
    }

    public void showNode(Node n) {
        GUI.scrollIntoCenter(this.getNodeUI(n), this.graphScrollPane.getViewport());
    }

    private void setupMouseHandling() {
        MouseInputListener m = new MouseInputAdapter() {

            // private Point handStart = null;
            private double distance(long x1, long y1, long x2, long y2) {
                return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
            }

            public void mousePressed(final MouseEvent e) {
                GraphUI.this.requestFocus();
                GraphUI.this.dragStart = GraphUI.this.dragEnd = null;
                GraphUI.this.draggedEdge = null;
                // handStart = null;
                switch (GraphUI.this.currentTool) {
                    case DefaultToolbox.ANCHOR:
                        // because of a bug in Mac OS X which overloads alt and
                        // ctrl modifiers
                        // it is important that the order of cases (popup -> alt
                        // -> else) is the
                        // same in Document and GraphUI.
                        if (GUI.isPopupTrigger(e)) {
                            this.showContextMenu(e.getX(), e.getY());
                        } else if (e.isAltDown() || e.getButton() == MouseEvent.BUTTON2) {
                            // don't do anything
                        } else {
                            EdgeUI selectedEdge = this.findEdge(e);

                            if (selectedEdge == null) {
                                GraphUI.this.getSelectionModel().clear();
                                GraphUI.this.dragStart = e.getPoint();
                                GraphUI.this.draggedEdge = null;
                            } else {
                                Edge edge = selectedEdge.getEdge();
                                if (e.isShiftDown()) {
                                    GraphUI.this.selectElement(edge, !GraphUI.this
                                            .getSelectionModel().contains(edge));
                                } else {
                                    if (!GraphUI.this.getSelectionModel().contains(edge)) {
                                        GraphUI.this.getSelectionModel().clear();
                                        GraphUI.this.selectElement(edge, true);
                                    }
                                }
                            }
                            GraphUI.this.repaint();
                        }
                        break;
                    case DefaultToolbox.DELETE:
                        EdgeUI selectedEdge = this.findEdge(e);
                        if (selectedEdge == null) {
                            GraphUI.this.getSelectionModel().clear();
                            GraphUI.this.dragStart = e.getPoint();
                            GraphUI.this.draggedEdge = null;
                        }
                        else{
                            GraphUI.this.deleteElements(Collections.singleton(selectedEdge
                                    .getEdge()));
                        }
                        break;
                    default:
                        break;
                }
            }

            public void mouseDragged(MouseEvent e) {
                if (GraphUI.this.currentTool == DefaultToolbox.ANCHOR || GraphUI.this.currentTool == DefaultToolbox.DELETE) {
                    if (GraphUI.this.dragStart != null) {
                        Rectangle r = GraphUI.this.getSelectingArea();
                        // constrain to canvas bounds
                        GraphUI.this.dragEnd = new Point(e.getX(), e.getY());
                        GraphUI.this.keepInside(GraphUI.this.dragEnd);
                        if (r == null) {
                            r = GraphUI.this.getSelectingArea();
                        } else {
                            r = r.union(GraphUI.this.getSelectingArea());
                        }
                        r = r.union(this.selectElements(GraphUI.this.getSelectingArea()));


                        r.width++;
                        r.height++;
                        GraphUI.this.repaint(r);
                    }
                }
            }

            public void mouseReleased(MouseEvent e) {
                if ((GraphUI.this.dragStart != null)
                        && (GraphUI.this.dragEnd != null)) {
                    if (GraphUI.this.currentTool == DefaultToolbox.ANCHOR) {
                        this.selectElements(GraphUI.this.getSelectingArea());
                    }
                    else if (GraphUI.this.currentTool == DefaultToolbox.DELETE) {
                        GraphUI.this.deleteElements(GraphUI.this.getSelectionModel().getSelectedObjects(Node.class));
                    }
                }
                GraphUI.this.dragStart = null;
                GraphUI.this.dragEnd = null;
                // handStart = null;

                GraphUI.this.repaint();
                GraphUI.this.updateMenus();
            }

            private Rectangle selectElements(Rectangle r) {
                Rectangle update = new Rectangle(r);
                for (VisualGraphElement elem : GraphUI.this.ui.keySet()) {
                    UIElement ui = GraphUI.this.ui.get(elem);
                    Rectangle bounds = ui.getVisibleBounds();
                    if (r.intersects(bounds)) {
                        if (!GraphUI.this.getSelectionModel().contains(elem)) {
                            GraphUI.this.selectElement(elem, true);
                            update = update.union(ui.getBounds());
                        }
                    } else {
                        if (GraphUI.this.getSelectionModel().contains(elem)) {
                            GraphUI.this.selectElement(elem, false);
                            update = update.union(ui.getBounds());
                        }
                    }
                }
                return update;
            }

            private EdgeUI findEdge(MouseEvent e) {
                // reuse one line object
                Polygon line = new Polygon();

                EdgeUI selectedEdge = null;
                for (NodeUI<?> ng : GraphUI.this.nodes) {
                    for (Edge edge : ng.getNode().edges()) {
                        if (edge.getTarget() != null) {
                            line = GraphUI.this.getEdgeLine(edge, line);
                            for (int k = 0; (k < line.npoints - 1) && (selectedEdge == null); k++) {
                                long x1 = line.xpoints[k];
                                long y1 = line.ypoints[k];
                                long x2 = line.xpoints[k + 1];
                                long y2 = line.ypoints[k + 1];
                                long x3 = e.getX();
                                long y3 = e.getY();

                                /*
                 * long dist = ((x3 - x1) (y2 - y1)) - ((y3 - y1) (x2 - x1));
                 * System.out.println(dist); if (Math.abs(dist) <= 3) return
                 * edge;
                                 */
                                double straight = this.distance(x1, y1, x2, y2);
                                double via
                                        = this.distance(x1, y1, x3, y3) + this.distance(x3, y3, x2, y2);
                                if (Math.abs(straight - via) < 0.2) {
                                    return ng.getEdgeUI(edge);
                                }

                            }
                        }
                    }
                }
                return null;
            }

            private void showContextMenu(final int x, final int y) {
                CmdPopupMenu popup
                        = new CmdPopupMenu(Resources.getString("InsertNode"),
                                GraphUI.this);

                GraphUI.this.fillNodeInsertPopup(popup, x, y, false);

                JMenuItem item = new JMenuItem(Resources.getString("Comment"));
                item.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent unused) {

                        final Comment comment = new Comment();
                        comment.setLocation(x, y);
                        GraphUI.this.getGraph().addComment(comment);
                        GraphUI.this.repaint();
                    }
                });
                item.setEnabled(!GraphUI.this.isReadOnly());
                popup.add(item);

                popup.addSeparator();
                popup.addItem(Resources.getString("Variables") + "...",
                        GraphUI.cmdEditVariables);
                popup.addItem(Resources.getString("Functions") + "...",
                        GraphUI.cmdEditFunctions);
                popup.addItem(Resources.getString("GroovyFunctions") + "...",
                        GraphUI.cmdEditGroovyFunctions);
                popup.addItem("Groovy " + Resources.getString("Variables") + "...",
                        GraphUI.cmdEditGroovyVariables);
                popup.addItem(Resources.getString("Grammars") + "...",
                        GraphUI.cmdEditGrammars);
                if (GraphUI.this.getGraph().supportsHandlers()) {
                    popup.addItem(Resources.getString("InputHandlers") + "...",
                            GraphUI.cmdEditHandlers);
                }
                popup.addItem(Resources.getString("CanvasSize") + "...",
                        GraphUI.cmdCanvasSize);
                // popup.addSeparator();
                // popup.add(new CmdMenuItem(Resources.getString("Validate"),
                // cmdValidate, KeyEvent.VK_K, GraphUI.this, true));

                popup.updateMenus();
                popup.show(GraphUI.this, x, y);
            }
        };

        this.addMouseListener(m);
        this.addMouseMotionListener(m);

        this.viewScroller = new MouseInputAdapter() {
            private Point handStart = null;
            private boolean isDrag = false;

            // because of a bug in Mac OS X which overloads alt and ctrl
            // modifiers
            // it is important that the order of cases (popup -> alt -> else) is
            // the
            // same in Document and GraphUI.
            public synchronized void mousePressed(MouseEvent e) {
                GraphUI.this.requestFocus();
                this.handStart = null;
                this.isDrag = false;

                Point pos = e.getPoint();
                Rectangle r
                        = GUI
                                .getRelativeBounds(e.getComponent(), GraphUI.this.graphScrollPane
                                        .getViewport());
                pos.translate(r.x, r.y);
                if (GUI.isPopupTrigger(e)) {
                    // don't drag or scroll on popup
                } else if (e.isAltDown()
                        || (GraphUI.this.currentTool == DefaultToolbox.HAND) || e.getButton() == MouseEvent.BUTTON2) {
                    this.handStart
                            = GraphUI.this.graphScrollPane.getViewport().getViewPosition();
                    this.handStart.translate(pos.x, pos.y);
                } else if (GraphUI.this.currentTool == DefaultToolbox.ANCHOR) {
                    this.isDrag = true;
                }
            }

            public synchronized void mouseDragged(MouseEvent e) {
                Point pos = e.getPoint();
                Rectangle r
                        = GUI
                                .getRelativeBounds(e.getComponent(), GraphUI.this.graphScrollPane
                                        .getViewport());
                pos.translate(r.x, r.y);

                if (this.handStart != null) {
                    Dimension d
                            = GraphUI.this.graphScrollPane.getViewport().getExtentSize();
                    pos.x = this.handStart.x - pos.x;
                    pos.y = this.handStart.y - pos.y;

                    // constrain to viewport bounds
                    pos.x
                            = Math.max(0, Math.min(pos.x, GraphUI.this.getWidth() - d.width));
                    pos.y
                            = Math.max(0, Math.min(pos.y, GraphUI.this.getHeight() - d.height));
                    GraphUI.this.graphScrollPane.getViewport().setViewPosition(pos);
                } else if (this.isDrag) {
                    Dimension d
                            = GraphUI.this.graphScrollPane.getViewport().getExtentSize();
                    int x = 0, y = 0;
                    if (pos.x < 0) {
                        x = pos.x;
                    } else if (pos.x > d.width) {
                        x = pos.x - d.width;
                    }

                    if (pos.y < 0) {
                        y = pos.y;
                    } else if (pos.y > d.height) {
                        y = pos.y - d.height;
                    }

                    if ((x != 0) || (y != 0)) {
                        Point p
                                = GraphUI.this.graphScrollPane.getViewport().getViewPosition();
                        p.translate(x, y);
                        p.x = Math.max(0, Math.min(p.x, GraphUI.this.getWidth() - d.width));
                        p.y
                                = Math.max(0, Math.min(p.y, GraphUI.this.getHeight() - d.height));
                        GraphUI.this.graphScrollPane.getViewport().setViewPosition(p);
                    }
                }
            }

            public synchronized void mouseReleased(MouseEvent e) {
                this.handStart = null;
                this.isDrag = false;
            }
        };

        this.addMouseListener(this.viewScroller);
        this.addMouseMotionListener(this.viewScroller);
        this.setAutoscrolls(true);
    }

    public void fillNodeInsertPopup(JPopupMenu popup, final int x, final int y,
            boolean structured) {
        JMenu nodeMenu = null;

        if (!structured) {
            nodeMenu = new JMenu(Resources.getString("InsertNode"));
        }

        Map<Object, List<Class<Node>>> allNodeTypes = Node.getAvailableNodeTypes();
        for (Iterator<Object> keys = allNodeTypes.keySet().iterator(); keys.hasNext();) {
            Object key = keys.next();
            if (structured) {
                nodeMenu = new JMenu(key.toString());
            }
            List<Class<Node>> nodeTypes = allNodeTypes.get(key);
            for (Class<Node> nodeType : nodeTypes) {
                if (nodeType == null) {
                    nodeMenu.add(new JSeparator());
                } else {
                    if (!EndNode.class.isAssignableFrom(nodeType)
                            || this.getGraph().supportsEndNode(nodeType)) {
                        nodeMenu.add(this.createNodeItem(nodeType, x, y));
                    }
                }
            }

            if (structured) {
                popup.add(nodeMenu);
            } else {
                if (keys.hasNext()) {
                    nodeMenu.add(new JSeparator());
                }
            }
        }

        if (!structured) {
            popup.add(nodeMenu);
        }
    }

    public JScrollPane getScrollPane() {
        return this.graphScrollPane;
    }

    public void adjustCursor(int mode) {
        Cursor cursor;

        switch (mode) {
            case DefaultToolbox.HAND:
                cursor = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
                break;
            default:
                /*
         * // problem with transparency if (mode >= DefaultToolbox.ADD_NODE &&
         * mode < DefaultToolbox.ADD_NODE + Node.NODE_TYPES.length) { try {
         * Class nodeType = Node.NODE_TYPES[mode-DefaultToolbox.ADD_NODE]; Icon
         * icon = NodeComponent.getNodeIcon(nodeType); String name =
         * Resources.getString(Node.getNodeTypeName(nodeType)); Dimension d =
         * getToolkit().getBestCursorSize(icon.getIconWidth(),
         * icon.getIconHeight()); BufferedImage im = new BufferedImage(d.width,
         * d.height, BufferedImage.TYPE_INT_ARGB); Graphics g =
         * im.getGraphics(); if (icon instanceof ImageIcon)
         * g.drawImage(((ImageIcon) icon).getImage(), (d.width -
         * icon.getIconWidth())/2, (d.height - icon.getIconHeight())/2,
         * Color.white, this); else icon.paintIcon(this, g, (d.width -
         * icon.getIconWidth())/2, (d.height - icon.getIconHeight())/2);
         * g.dispose(); cursor = getToolkit().createCustomCursor(im, new
         * Point(icon.getIconWidth()/2, icon.getIconHeight()/2), name); } catch
         * (Exception exn) { System.err.println(exn); cursor =
         * Cursor.getDefaultCursor(); } } else
                 */
                cursor = Cursor.getDefaultCursor();
                break;
        }

        this.setCursor(cursor);
    }

    public static ActionListener createNodeInsertAction(final GraphUI graph,
            final Class<? extends Node> c, final int x, final int y) {
        return new ActionListener() {
            public void actionPerformed(ActionEvent unused) {
                if (!EndNode.class.isAssignableFrom(c)
                        || graph.getGraph().supportsEndNode(c)) {
                    try {
                        Node n = c.newInstance();
                        n.setProperty("location", new Point(x, y));
                        graph.undo.addEdit(new NodeEdit(graph.getGraph(), n, true));
                        n.updateEdges();
                        graph.validate();
                        graph.updateMenus();
                    } catch (Exception exn) {
                        exn.printStackTrace();
                        OptionPane.error(graph, exn.getLocalizedMessage());
                    }
                } else {
                    OptionPane.message(graph, Resources.format(
                            "NodeTypeNotSupportedInGraph", Node
                                    .getLocalizedNodeTypeName(c)));
                }
            }
        };
    }

    private JMenuItem createNodeItem(final Class<? extends Node> c, final int x,
            final int y) {
        String name = Node.getLocalizedNodeTypeName(c);
        JMenuItem item = new JMenuItem(name);
        item.addActionListener(GraphUI.createNodeInsertAction(this, c, x, y));
        item.setIcon(NodeComponent.getNodeIcon(c));
        item.setEnabled(!this.isReadOnly());
        return item;
    }

    public static JMenuItem createColorItem(String name, final boolean hicolor,
            ActionListener action) {
        JMenuItem item = new JRadioButtonMenuItem(Resources.getString(name));
        final Color c = GraphUI.gNodeColors.get(name);
        item.setIcon(new Icon() {

            public void paintIcon(Component comp, Graphics g, int x, int y) {
                g.setColor(c);
                g.fillRect(x, y, this.getIconWidth() - 1, this.getIconHeight() - 1);
                if (hicolor) {
                    NodeComponent.paintGradient(g, x + 1, y + 1, this.getIconWidth() - 1,
                            this.getIconHeight() - 1);
                }
                g.setColor(Color.black);
                g.drawRect(x, y, this.getIconWidth() - 1, this.getIconHeight() - 1);
            }

            public int getIconWidth() {
                return 16;
            }

            public int getIconHeight() {
                return 12;
            }
        });
        item.addActionListener(action);
        return item;
    }

    public JMenuItem createColorItem(String name, final ColorizableElement element) {
        final Color c = GraphUI.gNodeColors.get(name);
        JMenuItem item
                = GraphUI.createColorItem(name, Version.HICOLOR, new ActionListener() {

                    private void setColor(final ColorizableElement o) {
                        final Color old_color = o.getColor();
                        if (!c.equals(old_color)) {
                            GraphUI.this.undo.addEdit(new AbstractEdit("Color") {

                                public void run() {

                                    o.setColor(c);
                                }

                                public void unrun() {

                                    o.setColor(old_color);
                                }
                            });
                        }
                    }

                    public void actionPerformed(ActionEvent e) {
                        if (element != null) {
                            this.setColor(element);
                        } else {
                            GraphUI.this.undo.startEdit(null);
                            for (ColorizableElement o : GraphUI.this.getSelectionModel()
                                    .getSelectedObjects(
                                            ColorizableElement.class)) {
                                this.setColor(o);
                            }
                            GraphUI.this.undo.endEdit();
                        }
                        GraphUI.this.updateMenus();
                    }
                });
        if (element != null) {
            item.setSelected(c.equals(element.getColor()));
        } else {
            boolean elementsFound = false;
            boolean selected = true;
            for (Iterator<ColorizableElement> it
                    = this.getSelectionModel().getSelectedObjects(
                            ColorizableElement.class).iterator(); it.hasNext() && selected;) {
                ColorizableElement o = it.next();
                elementsFound = true;
                if (!c.equals(o.getColor())) {
                    selected = false;
                }
            }
            item.setSelected(elementsFound && selected);
        }
        item.setEnabled(!this.isReadOnly());
        return item;
    }

    private void initAnts() {
        if (this.ants != null) {
            this.ants = new Ants();
            Thread t = new Thread(this.ants, "GraphUI marching ants");
            t.setPriority(Thread.MIN_PRIORITY);
            t.setDaemon(true);
            t.start();
        }
    }

    public static void initMenu(CmdMenu menu) {
        menu.addItem(Resources.getString("Delete"), Commands.cmdDelete, KeyStroke
                .getKeyStroke(
                        KeyEvent.VK_BACK_SPACE, 0));
        menu.addItem(Resources.getString("Delete"), Commands.cmdDelete, KeyStroke
                .getKeyStroke(
                        KeyEvent.VK_DELETE, 0));
        menu.addItem(Resources.getString("Up"), GraphUI.cmdUp, KeyStroke
                .getKeyStroke(
                        KeyEvent.VK_UP, 0));
        menu.addItem(Resources.getString("Down"), GraphUI.cmdDown, KeyStroke
                .getKeyStroke(
                        KeyEvent.VK_DOWN, 0));
        menu.addItem(Resources.getString("Left"), GraphUI.cmdLeft, KeyStroke
                .getKeyStroke(
                        KeyEvent.VK_LEFT, 0));
        menu.addItem(Resources.getString("Right"), GraphUI.cmdRight, KeyStroke
                .getKeyStroke(
                        KeyEvent.VK_RIGHT, 0));
    }

    public synchronized void addNotify() {
        super.addNotify();
        if (this.ui == null) {
            this.ui = new Hashtable<VisualGraphElement, UIElement>();
            this.nodes.clear();
            this.removeAll();
            Graph graph = this.getGraph();
            for (Node n : graph.getNodes()) {
                this.elementAdded(graph, n);
            }

            for (Comment c : graph.getComments()) {
                this.elementAdded(graph, c);
            }

            this.model.addGraphListener(this);
        }

        if (GraphUI.marchingAnts) {
            this.initAnts();
        }
    }

    public synchronized void removeNotify() {
        // in rare cases, removeNotify will be called twice, e.g. when we have
        // already disposed the window, but it is still owned by another window
        // because it is not finalized yet.
        // Therefore we need to make sure, that our cleanup code runs only once
        if (this.ui != null) {
            this.getSelectionModel().clear();

            this.model.removeGraphListener(this);
            for (UIElement e : this.ui.values()) {
                e.dispose();
            }

            this.ui = null;
        }
        if (this.ants != null) {
            this.ants.stop();
            this.ants = null;
        }
        super.removeNotify();
    }

    public void selectElement(GraphElement e, boolean selected) {
        if (selected) {
            this.getSelectionModel().add(e);
        } else {
            this.getSelectionModel().remove(e);
        }
        if (e instanceof GroupElement) {
            Group g = Group.getTopGroup((GroupElement) e);
            if (g != null) {
                for (Iterator<GroupElement> ns = g.leafs(); ns.hasNext();) {
                    GroupElement o = ns.next();
                    if (o instanceof GraphElement) {
                        GraphElement leaf = (GraphElement) o;
                        if (selected) {
                            this.getSelectionModel().add(leaf);
                        } else {
                            this.getSelectionModel().remove(leaf);
                        }
                    }
                }
            }
        }
    }

    public void setElementColor(Color c) {
        Collection<ColorizableElement> selection
                = this.getSelectionModel().getSelectedObjects(
                        ColorizableElement.class);
        if (selection.isEmpty()) {
            return;
        }
        if (c == null) {
            long r = 0, g = 0, b = 0;
            for (ColorizableElement o : selection) {
                Color nc = o.getColor();
                r += nc.getRed();
                g += nc.getGreen();
                b += nc.getBlue();
            }
            c = new Color((int) (r / selection.size()), (int) (g / selection.size()),
                    (int) (b / selection.size()));
            c = JColorChooser.showDialog(this, Resources.getString("ChooseColor"), c);
        }
        if (c != null) {
            final Color color = c;
            this.undo.startEdit(null);
            for (final ColorizableElement o : selection) {
                final Color old_color = o.getColor();
                if (!c.equals(old_color)) {
                    this.undo.addEdit(new AbstractEdit("Color") {
                        public void run() {
                            o.setColor(color);
                        }

                        public void unrun() {
                            o.setColor(old_color);
                        }
                    });
                }
            }
            this.undo.endEdit();
            this.updateMenus();
        }
    }

    public void propertyChange(final PropertyChangeEvent evt) {
        if ((evt.getSource() instanceof Toolbox)
                && evt.getPropertyName().equals("CurrentTool")) {
            this.currentTool = ((Integer) evt.getNewValue()).intValue();
            this.adjustCursor(this.currentTool);
        } else if (evt.getSource() instanceof NodeUI) {
            if (evt.getPropertyName().equals("nodeActive")) {
                if (this.isShowing() && (this.graphScrollPane.getViewport() != null)
                        && evt.getNewValue().equals(Boolean.TRUE)) {
                    if (this.dragStart == null) {// don't center during edge dragging
                        final NodeUI<?> n = (NodeUI<?>) evt.getSource();
                        if (n.getNode() instanceof InputNode) {
                            GUI.scrollIntoCenter(n, this.graphScrollPane.getViewport());
                        }
                    }
                }
            } else if (evt.getPropertyName().equals("nodeMoved")) {
                if (this.dragStart == null) {
                    // the node was moved in another view. Redraw edges.
                    Node n = ((NodeUI<?>) evt.getSource()).getNode();
                    final Rectangle r
                            = this.getVisualGraphElementBounds(Collections.singleton(n), true);
                    Point oldLoc = (Point) evt.getOldValue();
                    Point newLoc = (Point) evt.getNewValue();
                    int dx = newLoc.x - oldLoc.x;
                    if (dx > 0) { // moved to the right, so extend to the left
                        r.x -= dx;
                        r.width += dx;
                    } else {
                        r.width -= dx;
                    }
                    int dy = newLoc.y - oldLoc.y;
                    if (dy > 0) { // moved to the bottom, so extend to the top
                        r.y -= dy;
                        r.height += dy;
                    } else {
                        r.height -= dy;
                    }
                    this.repaint(r);
                }
            }
        }
    }

    private void moveSelection(int h, int v) {
        Collection<MoveableElement> selection
                = this.getSelectionModel().getSelectedObjects(
                        MoveableElement.class);
        if (selection.size() > 0) {
            Rectangle bounds = this.getSelectionBounds(true);
            if (bounds != null) {
                if (bounds.x + h < 0) {
                    h = -bounds.x;
                } else if (bounds.x + bounds.width + h > this.getWidth()) {
                    h = this.getWidth() - (bounds.x + bounds.width);
                }
                if (bounds.y + v < 0) {
                    v = -bounds.y;
                } else if (bounds.y + bounds.height + v > this.getHeight()) {
                    v = this.getHeight() - (bounds.y + bounds.height);
                }

                if ((h != 0) || (v != 0)) {
                    this.undo.addEdit(new MoveEdit(selection, new Point(0, 0), new Point(
                            h, v)));
                }

                bounds.add(this.getSelectionBounds(true));
                this.repaint(bounds);
            }
        }
    }

    private Group groupSelection() {
        return Group.group(this.getSelectionModel().getSelectedObjects(
                GroupElement.class));
    }

    private boolean selectionGrouped() {
        for (GroupElement o : this.getSelectionModel().getSelectedObjects(
                GroupElement.class)) {
            if (o.getGroup() != null) {
                return true;
            }
        }
        return false;
    }

    public boolean doCommand(final int cmd) {
        Set<Node> selectedNodes;
        switch (cmd) {

            // case showGraph:
            // // getN
            // System.out.println("nodes size:" + nodes.size());
            // System.out.println(nodes);
            // System.out.println("graph.getNodes()" + getGraph().getNodes().size());
            // System.out.println(getGraph().getNodes());
            // System.out.println("keys size:" + ui.keySet().size());
            // System.out.println("values size:" + ui.values().size());
            // for (VisualGraphElement elem : ui.keySet()) {
            // System.out.println("elem:" + elem + "\n value:" + this.ui.get(elem));
            // ;
            // }
            // break;
            case cmdUndo:
                this.undo.undo();
                this.repaint();
                break;

            case cmdRedo:
                this.undo.redo();
                this.repaint();
                break;

            case cmdCanvasSize:
                CanvasSizeDialog d
                        = new CanvasSizeDialog(this, this.getSize(), this.isReadOnly());
                d.setVisible(true);
                Dimension newsize = d.getNewSize();
                Dimension oldsize = this.getSize();
                if (newsize != null ? !newsize.equals(this.getSize()) : false) {
                    this.undo.startEdit(Resources.getString("CanvasSize"));
                    int anchor = d.getAnchor();
                    int xoffset = 0, yoffset = 0;
                    switch (anchor % 3) {
                        case 0:
                            xoffset = 0;
                            break;
                        case 1:
                            xoffset = (newsize.width - oldsize.width) / 2;
                            break;
                        case 2:
                            xoffset = newsize.width - oldsize.width;
                            break;
                    }
                    switch (anchor / 3) {
                        case 0:
                            yoffset = 0;
                            break;
                        case 1:
                            yoffset = (newsize.height - oldsize.height) / 2;
                            break;
                        case 2:
                            yoffset = newsize.height - oldsize.height;
                            break;
                    }
                    for (int i = 0; i < this.getComponentCount(); i++) {
                        Component c = this.getComponent(i);
                        Point p = c.getLocation();
                        p.translate(newsize.width < oldsize.width ? xoffset : 0,
                                newsize.height < oldsize.height ? yoffset : 0);
                        GraphUI.keepInside(p, c.getSize(), newsize);
                        this.undo.addEdit(new MoveEdit(c, c.getLocation(), p));
                    }
                    this.undo.addEdit(new CanvasEdit(this.getGraph(), oldsize, newsize));
                    for (int i = 0; i < this.getComponentCount(); i++) {
                        Component c = this.getComponent(i);
                        Point p = c.getLocation();
                        p.translate(newsize.width > oldsize.width ? xoffset : 0,
                                newsize.height > oldsize.height ? yoffset : 0);
                        GraphUI.keepInside(p, c.getSize(), newsize);
                        this.undo.addEdit(new MoveEdit(c, c.getLocation(), p));
                    }
                    this.undo.endEdit();
                }
                break;

            case cmdEditVariables:
                VariableDialog.showDialog(this, this.getGraph().getVariables(),
                        this.getGraph().getGroovyVariables(), Resources
                        .getString("Variables"));
                this.getGraph().setDirty(true);
                break;

            case cmdEditGroovyVariables:
                GroovyVariableDialog.showDialog(this,
                        this.getGraph().getGroovyVariables(), "Groovy " + Resources.getString("Variables"),
                        this.getGraph().getVariables());
                this.getGraph().setDirty(true);
                break;

            case cmdEditFunctions:
                List<Functions> functions = this.getGraph().getFunctions();
                FunctionsEditor.showDialog(this, functions);
                this.getGraph().setFunctions(functions);
                this.getGraph().setDirty(true);
                break;

            case cmdEditGroovyFunctions:
                String groovyFunctions = this.getGraph().getGroovyScript();
                this.getGraph().setGroovyFunctions(GroovyScriptEditorDialog.editScript(this, groovyFunctions));
                this.getGraph().setDirty(true);
                break;

            case cmdEditParameters:
                VariableDialog.showDialog(this, ((Procedure) this.getGraph())
                        .getParameters(), this.getGraph().getGroovyVariables(), Resources
                        .getString("Parameters"));
                this.getGraph().setDirty(true);
                break;

            case cmdEditReturnVariables:
                VariableDialog.showDialog(this, ((Procedure) this.getGraph())
                        .getReturnVariables(), this.getGraph().getGroovyVariables(),
                        Resources.getString("ReturnVariables"));
                this.getGraph().setDirty(true);
                break;

            case cmdEditGrammars:
                List<Grammar> grammars = this.getGraph().getGrammars();
                GrammarsEditor.showDialog(this, grammars);
                this.getGraph().setGrammars(grammars);
                this.getGraph().setDirty(true);
                break;

            case cmdEditHandlers:
                if (this.getGraph().supportsHandlers()) {
                    InputHandlerDialog.showDialog(this, this.getGraph());
                    this.getGraph().updateEdges();
                    this.getGraph().setDirty(true);
                }
                break;

            case cmdImport:
                this.getGraph().importGraph(this);
                break;

            case cmdFind:
                NodeSearchDialog.showDialog();
                break;

            case cmdBreakpoint:
                Set<Node> nodes
                        = this.getSelectionModel().getSelectedObjects(Node.class);
                boolean isBreakpoint = false;
                for (Iterator<Node> n = nodes.iterator(); !isBreakpoint && n.hasNext();) {
                    if (n.next().isBreakpoint()) {
                        isBreakpoint = true;
                    }
                }
                for (Node n : nodes) {
                    n.setBreakpoint(!isBreakpoint);
                }
                break;

            case cmdCut:
            case cmdDelete:
                if (cmd == Commands.cmdCut) {
                    this.doCommand(Commands.cmdCopy);
                }

                this.deleteSelection();
                this.repaint();
                this.updateMenus();
                break;

            case cmdCopy:
                if (this.getSelectionModel().size(VisualGraphElement.class) > 0) {
                    GraphUI.getClipboard().setContents(
                            new NodeCopy(this.getGraph().getMainOwner(), this
                                    .getSelectionModel()
                                    .getSelectedObjects(VisualGraphElement.class)), this);
                }
                break;

            case cmdCollateNodes:
                selectedNodes = this.getSelectionModel().getSelectedObjects(Node.class);
                if (selectedNodes.size() > 0) {
                    String title = OptionPane.input(this, new String[]{
                        Resources.getString("PleaseEnterSubgraphName"),
                        Resources.getString("CollateWarning")});
                    if (title != null) {
                        try {
                            this.collateNodes(selectedNodes, title);
                            this.repaint();
                        } catch (IllegalArgumentException exn) {
                            OptionPane.error(this, exn.getLocalizedMessage());
                        }
                    }
                }
                break;

            case cmdPaste:
                try {
                    this.getSelectionModel().clear();
                    Transferable t = GraphUI.getClipboard().getContents(this);
                    if (t instanceof NodeCopy) {
                        NodeCopy nc = (NodeCopy) t;
                        if (nc.getOwner() != this.getGraph().getMainOwner()) {
                            throw new Exception(
                                    "You cannot copy nodes from one model to another because that would "
                                    + "destroy references to variables and devices.");
                        } else {
                            this.paste(nc.getElements(), this.getGraph(), 20, 20);

                            this.revalidate();
                        }
                    }
                } catch (Exception exn) {
                    if (Version.DEBUG || (exn instanceof IllegalStateException)) {
                        exn.printStackTrace();
                    }
                    OptionPane.message(this, new String[]{
                        Resources.getString("ErrorWhilePasting"),
                        exn.getLocalizedMessage()}, Resources.getString("ClipboardError"),
                            OptionPane.ERROR);
                }
                break;

            case cmdSelectAll:
                this.getSelectionModel().clear();
                for (Node n : this.getGraph().getNodes()) {
                    this.getSelectionModel().add(n);
                }
                for (Comment c : this.getGraph().getComments()) {
                    this.getSelectionModel().add(c);
                }
                this.repaint();
                break;

            case cmdAlign:
                Set<MoveableElement> moveables
                        = this.getSelectionModel().getSelectedObjects(
                                MoveableElement.class);
                if (moveables.size() > 1) {
                    int alignment = AlignmentDialog.chooseAlignment(this);
                    if (alignment != -1) {
                        int h = 0, v = 0;
                        int num = 0;
                        for (MoveableElement elem : moveables) {
                            Rectangle r = ((Component) this.ui.get(elem)).getBounds();
                            switch (alignment) {
                                case 0:
                                    h += r.x;
                                    break;
                                case 1:
                                    h += r.x + r.width / 2;
                                    break;
                                case 2:
                                    h += r.x + r.width;
                                    break;
                                case 4:
                                    v += r.y;
                                    break;
                                case 5:
                                    v += r.y + r.height / 2;
                                    break;
                                case 6:
                                    v += r.y + r.height;
                                    break;
                            }
                            num++;
                        }
                        h /= num;
                        v /= num;

                        this.undo.startEdit(Resources.getString("Align"));

                        for (MoveableElement elem : moveables) {
                            Rectangle r = ((Component) this.ui.get(elem)).getBounds();
                            Point p = new Point();
                            switch (alignment) {
                                case 0:
                                    p.setLocation(h, r.y);
                                    break;
                                case 1:
                                    p.setLocation(h - r.width / 2, r.y);
                                    break;
                                case 2:
                                    p.setLocation(h - r.width, r.y);
                                    break;
                                case 4:
                                    p.setLocation(r.x, v);
                                    break;
                                case 5:
                                    p.setLocation(r.x, v - r.height / 2);
                                    break;
                                case 6:
                                    p.setLocation(r.x, v - r.height);
                                    break;
                            }
                            this.undo.addEdit(new MoveEdit(Collections.singleton(elem), r
                                    .getLocation(), p));
                        }

                        this.undo.endEdit();

                        this.repaint();
                    }
                }
                break;

            case cmdGroup:
                if (this.getSelectionModel().size(GroupElement.class) > 1) {
                    Group g = this.groupSelection();
                    this.undo.addEdit(new GroupEdit(g));
                }
                break;

            case cmdUngroup:
                if (this.selectionGrouped()) {
                    Set<Group> groups = new HashSet<Group>();
                    for (GroupElement elem : this.getSelectionModel().getSelectedObjects(
                            GroupElement.class)) {
                        groups.add(Group.getTopGroup(elem));
                    }
                    this.undo.startEdit(null);
                    for (Group g : groups) {
                        Set<GroupElement> children = Group.ungroup(g);
                        this.undo.addEdit(new GroupEdit(children));
                    }
                    this.undo.endEdit();
                }
                break;

            case cmdColorOther:
                this.setElementColor(null);
                break;

            case cmdUp:
                this.moveSelection(0, -1);
                break;
            case cmdDown:
                this.moveSelection(0, 1);
                break;
            case cmdLeft:
                this.moveSelection(-1, 0);
                break;
            case cmdRight:
                this.moveSelection(1, 0);
                break;

            default:
                return false;
        }

        return true;
    }

    public boolean menuItemState(int cmd) {
        switch (cmd) {
            case cmdSelectAll:
                return this.getComponentCount() > 0;

            case cmdUndo:
                return this.undo.canUndo();

            case cmdRedo:
                return this.undo.canRedo();

            case cmdDelete:
            case cmdCut:
                if (this.getGraph().isReadOnly()) {
                    return false;
                }
                Collection<?> selection
                        = this.getSelectionModel().getSelectedObjects(null);
                return (selection.size() > 1)
                        || ((selection.size() == 1) && !(selection.iterator().next() instanceof StartNode));

            case cmdCopy:
            case cmdCollateNodes:
                if (this.getGraph().isReadOnly()) {
                    return false;
                }
                return this.getSelectionModel().size(MoveableElement.class) > 0;

            case cmdPaste:
                if (this.getGraph().isReadOnly()) {
                    return false;
                }
                Transferable t = GraphUI.getClipboard().getContents(this);
                // if (t instanceof NodeCopy)
                // return ((NodeCopy) t).getOwner() ==
                // getGraph().getMainOwner();
                // else
                return t != null ? t.isDataFlavorSupported(NodeCopy.dataflavour)
                        : false;

            case cmdAlign:
                if (this.isReadOnly()) {
                    return false;
                }
                return this.getSelectionModel().size(VisualGraphElement.class) > 1;

            case cmdGroup:
                if (this.isReadOnly()) {
                    return false;
                }
                return this.getSelectionModel().size(VisualGraphElement.class) > 1;

            case cmdUngroup:
                if (this.isReadOnly()) {
                    return false;
                }
                return this.selectionGrouped();

            case cmdColorMenu:
            case cmdColorOther:
            case cmdUp:
            case cmdDown:
            case cmdLeft:
            case cmdRight:
                return this.getSelectionModel().size(VisualGraphElement.class) > 0;

            case cmdCanvasSize:
                return !this.isReadOnly();

            case cmdEditVariables:
            case cmdEditFunctions:

            case cmdEditGroovyVariables:
            case cmdEditGroovyFunctions:
            case cmdEditGrammars:
                return true;

            case cmdEditHandlers:
                return this.getGraph().supportsHandlers();

            case cmdEditParameters:
            case cmdEditReturnVariables:
                return this.getGraph() instanceof Procedure;

            case cmdImport:
                return !this.isReadOnly();

            // case showGraph:
            // return true;
            case cmdFind:
                return true;

            case cmdBreakpoint:
                return this.getSelectionModel().size(Node.class) > 0;

            default:
                return false;
        }
    }

    public String menuItemName(int cmd, String oldName) {
        switch (cmd) {
            case cmdUndo:
                return this.undo.canUndo() ? this.undo.getUndoPresentationName()
                        : Resources
                                .getString("CantUndo");

            case cmdRedo:
                return this.undo.canRedo() ? this.undo.getRedoPresentationName()
                        : Resources
                                .getString("CantRedo");

            case cmdBreakpoint:
                Set<Node> nodes
                        = this.getSelectionModel().getSelectedObjects(Node.class);
                boolean isBreakpoint = false;
                for (Iterator<Node> n = nodes.iterator(); !isBreakpoint && n.hasNext();) {
                    if (n.next().isBreakpoint()) {
                        isBreakpoint = true;
                    }
                }
                return isBreakpoint ? Resources.getString("ClearBreakpoint")
                        : Resources
                                .getString("SetBreakpoint");

            default:
                return oldName;
        }
    }

    private Mapping paste(Collection<VisualGraphElement> elements,
            final Graph graph, int h_offset,
            int v_offset) {
        this.undo.startEdit(Resources.getString("Paste"));

        final Set<VisualGraphElement> new_elements
                = new HashSet<VisualGraphElement>();
        final Mapping map = new Mapping();
        Node startTarget = null;
        for (VisualGraphElement elem : elements) {
            VisualGraphElement new_elem;
            if (elem instanceof StartNode) {
                StartNode n = (StartNode) elem;
                StartNode n2 = graph.getStartNode();
                map.addNode(n, n2);
                map.addEdge(n.getEdge(0), n2.getEdge(0));
                startTarget = n.getEdge(0).getTarget();
                new_elem = n2;
            } else {
                elem.setLocation(elem.getX() + h_offset, elem.getY() + v_offset);

                if (elem instanceof Node) {
                    Class<? extends Node> c = ((Node) elem).getClass();
                    if (EndNode.class.isAssignableFrom(c) && !graph.supportsEndNode(c)) {
                        c = graph.supportedEndNodes()[0];
                    }

                    new_elem = ((Node) elem).clone(c, map);
                } else {
                    new_elem = elem.clone(map);
                }
            }

            new_elements.add(new_elem);
        }

        for (VisualGraphElement elem : new_elements) {
            elem.update(map);
        }
        startTarget = map.getNode(startTarget);

        for (final VisualGraphElement elem : new_elements) {
            if (elem instanceof StartNode) {
                StartNode n = (StartNode) elem;
                if ((startTarget != null) && (n.getEdge(0).getTarget() == null)
                        && new_elements.contains(startTarget)) {
                    this.undo.addEdit(new EdgeEdit(n.getEdge(0), startTarget));
                }
            } else if (elem instanceof Node) {
                this.undo.addEdit(new NodeEdit(graph, (Node) elem, true));
            } else if (elem instanceof Comment) {
                this.undo.addEdit(new AbstractEdit("AddComment") {

                    public void run() {

                        GraphUI.this.getGraph().addComment((Comment) elem);
                    }

                    public void unrun() {

                        GraphUI.this.getGraph().removeComment((Comment) elem);
                    }
                });
            } else {
                throw new IllegalArgumentException("Unexpected type of element: "
                        + elem.getClass());
            }
        }

        this.undo.addEdit(new AbstractEdit("Update edges") {
            public void run() {
                for (VisualGraphElement elem : new_elements) {
                    if (elem instanceof Node) {
                        ((Node) elem).updateEdges();
                    }
                }
            }

            public void unrun() {
            }
        });

        if (graph == this.getGraph()) {
            this.undo.addEdit(new AbstractEdit("Update selection") {
                public void run() {
                    GraphUI.this.getSelectionModel().clear();
                    for (GraphElement elem : new_elements) {
                        GraphUI.this.selectElement(elem, true);
                    }
                }

                public void unrun() {
                    for (GraphElement elem : new_elements) {
                        GraphUI.this.selectElement(elem, false);
                    }
                }
            });
        }

        this.undo.endEdit();

        return map;
    }

    private void collateNodes(Collection<? extends VisualGraphElement> selection,
            String name)
            throws IllegalArgumentException {
        this.undo.startEdit(Resources.getString("Collate"));

        int minx = Integer.MAX_VALUE;
        int miny = Integer.MAX_VALUE;
        int maxx = Integer.MIN_VALUE;
        int maxy = Integer.MIN_VALUE;

        // We cannot collate StartNodes and EndNodes, so remove them
        Set<Node> nodes = new HashSet<Node>();
        Set<VisualGraphElement> others = new HashSet<VisualGraphElement>();
        for (VisualGraphElement elem : selection) {
            if (!((elem instanceof StartNode) || (elem instanceof EndNode))) {
                if (elem instanceof Node) {
                    nodes.add((Node) elem);
                } else {
                    others.add(elem);
                }

                // Record the node's position
                minx = Math.min(minx, elem.getX());
                maxx = Math.max(maxx, elem.getX() + elem.getWidth());
                miny = Math.min(miny, elem.getY());
                maxy = Math.max(maxy, elem.getY() + elem.getHeight());
            }
        }

        if ((nodes.size() == 0) && (others.size() == 0)) {
            throw new IllegalArgumentException("Can't collate end nodes.");
        }

        Node root = null;
        Collection<Edge> outgoing = new LinkedList<Edge>();
        Collection<Node> targets = new LinkedHashSet<Node>();
        for (Node n : nodes) {
            // First collect all in-edges of the group.
            // We must find a single or no root.
            for (Edge e : n.in_edges()) {
                if (!nodes.contains(e.getSource())) {
                    if ((root == null) || (root == n)) {
                        root = n;
                    } else {
                        throw new IllegalArgumentException(
                                "Can't collate node group, because it has more than one starting point.");
                    }
                }
            }

            // Then collect all outgoing edges of the group.
            // Their targets will be represented by end nodes of the subgraph.
            for (Edge e : n.edges()) {
                if (!nodes.contains(e.getTarget())) {
                    outgoing.add(e);
                    if (e.getTarget() != null) {
                        targets.add(e.getTarget());
                    }
                }
            }
        }

        // - Copy all selected nodes
        // - Adjust the graph's canvas so that all nodes will fit
        // - Paste them into the graph
        // - Connect the graph's StartNode to the root of the group
        // - Add an end node to the graph for each outgoing edge's target
        // - Add new graph node
        // - Connect incoming nodes to the new graph node
        // - Connect the outgoing edges of the GraphNode to the original targets
        // of the outgoing edges.
        // - Delete original nodes
        try {
            OwnerNode owner = new GraphNode();
            owner.setGraphName(name);
            owner.setProperty("location", new Point((minx + maxx) / 2,
                    (miny + maxy) / 2));

            NodeCopy copy = new NodeCopy(this.getGraph().getMainOwner(),
                    new MetaCollection<VisualGraphElement>(nodes, others));

            Graph g = owner.getOwnedGraph();
            g.setSize(Math.max(maxx - minx + 20, Math.max(targets.size() * 80 + 20,
                    400)), Math
                            .max(maxy - miny + 150, 300));
            g.getStartNode().setProperty("location", new Point(g.getWidth() / 2, 10));

            Mapping m = this.paste(copy.getElements(), g, -minx + 10
                    + (g.getWidth() - (maxx - minx + 20)) / 2, -miny + 70);

            if (root != null) {
                this.undo.addEdit(new EdgeEdit(g.getStartNode().getEdge(0), m
                        .getNode(copy.getMapping()
                                .getNode(root))));
            }

            int t = 0;
            Map<Node, Node> targetMap = new HashMap<Node, Node>();
            for (Iterator<Node> it = targets.iterator(); it.hasNext(); t++) {
                Node n = it.next();
                Node endNode = g.supportedEndNodes()[0].newInstance();
                this.undo.addEdit(new NodeEdit(g, endNode, true));
                endNode.setTitle("-> " + n.getTitle());
                targetMap.put(n, endNode);
                this.undo.addEdit(new MoveEdit(Collections.singleton(endNode),
                        (Point) endNode
                                .getProperty("location"), new Point(t * 80
                        + (g.getWidth() - targets.size() * 80) / 2, g.getHeight() - 40)));
            }

            // rewire outgoing edges to new end nodes
            // and new edges of the graph node to the original targets
            for (Edge e : outgoing) {
                Node originalTarget = e.getTarget();
                // System.out.println("Outgoing edge: " + e);
                // System.out.println(" Target: " + e.getTarget());
                if (originalTarget != null) {
                    Edge newEdge = m.getEdge(copy.getMapping().getEdge(e));
                    if (newEdge.getTarget() != null) {
                        throw new IllegalStateException();
                    }
                    // System.out.println(targetMap.get(e.getTarget()));
                    Node endNode = targetMap.get(originalTarget);
                    this.undo.addEdit(new EdgeEdit(newEdge, endNode));
                    this.undo.addEdit(new EdgeEdit(owner.getEdge(owner
                            .getEndNodeIndex(endNode)),
                            originalTarget));
                    if (endNode.in_edges().size() == 1) {
                        Edge edge = endNode.in_edges().iterator().next();
                        if (!StringTools.isEmpty(edge.getCondition())) {
                            endNode.setTitle(edge.getCondition());
                        } else {
                            endNode.setTitle(edge.getSource().getTitle());
                        }
                    }
                }
            }

            this.undo.addEdit(new NodeEdit(this.getGraph(), owner, true));

            // rewire incoming edges
            if (root != null) {
                for (Edge e : new ArrayList<Edge>(root.in_edges())) {
                    this.undo.addEdit(new EdgeEdit(e, owner));
                }
            }

            this.deleteElements(nodes);
            this.deleteElements(others);
            this.getSelectionModel().clear();
            this.selectElement(owner, true);
            for (GraphElement elem : selection) {
                if ((elem instanceof StartNode) || (elem instanceof EndNode)) {
                    this.selectElement(elem, true);
                }
            }
        } catch (Exception exn) {
            OptionPane.error(this, exn);
        }

        this.undo.endEdit();
    }

    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        // there is nothing we can do at the moment
    }

    protected void addImpl(Component comp, Object constraints, int index) {
        if (!(comp instanceof UIElement)) {
            throw new IllegalArgumentException("GraphUI only accepts UIElements.");
        }
        super.addImpl(comp, constraints, index);
    }

    private MouseInputListener setupElementMouseHandler(
            final VisualGraphElement element,
            final MouseHandler primaryHandler) {
        return new MouseInputAdapter() {
            Rectangle dragRect;
            Point originalPosition;

            public void mousePressed(MouseEvent evt) {
                boolean eventHandled = false;
                if (primaryHandler != null) {
                    eventHandled = primaryHandler.mousePressed(evt);
                }

                if (!eventHandled) {
                    switch (GraphUI.this.currentTool) {
                        case DefaultToolbox.ANCHOR:
                            GraphUI.this.dragStart = new Point(evt.getX(), evt.getY());
                            GraphUI.this.dragEnd = null;
                            GraphUI.this.draggedEdge = null;
                            if (evt.isShiftDown()) {
                                if (GraphUI.this.getSelectionModel().contains(element)) {
                                    GraphUI.this.selectElement(element, false);
                                    GraphUI.this.dragStart = null; // don't drag when removing
                                    // an element from the selection
                                } else {
                                    GraphUI.this.selectElement(element, true);
                                }
                                GraphUI.this.repaint();
                            } else {
                                if (!GraphUI.this.getSelectionModel().contains(element)) {
                                    GraphUI.this.getSelectionModel().clear();
                                    GraphUI.this.selectElement(element, true);
                                    GraphUI.this.repaint();
                                }
                            }
                            if (GraphUI.this.dragStart != null) {
                                GraphUI.this.ui.get(element).requestFocus();
                                this.dragRect = GraphUI.this.getSelectionBounds();
                                this.originalPosition
                                        = new Point(element.getX(), element.getY());
                            }
                            break;
                        case DefaultToolbox.DELETE:
                            GraphUI.this.deleteElements(Collections.singleton(element));
                            break;
                    }
                }
            }

            public void mouseReleased(MouseEvent evt) {
                if (primaryHandler != null) {
                    primaryHandler.mouseReleased(evt);
                }

                if (GraphUI.this.currentTool == DefaultToolbox.ANCHOR) {
                    if (this.originalPosition != null) {
                        Point drag_p = new Point(element.getX(), element.getY());
                        if (!drag_p.equals(this.originalPosition)) {
                            GraphUI.this.undo.addEdit(new MoveEdit(GraphUI.this
                                    .getSelectionModel().getSelectedObjects(
                                            MoveableElement.class), this.originalPosition, drag_p, true));
                        }
                    }
                }

                GraphUI.this.dragStart = null;
                GraphUI.this.dragEnd = null;
                GraphUI.this.draggedEdge = null;
                this.dragRect = null;
                this.originalPosition = null;
                GraphUI.this.repaint();
                GraphUI.this.updateMenus();
            }

            public void mouseDragged(MouseEvent evt) {
                if (primaryHandler != null) {
                    primaryHandler.mouseDragged(evt);
                }

                if (this.dragRect != null) {
                    int ex = evt.getX() - GraphUI.this.dragStart.x;
                    int ey = evt.getY() - GraphUI.this.dragStart.y;

                    this.dragRect.translate(ex, ey);
                    if (this.dragRect.x < 0) {
                        ex -= this.dragRect.x;
                        this.dragRect.x = 0;
                    } else {
                        int xoff
                                = this.dragRect.x + this.dragRect.width - GraphUI.this.getWidth();
                        if (xoff > 0) {
                            this.dragRect.x -= xoff;
                            ex -= xoff;
                        }
                    }
                    if (this.dragRect.y < 0) {
                        ey -= this.dragRect.y;
                        this.dragRect.y = 0;
                    } else {
                        int yoff
                                = this.dragRect.y + this.dragRect.height - GraphUI.this.getHeight();
                        if (yoff > 0) {
                            this.dragRect.y -= yoff;
                            ey -= yoff;
                        }
                    }

                    if ((ex != 0) || (ey != 0)) {
                        Rectangle selectionClip = GraphUI.this.getSelectionBounds(true);

                        for (MoveableElement o : GraphUI.this.getSelectionModel()
                                .getSelectedObjects(
                                        MoveableElement.class)) {
                            if (Preferences.getPrefs().snapToGrid.getValue()
                                    ^ evt.isShiftDown()) {
                                int raster = Preferences.getPrefs().gridSize.getValue();
                                int snapx
                                        = GraphUI.this.snap(o.getX() + ex + o.getWidth() / 2, raster);
                                int snapy
                                        = GraphUI.this.snap(o.getY() + ey + o.getHeight() / 2, raster);
                                o.setLocation(snapx - o.getWidth() / 2, snapy - o.getHeight()
                                        / 2);
                            } else {
                                o.setLocation(o.getX() + ex, o.getY() + ey);
                            }
                        }
                        selectionClip.add(GraphUI.this.getSelectionBounds(true));
                        GraphUI.this.repaint(selectionClip);
                    }
                }
            }
        };
    }

    private void nodeAdded(Graph g, Node n, final NodeUI<?> ng) {
        this.nodes.add(ng);
        ng.addPropertyChangeListener(this);

        MouseInputListener m = this.setupElementMouseHandler(n, new MouseHandler() {

            Node target;

            private void replaceNode(Node source, Node target, Node add,
                    boolean copyEdges) {

                GraphUI.this.undo.startEdit(Resources.getString("ConvertNode"));

                if (add != null) {
                    GraphUI.this.undo.addEdit(new NodeEdit(GraphUI.this.getGraph(), add,
                            true));
                }

                GraphUI.this.undo.addEdit(new NodeEdit(GraphUI.this.getGraph(), target,
                        true));

                if (copyEdges) {
                    for (Edge edge : new ArrayList<Edge>(source.in_edges())) {
                        GraphUI.this.undo.addEdit(new EdgeEdit(edge, target));
                    }

                    for (int i = 0; i < source.numEdges(); i++) {
                        GraphUI.this.undo
                                .addEdit(new EdgeEdit(target.getEdge(i), source.getEdge(i)
                                        .getTarget()));
                    }
                }

                GraphUI.this.undo.addEdit(new NodeEdit(GraphUI.this.getGraph(), source,
                        false));

                GraphUI.this.undo.endEdit();
                GraphUI.this.repaint();
                GraphUI.this.updateMenus();
            }

            public boolean mousePressed(MouseEvent e) {
                // bring selected node to front
                GraphUI.this.toFront(ng);
                ng.requestFocus();

                if (GUI.isPopupTrigger(e)) {
                    final Component c = ng.getComponentAt(e.getX(), e.getY());
                    if (c instanceof NodeComponent) {
                        final Node node = ((NodeComponent<?>) c).getNode();
                        JPopupMenu popup = new JPopupMenu();
                        JMenuItem item;

                        item = new JMenuItem(node.isBreakpoint() ? Resources
                                .getString("ClearBreakpoint") : Resources
                                .getString("SetBreakpoint"));
                        item.addActionListener(new ActionListener() {

                            public void actionPerformed(ActionEvent e) {

                                node.setBreakpoint(!node.isBreakpoint());
                            }
                        });
                        popup.add(item);

                        ButtonGroup group = new ButtonGroup();
                        JMenu colorMenu = new JMenu(Resources.getString("Color"));
                        boolean selected = false;
                        for (String name : GraphUI.gNodeColors.keySet()) {
                            item = GraphUI.this.createColorItem(name, node);
                            group.add(item);
                            colorMenu.add(item);
                            if (item.isSelected()) {
                                selected = true;
                            }
                        }

                        colorMenu.addSeparator();
                        item
                                = new JRadioButtonMenuItem(Resources.getString("Other") + "...");
                        group.add(item);
                        // item = new JCheckBoxMenuItem(Resources.getString("Other") +
                        // "...");
                        item.addActionListener(new ActionListener() {

                            public void actionPerformed(ActionEvent e) {

                                Color color
                                        = JColorChooser.showDialog(GraphUI.this, Resources
                                                .getString("ChooseColor"), (Color) node
                                                .getProperty("color"));
                                if (color != null) {
                                    node.setProperty("color", color);
                                }
                            }
                        });
                        if (!selected) {
                            item.setSelected(true);
                        }
                        colorMenu.add(item);

                        popup.add(colorMenu);

                        popup.addSeparator();

                        item = new JMenuItem(Resources.getString("DeleteSelection"));
                        item.addActionListener(new ActionListener() {

                            public void actionPerformed(ActionEvent e) {

                                GraphUI.this.deleteSelection();
                            }
                        });
                        item.setEnabled(!GraphUI.this.isReadOnly());
                        popup.add(item);

                        if (node instanceof EndNode) {
                            Class<Node>[] supportedEndNodes
                                    = GraphUI.this.getGraph().supportedEndNodes();
                            for (int i = 0; i < supportedEndNodes.length; i++) {
                                final Class<? extends Node> nodeClass = supportedEndNodes[i];
                                if (nodeClass != node.getClass()) {
                                    item = new JMenuItem(Resources.format("ConvertTo", Node
                                            .getLocalizedNodeTypeName(nodeClass)));
                                    item.addActionListener(new ActionListener() {

                                        public void actionPerformed(ActionEvent e) {

                                            try {
                                                Node newNode = nodeClass.newInstance();
                                                if (node.getTitle().equals(
                                                        Node.getLocalizedNodeTypeName(node
                                                                .getClass()))) {
                                                    newNode.setTitle(Node
                                                            .getLocalizedNodeTypeName(newNode
                                                                    .getClass()));
                                                } else {
                                                    newNode.setTitle(node.getTitle());
                                                }
                                                newNode.setProperty("location", new Point(
                                                        (Point) node.getProperty("location")));

                                                replaceNode(node, newNode, null, true);
                                            } catch (Exception exn) {
                                                OptionPane.error(GraphUI.this, exn);
                                            }
                                        }
                                    });
                                    item.setEnabled(!GraphUI.this.isReadOnly());
                                    popup.add(item);
                                }
                            }
                        } else if (node instanceof ProcNode) {
                            item = new JMenuItem("Convert to Subgraph");
                            item.addActionListener(new ActionListener() {

                                public void actionPerformed(ActionEvent e) {

                                    GraphNode gn = new GraphNode(((Procedure) ((ProcNode) node)
                                            .getOwnedGraph()).toGraph());
                                    gn.setTitle(node.getTitle());
                                    gn.setProperty("location", new Point((Point) node
                                            .getProperty("location")));

                                    replaceNode(node, gn, null, false);
                                }
                            });
                            popup.add(item);
                        } else if (node instanceof GraphNode) {
                            item = new JMenuItem("Convert to Procedure");
                            item.addActionListener(new ActionListener() {

                                public void actionPerformed(ActionEvent e) {

                                    ProcNode gn = new ProcNode(((GraphNode) node)
                                            .getOwnedGraph().toProcedure());
                                    gn.setTitle(node.getTitle());
                                    Point location = (Point) node.getProperty("location");

                                    if (node.isConnected()) {
                                        CallNode call = new CallNode();
                                        call.setProperty("procedure", gn);
                                        call.setTitle(node.getTitle());
                                        call.setProperty("location", new Point(location));
                                        gn.setProperty("location", new Point(location.x + 40,
                                                location.y + 20));

                                        replaceNode(node, call, gn, true);
                                    } else {
                                        gn.setProperty("location", new Point(location));
                                        replaceNode(node, gn, null, false);
                                    }
                                }
                            });
                            item.setEnabled(!GraphUI.this.isReadOnly());
                            popup.add(item);
                        } else if (node instanceof CallNode) {
                            final CallNode n = (CallNode) node;
                            item
                                    = new JMenuItem(Resources.getString("ShowCalledProcedure"));
                            item.setEnabled(n.getProcedure() != null);
                            item.addActionListener(new ActionListener() {

                                public void actionPerformed(ActionEvent e) {

                                    ProcNode proc = n.getProcedure();
                                    if (proc != null) {
                                        GraphEditorFactory.show(proc);
                                    }
                                }
                            });
                            popup.add(item);
                        } else if (node instanceof GotoNode) {
                            final GotoNode n = (GotoNode) node;
                            item = new JMenuItem(Resources.getString("ShowGotoTarget"));
                            item.setEnabled(n.getTarget() != null);
                            item.addActionListener(new ActionListener() {

                                public void actionPerformed(ActionEvent e) {

                                    LabelNode label = n.getTarget();
                                    if (label != null) {
                                        GraphEditor editor = GraphEditorFactory.show(label
                                                .getGraph().getOwner());
                                        editor.getGraphUI().selectAndShowNode(label);
                                    }
                                }
                            });
                            popup.add(item);
                        }

                        popup.addSeparator();

                        item = new JMenuItem(Resources.getString("Rename") + "...");
                        if (node instanceof OwnerNode) {
                            item.addActionListener(new ActionListener() {

                                public void actionPerformed(ActionEvent evt) {

                                    ((OwnerNode) node).getOwnedGraph().rename(GraphUI.this);
                                }
                            });
                        } else {
                            item.addActionListener(new ActionListener() {

                                public void actionPerformed(ActionEvent evt) {

                                    String s = OptionPane.edit(GraphUI.this, Resources
                                            .getString("EnterName"), Resources
                                            .getString("NodeName"), node.getTitle());
                                    if (s != null) {
                                        GraphUI.this.renameNode(node, s);
                                    }
                                }
                            });
                        }
                        item.setEnabled(!GraphUI.this.isReadOnly());
                        popup.add(item);

                        item = new JMenuItem(node instanceof OwnerNode ? (Resources
                                .getString("Edit") + "...") : (Resources
                                .getString("Properties") + "..."));
                        item.addActionListener(new ActionListener() {

                            public void actionPerformed(ActionEvent e) {

                                node.editProperties(ng);
                            }
                        });
                        popup.add(item);

                        popup.show(ng, e.getX(), e.getY());
                    }
                } else if ((GraphUI.this.currentTool == DefaultToolbox.HAND)
                        || e.isAltDown()) {
                    // don't do anything
                } else if (GraphUI.this.currentTool == DefaultToolbox.ANCHOR) {
                    GraphUI.this.dragStart = new Point(e.getX(), e.getY());
                    GraphUI.this.dragEnd = null;
                    GraphUI.this.draggedEdge = null;

                    Component c = ng.getComponentAt(e.getX(), e.getY());
                    if (c instanceof EdgeUI) {
                        Edge edge = ((EdgeUI) c).getEdge();
                        if (e.isShiftDown()) {
                            if (GraphUI.this.getSelectionModel().contains(edge)) {
                                GraphUI.this.selectElement(edge, false);
                            } else {
                                GraphUI.this.selectElement(edge, true);
                            }
                        } else {
                            if (!GraphUI.this.getSelectionModel().contains(edge)) {
                                GraphUI.this.getSelectionModel().clear();
                                GraphUI.this.selectElement(edge, true);
                            }

                            GraphUI.this.dragStart
                                    = ((EdgeUI) c).getOutputRelativeTo(GraphUI.this);
                            GraphUI.this.draggedEdge = edge;
                            this.target = null;
                        }
                        GraphUI.this.repaint();
                    } else if (c instanceof NodeComponent) {
                        if (e.getClickCount() > 1) {
                            GraphUI.this.dragStart = null; // don't drag, because we are
                            // about to open a dialog
                            GraphUI.this.getSelectionModel().clear();
                            GraphUI.this.selectElement(ng.getNode(), true);
                            GraphUI.this.repaint();
                            ng.getNode().editProperties(ng);
                        } else {
                            return false;
                        }
                    } else if (c == ng) {
                        ParentMouseInputListener.propagateEvent(e, GraphUI.this);
                    }
                } else if (GraphUI.this.currentTool == DefaultToolbox.DELETE) {
                    GraphUI.this.deleteElements(Collections.singleton(ng.getNode()));
                }

                return true;
            }

            public void mouseReleased(MouseEvent e) {
                if (GraphUI.this.currentTool == DefaultToolbox.ANCHOR) {
                    if ((GraphUI.this.dragEnd != null)
                            && (GraphUI.this.draggedEdge != null)) {
                        if (this.target != null) {
                            this.target.setActive(false);
                            GraphUI.this.undo.addEdit(new EdgeEdit(GraphUI.this.draggedEdge,
                                    this.target));
                        }
                    }
                }
            }

            public void mouseDragged(MouseEvent e) {
                if ((GraphUI.this.currentTool == DefaultToolbox.ANCHOR)
                        && (GraphUI.this.dragStart != null)) {
                    if (GraphUI.this.draggedEdge != null) {
                        Rectangle r = new Rectangle(GraphUI.this.dragStart);
                        if (GraphUI.this.dragEnd != null) {
                            r.add(GraphUI.this.dragEnd);
                        }
                        GraphUI.this.dragEnd = ng.getLocation();
                        GraphUI.this.dragEnd.translate(e.getX(), e.getY());
                        r.add(GraphUI.this.dragEnd);
                        r.grow(2, 2); // compensate anti-aliasing

                        Component c = GraphUI.this.getComponentAt(GraphUI.this.dragEnd);
                        Node newtarget = null;
                        if (c instanceof NodeComponent) {
                            newtarget = ((NodeComponent<?>) c).getNode();
                        } else if (c instanceof NodeUI) {
                            Rectangle body = GUI.getRelativeBounds(((NodeUI<?>) c).getBody(),
                                    GraphUI.this);
                            if (body.contains(GraphUI.this.dragEnd)) {
                                newtarget = ((NodeUI<?>) c).getNode();
                            }
                        }

                        if (newtarget != this.target) {
                            if (this.target != null) {
                                this.target.setActive(false);
                            }
                            if ((newtarget != null) && newtarget.supportsEdges()) {
                                this.target = newtarget;
                                this.target.setActive(true);
                            } else {
                                this.target = null;
                            }
                        }

                        GraphUI.this.repaint(r);
                    }

                }
            }
        });

        ng.setMouseInputListener(m);

        if (this.isShowing()) {
            this.revalidate();
        }
    }

    public void renameNode(final Node node, final String name) {
        final String oldName = node.getTitle();
        if (!name.equals(oldName)) {
            this.undo.addEdit(new AbstractEdit(Resources.getString("Rename")) {

                public void run() {

                    node.setTitle(name);
                }

                public void unrun() {

                    node.setTitle(oldName);
                }
            });
        }
    }

    private void deleteSelection() {
        this.deleteElements(this.getSelectionModel().getSelectedObjects(
                GraphElement.class));
    }

    void deleteElements(final Set<? extends GraphElement> elements) {
        String title = "Delete";
        if (elements.size() == 1) {
            Object element = elements.iterator().next();
            if (element instanceof Comment) {
                title = "DeleteComment";
            } else if (element instanceof Node) {
                title = "DeleteNode";
            } else if (element instanceof Edge) {
                title = "DeleteEdge";
            }
        }

        this.undo.startEdit(Resources.getString(title));

        /*
     * for (Object o: selection) { if (o instanceof EdgeUI) deleteEdge((EdgeUI)
     * o); else if (o instanceof NodeUI) deleteNodes(nodes.toArray(new
     * NodeUI[nodes.size()])); } clearSelection();
         */
        // Restore the selection state on Undo
        this.undo.addEdit(new AbstractEdit("") {

            public void run() {

            }

            public void unrun() {

                GraphUI.this.getSelectionModel().clear();
                for (GraphElement elem : elements) {
                    GraphUI.this.selectElement(elem, true);
                }
            }
        });

        // Delete each element in the selection (undoable)
        for (final GraphElement elem : elements) {
            if (elem instanceof Edge) {
                this.undo.addEdit(new EdgeEdit((Edge) elem, null));
            } else if (elem instanceof Node) {
                Node n = (Node) elem;
                if (!(n instanceof StartNode)) {
                    this.undo.addEdit(new NodeEdit(this.getGraph(), n, false));
                }
            } else if (elem instanceof Comment) {
                this.undo.addEdit(new AbstractEdit("DeleteComment") {

                    public void run() {

                        GraphUI.this.getGraph().removeComment((Comment) elem);
                    }

                    public void unrun() {

                        GraphUI.this.getGraph().addComment((Comment) elem);
                    }
                });
            } else {
                throw new IllegalArgumentException("Unexpected type of element: "
                        + elem.getClass());
            }
        }
        this.undo.endEdit();

        this.repaint();
        this.updateMenus();
        this.requestFocus();
    }

    private int snap(int x, int raster) {
        // System.out.println(x);
        if (x >= 0) {
            return ((x + raster / 2) / raster) * raster;
        } else {
            return ((x - raster / 2) / raster) * raster;
        }
    }

    public void toFront(UIElement element) {
        super.remove(element);
        super.add(element, 0);
    }

    public void remove(NodeUI<?> n) {
        super.remove(n);
        this.nodes.remove(n);

        n.removePropertyChangeListener(this);
        this.ui.remove(n.getNode());
        n.dispose();
    }

    public void elementAdded(Graph g, VisualGraphElement element) {
        UIElement ui;
        if (element instanceof Comment) {
            ui = new CommentUI((Comment) element, this);
        } else if (element instanceof Node) {
            ui = ((Node) element).createUI(this, this.viewScroller);
        } else {
            throw new IllegalArgumentException("Unexpected type of element: "
                    + element.getClass());
        }

        int x = element.getX();
        int y = element.getY();

        // __location
        // x -= ng.getWidth()/2;
        // y -= ng.getHeight()/2;
        x = Math.max(Math.min(x, this.getWidth() - ui.getWidth()), 0);
        y = Math.max(Math.min(y, this.getHeight() - ui.getHeight()), 0);

        ui.setLocation(x, y);

        this.ui.put(element, ui);
        this.add(ui, 0);

        if (element instanceof Node) {
            this.nodeAdded(g, (Node) element, (NodeUI<?>) ui);
        } else {
            MouseInputListener l = this.setupElementMouseHandler(element, null);
            ui.addMouseListener(l);
            ui.addMouseMotionListener(l);
        }

        this.repaint();
    }

    public void elementRemoved(Graph g, VisualGraphElement element) {
        this.getSelectionModel().remove(element);
        UIElement ui = this.ui.remove(element);
        ui.dispose();
        this.remove(ui);
        this.repaint();
    }

    public void sizeChanged(Graph g, int width, int height) {
        super.setSize(width, height);
    }

    public void graphRenamed(Graph g, String name) {
    }

    public Graph getGraph() {
        return this.model;
    }

    public void updateMenus() {
        MenuOwner c = GUI.getMenuOwnerForComponent(this.getParent());
        if (c != null) {
            c.updateMenus();
        }
    }

    public void layout() {
        this.doLayout();
    }

    public void doLayout() {

    }


    /*
   * public void print() { JobAttributes jobAttributes = new JobAttributes();
   * PageAttributes pageAttributes = new PageAttributes();
   * pageAttributes.setColor(PageAttributes.ColorType.COLOR); if (getWidth() >
   * getHeight())
   * pageAttributes.setOrientationRequested(PageAttributes.OrientationRequestedType
   * .LANDSCAPE); else
   * pageAttributes.setOrientationRequested(PageAttributes.OrientationRequestedType
   * .PORTRAIT);
   * pageAttributes.setPrinterResolution(getToolkit().getScreenResolution());
   * PrintJob pjob = getToolkit().getPrintJob(GUI.getFrameForComponent(this),
   * getGraph().getName(), jobAttributes, pageAttributes); if (pjob != null) {
   * Graphics pg = pjob.getGraphics(); Graphics gfx = pg instanceof Graphics2D ?
   * pg : pg.create(); Dimension pageSize = pjob.getPageDimension(); if (gfx
   * instanceof Graphics2D) { double scale = Math.min((double) getWidth() /
   * (double) pageSize.width, (double) getHeight() / (double) pageSize.height);
   * ((Graphics2D) gfx).scale(scale, scale); } printAll(pg); if (gfx != pg)
   * gfx.dispose(); pg.dispose(); pjob.end(); } }
     */
    public void print() {
        PrinterJob printJob = PrinterJob.getPrinterJob();
        PageFormat pageSetup = printJob.defaultPage();

        if (this.getWidth() > this.getHeight()) {
            pageSetup.setOrientation(PageFormat.LANDSCAPE);
        } else {
            pageSetup.setOrientation(PageFormat.PORTRAIT);
        }

        pageSetup = printJob.pageDialog(pageSetup);

        printJob.setJobName(this.getGraph().getName());
        if (printJob.printDialog()) {
            try {
                printJob.setPrintable(this, printJob.validatePage(pageSetup));
                printJob.print();
            } catch (Exception exn) {
                OptionPane.error(this, exn);
            }
        }
    }

    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
        if (pageIndex == 0) {
            Graphics gfx = graphics;
            if (!(gfx instanceof Graphics2D)) {
                gfx = graphics.create();
            }

            if (gfx instanceof Graphics2D) {
                ((Graphics2D) gfx)
                        .translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                double resolutionAdjustment
                        = Math.min(pageFormat.getImageableWidth() / this.getWidth(),
                                pageFormat.getImageableHeight() / this.getHeight());
                ((Graphics2D) gfx).scale(resolutionAdjustment, resolutionAdjustment);
            } else {
                gfx.translate((int) pageFormat.getImageableX(), (int) pageFormat
                        .getImageableY());
            }

            this.print(gfx);
            // paint(gfx);

            if (gfx != graphics) {
                gfx.dispose();
            }

            return Printable.PAGE_EXISTS;
        } else {
            return Printable.NO_SUCH_PAGE;
        }
    }

    long paintCount = 0;
    private static boolean showPaintTiming = false;

    public void repaint() {
        if (GraphUI.showPaintTiming) {
            System.out
                    .println("Requesting a full repaint at "
                            + new Exception().getStackTrace()[1]);
        }
        super.repaint();
    }

    public void paint(Graphics g) {
        if (g instanceof Graphics2D) {
            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_SPEED);
        }

        if (GraphUI.showPaintTiming) {
            System.out.println(++this.paintCount + " (" + System.currentTimeMillis()
                    + "): "
                    + g.getClipBounds());
        }
        super.paint(g);
    }

    protected void paintChildren(Graphics g) {
        long time = System.currentTimeMillis();
        super.paintChildren(g);
        if (GraphUI.showPaintTiming) {
            System.out
                    .println("Nodes: " + (System.currentTimeMillis() - time) + "ms");
            System.out.flush();
        }

        if (GraphUI.paintEdgesOnTop) {
            this.paintEdges(g);
        }

        this.drawCurrentDrag(g);
    }

    protected synchronized void paintComponent(Graphics g) {
        // System.out.println("GraphUI: paint Component");
        if (g instanceof Graphics2D) {
            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_SPEED);
        }
        super.paintComponent(g);

        if (Preferences.getPrefs().showGrid.getValue()) {
            g.setColor(Preferences.getPrefs().gridColor.getValue());
            int rasterSize = Preferences.getPrefs().gridSize.getValue();
            for (int i = rasterSize; i < this.getWidth(); i += rasterSize) {
                g.drawLine(i, 0, i, this.getHeight());
            }
            for (int i = rasterSize; i < this.getHeight(); i += rasterSize) {
                g.drawLine(0, i, this.getWidth(), i);
            }
        }

        long time = System.currentTimeMillis();
        if (!GraphUI.paintEdgesOnTop) {
            this.paintEdges(g);
        }
        if (GraphUI.showPaintTiming) {
            System.out
                    .println("Edges: " + (System.currentTimeMillis() - time) + "ms");
        }
    }

    protected void paintEdges(Graphics g) {

        Color saveColor = g.getColor();
        Font saveFont = g.getFont();

        Rectangle clip = g.getClipBounds();
        g.setFont(new Font("Sans", 0, 9));

        Color neighbourSelected = Preferences.getPrefs().neighbourColor.getValue();
        Polygon line = new Polygon(); // reuse one line object
        for (NodeUI<?> ng : this.nodes) {
            int j = 0;
            Node n = ng.getNode();
            // int d = n.getSelectionDistance(1);
            for (Edge edge : n.edges()) {
                if (edge.getTarget() != null) {
                    Color edgeColor;
                    if (this.getSelectionModel().contains(edge)) {
                        edgeColor = Color.RED;
                    } else {
                        if (Preferences.getPrefs().showSelectionNeighbours.getValue()
                                && (this.getSelectionModel().contains(n) || this
                                .getSelectionModel()
                                .contains(edge.getTarget()))) {
                            edgeColor = neighbourSelected;
                        } else {
                            edgeColor = n.getPortColor(j).darker();
                        }
                    }

                    this.drawEdge(g, edge, edgeColor, clip, line);
                }
                j++;
            }
        }

        g.setFont(saveFont);
        g.setColor(saveColor);
    }

    /**
     * Return the rectangle that the user is currently selecting. This rectangle
     * will usually be displayed on screen.
     *
     * @return The selecting rectangle of null if the user isn't making a
     * selection.
     */
    private Rectangle getSelectingArea() {

        if ((this.dragStart == null) || (this.dragEnd == null)) {
            return null;
        }
        return new Rectangle(Math.min(this.dragStart.x, this.dragEnd.x), Math.min(
                this.dragStart.y, this.dragEnd.y),
                Math.abs(this.dragEnd.x - this.dragStart.x), Math.abs(this.dragEnd.y
                - this.dragStart.y));
    }

    void drawCurrentDrag(Graphics g) {

        if (this.dragEnd != null) {
            Color saveColor = g.getColor();

            if (this.draggedEdge != null) {
                GraphUI.drawLine(g, this.dragStart, this.dragEnd, Color.red);
            } else {
                Rectangle r = this.getSelectingArea();
                if(GraphUI.this.currentTool == DefaultToolbox.DELETE) {
                    g.setColor(Color.red);
                }
                else {
                    g.setColor(Color.black);
                }

                this.drawMarchingAnts(g, r);
            }

            g.setColor(saveColor);
        }
    }

    private NodeUI<?> getNodeUI(Node n) {

        return (NodeUI<?>) this.ui.get(n);
    }

    private Polygon getEdgeLine(Edge edge, Polygon line) {

        if (line == null) {
            line = new Polygon();
        } else {
            line.reset();
        }

        Point start = this.getNodeUI(edge.getSource()).getOutput(edge, this);
        Point end = this.getNodeUI(edge.getTarget()).getNodeInput();
        final int offset = 5;

        line.addPoint(start.x, start.y);
        if (start.y > end.y - 5) {
            Rectangle startbounds = this.getNodeUI(edge.getSource()).getBounds();
            Rectangle endbounds = this.getNodeUI(edge.getTarget()).getBounds();

            if (startbounds.x + startbounds.width <= endbounds.x - 2 * offset) {
                line.addPoint(startbounds.x + startbounds.width + offset, start.y);
                line.addPoint(endbounds.x - offset, end.y - 5);
            } else if (startbounds.x >= endbounds.x + endbounds.width + 2 * offset) {
                line.addPoint(startbounds.x - offset, start.y);
                line.addPoint(endbounds.x + endbounds.width + offset, end.y - 5);
            } else {
                int right = Math.max(startbounds.x + startbounds.width, endbounds.x
                        + endbounds.width);
                line.addPoint(right + offset, start.y);
                line.addPoint(right + offset, end.y - 5);
            }
        }
        line.addPoint(end.x, end.y - 5);
        line.addPoint(end.x, end.y);

        return line;
    }

    private void drawEdge(Graphics g, Edge edge, Color c, Rectangle clip,
            Polygon line) {

        line = this.getEdgeLine(edge, line);

        Rectangle r = line.getBounds();
        r.width += 2;
        r.height += 2;
        if (r.intersects(clip)) {
            GraphUI.drawPolyLine(g, line, c);

            if (Preferences.getPrefs().showEdgeHandles.getValue()) {
                this.drawEdgeHandles(g, line);
            }

            String label = edge.getCondition();
            if (Preferences.getPrefs().showEdgeLabels.getValue() && (label != null)) {
                int n = line.npoints;
                int x = (line.xpoints[n / 2 - 1] + line.xpoints[n / 2]) / 2;
                int y = (line.ypoints[n / 2 - 1] + line.ypoints[n / 2]) / 2;

                FontMetrics fm = g.getFontMetrics();
                int width = fm.stringWidth(label);
                int height = fm.getAscent() + fm.getDescent();

                Rectangle labelBounds
                        = new Rectangle(x - width / 2 - 2, y - height / 2 - 2,
                                width + 5, height + 5);
                clip = clip.union(labelBounds);
                g.setClip(clip);
                g.setColor(this.getBackground());
                g.fillRect(labelBounds.x, labelBounds.y, labelBounds.width,
                        labelBounds.height);

                g.setColor(c.brighter());
                g
                        .fillRect(x - width / 2 - 1, y - height / 2 - 1, width + 2,
                                height + 2);
                g.setColor(c);
                g
                        .drawRect(x - width / 2 - 1, y - height / 2 - 1, width + 2,
                                height + 2);
                g.setColor(this.getForeground());
                g.drawString(label, x - width / 2, y - height / 2 + fm.getAscent());
            }
        }
    }

    // draw little square boxes at each corner point of the edge
    private void drawEdgeHandles(Graphics g, Polygon line) {

        final int size = 2;
        final int size2 = size + size + 2;
        for (int i = 1; i < line.npoints - 1; i++) {
            int x = line.xpoints[i];
            int y = line.ypoints[i];
            if (i % 2 == 0) {
                g.setColor(Color.black);
                g.fillRect(x - size, y - size, size2, size2);
            } else {
                g.setColor(Color.white);
                g.fillRect(x - size, y - size, size2, size2);
                g.setColor(Color.black);
                g.drawRect(x - size, y - size, size2 - 1, size2 - 1);
            }
        }
    }

    private static void drawLine(Graphics g, Point start, Point end, Color c) {

        g.setColor(c);

        if (g instanceof Graphics2D) {
            Graphics2D gfx = (Graphics2D) g;
            Object oldValue = gfx.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
            gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    GraphUI.lineAntiAliasingSettings);
            Stroke s = gfx.getStroke();
            gfx.setStroke(GraphUI.lineStroke);
            gfx.drawLine(start.x, start.y, end.x, end.y);
            gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldValue);
            gfx.setStroke(s);
        } else {
            g.drawLine(start.x, start.y, end.x, end.y);
            g.drawLine(start.x + 1, start.y, end.x + 1, end.y);
            g.drawLine(start.x, start.y + 1, end.x, end.y + 1);
            g.drawLine(start.x + 1, start.y + 1, end.x + 1, end.y + 1);
        }
    }

    private static void drawPolyLine(Graphics g, Polygon line, Color c) {

        g.setColor(c);

        if (g instanceof Graphics2D) {
            Graphics2D gfx = (Graphics2D) g;
            Stroke s = gfx.getStroke();
            Object oldValue = gfx.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
            gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, GraphUI.lineAntiAliasingSettings);
            gfx.setStroke(GraphUI.lineStroke);
            gfx.drawPolyline(line.xpoints, line.ypoints, line.npoints);
            gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldValue);
            gfx.setStroke(s);
        } else {
            g.drawPolyline(line.xpoints, line.ypoints, line.npoints);
            line.translate(1, 0);
            g.drawPolyline(line.xpoints, line.ypoints, line.npoints);
            line.translate(0, 1);
            g.drawPolyline(line.xpoints, line.ypoints, line.npoints);
            line.translate(-1, 0);
            g.drawPolyline(line.xpoints, line.ypoints, line.npoints);
            line.translate(0, -1);
        }
    }

    private void drawMarchingAnts(Graphics g, Rectangle r) {

        int offset = this.marchingAntsOffset;
        int i;
        int left = r.x, top = r.y, right = left + r.width, bottom = top + r.height;

        if (offset > this.marchingAntsLength) {
            g.drawLine(left, top, Math.min(left + offset - this.marchingAntsLength,
                    right), top);
        }
        for (i = left + offset; i + this.marchingAntsLength < right; i
                += 2 * this.marchingAntsLength) {
            g.drawLine(i, top, i + this.marchingAntsLength, top);
        }
        if (i < right) {
            g.drawLine(i, top, right, top);
            offset = 2 * this.marchingAntsLength - (right - i);
        } else {
            offset = i - right;
        }

        if (offset > this.marchingAntsLength) {
            g.drawLine(right, top, right, Math.min(top + offset
                    - this.marchingAntsLength, bottom));
        }
        for (i = top + offset; i + this.marchingAntsLength < bottom; i
                += 2 * this.marchingAntsLength) {
            g.drawLine(right, i, right, i + this.marchingAntsLength);
        }
        if (i < bottom) {
            g.drawLine(right, i, right, bottom);
            offset = 2 * this.marchingAntsLength - (bottom - i);
        } else {
            offset = i - bottom;
        }

        if (offset > this.marchingAntsLength) {
            g
                    .drawLine(Math.max(right - (offset - this.marchingAntsLength), left),
                            bottom, right,
                            bottom);
        }
        for (i = right - offset; i - this.marchingAntsLength > left; i
                -= 2 * this.marchingAntsLength) {
            g.drawLine(i - this.marchingAntsLength, bottom, i, bottom);
        }
        if (i > left) {
            g.drawLine(left, bottom, i, bottom);
            offset = 2 * this.marchingAntsLength - (i - left);
        } else {
            offset = left - i;
        }

        if (offset > this.marchingAntsLength) {
            g.drawLine(left, Math.max(bottom - (offset - this.marchingAntsLength),
                    top), left, bottom);
        }
        for (i = bottom - offset; i - this.marchingAntsLength > top; i
                -= 2 * this.marchingAntsLength) {
            g.drawLine(left, i - this.marchingAntsLength, left, i);
        }
        if (i > top) {
            g.drawLine(left, top, left, i);
        }

        try {
            int opacity = 60;
            int gray = 128;
            if (GraphUI.this.currentTool != DefaultToolbox.DELETE) {
                g.setColor(new Color(gray, gray, gray, opacity));
            }
            else {
                g.setColor(new Color(gray, 0, 0, opacity));
            }
            g.fillRect(r.x + 1, r.y + 1, r.width - 1, r.height - 1);
        } catch (Exception ignore) {
        }
    }

    public void setSize(int width, int height) {

    }

    public void setSize(Dimension d) {

    }


    /*
   * public void setCanvasSize(int width, int height) { super.setSize(width,
   * height); }
     */
    public Dimension getPreferredSize() {

        return new Dimension(this.getWidth(), this.getHeight());
    }

    public Dimension getMinimumSize() {

        return this.getPreferredSize();
    }

    public Dimension getMaximumSize() {

        return this.getPreferredSize();
    }

    // nodes can overlap, so optimization must be off
    public boolean isOptimizedDrawingEnabled() {

        return false;
    }

    public boolean isFocusTraversable() {

        return true;
    }

    private void keepInside(Point p) {

        p.x = Math.max(0, Math.min(p.x, this.getWidth() - 1));
        p.y = Math.max(0, Math.min(p.y, this.getHeight() - 1));
    }

    @SuppressWarnings("unused")
    private void keepInside(Rectangle r) {

        int x = r.x + r.width - this.getWidth();
        if (x > 0) {
            r.x -= x;
        }
        if (r.x < 0) {
            r.x = 0;
        }

        int y = r.y + r.height - this.getHeight();
        if (y > 0) {
            r.y -= y;
        }
        if (r.y < 0) {
            r.y = 0;
        }
    }

    private static void keepInside(Point p, Dimension csize, Dimension size) {

        int x = p.x + csize.width - size.width;
        if (x > 0) {
            p.x -= x;
        }
        if (p.x < 0) {
            p.x = 0;
        }

        int y = p.y + csize.height - size.height;
        if (y > 0) {
            p.y -= y;
        }
        if (p.y < 0) {
            p.y = 0;
        }
    }

    /**
     * Return the bounding box of all currently selected objects.
     *
     * @return The bounding box of the current selection or null if no object is
     * selected.
     */
    private Rectangle getSelectionBounds() {

        return this.getSelectionBounds(false);
    }

    private Rectangle getSelectionBounds(boolean includeEdges) {

        return this.getVisualGraphElementBounds(this.getSelectionModel()
                .getSelectedObjects(
                        VisualGraphElement.class), includeEdges);
    }

    private Rectangle getVisualGraphElementBounds(
            Collection<? extends VisualGraphElement> selection, boolean includeEdges) {

        Rectangle bounds = null;
        Set<Edge> edges = new HashSet<Edge>();
        for (VisualGraphElement e : selection) {
            Rectangle r
                    = new Rectangle(e.getX(), e.getY(), e.getWidth(), e.getHeight());
            if (bounds == null) {
                bounds = r;
            } else {
                bounds.add(r);
            }
            if ((e instanceof Node) && includeEdges) {
                for (Edge edge : ((Node) e).edges()) {
                    if (edge.getTarget() != null) {
                        edges.add(edge);
                    }
                }
                for (Edge edge : ((Node) e).in_edges()) {
                    edges.add(edge);
                }
            }
        }
        if (bounds != null) {
            Polygon line = new Polygon();
            for (Edge edge : edges) {
                bounds.add(this.getEdgeLine(edge, line).getBounds());
            }
            // compensate line width and antialiasing
            int grow = (int) Math.ceil(GraphUI.lineStroke.getLineWidth()) + 1;
            bounds.grow(grow, grow);
        }
        return bounds;
    }

    private class Ants implements Runnable {

        boolean active = true;

        public void run() {

            try {
                while (this.active) {
                    GraphUI.this.marchingAntsOffset
                            = (GraphUI.this.marchingAntsOffset + 1)
                            % (2 * GraphUI.this.marchingAntsLength);
                    Thread.sleep(50);
                    Rectangle r = GraphUI.this.getSelectingArea();
                    if ((r != null) && GraphUI.this.isVisible()) {
                        r.grow(1, 1);
                        GraphUI.this.repaint(r);
                    }
                }
            } catch (InterruptedException e) {
            }
        }

        public void stop() {

            this.active = false;
        }
    }

    public boolean isReadOnly() {

        return this.getGraph().isReadOnly();
    }

    public int getHorizontalScrollbarHeight() {
        int ret = graphScrollPane.getHorizontalScrollBar().getHeight();
//        System.err.println("horiz scrollbar height: " + ret); // AKAKAK #51
        return ret;
    }
}
