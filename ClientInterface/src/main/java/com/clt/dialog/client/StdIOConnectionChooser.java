package com.clt.dialog.client;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import com.clt.util.LongAction;

public class StdIOConnectionChooser
    implements ConnectionChooser {

  BufferedReader in;
  PrintWriter out;


  public StdIOConnectionChooser() {

    this(System.in, System.out);
  }


  public StdIOConnectionChooser(InputStream in, OutputStream out) {

    this(new InputStreamReader(in), new OutputStreamWriter(out));
  }


  public StdIOConnectionChooser(Reader in, Writer out) {

    if (in instanceof BufferedReader) {
      this.in = (BufferedReader)in;
    }
    else {
      this.in = new BufferedReader(in);
    }

    if (out instanceof PrintWriter) {
      this.out = (PrintWriter)out;
    }
    else {
      this.out = new PrintWriter(out);
    }
  }


  public synchronized void init(ServerDevice[] device) {

    for (int i = 0; i < device.length; i++) {
      this.out.println("Connecting " + device[i].getName() + "...");
    }
  }


  public void start(LongAction action)
      throws InvocationTargetException {

    try {
      action.run();
    } catch (Throwable exn) {
      throw new InvocationTargetException(exn);
    }
  }


  public synchronized boolean resolve(Map<ServerDevice, Object[]> ambiguities,
            Map<ServerDevice, Object> defaults) {

    for (final ServerDevice d : ambiguities.keySet()) {
      Object[] targets = ambiguities.get(d);
      Object def = defaults.get(d);
      int def_index = -1;
      this.out.println("Which version of \"" + d.getName()
        + "\" do you want to use?");
      for (int i = 0; i < targets.length; i++) {
        this.out.println(String.valueOf(i + 1) + ". "
          + String.valueOf(targets[i]));
        if (targets[i] == def) {
          def_index = i;
        }
      }
      int choice = -1;
      while (choice < 0) {
        this.out.println("("
                        + (def_index >= 0 ? "Default = "
                          + String.valueOf(def_index + 1) + ", "
                                : "") + "press 0 to cancel)");
        try {
          String s = this.in.readLine();
          int n = Integer.parseInt(s);
          if (n == 0) {
            return false;
          }
          else if ((n >= 1) && (n < targets.length)) {
            choice = n;
          }
        } catch (Exception ignore) {
        }
      }
      ambiguities.put(d, new Object[] { targets[choice] });
    }

    return true;
  }


  public synchronized void stateChanged(ServerDevice d) {

  }


  public synchronized void protocolChanged(ServerDevice d, String protocol) {

  }


  public synchronized void ambiguityDetected(ServerDevice d,
            Map<ServerDevice, Object[]> ambiguities) {

  }

}