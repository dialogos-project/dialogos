/*
 * @(#)TreesView.java
 * Created on 15.11.2006 by dabo
 *
 * Copyright (c) CLT Sprachtechnologie GmbH.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CLT Sprachtechnologie GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CLT Sprachtechnologie GmbH.
 */

package com.clt.dialog.client;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.clt.gui.Passpartout;
import com.clt.gui.border.GroupBorder;
import com.clt.speech.htk.MlfTreeView;
import com.clt.speech.recognition.RecognitionResult;
import com.clt.speech.recognition.Utterance;

/**
 * @author dabo
 * 
 */
public class TreesView
    extends JPanel {

  private JPanel trees;
  private List<Utterance> treeList = Collections.emptyList();
  private boolean showConfidences = true;
  private boolean showMarkers = true;


  public TreesView() {

    this.setLayout(new BorderLayout());
    this.trees = new JPanel();
    this.add(new JScrollPane(this.trees), BorderLayout.CENTER);

    JPanel options = new JPanel(new GridLayout(0, 1));
    final JCheckBox showConfidences = new JCheckBox("Show confidence values");
    showConfidences.setSelected(this.showConfidences);
    showConfidences.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent evt) {

        TreesView.this.showConfidences = showConfidences.isSelected();
        TreesView.this.show(TreesView.this.treeList);
      }
    });

    options.add(showConfidences);

    final JCheckBox showMarkers = new JCheckBox("Show audio timestamps");
    showMarkers.setSelected(this.showMarkers);
    showMarkers.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent evt) {

        TreesView.this.showMarkers = showMarkers.isSelected();
        TreesView.this.show(TreesView.this.treeList);
      }
    });

    options.add(showMarkers);

    this.add(options, BorderLayout.NORTH);

    this.setBorder(new GroupBorder("Semantic Trees"));
  }


  public void show(RecognitionResult utterance) {

    List<Utterance> trees = new ArrayList<Utterance>();
    if (utterance != null) {
      for (int i = 0; i < utterance.numAlternatives(); i++) {
        trees.add(utterance.getAlternative(i));
      }
    }
    this.show(trees);
  }


  public void show(List<Utterance> treeList) {

    this.trees.removeAll();
    this.treeList = treeList;
    if (treeList != null) {
      this.trees.setLayout(new GridLayout(treeList.size(), 1));
      for (int i = 0; i < treeList.size(); i++) {
        JComponent p =
          new Passpartout(new MlfTreeView(true, this.showConfidences,
            this.showMarkers,
                    treeList.get(i).getTree()));
        // JComponent p = new Passpartout(new TreeView(showConfidences,
        // utterance.getAlternative(i)));
        p.setBorder(new GroupBorder("Alternative " + (i + 1)));
        this.trees.add(p);
      }
    }
    this.validate();
    this.repaint();
  }
}