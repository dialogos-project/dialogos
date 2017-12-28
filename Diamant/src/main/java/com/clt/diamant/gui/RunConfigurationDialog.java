/*
 * @(#)ScriptEditorDialog.java
 * Created on Sun Jun 27 2004
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

package com.clt.diamant.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.clt.diamant.Preferences;
import com.clt.diamant.Resources;
import com.clt.gui.CmdButton;
import com.clt.gui.GUI;
import com.clt.gui.table.TableEditor;

/**
 * Run configuration dialog
 * 
 * @author Till Kollenda
 * @version 1.0
 */

public class RunConfigurationDialog extends JDialog {

    private boolean _loggingEnabled;
    private File _loggingFile;

    /**
     * Opens the run configuration Dialog In this class the location of logging
     * files can be changed, and logging can be turned on or off
     * 
     * @param parent
     *            parent component
     * @param windowTitle
     *            Title of the Dialog
     * @param preferences Reference to the preferences
     */
    public RunConfigurationDialog(Component parent, String windowTitle, Preferences preferences) {

	super(GUI.getFrameForComponent(parent), windowTitle, true);

	_loggingEnabled = preferences.loggingEnabled.getValue();
	if (preferences.loggingDirectory.getValue() != null) {
	    _loggingFile = preferences.loggingDirectory.getValue();
	} else {
	    _loggingFile = new File("");
	}

	this.setResizable(false);
	JPanel p = new JPanel(new BorderLayout(6, 6));
	p.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
	
	JPanel content = new JPanel(new GridBagLayout());
	//content.setLayout(new BoxLayout(content, BoxLayout.PAGE_AXIS));
	GridBagConstraints gbc = new GridBagConstraints();
	gbc.gridx = 0;
	gbc.gridy = 0;
	gbc.insets = new Insets(2, 3, 2, 3);
	gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;

	JCheckBox logEnabledCheckBox = new JCheckBox(Resources.getString("EnableLogging"));
	logEnabledCheckBox.setSelected(preferences.loggingEnabled.getValue());
	logEnabledCheckBox.addItemListener(new CheckBoxListener());
	
	JLabel logPathLabel = new JLabel(Resources.getString("LoggingPath"));
	final JTextField logPathTextField = new JTextField(_loggingFile.getPath());
	JButton browseButton = new JButton(Resources.getString("Browse"));
	browseButton.addActionListener(new BrowseButtonListener(content, logPathTextField));

	p.add(content);
	gbc.gridx++;
	content.add(logEnabledCheckBox, gbc);
	gbc.gridx--;
	gbc.gridy++;
	gbc.anchor = GridBagConstraints.EAST;
	content.add(logPathLabel, gbc);
	gbc.weightx = 0.5;
	gbc.gridx++;
	gbc.fill = GridBagConstraints.HORIZONTAL;
	content.add(logPathTextField, gbc);
	gbc.gridy++;
	gbc.fill = GridBagConstraints.NONE;
	content.add(browseButton, gbc);

	CmdButton ok = new CmdButton(new Runnable() {

	    public void run() {
		// Update logging enabled and logging directory
		preferences.setLoggingEnabled(_loggingEnabled);
		File f = new File(logPathTextField.getText());
		if (f.exists() && f.isDirectory()) {
		    // Make sure the directory exists and is a directory
		    // before we save it to the preferences.
		    preferences.setLoggingDirectory(new File(logPathTextField.getText()));
		}
		RunConfigurationDialog.this.dispose();

	    }
	}, Resources.getString("OK"));
	CmdButton cancel = new CmdButton(new Runnable() {

	    public void run() {
		RunConfigurationDialog.this.dispose();
	    }
	}, Resources.getString("Cancel"));
	p.add(TableEditor.createButtonPanel(new JButton[0], new JButton[] { cancel, ok }), BorderLayout.SOUTH);

	this.setContentPane(p);
	GUI.setDefaultButtons(this, ok, cancel);
	GUI.assignMnemonics(this.getContentPane());
	this.pack();
	this.setSize(400, 200);
	this.setLocationRelativeTo(parent);
	this.setVisible(true);
    }

    // Listener for the logging enabled checkbox
    public class CheckBoxListener implements ItemListener {
	@Override
	public void itemStateChanged(ItemEvent e) {
	    if (e.getStateChange() == ItemEvent.SELECTED) {
		_loggingEnabled = true;
	    } else {
		_loggingEnabled = false;
	    }
	}
    }

    // Listener for the browser button
    public class BrowseButtonListener implements ActionListener {

	public Component parent;
	public JTextField fileField;

	BrowseButtonListener(Component parent, JTextField fileField) {
	    this.fileField = fileField;
	    this.parent = parent;
	}

	@Override

	// If clicked, open file chooser
	public void actionPerformed(ActionEvent arg0) {
	    JFileChooser fileChooser = new JFileChooser(_loggingFile);
	    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	    // JPanel content = new JPanel();
	    // content.add(fileChooser, BorderLayout.CENTER);
	    int returnValue = fileChooser.showOpenDialog(parent);
	    if (returnValue == JFileChooser.APPROVE_OPTION) {
		_loggingFile = fileChooser.getSelectedFile();
		fileField.setText(_loggingFile.getPath());
	    }
	}
    }
}
