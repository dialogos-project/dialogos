package com.clt.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

/**
 * A PrintWriter that knows how to indent new lines.
 *
 * @author dabo
 */
public class IndentedWriter extends PrintWriter {

    private String lineSeparator = "\r\n";

    private boolean indent = true;
    private int level = 0;

    private boolean newline = true;

    public IndentedWriter(File file, String csn) throws FileNotFoundException, UnsupportedEncodingException {

        super(file, csn);
    }

    public IndentedWriter(File file) throws FileNotFoundException {

        super(file);
    }

    public IndentedWriter(OutputStream out, boolean autoFlush) {

        super(out, autoFlush);
    }

    public IndentedWriter(OutputStream out) {

        super(out);
    }

    public IndentedWriter(String fileName, String csn) throws FileNotFoundException, UnsupportedEncodingException {

        super(fileName, csn);
    }

    public IndentedWriter(String fileName) throws FileNotFoundException {

        super(fileName);
    }

    public IndentedWriter(Writer out, boolean autoFlush) {

        super(out, autoFlush);
    }

    public IndentedWriter(Writer out) {

        super(out);
    }

    public String getLineSeparator() {

        return this.lineSeparator;
    }

    public void setLineSeparator(String lineSeparator) {

        this.lineSeparator = lineSeparator;
    }

    public void setIndent(boolean indent) {

        this.indent = indent;
    }

    /**
     * Increase the indent for all following lines.
     */
    public void indent() {

        this.level++;
    }

    /**
     * Decrease the indent for all following lines.
     */
    public void unindent() {

        this.level--;
    }

    private void writeIndent() {

        if (this.newline) {
            this.newline = false;
            if (this.indent) {
                for (int i = this.level; i > 0; i--) {
                    this.print('\t');
                }
            }
        }
    }

    @Override
    public void println() {

        this.write(this.lineSeparator);
        this.newline = true;
    }

    @Override
    public void write(String s) {

        this.writeIndent();
        super.write(s);
    }

    @Override
    public void write(String s, int off, int len) {

        this.writeIndent();
        super.write(s, off, len);
    }

    @Override
    public void write(char buf[]) {

        this.writeIndent();
        super.write(buf);
    }

    @Override
    public void write(char buf[], int off, int len) {

        this.writeIndent();
        super.write(buf, off, len);
    }

    @Override
    public void write(int c) {

        this.writeIndent();
        super.write(c);
    }
}
