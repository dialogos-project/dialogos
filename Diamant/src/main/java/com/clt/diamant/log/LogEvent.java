package com.clt.diamant.log;

public class LogEvent<T> {

  private int time;
  private String type;
  private T[] arguments;


  public LogEvent(String type, int time, T[] args) {

    this.type = type;
    this.time = time;
    this.arguments = args;
  }


  /*
   * public LogEvent(String type, int time, int numArgs) { this(type, time, new
   * T[numArgs]); }
   */

  public LogEvent(String type, int time) {

    this(type, time, null);
  }


  public void setArgument(int i, T value) {

    this.arguments[i] = value;
  }


  public T getArgument(int index) {

    return this.arguments[index];
  }


  public int getTime() {

    return this.time;
  }


  public String getType() {

    return this.type;
  }


  public String getArgs() {

    if (this.arguments == null) {
      return "";
    }
    else {
      StringBuilder b = new StringBuilder();
      for (int i = 0; i < this.arguments.length; i++) {
        if (i > 0) {
          b.append(' ');
        }
        b.append(this.arguments[i]);
      }
      return b.toString();
    }
  }
}