package com.clt.diamant.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import com.clt.diamant.Resources;
import com.clt.diamant.Slot;
import com.clt.diamant.graph.GraphOwner;
import com.clt.gui.OptionPane;
import com.clt.script.DefaultEnvironment;
import com.clt.script.exp.EvaluationException;
import com.clt.script.exp.Expression;
import com.clt.script.exp.Type;
import com.clt.script.exp.Value;
import com.clt.script.exp.Variable;

class VarList
    implements ChangeListener {

  private List<Slot> elems = new ArrayList<Slot>();
  private Stack<GraphOwner> owners = new Stack<GraphOwner>();
  private Stack<List<Slot>> history = new Stack<List<Slot>>();
  private List<JTable> displays = new ArrayList<JTable>();


  private void addDisplay(JTable t) {

    this.displays.add(t);
  }


  private void add(Slot s) {

    this.elems.add(s);
    s.addChangeListener(this);
  }


  public synchronized void push(GraphOwner owner, List<Slot> vars) {

    this.history.push(this.elems);
    this.owners.push(owner);
    this.elems = new ArrayList<Slot>();
    for (int i = 0; i < vars.size(); i++) {
      this.add(vars.get(i));
    }
    this.stateChanged(new ChangeEvent(this));
  }


  public synchronized void pop() {

    for (int i = this.elems.size() - 1; i >= 0; i--) {
      Slot s = this.elems.remove(i);
      s.removeChangeListener(this);
    }
    this.elems = this.history.pop();
    this.owners.pop();
    this.stateChanged(new ChangeEvent(this));
  }


  public synchronized void clear() {

    while (!this.history.isEmpty()) {
      this.pop();
    }
  }


  public synchronized int size() {

    return this.elems.size();
  }


  public synchronized Slot get(int index) {

    return this.elems.get(index);
  }


  public synchronized GraphOwner getOwner() {

    return this.owners.peek();
  }


  public synchronized void stateChanged(ChangeEvent evt) {

    for (JTable t : this.displays) {
      TableModelEvent tevt = new TableModelEvent(t.getModel());
      t.tableChanged(tevt);
    }
  }


  public JTable createVariableDisplay() {

    final JTable table = new JTable();
    table.setModel(new AbstractTableModel() {

      public int getRowCount() {

        return VarList.this.size();
      }


      public int getColumnCount() {

        return 2;
      }


      @Override
      public String getColumnName(int column) {

        switch (column) {
          case 0:
                        return Resources.getString("Name");
                      case 1:
                        return Resources.getString("Value");
                      default:
                        throw new IllegalArgumentException("invalid column");
                    }
                  }


      @Override
      public Class<?> getColumnClass(int column) {

        switch (column) {
          case 0:
                        return String.class;
                      case 1:
                        return String.class;
                      default:
                        throw new IllegalArgumentException("invalid column");
                    }
                  }


      @Override
      public boolean isCellEditable(int row, int column) {

        return column == 1;
      }


      public Object getValueAt(int row, int column) {

        Slot s = VarList.this.get(row);
        switch (column) {
          case 0:
                        return s.getName();
                      case 1:
                        return s.getValue().toString();
                      default:
                        throw new IllegalArgumentException("invalid column");
                    }
                  }


      @Override
      public void setValueAt(Object aValue, int row, int column) {

        Slot s = VarList.this.get(row);
        switch (column) {
          case 1:
                        try {
                          s.setValue(Expression.parseExpression(
                            aValue.toString(),
                                new DefaultEnvironment() {

                                  @Override
                                  public Variable createVariableReference(
                                      final String id) {

                                    return new Variable() {

                                      private Slot getSlot() {

                                        for (int i = VarList.this.size() - 1; i >= 0; i--) {
                                          if (VarList.this.get(i).getName()
                                            .equals(id)) {
                                            return VarList.this.get(i);
                                          }
                                        }
                                        throw new EvaluationException(
                                          "Unknown variable "
                                                        + id);
                                      }


                                      public Type getType() {

                                        return this.getSlot().getType();
                                      }


                                      public Value getValue() {

                                        return this.getSlot().getValue();
                                      }


                                      public void setValue(Value value) {

                                        this.getSlot().setValue(value);
                                      }


                                      public String getName() {

                                        return id;
                                      }
                                    };
                                  }
                                }).evaluate());
                        }
                        catch (Exception exn) {
                          OptionPane.error(table, exn);
                        }
                        break;
                      default:
                        throw new IllegalArgumentException("invalid column");
                    }
                  }
    });

    this.addDisplay(table);
    return table;
  }


  public JTable createCallstackDisplay() {

    final JTable table = new JTable();
    table.setModel(new AbstractTableModel() {

      public int getRowCount() {

        return VarList.this.owners.size();
      }


      public int getColumnCount() {

        return 1;
      }


      @Override
      public String getColumnName(int column) {

        switch (column) {
          case 0:
                        return Resources.getString("Callstack");
                      default:
                        throw new IllegalArgumentException("invalid column");
                    }
                  }


      @Override
      public Class<?> getColumnClass(int column) {

        switch (column) {
          case 0:
                        return String.class;
                      default:
                        throw new IllegalArgumentException("invalid column");
                    }
                  }


      @Override
      public boolean isCellEditable(int row, int column) {

        return false;
      }


      private GraphOwner getOwner(int row) {

        return VarList.this.owners.get(VarList.this.owners.size() - 1 - row);
      }


      public Object getValueAt(int row, int column) {

        return this.getOwner(row).getGraphName();
      }


      @Override
      public void setValueAt(Object aValue, int row, int column) {

      }
    });

    this.addDisplay(table);
    return table;
  }

}