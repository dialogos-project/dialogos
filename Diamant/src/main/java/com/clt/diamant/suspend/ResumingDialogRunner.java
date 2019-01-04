/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.clt.diamant.suspend;

import com.clt.dialog.client.StdIOConnectionChooser;
import com.clt.dialogos.plugin.PluginLoader;
import com.clt.diamant.Executer;
import com.clt.diamant.ExecutionResult;
import com.clt.diamant.Preferences;
import com.clt.diamant.Resources;
import com.clt.diamant.SingleDocument;
import com.clt.diamant.WozInterface;
import com.clt.diamant.suspend.SuspendedExecutionResult;
import com.clt.gui.GUI;
import com.clt.util.Misc;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Runs a dialog in headless mode and suspends as needed.
 * 
 * @author koller
 */
public class ResumingDialogRunner<FromDialogos, ToDialogos> {

    private InputStream modelStream;
    private SingleDocument d;

    /**
     * Loads a dialog from the given input stream and initializes its
     * execution.
     * 
     * @param model
     * @throws IOException 
     */
    public ResumingDialogRunner(InputStream model) throws IOException {
        this.modelStream = model;
        
        // initialize preferences
        Preferences.getPrefs();

        // load plugins
        File appDir = Misc.getApplicationDirectory();
        PluginLoader.loadPlugins(appDir, e -> {
            GUI.invokeAndWait(() -> {
                String pluginName = e.getMessage();
                System.err.println(Resources.format("LoadingPluginX", pluginName));
            });
        });

        this.d = SingleDocument.loadFromStream(modelStream);
    }

    public SingleDocument getDocument() {
        return d;
    }
    
    

    /**
     * Runs the dialog from the given dialog state. The given inputValue
     * is passed as asynchronous input to the SuspendingNode that suspended
     * the execution of the dialog. If state is null, execution of the
     * dialog starts at the start node.<p>
     * 
     * If the dialog terminates successfully, the method returns null.
     * Otherwise, if the dialog was suspended by a SuspendingNode, the method
     * returns an object which bundles the dialog state with the prompt
     * (of type FromDialogos) the suspending node sent to the outside.
     * Execution of the dialog can be resumed by a subsequent call to
     * runUntilSuspend with the returned dialog state.
     * 
     * @param state
     * @param inputValue
     * @return
     * @throws Exception 
     */
    public SuspendedExecutionResult runUntilSuspend(DialogState state, ToDialogos inputValue) throws Exception {
        // resume suspended dialog
        if (state != null) {
            // send input value to node
            SuspendingNode<FromDialogos, ToDialogos> n = state.lookupNode(d.getOwnedGraph());
            n.resume(inputValue);

            // reset graph to correct state
            d.getOwnedGraph().resume(state);
        }

        if (d.connectDevices(new StdIOConnectionChooser(), Preferences.getPrefs().getConnectionTimeout())) {
            final WozInterface executer = new Executer(null, false);

            try {
                ExecutionResult result = d.run(null, executer);
            } catch (DialogSuspendedException exn) {
                // dialog was suspended
                return new SuspendedExecutionResult<FromDialogos>(exn.getDialogState(), (FromDialogos) exn.getPrompt());
            }
        }

        // dialog terminated by visiting an end node
        return null;
    }
}
