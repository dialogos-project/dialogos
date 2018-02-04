package com.clt.diamant.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import com.clt.diamant.Resources;
import com.clt.diamant.log.ExecutionTree;
import com.clt.diamant.log.LogEvent;
import com.clt.diamant.log.LogPlayer;
import com.clt.diamant.log.TimeLine;
import com.clt.diamant.log.ExecutionTree.ParentNode;
import com.clt.gui.GUI;
import com.clt.gui.ManagedFrame;
import com.clt.util.Platform;

/**
 * @author dabo
 *
 */
public class LogPlayerWindow extends ManagedFrame {

    private LogDocumentWindow document;
    private JTree callTree;
    private JTextPane info;
    private JScrollPane callTreeScroller;

    private WindowListener parentWindowListener = new WindowAdapter() {

        @Override
        public void windowClosed(WindowEvent evt) {

            LogPlayerWindow.this.dispose();
        }
    };

    public LogPlayerWindow(final LogDocumentWindow document) {

        super(Resources.getString("LogFile"));
        this.document = document;

        this.setTitle(Resources.format("LogFileFor", this.getLogPlayer().getVp()));

        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent evt) {

                LogPlayerWindow.this.dispose();

                document.closeEditor();
            }
        });

        Container content = this.getContentPane();
        content.setLayout(new BorderLayout());

        this.callTree
                = new ExecutionTree(new DefaultTreeModel(this.getLogPlayer()
                        .getExecutionRoot()), this);

        this.callTreeScroller
                = GUI.createScrollPane(this.callTree,
                        ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.callTreeScroller.setPreferredSize(new Dimension(350, 350));
        this.callTreeScroller.setMinimumSize(new Dimension(100, 100));
        if (Platform.isMac()) {
            this.callTreeScroller.setBorder(BorderFactory.createEmptyBorder());
        }

        this.info = new JTextPane();
        this.info.setEditorKit(new javax.swing.text.html.HTMLEditorKit());
        this.info.setEditable(false);
        final JScrollPane infoscroller
                = GUI.createScrollPane(this.info,
                        ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        infoscroller.setPreferredSize(new Dimension(300, 350));
        infoscroller.setMinimumSize(new Dimension(100, 100));
        if (Platform.isMac()) {
            infoscroller.setBorder(BorderFactory.createEmptyBorder());
        }

        // content.add(infoscroller, BorderLayout.EAST);
        JSplitPane split
                = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, this.callTreeScroller,
                        infoscroller);
        split.setBorder(BorderFactory.createEmptyBorder());

        content.add(split, BorderLayout.CENTER);

        final TimeLine line = new TimeLine(this.getLogPlayer().getDuration(),
                this.getLogPlayer().getEvents(), new ActionListener() {

            public void actionPerformed(ActionEvent evt) {

                Object src = evt.getSource();
                if (src instanceof LogEvent) {
                    LogEvent<?> e = (LogEvent) src;
                    TreePath p
                            = ((ParentNode) LogPlayerWindow.this.callTree.getModel()
                                    .getRoot()).findEvent(e);
                    if (p != null) {
                        LogPlayerWindow.this.selectAndCenter(p);
                    }
                } else if (src instanceof TimeLine) {
                    long time = new Long(evt.getActionCommand()).longValue();
                    TreePath p
                            = ((ParentNode) LogPlayerWindow.this.callTree.getModel()
                                    .getRoot()).findTime(time);
                    if (p != null) {
                        ParentNode n = (ParentNode) p.getLastPathComponent();
                        // Sonderbehandlung fuer den Start. Wir wollen
                        // nicht die Wurzel selber auswaehlen,
                        // sondern den Start-Knoten. Das ist immer die
                        // Quelle der ersten transition, also
                        // immer genau das erste Kind der Wurzel
                        if (n == LogPlayerWindow.this.callTree.getModel().getRoot()) {
                            LogPlayerWindow.this.selectAndCenter(p
                                    .pathByAddingChild(n.getChildAt(0)));
                        } else {
                            LogPlayerWindow.this.selectAndCenter(p);
                        }
                    }
                }
            }
        });
        final JScrollPane jsp
                = GUI.createScrollPane(line, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS, 600, 200);
        jsp.getHorizontalScrollBar().setUnitIncrement(line.getPixelsPerSecond());
        line.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {

                jsp.getHorizontalScrollBar()
                        .setUnitIncrement(line.getPixelsPerSecond());
            }
        });

        jsp.setRowHeaderView(line.createHeader());

        JComponent lcorner = new JPanel(new BorderLayout());
        lcorner.setBackground(this.getBackground());
        jsp.setCorner(ScrollPaneConstants.LOWER_LEFT_CORNER, lcorner);

        if (Platform.isMac()) {
            jsp.setBorder(BorderFactory.createEmptyBorder());
        }

        final JViewport view = jsp.getViewport();

        line.addMouseMotionListener(new MouseMotionAdapter() {

            @Override
            public void mouseDragged(MouseEvent e) {

                Point p = e.getPoint();
                Rectangle viewRect = view.getViewRect();

                p.y = viewRect.y;
                if (p.x < viewRect.x) {
                    p.x = Math.max(p.x, 0);
                    view.setViewPosition(p);
                } else if (p.x > viewRect.x + viewRect.width) {
                    p.x = Math.min(p.x, line.getWidth());
                    p.x -= viewRect.width;
                    view.setViewPosition(p);
                }
            }
        });

        JComboBox cb = new JComboBox();
        cb.setEditable(false);
        cb.setFont(GUI.getTinySystemFont());
        final int pps = line.getPixelsPerSecond();
        int[] sizes = new int[]{25, 50, 75, 100, 150, 200, 300, 400};
        for (int i = 0; i < sizes.length; i++) {
            final int percent = sizes[i];
            cb.addItem(new Object() {

                @Override
                public String toString() {

                    return String.valueOf(percent) + '%';
                }

                @Override
                public int hashCode() {

                    return pps * percent / 100;
                }
            });
        }
        cb.setSelectedIndex(3);
        cb.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {

                int newPPS = e.getItem().hashCode();
                int oldPPS = line.getPixelsPerSecond();
                int x = view.getViewPosition().x;
                line.setPixelsPerSecond(newPPS);
                x = line.translate(x, oldPPS);
                view.setViewPosition(new Point(x, view.getViewPosition().y));
            }
        });
        lcorner.add(cb);
        /*
     * jsp.setCorner(JScrollPane.LOWER_LEFT_CORNER, new CmdButton(new Runnable()
     * { public void run() { int oldPPS = line.getPixelsPerSecond(); int x =
     * view.getViewPosition().x;
     * line.setPixelsPerSecond(line.getPixelsPerSecond() % 100 + 10); x =
     * line.translate(x, oldPPS); view.setViewPosition(new Point(x,
     * view.getViewPosition().y)); } }, "Zoom"));
         */
        this.getContentPane().add(jsp, BorderLayout.SOUTH);

        this.pack();
    }

    @Override
    public void addNotify() {

        super.addNotify();

        this.document.addWindowListener(this.parentWindowListener);
    }

    @Override
    public void removeNotify() {

        this.document.removeWindowListener(this.parentWindowListener);

        super.removeNotify();
    }

    public void setInfo(String text) {

        this.info.setText(text);
        this.info.setCaretPosition(0);
    }

    private LogPlayer getLogPlayer() {

        return this.document.getDocument().getLogPlayer();
    }

    private void selectAndCenter(TreePath path) {

        this.callTree.setSelectionPath(path);
        this.callTree.makeVisible(path);
        Rectangle r = this.callTree.getPathBounds(path);
        if (r != null) {
            r.x = 0; // always keep left aligned
            GUI.scrollIntoCenter(r, this.callTreeScroller.getViewport());
        }
    }

    @Override
    protected ManagedFrame getMainWindow() {

        return this.document;
    }

}
