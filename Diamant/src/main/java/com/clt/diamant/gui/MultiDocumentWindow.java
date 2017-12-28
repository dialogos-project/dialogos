//
//  MultiDocument.java
//  Wizard
//
//  Created by Daniel Bobbert on Fri Jun 14 2002.
//  Copyright (c) 2002 CLT Sprachtechnologie GmbH. All rights reserved.
//

package com.clt.diamant.gui;

import java.awt.Container;
import java.awt.Event;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.clt.dialog.client.ConnectDialog;
import com.clt.diamant.Document;
import com.clt.diamant.ExecutionStoppedException;
import com.clt.diamant.MenuUtils;
import com.clt.diamant.MultiDocument;
import com.clt.diamant.Preferences;
import com.clt.diamant.Resources;
import com.clt.diamant.SingleDocument;
import com.clt.diamant.Version;
import com.clt.diamant.WozInterface;
import com.clt.diamant.graph.search.SearchResult;
import com.clt.diamant.graph.search.SearchResultsDialog;
import com.clt.event.ProgressEvent;
import com.clt.event.ProgressListener;
import com.clt.gui.Commands;
import com.clt.gui.FileChooser;
import com.clt.gui.GUI;
import com.clt.gui.OptionPane;
import com.clt.gui.ProgressDialog;
import com.clt.gui.menus.AbstractMenuCommander;
import com.clt.gui.menus.CmdMenu;
import com.clt.gui.menus.CmdMenuBar;
import com.clt.gui.menus.MenuCommander;
import com.clt.gui.table.TextRenderer;
import com.clt.mac.RequiredEventHandler;
import com.clt.util.AbstractLongAction;
import com.clt.util.Misc;
import com.clt.xml.AbstractHandler;
import com.clt.xml.XMLReader;

