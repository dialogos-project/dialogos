/*
 * @(#)SyntaxColor.java
 * Created on Tue Jun 22 2004
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

package com.clt.script.parser;

import java.awt.Color;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * 
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

public abstract class SyntaxColor {

  public static final int DEFAULT = 0, STRING = 1, CONSTANT = 2, COMMENT = 3,
      KEYWORD = 4,
            ERROR = 5,

            NUM_STYLES = 6;


  public static String getName(int style) {

    switch (style) {
      case STRING:
        return "string";
      case CONSTANT:
        return "constant";
      case COMMENT:
        return "comment";
      case KEYWORD:
        return "keyword";
      case ERROR:
        return "error";
      default:
        return "default";
    }
  }


  public abstract Color getColor(int style);

  public static class Token {

    private int start, end;
    private int symbol;


    public Token(int start, int end, int symbol) {

      this.start = start;
      this.end = end;
      this.symbol = symbol;
    }


    public int getStart() {

      return this.start;
    }


    public int getEnd() {

      return this.end;
    }


    public int getSymbol() {

      return this.symbol;
    }
  }


  /*
   * public Iterator parseScript(String script) throws IOException { return
   * parse(new StringReader(script), "$$SCRIPT$$"); }
   */

  public Iterator<Token> parseScript(Reader in)
      throws IOException {

    return this.parse(in, "$$SCRIPT$$");
  }


  public Iterator<Token> parseFunctions(Reader in)
      throws IOException {

    return this.parse(in, "$$FUNCTIONS$$");
  }


  public Iterator<Token> parseGrammar(Reader in)
      throws IOException {

    return this.parse(in, "$$SRGF$$");
  }


  private Iterator<Token> parse(Reader input, String prefix)
      throws IOException {

    final Lexer l = new Lexer(new PrefixedReader(input, prefix), true);
    l.setOffset(prefix.length());
    l.next_token(); // strip prefix token

    final java_cup.runtime.Symbol first = l.next_token();

    return new Iterator<Token>() {

      java_cup.runtime.Symbol symbol = first;


      public boolean hasNext() {

        return (this.symbol != null) && (this.symbol.sym != Sym.EOF);
      }


      public Token next() {

        if (this.symbol == null) {
          throw new NoSuchElementException();
        }

        int type;
        switch (this.symbol.sym) {
          case Sym.IF:
          case Sym.AS:
          case Sym.ELSE:
          case Sym.FOR:
          case Sym.WHILE:
          case Sym.DO:
          case Sym.RETURN:
          case Sym.SWITCH:
          case Sym.CASE:
          case Sym.DEFAULT:
          case Sym.BREAK:
          case Sym.PUBLIC:
          case Sym.PRIVATE:
          case Sym.REPEAT:
            type = SyntaxColor.KEYWORD;
            break;

          case Sym.UNDEF:
          case Sym.TRUE:
          case Sym.FALSE:
          case Sym.FLOAT:
          case Sym.INT:
          case Sym.RULENAME:
            type = SyntaxColor.CONSTANT;
            break;

          case Sym.STRING_TEXT:
            type = SyntaxColor.STRING;
            break;

          case Sym.COMMENT:
            type = SyntaxColor.COMMENT;
            break;

          case Sym.ERROR:
            type = SyntaxColor.ERROR;
            break;

          default:
            type = SyntaxColor.DEFAULT;
            break;
        }

        Token t = new Token(this.symbol.left, this.symbol.right, type);
        try {
          this.symbol = l.next_token();
        } catch (Exception exn) {
          this.symbol = null;
        }
        return t;
      }


      public void remove() {

        throw new UnsupportedOperationException();
      }
    };
  }
}
