/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.clt.dialogos.sphinx;

import com.clt.diamant.Device;
import com.clt.diamant.DialogInput;
import com.clt.diamant.ExecutionStoppedException;
import com.clt.diamant.InputCenter;
import com.clt.diamant.Resources;
import com.clt.diamant.graph.nodes.AbstractInputNode;
import com.clt.diamant.graph.nodes.NodeExecutionException;
import com.clt.gui.GUI;
import com.clt.gui.Passpartout;
import com.clt.gui.TextBox;
import com.clt.gui.border.GroupBorder;
import com.clt.script.exp.Pattern;
import com.clt.script.exp.Value;
import com.clt.script.exp.patterns.VarPattern;
import com.clt.speech.SpeechException;
import com.clt.speech.recognition.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import java.awt.event.ActionEvent;
import javax.swing.border.Border;

/**
 * @author koller
 */
public class SphinxNode extends AbstractInputNode {

    private static Device sphinxDevice = new Device(Resources.getString("Sphinx"));

    public SphinxNode() {
//        this.setProperty(THRESHOLD, new Long(40)); // TODO put this back in
    }

    private Sphinx getRecognizer() {
        return Plugin.getRecognizer();
    }

    @Override protected RecognitionExecutor createRecognitionExecutor(com.clt.srgf.Grammar recGrammar) {
        try {
            this.getRecognizer().stopRecognition();
        } catch (SpeechException exn) {
            throw new NodeExecutionException(this, Resources
                    .getString("RecognizerError")
                    + ".", exn);
        }
        return new SphinxRecognitionExecutor(getRecognizer(), getSettings());
    }

    @Override
    protected Device getDevice() {
        return sphinxDevice;
    }

    @Override
    protected List<LanguageName> getAvailableLanguages() {
        return getSettings() != null ? getSettings().getLanguages() : Plugin.getAvailableLanguages();
    }

    @Override
    protected LanguageName getDefaultLanguage() {
        assert this.getGraph() != null : "must not query default language when plugin settings are unreachable";
        return getSettings().getDefaultLanguage();
    }

    private Settings getSettings() {
        if (getGraph() != null && getGraph().getOwner() != null)
            return ((Settings) getGraph().getOwner()
                .getPluginSettings(Plugin.class));
        else
            return null;
    }
}
