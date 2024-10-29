package com.clt.speech.htk;

import java_cup.runtime.Symbol;
import java.util.*;
import java.text.*;
import com.clt.util.StringTools;

@SuppressWarnings("unused")

%%
%{
	static NumberFormat doubleFormat;

	static {
		doubleFormat = NumberFormat.getInstance(new Locale("en", "", ""));
	}

	public String error_format = "Unexpected symbol ''{0}'' in line {1} at position {2}";
	private int line_start = 0;
	private StringBuilder buffer;
	private Stack<Integer> states = new Stack<Integer>();
	
	public static String decode(String s) {
	    return StringTools.parseHTML(s);
	}
	
	public static String encode(String s) {
	    return StringTools.toHTML(s, false);
	}
	
	private Double createDouble(String s) {
		try {
			return Double.valueOf(doubleFormat.parse(s).doubleValue());
		}
		catch (Exception exn) {
			return Double.valueOf(s);
		}
	}
	
	private int getPos() {
		return yychar;
	}
	
	private void newline(String newline) {
		line_start = getPos() + newline.length();
		//System.out.println("state " + yy_lexical_state + ": Line " + (yyline+1) + " starts at " + line_start);
	}
	
	
	
	void yy_push(int state) {
		states.push(Integer.valueOf(yystate()));
		yybegin(state);
	}

	void yy_pop() {
		yybegin(states.pop().intValue());
	}

	public class LineSymbol extends Symbol {
		int line;
		int line_start;
		String text;
	
		public LineSymbol(int id, String text, int l, int r, int line, int line_start, Object value) {
			super(id, l, r, value);
			this.line = line;
			this.line_start = line_start;
			this.text = text;
		}
		
		public String toString() {
			return MessageFormat.format(error_format, new Object[] {
				text,
				Long.valueOf(line + 1),
				Long.valueOf(left - line_start + 1)
			});
		}
	}

	public Symbol createSymbol(int id) {
		return createSymbol(id, null);
	}

	public Symbol createSymbol(int id, Object value) {
		return createSymbol(id, id == Sym.EOF ? "EOF" : yytext(), getPos(), getPos()+yylength(), value);
	}
	
	public Symbol createSymbol(int id, String text, int start, int end, Object value) {
		//System.out.println("Symbol: " + yytext() + " (id " + id + ")");
		return new LineSymbol(id, text, start, end, yyline, line_start, value);
	}

	private static class ParseException extends RuntimeException {
		public ParseException(String s) {
			super(s);
		}
		public String toString() {
			return getLocalizedMessage();
		}
	}
%} 

%class Lexer
%cup
%unicode
%char
%line
%state STRING


%eofval{ 
return createSymbol(Sym.EOF); 
%eofval}


WHITESPACE  = [\t ]
INTEGER     = [0-9]+
DOUBLE      = -? {INTEGER} (\.{INTEGER})?
NAME        = [^\"\n\r\t ][^\n\r\t ]*
NEWLINE     = \r|\n|\r\n
ALTERNATIVE = "///"
HEADER      = "#!MLF!#"

%%

<YYINITIAL> {
    {INTEGER}     { return createSymbol(Sym.INT, Long.valueOf(yytext())); }
    {DOUBLE}      { return createSymbol(Sym.DBL, createDouble(yytext())); }


    "\""          { yy_push(STRING); buffer = new StringBuilder(); }
    "."           { return createSymbol(Sym.DOT); }
    {ALTERNATIVE} { return createSymbol(Sym.ALT); }

    {HEADER}      { return createSymbol(Sym.MLF); }
    {NEWLINE}     { newline(yytext()); return createSymbol(Sym.NL); }
    {WHITESPACE}+ { }

    {NAME}        { return createSymbol(Sym.NAME, yytext()); }
}


<STRING> {
    "\""          { yy_pop(); return createSymbol(Sym.STRING, buffer.toString()); }
    {NEWLINE}     { throw new RuntimeException("Unterminated string"); }
    .             { buffer.append(yytext()); }
}

.	{
		throw new ParseException(MessageFormat.format(error_format, new Object[] {
			yytext().substring(0, 1),
			Long.valueOf(yyline+1),
			Long.valueOf(getPos()-line_start+1)
		}));
	}
