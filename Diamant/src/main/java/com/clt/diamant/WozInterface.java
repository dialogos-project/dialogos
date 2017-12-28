package com.clt.diamant;

import java.util.Collection;

import javax.swing.JLayeredPane;

import com.clt.diamant.graph.GraphExecutionListener;
import com.clt.diamant.graph.Node;
import com.clt.diamant.graph.nodes.OwnerNode;
import com.clt.script.debug.Debugger;
import com.clt.script.exp.Pattern;
import com.clt.script.exp.Value;

public interface WozInterface
    extends Debugger {

  public enum State {
        NORMAL("idle"),
        INSTRUCT("instruct"),
        RUN("run"),
        END("finish");

    private String name;


    State(String name) {

      this.name = name;
    }


    public String getName() {

      return this.name;
    }


    @Override
    public String toString() {

      return this.name;
    }
  }


  public boolean initInterface();


  public void disposeInterface(boolean error);


  public void startDocument(Document d, String name, InputCenter input);


  public void endDocument(Document d);


  public void output(Device d, Value value);


  public void discardOldInput(Device d);


  public DialogInput<?> getInput(Pattern[] alternatives, Device d,
      Collection<Device> allDevices,
            Collection<Device> waitDevices, long timeout, boolean forceTimeout);


  public void transition(Node source, Node destination, int index,
      String condition);


  public void subgraph(OwnerNode owner, boolean enter);


  public void error(String type, String message);


  public String getName();


  public void setState(State state);


  public void abort();


  public boolean isDebugger();


  public boolean showSubdialogsDuringExecution();


  public void addGraphExecutionListener(GraphExecutionListener l);


  public void removeGraphExecutionListener(GraphExecutionListener l);


  public void setDelay(long delay);


  public long getDelay();


  public void preExecute(Node node);


  public JLayeredPane getLayeredPane();
}