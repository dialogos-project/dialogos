package com.clt.diamant.graph.search;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.RootPaneContainer;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.clt.diamant.Resources;
import com.clt.diamant.graph.ui.GraphUI;
import com.clt.gui.GUI;
import com.clt.gui.menus.CmdMenu;
import com.clt.gui.menus.CmdMenuBar;
import com.clt.gui.menus.CmdMenuItem;
import com.clt.gui.menus.MenuCommander;

/**
 * Dialog displaying the result of a node search.
 *
 * @author Daniel Bobbert
 * @version 1.0
 */
public class SearchResultsDialog extends JPanel {

    /**
     * Serial Version Id.
     */
    private static final long serialVersionUID = 1L;
    private DefaultListModel resultListModel;
    private JLabel resultHeader;
    private final JList resultList;

    private GraphUI currentGraphView = null;

    public static void show(Component parent, String title, Collection<? extends SearchResult> searchResults) {

        SearchResultsDialog.show(parent, title, searchResults.toArray(new SearchResult[searchResults.size()]));
    }

    public static void show(Component parent, String title, SearchResult searchResults[]) {

        final Window w;

        if (parent != null) {
            JDialog window
                    = new JDialog(GUI.getFrameForComponent(parent), title, true);
            window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

            w = window;
        } else {
            JFrame window = new JFrame(title);
            window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

            w = window;
        }

        RootPaneContainer rpc = (RootPaneContainer) w;
        final SearchResultsDialog d
                = new SearchResultsDialog(searchResults, parent != null);
        rpc.setContentPane(d);

        CmdMenuBar mbar = new CmdMenuBar(new MenuCommander() {

            public String menuItemName(int cmd, String oldName) {

                if (d.currentGraphView != null) {
                    return d.currentGraphView.menuItemName(cmd, oldName);
                } else {
                    return oldName;
                }
            }

            public boolean menuItemState(int cmd) {

                if (d.currentGraphView != null) {
                    return d.currentGraphView.menuItemState(cmd);
                } else {
                    return false;
                }
            }

            public boolean doCommand(int cmd) {

                if (d.currentGraphView != null) {
                    return d.currentGraphView.doCommand(cmd);
                } else {
                    return false;
                }
            }
        });
        CmdMenu menu = mbar.addMenu(Resources.getString("FileMenu"));
        menu.add(new CmdMenuItem(Resources.getString("Close"), KeyEvent.VK_W,
                new Runnable() {

            public void run() {

                w.dispose();
                // w.dispatchEvent(new WindowEvent(w,
                // WindowEvent.WINDOW_CLOSING));
            }
        }));
        GraphUI.initMenu(menu);

        rpc.getLayeredPane().add(mbar);

        w.pack();
        w.setSize(600, 400);
        w.setLocation(30, 30);

        w.addWindowListener(new WindowAdapter() {

            @Override
            public void windowActivated(WindowEvent evt) {

                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {

                        d.updateResults();
                    }
                });
            }

            @Override
            public void windowOpened(WindowEvent e) {

                d.selectFirst();
            }
        });

        w.setVisible(true);
    }

    public SearchResultsDialog(SearchResult searchResults[], boolean modal) {

        this(searchResults, modal, true);
    }

    public SearchResultsDialog(SearchResult searchResults[], boolean modal,
            boolean showGraphInline) {

        this.setLayout(new BorderLayout());

        this.resultListModel = new DefaultListModel();
        for (int i = 0; i < searchResults.length; i++) {
            searchResults[i].addNotify();
            this.resultListModel.addElement(searchResults[i]);
        }

        this.resultList = new JList(this.resultListModel);
        this.resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.resultList.setCellRenderer(new ListCellRenderer() {

            public Component getListCellRendererComponent(JList list, Object value,
                    int index,
                    boolean isSelected, boolean cellHasFocus) {

                SearchResult c = (SearchResult) value;
                c.setSelected(isSelected, index % 2 == 0);
                return c;
            }
        });

        JScrollPane jsp
                = new JScrollPane(this.resultList,
                        ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        JPanel results = new JPanel(new BorderLayout(6, 6));
        results.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        this.resultHeader = new JLabel();
        results.add(this.resultHeader, BorderLayout.NORTH);

        results.add(jsp, BorderLayout.CENTER);

        if (showGraphInline || modal) {
            if (this.resultListModel.size() > 0) {
                int width = this.resultList.getMinimumSize().width;
                int height
                        = ((SearchResult) this.resultListModel.firstElement())
                                .getPreferredSize().height * 2;
                int borderHeight = 0;
                Border border = this.resultList.getBorder();
                if (border != null) {
                    Insets insets = border.getBorderInsets(this.resultList);
                    borderHeight += insets.top + insets.bottom;
                }
                border = jsp.getBorder();
                if (border != null) {
                    Insets insets = border.getBorderInsets(this.resultList);
                    borderHeight += insets.top + insets.bottom;
                }
                jsp.setMinimumSize(new Dimension(width, height + borderHeight));
                jsp.setPreferredSize(new Dimension(width, height + borderHeight));
            }

            final JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
            split.setTopComponent(results);

            final JPanel graphView = new JPanel(new GridLayout(1, 1));
            // graphView.setPreferredSize(new
            // Dimension(jsp.getPreferredSize().width, 300));
            split.setBottomComponent(graphView);
            /*
       * JPanel p = new JPanel(new BorderLayout()); p.add(new
       * WindowHeader("akshkjakda", WindowHeader.LEFT), BorderLayout.NORTH);
       * p.add(graphView, BorderLayout.CENTER); split.setBottomComponent(p);
             */
            this.resultList.addListSelectionListener(new ListSelectionListener() {

                public void valueChanged(ListSelectionEvent evt) {

                    if (!evt.getValueIsAdjusting()) {
                        SearchResult info
                                = (SearchResult) SearchResultsDialog.this.resultList
                                        .getSelectedValue();
                        if (info != null) {
                            SearchResultsDialog.this.currentGraphView
                                    = info.showResult(graphView);
                        } else {
                            graphView.removeAll();
                            graphView.revalidate();
                            graphView.repaint();
                            SearchResultsDialog.this.currentGraphView = null;
                        }
                    }
                }
            });
            this.add(split);
        } else {
            this.add(results);
        }

        if (!modal) {
            this.resultList.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent evt) {

                    if (evt.getClickCount() == 2) {
                        int index
                                = SearchResultsDialog.this.resultList.locationToIndex(evt
                                        .getPoint());
                        if ((index >= 0)
                                || (index < SearchResultsDialog.this.resultList.getModel()
                                        .getSize())) {
                            ((SearchResult) SearchResultsDialog.this.resultList.getModel()
                                    .getElementAt(index))
                                    .showResult(null);
                        }
                    }
                }
            });
        }

        this.updateResults();
    }

    private void selectFirst() {

        if (this.resultList.getModel().getSize() > 0) {
            this.resultList.setSelectedIndex(0);
        }
    }

    public void updateResults() {

        for (int i = this.resultListModel.size() - 1; i >= 0; i--) {
            SearchResult result = (SearchResult) this.resultListModel.get(i);
            if (!result.isRelevant()) {
                this.resultListModel.removeElementAt(i);
            }
        }
        this.resultHeader.setText(this.resultListModel.size() == 1 ? Resources
                .getString("FoundOneNode")
                : Resources.format("FoundNNodes", this.resultListModel.size()));
    }
}
