package com.clt.dialogos.lego.nxt;

import java.util.Arrays;

import javax.swing.Icon;

import com.clt.dialogos.lego.nxt.nodes.MotorNode;
import com.clt.dialogos.lego.nxt.nodes.ProgramNode;
import com.clt.dialogos.lego.nxt.nodes.ReadSensorNode;
import com.clt.dialogos.lego.nxt.nodes.StopProgramNode;
import com.clt.dialogos.plugin.PluginSettings;
import com.clt.gui.Images;

/**
 * @author dabo
 *
 */
public class Plugin implements com.clt.dialogos.plugin.Plugin {

    @Override
    public void initialize() {
        com.clt.diamant.graph.Node.registerNodeTypes(this.getName(), Arrays.asList(new Class<?>[]{ProgramNode.class, StopProgramNode.class, ReadSensorNode.class, MotorNode.class}));
    }

    @Override
    public String getId() {
        return "dialogos.plugin.lego";
    }

    @Override
    public String getName() {
        return "Lego Mindstorms NXT";
    }

    @Override
    public Icon getIcon() {
        return Images.load(this, "Lego.png");
    }

    @Override
    public String getVersion() {
        return "2.0";
    }

    @Override
    public PluginSettings createDefaultSettings() {
        return new Settings();
    }
}
