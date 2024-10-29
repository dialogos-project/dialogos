/*
 * @(#)Lexer.java
 * Generated from script.lex by JFlex
 *
 * Copyright (c) 2002 CLT Sprachtechnologie GmbH.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CLT Sprachtechnologie GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CLT Sprachtechnologie GmbH.
 */
package com.clt.script.parser;

import java_cup.runtime.Symbol;
import java.util.*;
import java.io.Reader;
import java.text.MessageFormat;

@SuppressWarnings("unused")

%%
%{
	public String error_format = "Unexpected symbol: ''{0}'' in line {1} at position {2}";
	
	private int comment_count = 0;
	private int paren_count = 0;
	private int line_start = 0;
	private StringBuilder currentLine = new StringBuilder();
	private int offset = 0;
	private boolean syntaxColor = false;
	
	private StringBuilder buffer;
	private int stringbegin;
	private Stack<Integer> states = new Stack<Integer>();
	
	public Lexer(Reader r, boolean syntaxColor) {
		this(r);
		this.syntaxColor = syntaxColor;
	}
	
	void text() {
		currentLine.append(yytext());
	}

	private void newline(String newline) {
		line_start = getPos() + newline.length();
		currentLine = new StringBuilder();
		//System.out.println("state " + zzLexicalState + ": Line " + (yyline+1) + " starts at " + line_start);
	}
	
	char convertUni(String unistring) {
		return (char) Integer.parseInt(unistring,16);
	}
	char convertOctal(String octalstring) {
		return (char) Integer.parseInt(octalstring,8);
	}
	
	void yy_push(int state) {
		states.push(Integer.valueOf(yystate()));
		yybegin(state);
	}
	
	void yy_pop() {
		yybegin(states.pop().intValue());
	}
		
	public void setOffset(int offset) {
		this.offset = offset;
	}
	
	private int getPos() {
		return yychar - offset;
	}
	
	public class LineSymbol extends Symbol {
		int line;
		int line_start;
		String text;
		String linetext;
	
		public LineSymbol(int id, String text, int l, int r, int line, int line_start, Object value) {
			super(id, l, r, value);
			this.line = line;
			this.line_start = line_start;
			this.text = text;
			linetext = currentLine.toString();
		}
		
		public String toString() {
			return MessageFormat.format(error_format, new Object[] {
				text,
				Long.valueOf(line + 1),
				Long.valueOf(left - line_start + 1)
			}) + ":\n" + linetext;
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
  
	static class ParseException extends RuntimeException {
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
%state COMMENT1
%state COMMENT2
%state STRING
%state PATTERN
%state REGEXP
%state SRGF
%state SRGFHEADER
%state TAG


%eofval{
	if (syntaxColor) {
		int state = yystate();
		yybegin(YYINITIAL);
		if (state == STRING)
			return createSymbol(Sym.STRING_TEXT, "\"" + buffer.toString() + "\"", stringbegin, getPos(), buffer.toString());
		else if (state == REGEXP)
			return createSymbol(Sym.REGEXP, "\"" + buffer.toString() + "\"", stringbegin, getPos(), buffer.toString());
		else if (state == COMMENT1 || state == COMMENT2)
			return createSymbol(Sym.COMMENT, "", stringbegin, getPos(), "");
		else
			return createSymbol(Sym.EOF);
	}
	else
		return createSymbol(Sym.EOF);
%eofval}

%eof{
	if (!syntaxColor) {
		String lexicalEntity = null;
		int state = yystate();
		if (state == STRING)
			lexicalEntity = "string constant";
		else if (state == REGEXP)
			lexicalEntity = "regular expression";
		else if (state == COMMENT1)
			lexicalEntity = "comment";
		
		if (lexicalEntity != null)
			throw new ParseException("Unterminated " + lexicalEntity + " at "
								+ (yyline == 0 ? "" : ("line " + (yyline+1) + ", "))
								+ "position " + (getPos()-line_start+1) + ":\n" + currentLine);
	}
%eof}

%eofthrow{
	ParseException
%eofthrow} 

DIGIT=[0-9]
DIGITS=[0-9]+
NONNEWLINE_WHITE_SPACE_CHAR=[\ \t\b\u00a0]
NEWLINE=(\n\r)|(\r\n)|(\n)|(\r)

ALPHA=[A-Za-z\u00e4\u00c4\u00fc\u00dc\u00f6\u00d6\u00df]
IDSTART=({ALPHA}|_)
IDPART=({IDSTART}|{DIGIT})
ID={IDSTART}({IDPART})*

UNICODE=[a-fA-F0-9][a-fA-F0-9][a-fA-F0-9][a-fA-F0-9]
OCTALCODE=[0-3][0-7][0-7]

COMMENT_TEXT2=([^\n\r])*
RULENAME=(\$){ALPHA}({IDPART})*("."{IDPART}+)*
LHRULENAME="<"[^>]+">"
TOKEN=[^\""$()/+*;,=<>?{}[|]! "\t\n\r\b\u00a0]+
REPEAT="<"({DIGITS}((-){DIGIT}*)?)">"
PROBABILITY=\/{DIGITS}"."{DIGITS}\/
ABNF="#ABNF"

%%

<YYINITIAL> "$$EXPRESSION$$" { return createSymbol(Sym.__EXPRESSION__); }
<YYINITIAL> "$$PATTERN$$" { yy_push(PATTERN); return createSymbol(Sym.__PATTERN__); }
<YYINITIAL> "$$FUNCTIONS$$" { return createSymbol(Sym.__FUNCTIONS__); }
<YYINITIAL> "$$SCRIPT$$" { return createSymbol(Sym.__SCRIPT__); }
<YYINITIAL> "$$SRGF$$" { yy_push(SRGF); return createSymbol(Sym.__SRGF__); }
<YYINITIAL> "$$EXPANSION$$" { yy_push(SRGF); return createSymbol(Sym.__EXPANSION__); }


<SRGF>							{ABNF}		{ text(); yy_push(SRGFHEADER); buffer = new StringBuilder(); stringbegin=getPos(); }
<SRGFHEADER>					{NEWLINE}	{ newline(yytext()); yy_pop();
											  return createSymbol(Sym.ABNF, "#ABNF " + buffer.toString(),
														stringbegin, getPos(), buffer.toString());
											}
<SRGFHEADER>					.			{ text(); buffer.append(yytext());}

<SRGF>							{REPEAT}	{ text(); return createSymbol(Sym.REPEAT, yytext());}

<YYINITIAL,PATTERN,SRGF,TAG>	";"			{ text(); return createSymbol(Sym.SEMI); }
<YYINITIAL,PATTERN,SRGF,TAG>	","			{ text(); return createSymbol(Sym.COMMA); }
<YYINITIAL,PATTERN>				"{"			{ text(); return createSymbol(Sym.LPAREN); }
<YYINITIAL,PATTERN>				"}"			{ text(); return createSymbol(Sym.RPAREN); }
<SRGF,TAG>						"{"			{ text(); if(paren_count==0) yy_push(TAG);paren_count++;return createSymbol(Sym.LPAREN); }
<TAG>							"}"			{ text(); paren_count--; if(paren_count==0) yy_pop();return createSymbol(Sym.RPAREN); }

<YYINITIAL,PATTERN,SRGF,TAG>	"("			{ text(); return createSymbol(Sym.LRPAREN); }
<YYINITIAL,PATTERN,SRGF,TAG>	")"			{ text(); return createSymbol(Sym.RRPAREN); }
<YYINITIAL,PATTERN,SRGF,TAG>	"["			{ text(); return createSymbol(Sym.LEPAREN); }
<YYINITIAL,PATTERN,SRGF,TAG>	"]"			{ text(); return createSymbol(Sym.REPAREN); }
<YYINITIAL,PATTERN,TAG>			"+"			{ text(); return createSymbol(Sym.PLUS); }
<YYINITIAL,PATTERN,TAG>			"-"			{ text(); return createSymbol(Sym.MINUS); }
<YYINITIAL,TAG>					"*"			{ text(); return createSymbol(Sym.TIMES); }
<YYINITIAL,TAG>					"/"			{ text(); return createSymbol(Sym.DIVIDE); }
<YYINITIAL,TAG>					"%"			{ text(); return createSymbol(Sym.MOD); }
<YYINITIAL,PATTERN,TAG>			"#"			{ text(); return createSymbol(Sym.SHARP); }
<YYINITIAL,PATTERN,TAG>			"::"		{ text(); return createSymbol(Sym.CONS); }
<YYINITIAL,SRGF, TAG>			":"			{ text(); return createSymbol(Sym.COLON); }
<YYINITIAL,TAG>					"."			{ text(); return createSymbol(Sym.POINT); }
<YYINITIAL,TAG>					"->"		{ text(); return createSymbol(Sym.ARROW); }
<YYINITIAL,TAG>					"+="		{ text(); return createSymbol(Sym.PLUS_EQ); }
<YYINITIAL,TAG>					"-="		{ text(); return createSymbol(Sym.MINUS_EQ); }
<YYINITIAL,TAG>					"*="		{ text(); return createSymbol(Sym.TIMES_EQ); }
<YYINITIAL,TAG>					"/="		{ text(); return createSymbol(Sym.DIVIDE_EQ); }
<YYINITIAL,TAG>					"%="		{ text(); return createSymbol(Sym.MOD_EQ); }
<YYINITIAL>						"++"		{ text(); return createSymbol(Sym.PLUSPLUS); }
<YYINITIAL>						"--"		{ text(); return createSymbol(Sym.MINUSMINUS); }
<YYINITIAL,TAG>					"<"			{ text(); return createSymbol(Sym.LESSTHEN); }
<YYINITIAL,TAG>					"<="		{ text(); return createSymbol(Sym.EQUALLESS); }
<YYINITIAL,TAG>					">"			{ text(); return createSymbol(Sym.GREATERTHEN); }
<YYINITIAL,TAG>					">="		{ text(); return createSymbol(Sym.EQUALGREATER); }
<YYINITIAL,TAG>					"=="		{ text(); return createSymbol(Sym.EQUAL); }
<YYINITIAL,TAG>					"!="		{ text(); return createSymbol(Sym.NOTEQUAL); }
<YYINITIAL,TAG>					"&&"		{ text(); return createSymbol(Sym.AND); }
<YYINITIAL,TAG>					"||"		{ text(); return createSymbol(Sym.OR); }
<YYINITIAL,TAG>					"&"			{ text(); return createSymbol(Sym.BITAND); }
<YYINITIAL,TAG>					"|"			{ text(); return createSymbol(Sym.BITOR); }
<YYINITIAL,TAG>					"instanceof" { text(); return createSymbol(Sym.INSTANCEOF); }
<YYINITIAL,TAG>					"^"			{ text(); return createSymbol(Sym.XOR); }
<YYINITIAL,TAG>					"!"			{ text(); return createSymbol(Sym.NOT); }
<YYINITIAL,TAG>					"?"			{ text(); return createSymbol(Sym.QUESTIONMARK); }
<YYINITIAL,PATTERN,SRGF,TAG>	"="			{ text(); return createSymbol(Sym.IS);}
<YYINITIAL,PATTERN,TAG>			"undefined"	{ text(); return createSymbol(Sym.UNDEF); }
<YYINITIAL,PATTERN,TAG>			"true"		{ text(); return createSymbol(Sym.TRUE); }
<YYINITIAL,PATTERN,TAG>			"false"		{ text(); return createSymbol(Sym.FALSE); }

<PATTERN>						"as"		{ text(); return createSymbol(Sym.AS); }
<YYINITIAL>						"if"		{ text(); return createSymbol(Sym.IF); }
<YYINITIAL>						"else"		{ text(); return createSymbol(Sym.ELSE); }
<YYINITIAL>						"for"		{ text(); return createSymbol(Sym.FOR); }
<YYINITIAL>						"while"		{ text(); return createSymbol(Sym.WHILE); }
<YYINITIAL>						"do"		{ text(); return createSymbol(Sym.DO); }
<YYINITIAL>						"return"	{ text(); return createSymbol(Sym.RETURN); }
<YYINITIAL>						"switch"	{ text(); return createSymbol(Sym.SWITCH); }
<YYINITIAL>						"case"		{ text(); return createSymbol(Sym.CASE); }
<YYINITIAL>						"default"	{ text(); return createSymbol(Sym.DEFAULT); }
<YYINITIAL>						"break"		{ text(); return createSymbol(Sym.BREAK); }

<YYINITIAL,PATTERN,TAG>			{DIGITS}"."{DIGITS}	{ text(); return createSymbol(Sym.FLOAT, yytext()); }
<YYINITIAL,PATTERN,TAG,SRGF>	{DIGITS}	{ text(); return createSymbol(Sym.INT, yytext()); }

<SRGF,PATTERN>					"|"			{ text(); return createSymbol(Sym.OR);}
<SRGF>							"*"			{ text(); return createSymbol(Sym.TIMES); }
<SRGF>							"+"			{ text(); return createSymbol(Sym.PLUS); }
<SRGF>							"?"			{ text(); return createSymbol(Sym.QUESTIONMARK); }
<SRGF>							"::="		{ text(); return createSymbol(Sym.IS); }
<TAG>							"$"			{ text(); return createSymbol(Sym.DOLLAR); }
<SRGF,TAG>						{RULENAME}	{ text(); return createSymbol(Sym.RULENAME, yytext().substring(1)); }
<SRGF>							{LHRULENAME} { text(); return createSymbol(Sym.LHRULENAME, yytext().substring(1, yytext().length()-1)); }
<SRGF>							{PROBABILITY} { text(); return createSymbol(Sym.PROBABILITY, yytext().substring(1, yytext().length()-1)); }
<SRGF>							"!optional"	{ text(); return createSymbol(Sym.LH_OPTIONAL); }
<SRGF>							"!ignore"	{ text(); return createSymbol(Sym.LH_IGNORE); }
<SRGF>							"!action"	{ text(); return createSymbol(Sym.LH_ACTION); }
<SRGF>							"!repeat"	{ text(); return createSymbol(Sym.LH_REPEAT); }
<SRGF>							"!grammar"	{ text(); return createSymbol(Sym.LH_GRAMMAR); }
<SRGF>							"!language"	{ text(); return createSymbol(Sym.LH_LANGUAGE); }
<SRGF>							"!export"	{ text(); return createSymbol(Sym.LH_EXPORT); }
<SRGF>							"!class"	{ text(); return createSymbol(Sym.LH_CLASS); }
<SRGF>							"public"	{ text(); return createSymbol(Sym.PUBLIC);}
<SRGF>							"private"	{ text(); return createSymbol(Sym.PRIVATE);}
<SRGF>							"class"		{ text(); return createSymbol(Sym.CLASS);}
<SRGF>							{TOKEN}		{ text(); return createSymbol(Sym.TOKEN, yytext());}


<YYINITIAL,PATTERN,SRGF,TAG>	"/*"		{ text(); yy_push(COMMENT1); comment_count = 1; stringbegin=getPos(); }
<YYINITIAL,PATTERN,SRGF,TAG>	"\""		{ text(); yy_push(STRING); buffer = new StringBuilder(); stringbegin=getPos(); }
<STRING>						\\\\		{ text(); buffer.append("\\"); }
<STRING>						\\n			{ text(); buffer.append("\n"); }
<STRING>						\\u{UNICODE} { text(); buffer.append(convertUni(yytext().substring(2))); }
<STRING>						"\\\""		{ text(); buffer.append("\""); }
<STRING>						\\'			{ text(); buffer.append("'"); }
<STRING>						\\b			{ text(); buffer.append("\b"); }
<STRING>						\\t			{ text(); buffer.append("\t"); }
<STRING>						\\f			{ text(); buffer.append("\f"); }
<STRING>						\\r			{ text(); buffer.append("\r"); }
<STRING>						\\{OCTALCODE} { text(); buffer.append(convertOctal(yytext().substring(1))); }
<STRING>						\\.			{ throw new ParseException("Illegal escape sequence in " + (yyline+1) + " at position " +
																		(getPos()-line_start+1) + ": " + yytext() + ":\n" + currentLine); }
<STRING>						"\""		{ text(); yy_pop(); return createSymbol(Sym.STRING_TEXT, "\"" + buffer.toString() + "\"",
																					stringbegin, getPos()+1, buffer.toString()); }
<STRING>						{NEWLINE}	{ throw new ParseException("Unexpected end of line in string constant at "
																		+ (yyline == 0 ? "" : ("line " + (yyline+1) + ", "))
																		+ "position " + (getPos()-line_start+1) + ":\n" + currentLine); }
<STRING>						.			{ text(); buffer.append(yytext());}

<COMMENT1>						"/*"		{ text(); comment_count++; }
<COMMENT1>						"*/"		{ text();
										        comment_count--; 
										        if (comment_count == 0) {
										                yy_pop();
										        }
												if (syntaxColor)
													return createSymbol(Sym.COMMENT, "", stringbegin, getPos()+2, "");
											}
<COMMENT1>						.			{ text(); }
<COMMENT1>						{NEWLINE}	{ newline(yytext()); }


<YYINITIAL,PATTERN,SRGF,TAG>	"//"		{ text(); yy_push(COMMENT2); stringbegin=getPos(); }
<COMMENT2>						{COMMENT_TEXT2}	{ text(); }
<COMMENT2>						{NEWLINE}	{ newline(yytext());
												yy_pop();
												if (syntaxColor)
													return createSymbol(Sym.COMMENT, "", stringbegin, getPos(), "");
											}


<PATTERN>						"/"			{ text(); yy_push(REGEXP); buffer = new StringBuilder(); stringbegin=getPos(); }
<REGEXP>						"\\/"		{ text(); buffer.append("/"); }
<REGEXP>						"/"			{ text(); yy_pop(); return createSymbol(Sym.REGEXP, "/" + buffer.toString() + "/",
																					stringbegin, getPos()+1, buffer.toString()); }
<REGEXP>						{NEWLINE}	{ throw new ParseException("Unexpected end of line in regexp pattern at "
																	+ (yyline == 0 ? "" : ("line " + (yyline+1) + ", "))
																	+ "position " + (getPos()-line_start+1) + ":\n" + currentLine); }
<REGEXP>						.			{ text(); buffer.append(yytext());}


<YYINITIAL,PATTERN>				_			{ text(); return createSymbol(Sym.UNDERSCORE); }
<YYINITIAL,PATTERN,TAG>			{ID}		{ text(); return createSymbol(Sym.ID, yytext()); }



{NONNEWLINE_WHITE_SPACE_CHAR}	{ text(); }
{NEWLINE}						{ newline(yytext()); }

.	{
	text();
	if (syntaxColor) {
		return createSymbol(Sym.ERROR, yytext());
	}
	else {
		throw new ParseException(MessageFormat.format(error_format, new Object[] {
			yytext().substring(0, 1),
			Long.valueOf(yyline+1),
			Long.valueOf(getPos()-line_start+1)
		}) + ":\n" + currentLine);
	}
}
