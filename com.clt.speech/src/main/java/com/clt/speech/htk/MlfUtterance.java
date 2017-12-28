package com.clt.speech.htk;

import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.clt.speech.recognition.AbstractRecognitionResult;
import com.clt.speech.recognition.AbstractUtterance;
import com.clt.speech.recognition.Utterance;
import com.clt.speech.recognition.Word;

public class MlfUtterance
    extends AbstractRecognitionResult {

  private String source;
  private boolean html;
  private MlfNonterminalNode vroots[];


  public MlfUtterance(String source, boolean html, MlfNonterminalNode vroots[]) {

    if (vroots.length < 1) {
      throw new IllegalArgumentException();
    }

    this.source = source;
    this.vroots = vroots;
    this.html = html;
  }


  public String getSource() {

    return this.source;
  }


  @Override
  public int numAlternatives() {

    return this.vroots.length;
  }


  @Override
  public Utterance getAlternative(int index) {

    MlfNonterminalNode root = this.getRoot(index);
    List<Word> words = this.getTerminalWords(index);

    return new AbstractUtterance(words, root.getConfidence());
  }


  public MlfNonterminalNode getRoot(int index) {

    return this.vroots[index];
  }


  public List<Word> getTerminalWords(int index) {

    List<Word> words = new ArrayList<Word>();
    this.collectWords(this.vroots[index], words);
    return words;
  }


  private void collectWords(final MlfNode n, Collection<Word> words) {

    if (n instanceof MlfTerminalNode) {
      words.add(new Word() {

        public String getWord() {

          return n.getLabel();
        }


        public float getConfidence() {

          return n.getConfidence();
        }


        @Override
        public long getStart() {

          return ((MlfTerminalNode)n).getStart();
        }


        @Override
        public long getEnd() {

          return ((MlfTerminalNode)n).getEnd();
        }
      });
    }
    else {
      MlfNonterminalNode nt = (MlfNonterminalNode)n;
      for (int i = 0; i < nt.numChildren(); i++) {
        this.collectWords(nt.getChild(i), words);
      }
    }
  }


  public void write(PrintWriter out) {

    out.println("\"" + this.getSource() + "\"");
    for (int i = 0; i < this.vroots.length; i++) {
      this.writeTree(out, this.vroots[i]);

      if (i == this.vroots.length - 1) {
        out.println(".");
      }
      else {
        out.println("///");
      }
    }
  }


  private void writeTree(PrintWriter out, MlfNonterminalNode tree) {

    List<List<String>> lines = new ArrayList<List<String>>();
    for (int i = 0; i < tree.numChildren(); i++) {
      this.writeNode(lines, tree.getChild(i), tree.getDepth() - 1);
    }

    for (List<String> line : lines) {
      for (Iterator<String> it2 = line.iterator(); it2.hasNext();) {
        out.print(it2.next());
        if (it2.hasNext()) {
          out.print(' ');
        }
      }
      out.println();
    }
  }


  private int writeNode(List<List<String>> lines, MlfNode node, int depth) {

    if ((node instanceof MlfNonterminalNode)
      && (((MlfNonterminalNode)node).numChildren() > 0)) {
      MlfNonterminalNode nt = (MlfNonterminalNode)node;

      int firstLine = this.writeNode(lines, nt.getChild(0), depth - 1);
      lines.get(firstLine).add(
        this.html ? Lexer.encode(nt.getLabel()) : nt.getLabel());
      for (int i = 1; i < nt.numChildren(); i++) {
        this.writeNode(lines, nt.getChild(i), depth - 1);
      }
      return firstLine;
    }
    else {
      List<String> line = new ArrayList<String>(depth);
      for (int i = 0; i < depth; i++) {
        line.add(this.html ? Lexer.encode(node.getLabel()) : node.getLabel());
      }
      lines.add(line);
      return lines.size() - 1;
    }
  }


  public static MlfUtterance[] parse(Reader r, boolean html)
      throws Exception {

    return MlfUtterance.parse(r, html, null);
  }


  public static MlfUtterance[] parse(Reader r, boolean html,
            Map<String, Map<List<String>, String>> substitutions)
      throws Exception {

    List<MlfUtterance> utterances = Parser.parse(r, html, substitutions);
    return utterances.toArray(new MlfUtterance[utterances.size()]);
  }
}