public class MultiDocumentWindow
    extends DocumentWindow<MultiDocument> {

  private static final int cmdAdd = Commands.cmdDocument + 1,
      cmdRemove = Commands.cmdDocument + 2,
            cmdShuffle = Commands.cmdDocument + 3,
      cmdValidate = Commands.cmdDocument + 4,
      cmdWoz = Commands.cmdDocument + 5;

  private JTable table;


  public MultiDocumentWindow(MultiDocument d, MenuCommander superCommander,
                               RequiredEventHandler systemEventHandler) {

    super(d);

    Container c = this.getContentPane();

    this.table = new JTable(new DocumentsTableModel());
    this.table.getTableHeader().setReorderingAllowed(false);
    this.table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    this.table.getSelectionModel().addListSelectionListener(
      new ListSelectionListener() {

        public void valueChanged(ListSelectionEvent e) {

          if (!e.getValueIsAdjusting()) {
            MultiDocumentWindow.this.updateMenus();
          }
        }
      });

    this.getDocument().addPropertyChangeListener(new PropertyChangeListener() {

      public void propertyChange(PropertyChangeEvent evt) {

        if (evt.getPropertyName().equals("documents")) {
          ((DocumentsTableModel)MultiDocumentWindow.this.table.getModel())
            .fireTableDataChanged();
        }
      }
    });

    TableColumn name = this.table.getColumnModel().getColumn(0);
    name.setResizable(false);
    name.setMinWidth(120);
    name.setCellRenderer(new TextRenderer());

    JScrollPane jsp = GUI.createScrollPane(this.table, 200);

    jsp.setBorder(BorderFactory.createEmptyBorder());
    GUI.setupScrollBar(jsp.getVerticalScrollBar());
    GUI.setupScrollBar(jsp.getHorizontalScrollBar());

    c.add(jsp);

    this.setupMenus(this.initMenus(superCommander, systemEventHandler));

    this.finishSetup();
  }


  private Collection<CmdMenuBar> initMenus(MenuCommander commander,
            RequiredEventHandler systemEventHandler) {

    Collection<CmdMenuBar> mbars = new ArrayList<CmdMenuBar>();

    CmdMenuBar mbar = new CmdMenuBar(commander);
    MenuUtils.addFileMenu(mbar, systemEventHandler);
    MenuUtils.addEditMenu(mbar);

    CmdMenu m = mbar.addMenu(Resources.getString("Experiment"));

    m.addItem(Resources.getString("Add") + "...", MultiDocumentWindow.cmdAdd);
    m.addItem(Resources.getString("Remove"), MultiDocumentWindow.cmdRemove);
    m.addSeparator();
    m.addItem(Resources.getString("Shuffle"), MultiDocumentWindow.cmdShuffle);
    m.addItem(Resources.getString("Validate"), MultiDocumentWindow.cmdValidate,
      KeyEvent.VK_K);
    m.addSeparator();
    m.addItem(Resources.getString("Woz"), MultiDocumentWindow.cmdWoz, KeyStroke
      .getKeyStroke(KeyEvent.VK_R,
            (m.getToolkit().getMenuShortcutKeyMask() | Event.SHIFT_MASK)));

    mbar.add(m);

    mbar.add(this.getWindowMenu());
    this.setJMenuBar(mbar);
    mbars.add(mbar);

    CmdMenuBar hiddenMenuBar = new CmdMenuBar(commander);
    CmdMenu hiddenMenu = hiddenMenuBar.addMenu("");
    hiddenMenu.addItem("Clear", Commands.cmdDelete, KeyStroke.getKeyStroke(
            KeyEvent.VK_BACK_SPACE, 0));
    hiddenMenu.addItem("Clear", Commands.cmdDelete, KeyStroke.getKeyStroke(
      KeyEvent.VK_DELETE,
            0));

    this.getLayeredPane().add(hiddenMenuBar);
    mbars.add(hiddenMenuBar);

    return mbars;
  }


  @Override
  public boolean doCommand(int cmd) {

    List<File> documents = this.getDocuments();

    boolean cmdHandled = true;
    switch (cmd) {
      case cmdAdd:
        try {
          File f = new FileChooser().standardGetFile(this);
          if (f != null) {
            documents.add(f);
            ((AbstractTableModel)this.table.getModel()).fireTableRowsInserted(
                            documents.size() - 1, documents.size() - 1);
            this.table.setRowSelectionInterval(this.table.getRowCount() - 1,
                            this.table.getRowCount() - 1);
            this.table.requestFocus();
            this.setDirty(true);
          }
        } catch (Exception exn) {
        }
        break;

      case cmdRemove:
      case Commands.cmdDelete:
        if (this.table.getSelectedRow() >= 0) {
          Misc.removeElements(documents, this.table.getSelectedRows());

          this.table.clearSelection();
          ((AbstractTableModel)this.table.getModel()).fireTableDataChanged();
          this.table.requestFocus();
          this.setDirty(true);
        }
        break;

      case cmdShuffle:
        if (documents.size() > 1) {
          Random random = new Random();
          File[] docs = new File[documents.size()];
          for (int i = documents.size(); i > 0; i--) {
            int offset = Math.abs(random.nextInt() % i);
            docs[i - 1] = documents.get(offset);
            documents.remove(offset);
          }
          for (int i = 0; i < docs.length; i++) {
            documents.add(docs[i]);
          }
          ((AbstractTableModel)this.table.getModel()).fireTableDataChanged();
          this.table.requestFocus();
          this.setDirty(true);
        }
        break;

      case cmdValidate:
        Collection<SearchResult> errors = new LinkedList<SearchResult>();
        this.validateDocuments(errors);
        if (errors.size() > 0) {
          SearchResultsDialog.show(this, Resources
            .getString("DocumentProblems"), errors);
        }
        else {
          OptionPane.message(this, Resources.getString("DocumentValid"));
        }
        break;

      case cmdWoz:
        new Thread(new Runnable() {

          public void run() {

            MultiDocumentWindow.this.run();
          }
        }, "MultiDocument Woz").start();
        break;

      default:
        cmdHandled = super.doCommand(cmd);
        break;
    }
    return cmdHandled;
  }


  @Override
  public boolean menuItemState(int cmd) {

    List<File> documents = this.getDocuments();

    switch (cmd) {
      case cmdAdd:
        return true;

      case cmdRemove:
      case Commands.cmdDelete:
        return this.table.getSelectedRow() >= 0;

      case cmdShuffle:
        return documents.size() > 1;

      case cmdValidate:
        return true;

      case cmdWoz:
        return documents.size() > 1;

      default:
        return super.menuItemState(cmd);

    }
  }


  @Override
  public String menuItemName(int cmd, String old_name) {

    return super.menuItemName(cmd, old_name);
  }


  @Override
  public void updateMenus() {

    super.updateMenus();
  }


  public void run() {

    WozInterface transition;

    try {
      transition = new WizardUI(this, true);
    } catch (Exception exn) {
      OptionPane.error(this, new String[] {
        "A necessary resource could not be loaded.",
                    "Please reinstall " + Version.PRODUCT_NAME });
      return;
    }
    Object message = null;
    int type = OptionPane.INFORMATION;
    Container c = this.getContentPane();

    try {
      if (transition.initInterface()) {
        List<File> documents = this.getDocuments();

        for (int i = 0; i < documents.size(); i++) {
          SingleDocument d = this.loadDocument(documents.get(i));
          SingleDocumentWindow<SingleDocument> dw =
            new SingleDocumentWindow<SingleDocument>(
                        d, new AbstractMenuCommander(), null, false);

          this.setContentPane(dw.getContentPane());
          ComponentListener l = new ComponentAdapter() {

            @Override
            public void componentShown(ComponentEvent e) {

              if (e.getComponent() instanceof Container) {
                MultiDocumentWindow.this.setContentPane((Container)e
                  .getComponent());
              }
              else {
                JPanel p = new JPanel();
                p.add(e.getComponent());
                MultiDocumentWindow.this.setContentPane(p);
              }
            }
          };
          dw.addViewListener(l);
          if (d.connectDevices(new ConnectDialog(this),
                        Preferences.getPrefs().getConnectionTimeout())) {
            boolean again;
            try {
              do {
                again = false;
                try {
                  d.execute(dw, transition);
                } catch (ExecutionStoppedException exn) {
                  int result =
                    OptionPane
                      .showOptionDialog(
                        this,
                        new String[] {
                                            Resources
                                              .getString("ExecutionStopped"),
                                            Resources
                                              .getString("RepeatContinueOrCancel") },
                                        Resources.getString("ExecutionStopped"),
                        OptionPane.ERROR,
                                        OptionPane.YES_NO_CANCEL_OPTION,
                        null,
                        new String[] {
                                                Resources.getString("Repeat"),
                                                Resources.getString("Continue"),
                                                Resources.getString("Stop") },
                        null);
                  if (result == OptionPane.YES) {
                    again = true;
                  }
                  else if (result == OptionPane.NO) {
                    // do nothing just continue
                  }
                  else {
                    throw new ExecutionStoppedException();
                  }
                } catch (Exception exn) {
                  int result =
                    OptionPane
                      .showOptionDialog(
                        this,
                        new String[] {
                                            Resources
                                              .getString("ExecutionError"),
                                            Resources
                                              .getString("ContinueOrCancel") },
                                        Resources.getString("Error"),
                        OptionPane.ERROR,
                                        OptionPane.YES_NO_OPTION,
                        null,
                        new String[] {
                                                Resources.getString("Continue"),
                                                Resources.getString("Stop") },
                        null);
                  if (result != OptionPane.YES) {
                    throw new ExecutionStoppedException();
                  }
                }
              } while (again);
            } finally {
              d.closeDevices();
              dw.removeViewListener(l);
            }
          }
        }

        message = Resources.getString("ExecutionComplete");
        type = OptionPane.INFORMATION;
      }
      else {
        message = Resources.format("TransitionInitError", transition.getName());
        type = OptionPane.ERROR;
      }
    } catch (ExecutionStoppedException exn) {
      message = Resources.getString("ExecutionStopped");
      type = OptionPane.INFORMATION;
    } catch (Exception exn) {
      message =
        new String[] { Resources.getString("ExecutionError"), exn.toString() };
      type = OptionPane.ERROR;
    }

    this.setContentPane(c);

    transition.disposeInterface(type == OptionPane.ERROR);
    OptionPane.message(null, message,
            type == OptionPane.INFORMATION ? Resources.getString("Message")
                    : Resources.getString("Error"), type);
  }


  @Override
  public void setContentPane(Container c) {

    super.setContentPane(c);
    this.validate();
    this.repaint();
  }


  private SingleDocument loadDocument(final File f)
      throws IOException {

    final SingleDocument d = new SingleDocument();

    final XMLReader r = new XMLReader(Document.validateXML);
    r.parse(f, new AbstractHandler() {

      @Override
      public void start(String name, Attributes atts)
          throws SAXException {

        if (name.equals("wizard")) {
          d.load(f, r);
        }
        else {
          r.raiseException(Resources.getString("NotWizardDocument"));
        }
      }
    });

    d.setDirty(false);
    return d;
  }


  public List<File> getDocuments() {

    return this.getDocument().getDocuments();
  }


  public void validateDocuments(final Collection<SearchResult> errors) {

    if (this.getDocuments().size() > 0) {
      try {
        new ProgressDialog(this).run(new AbstractLongAction() {

          boolean canceled = false;


          @Override
          public void cancel() {

            this.canceled = true;
          }


          @Override
          public boolean canCancel() {

            return !this.canceled;
          }


          @Override
          public String getDescription() {

            return Resources.getString("ValidatingDocuments") + "...";
          }


          @Override
          protected void run(ProgressListener l) {

            List<File> documents = MultiDocumentWindow.this.getDocuments();
            ProgressEvent evt = new ProgressEvent(MultiDocumentWindow.this,
                            this.getDescription(), 0, documents.size() * 2, 0);
            for (int i = 0; (i < documents.size()) && !this.canceled; i++) {
              File f = documents.get(i);
              evt.setMessage(Resources.format("Reading", f.getName()) + "...");
              l.progressChanged(evt);
              try {
                SingleDocument d = MultiDocumentWindow.this.loadDocument(f);
                evt.setCurrent(i * 2 + 1);
                evt.setMessage(Resources.format("Validating", f.getName())
                  + "...");
                l.progressChanged(evt);

                d.validate(errors, l);
              }
                            catch (IOException exn) {
                              OptionPane.error(MultiDocumentWindow.this, exn);
                            }
                            evt.setCurrent((i + 1) * 2);
                            l.progressChanged(evt);
                          }
                        }
        });
      } catch (InvocationTargetException exn) {
        OptionPane.error(this, exn.getTargetException());
      }
    }
  }

  class DocumentsTableModel
        extends AbstractTableModel {

    public int getRowCount() {

      if (MultiDocumentWindow.this.getDocuments() != null) {
        return MultiDocumentWindow.this.getDocuments().size();
      }
      return 0;
    }


    public int getColumnCount() {

      return 1;
    }


    @Override
    public String getColumnName(int columnIndex) {

      switch (columnIndex) {
        case 0:
          return Resources.getString("Document");
      }
      return null;
    }


    @Override
    public Class<?> getColumnClass(int columnIndex) {

      switch (columnIndex) {
        case 0:
          return String.class;
      }
      return Object.class;
    }


    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {

      return false;
    }


    public Object getValueAt(int rowIndex, int columnIndex) {

      if ((MultiDocumentWindow.this.getDocuments() == null) || (rowIndex < 0)
        || (rowIndex >= MultiDocumentWindow.this.getDocuments().size())) {
        return null;
      }

      return MultiDocumentWindow.this.getDocuments().get(rowIndex).toString();
    }


    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

    }
  }

}
