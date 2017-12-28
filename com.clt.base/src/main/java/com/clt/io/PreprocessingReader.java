/*
 * @(#)CPPReader.java
 * Created on 04.11.04
 *
 * Copyright (c) 2004 CLT Sprachtechnologie GmbH.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CLT Sprachtechnologie GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CLT Sprachtechnologie GmbH.
 */

package com.clt.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A Reader that interprets its input like the C preprocessor (aka CPP). Right
 * know, the only supported directive is <code>#include</code>.
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

public class PreprocessingReader
    extends Reader {

  private char[] buffer;
  private int position;
  private State root;
  private State current;
  private long totalPosition;


  public PreprocessingReader(File f)
      throws IOException {

    this.init();

    this.root = new State(null, this.totalPosition, f);
    this.current = this.root;
  }


  public PreprocessingReader(File f, String encoding)
      throws IOException {

    this.init();

    this.root = new State(null, this.totalPosition, f, encoding);
    this.current = this.root;
  }


  public PreprocessingReader(Reader main) {

    this.init();

    this.root = new State(null, this.totalPosition, main);
    this.current = this.root;
  }


  public static Set<File> getIncludedFiles(File f)
      throws IOException {

    return PreprocessingReader.getIncludedFiles(new PreprocessingReader(f),
      new HashSet<File>());
  }


  public static Set<File> getIncludedFiles(File f, String encoding)
      throws IOException {

    return PreprocessingReader.getIncludedFiles(new PreprocessingReader(f,
      encoding), new HashSet<File>());
  }


  private static Set<File> getIncludedFiles(PreprocessingReader pr,
      Set<File> files)
        throws IOException {

    BufferedReader r = new BufferedReader(pr);

    files.add(pr.current.getFile());
    while (r.readLine() != null) {
      files.add(pr.current.getFile());
    }

    r.close();
    return files;
  }


  private void init() {

    this.buffer = new char[0];
    this.position = 0;
    this.totalPosition = 0;
  }


  private File findFile(String path, boolean local)
      throws IOException {

    File f = new File(this.current.getFile().getParentFile(), path);
    if (!f.isFile()) {
      // System.err.println("Could not find file " + path + " included from "
      // + current.getFile());
      throw new FileNotFoundException(f.getPath());
    }
    else {
      return f;
    }
  }


  private void fillBuffer()
      throws IOException {

    String line = this.current.readLine();
    if (line != null) {
      if (line.trim().startsWith("#include")) {
        String inc = line.trim().substring("#include".length()).trim();
        boolean local = false;
        if (inc.startsWith("\"") && inc.endsWith("\"")) {
          local = true;
        }
        else if (inc.startsWith("<") && inc.endsWith(">")) {
          local = false;
        }
        else {
          throw new FileFormatException("Illegal #include directive in "
                          + (this.current.getFile() != null ? "file "
                            + this.current.getFile().getName()
                                  + ", " : "") + "line "
            + this.current.getCurrentLine());
        }
        inc = inc.substring(1, inc.length() - 1);
        File f = this.findFile(inc, local);

        State newState =
          new State(this.current, this.totalPosition, f, this.current
            .getEncoding());
        this.current.addChild(newState);
        this.current = newState;
        this.fillBuffer();
      }
      else {
        this.buffer = new char[line.length() + 1];
        line.getChars(0, line.length(), this.buffer, 0);
        this.buffer[this.buffer.length - 1] = '\n';
        this.position = 0;
        this.totalPosition += this.buffer.length;
        this.current.setEnd(this.totalPosition);
      }
    }
    else {
      if (this.current == this.root) {
        this.buffer = null;
      }
      else {
        this.current = this.current.getParent();
        this.fillBuffer();
      }
    }
  }


  @Override
  public int read(char cbuf[], int off, int len)
      throws IOException {

    int totalBytes = 0;
    while ((len > 0) && (this.buffer != null)) {
      int bytes = Math.min(len, this.buffer.length - this.position);
      System.arraycopy(this.buffer, this.position, cbuf, off, bytes);
      totalBytes += bytes;
      this.position += bytes;
      off += bytes;
      len -= bytes;
      if (len > 0) {
        this.fillBuffer();
      }
    }
    return totalBytes == 0 ? -1 : totalBytes;
  }


  @Override
  public void close()
      throws IOException {

    try {
      while (this.current != null) {
        this.current.close();
        this.current = this.current.getParent();
      }
    } finally {
      this.current = null;
    }
    this.buffer = null;
  }


  public File getFileForGlobalPosition(long pos) {

    return this.getState(this.root, pos).getFile();
  }


  /*
   * public int getLineForGlobalPosition(long pos) { return getState(root,
   * pos).getLine(); }
   */

  private State getState(State parent, long pos) {

    for (int i = 0; i < parent.getChildCount(); i++) {
      State child = parent.getChildAt(i);
      if ((pos >= child.getStart()) && (pos < child.getEnd())) {
        return this.getState(child, pos);
      }
    }
    return parent;
  }


  public String getCurrentLineStack() {

    if (this.current == null) {
      return "";
    }
    else {
      State state = this.current;

      StringWriter sw = new StringWriter();
      PrintWriter w = new PrintWriter(sw);
      w.print("in");
      if (state.getFile() != null) {
        w.print(" file " + state.getFile().getName() + ",");
      }
      w.print(" line " + state.getCurrentLine());
      state = state.getParent();
      while (state != null) {
        w.println();
        w.print("included");
        if (state.getFile() != null) {
          w.print(" from " + state.getFile().getName());
        }
        w.print(" at line " + state.getCurrentLine());
        state = state.getParent();
      }
      w.close();
      return sw.toString();
    }
  }


  @SuppressWarnings("unused")
  private void printHierarchy() {

    this.printHierarchy(System.out, this.root, 0);
  }


  private void printHierarchy(PrintStream out, State state, int indent) {

    for (int i = 0; i < indent; i++) {
      out.print("  ");
    }
    out.println(state.getFile() + " (" + state.getStart() + "-"
      + state.getEnd());
    for (int i = 0; i < state.getChildCount(); i++) {
      this.printHierarchy(out, state.getChildAt(i), indent + 1);
    }
  }

  private static class State {

    private State parent;

    private List<State> children = null;

    private BufferedReader reader;

    private File file;

    private String encoding = null;

    private int currentLine;

    private long start;
    private long end;


    public State(State parent, long start, File file)
            throws FileNotFoundException, UnsupportedEncodingException {

      this(parent, start, file, null);
      this.file = file;
      this.encoding = null;
    }


    public State(State parent, long start, File file, String encoding)
            throws FileNotFoundException, UnsupportedEncodingException {

      this(
        parent,
        start,
        encoding == null
          ? new InputStreamReader(new FileInputStream(file))
                    : new InputStreamReader(new FileInputStream(file), encoding));
      this.file = file;
      this.encoding = encoding;
    }


    public State(State parent, long start, Reader r) {

      if (r instanceof BufferedReader) {
        this.reader = (BufferedReader)r;
      }
      else {
        this.reader = new BufferedReader(r);
      }
      this.currentLine = 0;
      this.parent = parent;
      this.start = start;
    }


    public State getParent() {

      return this.parent;
    }


    public long getStart() {

      return this.start;
    }


    public long getEnd() {

      return this.end;
    }


    public void setEnd(long end) {

      this.end = end;
    }


    public int getCurrentLine() {

      return this.currentLine;
    }


    public String readLine()
        throws IOException {

      ++this.currentLine;
      return this.reader.readLine();
    }


    public File getFile() {

      return this.file;
    }


    public String getEncoding() {

      return this.encoding;
    }


    public int getChildCount() {

      return this.children == null ? 0 : this.children.size();
    }


    public State getChildAt(int index) {

      return this.children.get(index);
    }


    public void addChild(State child) {

      if (this.children == null) {
        this.children = new ArrayList<State>();
      }
      this.children.add(child);
    }


    public void close()
        throws IOException {

      this.reader.close();
    }
  }
}
